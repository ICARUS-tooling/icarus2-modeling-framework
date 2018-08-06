/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * Not thread-safe!
 *
 * @author Markus Gärtner
 *
 */
public class LazyCollection<E extends Object> implements Consumer<E> {

	public static <E extends Object> LazyCollection<E> lazyList() {
		return new LazyCollection<>(() -> new ArrayList<>());
	}

	public static <E extends Object> LazyCollection<E> lazyList(final int capacity) {
		return new LazyCollection<>(() -> new ArrayList<>(capacity));
	}

	public static <E extends Object> LazyCollection<E> lazyLinkedList() {
		return new LazyCollection<>(() -> new LinkedList<>());
	}

	public static <E extends Object> LazyCollection<E> lazySet() {
		return new LazyCollection<>(() -> new ObjectOpenHashSet<>());
	}

	public static <E extends Object> LazyCollection<E> lazyLinkedSet() {
		return new LazyCollection<>(() -> new ObjectLinkedOpenHashSet<>());
	}

	public static <E extends Object> LazyCollection<E> lazySet(final int capacity) {
		return new LazyCollection<>(() -> new ObjectOpenHashSet<>(capacity));
	}

	public static <E extends Object> LazyCollection<E> lazyLinkedSet(final int capacity) {
		return new LazyCollection<>(() -> new ObjectLinkedOpenHashSet<>(capacity));
	}

	private final Supplier<Collection<E>> supplier;

	private Collection<E> buffer;

	public LazyCollection(Supplier<Collection<E>> supplier) {
		requireNonNull(supplier);

		this.supplier = supplier;
	}

	@Override
	public void accept(E t) {
		add(t);
	}

	public LazyCollection<E> forEach(Consumer<? super E> action) {
		Collection<E> c = buffer;
		if(c!=null) {
			c.forEach(action);
		}

		return this;
	}

	public LazyCollection<E> clear() {
		Collection<E> c = buffer;
		if(c!=null) {
			c.clear();
		}

		return this;
	}

	protected void ensureBuffer() {
		if(buffer==null) {
			buffer = supplier.get();
		}
	}

	protected void addExpectingBuffer(E item) {
		buffer.add(item);
	}

	public LazyCollection<E> add(E item) {
		if(item!=null) {
			ensureBuffer();

			addExpectingBuffer(item);
		}

		return this;
	}

	/**
	 * Directly adds the given colelction of items.
	 *
	 * @param items
	 * @return
	 */
	public LazyCollection<E> addAll(Collection<? extends E> items) {
		if(items!=null && !items.isEmpty()) {
			ensureBuffer();

			buffer.addAll(items);
		}

		return this;
	}

	/**
	 * Adds the given collection of items by first transforming each of them into
	 * the proper type used for storing objects in this {@code LazyCollection}.
	 *
	 * @param items
	 * @param transformer
	 * @return
	 */
	public <K extends Object> LazyCollection<E> addAll(Collection<? extends K> items, Function<K, E> transformer) {
		if(items!=null && !items.isEmpty()) {
			ensureBuffer();

			items.forEach(val -> addExpectingBuffer(transformer.apply(val)));
		}

		return this;
	}

	@SuppressWarnings("unchecked")
	public <C extends Consumer<? super E>> LazyCollection<E> addFromForEach(Consumer<C> forEach) {
		ensureBuffer();
		Consumer<E> action = this::addExpectingBuffer;
		forEach.accept((C)action);
		return this;
	}

	@SuppressWarnings("unchecked")
	public <K extends Object, C extends Consumer<? super K>> LazyCollection<E> addFromForEach(
			Consumer<C> forEach, Function<K, E> transform) {
		ensureBuffer();
		Consumer<K> action = val -> addExpectingBuffer(transform.apply(val));
		forEach.accept((C)action);
		return this;
	}

	public <C extends Collection<E>> C get() {
		@SuppressWarnings("unchecked")
		C result = (C) buffer;

		return result;
	}

	@SuppressWarnings("unchecked")
	public <L extends List<E>> L getAsList() {
		L result = (L) buffer;

		if(result==null) {
			result = (L) Collections.emptyList();
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public <L extends Set<E>> L getAsSet() {
		L result = (L) buffer;

		if(result==null) {
			result = (L) Collections.emptySet();
		}

		return result;
	}

	public Object[] getAsArray() {
		Collection<E> c = buffer;

		return (c==null || c.isEmpty()) ? new Object[0] : c.toArray();
	}

	@SuppressWarnings("unchecked")
	public <T extends Object> T[] getAsArray(T[] array) {
		Collection<E> c = buffer;

		return (c==null) ? (T[]) new Object[0] : c.toArray(array);
	}

	public boolean isEmpty() {
		Collection<E> c = buffer;
		return c==null || c.isEmpty();
	}

	public int size() {
		Collection<E> c = buffer;
		return c==null ? 0 : c.size();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		Collection<E> c = buffer;

		if(c==null || c.isEmpty()) {
			return "{}";
		}

		return CollectionUtils.toString(c);
	}

//	public static <E extends Object> Set<E> collectAsSet(Consumer<Consumer<? super E>> collector) {
//		LazyCollection<E> result = LazyCollection.lazySet();
//
//		collector.accept(result);
//
//		return result.getAsSet();
//	}
}
