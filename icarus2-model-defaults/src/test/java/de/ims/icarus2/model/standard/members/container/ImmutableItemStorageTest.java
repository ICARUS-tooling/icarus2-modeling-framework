/**
 *
 */
package de.ims.icarus2.model.standard.members.container;

import static de.ims.icarus2.SharedTestUtils.mockSequence;
import static de.ims.icarus2.model.api.ModelTestUtils.assertUnsupportedOperation;
import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
public interface ImmutableItemStorageTest<S extends ItemStorage>
		extends ItemStorageTest<S> {

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.container.ItemStorage#addItem(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testAddItem() {
		assertUnsupportedOperation(() -> create().addItem(mockContainer(), 0L, mockItem()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.container.ItemStorage#addItems(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.util.collections.seq.DataSequence)}.
	 */
	@Test
	default void testAddItems() {
		assertUnsupportedOperation(() -> create().addItems(
				mockContainer(), 0L, mockSequence(mockItem())));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.container.ItemStorage#removeItem(de.ims.icarus2.model.api.members.container.Container, long)}.
	 */
	@Test
	default void testRemoveItem() {
		assertUnsupportedOperation(() -> create().removeItem(mockContainer(), 0L));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.container.ItemStorage#removeItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
	 */
	@Test
	default void testRemoveItems() {
		assertUnsupportedOperation(() -> create().removeItems(mockContainer(), 0L, 1L));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.container.ItemStorage#swapItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
	 */
	@Test
	default void testSwapItems() {
		assertUnsupportedOperation(() -> create().swapItems(mockContainer(), 0L, 1L));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.Recyclable#recycle()}.
	 */
	@Override
	@Test
	default void testRecycle() {
		assertUnsupportedOperation(() -> create().recycle());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.Recyclable#revive()}.
	 */
	@Test
	default void testRevive() {
		assertFalse(create().revive());
	}

}
