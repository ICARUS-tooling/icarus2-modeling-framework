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
package de.ims.icarus2.model.api.members.item.manager;

import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItems;
import static de.ims.icarus2.test.TestUtils.assertIOOB;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ObjLongConsumer;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.test.ApiGuardedTest;

/**
 * @author Markus Gärtner
 *
 */
public interface ItemLookupTest<L extends ItemLookup> extends ApiGuardedTest<L> {

	L createFilled(Item...items);

	static Item[] makeItems() {
		return mockItems(123);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemLookup#getItemCount()}.
	 */
	@Test
	default void testGetItemCountEmpty() {
		assertEquals(0L, create().getItemCount());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemLookup#getItemCount()}.
	 */
	@Test
	default void testGetItemCount() {
		Item[] items = makeItems();
		L lookup = createFilled(items);
		assertEquals(items.length, lookup.getItemCount());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemLookup#isEmpty()}.
	 */
	@Test
	default void testIsEmpty() {
		assertTrue(create().isEmpty());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemLookup#isEmpty()}.
	 */
	@Test
	default void testIsEmptyFilled() {
		Item[] items = makeItems();
		L lookup = createFilled(items);
		assertFalse(lookup.isEmpty());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemLookup#getItemAt(long)}.
	 */
	@Test
	default void testGetItemAt() {
		Item[] items = makeItems();
		L lookup = createFilled(items);
		for (int i = 0; i < items.length; i++) {
			assertSame(items[i], lookup.getItemAt(i));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemLookup#getItemAt(long)}.
	 */
	@Test
	default void testGetItemAtInvalidIndex() {
		assertIOOB(() -> create().getItemAt(0L));

		Item[] items = makeItems();
		L lookup = createFilled(items);

		assertIOOB(() -> lookup.getItemAt(-1L));
		assertIOOB(() -> lookup.getItemAt(items.length));
		assertIOOB(() -> lookup.getItemAt(items.length+1));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemLookup#indexOfItem(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testIndexOfItem() {
		Item[] items = makeItems();
		L lookup = createFilled(items);
		for (int i = 0; i < items.length; i++) {
			assertEquals(i, lookup.indexOfItem(items[i]));
		}

		assertEquals(UNSET_LONG, lookup.indexOfItem(mockItem()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemLookup#forEachItem(java.util.function.ObjLongConsumer)}.
	 */
	@Test
	default void testForEachItemObjLongConsumerOfQsuperItem() {
		Item[] items = makeItems();
		L lookup = createFilled(items);

		Item[] tmp = new Item[items.length];
		ObjLongConsumer<Item> action = (item, index) -> tmp[strictToInt(index)] = item;
		lookup.forEachItem(action);

		assertArrayEquals(items, tmp);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemLookup#forEachItem(java.util.function.ObjLongConsumer)}.
	 */
	@Test
	default void testForEachItemObjLongConsumerOfQsuperItemEmpty() {
		ObjLongConsumer<Item> action = mock(ObjLongConsumer.class);
		create().forEachItem(action);
		verify(action, never()).accept(any(), anyLong());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemLookup#forEachItem(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachItemConsumerOfQsuperItem() {
		Item[] items = makeItems();
		L lookup = createFilled(items);

		List<Item> tmp = new ArrayList<>();
		Consumer<Item> action = tmp::add;
		lookup.forEachItem(action);

		assertThat(items).containsExactlyElementsOf(tmp);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemLookup#forEachItem(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachItemConsumerOfQsuperItemEmpty() {
		Consumer<Item> action = mock(Consumer.class);
		create().forEachItem(action);
		verify(action, never()).accept(any());
	}

}
