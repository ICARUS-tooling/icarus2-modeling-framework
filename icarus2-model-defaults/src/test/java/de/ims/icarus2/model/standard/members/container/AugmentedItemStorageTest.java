/**
 *
 */
package de.ims.icarus2.model.standard.members.container;

import static de.ims.icarus2.SharedTestUtils.assertIcarusException;
import static de.ims.icarus2.SharedTestUtils.mockSequence;
import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerFlag;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.util.collections.LookupList;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
class AugmentedItemStorageTest implements ItemStorageTest<AugmentedItemStorage> {

	@Override
	public Class<? extends AugmentedItemStorage> getTestTargetClass() {
		return AugmentedItemStorage.class;
	}

	@Override
	public AugmentedItemStorage createTestInstance(TestSettings settings) {
		return settings.process(new AugmentedItemStorage(createTargetContainer()));
	}

	@Override
	public ContainerType getExpectedContainerType() {
		return ContainerType.LIST;
	}

	@SuppressWarnings("boxing")
	private static Container createTargetContainer() {
		ContainerManifest manifest = mock(ContainerManifest.class);
		when(manifest.isContainerFlagSet(ContainerFlag.NON_STATIC)).thenReturn(Boolean.FALSE);
		Container container = mockContainer();
		when((ContainerManifest)container.getManifest()).thenReturn(manifest);
		when(container.getContainerType()).thenReturn(ContainerType.LIST);
		return container;
	}

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#AugmentedItemStorage(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testAugmentedItemStorageContainer() {
			assertNotNull(new AugmentedItemStorage(createTargetContainer()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#AugmentedItemStorage(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@SuppressWarnings("boxing")
		@Test
		void testAugmentedItemStorageContainerNonStatic() {
			ContainerManifest manifest = mock(ContainerManifest.class);
			when(manifest.isContainerFlagSet(ContainerFlag.NON_STATIC)).thenReturn(Boolean.TRUE);
			Container container = mockContainer();
			when((ContainerManifest)container.getManifest()).thenReturn(manifest);

			assertModelException(ModelErrorCode.MODEL_ILLEGAL_LINKING,
					() -> new AugmentedItemStorage(container));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#AugmentedItemStorage(de.ims.icarus2.model.api.members.container.Container, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {10, 100, 10_000})
		void testAugmentedItemStorageContainerIntFixedCapacity(int capacity) {
			assertNotNull(new AugmentedItemStorage(createTargetContainer(), capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#AugmentedItemStorage(de.ims.icarus2.model.api.members.container.Container, int)}.
		 */
		@Test
		void testAugmentedItemStorageContainerIntDefaultCapacity() {
			assertNotNull(new AugmentedItemStorage(createTargetContainer(), UNSET_INT));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#AugmentedItemStorage(de.ims.icarus2.model.api.members.container.Container, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -2})
		void testAugmentedItemStorageContainerIntInvalidCapacity(int capacity) {
			assertIcarusException(GlobalErrorCode.INVALID_INPUT,
					() -> new AugmentedItemStorage(createTargetContainer(), capacity));
		}

	}

	@Nested
	class WithInstance {
		private AugmentedItemStorage storage;
		private Container source;

		@BeforeEach
		void setUp() {
			source = createTargetContainer();
			storage = new AugmentedItemStorage(source);
		}

		@AfterEach
		void tearDown() {
			source = null;
			storage = null;
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#recycle()}.
		 */
		@Test
		void testRecycle() {
			storage.recycle();
		}

		@Nested
		class WithRandomWrappedSize {
			private long size;

			@SuppressWarnings("boxing")
			@BeforeEach
			void setUp() {
				size = random(1, Long.MAX_VALUE/2);
				when(source.getItemCount()).thenReturn(size);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#recycle()}.
			 */
			@SuppressWarnings("boxing")
			@Test
			void testRecycle() {
				Item item = mockItem();
				when(source.indexOfItem(item)).thenReturn(UNSET_LONG);

				storage.addItem(null, size, item);

				storage.recycle();

				assertNull(storage.getSourceContainer());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#getItemCount(de.ims.icarus2.model.api.members.container.Container)}.
			 */
			@Test
			void testGetItemCount() {
				assertEquals(size, storage.getItemCount(null));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#getItemAt(de.ims.icarus2.model.api.members.container.Container, long)}.
			 */
			@RepeatedTest(RUNS)
			void testGetItemAt() {
				long index = random(0, size);
				storage.getItemAt(null, index);
				verify(source).getItemAt(index);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#getItemAt(de.ims.icarus2.model.api.members.container.Container, long)}.
			 */
			@Test
			void testGetItemAtInvalidIndex() {
				assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
						() -> storage.getItemAt(null, -1));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@SuppressWarnings("boxing")
			@Test
			void testIndexOfItem() {
				Item item = mockItem();
				long index = random(0, size);
				when(source.indexOfItem(item)).thenReturn(index);
				assertEquals(index, storage.indexOfItem(null, item));
				verify(source).indexOfItem(item);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@SuppressWarnings("boxing")
			@Test
			void testIndexOfItemUnknown() {
				when(source.indexOfItem(any())).thenReturn(UNSET_LONG);
				assertEquals(UNSET_LONG, storage.indexOfItem(null, mockItem()));
				verify(source).indexOfItem(any());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#addItem(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testAddItemInvalidIndex() {
				assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
						() -> storage.addItem(null, 0, mockItem()));
				assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
						() -> storage.addItem(null, size-1, mockItem()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#addItem(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testAddItem() {
				Item item = mockItem();
				storage.addItem(null, size, item);
				assertEquals(size+1, storage.getItemCount(null));
				assertEquals(size, storage.indexOfItem(null, item));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#addItems(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.util.collections.seq.DataSequence)}.
			 */
			@Test
			void testAddItems() {
				Item item0 = mockItem();
				Item item1 = mockItem();
				DataSequence<Item> items = mockSequence(item0, item1);
				storage.addItems(null, size, items);
				assertEquals(size+2, storage.getItemCount(null));
				assertEquals(size, storage.indexOfItem(null, item0));
				assertEquals(size+1, storage.indexOfItem(null, item1));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#addItems(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.util.collections.seq.DataSequence)}.
			 */
			@Test
			void testAddItemsInvalidIndex() {
				assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
						() -> storage.addItems(null, 0, mockSequence(mockItem())));
				assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
						() -> storage.addItems(null, size-1, mockSequence(mockItem())));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#removeItem(de.ims.icarus2.model.api.members.container.Container, long)}.
			 */
			@Test
			void testRemoveItem() {
				Item item = mockItem();
				storage.addItem(null, size, item);
				assertSame(item, storage.removeItem(null, size));
				assertEquals(size, storage.getItemCount(null));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#removeItem(de.ims.icarus2.model.api.members.container.Container, long)}.
			 */
			@Test
			void testRemoveItemInvalidIndex() {
				assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
						() -> storage.removeItem(null, 0));
				assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
						() -> storage.removeItem(null, size-1));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#removeItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
			 */
			@Test
			void testRemoveItems() {
				Item item0 = mockItem();
				Item item1 = mockItem();
				storage.addItem(null, size, item0);
				storage.addItem(null, size+1, item1);

				DataSequence<? extends Item> removed = storage.removeItems(null, size, size+1);
				assertNotNull(removed);
				assertEquals(2, removed.entryCount());
				assertSame(item0, removed.elementAt(0));
				assertSame(item1, removed.elementAt(1));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#removeItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
			 */
			@Test
			void testRemoveItemsInvalidIndex() {
				assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
						() -> storage.removeItems(null, 0, 0));
				assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
						() -> storage.removeItems(null, size-1, size-1));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#swapItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
			 */
			@Test
			void testSwapItems() {
				Item item0 = mockItem();
				Item item1 = mockItem();
				storage.addItem(null, size, item0);
				storage.addItem(null, size+1, item1);

				assertSame(item0, storage.getItemAt(null, size));
				assertSame(item1, storage.getItemAt(null, size+1));

				storage.swapItems(null, size, size+1);

				assertSame(item1, storage.getItemAt(null, size));
				assertSame(item0, storage.getItemAt(null, size+1));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#swapItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
			 */
			@Test
			void testSwapItemsInvalidIndex() {
				assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
						() -> storage.swapItems(null, 0, 0));
				assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
						() -> storage.swapItems(null, size-1, size-1));
			}

		}

		@Nested
		class Internals {

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#createAugmentationBuffer(int)}.
			 */
			@ParameterizedTest
			@ValueSource(ints = {-1, 1, 100, 100_000})
			void testCreateAugmentationBuffer(int capacity) {
				LookupList<Item> buffer = storage.createAugmentationBuffer(capacity);
				assertNotNull(buffer);
				assertTrue(buffer.capacity()>=capacity);
			}

			@Nested
			class WithRandomWrappedSize {
				private long size;

				@SuppressWarnings("boxing")
				@BeforeEach
				void setUp() {
					size = random(1, Long.MAX_VALUE/2);
					when(source.getItemCount()).thenReturn(size);
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#getWrappedItemCount(de.ims.icarus2.model.api.members.container.Container)}.
				 */
				@RepeatedTest(RUNS)
				void testGetWrappedItemCount() {
					assertEquals(size, storage.getWrappedItemCount(null));
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#translateAndCheckEditIndex(de.ims.icarus2.model.api.members.container.Container, long)}.
				 */
				@Test
				void testTranslateAndCheckEditIndex() {
					assertEquals(0, storage.translateAndCheckEditIndex(null, size));
					assertEquals(1, storage.translateAndCheckEditIndex(null, size+1));
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#translateAndCheckEditIndex(de.ims.icarus2.model.api.members.container.Container, long)}.
				 */
				@Test
				void testTranslateAndCheckEditIndexOutOfBounds() {
					assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
							() -> storage.translateAndCheckEditIndex(null, 0));
					assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
							() -> storage.translateAndCheckEditIndex(null, size-1));
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.container.AugmentedItemStorage#isWrappedIndex(de.ims.icarus2.model.api.members.container.Container, long)}.
				 */
				@Test
				void testIsWrappedIndex() {
					assertTrue(storage.isWrappedIndex(null, 0L));
					assertTrue(storage.isWrappedIndex(null, size-1));

					assertFalse(storage.isWrappedIndex(null, size));
				}
			}

		}
	}
}
