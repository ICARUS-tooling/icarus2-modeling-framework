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

import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.Hierarchy;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;

/**
 * @author Markus Gärtner
 *
 */
public class ItemLayerManifestImpl extends AbstractLayerManifest<ItemLayerManifest> implements ItemLayerManifest {

	private Hierarchy<ContainerManifest> containerHierarchy;

	private TargetLayerManifest boundaryLayerManifest;
	private TargetLayerManifest foundationLayerManifest;

	/**
	 * @param manifestLocation
	 * @param registry
	 * @param layerGroupManifest
	 */
	public ItemLayerManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, LayerGroupManifest layerGroupManifest) {
		super(manifestLocation, registry, layerGroupManifest);
	}

	public ItemLayerManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		this(manifestLocation, registry, null);
	}

	public ItemLayerManifestImpl(LayerGroupManifest layerGroupManifest) {
		this(layerGroupManifest.getContextManifest().getManifestLocation(),
				layerGroupManifest.getContextManifest().getRegistry(), layerGroupManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && boundaryLayerManifest==null && containerHierarchy==null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.ITEM_LAYER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#getContainerHierarchy()
	 */
	@Override
	public Hierarchy<ContainerManifest> getContainerHierarchy() {
		Hierarchy<ContainerManifest> result = containerHierarchy;
		if(result==null && hasTemplate()) {
			result = getTemplate().getContainerHierarchy();
		}
		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#setContainerHierarchy(de.ims.icarus2.model.manifest.api.Hierarchy)
	 */
	@Override
	public void setContainerHierarchy(Hierarchy<ContainerManifest> hierarchy) {
		checkNotLocked();

		setContainerHierarchy0(hierarchy);
	}

	protected void setContainerHierarchy0(Hierarchy<ContainerManifest> hierarchy) {
		requireNonNull(hierarchy);

		this.containerHierarchy = hierarchy;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#hasLocalContainerHierarchy()
	 */
	@Override
	public boolean hasLocalContainerHierarchy() {
		return containerHierarchy!=null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#getRootContainerManifest()
	 */
	@Override
	public ContainerManifest getRootContainerManifest() {
		Hierarchy<ContainerManifest> hierarchy = getContainerHierarchy();
		return hierarchy==null ? null : hierarchy.getRoot();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#getBoundaryLayerManifest()
	 */
	@Override
	public TargetLayerManifest getBoundaryLayerManifest() {
		TargetLayerManifest result = boundaryLayerManifest;

		if(result==null && hasTemplate()) {
			result = getTemplate().getBoundaryLayerManifest();
		}

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#isLocalBoundaryLayerManifest()
	 */
	@Override
	public boolean isLocalBoundaryLayerManifest() {
		return boundaryLayerManifest!=null;
	}

	/**
	 * @param boundaryLayerManifest the boundaryLayerManifest to set
	 */
	@Override
	public TargetLayerManifest setBoundaryLayerId(String boundaryLayerId) {
		checkNotLocked();

		return setBoundaryLayerId0(boundaryLayerId);
	}

	protected TargetLayerManifest setBoundaryLayerId0(String boundaryLayerId) {
		checkAllowsTargetLayer();
		requireNonNull(boundaryLayerId);

		TargetLayerManifest manifest = createTargetLayerManifest(boundaryLayerId);
		boundaryLayerManifest = manifest;
		return manifest;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#getFoundationLayerManifest()
	 */
	@Override
	public TargetLayerManifest getFoundationLayerManifest() {
		TargetLayerManifest result = foundationLayerManifest;

		if(result==null && hasTemplate()) {
			result = getTemplate().getFoundationLayerManifest();
		}

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#isLocalFoundationLayerManifest()
	 */
	@Override
	public boolean isLocalFoundationLayerManifest() {
		return foundationLayerManifest!=null;
	}

	/**
	 * @param boundaryLayerManifest the boundaryLayerManifest to set
	 */
	@Override
	public TargetLayerManifest setFoundationLayerId(String foundationLayerId) {
		checkNotLocked();

		return setFoundationLayerId0(foundationLayerId);
	}

	protected TargetLayerManifest setFoundationLayerId0(String foundationLayerId) {
		checkAllowsTargetLayer();
		requireNonNull(foundationLayerId);

		TargetLayerManifest manifest = createTargetLayerManifest(foundationLayerId);
		foundationLayerManifest = manifest;
		return manifest;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#lock()
	 */
	@Override
	public void lock() {
		super.lock();

		lockNested(containerHierarchy);
	}
}
