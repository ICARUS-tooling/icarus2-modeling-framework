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
/*
 * $Revision: 380 $
 *
 */
package de.ims.icarus2.util.collections;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.WeakHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class WeakHashSet<E extends Object> extends AbstractSet<E> {

	private static final Object dummy = "DUMMY"; //$NON-NLS-1$

	private final WeakHashMap<E, Object> store;

	public WeakHashSet() {
		store = new WeakHashMap<>();
	}

	public WeakHashSet(int initialCapacity) {
		store = new WeakHashMap<>(initialCapacity);
	}

	public WeakHashSet(Collection<? extends E> c) {
		store = new WeakHashMap<>(c.size());
		addAll(c);
	}

	/**
	 * @see java.util.AbstractCollection#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return store.keySet().iterator();
	}

	/**
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return store.size();
	}

	@Override
	public boolean contains(Object o) {
		return store.containsKey(o);
	}

	@Override
	public Object[] toArray() {
		return store.keySet().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return store.keySet().toArray(a);
	}

	@Override
	public boolean add(E e) {
		if(store.containsKey(e)) {
			return false;
		} else {
			store.put(e, dummy);
			return true;
		}
	}

	@Override
	public boolean remove(Object o) {
		return store.remove(o) != null;
	}

	@Override
	public void clear() {
		store.clear();
	}

}
