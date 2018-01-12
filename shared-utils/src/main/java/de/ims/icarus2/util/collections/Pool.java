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
