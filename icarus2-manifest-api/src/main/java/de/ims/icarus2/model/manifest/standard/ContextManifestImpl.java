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
package de.ims.icarus2.model.manifest.standard;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.standard.Links.Link;
import de.ims.icarus2.model.manifest.standard.Links.MemoryLink;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public class ContextManifestImpl extends AbstractMemberManifest<ContextManifest, CorpusManifest> implements ContextManifest {

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

	private Optional<DriverManifest> driverManifest = Optional.empty();

	/**
	 * @param manifestLocation
	 * @param registry
	 */
	public ContextManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		super(manifestLocation, registry);
	}

	public ContextManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, CorpusManifest corpusManifest) {
		super(manifestLocation, registry, corpusManifest, CorpusManifest.class);
	}

	public ContextManifestImpl(CorpusManifest corpusManifest) {
		super(corpusManifest, hostIdentity(), CorpusManifest.class);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && prerequisiteManifests.isEmpty()
				&& groupManifests.isEmpty() && !driverManifest.isPresent() && locationManifests.isEmpty();
	}

	private <L extends LayerManifest<L>> Optional<L> lookupLayerManifest(String id) {
		requireNonNull(id);

		Optional<L> result = Optional.empty();

		List<LayerGroupManifest> groupManifests = getGroupManifests();

		for(int i=0; i<groupManifests.size(); i++) {
			result = groupManifests.get(i).getLayerManifest(id);
			if(result.isPresent()) {
				break;
			}
		}

		/// TODO maybe throw exception when no corpus manifest is set here?
		if(!result.isPresent() && getCorpusManifest().isPresent()) {
			List<PrerequisiteManifest> prerequisiteManifests = getPrerequisites();

			for(int i=0; i<prerequisiteManifests.size(); i++) {
				PrerequisiteManifest prerequisiteManifest = prerequisiteManifests.get(i);
				if(id.equals(prerequisiteManifest.getAlias())) {
					result = getCorpusManifest().flatMap(
							corpus -> corpus.getContextManifest(prerequisiteManifest.getContextId()
									.orElseThrow(Manifest.invalidId("Prerequisite does not declare a proper context id: "
												+ ManifestUtils.getName(prerequisiteManifest))))
							.flatMap(ccontext -> ccontext.getLayerManifest(prerequisiteManifest.getLayerId()
									.orElseThrow(Manifest.invalidId("Prerequisite does not declare a proper layer id: "
												+ ManifestUtils.getName(prerequisiteManifest))))));
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
	public <L extends LayerManifest<L>> Optional<L> getLayerManifest(String id) {
		return lookupLayerManifest(id);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContextManifest#getPrerequisite(java.lang.String)
	 */
	@Override
	public Optional<PrerequisiteManifest> getPrerequisite(String alias) {
		requireNonNull(alias);

		Optional<PrerequisiteManifest> result = Optional.empty();

		for(PrerequisiteManifest prerequisiteManifest : prerequisiteManifests) {
			if(alias.equals(prerequisiteManifest.getAlias())) {
				result = Optional.of(prerequisiteManifest);
				break;
			}
		}

		return getDerivable(result, t -> t.getPrerequisite(alias));
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContextManifest#getLocationManifest()
	 */
	@Override
	public List<LocationManifest> getLocationManifests() {
		return CollectionUtils.getListProxy(locationManifests);
	}

	@Override
	public ContextManifest addLocationManifest(LocationManifest manifest) {
		checkNotLocked();

		addLocationManifest0(manifest);

		return this;
	}

	protected void addLocationManifest0(LocationManifest manifest) {
		requireNonNull(manifest);
//		checkNotTemplate();

		if(locationManifests.contains(manifest))
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT, "Location already present: "+manifest); //$NON-NLS-1$

		locationManifests.add(manifest);
	}

	@Override
	public ContextManifest removeLocationManifest(LocationManifest manifest) {
		checkNotLocked();

		removeLocationManifest0(manifest);

		return this;
	}

	protected void removeLocationManifest0(LocationManifest manifest) {
		requireNonNull(manifest);
//		checkNotTemplate();

		if(!locationManifests.remove(manifest))
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT, "Location unknown: "+manifest); //$NON-NLS-1$
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
	public ContextManifest setIndependentContext(boolean independent) {
		checkNotLocked();

		setIndependentContext0(independent);

		return this;
	}

	protected void setIndependentContext0(boolean independent) {
//		checkNotLive();

		this.independent = (independent==DEFAULT_INDEPENDENT_VALUE && !hasTemplate()) ? null :  Boolean.valueOf(independent);
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
	public ContextManifest setEditable(boolean editable) {
		checkNotLocked();

		setEditable0(editable);

		return this;
	}

	protected void setEditable0(boolean editable) {
//		checkNotLive();

		this.editable = (editable==DEFAULT_EDITABLE_VALUE && !hasTemplate()) ? null : Boolean.valueOf(editable);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContextManifest#isRootContext()
	 */
	@Override
	public boolean isRootContext() {
		Optional<CorpusManifest> host = getCorpusManifest();
		return host.isPresent() && host.get().isRootContext(this);
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
	public Optional<DriverManifest> getDriverManifest() {
		return getDerivable(driverManifest, ContextManifest::getDriverManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContextManifest#isLocalDriverManifest()
	 */
	@Override
	public boolean isLocalDriverManifest() {
		return driverManifest.isPresent();
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
	public <L extends ItemLayerManifest> Optional<L> getPrimaryLayerManifest() {
		@SuppressWarnings("unchecked")
		Optional<L> result = Optional.ofNullable(
				primaryLayer==null ? null : (L)primaryLayer.get());
		return getDerivable(result, ContextManifest::getPrimaryLayerManifest);
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
	public ContextManifest setPrimaryLayerId(String primaryLayerId) {
		checkNotLocked();

		setPrimaryLayerId0(primaryLayerId);

		return this;
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
	public <L extends ItemLayerManifest> Optional<L> getFoundationLayerManifest() {
		@SuppressWarnings("unchecked")
		Optional<L> result = Optional.ofNullable(
				foundationLayer==null ? null : (L)foundationLayer.get());
		return getDerivable(result, ContextManifest::getFoundationLayerManifest);
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
	public ContextManifest setFoundationLayerId(String foundationLayerId) {
		checkNotLocked();

		setFoundationLayerId0(foundationLayerId);

		return this;
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
	public ContextManifest setDriverManifest(DriverManifest driverManifest) {
		checkNotLocked();

		setDriverManifest0(driverManifest);

		return this;
	}

	protected void setDriverManifest0(DriverManifest driverManifest) {
		this.driverManifest = Optional.of(driverManifest);
	}

	@Override
	public PrerequisiteManifest addAndGetPrerequisite(String alias) {
		checkNotLocked();

		return addPrerequisite0(alias);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContextManifest#addPrerequisite(java.lang.String, java.util.function.Consumer)
	 */
	@Override
	public ContextManifest addPrerequisite(String alias, Consumer<? super PrerequisiteManifest> action) {
		checkNotLocked();

		IcarusUtils.consumeIfAble(addPrerequisite0(alias), action);

		return this;
	}

	protected PrerequisiteManifest addPrerequisite0(String alias) {
		requireNonNull(alias);
//		checkNotLive();

		for(int i=0; i<prerequisiteManifests.size(); i++) {
			if(alias.equals(prerequisiteManifests.get(i).getAlias()))
				throw new ManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID,
						"Duplicate prerequisite alias: "+alias);
		}

		PrerequisiteManifestImpl result = new PrerequisiteManifestImpl(this, alias);
		prerequisiteManifests.add(result);

		return result;
	}

	@Override
	public ContextManifest removePrerequisite(PrerequisiteManifest prerequisiteManifest) {
		checkNotLocked();

		removePrerequisite0(prerequisiteManifest);

		return this;
	}

	protected void removePrerequisite0(PrerequisiteManifest prerequisiteManifest) {
		requireNonNull(prerequisiteManifest);
//		checkNotLive();

		if(!prerequisiteManifests.remove(prerequisiteManifest))
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
					"Unknown prerequisite manifest: "+prerequisiteManifest.getAlias());
	}

	@Override
	public ContextManifest addLayerGroup(LayerGroupManifest groupManifest) {
		checkNotLocked();

		addLayerGroup0(groupManifest);

		return this;
	}

	protected void addLayerGroup0(LayerGroupManifest groupManifest) {
		requireNonNull(groupManifest);
//		checkNotLive();

		ContextManifest declaredHost = groupManifest.getContextManifest().orElse(null);
		if(declaredHost!=null && declaredHost!=this)
			throw new ManifestException(ManifestErrorCode.MANIFEST_CORRUPTED_STATE,
					"Layer group already hosted in foreign context: "+groupManifest);

		if(groupManifests.contains(groupManifest))
			throw new ManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID,
					"Layer group already present: "+groupManifest);

		groupManifests.add(groupManifest);

//		resetLookup();
	}

	@Override
	public ContextManifest removeLayerGroup(LayerGroupManifest groupManifest) {
		checkNotLocked();

		removeLayerGroup0(groupManifest);

		return this;
	}

	protected void removeLayerGroup0(LayerGroupManifest groupManifest) {
		requireNonNull(groupManifest);
//		checkNotLive();

		if(!groupManifests.remove(groupManifest))
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
					"Layer group not present: "+groupManifest);

//		resetLookup();
	}

	@Override
	protected void lockNested() {
		super.lockNested();

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
		protected Optional<ItemLayerManifest> resolve() {
			return getLayerManifest(getId());
		}

	}

	/**
	 * Link to a previously defined {@link PrerequisiteManifest} in a
	 * context template manifest.
	 *
	 * @author Markus Gärtner
	 *
	 */
	protected static class PrerequisiteLink extends MemoryLink<PrerequisiteManifest> {

		private final ContextManifest contextManifest;

		/**
		 * @param id
		 */
		public PrerequisiteLink(ContextManifest contextManifest, String id) {
			super(id);

			this.contextManifest = requireNonNull(contextManifest);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.Links.Link#resolve()
		 */
		@Override
		protected Optional<PrerequisiteManifest> resolve() {
			return contextManifest.tryGetTemplate()
					.flatMap(c -> ((ContextManifest)c).getPrerequisite(getId()));
		}

	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class PrerequisiteManifestImpl extends AbstractLockable implements PrerequisiteManifest {

		private final ContextManifest contextManifest;

		private final String alias;
		private final PrerequisiteLink unresolvedForm;

		private Optional<String> layerId = Optional.empty();
		private Optional<String> typeId = Optional.empty();
		private Optional<String> contextId = Optional.empty();
		private Optional<String> description = Optional.empty();

		public PrerequisiteManifestImpl(ContextManifest contextManifest, String alias) {
			requireNonNull(contextManifest);
			requireNonNull(alias);

			ManifestUtils.checkId(alias);

			this.contextManifest = contextManifest;
			this.alias = alias;
			this.unresolvedForm = new PrerequisiteLink(contextManifest, alias);
		}

		/**
		 * @return the description
		 */
		@Override
		public Optional<String> getDescription() {
			return description;
		}

		/**
		 * @param description the description to set
		 */
		@Override
		public void setDescription(String description) {
			checkNotLocked();
//			getContextManifest().checkNotLive();

			this.description = Optional.ofNullable(description);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#getContextManifest()
		 */
		@Override
		public ContextManifest getContextManifest() {
			return contextManifest;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#getLayerId()
		 */
		@Override
		public Optional<String> getLayerId() {
			return layerId;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#getContextId()
		 */
		@Override
		public Optional<String> getContextId() {
			return contextId;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#getTypeId()
		 */
		@Override
		public Optional<String> getTypeId() {
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
		public Optional<PrerequisiteManifest> getUnresolvedForm() {
			return unresolvedForm.getOptional();
		}

		/**
		 * @param layerId the layerId to set
		 */
		@Override
		public void setLayerId(String layerId) {
			checkNotLocked();
//			getContextManifest().checkNotLive();

			this.layerId = Optional.ofNullable(layerId);
		}

		/**
		 * @param typeId the typeId to set
		 */
		@Override
		public void setTypeId(String typeId) {
			checkNotLocked();
//			getContextManifest().checkNotLive();

			this.typeId = Optional.ofNullable(typeId);
		}

		/**
		 * @param contextId the contextId to set
		 */
		@Override
		public void setContextId(String contextId) {
			checkNotLocked();
//			getContextManifest().checkNotLive();

			this.contextId = Optional.ofNullable(contextId);
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
