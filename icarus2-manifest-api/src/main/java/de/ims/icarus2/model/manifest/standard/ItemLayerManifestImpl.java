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

import java.util.Optional;
import java.util.function.Consumer;

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

	private Optional<Hierarchy<ContainerManifest>> containerHierarchy = Optional.empty();

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
		super(manifestLocation, registry, null);
	}

	public ItemLayerManifestImpl(LayerGroupManifest layerGroupManifest) {
		super(layerGroupManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && boundaryLayerManifest==null && !containerHierarchy.isPresent();
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
	public Optional<Hierarchy<ContainerManifest>> getContainerHierarchy() {
		return getDerivable(containerHierarchy, ItemLayerManifest::getContainerHierarchy);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#setContainerHierarchy(de.ims.icarus2.model.manifest.api.Hierarchy)
	 */
	@Override
	public ItemLayerManifest setContainerHierarchy(Hierarchy<ContainerManifest> hierarchy) {
		checkNotLocked();

		setContainerHierarchy0(hierarchy);

		return thisAsCast();
	}

	protected void setContainerHierarchy0(Hierarchy<ContainerManifest> hierarchy) {
		this.containerHierarchy = Optional.of(hierarchy);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#hasLocalContainerHierarchy()
	 */
	@Override
	public boolean hasLocalContainerHierarchy() {
		return containerHierarchy.isPresent();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#getRootContainerManifest()
	 */
	@Override
	public Optional<ContainerManifest> getRootContainerManifest() {
		// Expects the root container to be located at level 0
		return getContainerHierarchy().flatMap(h -> h.tryLevel(Hierarchy.ROOT));
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#getBoundaryLayerManifest()
	 */
	@Override
	public Optional<TargetLayerManifest> getBoundaryLayerManifest() {
		return getDerivable(
				Optional.ofNullable(boundaryLayerManifest),
				ItemLayerManifest::getBoundaryLayerManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#isLocalBoundaryLayerManifest()
	 */
	@Override
	public boolean isLocalBoundaryLayerManifest() {
		return boundaryLayerManifest!=null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#setBoundaryLayerId(java.lang.String, java.util.function.Consumer)
	 */
	@Override
	public ItemLayerManifest setBoundaryLayerId(String boundaryLayerId, Consumer<? super TargetLayerManifest> action) {
		checkNotLocked();

		setBoundaryLayerId0(boundaryLayerId);

		return thisAsCast();
	}

	protected TargetLayerManifest setBoundaryLayerId0(String boundaryLayerId) {
		checkAllowsTargetLayer();
		requireNonNull(boundaryLayerId);

		TargetLayerManifest manifest = createTargetLayerManifest(boundaryLayerId, "boundary layer");
		boundaryLayerManifest = manifest;
		return manifest;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#getFoundationLayerManifest()
	 */
	@Override
	public Optional<TargetLayerManifest> getFoundationLayerManifest() {
		return getDerivable(
				Optional.ofNullable(foundationLayerManifest),
				ItemLayerManifest::getFoundationLayerManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#isLocalFoundationLayerManifest()
	 */
	@Override
	public boolean isLocalFoundationLayerManifest() {
		return foundationLayerManifest!=null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#setFoundationLayerId(java.lang.String, java.util.function.Consumer)
	 */
	@Override
	public ItemLayerManifest setFoundationLayerId(String foundationLayerId,
			Consumer<? super TargetLayerManifest> action) {
		checkNotLocked();

		setFoundationLayerId0(foundationLayerId);

		return thisAsCast();
	}

	protected TargetLayerManifest setFoundationLayerId0(String foundationLayerId) {
		checkAllowsTargetLayer();
		requireNonNull(foundationLayerId);

		TargetLayerManifest manifest = createTargetLayerManifest(foundationLayerId, "foundation layer");
		foundationLayerManifest = manifest;
		return manifest;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#lock()
	 */
	@Override
	protected void lockNested() {
		super.lockNested();

		lockNested(containerHierarchy);
	}

	/**
	 * Returns the local container hierarchy of an {@link ItemLayerManifest}.
	 * This method will create and {@link ItemLayerManifest#setContainerHierarchy(Hierarchy) set}
	 * a new {@link Hierarchy} instance if no local container hierarchy has been
	 * applied so far.
	 *
	 * @param layerManifest
	 * @return
	 */
	public static Hierarchy<ContainerManifest> getOrCreateLocalContainerhierarchy(
			ItemLayerManifest layerManifest) {
		if(layerManifest.hasLocalContainerHierarchy()) {
			return layerManifest.getContainerHierarchy().get();
		} else {
			Hierarchy<ContainerManifest> hierarchy = new HierarchyImpl<>();
			layerManifest.setContainerHierarchy(hierarchy);
			return hierarchy;
		}
	}
}
