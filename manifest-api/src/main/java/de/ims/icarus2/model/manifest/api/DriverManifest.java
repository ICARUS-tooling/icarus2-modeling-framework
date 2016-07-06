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
package de.ims.icarus2.model.manifest.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.strings.StringResource;

/**
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface DriverManifest extends ForeignImplementationManifest {

	@Override
	default public ManifestFragment getHost() {
		return getContextManifest();
	};

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
	MappingManifest getMappingManifest(String id);

	/**
	 * Returns a hint on which type of resources the driver is depending to access
	 * corpus data.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	LocationType getLocationType();

	boolean isLocalLocationType();

	/**
	 *  For live driver manifests this method returns the manifest describing the
	 *  surrounding {@code Context}. For templates the return value is {@code null}.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	ContextManifest getContextManifest();

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
	ModuleSpec getModuleSpec(String specId);

	default Collection<ModuleManifest> getModuleManifests(String moduleId) {
		LazyCollection<ModuleManifest> result = LazyCollection.lazySet();

		forEachModuleManifest(m -> {if(moduleId.equals(m.getId()))result.add(m);});

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
	public interface ModuleSpec extends ModifiableIdentity, Lockable, Documentable, TypedManifest {

		public static final boolean DEFAULT_IS_CUSTOMIZABLE = false;
		public static final Multiplicity DEFAULT_MULTIPLICITY = Multiplicity.ONE;

		@AccessRestriction(AccessMode.READ)
		DriverManifest getDriverManifest();

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
		String getExtensionPointUid();

		// Modification methods

		void setMultiplicity(Multiplicity multiplicity);
		void setCustomizable(boolean customizable);
		void setExtensionPointUid(String extensionPointUid);
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	@AccessControl(AccessPolicy.DENY)
	public interface ModuleManifest extends ForeignImplementationManifest {

		@AccessRestriction(AccessMode.READ)
		DriverManifest getDriverManifest();

		@Override
		default public ManifestFragment getHost() {
			return getDriverManifest();
		};

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
		ModuleSpec getModuleSpec();

		void setModuleSpecId(String moduleSpecId);
	}

	/**
	 * Defines the multiplicity of allowed module manifests that are linked to a single module spec
	 * within a driver manifest.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static enum Multiplicity implements StringResource {

		NONE("none"),
		NONE_OR_ONE("none-or-one"),
		ONE("one"),
		ONE_OR_MORE("one-or-more"),
		ANY("any"),
		;

		private final String xmlForm;

		private Multiplicity(String xmlForm) {
			this.xmlForm = xmlForm;
		}

		/**
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return name().toLowerCase();
		}

		/**
		 * @see de.ims.icarus2.util.strings.StringResource#getStringValue()
		 */
		@Override
		public String getStringValue() {
			return xmlForm;
		}

		private static Map<String, Multiplicity> xmlLookup;

		public static Multiplicity parseMultiplicity(String s) {
			if(xmlLookup==null) {
				Map<String, Multiplicity> map = new HashMap<>();
				for(Multiplicity type : values()) {
					map.put(type.xmlForm, type);
				}
				xmlLookup = map;
			}

			return xmlLookup.get(s);
		}
	}
}
