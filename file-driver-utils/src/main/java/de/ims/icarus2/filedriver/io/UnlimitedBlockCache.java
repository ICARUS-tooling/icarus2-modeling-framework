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

 * $Revision: 410 $
 * $Date: 2015-06-26 16:44:18 +0200 (Fr, 26 Jun 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/driver/io/UnlimitedBlockCache.java $
 *
 * $LastChangedDate: 2015-06-26 16:44:18 +0200 (Fr, 26 Jun 2015) $
 * $LastChangedRevision: 410 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.filedriver.io;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import de.ims.icarus2.filedriver.io.BufferedIOResource.Block;
import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;

/**
 * @author Markus Gärtner
 * @version $Id: UnlimitedBlockCache.java 410 2015-06-26 14:44:18Z mcgaerty $
 *
 */
public class UnlimitedBlockCache implements BlockCache {

	private TIntObjectMap<Block> blocks;

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
		.append("[size=").append(blocks==null ? -1 : blocks.size())
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
		blocks = new TIntObjectHashMap<>((int)(capacity/0.75));
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache#close()
	 */
	@Override
	public void close() {
		blocks = null;
	}

}
