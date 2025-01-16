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
package de.ims.icarus2.model.standard.driver.cache;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.api.ModelTestUtils.stubIndex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.members.item.Item;

/**
 * @author Markus Gärtner
 *
 */
class MemberCacheTest {

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberCache#MemberCache()}.
		 */
		@Test
		void noArgs() {
			assertNotNull(new MemberCache<>());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberCache#MemberCache(int)}.
		 */
		@Test
		void withCapacity() {
			assertNotNull(new MemberCache<>(10));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberCache#MemberCache(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -1, Integer.MIN_VALUE})
		void withInvalidCapacity(int value) {
			assertModelException(GlobalErrorCode.INVALID_INPUT, () -> new MemberCache<>(value));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberCache#MemberCache(int, int, float)}.
		 */
		@Test
		void full() {
			assertNotNull(new MemberCache<>(10, 100, 0.5f));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberCache#MemberCache(int, int, float)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -1, Integer.MIN_VALUE})
		void fullIllegalCapacity(int value) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new MemberCache<>(value, 10, 0.5f));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberCache#MemberCache(int, int, float)}.
		 */
		@ParameterizedTest
		@ValueSource(floats = {0f, -1f, -Float.MAX_VALUE})
		void fullIllegalGrowthFactor(float value) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new MemberCache<>(10, 10, value));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberCache#size()}.
	 */
	@Test
	void testSize() {
		MemberCache<Item> cache = new MemberCache<>();
		assertEquals(0, cache.size());

		assertTrue(cache.register(1, mockItem()));
		assertEquals(1, cache.size());

		assertTrue(cache.register(3, mockItem()));
		assertEquals(2, cache.size());

		cache.remove(1);
		assertEquals(1, cache.size());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberCache#isEmpty()}.
	 */
	@Test
	void testIsEmpty() {
		MemberCache<Item> cache = new MemberCache<>();
		assertTrue(cache.isEmpty());

		assertTrue(cache.register(1, mockItem()));
		assertFalse(cache.isEmpty());

		cache.remove(1);
		assertTrue(cache.isEmpty());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberCache#isCached(long)}.
	 */
	@Test
	void testIsCachedLong() {
		MemberCache<Item> cache = new MemberCache<>();
		assertFalse(cache.isCached(1));
		assertFalse(cache.isCached(3));

		assertTrue(cache.register(1, mockItem()));
		assertTrue(cache.isCached(1));
		assertFalse(cache.isCached(3));

		assertTrue(cache.register(3, mockItem()));
		assertTrue(cache.isCached(1));
		assertTrue(cache.isCached(3));

		cache.remove(1);
		assertFalse(cache.isCached(1));
		assertTrue(cache.isCached(3));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberCache#isCached(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	void testIsCachedM() {
		MemberCache<Item> cache = new MemberCache<>();
		Item item1 =  stubIndex(mockItem(), 1);
		Item item3 =  stubIndex(mockItem(), 3);
		assertFalse(cache.isCached(item1));
		assertFalse(cache.isCached(item3));

		assertTrue(cache.register(item1));
		assertTrue(cache.isCached(item1));
		assertFalse(cache.isCached(item3));

		assertTrue(cache.register(item3));
		assertTrue(cache.isCached(item1));
		assertTrue(cache.isCached(item3));

		cache.remove(item1);
		assertFalse(cache.isCached(item1));
		assertTrue(cache.isCached(item3));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberCache#register(long, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	@Disabled("covered in testIsCachedLong()")
	void testRegister() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberCache#register(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	@Disabled("covered in testIsCachedM()")
	void testRegisterM() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberCache#lookup(long)}.
	 */
	@Test
	void testLookup() {
		MemberCache<Item> cache = new MemberCache<>();
		Item item1 =  stubIndex(mockItem(), 1);
		Item item3 =  stubIndex(mockItem(), 3);

		assertNull(cache.lookup(1));
		assertNull(cache.lookup(3));

		assertTrue(cache.register(item1));
		assertSame(item1, cache.lookup(1));
		assertNull(cache.lookup(3));

		assertTrue(cache.register(item3));
		assertSame(item1, cache.lookup(1));
		assertSame(item3, cache.lookup(3));

		cache.remove(item1);
		assertNull(cache.lookup(1));
		assertSame(item3, cache.lookup(3));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberCache#remove(long)}.
	 */
	@Test
	@Disabled("covered in testIsCachedLong()")
	void testRemoveLong() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberCache#remove(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	@Disabled("covered in testIsCachedM()")
	void testRemoveM() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberCache#clear()}.
	 */
	@Test
	void testClear() {
		MemberCache<Item> cache = new MemberCache<>();
		assertTrue(cache.register(1, mockItem()));
		assertTrue(cache.register(10, mockItem()));
		assertTrue(cache.register(100, mockItem()));

		assertFalse(cache.isEmpty());

		cache.clear();
		assertTrue(cache.isEmpty());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.MemberCache#recycleTo(de.ims.icarus2.model.standard.driver.cache.MemberPool)}.
	 */
	@Test
	void testRecycleTo() {
		MemberCache<Item> cache = new MemberCache<>();

		Item item1 =  stubIndex(mockItem(), 1);
		Item item2 =  stubIndex(mockItem(), 2);
		Item item3 =  stubIndex(mockItem(), 3);

		assertTrue(cache.register(item1));
		assertTrue(cache.register(item2));
		assertTrue(cache.register(item3));
		assertEquals(3, cache.size());

		MemberPool<Item> pool = mock(MemberPool.class);

		cache.recycleTo(pool);
		assertTrue(cache.isEmpty());

		verify(pool).recycle(item1);
		verify(pool).recycle(item2);
		verify(pool).recycle(item3);
	}

}
