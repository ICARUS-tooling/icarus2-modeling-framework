/**
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
package de.ims.icarus2.model.standard.driver.cache;

import static de.ims.icarus2.SharedTestUtils.mockSequence;
import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
class MemberPoolTest {

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberPool#MemberPool()}.
		 */
		@Test
		void noArgs() {
			assertNotNull(new MemberPool<>());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberPool#MemberPool(int)}.
		 */
		@Test
		void withSize() {
			assertNotNull(new MemberPool<>(10));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberPool#MemberPool(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -1, Integer.MIN_VALUE})
		void withIllegalSize(int value) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new MemberPool<>(value));
		}

	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberPool#recycle(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	void testRecycle() {
		MemberPool<Item> pool = new MemberPool<>(3);

		for (int i = 0; i < 3; i++) {
			assertTrue(pool.recycle(mockItem()));
		}

		assertFalse(pool.recycle(mockItem()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberPool#isEmpty()}.
	 */
	@Test
	void testIsEmpty() {
		MemberPool<Item> pool = new MemberPool<>();
		assertTrue(pool.isEmpty());

		assertTrue(pool.recycle(mockItem()));
		assertFalse(pool.isEmpty());

		assertNotNull(pool.revive());
		assertTrue(pool.isEmpty());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberPool#revive()}.
	 */
	@Test
	void testRevive() {
		MemberPool<Item> pool = new MemberPool<>();

		assertTrue(pool.recycle(mockItem()));
		assertTrue(pool.recycle(mockItem()));
		assertTrue(pool.recycle(mockItem()));

		assertNotNull(pool.revive());
		assertNotNull(pool.revive());
		assertNotNull(pool.revive());

		assertNull(pool.revive());
		assertTrue(pool.isEmpty());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberPool#recycleAll(java.util.Collection)}.
	 */
	@Test
	void testRecycleAllCollectionOfQextendsM() {
		MemberPool<Item> pool = new MemberPool<>();

		Item item1 = mockItem();
		Item item2 = mockItem();
		Item item3 = mockItem();

		Set<Item> items = set(item1, item2, item3);

		pool.recycleAll(items);
		assertFalse(pool.isEmpty());

		for (int i = 0; i < items.size(); i++) {
			assertTrue(items.contains(pool.revive()));
		}

		assertNull(pool.revive());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberPool#recycleAll(de.ims.icarus2.util.collections.seq.DataSequence)}.
	 */
	@Test
	void testRecycleAllDataSequenceOfQextendsM() {
		MemberPool<Item> pool = new MemberPool<>();

		Item item1 = mockItem();
		Item item2 = mockItem();
		Item item3 = mockItem();

		DataSequence<Item> items = mockSequence(item1, item2, item3);

		pool.recycleAll(items);
		assertFalse(pool.isEmpty());

		verify(items).elementAt(0);
		verify(items).elementAt(1);
		verify(items).elementAt(2);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberPool#get()}.
	 */
	@Test
	void testGet() {
		MemberPool<Item> pool = new MemberPool<>();

		assertTrue(pool.recycle(mockItem()));
		assertTrue(pool.recycle(mockItem()));
		assertTrue(pool.recycle(mockItem()));

		assertNotNull(pool.get());
		assertNotNull(pool.get());
		assertNotNull(pool.get());

		assertTrue(pool.isEmpty());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberPool#accept(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	void testAccept() {
		MemberPool<Item> pool = new MemberPool<>();

		// no return value as indicator of full pool here
		pool.accept(mockItem());
	}

}
