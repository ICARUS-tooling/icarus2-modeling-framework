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
package de.ims.icarus2.util;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.function.Consumer;

import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;

/**
 * @author Markus Gärtner
 *
 */
public class Counter<T extends Object> {

	private final Object2IntMap<T> counts = new Object2IntOpenHashMap<>();

	public Counter() {
		// no-op
	}

	public Counter<T> copy() {
		Counter<T> c = new Counter<>();
		c.copyFrom(this);
		return c;
	}

	/**
	 * Overwrites in this counter all mappings defined by the given
	 * {@code source} counter. Mappings not part of the {@code source}
	 * counter are not affected.
	 * <p>
	 * If you want to <b>add</b> counts for objects contained in both
	 * counters, then use the {@link #addAll(Counter)} method instead!
	 *
	 * @param source
	 * @return this counter instance
	 *
	 * @see #addAll(Counter)
	 */
	public Counter<T> copyFrom(Counter<? extends T> source) {
		requireNonNull(source);
		counts.putAll(source.counts);
		return this;
	}

	/**
	 * For each mapping in the given {@code source} counter this method
	 * effectively calls the internal equivalent of {@link #add(Object, int)} to aggregate.
	 *
	 * @param source
	 * @return
	 */
	public Counter<T> addAll(Counter<? extends T> source) {
		requireNonNull(source);

		Consumer<? super Object2IntMap.Entry<T>> action =
				entry -> add0(entry.getKey(), entry.getIntValue());

		@SuppressWarnings("unchecked")
		Object2IntMap<T> other = (Object2IntMap<T>) source.counts;

		ObjectSet<Object2IntMap.Entry<T>> set = other.object2IntEntrySet();

		// Optimize for cheap traversal if we have a fast entry set
		if(set instanceof Object2IntMap.FastEntrySet) {
			((Object2IntMap.FastEntrySet<T>)set).fastForEach(action);
		} else {
			set.forEach(action);
		}

		return this;
	}

	private int add0(T data, int delta) {
		int c = counts.getInt(data);
		if(c==counts.defaultReturnValue()) {
			c = 0;
		}

		if(delta!=0) {

			if(delta>0 && Integer.MAX_VALUE-delta<c)
				throw new IllegalStateException("Positive overflow for data: "+data);

			c += delta;

			if(c<0)
				throw new IllegalStateException("Counter cannot get negative for data: "+data);

			if(c==0) {
				counts.removeInt(data);
			} else {
				counts.put(data, c);
			}
		}

		return c;
	}

	public int add(T data, int delta) {
		requireNonNull(data);

		return add0(data, delta);
	}

	public int increment(T data) {
		requireNonNull(data);

		return add0(data, +1);
	}

	public int decrement(T data) {
		requireNonNull(data);

		return add0(data, -1);
	}

	public Counter<T> clear() {
		counts.clear();

		return this;
	}

	public int getCount(T data) {
		requireNonNull(data);

		int c = counts.getInt(data);
		return c==counts.defaultReturnValue() ? 0 : c;
	}

	public Counter<T> setCount(T data, int count) {
		requireNonNull(data);

		counts.put(data, count);

		return this;
	}

	/**
	 * Returns {@code true} iff the count for the given {@code data} object is greater
	 * than {@code 0}.
	 *
	 * @param data
	 * @return
	 */
	public boolean hasCount(Object data) {
		int c = counts.getInt(data);
		return c>0;
	}

	public Set<T> getItems() {
		return CollectionUtils.getSetProxy(counts.keySet());
	}

	public boolean isEmpty() {
		return counts.isEmpty();
	}
}
