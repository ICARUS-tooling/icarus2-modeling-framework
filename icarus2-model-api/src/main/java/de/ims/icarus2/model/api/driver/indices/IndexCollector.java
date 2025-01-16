/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.util.Conditions.checkArgument;

import java.util.List;
import java.util.PrimitiveIterator.OfInt;
import java.util.PrimitiveIterator.OfLong;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;


/**
 * Provides a broad way of collecting index values. Ultimately all
 * the default methods in this interface delegate to the {@link #add(long)}
 * method for single values.
 * <p>
 * Implementations are encouraged to also override the {@link #add(IndexSet)}
 * method, as all methods dealing with {@link IndexSet} objects as input also
 * delegate to this method.
 *
 * @author Markus Gärtner
 *
 */
@FunctionalInterface
public interface IndexCollector extends LongConsumer, IntConsumer, Consumer<IndexSet> {

	/**
	 * Collects a single value.
	 * <p>
	 * This method is the delegation target for single value
	 * operations.
	 *
	 * @param index
	 */
	void add(long index);

	/**
	 * Collects a continuous range of values.
	 *
	 * @param fromIndex
	 * @param toIndex
	 * @throws IllegalArgumentException if either of the two boundary values
	 * is negative or if {@code fromIndex>toIndex}
	 */
	default void add(long fromIndex, long toIndex) {
		checkArgument(fromIndex>=0 && fromIndex<=toIndex);
		for(long index = fromIndex; index <= toIndex; index++) {
			add(index);
		}
	}

	/**
	 * Adds an entire {@link IndexSet}.
	 * <p>
	 * This method is the delegation target for batch operations.
	 * @param indices
	 */
	default void add(IndexSet indices) {
		indices.forEachIndex((LongConsumer)this);
	}

	// DELEGATING METHODS

	default void add(IndexSet[] indices) {
		for(IndexSet set : indices) {
			add(set);
		}
	}

	default void add(List<? extends IndexSet> indices) {
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
