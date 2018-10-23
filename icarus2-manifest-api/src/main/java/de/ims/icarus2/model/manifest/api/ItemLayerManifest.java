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
package de.ims.icarus2.model.manifest.api;

import java.util.Optional;

import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;




/**
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface ItemLayerManifest extends LayerManifest {

	@AccessRestriction(AccessMode.READ)
	Optional<Hierarchy<ContainerManifest>> getContainerHierarchy();

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
	Optional<ContainerManifest> getRootContainerManifest();

	/**
	 * Returns the {@link ItemLayerManifest} that describes the layer hosting
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
	 * Returns the {@link ItemLayerManifest} that specifies the granularity of atomic elements
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

	void setContainerHierarchy(Hierarchy<ContainerManifest> hierarchy);

	TargetLayerManifest setBoundaryLayerId(String boundaryLayerId);

	TargetLayerManifest setFoundationLayerId(String foundationLayerId);
}
