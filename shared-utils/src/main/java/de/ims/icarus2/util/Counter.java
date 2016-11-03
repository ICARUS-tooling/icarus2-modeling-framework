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

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.Set;

import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public class Counter<T extends Object> {

	private final Object2IntOpenHashMap<T> counts = new Object2IntOpenHashMap<>();

	public Counter() {
		// no-op
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

	public int getCount(Object data) {
		int c = counts.getInt(data);
		return c==counts.defaultReturnValue() ? 0 : c;
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
