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
package de.ims.icarus2.util;

import java.util.Set;

import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class LongCounter<T extends Object> {

	private final Object2LongOpenHashMap<T> counts = new Object2LongOpenHashMap<>();

	public LongCounter() {
		// no-op
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
		if(c==counts.defaultReturnValue()) {
			c = 0;
		}

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
		long c = counts.getLong(data);
		return c==counts.defaultReturnValue() ? 0 : c;
	}

	public void setCount(T data, long count) {
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
