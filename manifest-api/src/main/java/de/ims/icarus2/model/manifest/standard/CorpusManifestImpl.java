/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 447 $
 * $Date: 2016-01-14 11:34:47 +0100 (Do, 14 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/manifest/CorpusManifestImpl.java $
 *
 * $LastChangedDate: 2016-01-14 11:34:47 +0100 (Do, 14 Jan 2016) $
 * $LastChangedRevision: 447 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.standard;

import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.standard.Links.Link;

/**
 * @author Markus Gärtner
 * @version $Id: CorpusManifestImpl.java 447 2016-01-14 10:34:47Z mcgaerty $
 *
 */
public class CorpusManifestImpl extends AbstractMemberManifest<CorpusManifest> implements CorpusManifest {

	private ContextLink rootContext;
	private final List<ContextManifest> contextManifests = new ArrayList<>(3);
	private final Map<String, ContextManifest> contextManifestLookup = new HashMap<>();
	private boolean editable;
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
		return super.isEmpty() && contextManifests.isEmpty() && notes.isEmpty();
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
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#getRootContextManifest()
	 */
	@Override
	public ContextManifest getRootContextManifest() {
		return rootContext.get();
	}

	/**
	 * Defines the {@code id} of the context that should be used as root context.
	 * Note that the context itself has to be added separately.
	 *
	 * @param rootContextManifest the rootContextManifest to set
	 */
	@Override
	public void setRootContextId(String rootContextId) {
		checkNotNull(rootContextId);

		rootContext = new ContextLink(rootContextId);
	}

	@Override
	public void forEachCustomContextManifest( Consumer<? super ContextManifest> action) {
		contextManifests.forEach(action);
	}

	/**
	 *
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#addCustomContextManifest(de.ims.icarus2.model.manifest.api.ContextManifest)
	 */
	@Override
	public void addCustomContextManifest(ContextManifest manifest) {
		checkNotNull(manifest);
		manifest.checkNotTemplate();

		if(contextManifestLookup.containsKey(manifest.getId()))
			throw new IllegalArgumentException("Duplicate context manifest: "+manifest); //$NON-NLS-1$

		contextManifestLookup.put(manifest.getId(), manifest);
		contextManifests.add(manifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#removeCustomContextManifest(de.ims.icarus2.model.manifest.api.ContextManifest)
	 */
	@Override
	public void removeCustomContextManifest(ContextManifest manifest) {
		checkNotNull(manifest);

		boolean isRootContext = manifest==getRootContextManifest();

		if(isRootContext && contextManifests.size()>1)
			throw new IllegalArgumentException("Cannot remove root context with more than 1 context in corpus!"); //$NON-NLS-1$

		if(!contextManifests.remove(manifest))
			throw new IllegalArgumentException("Unknown context manifest: "+manifest); //$NON-NLS-1$
		contextManifestLookup.remove(manifest.getId());

		if(isRootContext) {
			rootContext = null;
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#getContextManifest(java.lang.String)
	 */
	@Override
	public ContextManifest getContextManifest(String id) {
		checkNotNull(id);

		ContextManifest contextManifest = contextManifestLookup.get(id);
		if(contextManifest==null && rootContext.get().getId().equals(id)) {
			contextManifest = rootContext.get();
		}
		if(contextManifest==null)
			throw new IllegalArgumentException("No such context: "+id); //$NON-NLS-1$

		return contextManifest;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#isEditable()
	 */
	@Override
	public boolean isEditable() {
		return editable;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#setEditable(boolean)
	 */
	@Override
	public void setEditable(boolean value) {
		this.editable = value;
	}

	@Override
	public void forEachNote(Consumer<? super Note> action) {
		notes.forEach(action);
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

	protected class ContextLink extends Link<ContextManifest> {

		/**
		 * @param id
		 */
		public ContextLink(String id) {
			super(id);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.Links.Link#resolve()
		 */
		@Override
		protected ContextManifest resolve() {
			return getContextManifest(getId());
		}

	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#lock()
	 */
	@Override
	public void lock() {
		super.lock();

		for(ContextManifest contextManifest : contextManifests) {
			contextManifest.lock();
		}
	}

	public static class NoteImpl implements Note {

		private Date modificationDate;
		private final String name;
		private String content;

		public NoteImpl(String name) {
			checkNotNull(name);

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
