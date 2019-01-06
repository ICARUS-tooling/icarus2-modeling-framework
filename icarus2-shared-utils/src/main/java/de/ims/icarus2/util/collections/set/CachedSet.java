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
package de.ims.icarus2.util.collections.set;

import static java.util.Objects.requireNonNull;

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
		requireNonNull(elements);

		items.clear();

		items.addAll(elements);
	}

	public void reset(List<? extends E> elements) {
		requireNonNull(elements);

		items.clear();

		items.addAll(elements);
	}

	/**
	 *
	 * @see de.ims.icarus2.util.collections.set.DataSet#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(E element) {
		requireNonNull(element);

		return items.contains(element);
	}

	/**
	 * @see de.ims.icarus2.util.collections.set.AbstractDataSet#add(java.lang.Object)
	 */
	@Override
	public void add(E element) {
		requireNonNull(element);

		items.add(element);
	}
}