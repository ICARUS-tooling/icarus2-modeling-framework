/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;




/**
 * Common base for both the {@link ItemLayerManifest} and {@link StructureLayerManifest}
 * class hierarchy.
 *
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface ItemLayerManifestBase<M extends ItemLayerManifestBase<M>> extends LayerManifest<M> {

	@AccessRestriction(AccessMode.READ)
	Optional<Hierarchy<ContainerManifestBase<?>>> getContainerHierarchy();

	@AccessRestriction(AccessMode.READ)
	boolean hasLocalContainerHierarchy();

	/**
	 * Returns the manifest for the top-level container in this layer
	 * or an empty {@link Optional} if no container manifests have been added so far.
	 * Note that usually this will always be a manifest describing a list
	 * type container.
	 * <p>
	 * This is a shorthand method for accessing the {@link Hierarchy#getRoot() root}
	 * of the {@link #getContainerHierarchy() container hierarchy}.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<ContainerManifestBase<?>> getRootContainerManifest();

	/**
	 * Returns the {@link ItemLayerManifestBase} that describes the layer hosting
	 * <i>boundary containers</i> for the items in this manifests'
	 * {@code ItemLayer}. If the items are not restricted by <i>boundary containers</i>
	 * this method should return an empty {@link Optional}.
	 * <p>
	 * Being restricted by a <i>boundary container</i> means that all non-virtual members of a
	 * container (or structure) must reside within the same range of indices defined by the boundary.
	 * So for example in the case of containers they are not allowed to span across borders of
	 * their respective <i>boundary container</i>.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<TargetLayerManifest> getBoundaryLayerManifest();

	boolean isLocalBoundaryLayerManifest();

	/**
	 * Returns the {@link ItemLayerManifestBase} that specifies the granularity of atomic elements
	 * this layer builds upon.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<TargetLayerManifest> getFoundationLayerManifest();

	boolean isLocalFoundationLayerManifest();

	default boolean isPrimaryLayerManifest() {
		return getGroupManifest()
				.flatMap(LayerGroupManifest::getPrimaryLayerManifest)
				.orElse(null)==this;
	}

	// Modification methods

	M setContainerHierarchy(Hierarchy<ContainerManifestBase<?>> hierarchy);

	default TargetLayerManifest setAndGetBoundaryLayer(String boundaryLayerId) {
		return IcarusUtils.extractSupplied(action -> setBoundaryLayerId(boundaryLayerId, action));
	}

	M setBoundaryLayerId(String boundaryLayerId,
			@Nullable Consumer<? super TargetLayerManifest> action);

	default TargetLayerManifest setAndGetFoundationLayer(String foundationLayerId) {
		return IcarusUtils.extractSupplied(action -> setFoundationLayerId(foundationLayerId, action));
	}

	M setFoundationLayerId(String foundationLayerId,
			@Nullable Consumer<? super TargetLayerManifest> action);
}
