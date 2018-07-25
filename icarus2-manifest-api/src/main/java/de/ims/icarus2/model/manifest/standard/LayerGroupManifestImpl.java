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

import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.standard.Links.Link;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.lang.ClassUtils;

/**
 * @author Markus Gärtner
 *
 */
public class LayerGroupManifestImpl extends DefaultModifiableIdentity implements LayerGroupManifest {

	private final ContextManifest contextManifest;

	private final List<LayerManifest> layerManifests = new ArrayList<>();
	private LayerLink primaryLayer;
	private Boolean independent;

	public LayerGroupManifestImpl(ContextManifest contextManifest) {
		requireNonNull(contextManifest);

		this.contextManifest = contextManifest;
	}

	@Override
	public ManifestType getManifestType() {
		return ManifestType.LAYER_GROUP_MANIFEST;
	}

	public LayerGroupManifestImpl(ContextManifest contextManifest, String name) {
		requireNonNull(contextManifest);
		requireNonNull(name);

		this.contextManifest = contextManifest;
		setName(name);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LayerGroupManifest#getContextManifest()
	 */
	@Override
	public ContextManifest getContextManifest() {
		return contextManifest;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LayerGroupManifest#layerCount()
	 */
	@Override
	public int layerCount() {
		return layerManifests.size();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LayerGroupManifest#getLayerManifests()
	 */
	@Override
	public List<LayerManifest> getLayerManifests() {
		return CollectionUtils.getListProxy(layerManifests);
	}

	@Override
	public void forEachLayerManifest(Consumer<? super LayerManifest> action) {
		layerManifests.forEach(action);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LayerGroupManifest#getPrimaryLayerManifest()
	 */
	@Override
	public ItemLayerManifest getPrimaryLayerManifest() {
		return primaryLayer.get();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LayerGroupManifest#isIndependent()
	 */
	@Override
	public boolean isIndependent() {
		return independent==null ? DEFAULT_INDEPENDENT_VALUE : independent.booleanValue();
	}

	/**
	 * @param independent the independent to set or {@code null} if the implementation should use the default value
	 */
	@Override
	public void setIndependent(boolean independent) {
		checkNotLocked();

		setIndependent0(independent);
	}

	protected void setIndependent0(boolean independent) {
		this.independent = independent==DEFAULT_INDEPENDENT_VALUE ? null : Boolean.valueOf(independent);
	}

	@Override
	public void addLayerManifest(LayerManifest layerManifest) {
		checkNotLocked();

		addLayerManifest0(layerManifest);
	}

	protected void addLayerManifest0(LayerManifest layerManifest) {
		requireNonNull(layerManifest);

		if(layerManifests.contains(layerManifest))
			throw new IllegalArgumentException("Layer manifest already present in group: "+layerManifest.getId()); //$NON-NLS-1$

		layerManifests.add(layerManifest);
	}

	@Override
	public void removeLayerManifest(LayerManifest layerManifest) {
		checkNotLocked();

		removeLayerManifest0(layerManifest);
	}

	protected void removeLayerManifest0(LayerManifest layerManifest) {
		requireNonNull(layerManifest);

		if(!layerManifests.remove(layerManifest))
			throw new IllegalArgumentException("Layer manifest not present in group: "+layerManifest.getId()); //$NON-NLS-1$
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

		primaryLayer = new LayerLink(primaryLayerId);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LayerGroup:"+getId(); //$NON-NLS-1$
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = contextManifest.hashCode() * (1+layerManifests.size());
		if(getId()!=null) {
			hash *= (1+getId().hashCode());
		}
		return hash;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} if(obj instanceof LayerGroupManifest) {
			LayerGroupManifest other = (LayerGroupManifest) obj;
			return contextManifest.equals(other.getContextManifest())
					&& layerManifests.size()==other.layerCount()
					&& ClassUtils.equals(getId(), other.getId());
		}
		return false;
	}

	private LayerManifest lookupLayer(final String id) {
		for(LayerManifest layerManifest : layerManifests) {
			if(id.equals(layerManifest.getId())) {
				return layerManifest;
			}
		}

		return null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LayerGroupManifest#getLayerManifest(java.lang.String)
	 */
	@Override
	public LayerManifest getLayerManifest(String id) {
		requireNonNull(id);

		return lookupLayer(id);
	}

	protected class LayerLink extends Link<ItemLayerManifest> {

		/**
		 * @param lazyResolver
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
			return (ItemLayerManifest) lookupLayer(getId());
		}

	}
}
