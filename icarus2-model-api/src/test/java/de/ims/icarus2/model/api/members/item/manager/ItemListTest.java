/**
 *
 */
package de.ims.icarus2.model.api.members.item.manager;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.api.members.item.manager.ItemLookupTest.makeItems;
import static de.ims.icarus2.test.TestUtils.assertIOOB;
import static de.ims.icarus2.test.TestUtils.random;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.members.item.Item;

/**
 * @author Markus Gärtner
 *
 */
public interface ItemListTest<L extends ItemList> extends ItemLookupTest<L> {

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#removeItem(long)}.
	 */
	@Test
	default void testRemoveItemLongEmpty() {
		assertIOOB(() -> create().removeItem(0L));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#removeItem(long)}.
	 */
	@Test
	default void testRemoveItemLong() {
		Item[] items = makeItems();
		L list = createFilled(items);
		for (int i = 0; i < items.length; i++) {
			assertSame(items[i], list.removeItem(0L));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#removeItem(long)}.
	 */
	@Test
	default void testRemoveItemLongInvalidIndex() {
		Item[] items = makeItems();
		L list = createFilled(items);

		assertIOOB(() -> list.removeItem(-1L));
		assertIOOB(() -> list.removeItem(items.length));
		assertIOOB(() -> list.removeItem(items.length+1));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#removeItem(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testRemoveItemItem() {
		Item[] items = makeItems();
		L list = createFilled(items);

		assertModelException(GlobalErrorCode.INVALID_INPUT, () -> list.removeItem(mockItem()));

		for(Item item : items) {
			list.removeItem(item);
		}
		assertTrue(list.isEmpty());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#addItem(long, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testAddItemLongItem() {
		Item[] items = makeItems();
		L list = create();

		for(Item item : items) {
			list.addItem(item);
		}

		assertEquals(items.length, list.getItemCount());
		for (int i = 0; i < items.length; i++) {
			assertSame(items[i], list.getItemAt(i));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#addItem(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testAddItemItem() {
		Item[] items = makeItems();
		L list = create();

		for (int i = 0; i < items.length; i++) {
			list.addItem(i, items[i]);
		}

		assertEquals(items.length, list.getItemCount());
		for (int i = 0; i < items.length; i++) {
			assertSame(items[i], list.getItemAt(i));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#addItem(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testAddItemItemRandom() {
		Item[] items = makeItems();
		L list = create();
		List<Item> tmp = new ArrayList<>();

		for (Item item : items) {
			int index = random(0, tmp.size()+1);
			list.addItem(index, item);
			tmp.add(index, item);
		}

		assertEquals(tmp.size(), list.getItemCount());
		for (int i = 0; i < tmp.size(); i++) {
			assertSame(tmp.get(i), list.getItemAt(i));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#swapItems(long, long)}.
	 */
	@Test
	default void testSwapItems() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#addItems(long, de.ims.icarus2.util.collections.seq.DataSequence)}.
	 */
	@Test
	default void testAddItems() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#removeAllItems()}.
	 */
	@Test
	default void testRemoveAllItems() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#removeItems(long, long)}.
	 */
	@Test
	default void testRemoveItems() {
		fail("Not yet implemented"); // TODO
	}

}
