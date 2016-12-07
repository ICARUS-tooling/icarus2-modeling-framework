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
 */
package de.ims.icarus2.model.api.driver.id;

import static de.ims.icarus2.util.Conditions.checkNotNull;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;

/**
 * Implements an {@link IdManager} that reflects the simple identity relation between
 * an {@link Item item's} {@link Item#getId() id} and its {@link Item#getIndex() index}.
 * This sort of relation is typical for immutable resources and can still be applied to
 * non-static resources when it can be guaranteed that new data will only ever get appended
 * (for example incrementally growing corpus resources where the "old" parts of primary
 * data will never undergo any form of structural change).
 *
 * @author Markus Gärtner
 *
 */
public class StaticContinuousIdManager implements IdManager {

	//TODO maybe we should at least verify for each parameter that it is a valid id/index ? maybe shift requirement up to IdManager interface contract

	private final ItemLayerManifest layerManifest;

	/**
	 * @param layerManifest
	 */
	public StaticContinuousIdManager(ItemLayerManifest layerManifest) {
		checkNotNull(layerManifest);
		this.layerManifest = layerManifest;
	}

	/**
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.id.IdManager#getLayerManifest()
	 */
	@Override
	public ItemLayerManifest getLayerManifest() {
		return layerManifest;
	}

	/**
	 * This implementations simply returns the given {@code index} as the
	 * respective {@code id}.
	 *
	 * @see de.ims.icarus2.model.api.driver.id.IdManager#getIdAt(long)
	 */
	@Override
	public long getIdAt(long index) {
		return index;
	}

	/**
	 * This implementations simply returns the given {@code id} as the
	 * respective {@code index}.
	 *
	 * @see de.ims.icarus2.model.api.driver.id.IdManager#indexOfId(long)
	 */
	@Override
	public long indexOfId(long id) {
		return id;
	}

}
