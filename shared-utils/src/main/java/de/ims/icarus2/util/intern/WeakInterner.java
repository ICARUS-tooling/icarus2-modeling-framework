/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 400 $
 * $Date: 2015-05-29 15:06:46 +0200 (Fr, 29 Mai 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.core/source/de/ims/icarus2/util/intern/WeakInterner.java $
 *
 * $LastChangedDate: 2015-05-29 15:06:46 +0200 (Fr, 29 Mai 2015) $
 * $LastChangedRevision: 400 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util.intern;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;


/**
 * @author Markus Gärtner
 * @version $Id: WeakInterner.java 400 2015-05-29 13:06:46Z mcgaerty $
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
