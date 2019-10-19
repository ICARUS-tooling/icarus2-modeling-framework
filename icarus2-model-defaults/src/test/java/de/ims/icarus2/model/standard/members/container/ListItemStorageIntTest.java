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
package de.ims.icarus2.model.standard.members.container;

import static de.ims.icarus2.SharedTestUtils.mockSequence;
import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItems;
import static de.ims.icarus2.model.api.ModelTestUtils.stubOffsets;
import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.test.TestUtils.assertGetter;
import static de.ims.icarus2.test.TestUtils.assertIOOB;
import static de.ims.icarus2.test.TestUtils.assertSetter;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.collections.LookupList;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
class ListItemStorageIntTest implements ItemStorageTest<ListItemStorageInt> {

	@Nested
	class Constructors {

		@Test
		void noArgs() {
			assertNotNull(new ListItemStorageInt());
		}

		@Test
		void withCapacity() {
			assertNotNull(new ListItemStorageInt(100));
		}

		@Test
		void withDefaultCapacity() {
			assertNotNull(new ListItemStorageInt(UNSET_INT));
		}

		@Test
		void invalidCapacity() {
			IcarusRuntimeException exception = assertThrows(IcarusRuntimeException.class,
					() -> new ListItemStorageInt(-10));
			assertEquals(GlobalErrorCode.INVALID_INPUT, exception.getErrorCode());
		}
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends ListItemStorageInt> getTestTargetClass() {
		return ListItemStorageInt.class;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public ListItemStorageInt createTestInstance(TestSettings settings) {
		return settings.process(new ListItemStorageInt());
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorageTest#getExpectedContainerType()
	 */
	@Override
	public ContainerType getExpectedContainerType() {
		return ContainerType.LIST;
	}

	private static Item[] randomItems(RandomGenerator rng) {
		return mockItems(rng.random(1, 30));
	}

	private static void fill(ListItemStorageInt storage, Item...items) {
		for (int i = 0; i < items.length; i++) {
			storage.addItem(null, i, items[i]);
		}
	}

	private static void assertItems(ListItemStorageInt storage, Item...items) {
		assertEquals(items.length, storage.getItemCount(null));
		for (int i = 0; i < items.length; i++) {
			assertSame(items[i], storage.getItemAt(null, i), "Item mismatch at index "+i);
		}
	}

	private static void assertItems(ListItemStorageInt storage, List<Item> items) {
		assertEquals(items.size(), storage.getItemCount(null));
		for (int i = 0; i < items.size(); i++) {
			assertSame(items.get(i), storage.getItemAt(null, i), "Item mismatch at index "+i);
		}
	}

	@Nested
	class WithInstance {
		private ListItemStorageInt storage;

		@BeforeEach
		void setUp() {
			storage = create();
		}

		@AfterEach
		void tearDown() {
			storage = null;
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#getItemCount(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testGetItemCount() {
			assertEquals(0, storage.getItemCount(null));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		void testIndexOfItem() {
			assertEquals(UNSET_INT, storage.indexOfItem(null, mockItem()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#setBeginItem(de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		void testSetBeginItem() {
			assertSetter(storage, ListItemStorageInt::setBeginItem, mockItem(), false, NO_CHECK);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#setEndItem(de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		void testSetEndItem() {
			assertSetter(storage, ListItemStorageInt::setEndItem, mockItem(), false, NO_CHECK);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#getBeginOffset(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		@RandomizedTest
		void testGetBeginOffset(RandomGenerator rng) {
			assertEquals(UNSET_LONG, storage.getBeginOffset(null));

			long index = rng.randomId();
			Item item = stubOffsets(mockItem(), index, index);
			storage.setBeginItem(item);

			assertSame(item, storage.getBeginItem());
			assertEquals(item.getBeginOffset(), storage.getBeginOffset(null));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#getEndOffset(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		@RandomizedTest
		void testGetEndOffset(RandomGenerator rng) {
			assertEquals(UNSET_LONG, storage.getEndOffset(null));

			long index = rng.randomId();
			Item item = stubOffsets(mockItem(), index, index);
			storage.setEndItem(item);

			assertSame(item, storage.getEndItem());
			assertEquals(item.getEndOffset(), storage.getEndOffset(null));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#getBeginItem()}.
		 */
		@Test
		void testGetBeginItem() {
			assertGetter(storage,
					mockItem(), mockItem(),
					NO_DEFAULT(),
					ListItemStorageInt::getBeginItem,
					ListItemStorageInt::setBeginItem);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#getEndItem()}.
		 */
		@Test
		void testGetEndItem() {
			assertGetter(storage,
					mockItem(), mockItem(),
					NO_DEFAULT(),
					ListItemStorageInt::getEndItem,
					ListItemStorageInt::setEndItem);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#revive()}.
		 */
		@Test
		void testRevive() {
			assertTrue(storage.revive());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#swapItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, 1, -1})
		void testSwapItems(int index) {
			// Can only test the first index here
			assertIOOB(() -> storage.swapItems(null, index, 0));
		}

		@Nested
		class Internals {

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#createItemsBuffer(int)}.
			 */
			@ParameterizedTest
			@ValueSource(ints = {-1, 1, 10, 100_000})
			void testCreateItemsBuffer(int capacity) {
				int expectedCapacity = capacity==-1 ? ListItemStorageInt.DEFAULT_CAPACITY : capacity;
				LookupList<Item> list = storage.createItemsBuffer(capacity);
				assertEquals(expectedCapacity, list.capacity());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#refreshOffsetItems()}.
			 */
			@Test
			void testRefreshOffsetItems() {
				Item item0 = mockItem();
				Item item1 = mockItem();

				fill(storage, item0, item1);

				storage.refreshOffsetItems();

				verify(item0, never()).getBeginOffset();
				verify(item0, never()).getEndOffset();
				verify(item1, never()).getBeginOffset();
				verify(item1, never()).getEndOffset();
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#tryRefreshOffsetItems(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testTryRefreshOffsetItems() {
				Item item = mockItem();

				storage.tryRefreshOffsetItems(item);

				assertNull(storage.getBeginItem());
				assertNull(storage.getEndItem());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#maybeClearOffsetItems(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testMaybeClearOffsetItems() {
				Item item0 = mockItem();
				Item item1 = mockItem();

				storage.setBeginItem(item0);
				storage.setEndItem(item1);

				storage.maybeClearOffsetItems(item0);
				storage.maybeClearOffsetItems(item1);

				assertSame(item0, storage.getBeginItem());
				assertSame(item1, storage.getEndItem());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#clearOffsetItems()}.
			 */
			@Test
			void testClearOffsetItems() {
				storage.setBeginItem(mockItem());
				storage.setEndItem(mockItem());

				storage.clearOffsetItems();

				assertNull(storage.getBeginItem());
				assertNull(storage.getEndItem());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#isDoStoreItemsForOffset()}.
			 */
			@Test
			void testIsDoStoreItemsForOffset() {
				assertFalse(storage.isDoStoreItemsForOffset());
			}

			@Nested
			class WithOffsetCachee {

				private Container context;

				@BeforeEach
				void setUp() {
					TargetLayerManifest foundation = mock(TargetLayerManifest.class);
					ItemLayerManifest layer = mock(ItemLayerManifest.class);
					when(layer.getFoundationLayerManifest()).thenReturn(Optional.of(foundation));
					ContainerManifest manifest = mock(ContainerManifest.class);
					when(manifest.getLayerManifest()).thenReturn(Optional.of(layer));
					context = mockContainer();
					when((ContainerManifest)context.getManifest()).thenReturn(manifest);

					storage.addNotify(context);
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#isDoStoreItemsForOffset()}.
				 */
				@Test
				void testIsDoStoreItemsForOffset() {
					assertTrue(storage.isDoStoreItemsForOffset());
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#refreshOffsetItems()}.
				 */
				@Test
				void testRefreshOffsetItems() {
					Item item0 = stubOffsets(mockItem(), 10, 10);
					Item item1 = stubOffsets(mockItem(), 20, 20);

					fill(storage, item0, item1);

					storage.refreshOffsetItems();

					verify(item0, atLeastOnce()).getBeginOffset();
					verify(item0, atLeastOnce()).getEndOffset();
					verify(item1, atLeastOnce()).getBeginOffset();
					verify(item1, atLeastOnce()).getEndOffset();

					assertSame(item0, storage.getBeginItem());
					assertSame(item1, storage.getEndItem());
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#tryRefreshOffsetItems(de.ims.icarus2.model.api.members.item.Item)}.
				 */
				@Test
				void testTryRefreshOffsetItems() {
					Item item0 = stubOffsets(mockItem(), 10, 10);
					Item item1 = stubOffsets(mockItem(), 20, 20);

					storage.setBeginItem(item0);
					storage.setEndItem(item1);

					Item item2 = stubOffsets(mockItem(), 5, 5); // outside
					Item item3 = stubOffsets(mockItem(), 25, 25); // outside
					Item item4 = stubOffsets(mockItem(), 21, 21); // inside (after expanding the bounds)

					// Expand begin
					storage.tryRefreshOffsetItems(item2);
					assertSame(item2, storage.getBeginItem());
					assertSame(item1, storage.getEndItem());

					// Expand end
					storage.tryRefreshOffsetItems(item3);
					assertSame(item2, storage.getBeginItem());
					assertSame(item3, storage.getEndItem());

					// Nothing happening
					storage.tryRefreshOffsetItems(item4);
					assertSame(item2, storage.getBeginItem());
					assertSame(item3, storage.getEndItem());
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#maybeClearOffsetItems(de.ims.icarus2.model.api.members.item.Item)}.
				 */
				@Test
				void testMaybeClearOffsetItems() {
					Item item0 = mockItem();
					Item item1 = mockItem();

					storage.setBeginItem(item0);
					storage.setEndItem(item1);

					// Foreign item shouldn't change the bounds
					storage.maybeClearOffsetItems(mockItem());
					assertSame(item0, storage.getBeginItem());
					assertSame(item1, storage.getEndItem());

					storage.maybeClearOffsetItems(item0);

					assertNull(storage.getBeginItem());
					assertNull(storage.getEndItem());
				}
			}

		}

		@Nested
		@RandomizedTest
		class WithRandomItems {

			private Item[] items;

			@BeforeEach
			void setUp(RandomGenerator rng) {
				items = randomItems(rng);
			}

			@AfterEach
			void tearDown() {
				items = null;
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#addItems(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.util.collections.seq.DataSequence)}.
			 */
			@RepeatedTest(RUNS)
			void testAddItems() {
				storage.addItems(null, 0, mockSequence(items));

				assertEquals(items.length, storage.getItemCount(null));
			}

			@Nested
			class Prefilled {

				@BeforeEach
				void setUp() {
					fill(storage, items);
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#getItemCount(de.ims.icarus2.model.api.members.container.Container)}.
				 */
				@RepeatedTest(RUNS)
				void testGetItemCount() {
					assertEquals(items.length, storage.getItemCount(null));
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#getItemAt(de.ims.icarus2.model.api.members.container.Container, long)}.
				 */
				@RepeatedTest(RUNS)
				void testGetItemAt() {
					assertItems(storage, items);
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#addItem(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.model.api.members.item.Item)}.
				 */
				@Test
				@Disabled("covered by testGetItemAt()")
				void testAddItem() {
					fail("Not yet implemented");
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)}.
				 */
				@RepeatedTest(RUNS)
				void testIndexOfItem() {
					for (int i = 0; i < items.length; i++) {
						assertEquals(i, storage.indexOfItem(null, items[i]));
					}
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#removeItem(de.ims.icarus2.model.api.members.container.Container, long)}.
				 */
				@RepeatedTest(RUNS)
				@RandomizedTest
				void testRemoveItem(RandomGenerator rng) {
					List<Item> buffer = list(items);
					while (!buffer.isEmpty()) {
						int index = rng.random(0, buffer.size());
						Item expected = buffer.remove(index);
						assertSame(expected, storage.removeItem(null, index));
						assertEquals(buffer.size(), storage.getItemCount(null));
					}
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#removeItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
				 */
				@RepeatedTest(RUNS)
				@RandomizedTest
				void testRemoveItems(RandomGenerator rng) {
					List<Item> buffer = list(items);
					while (!buffer.isEmpty()) {
						int index0 = rng.random(0, buffer.size());
						int index1 = rng.random(index0, buffer.size());

						List<Item> subList = buffer.subList(index0, index1+1);
						Item[] expected = subList.toArray(new Item[subList.size()]);
						subList.clear();

						DataSequence<? extends Item> removed = storage.removeItems(null, index0, index1);
						assertEquals(expected.length, removed.entryCount());
						for (int i = 0; i < expected.length; i++) {
							assertSame(expected[i], removed.elementAt(i), "Mismatch of removed items at index "+i);
						}
						assertEquals(buffer.size(), storage.getItemCount(null));

						assertItems(storage, buffer);
					}

					assertEquals(0, storage.getItemCount(null));
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#clear()}.
				 */
				@Test
				void testClear() {
					storage.clear();
					assertEquals(0, storage.getItemCount(null));
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#getBeginOffset(de.ims.icarus2.model.api.members.container.Container)}.
				 */
				@Test
				void testGetBeginOffset() {
					// Boundary cache disabled
					assertEquals(UNSET_LONG, storage.getBeginOffset(null));
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#getEndOffset(de.ims.icarus2.model.api.members.container.Container)}.
				 */
				@Test
				void testGetEndOffset() {
					// Boundary cache disabled
					assertEquals(UNSET_LONG, storage.getEndOffset(null));
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#getBeginItem()}.
				 */
				@Test
				void testGetBeginItem() {
					// Boundary cache disabled
					assertNull(storage.getBeginItem());
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#getEndItem()}.
				 */
				@Test
				void testGetEndItem() {
					// Boundary cache disabled
					assertNull(storage.getEndItem());
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#revive()}.
				 */
				@Test
				void testRevive() {
					assertTrue(storage.revive());
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.container.ListItemStorageInt#swapItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
				 */
				@RepeatedTest(RUNS)
				@RandomizedTest
				void testSwapItems(RandomGenerator rng) {
					List<Item> list = list(items);

					for (int i = 0; i < items.length/2; i++) {
						int index0 = rng.random(0, items.length-1);
						int index1 = rng.random(index0+1, items.length);

						Item item0 = list.get(index0);
						Item item1 = list.get(index1);
						list.set(index0, item1);
						list.set(index1, item0);

						storage.swapItems(null, index0, index1);
						assertSame(item0, storage.getItemAt(null, index1));
						assertSame(item1, storage.getItemAt(null, index0));
					}

					assertItems(storage, list);
				}
			}
		}
	}

}
