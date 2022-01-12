/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.driver.id;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;

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

	private final ItemLayerManifestBase<?> layerManifest;

	/**
	 * @param layerManifest
	 */
	public StaticContinuousIdManager(ItemLayerManifestBase<?> layerManifest) {
		requireNonNull(layerManifest);
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
	public ItemLayerManifestBase<?> getLayerManifest() {
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
