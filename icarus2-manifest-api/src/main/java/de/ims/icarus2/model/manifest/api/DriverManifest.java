/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.api;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ims.icarus2.util.Multiplicity;
import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;
import de.ims.icarus2.util.collections.LazyCollection;

/**
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface DriverManifest extends ForeignImplementationManifest<DriverManifest>, Embedded {

	@AccessRestriction(AccessMode.READ)
	void forEachMappingManifest(Consumer<? super MappingManifest> action);

	@AccessRestriction(AccessMode.READ)
	void forEachLocalMappingManifest(Consumer<? super MappingManifest> action);

	/**
	 * Returns manifests describing all the mappings that should be created for this
	 * context.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	default List<MappingManifest> getMappingManifests() {
		LazyCollection<MappingManifest> result = LazyCollection.lazyList();

		forEachMappingManifest(result);

		return result.getAsList();
	}

	@AccessRestriction(AccessMode.READ)
	default List<MappingManifest> getLocalMappingManifests() {
		LazyCollection<MappingManifest> result = LazyCollection.lazyList();

		forEachLocalMappingManifest(result);

		return result.getAsList();
	}

	/**
	 * Allows lookup of mapping manifests by their respective ids.
	 *
	 * @param id
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<MappingManifest> getMappingManifest(String id);

	/**
	 * Returns a hint on which type of resources the driver is depending to access
	 * corpus data.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<LocationType> getLocationType();

	boolean isLocalLocationType();

	/**
	 *  For live driver manifests this method returns the manifest describing the
	 *  surrounding {@code Context}. For templates the return value is always empty.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	default <M extends ContextManifest> Optional<M> getContextManifest() {
		return getHost();
	}

	/**
	 * Applies the given {@code action} to each {@code ModuleManifest} accessible via
	 * this driver manifest. The implementation should apply the action to locally defined
	 * manifests first and then recursively continue with templates, if present.
	 *
	 * @param action
	 */
	@AccessRestriction(AccessMode.READ)
	void forEachModuleManifest(Consumer<? super ModuleManifest> action);

	@AccessRestriction(AccessMode.READ)
	void forEachLocalModuleManifest(Consumer<? super ModuleManifest> action);

	@AccessRestriction(AccessMode.READ)
	default Set<ModuleManifest> getModuleManifests() {
		LazyCollection<ModuleManifest> result = LazyCollection.lazyLinkedSet();

		forEachModuleManifest(result);

		return result.getAsSet();
	}

	@AccessRestriction(AccessMode.READ)
	default Set<ModuleManifest> getLocalModuleManifests() {
		LazyCollection<ModuleManifest> result = LazyCollection.lazyLinkedSet();

		forEachLocalModuleManifest(result);

		return result.getAsSet();
	}

	/**
	 * Applies the given {@code action} to each {@code ModuleSpec} accessible via
	 * this driver manifest. The implementation should apply the action to locally defined
	 * specs first and then recursively continue with templates, if present.
	 *
	 * @param action
	 */
	@AccessRestriction(AccessMode.READ)
	void forEachModuleSpec(Consumer<? super ModuleSpec> action);

	@AccessRestriction(AccessMode.READ)
	void forEachLocalModuleSpec(Consumer<? super ModuleSpec> action);

	@AccessRestriction(AccessMode.READ)
	default Set<ModuleSpec> getModuleSpecs() {
		LazyCollection<ModuleSpec> result = LazyCollection.lazyLinkedSet();

		forEachModuleSpec(result);

		return result.getAsSet();
	}

	@AccessRestriction(AccessMode.READ)
	default Set<ModuleSpec> getLocalModuleSpecs() {
		LazyCollection<ModuleSpec> result = LazyCollection.lazyLinkedSet();

		forEachLocalModuleSpec(result);

		return result.getAsSet();
	}

	/**
	 * Returns the {@code ModuleSpec} that is mapped to the given {@code id}. Note that
	 * module spec ids are required to be unique in the scope of a single driver manifest
	 * and that locally defined specs will shadow those inherited from templates.
	 *
	 * @param specId
	 * @return
	 */
	Optional<ModuleSpec> getModuleSpec(String specId);

	default Set<ModuleManifest> getModuleManifests(String moduleId) {
		requireNonNull(moduleId);

		LazyCollection<ModuleManifest> result = LazyCollection.lazySet();

		forEachModuleManifest(m -> {
			if(moduleId.equals(m.getId().orElse(null)))
				result.add(m);
		});

		return result.getAsSet();
	}

	default Set<ModuleManifest> getModuleManifests(ModuleSpec moduleSpec) {
		requireNonNull(moduleSpec);

		LazyCollection<ModuleManifest> result = LazyCollection.lazySet();

		forEachModuleManifest(m -> {
			if(m.getModuleSpec().orElse(null)==moduleSpec)
				result.add(m);
		});

		return result.getAsSet();
	}

	// Modification methods

	void addMappingManifest(MappingManifest mappingManifest);
	void removeMappingManifest(MappingManifest mappingManifest);

	void addModuleManifest(ModuleManifest moduleManifest);
	void removeModuleManifest(ModuleManifest moduleManifest);

	void addModuleSpec(ModuleSpec moduleSpec);
	void removeModuleSpec(ModuleSpec moduleSpec);

	void setLocationType(LocationType locationType);

	/**
	 * Describes a module this driver manifest is depending on. A driver can
	 * contain an arbitrary number of {@code ModuleSpec} declarations. Each
	 * module spec represents a pluggable part of the driver that (optionally)
	 * can be customized by the user.
	 * <p>
	 * Note that {@code ModuleSpec} ids are required to be unique within a single
	 * driver manifest's scope. Therefore declaring module specs in a derived manifest
	 * effectively shadows previous declarations in the original template!
	 *
	 * @author Markus Gärtner
	 *
	 */
	@AccessControl(AccessPolicy.DENY)
	public interface ModuleSpec extends ModifiableIdentity, Categorizable<ModuleSpec>,
			ManifestFragment, Documentable<ModuleSpec>, Embedded {

		public static final boolean DEFAULT_IS_CUSTOMIZABLE = false;
		public static final Multiplicity DEFAULT_MULTIPLICITY = Multiplicity.ONE;

		@AccessRestriction(AccessMode.READ)
		default <M extends DriverManifest> Optional<M> getDriverManifest() {
			return getHost();
		}


		/**
		 * @see de.ims.icarus2.model.manifest.api.TypedManifest#getManifestType()
		 */
		@Override
		default public ManifestType getManifestType() {
			return ManifestType.MODULE_SPEC;
		}

		/**
		 * Specifies whether or not the described module can be customized
		 * by the user. When a module is customizable it will show up in the
		 * options dialog for a driver. Non-customizable modules are final
		 * upon their manifest declaration. For example in the specification
		 * of a certain format the module describing the file connector will
		 * probably be non-customizable, while additional language specific modules
		 * will be left for the user to customize.
		 * <p>
		 * The default is {@value #DEFAULT_IS_CUSTOMIZABLE}.
		 *
		 * @return
		 *
		 * @see #DEFAULT_IS_CUSTOMIZABLE
		 */
		@AccessRestriction(AccessMode.READ)
		boolean isCustomizable();

		/**
		 *
		 * @return
		 *
		 * @see #DEFAULT_MULTIPLICITY
		 */
		@AccessRestriction(AccessMode.READ)
		Multiplicity getMultiplicity();

		/**
		 * Specifies an extension-point from which to load connected extensions. Those
		 * extensions then describe the legal implementations that can be used for this module.
		 * <p>
		 * Note that when no extension-point uid is defined, the module spec must have exactly one
		 * matching {@link ModuleManifest} present in the scope of the surrounding driver manifest.
		 *
		 * TODO
		 *
		 * @return
		 */
		@AccessRestriction(AccessMode.READ)
		Optional<String> getExtensionPointUid();

		// Modification methods

		void setMultiplicity(Multiplicity multiplicity);
		void setCustomizable(boolean customizable);
		void setExtensionPointUid(@Nullable String extensionPointUid);
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	@AccessControl(AccessPolicy.DENY)
	public interface ModuleManifest extends ForeignImplementationManifest<ModuleManifest>, Embedded {

		@AccessRestriction(AccessMode.READ)
		default <M extends DriverManifest> Optional<M> getDriverManifest() {
			return getHost();
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.TypedManifest#getManifestType()
		 */
		@Override
		default public ManifestType getManifestType() {
			return ManifestType.MODULE_MANIFEST;
		}

		/**
		 * Returns the {@link ModuleSpec} that describes this module.
		 * @return
		 */
		@AccessRestriction(AccessMode.READ)
		Optional<ModuleSpec> getModuleSpec();

		void setModuleSpecId(String moduleSpecId);
	}
}
