/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
