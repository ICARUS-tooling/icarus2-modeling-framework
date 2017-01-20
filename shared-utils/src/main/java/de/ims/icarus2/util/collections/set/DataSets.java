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
package de.ims.icarus2.util.collections.set;

import static java.util.Objects.requireNonNull;

import java.util.List;

/**
 * @author Markus Gärtner
 *
 */
public class DataSets {

	public static final int ARRAY_SET_THRESHOLD = 7;

	public static <E extends Object> DataSet<E> createDataSet(List<E> items) {
		requireNonNull(items);

		int size = items.size();

		if(size==0) {
			return DataSet.emptySet();
		} else if(size==1) {
			return new SingletonSet<>(items.get(0));
		} else if(size<=ARRAY_SET_THRESHOLD) {
			return new ArraySet<>(items);
		} else {
			return new CachedSet<>(items);
		}
	}

	public static <E extends Object> DataSet<E> createDataSet(E[] items) {
		requireNonNull(items);

		int size = items.length;

		if(size==0) {
			return DataSet.emptySet();
		} else if(size==1) {
			return new SingletonSet<>(items[0]);
		} else if(size<=ARRAY_SET_THRESHOLD) {
			return new ArraySet<>(items);
		} else {
			return new CachedSet<>(items);
		}
	}
}
