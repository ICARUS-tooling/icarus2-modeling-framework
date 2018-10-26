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
