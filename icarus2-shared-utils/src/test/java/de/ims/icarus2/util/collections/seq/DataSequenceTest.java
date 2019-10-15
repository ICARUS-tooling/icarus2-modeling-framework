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
package de.ims.icarus2.util.collections.seq;

import static de.ims.icarus2.test.TestUtils.assertCollectionEmpty;
import static de.ims.icarus2.test.TestUtils.assertIOOB;
import static de.ims.icarus2.test.TestUtils.assertListEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.util.collections.IterableTest;

/**
 * @author Markus Gärtner
 *
 */
public interface DataSequenceTest<S extends DataSequence<Object>>
	extends ApiGuardedTest<S>, IterableTest<Object, S> {

	@Override
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
	 * Test method for {@link de.ims.icarus2.util.collections.seq.DataSequence#entryCount()}.
	 */
	@Test
	default void testEntryCount() {
		Object[] items = randomContent();
		assertEquals(items.length, createFilled(items).entryCount());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.seq.DataSequence#entryCount()}.
	 */
	@Test
	default void testEntryCountEmpty() {
		assertEquals(0, createEmpty().entryCount());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.seq.DataSequence#elementAt(long)}.
	 */
	@Test
	default void testElementAt() {
		Object[] items = randomContent();
		S seq = createFilled(items);
		for (int i = 0; i < items.length; i++) {
			assertSame(items[i], seq.elementAt(i));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.seq.DataSequence#elementAt(long)}.
	 */
	@Test
	default void testElementAtEmpty() {
		assertIOOB(() -> createEmpty().elementAt(0));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.seq.DataSequence#getEntries()}.
	 */
	@Test
	default void testGetEntries() {
		Object[] items = randomContent();
		S seq = createFilled(items);
		assertListEquals(seq.getEntries(), items);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.seq.DataSequence#getEntries()}.
	 */
	@Test
	default void testGetEntriesEmpty() {
		assertCollectionEmpty(createEmpty().getEntries());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.seq.DataSequence#emptySequence()}.
	 */
	@Test
	default void testEmptySequence() {
		assertTrue(DataSequence.emptySequence().entryCount()==0L);
	}

}
