/**
 *
 */
package de.ims.icarus2.model.api.members.container;

import static de.ims.icarus2.SharedTestUtils.mockSequence;
import static de.ims.icarus2.model.api.ModelTestUtils.assertUnsupportedOperation;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;

import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
public interface ImmutableContainerTest<C extends Container> extends ContainerTest<C> {

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#removeItem(long)}.
	 */
	@Test
	default void testRemoveItemLong() {
		assertUnsupportedOperation(() -> create().removeItem(0L));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#removeItem(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testRemoveItemItem() {
		assertUnsupportedOperation(() -> create().removeItem(mockItem()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#addItem(long, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testAddItemLongItem() {
		assertUnsupportedOperation(() -> create().addItem(0L, mockItem()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#addItem(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testAddItemItem() {
		assertUnsupportedOperation(() -> create().addItem(mockItem()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#swapItems(long, long)}.
	 */
	@Test
	default void testSwapItems() {
		assertUnsupportedOperation(() -> create().swapItems(0L,  1L));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#addItems(long, de.ims.icarus2.util.collections.seq.DataSequence)}.
	 */
	@Test
	default void testAddItems() {
		assertUnsupportedOperation(() -> create().addItems(0L, mockSequence(mockItem())));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#removeAllItems()}.
	 */
	@Test
	default void testRemoveAllItems() {
		assertUnsupportedOperation(() -> create().removeAllItems());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#removeItems(long, long)}.
	 */
	@Test
	default void testRemoveItems() {
		assertUnsupportedOperation(() -> create().removeItems(0L, 1L));
	}

}
