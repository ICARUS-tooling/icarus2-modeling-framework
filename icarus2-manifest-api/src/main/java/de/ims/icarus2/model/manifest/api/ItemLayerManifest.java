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

import de.ims.icarus2.model.manifest.standard.HierarchyImpl;
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
	Hierarchy<ContainerManifest> getContainerHierarchy();

	@AccessRestriction(AccessMode.READ)
	boolean hasLocalContainerHierarchy();

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
			return layerManifest.getContainerHierarchy();
		} else {
			Hierarchy<ContainerManifest> hierarchy = new HierarchyImpl<>();
			layerManifest.setContainerHierarchy(hierarchy);
			return hierarchy;
		}
	}

	/**
	 * Returns the manifest for the top-level container in this layer
	 * or {@code null} if no container manifests have been added so far.
	 * Note that usually this will always be a manifest describing a list
	 * type container.
	 * <p>
	 * This is a shorthand method for accessing the {@link Hierarchy#getRoot() root}
	 * of the {@link #getContainerHierarchy() container hierarchy}.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	ContainerManifest getRootContainerManifest();

	/**
	 * Returns the {@code ItemLayerManifest} that describes the layer hosting
	 * <i>boundary containers</i> for the items in this manifests'
	 * {@code ItemLayer}. If the items are not restricted by <i>boundary containers</i>
	 * this method should return {@code null}.
	 * <p>
	 * Being restricted by a <i>boundary container</i> means that all non-virtual members of a
	 * container (or structure) must reside within the same range of indices defined by the boundary.
	 * So for example in the case of containers they are not allowed to span across borders of
	 * their respective <i>boundary container</i>.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	TargetLayerManifest getBoundaryLayerManifest();

	boolean isLocalBoundaryLayerManifest();

	/**
	 * Returns the
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	TargetLayerManifest getFoundationLayerManifest();

	boolean isLocalFoundationLayerManifest();

	default boolean isPrimaryLayerManifest() {
		LayerGroupManifest groupManifest = getGroupManifest();
		return groupManifest!=null && groupManifest.getPrimaryLayerManifest()==this;
	}

	// Modification methods

	void setContainerHierarchy(Hierarchy<ContainerManifest> hierarchy);

	TargetLayerManifest setBoundaryLayerId(String boundaryLayerId);

	TargetLayerManifest setFoundationLayerId(String foundationLayerId);
}
