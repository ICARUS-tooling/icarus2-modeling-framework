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
package de.ims.icarus2.util.intern;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;


/**
 * @author Markus Gärtner
 *
 */
public class WeakInterner<E extends Object> implements Interner<E> {

	private final transient ReferenceQueue<Object> queue = new ReferenceQueue<>();

	private transient Entry table[];

	private transient int count;

	private int threshold;

	private float loadFactor;

	private static class Entry extends WeakReference<Object> {
		int hash;
		Entry next;

		protected Entry(int hash, Object element, ReferenceQueue<Object> queue, Entry next) {
			super(element, queue);
			this.hash = hash;
			this.next = next;
		}
	}

	public WeakInterner() {
		this(20, 0.75f);
	}

	public WeakInterner(int initialCapacity) {
		this(initialCapacity, 0.75f);
	}

	public WeakInterner(int initialCapacity, float loadFactor) {

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

	private void rehash() {
		Entry oldMap[] = getTable();
		int oldCapacity = oldMap.length;

		int newCapacity = (oldCapacity * 2) + 1;
		Entry newMap[] = new Entry[newCapacity];

//		System.out.println("rehashing table to size: "+newCapacity);

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

    private void expungeStaleEntries() {
        for (Object x; (x = queue.poll()) != null; ) {
            synchronized (queue) {
                Entry e = (Entry)x;

        		int index = (e.hash & 0x7FFFFFFF) % table.length;

                Entry prev = table[index];
                Entry p = prev;
                while (p != null) {
                    Entry next = p.next;
                    if (p == e) {
                        if (prev == e)
                            table[index] = next;
                        else
                            prev.next = next;

                        e.next = null; // Help GC
                        count--;
                        break;
                    }
                    prev = p;
                    p = next;
                }
            }
        }
    }

	@SuppressWarnings("unchecked")
	@Override
	public E intern(final E item) {
		if(item==null)
			throw new NullPointerException("Invalid item"); //$NON-NLS-1$

		Object element = null;

		Entry tab[] = getTable();
		int hash = item.hashCode();
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for (Entry e = tab[index]; e != null; e = e.next) {
			Object current = e.get();
			if (current!=null && item.equals(current)) {
				element = current;
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

//			System.out.println("interning new item: '"+element+"'");

			// Creates the new entry.
			Entry e = new Entry(hash, element, queue, tab[index]);
			tab[index] = e;
			count++;
		}

		return (E) element;
	}

	private Entry[] getTable() {
		expungeStaleEntries();
		return table;
	}

	/**
	 * Hook for subclasses to prepare a new element prior to insertion.
	 * Note that the returned value is only required to be <i>equal</i> to
	 * the initial argument, not the same!
	 */
	protected E delegate(E item) {
		return item;
	}
}
