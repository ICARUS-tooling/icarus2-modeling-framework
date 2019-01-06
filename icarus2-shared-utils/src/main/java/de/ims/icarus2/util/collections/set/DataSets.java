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
