/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.test.TestUtils.assertArrayEmpty;
import static de.ims.icarus2.test.TestUtils.assertCollectionEmpty;
import static de.ims.icarus2.test.TestUtils.assertCollectionEquals;
import static de.ims.icarus2.test.TestUtils.assertIOOB;
import static de.ims.icarus2.test.TestUtils.assertListEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.collections.IterableTest;

/**
 * @author Markus Gärtner
 *
 */
public interface DataSetTest<S extends DataSet<Object>>
		extends ApiGuardedTest<S>, IterableTest<Object, S> {

	@Override
	default Object[] randomContent(RandomGenerator rand) {
		return rand.randomContent();
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default S createTestInstance(TestSettings settings) {
		return settings.process(createEmpty());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#entryCount()}.
	 */
	@Test
	@RandomizedTest
	default void testEntryCount(RandomGenerator rand) {
		Object[] items = randomContent(rand);
		assertEquals(items.length, createFilled(items).entryCount());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#entryCount()}.
	 */
	@Test
	default void testEntryCountEmpty() {
		assertEquals(0, createEmpty().entryCount());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#isEmpty()}.
	 */
	@Test
	@RandomizedTest
	default void testIsEmpty(RandomGenerator rand) {
		assertFalse(createFilled(randomContent(rand)).isEmpty());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#isEmpty()}.
	 */
	@Test
	default void testIsEmptyEmpty() {
		assertTrue(createEmpty().isEmpty());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#entryAt(int)}.
	 */
	@Test
	@RandomizedTest
	default void testEntryAt(RandomGenerator rand) {
		Object[] items = randomContent(rand);
		S set = createFilled(items);
		for (int i = 0; i < items.length; i++) {
			assertSame(items[i], set.entryAt(i));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#entryAt(int)}.
	 */
	@Test
	default void testEntryAtEmpty() {
		assertIOOB(() -> createEmpty().entryAt(0));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#contains(java.lang.Object)}.
	 */
	@Test
	@RandomizedTest
	default void testContains(RandomGenerator rand) {
		Object[] items = randomContent(rand);
		S set = createFilled(items);
		for (int i = 0; i < items.length; i++) {
			assertTrue(set.contains(items[i]));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#contains(java.lang.Object)}.
	 */
	@Test
	@RandomizedTest
	default void testContainsForeign(RandomGenerator rand) {
		assertFalse(createFilled(randomContent(rand)).contains(new Object()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#contains(java.lang.Object)}.
	 */
	@Test
	default void testContainsEmpty() {
		assertFalse(createEmpty().contains(new Object()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#toSet()}.
	 */
	@Test
	@RandomizedTest
	default void testToSet(RandomGenerator rand) {
		Object[] items = randomContent(rand);
		S set = createFilled(items);
		assertCollectionEquals(set.toSet(), items);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#toSet()}.
	 */
	@Test
	default void testToSetEmpty() {
		assertCollectionEmpty(createEmpty().toSet());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#toList()}.
	 */
	@Test
	@RandomizedTest
	default void testToList(RandomGenerator rand) {
		Object[] items = randomContent(rand);
		S set = createFilled(items);
		assertListEquals(set.toList(), items);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#toList()}.
	 */
	@Test
	default void testToListEmpty() {
		assertCollectionEmpty(createEmpty().toList());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#toArray()}.
	 */
	@Test
	@RandomizedTest
	default void testToArray(RandomGenerator rand) {
		Object[] items = randomContent(rand);
		S set = createFilled(items);
		assertArrayEquals(items, set.toArray());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#toArray()}.
	 */
	@Test
	default void testToArrayEmpty() {
		assertArrayEmpty(createEmpty().toArray());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#toArray(Object[])}.
	 */
	@Test
	@RandomizedTest
	default void testToArrayTArray(RandomGenerator rand) {
		Object[] items = randomContent(rand);
		S set = createFilled(items);
		// Direct fit
		assertArrayEquals(items, set.toArray(new Object[items.length]));
		// Growing
		assertArrayEquals(items, set.toArray(new Object[0]));

		// Null-marker in array with extra capacity
		Object[] target = new Object[items.length*2];
		Object[] result = set.toArray(target);
		assertSame(target, result);

		for (int i = 0; i < items.length; i++) {
			assertSame(items[i], result[i]);
		}
		assertNull(result[items.length]);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#toArray(Object[]))}.
	 */
	@Test
	default void testToArrayTArrayEmpty() {
		assertArrayEmpty(createEmpty().toArray(new Object[0]));
		assertNull(createEmpty().toArray(new Object[10])[0]);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#emptySet()}.
	 */
	@Test
	default void testEmptySet() {
		assertTrue(DataSet.emptySet().isEmpty());
	}

}
