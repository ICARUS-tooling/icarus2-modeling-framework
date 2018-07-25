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
package de.ims.icarus2.util;

import java.util.Set;

import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class Counter<T extends Object> {

	private final Object2IntOpenHashMap<T> counts = new Object2IntOpenHashMap<>();

	public Counter() {
		// no-op
	}

	public void copyFrom(Counter<? extends T> source) {
		counts.putAll(source.counts);
	}

	public int increment(T data) {
		return counts.addTo(data, 1) + 1;
	}

	public int add(T data, int delta) {
		int c = counts.getInt(data);
		if(c==counts.defaultReturnValue()) {
			c = 0;
		}

		if(delta>0 && Integer.MAX_VALUE-delta<c)
			throw new IllegalStateException("Positive overflow");

		c += delta;

		if(c<0)
			throw new IllegalStateException("Counter cannot get negative");

		counts.put(data, c);

//		System.out.printf("%s: %d size=%d\n",data,c,counts.size());

		return c;
	}

	public int decrement(T data) {
		int c = counts.getInt(data);
		if(c<1)
			throw new IllegalStateException("Cannot decrement count for data: "+data); //$NON-NLS-1$

		c--;
		if(c==0) {
			counts.removeInt(data);
		} else {
			counts.put(data, c);
		}

		return c;
	}

	public void clear() {
		counts.clear();
	}

	public int getCount(T data) {
		int c = counts.getInt(data);
		return c==counts.defaultReturnValue() ? 0 : c;
	}

	public void setCount(T data, int count) {
		counts.put(data, count);
	}

	/**
	 * Returns {@code true} iff the count for the giveb {@code data} is greater
	 * that {@code 0}.
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
