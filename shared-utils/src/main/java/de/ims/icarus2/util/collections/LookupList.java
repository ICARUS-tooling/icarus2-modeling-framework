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
package de.ims.icarus2.util.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import de.ims.icarus2.util.mem.Assessable;
import de.ims.icarus2.util.mem.Link;
import de.ims.icarus2.util.mem.Primitive;
import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
@Assessable
public class LookupList<E extends Object> implements Iterable<E> {

	/**
	 * Size of internal array up to which lookup will be done via
	 * linear search instead of using the lookup table.
	 */
	private static final int MIN_LOOKUP_SIZE = 6;

    private static final Object[] EMPTY_ITEMS = {};

    /**
     * Actual list of items
     */
    @Link
	private Object[] items;
    /**
     * Lookup table to map from items to their respective index in the list
     */
    @Link
	private Object2IntMap<E> lookup;
    /**
     * Modification counter for detection of concurrent modifications during iterator traversal
     */
    @Primitive
	private int modCount = 0;
    /**
     * Current number of item in the list
     */
    @Primitive
    private int size = 0;
    /**
     * Pointer to mark the current "dirty" region in the list.
     * Used for delayed update of the lookup table. Semantic of this value is simple:
     *
     * If greater than {@code -1} it denotes the smallest index for which mappings in the
     * internal lookup table are to be considered invalid. Before performing an actual
     * lookup, this implementation will refresh mappings for all those items in the currently
     * dirty region. Since this is done lazily it helps to reduce computational overhead.
     *
     */
    @Primitive
    private int dirtyIndex = -1;

	public LookupList() {
		items = EMPTY_ITEMS;
	}

	public LookupList(int capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: "+capacity); //$NON-NLS-1$

        items = new Object[capacity];
	}

	public LookupList(Collection<? extends E> c) {

        items = new Object[c.size()];
        c.toArray(items);

        addDirtyRegion(0);
	}

