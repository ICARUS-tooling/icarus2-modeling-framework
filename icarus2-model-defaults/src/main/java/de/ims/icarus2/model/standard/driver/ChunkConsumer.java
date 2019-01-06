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