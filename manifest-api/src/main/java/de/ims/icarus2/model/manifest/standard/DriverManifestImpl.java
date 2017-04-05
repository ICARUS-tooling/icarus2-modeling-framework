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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.Documentation;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.LocationType;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.standard.Links.Link;
import de.ims.icarus2.util.Multiplicity;
import de.ims.icarus2.util.classes.ClassUtils;
import de.ims.icarus2.util.collections.LazyCollection;

/**
 * @author Markus Gärtner
 *
 */
public class DriverManifestImpl extends AbstractForeignImplementationManifest<DriverManifest> implements DriverManifest {

	private LocationType locationType;
	private final List<MappingManifest> mappingManifests = new ArrayList<>();
	private final List<ModuleSpec> moduleSpecs = new ArrayList<>();
	private final Map<String,Collection<ModuleManifest>> moduleManifests = new LinkedHashMap<>();
	private final ContextManifest contextManifest;

	public DriverManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, ContextManifest contextManifest) {
		super(manifestLocation, registry);

		verifyEnvironment(manifestLocation, contextManifest, ContextManifest.class);

		this.contextManifest = contextManifest;
	}

	public DriverManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		this(manifestLocation, registry, null);
	}

	public DriverManifestImpl(ContextManifest contextManifest) {
		this(contextManifest.getManifestLocation(), contextManifest.getRegistry(), contextManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && mappingManifests.isEmpty() && moduleSpecs.isEmpty() && moduleManifests.isEmpty();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#getContextManifest()
	 */
	@Override
	public ContextManifest getContextManifest() {
		return contextManifest;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.DRIVER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#getImplementationManifest()
	 */
	@Override
	public ImplementationManifest getImplementationManifest() {
		ImplementationManifest result = super.getImplementationManifest();
		if(result==null && hasTemplate()) {
			result = getTemplate().getImplementationManifest();
		}

		return result;
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
	public LocationType getLocationType() {
		LocationType result = locationType;
		if(result==null && hasTemplate()) {
			result = getTemplate().getLocationType();
		}

		if(result==null)
			throw new ManifestException(ManifestErrorCode.MANIFEST_MISSING_LOCATION,
					"No location type available for driver manifest: "+getId()); //$NON-NLS-1$

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#isLocalLocationType()
	 */
	@Override
	public boolean isLocalLocationType() {
		return locationType!=null;
	}

	@Override
	public void forEachModuleManifest(Consumer<? super ModuleManifest> action) {

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
		for(Collection<ModuleManifest> manifests : moduleManifests.values()) {
			manifests.forEach(action);
		}
	}

	@Override
	public void forEachModuleSpec(Consumer<? super ModuleSpec> action) {

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
		moduleSpecs.forEach(action);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#getModuleSpec(java.lang.String)
	 */
	@Override
	public ModuleSpec getModuleSpec(final String specId) {
		if (specId == null)
			throw new NullPointerException("Invalid specId"); //$NON-NLS-1$

		ModuleSpec result = null;

		for(ModuleSpec spec : moduleSpecs) {
			if(specId.equals(spec.getId())) {
				result = spec;
				break;
			}
		}

		if(result==null && hasTemplate()) {
			result = getTemplate().getModuleSpec(specId);
		}

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#getModuleManifests(java.lang.String)
	 */
	@Override
	public List<ModuleManifest> getModuleManifests(String moduleId) {
		if (moduleId == null)
			throw new NullPointerException("Invalid specId"); //$NON-NLS-1$

		LazyCollection<ModuleManifest> result = LazyCollection.lazyList();

		result.addAll(moduleManifests.get(moduleId));

		if(hasTemplate()) {
			result.addAll(getTemplate().getModuleManifests(moduleId));
		}

		return result.getAsList();
	}

	public Collection<ModuleManifest> getLocalModuleManifests(String moduleId) {
		if (moduleId == null)
			throw new NullPointerException("Invalid specId"); //$NON-NLS-1$

		LazyCollection<ModuleManifest> result = LazyCollection.lazyLinkedSet();

		result.addAll(moduleManifests.get(moduleId));

		return result.getAsSet();
	}

	@Override
	public MappingManifest getMappingManifest(String id) {
		requireNonNull(id);

		for(MappingManifest manifest : mappingManifests) {
			if(id.equals(manifest.getId())) {
				return manifest;
			}
		}

		throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
				"No mapping available for given id: "+id);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#addMappingManifest(de.ims.icarus2.model.manifest.api.MappingManifest)
	 */
	@Override
	public void addMappingManifest(MappingManifest mappingManifest) {
		checkNotLocked();

		addMappingManifest0(mappingManifest);
	}

	protected void addMappingManifest0(MappingManifest mappingManifest) {
		requireNonNull(mappingManifest);
//		checkNotLive();

		if(mappingManifests.contains(mappingManifest))
			throw new IllegalArgumentException("Duplicate mapping manifest: "+mappingManifest); //$NON-NLS-1$

		mappingManifests.add(mappingManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#removeMappingManifest(de.ims.icarus2.model.manifest.api.MappingManifest)
	 */
	@Override
	public void removeMappingManifest(MappingManifest mappingManifest) {
		checkNotLocked();

		removeMappingManifest0(mappingManifest);
	}

	protected void removeMappingManifest0(MappingManifest mappingManifest) {
		requireNonNull(mappingManifest);
//		checkNotLive();

		if(!mappingManifests.remove(mappingManifest))
			throw new IllegalArgumentException("Unknown mapping manifest: "+mappingManifest); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#addModuleManifest(de.ims.icarus2.model.manifest.api.ModuleManifest)
	 */
	@Override
	public void addModuleManifest(ModuleManifest moduleManifest) {
		checkNotLocked();

		addModuleManifest0(moduleManifest);
	}

	protected void addModuleManifest0(ModuleManifest moduleManifest) {
		requireNonNull(moduleManifest);
//		checkNotLive();

		String moduleId = moduleManifest.getId();

		Collection<ModuleManifest> manifests = moduleManifests.get(moduleId);

		if(manifests==null) {
			 manifests = new LinkedHashSet<>();
			 moduleManifests.put(moduleId, manifests);
		}

		if(manifests.contains(moduleManifest))
			throw new IllegalArgumentException("Duplicate module manifest: "+moduleManifest); //$NON-NLS-1$

		manifests.add(moduleManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#removeModuleManifest(de.ims.icarus2.model.manifest.api.ModuleManifest)
	 */
	@Override
	public void removeModuleManifest(ModuleManifest moduleManifest) {
		checkNotLocked();

		removeModuleManifest0(moduleManifest);
	}

	protected void removeModuleManifest0(ModuleManifest moduleManifest) {
		requireNonNull(moduleManifest);
//		checkNotLive();

		String moduleId = moduleManifest.getId();

		Collection<ModuleManifest> manifests = moduleManifests.get(moduleId);

		if(manifests==null || !manifests.remove(moduleManifest))
			throw new IllegalArgumentException("Unknown module manifest: "+moduleManifest); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#addModuleManifest(de.ims.icarus2.model.manifest.api.addModuleSpec)
	 */
	@Override
	public void addModuleSpec(ModuleSpec moduleSpec) {
		checkNotLocked();

		addModuleSpec0(moduleSpec);
	}

	protected void addModuleSpec0(ModuleSpec moduleSpec) {
		requireNonNull(moduleSpec);
//		checkNotLive();

		if(moduleSpecs.contains(moduleSpec))
			throw new IllegalArgumentException("Duplicate module spec: "+moduleSpec); //$NON-NLS-1$

		moduleSpecs.add(moduleSpec);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#removeModuleManifest(de.ims.icarus2.model.manifest.api.removeModuleSpec)
	 */
	@Override
	public void removeModuleSpec(ModuleSpec moduleSpec) {
		checkNotLocked();

		removeModuleSpec0(moduleSpec);
	}

	protected void removeModuleSpec0(ModuleSpec moduleSpec) {
		requireNonNull(moduleSpec);
//		checkNotLive();

		if(!moduleSpecs.remove(moduleSpec))
			throw new IllegalArgumentException("Unknown module spec: "+moduleSpec); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DriverManifest#setLocationType(de.ims.icarus2.model.io.LocationType)
	 */
	@Override
	public void setLocationType(LocationType locationType) {
		checkNotLocked();

		setLocationType0(locationType);
	}

	protected void setLocationType0(LocationType locationType) {
		requireNonNull(locationType);
//		checkNotLive();

		this.locationType = locationType;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractForeignImplementationManifest#lock()
	 */
	@Override
	public void lock() {
		super.lock();

		lockNested(moduleSpecs);
		lockNested(mappingManifests);
		moduleManifests.values().forEach(this::lockNested);
	}

	public static class ModuleSpecImpl extends DefaultModifiableIdentity implements ModuleSpec {

		private final DriverManifest driverManifest;
		private boolean customizable = DEFAULT_IS_CUSTOMIZABLE;
		private Multiplicity multiplicity;
		private String extensionPointUid;
		private Documentation documentation;

		public ModuleSpecImpl(DriverManifest driverManifest) {
			requireNonNull(driverManifest);

			this.driverManifest = driverManifest;
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
		 * @see de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec#getDriverManifest()
		 */
		@Override
		public DriverManifest getDriverManifest() {
			return driverManifest;
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
		public String getExtensionPointUid() {
			return extensionPointUid;
		}

		@Override
		public Multiplicity getMultiplicity() {
			return multiplicity==null ? DEFAULT_MULTIPLICITY : multiplicity;
		}

		/**
		 * @return the documentation
		 */
		@Override
		public Documentation getDocumentation() {
			return documentation;
		}

		/**
		 * @param documentation the documentation to set
		 */
		@Override
		public void setDocumentation(Documentation documentation) {
			checkNotLocked();

			setDocumentation0(documentation);
		}

		protected void setDocumentation0(Documentation documentation) {
			this.documentation = documentation;
		}

		@Override
		public void setMultiplicity(Multiplicity multiplicity) {
			checkNotLocked();

			setMultiplicity0(multiplicity);
		}

		protected void setMultiplicity0(Multiplicity multiplicity) {
			requireNonNull(multiplicity);

			this.multiplicity = multiplicity;
		}

		/**
		 * @param customizable the customizable to set
		 */
		@Override
		public void setCustomizable(boolean customizable) {
			checkNotLocked();

			setCustomizable0(customizable);
		}

		protected void setCustomizable0(boolean customizable) {
			this.customizable = customizable;
		}

		/**
		 * @param extensionPointUid the extensionPointUid to set
		 */
		@Override
		public void setExtensionPointUid(String extensionPointUid) {
			checkNotLocked();

			setExtensionPointUid0(extensionPointUid);
		}

		protected void setExtensionPointUid0(String extensionPointUid) {
			this.extensionPointUid = extensionPointUid;
		}

	}

	public static class ModuleManifestImpl extends AbstractForeignImplementationManifest<ModuleManifestImpl> implements ModuleManifest {

		private final DriverManifest driverManifest;
		private ModuleSpecLink moduleSpec;

		public ModuleManifestImpl(ManifestLocation manifestLocation,
				ManifestRegistry registry, DriverManifest driverManifest) {
			super(manifestLocation, registry);

			verifyEnvironment(manifestLocation, driverManifest, DriverManifest.class);

			this.driverManifest = driverManifest;
		}

		public ModuleManifestImpl(ManifestLocation manifestLocation,
				ManifestRegistry registry) {
			this(manifestLocation, registry, null);
		}

		public ModuleManifestImpl(DriverManifest driverManifest) {
			this(driverManifest.getManifestLocation(), driverManifest.getRegistry(), driverManifest);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.Manifest#getManifestType()
		 */
		@Override
		public ManifestType getManifestType() {
			return ManifestType.MODULE_MANIFEST;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.AbstractForeignImplementationManifest#getImplementationManifest()
		 */
		@Override
		public ImplementationManifest getImplementationManifest() {
			ImplementationManifest result = super.getImplementationManifest();
			if(result==null && hasTemplate()) {
				result = getTemplate().getImplementationManifest();
			}

			return result;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest#getDriverManifest()
		 */
		@Override
		public DriverManifest getDriverManifest() {
			return driverManifest;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest#getModuleSpecId()
		 */
		@Override
		public ModuleSpec getModuleSpec() {
			return moduleSpec==null ? null : moduleSpec.get();
		}

		/**
		 * @param moduleSpecId the moduleSpecId to set
		 */
		@Override
		public void setModuleSpecId(String moduleSpecId) {
			checkNotLocked();

			setModuleSpecId0(moduleSpecId);
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
			protected ModuleSpec resolve() {
				return getDriverManifest().getModuleSpec(getId());
			}

		}
	}
}
