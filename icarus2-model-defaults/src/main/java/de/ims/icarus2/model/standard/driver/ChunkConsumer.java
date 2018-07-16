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
package de.ims.icarus2.model.standard.driver;

import de.ims.icarus2.model.api.driver.ChunkState;
import de.ims.icarus2.model.api.members.item.Item;

/**
 *
 * @author Markus Gärtner
 *
 */
@FunctionalInterface
public interface ChunkConsumer {

	/**
	 * Consumes information about an individual chunk.
	 *
	 * @param index the index or id of the chunk in question
	 * @param item the model instance for the given {@code index} or {@code null}
	 * 		  if loading for that chunk hasn't been successful.
	 * @param state the resulting state of the chunk after loading
	 */
	void accept(long index, Item item, ChunkState state);

	/**
	 * Optional helper method to facilitate buffered consumption of
	 * chunk information. Usually calls to {@link #accept(long, Item, ChunkState)}
	 * will be made by producer code for a large number of chunks without
	 * passing on information whether or not an individual chunk is the last
	 * one to be processed. After all relevant chunks have been send
	 * to the consumer, a call to this method will signal the end of
	 * the (loading) process and allow the consumer to wrap up its
	 * operation.
	 * <p>
	 * The default implementation does nothing
	 */
	default void flush() {
		// no-op
	}
}