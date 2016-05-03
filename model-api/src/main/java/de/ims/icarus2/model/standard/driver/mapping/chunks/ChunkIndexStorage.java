/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 411 $
 * $Date: 2015-06-26 17:07:19 +0200 (Fr, 26 Jun 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/driver/mapping/chunks/ChunkIndexStorage.java $
 *
 * $LastChangedDate: 2015-06-26 17:07:19 +0200 (Fr, 26 Jun 2015) $
 * $LastChangedRevision: 411 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.driver.mapping.chunks;

import static de.ims.icarus2.model.standard.util.CorpusUtils.getName;
import static de.ims.icarus2.model.util.Conditions.checkNotNull;
import static de.ims.icarus2.model.util.Conditions.checkState;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TCustomHashSet;
import gnu.trove.strategy.IdentityHashingStrategy;

import java.util.Set;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id: ChunkIndexStorage.java 411 2015-06-26 15:07:19Z mcgaerty $
 *
 */
public class ChunkIndexStorage {

	private final TIntObjectMap<ChunkIndex> chunkIndexMap = new TIntObjectHashMap<>();
	private final Set<ChunkIndex> chunkIndices = new TCustomHashSet<>(IdentityHashingStrategy.INSTANCE);

	protected ChunkIndexStorage(Builder builder) {
		checkNotNull(builder);

		chunkIndexMap.putAll(builder.chunkIndexMap);
		chunkIndices.addAll(chunkIndexMap.valueCollection());
	}

	public Set<ChunkIndex> getChunkIndices() {
		return CollectionUtils.getSetProxy(chunkIndices);
	}

	public boolean hasChunkIndex(ItemLayer layer) {
		return hasChunkIndex(layer.getManifest());
	}

	public boolean hasChunkIndex(ItemLayerManifest layer) {
		return chunkIndexMap.containsKey(layer.getUID());
	}

	public ChunkIndex getChunkIndex(ItemLayer layer) {
		return getChunkIndex(layer.getManifest());
	}

	public ChunkIndex getChunkIndex(ItemLayerManifest layer) {
		checkNotNull(layer);

		int id = layer.getUID();
		ChunkIndex chunkIndex = chunkIndexMap.get(id);
		if(chunkIndex==null)
			throw new ModelException(ModelErrorCode.INVALID_INPUT,
					"No chunk index available for layer: "+getName(layer));

		return chunkIndex;
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id: ChunkIndexStorage.java 411 2015-06-26 15:07:19Z mcgaerty $
	 *
	 */
	public static class Builder {

		private final TIntObjectMap<ChunkIndex> chunkIndexMap = new TIntObjectHashMap<>();

		public Builder add(ItemLayerManifest layer, ChunkIndex chunkIndex) {
			checkNotNull(layer);
			checkNotNull(chunkIndex);

			int id = layer.getUID();

			if(chunkIndexMap.containsKey(id))
				throw new ModelException(ModelErrorCode.INVALID_INPUT,
						"UID for layer is already mapped: "+getName(layer));

			chunkIndexMap.put(id, chunkIndex);

			return this;
		}

		protected void validate() {
			checkState("No chunk indices defined", !chunkIndexMap.isEmpty());
		}

		public ChunkIndexStorage build() {
			validate();

			return new ChunkIndexStorage(this);
		}
	}
}
