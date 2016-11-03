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
package de.ims.icarus2.filedriver.io;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import de.ims.icarus2.filedriver.io.BufferedIOResource.Block;
import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;

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
		.append("[size=").append(blocks==null ? "<unopened>" : blocks.size())
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
	public Block addBlock(Block block, int id) {
		blocks.put(id, block);
		return null;
	}

	/**
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
		blocks = null;
	}

}
