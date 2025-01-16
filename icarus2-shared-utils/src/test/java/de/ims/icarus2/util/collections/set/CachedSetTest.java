/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
class CachedSetTest implements DataSetTest<CachedSet<Object>> {

	@Override
	public Class<?> getTestTargetClass() {
		return CachedSet.class;
	}

	@Override
	public CachedSet<Object> createEmpty() {
		return new CachedSet<>();
	}

	@Override
	public CachedSet<Object> createFilled(Object... items) {
		return new CachedSet<>(items);
	}

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.set.CachedSet#CachedSet()}.
		 */
		@Test
		void testCachedSet() {
			assertTrue(new CachedSet<>().isEmpty());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.set.CachedSet#CachedSet(E[])}.
		 */
		@Test
		@RandomizedTest
		void testCachedSetEArray(RandomGenerator rand) {
			Object[] items = randomContent(rand);
			CachedSet<Object> set = new CachedSet<>(items);
			assertArrayEquals(items, set.toArray());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.set.CachedSet#CachedSet(java.util.List)}.
		 */
		@Test
		@RandomizedTest
		void testCachedSetListOfQextendsE(RandomGenerator rand) {
			Object[] items = randomContent(rand);
			CachedSet<Object> set = new CachedSet<>(list(items));
			assertArrayEquals(items, set.toArray());
		}

	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.CachedSet#add(java.lang.Object)}.
	 */
	@Test
	@RandomizedTest
	void testAdd(RandomGenerator rand) {
		Object[] items = randomContent(rand);
		CachedSet<Object> set = createEmpty();
		for (int i = 0; i < items.length; i++) {
			Object item = items[i];
			set.add(item);
			assertSame(item, set.entryAt(i));
		}
		assertArrayEquals(items, set.toArray());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.CachedSet#add(java.lang.Object)}.
	 */
	@Test
	void testAdd_NPE() {
		assertNPE(() -> createEmpty().add(null));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.CachedSet#reset(E[])}.
	 */
	@Test
	@RandomizedTest
	void testResetEArray(RandomGenerator rand) {
		CachedSet<Object> set = createEmpty();
		Object[] items = randomContent(rand);

		set.reset(items);
		assertArrayEquals(items, set.toArray());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.CachedSet#reset(java.util.List)}.
	 */
	@Test
	@RandomizedTest
	void testResetListOfQextendsE(RandomGenerator rand) {
		CachedSet<Object> set = createEmpty();
		Object[] items = randomContent(rand);

		set.reset(list(items));
		assertArrayEquals(items, set.toArray());
	}

}
