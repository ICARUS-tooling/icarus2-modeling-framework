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
import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
class SingletonItemStorageTest implements ItemStorageTest<SingletonItemStorage>{

	@Override
	public Class<? extends SingletonItemStorage> getTestTargetClass() {
		return SingletonItemStorage.class;
	}

	@Override
	public SingletonItemStorage createTestInstance(TestSettings settings) {
		return settings.process(new SingletonItemStorage());
	}

	@Override
	public ContainerType getExpectedContainerType() {
		return ContainerType.SINGLETON;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#revive()}.
	 */
	@Test
	void testRevive() {
		SingletonItemStorage storage = create();

		storage.recycle();
		assertTrue(storage.revive());
	}

	@Nested
	class WhenEmpty {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#getItemCount(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testGetItemCount() {
			assertEquals(0, create().getItemCount(mockContainer()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#isEmpty()}.
		 */
		@Test
		void testIsEmpty() {
			assertTrue(create().isEmpty());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#getItemAt(de.ims.icarus2.model.api.members.container.Container, long)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {0, -1, Long.MAX_VALUE, Long.MIN_VALUE})
		void testGetItemAt(long index) {
			assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					() -> create().getItemAt(mockContainer(), index));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		void testIndexOfItem() {
			assertEquals(UNSET_LONG, create().indexOfItem(mockContainer(), mockItem()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#addItem(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		void testAddItem() {
			create().addItem(mockContainer(), 0, mockItem());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#addItem(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {1, -1})
		void testAddItemInvalidIndex(long index) {
			assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					() -> create().addItem(mockContainer(), index, mockItem()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#addItems(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.util.collections.seq.DataSequence)}.
		 */
		@Test
		void testAddItems() {
			create().addItems(mockContainer(), 0, mockSequence(mockItem()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#addItems(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.util.collections.seq.DataSequence)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {1, -1})
		void testAddItemsInvalidIndex(long index) {
			assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					() -> create().addItems(mockContainer(), index, mockSequence(mockItem())));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#addItems(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.util.collections.seq.DataSequence)}.
		 */
		@Test
		void testAddItemsTooManyItems() {
			assertModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
					() -> create().addItems(mockContainer(), 0, mockSequence(mockItem(), mockItem())));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#removeItem(de.ims.icarus2.model.api.members.container.Container, long)}.
		 */
		@Test
		void testRemoveItem() {
			assertModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
					() -> create().removeItem(mockContainer(), 0));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#removeItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
		 */
		@Test
		void testRemoveItems() {
			assertModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
					() -> create().removeItems(mockContainer(), 0, 0));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#swapItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
		 */
		@Test
		void testSwapItems() {
			assertModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
					() -> create().swapItems(mockContainer(), 0, 0));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#getBeginOffset(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testGetBeginOffset() {
			assertEquals(UNSET_LONG, create().getBeginOffset(mockContainer()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#getEndOffset(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testGetEndOffset() {
			assertEquals(UNSET_LONG, create().getEndOffset(mockContainer()));
		}

	}

	@Nested
	class WhenFilled {
		SingletonItemStorage instance;
		Container context;
		Item item;

		@BeforeEach
		void setUp() {
			item = mockItem();
			context = mockContainer();
			instance = create();
			instance.addItem(context, 0, item);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#getItemCount(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testGetItemCount() {
			assertEquals(1, instance.getItemCount(context));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#isEmpty()}.
		 */
		@Test
		void testIsEmpty() {
			assertFalse(instance.isEmpty());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#getItemAt(de.ims.icarus2.model.api.members.container.Container, long)}.
		 */
		@Test
		void testGetItemAt() {
			assertSame(item, instance.getItemAt(context, 0));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#getItemAt(de.ims.icarus2.model.api.members.container.Container, long)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {-1, 1})
		void testGetItemAtInvalidIndex(long index) {
			assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					() -> create().getItemAt(context, index));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		void testIndexOfItem() {
			assertEquals(0, instance.indexOfItem(context, item));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		void testIndexOfItemForeign() {
			assertEquals(UNSET_LONG, instance.indexOfItem(context, mockItem()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#addItem(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		void testAddItem() {
			assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					() -> instance.addItem(context, 0, mockItem()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#addItem(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {-1, 1})
		void testAddItemInvalidIndex(long index) {
			assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					() -> instance.addItem(context, index, mockItem()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#addItems(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.util.collections.seq.DataSequence)}.
		 */
		@Test
		void testAddItems() {
			assertModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
					() -> instance.addItems(context, 0, mockSequence(mockItem())));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#removeItem(de.ims.icarus2.model.api.members.container.Container, long)}.
		 */
		@Test
		void testRemoveItem() {
			assertSame(item, instance.removeItem(context, 0));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#removeItem(de.ims.icarus2.model.api.members.container.Container, long)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {-1, 1})
		void testRemoveItemInvalidIndex(long index) {
			assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					() -> instance.removeItem(context, index));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#removeItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
		 */
		@Test
		void testRemoveItems() {
			DataSequence<? extends Item> result = instance.removeItems(context, 0, 0);
			assertNotNull(result);
			assertEquals(1, result.entryCount());
			assertSame(item, result.elementAt(0));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#removeItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {-1, 1})
		void testRemoveItemsInvalidIndex0(long index) {
			assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					() -> instance.removeItems(context, index, 0));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#removeItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {-1, 1})
		void testRemoveItemsInvalidIndex1(long index) {
			assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					() -> instance.removeItems(context, 0, index));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#removeItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
		 */
		@Test
		void testRemoveItemsTooBigSpan() {
			assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					() -> instance.removeItems(context, 0, 1));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#swapItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
		 */
		@Test
		void testSwapItems() {
			assertModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
					() -> instance.swapItems(mockContainer(), 0, 0));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#swapItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {-1, 1})
		void testSwapItemsInvalidIndex1(long index) {
			assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					() -> instance.removeItems(context, 0, index));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#getBeginOffset(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@SuppressWarnings("boxing")
		@Test
		@RandomizedTest
		void testGetBeginOffset(RandomGenerator rng) {
			long index = rng.randomId();
			when(item.getBeginOffset()).thenReturn(index);
			assertEquals(index, instance.getBeginOffset(context));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SingletonItemStorage#getEndOffset(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@SuppressWarnings("boxing")
		@Test
		@RandomizedTest
		void testGetEndOffset(RandomGenerator rng) {
			long index = rng.randomId();
			when(item.getEndOffset()).thenReturn(index);
			assertEquals(index, instance.getEndOffset(context));
		}
	}

}
