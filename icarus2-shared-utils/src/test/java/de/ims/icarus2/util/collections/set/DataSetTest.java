/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.test.TestUtils.assertIOOB;
import static org.assertj.core.api.Assertions.assertThat;

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
		assertThat(createFilled(items).entryCount()).isEqualTo(items.length);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#entryCount()}.
	 */
	@Test
	default void testEntryCountEmpty() {
		assertThat(createEmpty().entryCount()).isEqualTo(0);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#isEmpty()}.
	 */
	@Test
	@RandomizedTest
	default void testIsEmpty(RandomGenerator rand) {
		assertThat(createFilled(randomContent(rand)).isEmpty()).isFalse();
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#isEmpty()}.
	 */
	@Test
	default void testIsEmptyEmpty() {
		assertThat(createEmpty().isEmpty()).isTrue();
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
			assertThat(set.entryAt(i)).isSameAs(items[i]);
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
			assertThat(set.contains(items[i])).isTrue();
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#contains(java.lang.Object)}.
	 */
	@Test
	@RandomizedTest
	default void testContainsForeign(RandomGenerator rand) {
		assertThat(createFilled(randomContent(rand)).contains(new Object())).isFalse();
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#contains(java.lang.Object)}.
	 */
	@Test
	default void testContainsEmpty() {
		assertThat(createEmpty().contains(new Object())).isFalse();
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#toSet()}.
	 */
	@Test
	@RandomizedTest
	default void testToSet(RandomGenerator rand) {
		Object[] items = randomContent(rand);
		S set = createFilled(items);
		assertThat(set.toSet()).containsOnly(items);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#toSet()}.
	 */
	@Test
	default void testToSetEmpty() {
		assertThat(createEmpty().toSet()).isEmpty();
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#toList()}.
	 */
	@Test
	@RandomizedTest
	default void testToList(RandomGenerator rand) {
		Object[] items = randomContent(rand);
		S set = createFilled(items);
		assertThat(set.toList()).containsExactly(items);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#toList()}.
	 */
	@Test
	default void testToListEmpty() {
		assertThat(createEmpty().toList()).isEmpty();
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#toArray()}.
	 */
	@Test
	@RandomizedTest
	default void testToArray(RandomGenerator rand) {
		Object[] items = randomContent(rand);
		S set = createFilled(items);
		assertThat(set.toArray()).containsExactly(items);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#toArray()}.
	 */
	@Test
	default void testToArrayEmpty() {
		assertThat(createEmpty()).isEmpty();
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
		assertThat(set.toArray(new Object[items.length])).containsExactly(items);
		// Growing
		assertThat(set.toArray(new Object[0])).containsExactly(items);

		// Null-marker in array with extra capacity
		Object[] target = new Object[items.length*2];
		Object[] result = set.toArray(target);
		assertThat(result).isSameAs(target).startsWith(target);
		assertThat(result[items.length]).isNull();
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#toArray(Object[]))}.
	 */
	@Test
	default void testToArrayTArrayEmpty() {
		assertThat(createEmpty().toArray(new Object[0])).isEmpty();
		assertThat(createEmpty().toArray(new Object[10])[0]).isNull();
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#emptySet()}.
	 */
	@Test
	default void testEmptySet() {
		assertThat(DataSet.emptySet().isEmpty()).isTrue();
	}

}
