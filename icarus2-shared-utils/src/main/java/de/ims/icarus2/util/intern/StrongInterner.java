/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.intern;

/**
 * @author Markus Gärtner
 *
 */
public class StrongInterner<E extends Object> implements Interner<E> {

	private transient Entry table[];

	private transient int count;

	private int threshold;

	private float loadFactor;

	private static class Entry {
		int hash;
		Object element;
		Entry next;

		protected Entry(int hash, Object element, Entry next) {
			this.hash = hash;
			this.element = element;
			this.next = next;
		}
	}

	public StrongInterner() {
		this(20, 0.75f);
	}

	public StrongInterner(int initialCapacity) {
		this(initialCapacity, 0.75f);
	}

	public StrongInterner(int initialCapacity, float loadFactor) {

		if (initialCapacity < 0)
			throw new IllegalArgumentException("Illegal capacity (negative): " //$NON-NLS-1$
					+ initialCapacity);
		if (loadFactor <= 0)
			throw new IllegalArgumentException("Illegal load-factor (zero or less): " + loadFactor); //$NON-NLS-1$

		if (initialCapacity == 0) {
			initialCapacity = 1;
		}

		this.loadFactor = loadFactor;
		table = new Entry[initialCapacity];
		threshold = (int) (initialCapacity * loadFactor);
	}

	public int size() {
		return count;
	}

	public boolean isEmpty() {
		return count == 0;
	}

	protected void rehash() {
		int oldCapacity = table.length;
		Entry oldMap[] = table;

		int newCapacity = (oldCapacity * 2) + 1;
		Entry newMap[] = new Entry[newCapacity];

		threshold = (int) (newCapacity * loadFactor);
		table = newMap;

		for (int i = oldCapacity; i-- > 0;) {
			for (Entry old = oldMap[i]; old != null;) {
				Entry e = old;
				old = old.next;

				int index = (e.hash & 0x7FFFFFFF) % newCapacity;
				e.next = newMap[index];
				newMap[index] = e;
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public E intern(final E item) {
		if(item==null)
			throw new NullPointerException("Invalid item"); //$NON-NLS-1$

		Object element = null;

		Entry tab[] = table;
		int hash = item.hashCode();
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for (Entry e = tab[index]; e != null; e = e.next) {
			if (item.equals(e.element)) {
				element = e.element;
				break;
			}
		}

		if(element==null) {

			if (count >= threshold) {
				// Rehash the table if the threshold is exceeded
				rehash();

				tab = table;
				index = (hash & 0x7FFFFFFF) % tab.length;
			}

			element = delegate(item);
			// Creates the new entry.
			Entry e = new Entry(hash, element, tab[index]);
			tab[index] = e;
			count++;
		}

		return (E) element;
	}

	protected E delegate(E item) {
		return item;
	}
}
