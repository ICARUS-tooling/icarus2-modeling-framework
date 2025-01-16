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
package de.ims.icarus2.model.standard.members.item;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.test.TestUtils.assertFlagGetter;
import static de.ims.icarus2.test.TestUtils.assertGetter;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.test.TestUtils.assertSetter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.driver.id.IdManager;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.ItemTest;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
class DefaultItemTest implements ItemTest<Item> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends Item> getTestTargetClass() {
		return DefaultItem.class;
	}

	@Nested
	class Constructors {


		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#DefaultItem()}.
		 */
		@Test
		void testDefaultItem() {
			assertNotNull(new DefaultItem());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#DefaultItem(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testDefaultItemContainer() {
			assertNotNull(new DefaultItem(mockContainer()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#DefaultItem(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testDefaultItemContainerNull() {
			assertNPE(() -> new DefaultItem(null));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#DefaultItem(de.ims.icarus2.model.api.members.container.Container, long)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {IcarusUtils.UNSET_LONG, 0, 1, Long.MAX_VALUE})
		void testDefaultItemContainerLong(long id) {
			assertNotNull(new DefaultItem(mockContainer(), id));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#DefaultItem(de.ims.icarus2.model.api.members.container.Container, long)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {IcarusUtils.UNSET_LONG-1})
		void testDefaultItemContainerLongIllegalId(long id) {
			assertThrows(IllegalArgumentException.class, () -> new DefaultItem(mockContainer(), id));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#DefaultItem(long)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {IcarusUtils.UNSET_LONG, 0, 1, Long.MAX_VALUE})
		void testDefaultItemLong(long id) {
			assertNotNull(new DefaultItem(id));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#DefaultItem(long)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {IcarusUtils.UNSET_LONG-1})
		void testDefaultItemLongIllegalId(long id) {
			assertThrows(IllegalArgumentException.class, () -> new DefaultItem(id));
		}
	}

	@Nested
	class WithBareInstance {
		private DefaultItem instance;


		@BeforeEach
		void setUp() {
			instance = new DefaultItem();
		}


		@AfterEach
		void tearDown() {
			instance = null;
		}

		/**
		 * Test method for {@link DefaultItem#getCorpus())}.
		 */
		@Test
		void testGetCorpus() {
			assertModelException(GlobalErrorCode.ILLEGAL_STATE, () -> instance.getCorpus());
		}

		/**
		 * Test method for {@link DefaultItem#isTopLevel())}.
		 */
		@Test
		void testIsTopLevel() {
			assertModelException(GlobalErrorCode.ILLEGAL_STATE, () -> instance.isTopLevel());
		}

		/**
		 * Test method for {@link DefaultItem#getLayer())}.
		 */
		@Test
		void testGetLayer() {
			assertModelException(GlobalErrorCode.ILLEGAL_STATE, () -> instance.getLayer());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#setContainer(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testSetContainerNull() {
			assertNPE(() -> instance.setContainer(null));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#getContainer()}.
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#setContainer(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testSetContainer() {
			assertSetter(instance,
					DefaultItem::setContainer,
					mockContainer(), NPE_CHECK, NO_CHECK);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#getContainer()}.
		 */
		@Test
		void testGetContainer() {
			assertGetter(instance,
					mockContainer(), mockContainer(),
					NO_DEFAULT(),
					DefaultItem::getContainer,
					DefaultItem::setContainer);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#recycle()}.
		 */
		@Test
		void testRecycle() {
			instance.setContainer(mockContainer());
			instance.setAlive(true);
			instance.setDirty(true);
			instance.setLocked(true);
			instance.setId(1);

			instance.recycle();

			assertNull(instance.getContainer());
			assertEquals(IcarusUtils.UNSET_LONG, instance.getId());

			assertFalse(instance.isAlive());
			assertFalse(instance.isDirty());
			assertFalse(instance.isLocked());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#getId()}.
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#setId(long)}.
		 */
		@Test
		void testGetAndSetId() {
			assertEquals(IcarusUtils.UNSET_LONG, instance.getId());

			instance.setId(1L);
			assertEquals(1L, instance.getId());

			assertThrows(IllegalStateException.class, () -> instance.setId(2L));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#setId(long)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {Long.MIN_VALUE, IcarusUtils.UNSET_LONG-1})
		void testSetIdIllegalValues(long id) {
			assertThrows(IllegalArgumentException.class, () -> instance.setId(id));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#toString()}.
		 */
		@Test
		void testToString() {
			assertNotNull(instance.toString());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#getMemberType()}.
		 */
		@Test
		void testGetMemberType() {
			assertEquals(MemberType.ITEM, instance.getMemberType());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#isAlive()}.
		 */
		@Test
		void testIsAlive() {
			assertFlagGetter(instance, Boolean.valueOf(Item.DEFAULT_ALIVE),
					DefaultItem::isAlive, DefaultItem::setAlive);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#isLocked()}.
		 */
		@Test
		void testIsLocked() {
			assertFlagGetter(instance, Boolean.valueOf(Item.DEFAULT_LOCKED),
					DefaultItem::isLocked, DefaultItem::setLocked);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#isDirty()}.
		 */
		@Test
		void testIsDirty() {
			assertFlagGetter(instance, Boolean.valueOf(Item.DEFAULT_DIRTY),
					DefaultItem::isDirty, DefaultItem::setDirty);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#isUsable()}.
		 */
		@Test
		void testIsUsable() {
			assertFlagGetter(instance, Boolean.FALSE, DefaultItem::isUsable, DefaultItem::setAlive);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#setAlive(boolean)}.
		 */
		@Test
		void testSetAlive() {
			assertSetter(instance, DefaultItem::setAlive);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#setLocked(boolean)}.
		 */
		@Test
		void testSetLocked() {
			assertSetter(instance, DefaultItem::setLocked);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#setDirty(boolean)}.
		 */
		@Test
		void testSetDirty() {
			assertSetter(instance, DefaultItem::setDirty);
		}
	}

	/**
	 * Needs a working container, layer and {@link IdManager}
	 * @author Markus Gärtner
	 *
	 */
	@Nested
	@RandomizedTest
	class WithComplexEnvironment {
		private IdManager idManager;
		private ItemLayer layer;
		private Container container;

		private long id, index;

		private DefaultItem instance;

		@BeforeEach
		void setUp(RandomGenerator rng) {
			idManager = mock(IdManager.class);
			layer = mock(ItemLayer.class);
			container = mockContainer();

			when(container.getLayer()).thenReturn(layer);
			when(layer.getIdManager()).thenReturn(idManager);

			id = Math.max(1L, rng.nextLong());
			index = Math.max(1L, rng.nextLong());

			instance = new DefaultItem();
			instance.setContainer(container);
			instance.setId(id);
		}

		@AfterEach
		void tearDown() {
			idManager = null;
			layer = null;
			container = null;
			id = IcarusUtils.UNSET_LONG;
			index = IcarusUtils.UNSET_LONG;
			instance = null;
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#getIndex()}.
		 */
		@SuppressWarnings("boxing")
		@Test
		void testGetIndex() {
			when(idManager.indexOfId(id)).thenReturn(1L,  2L, 3L, index);

			assertEquals(1L, instance.getIndex());
			assertEquals(2L, instance.getIndex());
			assertEquals(3L, instance.getIndex());
			assertEquals(index, instance.getIndex());

			verify(idManager, times(4)).indexOfId(id);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultItem#revive()}.
		 */
		@SuppressWarnings("boxing")
		@RepeatedTest(value=RUNS)
		void testRevive() {
			when(idManager.indexOfId(anyLong())).thenReturn(IcarusUtils.UNSET_LONG);

			assertFalse(instance.revive());

			when(idManager.indexOfId(id)).thenReturn(index);

			assertTrue(instance.revive());
		}

		/**
		 * Test method for {@link DefaultItem#getCorpus())}.
		 */
		@Test
		void testGetCorpus() {
			Corpus corpus = mock(Corpus.class);

			when(container.getCorpus()).thenReturn(corpus);
			assertSame(corpus, instance.getCorpus());

			verify(container).getCorpus();
		}

		/**
		 * Test method for {@link DefaultItem#isTopLevel())}.
		 */
		@SuppressWarnings("boxing")
		@Test
		void testIsTopLevel() {
			when(container.isProxy()).thenReturn(Boolean.TRUE);

			assertTrue(instance.isTopLevel());

			verify(container).isProxy();
		}

		/**
		 * Test method for {@link DefaultItem#getLayer())}.
		 */
		@Test
		void testGetLayer() {
			assertSame(layer, instance.getLayer());
		}

	}

}
