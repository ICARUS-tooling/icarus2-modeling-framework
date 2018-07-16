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
 */
package de.ims.icarus2.model.api.driver;

import de.ims.icarus2.model.api.members.item.Item;

/**
 * @author Markus Gärtner
 *
 */
public enum ChunkState {

	/**
	 * Chunk in healthy state
	 */
	VALID(1),

	/**
	 * Chunk in inconsistent state but still valid.
	 * Client code should take this as an indicator
	 * to wait for a driver's background operation
	 * that involves the chunk in question to finish.
	 */
	MODIFIED(2),

	/**
	 * Chunk contains invalid data, has experienced
	 * untracked changes outside of driver's control
	 * or an error occurred that prevented it from
	 * being properly loaded.
	 * <p>
	 * Client code should not use the chunk.
	 */
	CORRUPTED(3),
	;

	private final int statusCode;

	ChunkState(int statusCode) {
		this.statusCode = statusCode;
	}

	public int getStatusCode() {
		return statusCode;
	}

	@Override
	public String toString() {
		return name()+" ("+statusCode+")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns {@link #VALID} in case the item is {@link Item#isUsable() usable}
	 * and {@link #CORRUPTED} otherwise.
	 *
	 * @param item
	 * @return
	 */
	public static ChunkState forItem(Item item) {
		return item.isUsable() ? VALID : CORRUPTED;
	}
}
