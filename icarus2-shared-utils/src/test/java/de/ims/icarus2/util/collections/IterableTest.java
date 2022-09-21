/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
public interface IterableTest<T, S extends Iterable<T>> {

	@Provider
	S createEmpty();

	@Provider
	S createFilled(@SuppressWarnings("unchecked") T...items);

	T[] randomContent(RandomGenerator rand);

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.seq.DataSequence#iterator()}.
	 */
	@Test
	@RandomizedTest
	default void testIterator(RandomGenerator rand) {
		T[] items = randomContent(rand);
		S seq = createFilled(items);
		Iterator<T> it = seq.iterator();
		for (int i = 0; i < items.length; i++) {
			assertTrue(it.hasNext());
			assertSame(items[i], it.next());
		}
		assertFalse(it.hasNext());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.seq.DataSequence#iterator()}.
	 */
	@Test
	default void testIteratorEmpty() {
		assertFalse(createEmpty().iterator().hasNext());
		assertThrows(NoSuchElementException.class, () -> createEmpty().iterator().next());
	}

	/**
	 * Test method for {@link java.lang.Iterable#forEach(java.util.function.Consumer)}.
	 */
	@Test
	@RandomizedTest
	default void testForEach(RandomGenerator rand) {
		T[] items = randomContent(rand);
		S seq = createFilled(items);
		List<T> tmp = new ArrayList<>();
		seq.forEach(tmp::add);
		assertThat(tmp).containsOnly(items);
	}

	/**
	 * Test method for {@link java.lang.Iterable#forEach(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachEmpty() {
		Consumer<T> action = mock(Consumer.class);
		createEmpty().forEach(action);
		verify(action, never()).accept(any());
	}

	/**
	 * Test method for {@link java.lang.Iterable#spliterator()}.
	 */
	@Test
	@RandomizedTest
	default void testSpliterator(RandomGenerator rand) {
		T[] items = randomContent(rand);
		S seq = createFilled(items);
		assertArrayEquals(items, StreamSupport.stream(seq.spliterator(), false).toArray());
	}

}
