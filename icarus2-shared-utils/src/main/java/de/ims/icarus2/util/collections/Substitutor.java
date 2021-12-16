/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.collections;

import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

/**
 * Implements (re)substitution of arbitrary types via a backing {@link LookupList}.
 * Note that the back-end storage only ever grows through the lifetime of any instance
 * of this class!
 *
 * @author Markus Gärtner
 *
 * @param <T> type of values to be substituted (must be immutable
 * wrt {@link Object#equals(Object)})
 */
//TODO we need a high-performance alternative for this class specialized on CharSequence objects!
public class Substitutor<T> implements Closeable, ToIntFunction<T>, IntFunction<T> {

	/**
	 * Sentinel value to signal an empty or {@code null} value.
	 * Chosen to be {@code 0} so that empty storage doesn't need
	 * any pre-processing.
	 */
	private static final int EMPTY_VALUE = 0;

	private final LookupList<T> buffer;
	private final boolean clearOnClose;

	public Substitutor(LookupList<T> buffer, boolean clearOnClose) {
		this.buffer = requireNonNull(buffer);
		this.clearOnClose = clearOnClose;
	}

	public Substitutor(int capacity) {
		buffer = new LookupList<>(capacity);
		clearOnClose = true;
	}

	public Substitutor() {
		buffer = new LookupList<>();
		clearOnClose = true;
	}

	/** Encodes the given {@code value} into an integer replacement.
	 */
	@Override
	public int applyAsInt(T value) {
		if(value==null) {
			return EMPTY_VALUE;
		}

		int index = buffer.indexOf(value);
		if(index==UNSET_INT) {
			synchronized (buffer) {
				index = buffer.size();
				buffer.add(index, value);
			}
		}
		return index+1;
	}

	/** Decodes the given integer replacement back into a proper object */
	@Override
	public T apply(int value) {
		return value==EMPTY_VALUE ? null : buffer.get(value-1);
	}

	@Override
	public void close() {
		if(clearOnClose) {
			buffer.clear();
		}
	}
}