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
package de.ims.icarus2.model.api.driver.id;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;


/**
 * @author Markus Gärtner
 *
 */
public interface IdManager extends AutoCloseable {

	/**
	 * Returns the manifest describing the layer this manager stores ids for.
	 *
	 * @return
	 */
	public ItemLayerManifest getLayerManifest();

	/**
	 * Maps a given {@code index} to the {@link Item#getId() id} of the item at that position.
	 *
	 * @param index
	 * @return
	 */
	long getIdAt(long index);

	/**
	 * Performs the inverse lookup operation of {@link #getIdAt(long)}, returning the position
	 * in a layer where the item with a given {@code id} is located.
	 *
	 * @param id
	 * @return
	 */
	long indexOfId(long id);

	public static class IdentityIdManager implements IdManager {
		private ItemLayerManifest layerManifest;

		/**
		 * @param layerManifest
		 */
		public IdentityIdManager(ItemLayerManifest layerManifest) {
			requireNonNull(layerManifest);

			this.layerManifest = layerManifest;
		}

		/**
		 * Clears the link to this manager's {@link ItemLayerManifest}
		 * layer manifest.
		 *
		 * @see java.lang.AutoCloseable#close()
		 */
		@Override
		public void close() throws Exception {
			layerManifest = null;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.id.IdManager#getLayerManifest()
		 */
		@Override
		public ItemLayerManifest getLayerManifest() {
			checkState("Manager already closed", layerManifest!=null);

			return layerManifest;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.id.IdManager#getIdAt(long)
		 */
		@Override
		public long getIdAt(long index) {
			return index;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.id.IdManager#indexOfId(long)
		 */
		@Override
		public long indexOfId(long id) {
			return id;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return layerManifest.getUID();
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return getClass().getName()+"@"+layerManifest.getUniqueId();
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} else if(obj instanceof IdentityIdManager) {
				return layerManifest==((IdentityIdManager)obj).layerManifest;
			}
			return false;
		}
	}
}
