/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class LongCounter<T extends Object> {

	private final Object2LongOpenHashMap<T> counts;

	public LongCounter() {
		counts = new Object2LongOpenHashMap<>();
		counts.defaultReturnValue(0L);
	}

	public void copyFrom(LongCounter<? extends T> source) {
		counts.putAll(source.counts);
	}

	public void addAll(LongCounter<? extends T> source) {
		source.counts.object2LongEntrySet().forEach(e -> {
			add(e.getKey(), e.getLongValue());
		});
	}

	public long increment(T data) {
		return counts.addTo(data, 1) + 1;
	}

	public long add(T data, long delta) {
		long c = counts.getLong(data);

		c += delta;

		if(c<0)
			throw new IllegalStateException("Counter cannot get negative");

		counts.put(data, c);

//		System.out.printf("%s: %d size=%d\n",data,c,counts.size());

		return c;
	}

	public long decrement(T data) {
		long c = counts.getLong(data);
		if(c<1)
			throw new IllegalStateException("Cannot decrement count for data: "+data); //$NON-NLS-1$

		c--;
		if(c==0) {
			counts.removeLong(data);
		} else {
			counts.put(data, c);
		}

		return c;
	}

	public void clear() {
		counts.clear();
	}

	public long getCount(Object data) {
		return counts.getLong(data);
	}

	public void setCount(T data, long count) {
		if(count<0)
			throw new IllegalStateException("Counter cannot get negative: "+count);
		counts.put(data, count);
	}

	/**
	 * Returns {@code true} iff the count for the given {@code data} is greater
	 * that {@code 0}.
	 *
	 * @param data
	 * @return
	 */
	public boolean hasCount(Object data) {
		long c = counts.getLong(data);
		return c>0;
	}

	public Set<T> getItems() {
		return CollectionUtils.getSetProxy(counts.keySet());
	}

	public boolean isEmpty() {
		return counts.isEmpty();
	}
}
