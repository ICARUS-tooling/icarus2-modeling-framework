/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.Category;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.Documentation;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.LocationType;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.api.TypedManifest;
import de.ims.icarus2.model.manifest.standard.Links.Link;
import de.ims.icarus2.util.Multiplicity;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.lang.ClassUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;

/**
 * @author Markus Gärtner
 *
 */
public class DriverManifestImpl extends AbstractForeignImplementationManifest<DriverManifest, ContextManifest>
		implements DriverManifest {

	private Optional<LocationType> locationType = Optional.empty();
	private final List<MappingManifest> mappingManifests = new ArrayList<>();
	private final List<ModuleSpec> moduleSpecs = new ArrayList<>();
	private final Map<String,Collection<ModuleManifest>> moduleManifests = new LinkedHashMap<>();

	public DriverManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, @Nullable ContextManifest contextManifest) {
		super(manifestLocation, registry, contextManifest, ContextManifest.class);
	}

	public DriverManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		super(manifestLocation, registry);
	}

	public DriverManifestImpl(ContextManifest contextManifest) {
		super(contextManifest, hostIdentity(), ContextManifest.class);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && mappingManifests.isEmpty()
				&& moduleSpecs.isEmpty() && moduleManifests.isEmpty();
	}

	@Override
	public void forEachMappingManifest(Consumer<? super MappingManifest> action) {
		if(hasTemplate()) {
			getTemplate().forEachMappingManifest(action);
		}

		mappingManifests.forEach(action);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#forEachLocalMappingManifest(java.util.function.Consumer)
	 */
	@Override
	public void forEachLocalMappingManifest(
			Consumer<? super MappingManifest> action) {
		mappingManifests.forEach(action);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#getLocationType()
	 */
	@Override
	public Optional<LocationType> getLocationType() {
		return getDerivable(locationType, DriverManifest::getLocationType);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#isLocalLocationType()
	 */
	@Override
	public boolean isLocalLocationType() {
		return locationType.isPresent();
	}

	@Override
	public void forEachModuleManifest(Consumer<? super ModuleManifest> action) {
		requireNonNull(action);

		if(hasTemplate()) {
			getTemplate().forEachModuleManifest(action);
		}

		for(Collection<ModuleManifest> manifests : moduleManifests.values()) {
			manifests.forEach(action);
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#forEachLocalModuleManifest(java.util.function.Consumer)
	 */
	@Override
	public void forEachLocalModuleManifest(
			Consumer<? super ModuleManifest> action) {
		requireNonNull(action);

		for(Collection<ModuleManifest> manifests : moduleManifests.values()) {
			manifests.forEach(action);
		}
	}

	@Override
	public void forEachModuleSpec(Consumer<? super ModuleSpec> action) {
		requireNonNull(action);

		if(hasTemplate()) {
			getTemplate().forEachModuleSpec(action);
		}

		moduleSpecs.forEach(action);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#forEachLocalModuleSpec(java.util.function.Consumer)
	 */
	@Override
	public void forEachLocalModuleSpec(Consumer<? super ModuleSpec> action) {
		requireNonNull(action);

		moduleSpecs.forEach(action);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#getModuleSpec(java.lang.String)
	 */
	@Override
	public Optional<ModuleSpec> getModuleSpec(final String specId) {
		requireNonNull(specId);

		ModuleSpec result = null;

		for(ModuleSpec spec : moduleSpecs) {
			if(specId.equals(spec.getId().orElse(null))) {
				result = spec;
				break;
			}
		}

		return getDerivable(Optional.ofNullable(result),
				t -> t.getModuleSpec(specId));
	}

	//TODO why do we keep this one?
	public Collection<ModuleManifest> getLocalModuleManifests(String moduleId) {
		requireNonNull(moduleId);

		LazyCollection<ModuleManifest> result = LazyCollection.lazyLinkedSet();

		result.addAll(moduleManifests.get(moduleId));

		return result.getAsSet();
	}

	@Override
	public Optional<MappingManifest> getMappingManifest(String id) {
		requireNonNull(id);

		MappingManifest result = null;

		for(MappingManifest manifest : mappingManifests) {
			if(id.equals(manifest.getId().orElse(null))) {
				result = manifest;
				break;
			}
		}

		return getDerivable(Optional.ofNullable(result),
				t -> t.getMappingManifest(id));
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#addMappingManifest(de.ims.icarus2.model.manifest.api.MappingManifest)
	 */
	@Override
	public DriverManifest addMappingManifest(MappingManifest mappingManifest) {
		checkNotLocked();

		addMappingManifest0(mappingManifest);

		return thisAsCast();
	}

	protected void addMappingManifest0(MappingManifest mappingManifest) {
		requireNonNull(mappingManifest);
//		checkNotLive();

		if(mappingManifests.contains(mappingManifest))
			throw new ManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID,
					"Duplicate mapping manifest: "+mappingManifest);

		mappingManifests.add(mappingManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#removeMappingManifest(de.ims.icarus2.model.manifest.api.MappingManifest)
	 */
	@Override
	public DriverManifest removeMappingManifest(MappingManifest mappingManifest) {
		checkNotLocked();

		removeMappingManifest0(mappingManifest);

		return thisAsCast();
	}

	protected void removeMappingManifest0(MappingManifest mappingManifest) {
		requireNonNull(mappingManifest);
//		checkNotLive();

		if(!mappingManifests.remove(mappingManifest))
			throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
					"Unknown mapping manifest: "+mappingManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#addModuleManifest(de.ims.icarus2.model.manifest.api.ModuleManifest)
	 */
	@Override
	public DriverManifest addModuleManifest(ModuleManifest moduleManifest) {
		checkNotLocked();

		addModuleManifest0(moduleManifest);

		return thisAsCast();
	}

	protected void addModuleManifest0(ModuleManifest moduleManifest) {
		requireNonNull(moduleManifest);
//		checkNotLive();

		String moduleId = moduleManifest.getId().orElseThrow(Manifest.invalidId(
				"Module manifest does not declare valid identifier"));

		Collection<ModuleManifest> manifests = moduleManifests.get(moduleId);

		if(manifests==null) {
			 manifests = new LinkedHashSet<>();
			 moduleManifests.put(moduleId, manifests);
		}

		if(manifests.contains(moduleManifest))
			throw new ManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID,
					"Duplicate module manifest: "+moduleManifest);

		manifests.add(moduleManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#removeModuleManifest(de.ims.icarus2.model.manifest.api.ModuleManifest)
	 */
	@Override
	public DriverManifest removeModuleManifest(ModuleManifest moduleManifest) {
		checkNotLocked();

		removeModuleManifest0(moduleManifest);

		return thisAsCast();
	}

	protected void removeModuleManifest0(ModuleManifest moduleManifest) {
		requireNonNull(moduleManifest);
//		checkNotLive();

		String moduleId = moduleManifest.getId().orElseThrow(Manifest.invalidId(
				"Module manifest does not declare valid identifier"));

		Collection<ModuleManifest> manifests = moduleManifests.get(moduleId);

		if(manifests==null || !manifests.remove(moduleManifest))
			throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
					"Unknown module manifest: "+moduleManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#addModuleManifest(de.ims.icarus2.model.manifest.api.addModuleSpec)
	 */
	@Override
	public DriverManifest addModuleSpec(ModuleSpec moduleSpec) {
		checkNotLocked();

		addModuleSpec0(moduleSpec);

		return thisAsCast();
	}

	protected void addModuleSpec0(ModuleSpec moduleSpec) {
		requireNonNull(moduleSpec);
//		checkNotLive();

		if(moduleSpecs.contains(moduleSpec))
			throw new ManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID,
					"Duplicate module spec: "+moduleSpec);

		moduleSpecs.add(moduleSpec);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#removeModuleManifest(de.ims.icarus2.model.manifest.api.removeModuleSpec)
	 */
	@Override
	public DriverManifest removeModuleSpec(ModuleSpec moduleSpec) {
		checkNotLocked();

		removeModuleSpec0(moduleSpec);

		return thisAsCast();
	}

	protected void removeModuleSpec0(ModuleSpec moduleSpec) {
		requireNonNull(moduleSpec);
//		checkNotLive();

		if(!moduleSpecs.remove(moduleSpec))
			throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
					"Unknown module spec: "+moduleSpec);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#setLocationType(de.ims.icarus2.model.io.LocationType)
	 */
	@Override
	public DriverManifest setLocationType(LocationType locationType) {
		checkNotLocked();

		setLocationType0(locationType);

		return thisAsCast();
	}

	protected void setLocationType0(LocationType locationType) {
		this.locationType = Optional.of(locationType);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractForeignImplementationManifest#lock()
	 */
	@Override
	protected void lockNested() {
		super.lockNested();

		lockNested(moduleSpecs);
		lockNested(mappingManifests);
		moduleManifests.values().forEach(this::lockNested);
	}

	public static class ModuleSpecImpl extends DefaultModifiableIdentity<ModuleSpec> implements ModuleSpec {

		private final DriverManifest driverManifest;
		private boolean customizable = DEFAULT_IS_CUSTOMIZABLE;
		private Optional<Multiplicity> multiplicity = Optional.empty();
		private Optional<String> extensionPointUid = Optional.empty();
		private Optional<String> moduleClass = Optional.empty();
		private Optional<Documentation> documentation = Optional.empty();

		private final Set<Category> categories = new ObjectOpenCustomHashSet<>(Category.HASH_STRATEGY);

		public ModuleSpecImpl(DriverManifest driverManifest) {
			this.driverManifest = requireNonNull(driverManifest);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.Embedded#getHost()
		 */
		@SuppressWarnings("unchecked")
		@Override
		public <T extends TypedManifest> Optional<T> getHost() {
			return (Optional<T>) Optional.of(driverManifest);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.DefaultModifiableIdentity#hashCode()
		 */
		@Override
		public int hashCode() {
			int hash = driverManifest.hashCode()+1;
			if(getId()!=null) {
				hash *= getId().hashCode();
			}
			return hash;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.DefaultModifiableIdentity#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} if(obj instanceof ModuleSpec) {
				ModuleSpec other = (ModuleSpec) obj;
				return ClassUtils.equals(getId(), other.getId());
			}
			return false;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.DefaultModifiableIdentity#toString()
		 */
		@Override
		public String toString() {
			return "ModuleSpec@"+ (getId()==null ? "<unnamed>" : getId()); //$NON-NLS-1$ //$NON-NLS-2$
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec#isCustomizable()
		 */
		@Override
		public boolean isCustomizable() {
			return customizable;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec#getExtensionPointUid()
		 */
		@Override
		public Optional<String> getExtensionPointUid() {
			return extensionPointUid;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec#getModuleClassName()
		 */
		@Override
		public Optional<String> getModuleClassName() {
			return moduleClass;
		}

		@Override
		public Multiplicity getMultiplicity() {
			return multiplicity.orElse(DEFAULT_MULTIPLICITY);
		}

		/**
		 * @return the documentation
		 */
		@Override
		public Optional<Documentation> getDocumentation() {
			return documentation;
		}

		/**
		 * @param documentation the documentation to set
		 */
		@Override
		public ModuleSpec setDocumentation(Documentation documentation) {
			checkNotLocked();

			setDocumentation0(documentation);

			return this;
		}

		protected void setDocumentation0(Documentation documentation) {
			this.documentation = Optional.ofNullable(documentation);
		}

		@Override
		public ModuleSpec setMultiplicity(Multiplicity multiplicity) {
			checkNotLocked();

			setMultiplicity0(multiplicity);

			return this;
		}

		protected void setMultiplicity0(Multiplicity multiplicity) {
			this.multiplicity = Optional.of(multiplicity);
		}

		/**
		 * @param customizable the customizable to set
		 */
		@Override
		public ModuleSpec setCustomizable(boolean customizable) {
			checkNotLocked();

			setCustomizable0(customizable);

			return this;
		}

		protected void setCustomizable0(boolean customizable) {
			this.customizable = customizable;
		}

		/**
		 * @param extensionPointUid the extensionPointUid to set
		 */
		@Override
		public ModuleSpec setExtensionPointUid(@Nullable String extensionPointUid) {
			checkNotLocked();

			setExtensionPointUid0(extensionPointUid);

			return this;
		}

		protected void setExtensionPointUid0(String extensionPointUid) {
			this.extensionPointUid = Optional.ofNullable(extensionPointUid);
		}

		/**
		 * @param moduleClassName the moduleClass to set
		 */
		@Override
		public ModuleSpec setModuleClassName(@Nullable String moduleClassName) {
			checkNotLocked();

			setModuleClassName0(moduleClassName);

			return this;
		}

		protected void setModuleClassName0(String moduleClassName) {
			this.moduleClass = Optional.ofNullable(moduleClassName);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.Categorizable#hasCategory(de.ims.icarus2.model.manifest.api.Category)
		 */
		@Override
		public boolean hasCategory(Category category) {
			return categories.contains(requireNonNull(category));
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.Categorizable#addCategory(de.ims.icarus2.model.manifest.api.Category)
		 */
		@Override
		public boolean addCategory(Category category) {
			checkNotLocked();

			return addCategory0(category);
		}

		protected boolean addCategory0(Category category) {
			requireNonNull(category);

			return categories.add(category);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.Categorizable#removeCategory(de.ims.icarus2.model.manifest.api.Category)
		 */
		@Override
		public boolean removeCategory(Category category) {
			checkNotLocked();

			return removeCategory0(category);
		}

		protected boolean removeCategory0(Category category) {
			requireNonNull(category);

			return categories.remove(category);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.Categorizable#forEachCategory(java.util.function.Consumer)
		 */
		@Override
		public void forEachCategory(Consumer<? super Category> action) {
			categories.forEach(action);
		}

	}

	public static class ModuleManifestImpl extends AbstractForeignImplementationManifest<ModuleManifest, DriverManifest>
			implements ModuleManifest {

		private ModuleSpecLink moduleSpec;

		public ModuleManifestImpl(ManifestLocation manifestLocation,
				ManifestRegistry registry, @Nullable DriverManifest driverManifest) {
			super(manifestLocation, registry, driverManifest, DriverManifest.class);
		}

		public ModuleManifestImpl(ManifestLocation manifestLocation,
				ManifestRegistry registry) {
			super(manifestLocation, registry);
		}

		public ModuleManifestImpl(DriverManifest driverManifest) {
			super(driverManifest, hostIdentity(), DriverManifest.class);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest#getModuleSpecId()
		 */
		@Override
		public Optional<ModuleSpec> getModuleSpec() {
			return Optional.ofNullable(moduleSpec)
					.flatMap(ModuleSpecLink::getOptional);
		}

		/**
		 * @param moduleSpecId the moduleSpecId to set
		 */
		@Override
		public ModuleManifest setModuleSpecId(String moduleSpecId) {
			checkNotLocked();

			setModuleSpecId0(moduleSpecId);

			return this;
		}

		protected void setModuleSpecId0(String moduleSpecId) {
			requireNonNull(moduleSpecId);

			moduleSpec = new ModuleSpecLink(moduleSpecId);
		}

		protected class ModuleSpecLink extends Link<ModuleSpec> {

			/**
			 * @param id
			 */
			public ModuleSpecLink(String id) {
				super(id);
			}

			/**
			 * @see de.ims.icarus2.model.manifest.standard.Links.Link#resolve()
			 */
			@Override
			protected Optional<ModuleSpec> resolve() {
				return getDriverManifest().flatMap(d -> d.getModuleSpec(getId()));
			}

		}
	}
}