	protected void rangeCheck(int index) {
        if (index >= size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

	protected void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

	protected String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size; //$NON-NLS-1$ //$NON-NLS-2$
    }

	public int size() {
		return size;
	}

	@SuppressWarnings("unchecked")
	public E get(int index) {
		rangeCheck(index);
		return (E) items[index];
	}

	public void add(E item) {
        ensureCapacity(size + 1);  // Increments modCount!!

		int index = size;
        items[size++] = item;
        addDirtyRegion(index);
	}

	public void add(int index, E item) {
        rangeCheckForAdd(index);

        ensureCapacity(size + 1);  // Increments modCount!!
        System.arraycopy(items, index, items, index + 1,
                         size - index);
        items[index] = item;
        size++;
        addDirtyRegion(index);
	}

	public void addAll(Collection<? extends E> elements) {
		if (elements == null)
			throw new NullPointerException("Invalid elements");  //$NON-NLS-1$

		ensureCapacity(size + elements.size()); // Increments modCount!!
		int firstIndex = size;
		for(E item : elements) {
			items[size++] = item;
		}
		addDirtyRegion(firstIndex);
	}

	public void addAll(int index, Collection<? extends E> elements) {
		if (elements == null)
			throw new NullPointerException("Invalid elements");  //$NON-NLS-1$


        rangeCheckForAdd(index);
		int addCount = elements.size();

		ensureCapacity(size + addCount); // Increments modCount!!
        System.arraycopy(items, index, items, index + addCount,
                size - index);

		size += addCount;
		addDirtyRegion(index);
	}

	public void addAll(Iterable<? extends E> elements, int elementCount) {
		if (elements == null)
			throw new NullPointerException("Invalid elements");  //$NON-NLS-1$

		ensureCapacity(size + elementCount); // Increments modCount!!
		int firstIndex = size;
		for(E item : elements) {
			items[size++] = item;
		}
		addDirtyRegion(firstIndex);
	}

	public void addAll(Iterator<? extends E> elements, int elementCount) {
		if (elements == null)
			throw new NullPointerException("Invalid elements");  //$NON-NLS-1$

		ensureCapacity(size + elementCount); // Increments modCount!!
		int firstIndex = size;
		for(;elements.hasNext();) {
			E item = elements.next();
			items[size++] = item;
		}
		addDirtyRegion(firstIndex);
	}

	public void addAll(@SuppressWarnings("unchecked") E...elements) {
		if (elements == null)
			throw new NullPointerException("Invalid elements");  //$NON-NLS-1$

		ensureCapacity(size + elements.length); // Increments modCount!!
		int firstIndex = size;
		for(E item : elements) {
			items[size++] = item;
		}
		addDirtyRegion(firstIndex);
	}

	public E set(E item, int index) {
        rangeCheck(index);

        @SuppressWarnings("unchecked")
		E oldValue = (E) items[index];
        items[index] = item;

        unmap(oldValue);
        map(item, index);

        return oldValue;
	}

	public E remove(int index) {
        rangeCheck(index);

        modCount++;
        @SuppressWarnings("unchecked")
		E oldValue = (E) items[index];

        fastRemove(index);

        return oldValue;
	}

	/**
	 * Remove a selected region from this list
	 *
	 * @param index0 index of first item to be removed (inclusive)
	 * @param index1 index of the last item to be removed (inclusive)
	 * @param c action to be performed on each removed item
	 */
	public void removeAll(int index0, int index1, Consumer<? super E> c) {
		rangeCheck(index0);
		rangeCheck(index1);

		if(index0>index1)
			throw new IllegalArgumentException("Begin index of interval to be removed must be less than desired end index: "+index0+"-"+index1);

		fastRemove(index0, index1, c);
	}

	public boolean remove(E item) {
        if (item == null) {
            for (int index = 0; index < size; index++)
                if (items[index] == null) {
                    fastRemove(index);
                    return true;
                }
        } else {
           int index = indexOf(item);
           if(index!=-1) {
        	   fastRemove(index);
        	   return true;
           }
        }
        return false;
	}

	public void move(int index0, int index1) {
		if(index0==index1) {
			return;
		}

		rangeCheck(index1);
		rangeCheck(index0);

		modCount++;

		if(index0>index1) {
			int tmp = index0;
			index0 = index1;
			index1 = tmp;
		}

		Object item = items[index0];

		System.arraycopy(items, index0+1, items, index0, index1-index0);

		items[index1] = item;

		addDirtyRegion(index0);
	}

	public void clear() {
        modCount++;

        // clear to let GC do its work
        for (int i = 0; i < size; i++) {
            items[i] = null;
        }

        // Directly remove the entire lookup
        lookup = null;
        dirtyIndex = -1;

        size = 0;
	}

	public boolean contains(E item) {
		return indexOf(item)!=-1;
	}

	public int indexOf(E item) {
		// If lookup table is active make sure it's up2date and then use it
		if(checkRequiresLookup()) {
			return lookup.getInt(item);
		}

		// For small list sizes just traverse the items
        for (int i = 0; i < size; i++) {
        	//TODO why do we use equals(o) method instea dof identity?
            if(item.equals(items[i])) {
            	return i;
            }
        }

        return -1;
	}

	public boolean isEmpty() {
		return size==0;
	}

	public Object[] toArray() {
		return Arrays.copyOf(items, size);
	}

	public void set(Object[] elements) {
		if (elements == null)
			throw new NullPointerException("Invalid elements"); //$NON-NLS-1$

		ensureCapacity(elements.length);

		System.arraycopy(elements, 0, items, 0, elements.length);
		size = elements.length;
		addDirtyRegion(0);
		modCount++;
	}

	public void trim() {
		int capacity = items.length;
		float load = (float)size/capacity;

		if(capacity>CollectionUtils.DEFAULT_COLLECTION_CAPACITY
				&& load<CollectionUtils.DEFAULT_MIN_LOAD) {
			items = Arrays.copyOf(items, size);
		}

		// dirty index doesn't change
	}

	@SuppressWarnings("unchecked")
	public E first() {
		return (E) items[0];
	}

	@SuppressWarnings("unchecked")
	public E last() {
		return (E) items[size-1];
	}

	public synchronized void sort(Comparator<? super E> comparator) {
		@SuppressWarnings("unchecked")
		Comparator<Object> comp =(Comparator<Object>)comparator;
		Arrays.sort(items, 0, size, comp);

		addDirtyRegion(0);
	}

	/**
	 * Expands if necessary the current dirty region to also include the given {@code index}.
	 *
	 * @param index
	 */
	private void addDirtyRegion(int index) {
		if(dirtyIndex==-1 || index<dirtyIndex) {
			dirtyIndex = index;
		}
	}

    private void fastRemove(int index) {
        modCount++;
        @SuppressWarnings("unchecked")
		E item = (E) items[index];
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(items, index+1, items, index,
                             numMoved);
        items[--size] = null; // clear to let GC do its work
        unmap(item);

        addDirtyRegion(index);
    }

    private void fastRemove(int index0, int index1, Consumer<? super E> c) {
        modCount++;

        int numRemoved = index1-index0+1;

        // Shift the next block of items into the "empty" space and unmap all the former inhabitants
        for(int index=index0; index<=index1; index++) {
        	@SuppressWarnings("unchecked")
			E item = (E) items[index];
        	c.accept(item);
        	unmap(item);
        	items[index] = items[index+numRemoved];
        }

        // Shift the remaining items
        int remaining = size-index1-1-numRemoved;
        if(remaining>0) {
        	System.arraycopy(items, index0+numRemoved, items, index0, numRemoved);
        }

        // Clear last entries to let GC do its work
        Arrays.fill(items, size-numRemoved-1, size, null);

        size -= numRemoved;
        addDirtyRegion(index0);
    }

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    public void ensureCapacity(int minCapacity) {

        if (items == EMPTY_ITEMS) {
            minCapacity = Math.max(CollectionUtils.DEFAULT_COLLECTION_CAPACITY, minCapacity);
        }

        modCount++;

        // overflow-conscious code
        if (minCapacity - items.length > 0) {
            // overflow-conscious code
            int oldCapacity = items.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0)
                newCapacity = minCapacity;
            if (newCapacity - MAX_ARRAY_SIZE > 0)
                newCapacity = hugeCapacity(minCapacity);
            // minCapacity is usually close to size, so this is a win:
            items = Arrays.copyOf(items, newCapacity);
        }
    }

