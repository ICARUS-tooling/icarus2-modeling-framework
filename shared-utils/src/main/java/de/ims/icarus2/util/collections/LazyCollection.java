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
 *
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

	public void add(E item) {
		if(item==null) {
			return;
		}

		if(buffer==null) {
			buffer = supplier.get();
		}

		buffer.add(item);
	}

	public void addAll(Collection<? extends E> items) {
		if(items==null || items.isEmpty()) {
			return;
		}

		if(buffer==null) {
			buffer = supplier.get();
		}

		buffer.addAll(items);
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
