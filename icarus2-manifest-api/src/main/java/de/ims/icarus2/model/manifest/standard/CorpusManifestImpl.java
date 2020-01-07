/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ims.icarus2.model.manifest.standard;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.TypedManifest;
import de.ims.icarus2.model.manifest.util.ManifestUtils;

/**
 * @author Markus Gärtner
 *
 */
public class CorpusManifestImpl extends AbstractMemberManifest<CorpusManifest, TypedManifest> implements CorpusManifest {

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
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#getHost()
	 */
	@Override
	//TODO remove Embedded from the hierarchy of this class so we can get rid of this emthod and its side effects
	final public <T extends TypedManifest> Optional<T> getHost() {
		return Optional.empty();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && rootContextManifests.isEmpty()
				&& customContextManifests.isEmpty() && notes.isEmpty();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#setIsTemplate(boolean)
	 */
	@Override
	public CorpusManifest setIsTemplate(boolean isTemplate) {
		if(isTemplate)
			throw new ManifestException(ManifestErrorCode.MANIFEST_ILLEGAL_TEMPLATE_STATE,
					"Cannot declare corpus manifest as template");

		super.setIsTemplate(isTemplate);

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#setTemplateId(java.lang.String)
	 */
	@Override
	public CorpusManifest setTemplateId(String templateId) {
		throw new ManifestException(ManifestErrorCode.MANIFEST_ILLEGAL_TEMPLATE_STATE,
				"Corpus manifest does not support templating");
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

		String contextId = manifest.getId().orElseThrow(Manifest.invalidId(
				"Context does not declare a valid identifier"));

		if(contextManifestLookup.containsKey(contextId))
			throw new ManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID,
					"Duplicate context manifest: "+manifest);

		Collection<ContextManifest> storage = isRoot ? rootContextManifests : customContextManifests;

		/*
		 * If the given manifest should be added as root context we need to check whether this
		 * corpus is declared to be parallel. If it is not parallel the maximum number of root
		 * contexts is 1 and any attempt to add more will be met with an exception.
		 */
		if(isRoot && !isParallel() && !storage.isEmpty())
			throw new ManifestException(GlobalErrorCode.ILLEGAL_STATE,
					"Cannot add more than one context as root to non-parallel corpus: "+ManifestUtils.getName(manifest));

		contextManifestLookup.put(contextId, manifest);
		storage.add(manifest);
	}

	protected void removeContextManifest0(ContextManifest manifest, boolean isRoot) {
		requireNonNull(manifest);

		String id = manifest.getId().orElseThrow(Manifest.invalidId(
				"Missing id on manifest: "+ManifestUtils.getName(manifest)));
		if(contextManifestLookup.remove(id)!=manifest)
			throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
					"Unknown context manifest: "+manifest);


		Collection<ContextManifest> storage = isRoot ? rootContextManifests : customContextManifests;

		storage.remove(manifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#addRootContextManifest(de.ims.icarus2.model.manifest.api.ContextManifest)
	 */
	@Override
	public CorpusManifest addRootContextManifest(ContextManifest manifest) {
		checkNotLocked();

		addContextManifest0(manifest, true);

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#removeRootContextManifest(de.ims.icarus2.model.manifest.api.ContextManifest)
	 */
	@Override
	public CorpusManifest removeRootContextManifest(ContextManifest manifest) {
		checkNotLocked();

		removeContextManifest0(manifest, true);

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#isRootContext(de.ims.icarus2.model.manifest.api.ContextManifest)
	 */
	@Override
	public boolean isRootContext(ContextManifest manifest) {
		requireNonNull(manifest);

		return rootContextManifests.contains(manifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#getRootContextManifest()
	 */
	@Override
	public Optional<ContextManifest> getRootContextManifest() {
		return rootContextManifests.size()==1 ? Optional.of(rootContextManifests.get(0)) : Optional.empty();
	}

	/**
	 *
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#addCustomContextManifest(de.ims.icarus2.model.manifest.api.ContextManifest)
	 */
	@Override
	public CorpusManifest addCustomContextManifest(ContextManifest manifest) {
		checkNotLocked();

		addContextManifest0(manifest, false);

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#removeCustomContextManifest(de.ims.icarus2.model.manifest.api.ContextManifest)
	 */
	@Override
	public CorpusManifest removeCustomContextManifest(ContextManifest manifest) {
		checkNotLocked();

		removeContextManifest0(manifest, false);

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#isCustomContext(de.ims.icarus2.model.manifest.api.ContextManifest)
	 */
	@Override
	public boolean isCustomContext(ContextManifest manifest) {
		requireNonNull(manifest);

		return customContextManifests.contains(manifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#getContextManifest(java.lang.String)
	 */
	@Override
	public Optional<ContextManifest> getContextManifest(String id) {
		requireNonNull(id);

		return Optional.ofNullable(contextManifestLookup.get(id));
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
	public CorpusManifest setEditable(boolean value) {
		checkNotLocked();

		setEditable0(value);

		return this;
	}

	protected void setEditable0(boolean value) {

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
	public CorpusManifest setParallel(boolean value) {
		checkNotLocked();

		setParallel0(value);

		return this;
	}

	protected void setParallel0(boolean value) {
		if(rootContextManifests.size()>1)
			throw new ManifestException(GlobalErrorCode.ILLEGAL_STATE,
					"Cannot change 'parallel' property of corpus once more than one root context is present");

		this.parallel = (value==DEFAULT_PARALLEL_VALUE) ? null : Boolean.valueOf(value);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#addNote(de.ims.icarus2.model.manifest.api.CorpusManifest.Note)
	 */
	@Override
	public CorpusManifest addNote(Note note) {
		checkNotLocked();

		addNote0(note);

		return this;
	}

	protected void addNote0(Note note) {
		requireNonNull(note);

		if(notes.contains(note))
			throw new ManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID, "Duplicate note: "+note);

		notes.add(note);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CorpusManifest#removeNote(de.ims.icarus2.model.manifest.api.CorpusManifest.Note)
	 */
	@Override
	public CorpusManifest removeNote(Note note) {
		checkNotLocked();

		removeNote0(note);

		return this;
	}

	protected void removeNote0(Note note) {
		requireNonNull(note);

		if(!notes.contains(note))
			throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID, "Unknown note: "+note);

		notes.remove(note);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#lock()
	 */
	@Override
	protected void lockNested() {
		super.lockNested();

		lockNested(customContextManifests);
	}

	public static class NoteImpl implements Note {

		private final String name;
		private Optional<LocalDateTime> modificationDate = Optional.empty();
		private Optional<String> content = Optional.empty();

		public NoteImpl(String name) {
			this.name = requireNonNull(name);
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
		public Optional<LocalDateTime> getModificationDate() {
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
		public Optional<String> getContent() {
			return content;
		}

		/**
		 * @param modificationDate the modificationDate to set
		 */
		public void setModificationDate(LocalDateTime modificationDate) {
			this.modificationDate = Optional.of(modificationDate);
		}

		/**
		 * @param content the content to set
		 */
		@Override
		public Note setContent(String content) {
			changeContent(content);
			setModificationDate(LocalDateTime.now());
			return this;
		}

		/**
		 * Similar to {@link #setContent(String)} but does not change the modification
		 * date timestamp!
		 * @param content
		 */
		public void changeContent(String content) {
			this.content = Optional.ofNullable(content);
		}

	}
}
