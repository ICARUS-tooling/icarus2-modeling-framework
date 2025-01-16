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

import static de.ims.icarus2.SharedTestUtils.assertIcarusException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
class SingletonSetTest implements DataSetTest<SingletonSet<Object>> {

	@Override
	public Class<?> getTestTargetClass() {
		return SingletonSet.class;
	}

	@Override
	public SingletonSet<Object> createEmpty() {
		return new SingletonSet<>();
	}

	/**
	 * @see de.ims.icarus2.util.collections.set.DataSetTest#randomContent()
	 */
	@Override
	public Object[] randomContent(RandomGenerator rand) {
		return new Object[] {new Object()};
	}

	@Override
	public SingletonSet<Object> createFilled(Object... items) {
		assertTrue(items.length<=1);
		return new SingletonSet<>(items[0]);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.SingletonSet#SingletonSet()}.
	 */
	@Test
	void testSingletonSet() {
		assertNotNull(new SingletonSet<>());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.SingletonSet#SingletonSet(java.lang.Object)}.
	 */
	@Test
	void testSingletonSetE() {
		Object item = new Object();
		SingletonSet<Object> set = new SingletonSet<Object>(item);
		assertEquals(1, set.entryCount());
		assertSame(item, set.entryAt(0));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.SingletonSet#reset(java.lang.Object)}.
	 */
	@Test
	void testReset() {
		Object item = new Object();
		SingletonSet<Object> set = createEmpty();
		set.reset(item);
		assertEquals(1, set.entryCount());
		assertSame(item, set.entryAt(0));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.SingletonSet#add(Object)}.
	 */
	@Test
	void testAdd() {
		Object item = new Object();
		SingletonSet<Object> set = createEmpty();
		set.add(item);
		assertEquals(1, set.entryCount());
		assertSame(item, set.entryAt(0));

		assertIcarusException(GlobalErrorCode.ILLEGAL_STATE, () -> set.add(new Object()));
	}

}
