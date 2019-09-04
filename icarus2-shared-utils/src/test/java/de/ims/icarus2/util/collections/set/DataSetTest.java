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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.test.guard.ApiGuard;

/**
 * @author Markus Gärtner
 *
 */
public interface DataSetTest<S extends DataSet<Object>> extends ApiGuardedTest<S> {

	/**
	 * @see de.ims.icarus2.test.ApiGuardedTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
	 */
	@Override
	default void configureApiGuard(ApiGuard<S> apiGuard) {
		ApiGuardedTest.super.configureApiGuard(apiGuard);
		apiGuard.nullGuard(true);
	}

	@Provider
	S createEmpty();

	@Provider
	S createFilled(Object...items);

	default Object[] randomContent() {
		return TestUtils.randomContent();
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
	default void testEntryCount() {
		Object[] items = randomContent();
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
	default void testIsEmpty() {
		assertFalse(createFilled(randomContent()).isEmpty());
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
	default void testEntryAt() {
		Object[] items = randomContent();
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
	default void testContains() {
		Object[] items = randomContent();
		S set = createFilled(items);
		for (int i = 0; i < items.length; i++) {
			assertTrue(set.contains(items[i]));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#contains(java.lang.Object)}.
	 */
	@Test
	default void testContainsForeign() {
		assertFalse(createFilled(randomContent()).contains(new Object()));
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
	default void testToSet() {
		Object[] items = randomContent();
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
	default void testToList() {
		Object[] items = randomContent();
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
	default void testToArray() {
		Object[] items = randomContent();
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
	default void testToArrayTArray() {
		Object[] items = randomContent();
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
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#iterator()}.
	 */
	@Test
	default void testIterator() {
		Object[] items = randomContent();
		S set = createFilled(items);
		Iterator<Object> it = set.iterator();
		for (int i = 0; i < items.length; i++) {
			assertTrue(it.hasNext());
			assertSame(items[i], it.next());
		}
		assertFalse(it.hasNext());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#iterator()}.
	 */
	@Test
	default void testIteratorEmpty() {
		assertFalse(createEmpty().iterator().hasNext());
		assertThrows(NoSuchElementException.class, () -> createEmpty().iterator().next());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#emptySet()}.
	 */
	@Test
	default void testEmptySet() {
		assertTrue(DataSet.emptySet().isEmpty());
	}

	/**
	 * Test method for {@link java.lang.Iterable#forEach(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEach() {
		Object[] items = randomContent();
		S set = createFilled(items);
		List<Object> tmp = new ArrayList<>();
		set.forEach(tmp::add);
		assertCollectionEquals(tmp, items);
	}

	/**
	 * Test method for {@link java.lang.Iterable#forEach(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachEmpty() {
		Consumer<Object> action = mock(Consumer.class);
		createEmpty().forEach(action);
		verify(action, never()).accept(any());
	}

	/**
	 * Test method for {@link java.lang.Iterable#spliterator()}.
	 */
	@Test
	default void testSpliterator() {
		Object[] items = randomContent();
		S set = createFilled(items);
		assertArrayEquals(items, StreamSupport.stream(set.spliterator(), false).toArray());
	}

}
