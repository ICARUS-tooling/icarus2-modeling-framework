/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision: 382 $
 *
 */
package de.ims.icarus2.model.api.layer;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
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
public interface ItemLayer extends Layer, ManifestOwner<ItemLayerManifest> {

	/**
	 * Returns the shared {@code ItemLayerManifest} that holds
	 * information about markable composition and possible structures
	 * in this layer.
	 *
	 * @return The manifest that describes this collection of items
	 */
	@AccessRestriction(AccessMode.ALL)
	@Override
	ItemLayerManifest getManifest();

	/**
	 * Returns the {@code ItemLayer} that holds the bounding
	 * containers the elements in this layer correspond to. For
	 * example if a structural layer represents syntax trees for
	 * another layer that holds word tokens then this layer would
	 * be referenced via {@link Layer#getBaseLayer()} and the
	 * layer representing sentences would be accessed by
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
	 * @throws ModelException in case the value layer has already been set
	 */
	void setBoundaryLayer(ItemLayer layer);

	/**
	 * Defines the boundary layer to use for this layer.
	 * @param layer
	 *
	 * @throws NullPointerException iff the {@code layer} argument is {@code null}
	 * @throws ModelException in case the value layer has already been set
	 */
	void setFoundationLayer(ItemLayer layer);
}
