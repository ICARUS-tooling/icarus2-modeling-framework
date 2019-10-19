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
/**
 *
 */
package de.ims.icarus2.model.api.driver.indices.standard;

import static de.ims.icarus2.model.api.ModelTestUtils.set;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.RandomAccessIndexSetTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
@RandomizedTest
class DelegatingFilteredIndexSetTest implements RandomAccessIndexSetTest<DelegatingFilteredIndexSet> {

	static RandomGenerator rand;

	private static IndexSet makeSet(Config config, int sourceSize, int[] filter) {
		long[] indices = config.getIndices();
		int size = indices.length;
		IndexValueType type = config.getValueType();
		Object array = type.newArray(sourceSize);
		if(filter==null) {
			type.copyFrom(indices, 0, array, 0, size);
		} else {
			type.fill(array, UNSET_LONG); // markers for unused values
			for (int i = 0; i < filter.length; i++) {
				// copy over all the "used" values
				type.set(array, filter[i], indices[i]);
			}
		}
		IndexSet source = new ArrayIndexSet(type, array, 0, sourceSize-1, config.isSorted());
		return new DelegatingFilteredIndexSet(source, filter);
	}

	private static int[] makeFilter(int size, int range, boolean sorted) {
		int[] filter = IntStream.range(0, range)
				.distinct()
				.limit(size)
				.toArray();
		if(sorted) {
			Arrays.sort(filter);
		}
		assertEquals(size, filter.length);
		return filter;
	}

	private static int randomSize() {
		return rand.random(10, 100);
	}

	private static String label(Config config, boolean full, Boolean filterSorted) {
		return config.getValueType()+" "
					+(full ? "full" : "partial")+", "
					+(config.isSorted() ? "sorted" : "unsorted")+", "
					+(filterSorted==null ? "unfiltered" :
						(filterSorted.booleanValue() ? "sorted filter" : "random filter"));
	}

	private Stream<Config> variate(Config origin) {
		List<Config> buffer = new ArrayList<>();
		int size = origin.getIndices().length;

		// full unsorted
		buffer.add(origin.clone()
				.label(label(origin, true, null))
				.set(makeSet(origin, size, null)));

		// full sorted filter
		buffer.add(origin.clone()
				.label(label(origin, true, Boolean.TRUE))
				.set(makeSet(origin, size, makeFilter(size, size, true))));

		// full unsorted filter
		buffer.add(origin.clone()
				.label(label(origin, true, Boolean.FALSE))
				.set(makeSet(origin, size, makeFilter(size, size, false))));

		// partial sorted filter
		buffer.add(origin.clone()
				.label(label(origin, false, Boolean.TRUE))
				.set(makeSet(origin, size*2, makeFilter(size, size, true))));

		// partial unsorted filter
		buffer.add(origin.clone()
				.label(label(origin, false, Boolean.FALSE))
				.set(makeSet(origin, size*2, makeFilter(size, size, false))));

		// empty
		buffer.add(origin.clone()
				.indices()
				.label("empty")
				.set(makeSet(origin, size, new int[0])));

		return buffer.stream();
	}

	@Override
	public Stream<Config> configurations() {
		Config base = new Config()
				.rand(rand)
				.defaultFeatures();

		return Stream.of(IndexValueType.values())
				.map(type -> base.clone().valueType(type))
				.flatMap(config -> Stream.of(
						config.clone().randomIndices(randomSize()),
						config.clone().sortedIndices(randomSize()).sorted(true)
				))
				.flatMap(this::variate);
	}

	@Override
	public Class<?> getTestTargetClass() {
		return DelegatingFilteredIndexSet.class;
	}

	@Override
	public DelegatingFilteredIndexSet createTestInstance(TestSettings settings) {
		return settings.process(new DelegatingFilteredIndexSet(set(0, 1, 2)));
	}

	@Nested
	class Constructors {

		@Test
		void wrapped() {
			IndexSet source = set(0, 1, 2);
			DelegatingFilteredIndexSet set = new DelegatingFilteredIndexSet(source);
			assertSame(source, set.getSource());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.DelegatingFilteredIndexSet#DelegatingFilteredIndexSet(de.ims.icarus2.model.api.driver.indices.IndexSet, int[])}.
		 */
		@Test
		void testDelegatingFilteredIndexSetIndexSetIntArray() {
			IndexSet source = set(0, 1, 2);
			int[] filter = {1, 0};
			DelegatingFilteredIndexSet set = new DelegatingFilteredIndexSet(source, filter);
			assertSame(source, set.getSource());
			assertSame(filter, set.getFilter());
		}

	}

}
