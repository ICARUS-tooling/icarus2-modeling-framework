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
package de.ims.icarus2.util.collections;

import static de.ims.icarus2.SharedTestUtils.assertIcarusException;
import static de.ims.icarus2.test.TestUtils.DO_NOTHING;
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.test.TestUtils.assertArrayEmpty;
import static de.ims.icarus2.test.TestUtils.assertIOOB;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

/**
 * @author Markus Gärtner
 *
 */
@SuppressWarnings("rawtypes")
@RandomizedTest
class LookupListTest implements ApiGuardedTest<LookupList> {

	static RandomGenerator rand;

	@Override
	public Class<LookupList> getTestTargetClass() {
		return LookupList.class;
	}

	@Override
	public LookupList<?> createTestInstance(TestSettings settings) {
		return new LookupList<>();
	}

	/**
	 * Generates random size between 11 (inclusive) and 31 (exclusive)
	 */
	private int randomSize() {
		return rand.random(CollectionUtils.DEFAULT_COLLECTION_CAPACITY + 1,
				CollectionUtils.DEFAULT_COLLECTION_CAPACITY + 21);
	}

	private Object[] randomItems() {
		return randomItems(randomSize());
	}

	/** Creates array of given size whose items are strings */
	private Object[] randomItems(int count) {
		return IntStream.range(0, count)
				.mapToObj(i -> "item_"+i)
				.toArray();
	}

	@SafeVarargs
	private final <T> void fill(LookupList<T> list, T...items) {
		list.addAll(items);
	}

	@SafeVarargs
	private final <T> void assertItems(LookupList<T> list, T...items) {
		assertEquals(items.length, list.size());
		for (int i = 0; i < items.length; i++) {
			assertSame(items[i], list.get(i), "Mismatch for item on index "+i);
		}
	}

