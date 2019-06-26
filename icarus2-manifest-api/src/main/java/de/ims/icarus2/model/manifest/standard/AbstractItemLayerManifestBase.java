/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import de.ims.icarus2.model.manifest.api.ContainerManifestBase;
import de.ims.icarus2.model.manifest.api.Hierarchy;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractItemLayerManifestBase<M extends ItemLayerManifestBase<M>>
		extends AbstractLayerManifest<M> implements ItemLayerManifestBase<M> {

	private Optional<Hierarchy<ContainerManifestBase<?>>> containerHierarchy = Optional.empty();

	private TargetLayerManifest boundaryLayerManifest;
	private TargetLayerManifest foundationLayerManifest;

	/**
	 * @param manifestLocation
	 * @param registry
	 * @param layerGroupManifest
	 */
	protected AbstractItemLayerManifestBase(ManifestLocation manifestLocation,
			ManifestRegistry registry, LayerGroupManifest layerGroupManifest) {
		super(manifestLocation, registry, layerGroupManifest);
	}

	protected AbstractItemLayerManifestBase(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		super(manifestLocation, registry, null);
	}

	protected AbstractItemLayerManifestBase(LayerGroupManifest layerGroupManifest) {
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
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifestBase#getContainerHierarchy()
	 */
	@Override
	public Optional<Hierarchy<ContainerManifestBase<?>>> getContainerHierarchy() {
		return getDerivable(containerHierarchy, ItemLayerManifestBase::getContainerHierarchy);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifestBase#setContainerHierarchy(de.ims.icarus2.model.manifest.api.Hierarchy)
	 */
	@Override
	public M setContainerHierarchy(Hierarchy<ContainerManifestBase<?>> hierarchy) {
		checkNotLocked();

		setContainerHierarchy0(hierarchy);

		return thisAsCast();
	}

	protected void setContainerHierarchy0(Hierarchy<ContainerManifestBase<?>> hierarchy) {
		this.containerHierarchy = Optional.of(hierarchy);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifestBase#hasLocalContainerHierarchy()
	 */
	@Override
	public boolean hasLocalContainerHierarchy() {
		return containerHierarchy.isPresent();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifestBaseBase#getRootContainerManifest()
	 */
	@Override
	public Optional<ContainerManifestBase<?>> getRootContainerManifest() {
		// Expects the root container to be located at level 0
		return getContainerHierarchy().flatMap(h -> h.tryLevel(Hierarchy.ROOT));
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifestBase#getBoundaryLayerManifest()
	 */
	@Override
	public Optional<TargetLayerManifest> getBoundaryLayerManifest() {
		return getDerivable(
				Optional.ofNullable(boundaryLayerManifest),
				ItemLayerManifestBase::getBoundaryLayerManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifestBase#isLocalBoundaryLayerManifest()
	 */
	@Override
	public boolean isLocalBoundaryLayerManifest() {
		return boundaryLayerManifest!=null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifestBase#setBoundaryLayerId(java.lang.String, java.util.function.Consumer)
	 */
	@Override
	public M setBoundaryLayerId(String boundaryLayerId, Consumer<? super TargetLayerManifest> action) {
		checkNotLocked();

		IcarusUtils.consumeIfAble(setBoundaryLayerId0(boundaryLayerId), action);

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
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifestBase#getFoundationLayerManifest()
	 */
	@Override
	public Optional<TargetLayerManifest> getFoundationLayerManifest() {
		return getDerivable(
				Optional.ofNullable(foundationLayerManifest),
				ItemLayerManifestBase::getFoundationLayerManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifestBase#isLocalFoundationLayerManifest()
	 */
	@Override
	public boolean isLocalFoundationLayerManifest() {
		return foundationLayerManifest!=null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifestBase#setFoundationLayerId(java.lang.String, java.util.function.Consumer)
	 */
	@Override
	public M setFoundationLayerId(String foundationLayerId,
			Consumer<? super TargetLayerManifest> action) {
		checkNotLocked();

		IcarusUtils.consumeIfAble(setFoundationLayerId0(foundationLayerId), action);

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
	 * Returns the local container hierarchy of an {@link ItemLayerManifestBase}.
	 * This method will create and {@link ItemLayerManifestBase#setContainerHierarchy(Hierarchy) set}
	 * a new {@link Hierarchy} instance if no local container hierarchy has been
	 * applied so far.
	 *
	 * @param layerManifest
	 * @return
	 */
	public static Hierarchy<ContainerManifestBase<?>> getOrCreateLocalContainerhierarchy(
			ItemLayerManifestBase<?> layerManifest) {
		if(layerManifest.hasLocalContainerHierarchy()) {
			return layerManifest.getContainerHierarchy().get();
		}

		Hierarchy<ContainerManifestBase<?>> hierarchy = new HierarchyImpl<>();
		layerManifest.setContainerHierarchy(hierarchy);
		return hierarchy;
	}
}
