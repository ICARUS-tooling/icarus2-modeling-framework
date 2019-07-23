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
/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import de.ims.icarus2.util.collections.LazyCollection;

/**
 * @author Markus Gärtner
 *
 */
public interface Hierarchy<E extends Object> extends Lockable, Iterable<E> {

	public static final int ROOT = 0;

	E getRoot();

	int getDepth();

	E atLevel(int level);

	default Optional<E> tryLevel(int level) {
		return Optional.ofNullable(level<getDepth() ? atLevel(level) : null);
	}

	default Optional<E> adjacent(E item, Direction direction) {
		requireNonNull(direction);
		int level = levelOf(item);
		if(level != -1) {
			level += direction==Direction.BELOW ? 1 : -1;
			if(level>=0 && level <getDepth()) {
				return Optional.of(atLevel(level));
			}
		}

		return Optional.empty();
	}

	Hierarchy<E> add(E item);

	Hierarchy<E> remove(E item);

	Hierarchy<E> insert(E item, int index);

	/**
	 * Returns the level of specified {@code item} or {@code -1}
	 * if the item is not contained in this heirarchy.
	 *
	 * @param item
	 * @return
	 */
	int levelOf(E item);

	default boolean isEmpty() {
		return getDepth()==0;
	}

	default void forEachItem(Consumer<? super E> action) {
		requireNonNull(action);
		for(int i=0; i<getDepth(); i++) {
			action.accept(atLevel(i));
		}
	}

	default List<E> getItems() {
		return LazyCollection.<E>lazyList()
				.addFromForEach(this::forEachItem)
				.getAsList();
	}

	public enum Direction {
		ABOVE,
		BELOW,
		;
	}

	@SuppressWarnings("unchecked")
	public static <E extends Object> Hierarchy<E> empty() {
		return (Hierarchy<E>) EMPTY;
	}

	public static final Hierarchy<Object> EMPTY = new Hierarchy<Object>() {

		private final Collection<Object> buffer = Collections.emptyList();

		@Override
		public void lock() {
			// no-op
		}

		@Override
		public boolean isLocked() {
			return true;
		}

		@Override
		public Iterator<Object> iterator() {
			return buffer.iterator();
		}

		@Override
		public Object getRoot() {
			return null;
		}

		@Override
		public int getDepth() {
			return 0;
		}

		@Override
		public Object atLevel(int level) {
			throw new IndexOutOfBoundsException();
		}

		@Override
		public Hierarchy<Object> add(Object item) {
			// no-op
			return this;
		}

		@Override
		public Hierarchy<Object> remove(Object item) {
			// no-op
			return this;
		}

		@Override
		public Hierarchy<Object> insert(Object item, int index) {
			// no-op
			return this;
		}

		@Override
		public int levelOf(Object item) {
			return -1;
		}

	};
}
