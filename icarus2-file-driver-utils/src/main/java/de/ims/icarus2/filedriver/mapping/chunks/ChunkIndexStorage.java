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
package de.ims.icarus2.filedriver.mapping.chunks;

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Set;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

/**
 * @author Markus Gärtner
 *
 */
public class ChunkIndexStorage {

	private final Int2ObjectMap<ChunkIndex> chunkIndexMap = new Int2ObjectOpenHashMap<>();
	private final Set<ChunkIndex> chunkIndices = new ReferenceOpenHashSet<>();

	protected ChunkIndexStorage(Builder builder) {
		requireNonNull(builder);

		chunkIndexMap.putAll(builder.chunkIndexMap);
		chunkIndices.addAll(chunkIndexMap.values());
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
		requireNonNull(layer);

		int id = layer.getUID();
		ChunkIndex chunkIndex = chunkIndexMap.get(id);
		if(chunkIndex==null)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"No chunk index available for layer: "+getName(layer));

		return chunkIndex;
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class Builder {

		private final Int2ObjectMap<ChunkIndex> chunkIndexMap = new Int2ObjectOpenHashMap<>();

		public Builder add(ItemLayerManifest layer, ChunkIndex chunkIndex) {
			requireNonNull(layer);
			requireNonNull(chunkIndex);

			int id = layer.getUID();

			if(chunkIndexMap.containsKey(id))
				throw new ModelException(GlobalErrorCode.INVALID_INPUT,
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