	private <T> void assertItems(LookupList<T> list, List<T> items) {
		assertEquals(items.size(), list.size());
		for (int i = 0; i < items.size(); i++) {
			assertSame(items.get(i), list.get(i), "Mismatch for item on index "+i);
		}
	}

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#LookupList()}.
		 */
		@Test
		void testLookupList() {
			assertNotNull(new LookupList<>());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#LookupList(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {1, 10, 100_000})
		void testLookupListInt(int capacity) {
			LookupList<Object> list = new LookupList<>(capacity);
			assertEquals(capacity, list.capacity());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#LookupList(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -1})
		void testLookupListIntInvalidCapacity(int capacity) {
			assertIcarusException(GlobalErrorCode.INVALID_INPUT,
					() -> new LookupList<>(capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#LookupList(java.util.Collection)}.
		 */
		@RepeatedTest(RUNS)
		void testLookupListCollectionOfQextendsE() {
			List<Object> items = list(randomItems());
			LookupList<Object> list = new LookupList<>(items);

			assertEquals(items.size(), list.size());
			assertEquals(items.size(), list.capacity());
			assertItems(list, items);
		}

	}

	@Nested
	class WithInstance {

		private LookupList<Object> instance;

		@BeforeEach
		void setUp() {
			instance = new LookupList<>();
		}

		@AfterEach
		void tearDown() {
			instance = null;
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#size()}.
		 */
		@Test
		void testSizeEmpty() {
			assertEquals(0, instance.size());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#add(java.lang.Object)}.
		 */
		@Disabled("covered by testGet()")
		@Test
		void testAddE() {
			fail("Not yet implemented");
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#add(int, java.lang.Object)}.
		 */
		@RepeatedTest(RUNS)
		void testAddIntEIncremental() {
			Object[] items = randomItems(rand.random(20, 30));

			for (int i = 0; i < items.length; i++) {
				instance.add(items[i]);
				assertSame(items[i], instance.get(i), "Mismatch at index "+i);
			}

			assertEquals(items.length, instance.size());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#add(int, java.lang.Object)}.
		 */
		@RepeatedTest(RUNS)
		void testAddIntERandom() {
			Object[] items = randomItems();
			List<Object> list = new ArrayList<>();

			for (int i = 0; i < items.length; i++) {
				int insertionIndex = rand.random(0, list.size()+1);
				instance.add(insertionIndex, items[i]);
				list.add(insertionIndex, items[i]);
			}

			assertItems(instance, list);
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#set(int, java.lang.Object)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -1, 1})
		void testSetEIntEmptyInvalidIndex(int index) {
			assertIOOB(() -> instance.set(index, new Object()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#remove(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -1, 1})
		void testRemoveIntEmptyInvalidIndex(int index) {
			assertIOOB(() -> instance.remove(index));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#removeAll(int, int, java.util.function.Consumer)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -1, 1})
		void testRemoveAllInvalidIndices(int index) {
			// Only tests the first index, as the list is empty and we cannot produce an invalid 2nd argument
			assertIOOB(() -> instance.removeAll(index, 0, DO_NOTHING()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#remove(java.lang.Object)}.
		 */
		@Test
		void testRemoveE() {
			assertFalse(instance.remove(new Object()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#clear()}.
		 */
		@Test
		void testClear() {
			instance.clear();
			assertTrue(instance.isEmpty());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#contains(java.lang.Object)}.
		 */
		@Test
		void testContains() {
			assertFalse(instance.contains(new Object()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#indexOf(java.lang.Object)}.
		 */
		@Test
		void testIndexOf() {
			assertEquals(UNSET_INT, instance.indexOf(new Object()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#isEmpty()}.
		 */
		@Test
		void testIsEmpty() {
			assertTrue(instance.isEmpty());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#toArray()}.
		 */
		@Test
		void testToArray() {
			assertArrayEmpty(instance.toArray());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#set(java.lang.Object[])}.
		 */
		@Test
		void testSetObjectArray() {
			Object[] items = randomItems();
			instance.set(items);
			assertItems(instance, items);
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#sort(java.util.Comparator)}.
		 */
		@Test
		void testSort() {
			Comparator<Object> comp = mock(Comparator.class);
			instance.sort(comp);
			verify(comp, never()).compare(any(), any());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#iterator()}.
		 */
		@Test
		void testIterator() {
			Iterator<Object> iterator = instance.iterator();
			assertNotNull(iterator);
			assertFalse(iterator.hasNext());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#remove(java.lang.Object)}.
		 */
		@Test
		void testRemoveENull() {
			fill(instance, new Object(), null, new Object());
			assertEquals(3, instance.size());
			assertTrue(instance.remove(null));
			assertEquals(2, instance.size());
		}

		@Nested
		class Internals {

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#rangeCheck(int)}.
			 */
			@Test
			void testRangeCheckEmpty() {
				assertIOOB(() -> instance.rangeCheck(0));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#rangeCheck(int)}.
			 */
			@RepeatedTest(RUNS)
			void testRangeCheckFilled() {
				int size = randomSize();
				fill(instance, randomItems(size));
				for (int i = 0; i < size; i++) {
					instance.rangeCheck(i);
				}

				IntStream.of(-1, size, size+1)
					.forEach(index -> assertIOOB(() -> instance.rangeCheck(index)));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#rangeCheckForAdd(int)}.
			 */
			@Test
			void testRangeCheckForAddEmpty() {
				instance.rangeCheckForAdd(0);

				assertIOOB(() -> instance.rangeCheck(1));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#rangeCheckForAdd(int)}.
			 */
			@RepeatedTest(RUNS)
			void testRangeCheckForAddFilled() {
				int size = randomSize();
				fill(instance, randomItems(size));
				for (int i = 0; i <= size; i++) {
					instance.rangeCheckForAdd(i);
				}

				IntStream.of(-1, size+1)
					.forEach(index -> assertIOOB(() -> instance.rangeCheckForAdd(index)));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#ensureCapacity(int)}.
			 */
			@ParameterizedTest
			@ValueSource(ints = {15, 101, 100_000})
			void testEnsureCapacity(int capacity) {
				instance.ensureCapacity(capacity);
				assertTrue(instance.capacity()>=capacity);
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#createLookup(int)}.
			 */
			@ParameterizedTest
			@ValueSource(ints = {10, 100, 100_000})
			void testCreateLookup(int capacity) {
				Object2IntMap<Object> lookup = instance.createLookup(capacity);
				assertNotNull(lookup);
				assertEquals(UNSET_INT, lookup.defaultReturnValue());
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#createLookup(int)}.
			 */
			@RepeatedTest(RUNS)
			void testCreateLookupAutomatically() {
				fill(instance, randomItems());

				// Create new random data (including null) to overwrite existing list
				Object[] items = randomItems(instance.size());
				items[rand.random(0, items.length)] = null;

				for (int i = 0; i < items.length; i++) {
					instance.set(i, items[i]);
					assertEquals(i, instance.indexOf(items[i]), "Mismatch on index "+i+" for "+items[i]);
				}
			}

		}

		@Nested
		class ForBatchInserts {

			private Object[] items;

			@BeforeEach
			void setUp() {
				items = randomItems();
			}

			@AfterEach
			void tearDown() {
				items = null;
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#addAll(java.util.Collection)}.
			 */
			@Test
			void testAddAllCollectionOfQextendsE() {
				Collection<Object> coll = list(items);
				instance.addAll(coll);
				assertItems(instance, items);
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#addAll(int, java.util.Collection)}.
			 */
			@Test
			void testAddAllIntCollectionOfQextendsE() {
				Collection<Object> coll = list(items);
				instance.addAll(0, coll);
				assertItems(instance, items);
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#addAll(int, java.util.Collection)}.
			 */
			@Test
			void testAddAllIntCollectionOfQextendsEPrefilled() {
				Collection<Object> coll = list(items);

				Object[] existing = randomItems();
				List<Object> list = list(existing);
				fill(instance, existing);

				int insertionIndex = rand.random(1, list.size()-1);
				list.addAll(insertionIndex, coll);

				instance.addAll(insertionIndex, coll);
				assertItems(instance, list);
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#addAll(E[])}.
			 */
			@Test
			void testAddAllEArray() {
				instance.addAll(items);
				assertItems(instance, items);
			}

		}

		@Nested
		class WhenFilled {
			private Object[] items;

			@BeforeEach
			void setUp() {
				items = randomItems();
				fill(instance, items);
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#size()}.
			 */
			@RepeatedTest(RUNS)
			void testSize() {
				assertEquals(items.length, instance.size());
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#get(int)}.
			 */
			@RepeatedTest(RUNS)
			void testGet() {
				assertItems(instance, items);
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#set(int, java.lang.Object)}.
			 */
			@RepeatedTest(RUNS)
			void testSetEInt() {
				IntStream.generate(() -> rand.random(0, items.length))
					.distinct()
					.limit(items.length/2)
					.forEach(index -> {
						Object item = new Object();
						items[index] = item;
						instance.set(index, item);
					});

				assertItems(instance, items);
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#remove(int)}.
			 */
			@RepeatedTest(RUNS)
			void testRemoveInt() {
				List<Object> list = list(items);

				while (!list.isEmpty()) {
					int index = rand.random(0, list.size());
					Object expected = list.remove(index);
					assertSame(expected, instance.remove(index));

					assertItems(instance, list);
				}

				assertTrue(instance.isEmpty());
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#removeAll(int, int, java.util.function.Consumer)}.
			 */
			@RepeatedTest(RUNS)
			void testRemoveAll() {
				int index0 = rand.random(0, items.length);
				int index1 = rand.random(index0, items.length);

				Consumer<Object> action = mock(Consumer.class);
				List<Object> list = list(items);
				list.subList(index0, index1+1).clear();

				instance.removeAll(index0, index1, action);

				for (int i = index0; i <= index1; i++) {
					verify(action).accept(items[i]);
				}

				assertItems(instance, list);
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#removeAll(int, int, java.util.function.Consumer)}.
			 */
			@RepeatedTest(RUNS)
			void testRemoveAllInvalidIndices() {
				int index1 = rand.random(0, items.length/2);
				int index0 = rand.random(index1+1, items.length);

				assertThrows(IllegalArgumentException.class,
						() -> instance.removeAll(index0, index1, mock(Consumer.class)));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#remove(java.lang.Object)}.
			 */
			@RepeatedTest(RUNS)
			void testRemoveE() {
				List<Object> list = list(items);

				assertFalse(instance.remove(new Object()));

				while (!list.isEmpty()) {
					Object item = list.remove(rand.random(0, list.size()));
					assertTrue(instance.remove(item));

					assertItems(instance, list);
				}

				assertFalse(instance.remove(new Object()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#remove(java.lang.Object)}.
			 */
			@Test
			void testRemoveENull() {
				assertFalse(instance.remove(null));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#clear()}.
			 */
			@Test
			void testClear() {
				instance.clear();
				assertTrue(instance.isEmpty());
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#contains(java.lang.Object)}.
			 */
			@Test
			void testContains() {
				for(Object item : items) {
					assertTrue(instance.contains(item));
				}
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#contains(java.lang.Object)}.
			 */
			@Test
			void testContainsForeign() {
				assertFalse(instance.contains(new Object()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#indexOf(java.lang.Object)}.
			 */
			@Test
			void testIndexOf() {
				for (int i = 0; i < items.length; i++) {
					assertEquals(i, instance.indexOf(items[i]));
				}

				assertEquals(UNSET_INT, instance.indexOf(new Object()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.collections.LookupList#isEmpty()}.
			 */
			@Test
			void testIsEmpty() {
				assertFalse(instance.isEmpty());
			}

			@Nested
			class ForIterator {
				private Iterator<Object> iterator;

				@BeforeEach
				void setUp() {
					iterator = instance.iterator();
				}

				@AfterEach
				void tearDown() {
					iterator = null;
				}

				/**
				 * Test method for {@link de.ims.icarus2.util.collections.LookupList.Itr#hasNext()}.
				 */
				@RepeatedTest(RUNS)
				void testHasNext() {
					for (int i = 0; i < items.length; i++) {
						assertTrue(iterator.hasNext());
					}
				}

				/**
				 * Test method for {@link de.ims.icarus2.util.collections.LookupList.Itr#next()}.
				 */
				@RepeatedTest(RUNS)
				void testNext() {
					for (int i = 0; i < items.length; i++) {
						assertSame(items[i], iterator.next());
					}

					assertThrows(NoSuchElementException.class, () -> iterator.next());
				}

				/**
				 * Test method for {@link de.ims.icarus2.util.collections.LookupList.Itr#next()}.
				 */
				@Test
				void testNextConcurrentModification() {
					instance.remove(0);
					assertThrows(ConcurrentModificationException.class,
							() -> iterator.next());
				}

				/**
				 * Test method for {@link de.ims.icarus2.util.collections.LookupList.Itr#remove()}.
				 */
				@RepeatedTest(RUNS)
				void testRemove() {
					int removalIndex = rand.random(1, items.length);
					Object item = instance.get(removalIndex);
					assertEquals(removalIndex, instance.indexOf(item));

					for (int i = 0; i <= removalIndex; i++) {
						iterator.next();
					}

					iterator.remove();
					assertEquals(UNSET_INT, instance.indexOf(item));
				}

				/**
				 * Test method for {@link de.ims.icarus2.util.collections.LookupList.Itr#remove()}.
				 */
				@Test
				void testRemoveConcurrentModification() {
					iterator.next();
					instance.remove(0);
					assertThrows(ConcurrentModificationException.class,
							() -> iterator.remove());
				}

				/**
				 * Test method for {@link de.ims.icarus2.util.collections.LookupList.Itr#remove()}.
				 */
				@Test
				void testRemoveInitial() {
					assertThrows(IllegalStateException.class,
							() -> iterator.remove());
				}
			}
		}
	}

	@Nested
	class Plain {

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#capacity()}.
		 */
		@Test
		void testCapacity() {
			assertEquals(0, create().capacity());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#trim()}.
		 */
		@Test
		void testTrimEmpty() {
			LookupList<Object> list = new LookupList<>();
			assertEquals(0, list.capacity());

			list.trim();
			assertEquals(0, list.capacity());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#trim()}.
		 */
		@RepeatedTest(RUNS)
		void testTrimWithCapacity() {
			int initialCapacity = randomSize();
			LookupList<Object> list = new LookupList<>(initialCapacity);
			assertEquals(initialCapacity, list.capacity());

			list.trim();
			assertEquals(0, list.capacity());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.LookupList#trim()}.
		 */
		@RepeatedTest(RUNS)
		void testTrimWithContent() {
			int initialCapacity = randomSize();
			LookupList<Object> list = new LookupList<>(initialCapacity);
			Object[] items = randomItems(rand.random(1, initialCapacity/4));
			fill(list, items);

			list.trim();
			assertEquals(items.length, list.capacity());
		}

	}

}
