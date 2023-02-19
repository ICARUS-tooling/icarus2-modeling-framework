/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
