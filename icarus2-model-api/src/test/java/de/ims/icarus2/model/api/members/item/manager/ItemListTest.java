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
package de.ims.icarus2.model.api.members.item.manager;

import static de.ims.icarus2.SharedTestUtils.mockSequence;
import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.api.members.item.manager.ItemLookupTest.makeItems;
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.test.TestUtils.assertIOOB;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.collections.seq.DataSequence;

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
	@RandomizedTest
	default void testAddItemItemRandom(RandomGenerator rand) {
		Item[] items = makeItems();
		L list = create();
		List<Item> tmp = new ArrayList<>();

		for (Item item : items) {
			int index = rand.random(0, tmp.size()+1);
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
	@RandomizedTest
	default void testSwapItems(RandomGenerator rand) {
		Item[] items = makeItems();
		L list = createFilled(items);
		Item[] tmp = items.clone();

		for (int i = 0; i < items.length; i++) {
			int idx = rand.random(0, items.length);
			if(i!=idx) {
				list.swapItems(i, idx);

				Item item = tmp[i];
				tmp[i] = tmp[idx];
				tmp[idx] = item;
			}
		}

		assertEquals(tmp.length, list.getItemCount());
		for (int i = 0; i < tmp.length; i++) {
			assertSame(tmp[i], list.getItemAt(i));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#addItems(long, de.ims.icarus2.util.collections.seq.DataSequence)}.
	 */
	@Test
	@RandomizedTest
	default void testAddItems() {
		Item[] items = makeItems();
		L list = create();

		list.addItems(0, mockSequence(items));

		assertEquals(items.length, list.getItemCount());
		for (int i = 0; i < items.length; i++) {
			assertSame(items[i], list.getItemAt(i));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#addItems(long, de.ims.icarus2.util.collections.seq.DataSequence)}.
	 */
	@RepeatedTest(RUNS)
	@RandomizedTest
	default void testAddItemsRandom(RandomGenerator rand) {
		Item[] items = makeItems();
		L list = createFilled(items);
		List<Item> tmp = CollectionUtils.list(items);

		Item[] add = makeItems();
		int index = rand.random(0, items.length+1);
		list.addItems(index, mockSequence(add));
		tmp.addAll(index, CollectionUtils.list(add));

		assertEquals(tmp.size(), list.getItemCount());
		for (int i = 0; i < tmp.size(); i++) {
			assertSame(tmp.get(i), list.getItemAt(i));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#removeAllItems()}.
	 */
	@Test
	default void testRemoveAllItems() {
		Item[] items = makeItems();
		L list = createFilled(items);

		DataSequence<? extends Item> removed = list.removeAllItems();
		assertTrue(list.isEmpty());

		assertEquals(items.length, removed.entryCount());
		for (int i = 0; i < items.length; i++) {
			assertSame(items[i], removed.elementAt(i));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.item.manager.ItemList#removeItems(long, long)}.
	 */
	@RepeatedTest(RUNS)
	@RandomizedTest
	default void testRemoveItems(RandomGenerator rand) {
		Item[] items = makeItems();
		L list = createFilled(items);
		List<Item> tmp = CollectionUtils.list(items);

		int from = rand.random(0, items.length);
		int to = rand.random(from, items.length);

		DataSequence<? extends Item> removed = list.removeItems(from, to);
		List<Item> subList = tmp.subList(from, to+1);

		assertEquals(subList.size(), removed.entryCount());
		for (int i = 0; i < subList.size(); i++) {
			assertSame(subList.get(i), removed.elementAt(i));
		}

		subList.clear();

		assertEquals(tmp.size(), list.getItemCount());
		for (int i = 0; i < tmp.size(); i++) {
			assertSame(tmp.get(i), list.getItemAt(i));
		}
	}

}
