/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 */
package de.ims.icarus2.model.manifest.standard;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.util.ManifestUtils;

/**
 * @author Markus Gärtner
 *
 */
public class CorpusManifestImpl extends AbstractMemberManifest<CorpusManifest> implements CorpusManifest {

	private final List<ContextManifest> rootContextManifests = new ArrayList<>(3);
	private final List<ContextManifest> customContextManifests = new ArrayList<>(5);
	private final Map<String, ContextManifest> contextManifestLookup = new HashMap<>();
	private Boolean editable;
	private Boolean parallel;
	private final List<Note> notes = new ArrayList<>();

	/**
	 * @param manifestLocation
	 * @param registry
	 */
	public CorpusManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		super(manifestLocation, registry);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && rootContextManifests.isEmpty() && customContextManifests.isEmpty() && notes.isEmpty();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#setIsTemplate(boolean)
	 */
	@Override
	public void setIsTemplate(boolean isTemplate) {
		if(isTemplate)
			throw new ManifestException(ManifestErrorCode.MANIFEST_ILLEGAL_TEMPLATE_STATE,
					"Cannot declare corpus manifest as template"); //$NON-NLS-1$

		super.setIsTemplate(isTemplate);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.CORPUS_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#forEachRootContextManifest(java.util.function.Consumer)
	 */
	@Override
	public void forEachRootContextManifest(Consumer<? super ContextManifest> action) {
		rootContextManifests.forEach(action);
	}

	@Override
	public void forEachCustomContextManifest( Consumer<? super ContextManifest> action) {
		customContextManifests.forEach(action);
	}

	protected void addContextManifest0(ContextManifest manifest, boolean isRoot) {
		requireNonNull(manifest);
//		manifest.checkNotTemplate();

		if(contextManifestLookup.containsKey(manifest.getId()))
			throw new IllegalArgumentException("Duplicate context manifest: "+manifest); //$NON-NLS-1$

		Collection<ContextManifest> storage = isRoot ? rootContextManifests : customContextManifests;

		/*
		 * If the given manifest should be added as root context we need to check whether this
		 * corpus is declared to be parallel. If it is not parallel the maximum number of root
		 * contexts is 1 and any attempt to add more will be met with an exception.
		 */
		if(isRoot && !isParallel() && !storage.isEmpty())
			throw new ManifestException(GlobalErrorCode.ILLEGAL_STATE,
					"Cannot add more than one context as root to non-parallel corpus: "+ManifestUtils.getName(manifest));

		contextManifestLookup.put(manifest.getId(), manifest);
		storage.add(manifest);
	}

	protected void removeContextManifest0(ContextManifest manifest, boolean isRoot) {
		requireNonNull(manifest);

		if(!customContextManifests.remove(manifest))
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT, "Unknown context manifest: "+manifest); //$NON-NLS-1$


		Collection<ContextManifest> storage = isRoot ? rootContextManifests : customContextManifests;

		storage.remove(manifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#addRootContextManifest(de.ims.icarus2.model.manifest.api.ContextManifest)
	 */
	@Override
	public void addRootContextManifest(ContextManifest manifest) {
		addContextManifest0(manifest, true);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#removeRootContextManifest(de.ims.icarus2.model.manifest.api.ContextManifest)
	 */
	@Override
	public void removeRootContextManifest(ContextManifest manifest) {
		removeContextManifest0(manifest, true);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#isRootContext(de.ims.icarus2.model.manifest.api.ContextManifest)
	 */
	@Override
	public boolean isRootContext(ContextManifest manifest) {
		return rootContextManifests.contains(manifest);
	}

	/**
	 *
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#addCustomContextManifest(de.ims.icarus2.model.manifest.api.ContextManifest)
	 */
	@Override
	public void addCustomContextManifest(ContextManifest manifest) {
		addContextManifest0(manifest, false);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#removeCustomContextManifest(de.ims.icarus2.model.manifest.api.ContextManifest)
	 */
	@Override
	public void removeCustomContextManifest(ContextManifest manifest) {
		removeContextManifest0(manifest, false);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#isCustomContext(de.ims.icarus2.model.manifest.api.ContextManifest)
	 */
	@Override
	public boolean isCustomContext(ContextManifest manifest) {
		return customContextManifests.contains(manifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#getContextManifest(java.lang.String)
	 */
	@Override
	public ContextManifest getContextManifest(String id) {
		requireNonNull(id);

		ContextManifest contextManifest = contextManifestLookup.get(id);
		//FIXME reevaluate decision to remove exception in case of unknown id to stay consistent with layer group and context level lookups
//		if(contextManifest==null)
//			throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID, "No such context: "+id); //$NON-NLS-1$

		return contextManifest;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#isEditable()
	 */
	@Override
	public boolean isEditable() {
		return editable==null ? DEFAULT_EDITABLE_VALUE : editable.booleanValue();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#setEditable(boolean)
	 */
	@Override
	public void setEditable(boolean value) {

		this.editable = (value==DEFAULT_EDITABLE_VALUE) ? null : Boolean.valueOf(value);
	}

	@Override
	public void forEachNote(Consumer<? super Note> action) {
		notes.forEach(action);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#isParallel()
	 */
	@Override
	public boolean isParallel() {
		return parallel==null ? DEFAULT_PARALLEL_VALUE : parallel.booleanValue();
	}

	/**
	 *
	 * @throws ManifestException if there is more than 1 root context already defined for this corpus
	 *
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#setParallel(boolean)
	 */
	@Override
	public void setParallel(boolean value) {
		if(rootContextManifests.size()>1)
			throw new ManifestException(GlobalErrorCode.ILLEGAL_STATE,
					"Cannot change 'parallel' property of corpus once more than one root context is present");

		this.parallel = (value==DEFAULT_PARALLEL_VALUE) ? null : Boolean.valueOf(value);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#addNote(de.ims.icarus2.model.manifest.api.CorpusManifest.Note)
	 */
	@Override
	public void addNote(Note note) {
		if(!notes.contains(note)) {
			notes.add(note);
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#removeNote(de.ims.icarus2.model.manifest.api.CorpusManifest.Note)
	 */
	@Override
	public void removeNote(Note note) {
		notes.remove(note);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#lock()
	 */
	@Override
	public void lock() {
		super.lock();

		for(ContextManifest contextManifest : customContextManifests) {
			contextManifest.lock();
		}
	}

	//FIXME removed due to explicit storage of added contexts instead of links
//	protected class ContextLink extends Link<ContextManifest> {
//
//		/**
//		 * @param id
//		 */
//		public ContextLink(String id) {
//			super(id);
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.manifest.standard.Links.Link#resolve()
//		 */
//		@Override
//		protected ContextManifest resolve() {
//			return getContextManifest(getId());
//		}
//
//	}

	public static class NoteImpl implements Note {

		private Date modificationDate;
		private final String name;
		private String content;

		public NoteImpl(String name) {
			requireNonNull(name);

			this.name = name;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} else if(obj instanceof Note) {
				return name.equals(((Note)obj).getName());
			}

			return false;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return name.hashCode();
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.CorpusManifest.Note#getModificationDate()
		 */
		@Override
		public Date getModificationDate() {
			return modificationDate;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.CorpusManifest.Note#getName()
		 */
		@Override
		public String getName() {
			return name;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.CorpusManifest.Note#getContent()
		 */
		@Override
		public String getContent() {
			return content;
		}

		/**
		 * @param modificationDate the modificationDate to set
		 */
		public void setModificationDate(Date modificationDate) {
			if (modificationDate == null)
				throw new NullPointerException("Invalid modificationDate"); //$NON-NLS-1$

			this.modificationDate = modificationDate;
		}

		/**
		 * @param content the content to set
		 */
		@Override
		public void setContent(String content) {
			changeContent(content);
			modificationDate = new Date();
		}

		/**
		 * Similar to {@link #setContent(String)} but does not change the modification
		 * date timestamp!
		 * @param content
		 */
		public void changeContent(String content) {
			if (content == null)
				throw new NullPointerException("Invalid content"); //$NON-NLS-1$

			this.content = content;
		}

	}
}
