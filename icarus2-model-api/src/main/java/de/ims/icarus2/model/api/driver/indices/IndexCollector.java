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
package de.ims.icarus2.model.api.driver.indices;

import java.util.PrimitiveIterator.OfInt;
import java.util.PrimitiveIterator.OfLong;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;


/**
 * @author Markus Gärtner
 *
 */
public interface IndexCollector extends LongConsumer, IntConsumer, Consumer<IndexSet> {

	void add(long index);

	default void add(long fromIndex, long toIndex) {
		for(long index = fromIndex; index <= toIndex; index++) {
			add(index);
		}
	}

	default void add(IndexSet indices) {
		indices.forEachIndex((LongConsumer)this);
	}

	default void add(IndexSet[] indices) {
		for(IndexSet set : indices) {
			add(set);
		}
	}

	@Override
	default void accept(long value) {
		add(value);
	}

	@Override
	default void accept(int value) {
		add(value);
	}

	@Override
	default void accept(IndexSet indices) {
		add(indices);
	}

	default void add(OfLong source) {
		source.forEachRemaining(this);
	}

	default void add(OfInt source) {
		source.forEachRemaining(this);
	}
}