    /**
     * Creates the {@link Object2IntMap} to be used for lookup
     * purposes.
     *
     * Subclasses can override this method if they need to
     * add a custom {@link Strategy hash strategy} or perform
     * other special customizations.
     *
     * @param capacity
     * @return
     */
    protected Object2IntMap<E> createLookup(int capacity) {
    	return new Object2IntOpenHashMap<>(size);
    }

    @SuppressWarnings("unchecked")
	private boolean checkRequiresLookup() {

    	boolean requires = size>=MIN_LOOKUP_SIZE;

    	if(requires) {

    		if(lookup==null) {
	    		// Create and fill lookup
	    		lookup = createLookup(size);
    		}

            if(dirtyIndex!=-1 && dirtyIndex<size) {
            	for(int i=dirtyIndex; i<size; i++) {
            		lookup.put((E)items[i], i);
            	}
            	dirtyIndex = -1;
            }
    	}

    	return requires;
    }

    /**
     * Adds the given mapping to the lookup table if available or otherwise
     * adds the given index as dirty region.
     *
     * @param item
     * @param index
     */
    private void map(E item, int index) {
    	if(item!=null) {
    		if(lookup!=null) {
    			lookup.put(item, index);
    		} else {
    			addDirtyRegion(index);
    		}
    	}
    }

    /**
     * Removes the mapping for a given {@code item} from the lookup table
     * if the table is set.
     *
     * @param item
     */
    private void unmap(E item) {
    	if(item!=null && lookup!=null) {
    		lookup.remove(item);
    	}
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return new Itr();
	}



	private class Itr implements Iterator<E> {
        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedModCount = modCount;

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
            return cursor != size;
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		@SuppressWarnings("unchecked")
		@Override
		public E next() {
            checkForComodification();
            int i = cursor;
            if (i >= size)
                throw new NoSuchElementException();
            Object[] items = LookupList.this.items;
            if (i >= items.length)
                throw new ConcurrentModificationException();
            cursor = i + 1;
            return (E) items[lastRet = i];
//            return (E) items[i];
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
            	LookupList.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
//			throw new UnsupportedOperationException("Remove not supported"); //$NON-NLS-1$
		}

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
	}
}
