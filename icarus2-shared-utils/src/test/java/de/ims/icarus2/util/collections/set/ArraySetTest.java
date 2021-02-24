/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.SharedTestUtils.assertIcarusException;
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
class ArraySetTest implements DataSetTest<ArraySet<Object>> {

	@Override
	public Class<?> getTestTargetClass() {
		return ArraySet.class;
	}

	@Override
	public ArraySet<Object> createEmpty() {
		return new ArraySet<>(new Object[0]);
	}

	@Override
	public ArraySet<Object> createFilled(Object... items) {
		return new ArraySet<>(items);
	}

	public ArraySet<Object> createEmpty(int capacity) {
		return new ArraySet<>(new Object[capacity]);
	}

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.set.ArraySet#ArraySet()}.
		 */
		@Test
		void testArraySet() {
			assertNotNull(new ArraySet<>());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.set.ArraySet#ArraySet(E[])}.
		 */
		@Test
		@RandomizedTest
		void testArraySetEArray(RandomGenerator rand) {
			Object[] items = randomContent(rand);
			ArraySet<Object> set = new ArraySet<>(items);
			assertArrayEquals(items, set.toArray());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.set.ArraySet#ArraySet(java.util.List)}.
		 */
		@Test
		@RandomizedTest
		void testArraySetListOfQextendsE(RandomGenerator rand) {
			Object[] items = randomContent(rand);
			ArraySet<Object> set = new ArraySet<>(list(items));
			assertArrayEquals(items, set.toArray());
		}

	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.ArraySet#add(java.lang.Object)}.
	 */
	@Test
	void testAdd() {
		Object[] items = new Object[10];
		ArraySet<Object> set = new ArraySet<>(items);
		for (int i = 0; i < items.length; i++) {
			Object item = new Object();
			set.add(item);
			assertSame(item, set.entryAt(i));
		}
		assertArrayEquals(items, set.toArray());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.ArraySet#add(java.lang.Object)}.
	 */
	@Test
	void testAdd_NPE() {
		assertNPE(() -> createEmpty(1).add(null));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.ArraySet#reset(int)}.
	 */
	@RepeatedTest(RUNS)
	@RandomizedTest
	void testResetInt(RandomGenerator rand) {
		int size = rand.random(1, 100);
		ArraySet<Object> set = create();
		set.reset(size);
		assertEquals(size, set.entryCount());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.ArraySet#reset(int)}.
	 */
	@ParameterizedTest
	@ValueSource(ints = {-1, 0})
	void testResetInt_InvalidSize(int size) {
		assertIcarusException(GlobalErrorCode.INVALID_INPUT, () -> create().reset(size));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.ArraySet#set(int, java.lang.Object)}.
	 */
	@Test
	@RandomizedTest
	void testSet(RandomGenerator rand) {
		ArraySet<Object> set = createFilled(randomContent(rand));
		for (int i = 0; i < 10; i++) {
			Object item = new Object();
			int index = rand.random(0, set.entryCount());
			set.set(index, item);
			assertSame(item, set.entryAt(index));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.ArraySet#reset(java.lang.Object[])}.
	 */
	@Test
	@RandomizedTest
	void testResetObjectArray(RandomGenerator rand) {
		ArraySet<Object> set = createEmpty();
		Object[] items = randomContent(rand);

		set.reset(items);
		assertArrayEquals(items, set.toArray());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.ArraySet#reset(java.util.List)}.
	 */
	@Test
	@RandomizedTest
	void testResetListOfQextendsE(RandomGenerator rand) {
		ArraySet<Object> set = createEmpty();
		Object[] items = randomContent(rand);

		set.reset(list(items));
		assertArrayEquals(items, set.toArray());
	}

}
