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

 * $Revision: 411 $
 * $Date: 2015-06-26 17:07:19 +0200 (Fr, 26 Jun 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.core/source/de/ims/icarus2/util/collections/IdentityHashSet.java $
 *
 * $LastChangedDate: 2015-06-26 17:07:19 +0200 (Fr, 26 Jun 2015) $
 * $LastChangedRevision: 411 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util.collections;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.ims.icarus2.util.mem.HeapMember;
import de.ims.icarus2.util.mem.Link;
import de.ims.icarus2.util.mem.Primitive;
import de.ims.icarus2.util.mem.ReferenceType;

/**
 * @author Markus Gärtner
 * @version $Id: IdentityHashSet.java 411 2015-06-26 15:07:19Z mcgaerty $
 *
 */
public class IdentityHashSet<E extends Object> extends AbstractSet<E> implements Serializable {

	private static final long serialVersionUID = -2966583823165492803L;

	/**
	 * The hash table data.
	 */
	@Link(type=ReferenceType.DOWNLINK)
	private transient Entry table[];

	/**
	 * The total number of entries in the hash table.
	 */
	@Primitive
	private transient int count;

	private transient int modCount;

	/**
	 * The table is rehashed when its size exceeds this threshold. (The value of
	 * this field is (int)(capacity * loadFactor).)
	 *
	 * @serial
	 */
	@Primitive
	private int threshold;

	/**
	 * The load factor for the hash-table.
	 *
	 * @serial
	 */
	@Primitive
	private float loadFactor;

	/**
	 * Inner class that acts as a data structure to create a new entry in the
	 * table.
	 */
	@HeapMember
	private static class Entry implements Serializable {
		private static final long serialVersionUID = 4698074972380429762L;

		@Primitive
		int hash;
		@Link(cache=true)
		Object value;
		@Link
		Entry next;

		/**
		 * Create a new entry with the given values.
		 *
		 * @param hash The hash used to enter this in the table
		 * @param value The value for this key
		 * @param next A reference to the next entry in the table
		 */
		protected Entry(int hash, Object value, Entry next) {
			this.hash = hash;
			this.value = value;
			this.next = next;
		}
	}

	public IdentityHashSet() {
		this(20, 0.75f);
	}

	public IdentityHashSet(int initialCapacity) {
		this(initialCapacity, 0.75f);
	}

	public IdentityHashSet(int initialCapacity, float loadFactor) {

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

	/**
	 * @see java.util.AbstractCollection#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		return count;
	}

	@Override
	public boolean isEmpty() {
		return count == 0;
	}

	@Override
	public boolean contains(Object value) {
		if (value == null)
			throw new NullPointerException("Invalid value"); //$NON-NLS-1$
		Entry tab[] = table;
		int hash = value.hashCode();
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for (Entry e = tab[index]; e != null; e = e.next) {
			if (e.value == value) {
				return true;
			}
		}
		return false;
	}

	public boolean containsEquals(Object value) {
		if (value == null)
			throw new NullPointerException("Invalid value"); //$NON-NLS-1$
		Entry tab[] = table;
		int hash = value.hashCode();
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for (Entry e = tab[index]; e != null; e = e.next) {
			if (value.equals(e.value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Increases the capacity of and internally reorganizes this hash-table, in
	 * order to accommodate and access its entries more efficiently.
	 *
	 * This method is called automatically when the number of keys in the
	 * hash-table exceeds this hash-table's capacity and load factor.
	 */
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

	@Override
	public boolean add(E value) {
		if (value == null)
			throw new NullPointerException("Invalid value"); //$NON-NLS-1$
		// Makes sure the key is not already in the hash-table.
		Entry tab[] = table;
		int hash = value.hashCode();
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for (Entry e = tab[index]; e != null; e = e.next) {
			if (e.value == value) {
				return false;
			}
		}

		if (count >= threshold) {
			// Rehash the table if the threshold is exceeded
			rehash();

			tab = table;
			index = (hash & 0x7FFFFFFF) % tab.length;
		}

		// Creates the new entry.
		Entry e = new Entry(hash, value, tab[index]);
		tab[index] = e;
		count++;
		modCount++;
		return true;
	}

	public boolean addEquals(E value) {
		if (value == null)
			throw new NullPointerException("Invalid value"); //$NON-NLS-1$
		// Makes sure the key is not already in the hash-table.
		Entry tab[] = table;
		int hash = value.hashCode();
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for (Entry e = tab[index]; e != null; e = e.next) {
			if (value.equals(e.value)) {
				return false;
			}
		}

		if (count >= threshold) {
			// Rehash the table if the threshold is exceeded
			rehash();

			tab = table;
			index = (hash & 0x7FFFFFFF) % tab.length;
		}

		// Creates the new entry.
		Entry e = new Entry(hash, value, tab[index]);
		tab[index] = e;
		count++;
		modCount++;
		return true;
	}

	@Override
	public boolean remove(Object value) {
		if (value == null)
			throw new NullPointerException("Invalid value"); //$NON-NLS-1$
		Entry tab[] = table;
		int hash = value.hashCode();
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for (Entry e = tab[index], prev = null; e != null; prev = e, e = e.next) {
			if (e.value == value) {
				if (prev != null) {
					prev.next = e.next;
				} else {
					tab[index] = e.next;
				}
				count--;
				modCount++;
				e.value = null;
				return true;
			}
		}
		return false;
	}

	/**
	 * Clears this hash-table so that it contains no keys.
	 */
	@Override
	public synchronized void clear() {
		Entry tab[] = table;
		for (int index = tab.length; --index >= 0;) {
			tab[index] = null;
		}
		count = 0;
		modCount++;
	}


    class HashIterator implements Iterator<E> {
        Entry next;        // next entry to return
        Entry current;     // current entry
        int expectedModCount;  // for fast-fail
        int index;             // current slot

        HashIterator() {
            expectedModCount = modCount;
            Entry[] t = table;
            current = next = null;
            index = 0;
            if (t != null && count > 0) { // advance to first entry
                do {} while (index < t.length && (next = t[index++]) == null);
            }
        }

        @Override
		public final boolean hasNext() {
            return next != null;
        }

        final Entry nextNode() {
        	Entry[] t;
        	Entry e = next;
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (e == null)
                throw new NoSuchElementException();
            if ((next = (current = e).next) == null && (t = table) != null) {
                do {} while (index < t.length && (next = t[index++]) == null);
            }
            return e;
        }

        @Override
		public final void remove() {
        	Entry p = current;
            if (p == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            current = null;
            IdentityHashSet.this.remove(p.value);
            expectedModCount = modCount;
        }

		/**
		 * @see java.util.Iterator#next()
		 */
		@SuppressWarnings("unchecked")
		@Override
		public E next() {
			return (E) nextNode().value;
		}
    }
}
