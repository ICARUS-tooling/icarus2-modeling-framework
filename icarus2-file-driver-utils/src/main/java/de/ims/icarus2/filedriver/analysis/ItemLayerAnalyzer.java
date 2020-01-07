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
package de.ims.icarus2.filedriver.analysis;

import java.util.function.ObjLongConsumer;

import de.ims.icarus2.model.api.members.item.Item;

/**
 * An analyzer that consumes {@link Item items} to collect its metadata.
 *
 * @author Markus Gärtner
 *
 */
public interface ItemLayerAnalyzer extends Analyzer, ObjLongConsumer<Item> {

	/**
	 * Analyzer usable as empty fallback. This implementation does nothing,
	 * all methods simply return.
	 */
	public static final ItemLayerAnalyzer EMPTY_ANALYZER = new ItemLayerAnalyzer() {

		@Override
		public void accept(Item t, long value) {
			// no-op
		}
	};
}
