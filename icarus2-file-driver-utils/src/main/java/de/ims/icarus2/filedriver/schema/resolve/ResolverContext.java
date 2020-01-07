/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver.schema.resolve;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;

/**
 * @author Markus Gärtner
 *
 */
public interface ResolverContext {

	/**
	 * Returns the currently active host container
	 *
	 * @return
	 */
	Container currentContainer();

	/**
	 * Returns the index within the current container that reading is taking place at
	 *
	 * @return
	 */
	long currentIndex();

	/**
	 * Returns the item with which currently read data should be associated
	 *
	 * @return
	 */
	Item currentItem();

	/**
	 * Returns the current raw input data that should be read and converted
	 *
	 * @return
	 */
	CharSequence rawData();

	/**
	 * Tells the context that the current raw input line should be treated as being consumed,
	 * meaning that the code controlling the read operation must not pass it on to other
	 * subsequent tasks.
	 */
	default void consumeData() {
		// no-op
	}
}
