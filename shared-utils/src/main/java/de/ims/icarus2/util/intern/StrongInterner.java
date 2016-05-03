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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.core/source/de/ims/icarus2/util/intern/StrongInterner.java $
 *
 * $LastChangedDate: 2015-05-29 15:06:46 +0200 (Fr, 29 Mai 2015) $
 * $LastChangedRevision: 400 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util.intern;







/**
 * @author Markus Gärtner
 * @version $Id: StrongInterner.java 400 2015-05-29 13:06:46Z mcgaerty $
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
