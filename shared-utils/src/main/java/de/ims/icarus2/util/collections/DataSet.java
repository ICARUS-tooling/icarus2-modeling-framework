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

 * $Revision: 457 $
 * $Date: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/util/DataSet.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util.collections;

import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;


/**
 * Defines an array like access protocol for members of a corpus which
 * in addition allows for containment checks. Note that unlike the
 * {@link DataSequence sequence} counterpart the size of this storage
 * is limited to {@code int} size.
 *
 * @param <E> The type of elements stored in this set
 *
 * @author Markus Gärtner
 * @version $Id: DataSet.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 * @see DataSequence
 */
public interface DataSet<E extends Object> {

	int entryCount();

	default boolean isEmpty() {
		return entryCount()==0;
	}

	E entryAt(int index);

	boolean contains(E element);

	default void forEachEntry(Consumer<? super E> action) {
		for(int i = 0; i<entryCount(); i++) {
			action.accept(entryAt(i));
		}
	}

	default Set<E> toSet() {
		LazyCollection<E> result = LazyCollection.lazySet(entryCount());

		forEachEntry(result);

		return result.getAsSet();
	}

	default List<E> toList() {
		LazyCollection<E> result = LazyCollection.lazyList(entryCount());

		forEachEntry(result);

		return result.getAsList();
	}

	default Object[] toArray() {
		Object[] result = new Object[entryCount()];

		for(int i = 0; i<entryCount(); i++) {
			result[i] = entryAt(i);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	default <T> T[] toArray(T[] array) {
		checkNotNull(array);
		int size = entryCount();

		if(array.length<size) {
			array = (T[]) Array.newInstance(array.getClass().getComponentType(), size);
		}

		Object[] result = array;

		for(int i = 0; i<size; i++) {
			result[i] = entryAt(i);
		}

        if (array.length > size) {
        	array[size] = null;
        }

		return array;
	}

	@SuppressWarnings("unchecked")
	public static <E extends Object> DataSet<E> emptySet() {
		return (DataSet<E>) EMPTY_SET;
	}

	public static final DataSet<Object> EMPTY_SET = new DataSet<Object>() {

		@Override
		public int entryCount() {
			return 0;
		}

		@Override
		public Object entryAt(int index) {
			throw new IndexOutOfBoundsException("Set is empty");
		}

		@Override
		public boolean contains(Object element) {
			return false;
		}
	};
}
