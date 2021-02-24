/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver.io;

import static de.ims.icarus2.util.lang.Primitives._int;

import de.ims.icarus2.filedriver.io.BufferedIOResource.Block;
import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Implements {@link BlockCache} without configurable restrictions regarding capacity.
 * The implementation uses a {@link TIntObjectMap} as internal storage and will never
 * purge entries. It will fail to add new entries once the map runs out of natural space
 * which is determined by the maximum size of an array and the load factor of the map
 * implementation.
 * <p>
 * Upon closing the entire map is deleted and gc'd.
 *
 * @author Markus Gärtner
 *
 */
public class UnlimitedBlockCache implements BlockCache {

	private Int2ObjectMap<Block> blocks;

	public UnlimitedBlockCache() {
		// no-op
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder()
		.append(getClass().getName())
		.append("[size=").append(blocks==null ? "<unopened>" : _int(blocks.size()))
		.append(']')
		.toString();
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache#getBlock(int)
	 */
	@Override
	public Block getBlock(int id) {
		return blocks.get(id);
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache#addBlock(de.ims.icarus2.filedriver.io.BufferedIOResource.Block, int)
	 */
	@Override
	public Block addBlock(Block block) {
		int id = block.getId();
		blocks.put(id, block);
		return null;
	}

	/**
	 * This implementation ignores the {@code capacity} limit and treats it as a starting
	 * size for the internal storage map.
	 *
	 * @see de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache#open(int)
	 */
	@Override
	public void open(int capacity) {
		blocks = new Int2ObjectOpenHashMap<>(capacity);
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache#close()
	 */
	@Override
	public void close() {
		blocks.clear(); // really needed?
		blocks = null;
	}

}
