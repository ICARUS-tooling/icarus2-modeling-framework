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
package de.ims.icarus2.util.collections;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Stack;

import com.google.common.base.Supplier;

/**
 * @author Markus Gärtner
 *
 */
public class Pool<E extends Object> implements AutoCloseable {

	public static final int DEFAULT_CAPACITY = 10;

	private final Supplier<? extends E> supplier;

	private final int capacity;

	/**
	 * Actual pool content.
	 * Must make sure to let no {@code null} objects sneak
	 * in here.
	 */
	private final Stack<E> buffer;

	public Pool(Supplier<? extends E> supplier) {
		this(supplier, DEFAULT_CAPACITY);
	}

	public Pool(Supplier<? extends E> supplier, int capacity) {
		requireNonNull(supplier);
		checkArgument("Capacity must be positive", capacity>0);

		this.supplier = supplier;
		this.capacity = capacity;
		this.buffer = new Stack<>();
	}

	public E get() {
		// Refactored from using the EmptyStackException for control flow to just synchronizing
		synchronized (buffer) {
			if(buffer.isEmpty()) {
				return supplier.get();
			} else {
				return buffer.pop();
			}
		}
	}

	public void recycle(E element) {
		requireNonNull(element);

		if(buffer.size()<capacity) {
			buffer.push(element);
		}
	}

	public int size() {
		return buffer.size();
	}

	public int capacity() {
		return capacity;
	}

	public boolean isEmpty() {
		return buffer.isEmpty();
	}

	/**
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() {
		buffer.clear();
	}
}
