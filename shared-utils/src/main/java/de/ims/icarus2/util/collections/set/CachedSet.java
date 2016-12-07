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
package de.ims.icarus2.util.collections.set;

import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.util.List;

import de.ims.icarus2.util.collections.LookupList;
import de.ims.icarus2.util.mem.Assessable;

@Assessable
public class CachedSet<E extends Object> extends AbstractDataSet<E> {

	private final LookupList<E> items = new LookupList<>();

	public CachedSet() {
		// no-op
	}

	@SafeVarargs
	public CachedSet(E...items) {
		reset(items);
	}

	public CachedSet(List<? extends E> items) {
		reset(items);
	}

	/**
	 * @see de.ims.icarus2.util.collections.set.DataSet#entryCount()
	 */
	@Override
	public int entryCount() {
		return items.size();
	}

	/**
	 * @see de.ims.icarus2.util.collections.set.DataSet#entryAt(int)
	 */
	@Override
	public E entryAt(int index) {
		return items.get(index);
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		items.clear();
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return !items.isEmpty();
	}

	public void reset(E[] elements) {
		checkNotNull(elements);

		items.clear();

		items.addAll(elements);
	}

	public void reset(List<? extends E> elements) {
		checkNotNull(elements);

		items.clear();

		items.addAll(elements);
	}

	/**
	 *
	 * @see de.ims.icarus2.util.collections.set.DataSet#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(E element) {
		checkNotNull(element);

		return items.contains(element);
	}

	/**
	 * @see de.ims.icarus2.util.collections.set.AbstractDataSet#add(java.lang.Object)
	 */
	@Override
	public void add(E element) {
		checkNotNull(element);

		items.add(element);
	}
}