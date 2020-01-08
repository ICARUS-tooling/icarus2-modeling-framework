/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.layer;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.id.IdManager;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.ManifestOwner;
import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;

/**
 * A {@code ItemLayer} defines a collection of items with a well defined
 * order.
 *
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface ItemLayer extends Layer, ManifestOwner<ItemLayerManifestBase<?>> {

	/**
	 * Returns the shared {@code ItemLayerManifestBase} that holds
	 * information about markable composition and possible structures
	 * in this layer.
	 *
	 * @return The manifest that describes this collection of items
	 */
	@AccessRestriction(AccessMode.ALL)
	@Override
	ItemLayerManifestBase<?> getManifest();

	/**
	 * Short-hand method for fetching the {@link IdManager} that is used to map
	 * between ids and index values for this layer.
	 *
	 * @return
	 */
	IdManager getIdManager();

	/**
	 * Returns the single {@link Container#isProxy() proxy} container of this
	 * layer that all top-level members are connected to.
	 *
	 * @return
	 */
	Container getProxyContainer();

	/**
	 * Returns the {@code ItemLayer} that holds the bounding
	 * containers the elements in this layer correspond to. For
	 * example if a structural layer represents syntax trees for
	 * another layer that holds word tokens then this layer would
	 * be referenced via {@link Layer#getBaseLayers() a base layer}
	 * and the layer representing sentences would be accessed by
	 * this method. Note that for containers that do not correspond
	 * to the groups defined by other {@code ItemLayer}s this
	 * method is allowed to return {@code null}. A {@code non-null}
	 * return value is a hint for visualization facilities on
	 * how to link certain layers.
	 * <p>
	 * The main difference between the containers (C) of this layer and
	 * the ones returned by this method (B) are as follows:
	 * <ul>
	 * <li>Containers C do not have to hold all the elements in their
	 * <i>boundary container</i> B</li>
	 * <li>Containers C can define <i>virtual</i> items outside of
	 * the ones provided by B</li>
	 * <li>Containers B therefore define the <i>base</i> collection
	 * of items that is available for containers C to build upon</li>
	 * <li>For each C there has to be exactly one matching B</li>
	 * <li>Not every B is required to have a container C referencing it!</li>
	 * </ul>
	 * If the items in this layer are mere containers then the members
	 * of the boundary layer define borders that those containers are not allowed
	 * to span across.
	 * <p>
	 * This is an optional method.
	 *
	 * @return the {@code ItemLayer} holding boundary containers for
	 * the structures in this layer or {@code null} if the structures this
	 * layer defines are not mapped to existing layer boundaries.
	 */
	@AccessRestriction(AccessMode.ALL)
	ItemLayer getBoundaryLayer();

	/**
	 * Returns the (optional) foundation layer that the elements in this layer
	 * refer to in their {@link Item#getBeginOffset() beginOffset} and
	 * {@link Item#getEndOffset() endOffset} methods. If the method returns {@code null}
	 * the layer itself is assumed to be a foundation layer.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.ALL)
	ItemLayer getFoundationLayer();

	/**
	 * Defines the boundary layer to use for this layer.
	 * @param layer
	 *
	 * @throws NullPointerException iff the {@code layer} argument is {@code null}
	 * @throws ModelException in case the boundary layer has already been set
	 */
	void setBoundaryLayer(ItemLayer layer);

	/**
	 * Defines the boundary layer to use for this layer.
	 * @param layer
	 *
	 * @throws NullPointerException iff the {@code layer} argument is {@code null}
	 * @throws ModelException in case the foundation layer has already been set
	 */
	void setFoundationLayer(ItemLayer layer);

	default boolean isPrimaryLayer() {
		return this==getLayerGroup().getPrimaryLayer();
	}
}
