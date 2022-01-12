/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.query.api.exp;

import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.LongConsumer;

import de.ims.icarus2.util.MutablePrimitives.Primitive;

/**
 * A proxy to model arbitrary random-access data structures.
 *
 * @author Markus Gärtner
 *
 */
//TODO for now we restrict indices to int space, but Container for instance uses long
public interface ListProxy<T> {

	/** Total number of elements in this proxy. */
	int size();

	/** Fetch element at specified {@code index}. */
	T get(int index);

	default void forEachItem(Consumer<? super T> action) {
		requireNonNull(action);
		int size = size();
		for (int i = 0; i < size; i++) {
			action.accept(get(i));
		}
	}

	public interface OfInteger extends ListProxy<Primitive<Long>> {
		long getAsLong(int index);

		default int getAsInt(int index) {
			return strictToInt(getAsLong(index));
		}

		default void forEachInteger(LongConsumer action) {
			requireNonNull(action);
			int size = size();
			for (int i = 0; i < size; i++) {
				action.accept(getAsLong(i));
			}
		}
	}

	public interface OfFloatingPoint extends ListProxy<Primitive<Double>> {
		double getAsDouble(int index);

		default void forEachFloatingPoint(DoubleConsumer action) {
			requireNonNull(action);
			int size = size();
			for (int i = 0; i < size; i++) {
				action.accept(getAsDouble(i));
			}
		}
	}

	public interface OfBoolean extends ListProxy<Primitive<Boolean>> {
		boolean getAsBoolean(int index);
	}
}
