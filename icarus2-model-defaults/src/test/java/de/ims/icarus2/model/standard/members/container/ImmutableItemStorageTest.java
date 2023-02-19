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
