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
package de.ims.icarus2.util.collections;

import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.mem.Assessable;
import de.ims.icarus2.util.mem.Link;
import de.ims.icarus2.util.mem.Primitive;
import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * A list-like data structure with accumulated constant lookup
 * cost. This implementation allows {@code null} values!
 *
 * @author Markus Gärtner
 *
 */
@Assessable
public class LookupList<E extends Object> implements Iterable<E>, Clearable {

	/**
	 * Size of internal array up to which lookup will be done via
	 * linear search instead of using the lookup table.
	 */
	private static final int MIN_LOOKUP_SIZE = 6;

    private static final Object[] EMPTY_ITEMS = {};

    /**
     * Actual list of items, never {@code null}.
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
        if (capacity <= 0)
            throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT,
            		"Illegal Capacity: "+capacity);

        items = new Object[capacity];
	}

	public LookupList(Collection<? extends E> c) {

        size = c.size();
        items = new Object[size];
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

	/**
	 * Returns the current capacity of the list, i.e. the number of elements
	 * it can store before needing to extend the internal storage.
	 * @return
	 */
	public int capacity() {
		return items.length;
	}

	public int size() {
		return size;
	}

	@SuppressWarnings("unchecked")
	public E get(int index) {
		rangeCheck(index);
		return (E) items[index];
	}

	public void add(@Nullable E item) {
        ensureCapacity(size + 1);  // Increments modCount!!

		int index = size;
        items[size++] = item;
        addDirtyRegion(index);
	}

	public void add(int index, @Nullable E item) {
        rangeCheckForAdd(index);

        ensureCapacity(size + 1);  // Increments modCount!!
        System.arraycopy(items, index, items, index + 1,
                         size - index);
        items[index] = item;
        size++;
        addDirtyRegion(index);
	}

	public void addAll(Collection<? extends E> elements) {
		if(elements.isEmpty()) {
			return;
		}
		ensureCapacity(size + elements.size()); // Increments modCount!!
		int firstIndex = size;
		for(E item : elements) {
			items[size++] = item;
		}
		addDirtyRegion(firstIndex);
	}

	public void addAll(int index, Collection<? extends E> elements) {
		if(elements.isEmpty()) {
			return;
		}

        rangeCheckForAdd(index);
		int addCount = elements.size();

		ensureCapacity(size + addCount); // Increments modCount!!
        System.arraycopy(items, index, items, index + addCount,
                size - index);

        int insertionPoint = index;
		for(E item : elements) {
			items[insertionPoint++] = item;
		}

		size += addCount;
		addDirtyRegion(index);
	}

	public void addAll(@SuppressWarnings("unchecked") E...elements) {
		if(elements.length==0) {
			return;
		}

		ensureCapacity(size + elements.length); // Increments modCount!!
		int firstIndex = size;
		for(E item : elements) {
			items[size++] = item;
		}
		addDirtyRegion(firstIndex);
	}

	public E set(@Nullable E item, int index) {
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
		requireNonNull(c);
		rangeCheck(index0);
		rangeCheck(index1);

		if(index0>index1)
			throw new IllegalArgumentException("Begin index of interval to be removed must be less than desired end index: "+index0+"-"+index1);

		fastRemove(index0, index1, c);
	}

	public void removeAll(Iterable<? extends E> items) {
		for(E item : items) {
			remove(item);
		}
	}

	public boolean remove(@Nullable E item) {
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

	@Override
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

	public boolean contains(@Nullable E item) {
		return indexOf(item)!=-1;
	}

	public int indexOf(@Nullable E item) {
		// If lookup table is active make sure it's up2date and then use it
		if(checkRequiresLookup()) {
			return lookup.getInt(item);
		}

		// For small list sizes just traverse the items
        for (int i = 0; i < size; i++) {
            if(items[i] == item) {
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

	public void sort(Comparator<? super E> comparator) {
		requireNonNull(comparator);
		@SuppressWarnings("unchecked")
		Comparator<Object> comp = (Comparator<Object>)comparator;
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

        /*
         * |-unchanged-|-removed-|-shifted-|-cleared-|
         */

        // Unmap all the former inhabitants and send to consumer
        for(int index=index0; index<=index1; index++) {
        	@SuppressWarnings("unchecked")
			E item = (E) items[index];
        	c.accept(item);
        	unmap(item);
        }

        // Shift buffer content into empty space
    	System.arraycopy(items, index1+1, items, index0, size-index1-1);

        // Clear last entries to let GC do its work
        Arrays.fill(items, size-numRemoved, size, null);

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
     * <p>
     * Subclasses can override this method if they need to
     * add a custom {@link Strategy hash strategy} or perform
     * other special customizations.
     * <p>
     * In any case the returned map <b>must</b> be configured to
     * return {@value IcarusUtils#UNSET_INT} as default value for
     * missing mappings!
     *
     * @param capacity
     * @return
     */
    protected Object2IntMap<E> createLookup(int capacity) {
    	Object2IntOpenHashMap<E> map = new Object2IntOpenHashMap<>(size);
    	map.defaultReturnValue(UNSET_INT);
    	return map;
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
		if(lookup!=null) {
			lookup.put(item, index);
		} else {
			addDirtyRegion(index);
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
    		lookup.removeInt(item);
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
		}

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
	}
}
