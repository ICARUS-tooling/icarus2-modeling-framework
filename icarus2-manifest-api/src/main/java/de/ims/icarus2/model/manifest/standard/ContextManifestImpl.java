/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
import java.util.List;
import java.util.function.Consumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.standard.Links.Link;
import de.ims.icarus2.model.manifest.standard.Links.MemoryLink;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public class ContextManifestImpl extends AbstractMemberManifest<ContextManifest> implements ContextManifest {

	// Lookup structures
//	private final List<LayerManifest> layerManifests = new ArrayList<>();
//	private final Map<String, LayerManifest> layerManifestLookup = new HashMap<>();

	// Main storage
	private final List<PrerequisiteManifest> prerequisiteManifests = new ArrayList<>();
	private final List<LayerGroupManifest> groupManifests = new ArrayList<>();
	private final List<LocationManifest> locationManifests = new ArrayList<>();

	private LayerLink primaryLayer;
	private LayerLink foundationLayer;

	private Boolean independent;
	private Boolean editable;

	private final CorpusManifest corpusManifest;
	private DriverManifest driverManifest;

	/**
	 * @param manifestLocation
	 * @param registry
	 */
	public ContextManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		super(manifestLocation, registry);

		corpusManifest = null;
	}

	public ContextManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, CorpusManifest corpusManifest) {
		super(manifestLocation, registry);

		verifyEnvironment(manifestLocation, corpusManifest, CorpusManifest.class);

		this.corpusManifest = corpusManifest;
	}

	public ContextManifestImpl(CorpusManifest corpusManifest) {
		this(corpusManifest.getManifestLocation(), corpusManifest.getRegistry(), corpusManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && prerequisiteManifests.isEmpty()
				&& groupManifests.isEmpty() && driverManifest==null && locationManifests.isEmpty();
	}

	@Override
	public void forEachLayerManifest(Consumer<? super LayerManifest> action) {
		forEachGroupManifest(g -> g.forEachLayerManifest(action));
	}

	private LayerManifest lookupLayerManifest(String id) {

		LayerManifest result = null;

		List<LayerGroupManifest> groupManifests = getGroupManifests();

		for(int i=0; i<groupManifests.size(); i++) {
			result = groupManifests.get(i).getLayerManifest(id);
			if(result!=null) {
				break;
			}
		}

		/// TODO maybe throw exception when no corpus manifest is set here?
		if(result==null && corpusManifest!=null) {
			List<PrerequisiteManifest> prerequisiteManifests = getPrerequisites();

			for(int i=0; i<prerequisiteManifests.size(); i++) {
				PrerequisiteManifest prerequisiteManifest = prerequisiteManifests.get(i);
				if(id.equals(prerequisiteManifest.getAlias())) {
					CorpusManifest corpusManifest = getCorpusManifest();
					ContextManifest targetContext = corpusManifest.getContextManifest(prerequisiteManifest.getContextId());
					result = targetContext.getLayerManifest(prerequisiteManifest.getLayerId());
					break;
				}
			}
		}

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContextManifest#getLayerManifest(java.lang.String)
	 */
	@Override
	public LayerManifest getLayerManifest(String id) {
		requireNonNull(id);

//		ensureLookup();

//		LayerManifest result = layerManifestLookup.get(id);

		LayerManifest result = lookupLayerManifest(id);

		if(result==null)
			throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
					"No layer available for id "+id+" in context "+getId()); //$NON-NLS-1$ //$NON-NLS-2$

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContextManifest#getPrerequisite(java.lang.String)
	 */
	@Override
	public PrerequisiteManifest getPrerequisite(String alias) {
		requireNonNull(alias);

		PrerequisiteManifest result = null;

		for(PrerequisiteManifest prerequisiteManifest : prerequisiteManifests) {
			if(alias.equals(prerequisiteManifest.getAlias())) {
				result = prerequisiteManifest;
				break;
			}
		}

		if(result==null && hasTemplate()) {
			result = getTemplate().getPrerequisite(alias);
		}

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContextManifest#getCorpusManifest()
	 */
	@Override
	public CorpusManifest getCorpusManifest() {
		return corpusManifest;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContextManifest#getLocationManifest()
	 */
	@Override
	public List<LocationManifest> getLocationManifests() {
		return CollectionUtils.getListProxy(locationManifests);
	}

	@Override
	public void addLocationManifest(LocationManifest manifest) {
		checkNotLocked();

		addLocationManifest0(manifest);
	}

	protected void addLocationManifest0(LocationManifest manifest) {
		requireNonNull(manifest);
//		checkNotTemplate();

		if(locationManifests.contains(manifest))
			throw new IllegalArgumentException("Location already present: "+manifest); //$NON-NLS-1$

		locationManifests.add(manifest);
	}

	@Override
	public void removeLocationManifest(LocationManifest manifest) {
		checkNotLocked();

		removeLocationManifest0(manifest);
	}

	protected void removeLocationManifest0(LocationManifest manifest) {
		requireNonNull(manifest);
//		checkNotTemplate();

		if(!locationManifests.remove(manifest))
			throw new IllegalArgumentException("Location unknown: "+manifest); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContextManifest#isIndependentContext()
	 */
	@Override
	public boolean isIndependentContext() {
		if(independent==null) {
			return hasTemplate() ? getTemplate().isIndependentContext() : DEFAULT_INDEPENDENT_VALUE;
		} else {
			return independent.booleanValue();
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContextManifest#isLocalIndependentContext()
	 */
	@Override
	public boolean isLocalIndependentContext() {
		return independent!=null;
	}

	/**
	 * @param independent the independent to set
	 */
	@Override
	public void setIndependentContext(boolean independent) {
		checkNotLocked();

		setIndependentContext0(independent);
	}

	protected void setIndependentContext0(boolean independent) {
//		checkNotLive();

		this.independent = Boolean.valueOf(independent);
	}

	@Override
	public boolean isEditable() {
		if(editable==null) {
			return hasTemplate() ? getTemplate().isEditable() : DEFAULT_EDITABLE_VALUE;
		} else {
			return editable.booleanValue();
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContextManifest#isLocalEditable()
	 */
	@Override
	public boolean isLocalEditable() {
		return editable!=null;
	}

	/**
	 * @param independent the independent to set
	 */
	@Override
	public void setEditable(boolean editable) {
		checkNotLocked();

		setEditable0(editable);
	}

	protected void setEditable0(boolean editable) {
//		checkNotLive();

		this.editable = Boolean.valueOf(editable);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContextManifest#isRootContext()
	 */
	@Override
	public boolean isRootContext() {
		return corpusManifest!=null && corpusManifest.isRootContext(this);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.CONTEXT_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContextManifest#getDriverManifest()
	 */
	@Override
	public DriverManifest getDriverManifest() {
		DriverManifest result = driverManifest;
		if(result==null && hasTemplate()) {
			result = getTemplate().getDriverManifest();
		}
		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContextManifest#isLocalDriverManifest()
	 */
	@Override
	public boolean isLocalDriverManifest() {
		return driverManifest!=null;
	}

	@Override
	public void forEachPrerequisite(Consumer<? super PrerequisiteManifest> action) {
		if(hasTemplate()) {
			getTemplate().forEachPrerequisite(action);
		}
		prerequisiteManifests.forEach(action);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContextManifest#forEachLocalPrerequisite(java.util.function.Consumer)
	 */
	@Override
	public void forEachLocalPrerequisite(
			Consumer<? super PrerequisiteManifest> action) {
		prerequisiteManifests.forEach(action);
	}

	@Override
	public void forEachGroupManifest(Consumer<? super LayerGroupManifest> action) {
		if(hasTemplate()) {
			getTemplate().forEachGroupManifest(action);
		}
		groupManifests.forEach(action);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContextManifest#forEachLocalGroupManifest(java.util.function.Consumer)
	 */
	@Override
	public void forEachLocalGroupManifest(
			Consumer<? super LayerGroupManifest> action) {
		groupManifests.forEach(action);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContextManifest#getPrimaryLayerManifest()
	 */
	@Override
	public ItemLayerManifest getPrimaryLayerManifest() {
		if(primaryLayer!=null) {
			return primaryLayer.get();
		}

		if(hasTemplate()) {
			return getTemplate().getPrimaryLayerManifest();
		}

		throw new IllegalStateException("No primary layer defined for context: "+this); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContextManifest#isLocalPrimaryLayerManifest()
	 */
	@Override
	public boolean isLocalPrimaryLayerManifest() {
		return primaryLayer!=null;
	}

	/**
	 * @param primaryLayerManifest the primaryLayerManifest to set
	 */
	@Override
	public void setPrimaryLayerId(String primaryLayerId) {
		checkNotLocked();

		setPrimaryLayerId0(primaryLayerId);
	}

	protected void setPrimaryLayerId0(String primaryLayerId) {
		requireNonNull(primaryLayerId);
//		checkNotLive();

		primaryLayer = new LayerLink(primaryLayerId);
	}

	/**
	 * @return the foundationLayerManifest
	 */
	@Override
	public ItemLayerManifest getFoundationLayerManifest() {
		if(foundationLayer!=null) {
			return foundationLayer.get();
		}

		if(hasTemplate()) {
			return getTemplate().getFoundationLayerManifest();
		}

		return null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContextManifest#isLocalFoundationLayerManifest()
	 */
	@Override
	public boolean isLocalFoundationLayerManifest() {
		return foundationLayer!=null;
	}

	/**
	 * @param foundationLayerId the foundationLayerId to set
	 */
	@Override
	public void setFoundationLayerId(String foundationLayerId) {
		checkNotLocked();

		setFoundationLayerId0(foundationLayerId);
	}

	protected void setFoundationLayerId0(String foundationLayerId) {
		requireNonNull(foundationLayerId);
//		checkNotLive();

		foundationLayer = new LayerLink(foundationLayerId);
	}

	/**
	 * @param driverManifest the driverManifest to set
	 */
	@Override
	public void setDriverManifest(DriverManifest driverManifest) {
		checkNotLocked();

		setDriverManifest0(driverManifest);
	}

	protected void setDriverManifest0(DriverManifest driverManifest) {
		requireNonNull(driverManifest);
//		checkNotLive();

		this.driverManifest = driverManifest;
	}

	@Override
	public PrerequisiteManifest addPrerequisite(String alias) {
		checkNotLocked();

		return addPrerequisite0(alias);
	}

	protected PrerequisiteManifest addPrerequisite0(String alias) {
		requireNonNull(alias);
//		checkNotLive();

		for(int i=0; i<prerequisiteManifests.size(); i++) {
			if(alias.equals(prerequisiteManifests.get(i).getAlias()))
				throw new IllegalArgumentException("Duplicate prerequisite alias: "+alias); //$NON-NLS-1$
		}

		PrerequisiteManifestImpl result = new PrerequisiteManifestImpl(alias);
		prerequisiteManifests.add(result);

		return result;
	}

	@Override
	public void removePrerequisite(PrerequisiteManifest prerequisiteManifest) {
		checkNotLocked();

		removePrerequisite0(prerequisiteManifest);
	}

	protected void removePrerequisite0(PrerequisiteManifest prerequisiteManifest) {
		requireNonNull(prerequisiteManifest);
//		checkNotLive();

		if(!prerequisiteManifests.remove(prerequisiteManifest))
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
					"Unknown prerequisite manifest: "+prerequisiteManifest.getAlias());
	}

	@Override
	public void addLayerGroup(LayerGroupManifest groupManifest) {
		checkNotLocked();

		addLayerGroup0(groupManifest);
	}

	protected void addLayerGroup0(LayerGroupManifest groupManifest) {
		requireNonNull(groupManifest);
//		checkNotLive();

		if(groupManifest.getContextManifest()!=this)
			throw new IllegalArgumentException("Layer group already hosted in foreign context: "+groupManifest); //$NON-NLS-1$

		if(groupManifests.contains(groupManifest))
			throw new IllegalArgumentException("Layer group already present: "+groupManifest); //$NON-NLS-1$

		groupManifests.add(groupManifest);

//		resetLookup();
	}

	@Override
	public void removeLayerGroup(LayerGroupManifest groupManifest) {
		checkNotLocked();

		removeLayerGroup0(groupManifest);
	}

	protected void removeLayerGroup0(LayerGroupManifest groupManifest) {
		requireNonNull(groupManifest);
//		checkNotLive();

		if(!groupManifests.remove(groupManifest))
			throw new IllegalArgumentException("Layer group not present: "+groupManifest); //$NON-NLS-1$

//		resetLookup();
	}

	@Override
	public void lock() {
		super.lock();

		for(PrerequisiteManifest prerequisiteManifest : prerequisiteManifests) {
			prerequisiteManifest.lock();
		}

		for(LayerGroupManifest layerGroupManifest : groupManifests) {
			layerGroupManifest.lock();
		}
	}

	protected class LayerLink extends Link<ItemLayerManifest> {

		/**
		 * @param id
		 */
		public LayerLink(String id) {
			super(id);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.Links.Link#resolve()
		 */
		@Override
		protected ItemLayerManifest resolve() {
			return (ItemLayerManifest) getLayerManifest(getId());
		}

	}

	/**
	 * Link to a previously defined {@link PrerequisiteManifest} in a
	 * context template manifest.
	 *
	 * @author Markus Gärtner
	 *
	 */
	protected class PrerequisiteLink extends MemoryLink<PrerequisiteManifest> {

		/**
		 * @param id
		 */
		public PrerequisiteLink(String id) {
			super(id);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.Links.Link#resolve()
		 */
		@Override
		protected PrerequisiteManifest resolve() {
			ContextManifest contextManifest = ContextManifestImpl.this.getTemplate();
			return contextManifest==null ? null : contextManifest.getPrerequisite(getId());
		}

	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public class PrerequisiteManifestImpl extends AbstractLockable implements PrerequisiteManifest {

		private final String alias;
		private final PrerequisiteLink unresolvedForm;

		private String layerId;
		private String typeId;
		private String contextId;
		private String description;

		PrerequisiteManifestImpl(String alias) {
			requireNonNull(alias);

			if(!ManifestUtils.isValidId(alias))
				throw new IllegalArgumentException("Alias format not supported: "+alias); //$NON-NLS-1$

			this.alias = alias;
			this.unresolvedForm = new PrerequisiteLink(alias);
		}

		/**
		 * @return the description
		 */
		@Override
		public String getDescription() {
			return description;
		}

		/**
		 * @param description the description to set
		 */
		@Override
		public void setDescription(String description) {
			checkNotLocked();
//			getContextManifest().checkNotLive();

			this.description = description;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#getContextManifest()
		 */
		@Override
		public ContextManifest getContextManifest() {
			return ContextManifestImpl.this;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#getLayerId()
		 */
		@Override
		public String getLayerId() {
			return layerId;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#getContextId()
		 */
		@Override
		public String getContextId() {
			return contextId;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#getTypeId()
		 */
		@Override
		public String getTypeId() {
			return typeId;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#getAlias()
		 */
		@Override
		public String getAlias() {
			return alias;
		}

		/**
		 * @return the unresolvedForm
		 */
		@Override
		public PrerequisiteManifest getUnresolvedForm() {
			return unresolvedForm.get();
		}

		/**
		 * @param layerId the layerId to set
		 */
		@Override
		public void setLayerId(String layerId) {
			checkNotLocked();
//			getContextManifest().checkNotLive();

			this.layerId = layerId;
		}

		/**
		 * @param typeId the typeId to set
		 */
		@Override
		public void setTypeId(String typeId) {
			checkNotLocked();
//			getContextManifest().checkNotLive();

			this.typeId = typeId;
		}

		/**
		 * @param contextId the contextId to set
		 */
		@Override
		public void setContextId(String contextId) {
			checkNotLocked();
//			getContextManifest().checkNotLive();

			this.contextId = contextId;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return alias.hashCode();
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} if(obj instanceof PrerequisiteManifest) {
				return alias.equals(((PrerequisiteManifest)obj).getAlias());
			}
			return false;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Prerequisite:"+alias; //$NON-NLS-1$
		}

	}
}
