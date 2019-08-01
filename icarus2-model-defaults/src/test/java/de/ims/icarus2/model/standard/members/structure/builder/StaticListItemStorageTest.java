/**
 *
 */
package de.ims.icarus2.model.standard.members.structure.builder;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItems;
import static de.ims.icarus2.test.TestUtils.assertListEquals;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.standard.members.container.ImmutableItemStorageTest;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class StaticListItemStorageTest implements ImmutableItemStorageTest<StaticListItemStorage> {

	private Item[] items;
	private Item beginItem, endItem;

	@BeforeEach
	void setUp() {
		items = mockItems(random(10, 20));
		beginItem = items[0];
		endItem = items[items.length-1];
	}

	@AfterEach
	void tearDown() {
		items = null;
	}

	@Override
	public ContainerType getExpectedContainerType() {
		return ContainerType.LIST;
	}

	@Override
	public Class<? extends StaticListItemStorage> getTestTargetClass() {
		return StaticListItemStorage.class;
	}

	@Override
	public StaticListItemStorage createTestInstance(TestSettings settings) {
		return settings.process(new StaticListItemStorage(list(items), beginItem, endItem));
	}

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticListItemStorage#StaticListItemStorage(java.util.Collection, de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@SuppressWarnings("boxing")
		@Test
		void testStaticListItemStorage() {
			StaticListItemStorage storage = new StaticListItemStorage(list(items), beginItem, endItem);

			assertListEquals(storage,
					s -> (int)s.getItemCount(null),
					(s, i) -> s.getItemAt(null, i),
					items);

			storage.getBeginOffset(null);
			verify(beginItem).getBeginOffset();

			storage.getEndOffset(null);
			verify(endItem).getEndOffset();
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticListItemStorage#StaticListItemStorage(java.util.Collection, de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		void testStaticListItemStorageUnknownBeginItem() {
			assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					() -> new StaticListItemStorage(list(items), mockItem(), endItem));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticListItemStorage#StaticListItemStorage(java.util.Collection, de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		void testStaticListItemStorageUnknownEndItem() {
			assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					() -> new StaticListItemStorage(list(items), beginItem, mockItem()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticListItemStorage#StaticListItemStorage(java.util.Collection, de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		void testStaticListItemStorageNoBeginItem() {
			StaticListItemStorage storage =
					new StaticListItemStorage(list(items), null, endItem);

			assertEquals(UNSET_LONG, storage.getBeginOffset(null));
			verify(beginItem, never()).getBeginOffset();
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticListItemStorage#StaticListItemStorage(java.util.Collection, de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		void testStaticListItemStorageNoEndItem() {
			StaticListItemStorage storage =
					new StaticListItemStorage(list(items), beginItem, null);

			assertEquals(UNSET_LONG, storage.getEndOffset(null));
			verify(endItem, never()).getEndOffset();
		}

	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticListItemStorage#getItemCount(de.ims.icarus2.model.api.members.container.Container)}.
	 */
	@Test
	void testGetItemCount() {
		assertEquals(items.length, create().getItemCount(null));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticListItemStorage#getItemAt(de.ims.icarus2.model.api.members.container.Container, long)}.
	 */
	@SuppressWarnings("boxing")
	@Test
	void testGetItemAt() {
		assertListEquals(create(),
				s -> (int)s.getItemCount(null),
				(s, i) -> s.getItemAt(null, i),
				items);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticListItemStorage#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	void testIndexOfItem() {
		StaticListItemStorage storage = create();
		for (int i = 0; i < items.length; i++) {
			assertEquals(i, storage.indexOfItem(null, items[i]));
		}

		assertEquals(UNSET_LONG, storage.indexOfItem(null, mockItem()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticListItemStorage#getBeginOffset(de.ims.icarus2.model.api.members.container.Container)}.
	 */
	@SuppressWarnings("boxing")
	@Test
	void testGetBeginOffset() {
		when(beginItem.getBeginOffset()).thenReturn(123L);
		assertEquals(123, create().getBeginOffset(null));
		verify(beginItem).getBeginOffset();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticListItemStorage#getEndOffset(de.ims.icarus2.model.api.members.container.Container)}.
	 */
	@SuppressWarnings("boxing")
	@Test
	void testGetEndOffset() {
		when(endItem.getEndOffset()).thenReturn(321L);
		assertEquals(321, create().getEndOffset(null));
		verify(endItem).getEndOffset();
	}
}
