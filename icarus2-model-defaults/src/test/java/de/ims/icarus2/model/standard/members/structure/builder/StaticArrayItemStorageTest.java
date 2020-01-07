/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.structure.builder;

import static de.ims.icarus2.model.api.ModelTestUtils.mockItems;
import static de.ims.icarus2.test.TestUtils.assertListEquals;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.IcarusUtils.ensureIntegerValueRange;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.standard.members.container.ImmutableItemStorageTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
@RandomizedTest
class StaticArrayItemStorageTest implements ImmutableItemStorageTest<StaticArrayItemStorage> {

	RandomGenerator rng;

	private Item[] items;

	@BeforeEach
	void setUp() {
		items = mockItems(rng.random(10, 20));
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
	public Class<? extends StaticArrayItemStorage> getTestTargetClass() {
		return StaticArrayItemStorage.class;
	}

	@Override
	public StaticArrayItemStorage createTestInstance(TestSettings settings) {
		return settings.process(new StaticArrayItemStorage(list(items)));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticArrayItemStorage#StaticArrayItemStorage(java.util.Collection)}.
	 */
	@SuppressWarnings("boxing")
	@Test
	void testStaticArrayItemStorageCollectionOfItem() {
		StaticArrayItemStorage storage = new StaticArrayItemStorage(list(items));
		assertListEquals(storage,
				s -> ensureIntegerValueRange(s.getItemCount(null)),
				(s, i) -> s.getItemAt(null, i),
				items);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticArrayItemStorage#StaticArrayItemStorage(java.util.Collection)}.
	 */
	@SuppressWarnings("boxing")
	@Test
	void testStaticArrayItemStorageArrayOfItem() {
		StaticArrayItemStorage storage = new StaticArrayItemStorage(items);
		assertListEquals(storage,
				s -> ensureIntegerValueRange(s.getItemCount(null)),
				(s, i) -> s.getItemAt(null, i),
				items);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticArrayItemStorage#getItemCount(de.ims.icarus2.model.api.members.container.Container)}.
	 */
	@Test
	void testGetItemCount() {
		assertEquals(items.length, create().getItemCount(null));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticArrayItemStorage#getItemAt(de.ims.icarus2.model.api.members.container.Container, long)}.
	 */
	@SuppressWarnings("boxing")
	@Test
	void testGetItemAt() {
		assertListEquals(create(),
				s -> ensureIntegerValueRange(s.getItemCount(null)),
				(s, i) -> s.getItemAt(null, i),
				items);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticArrayItemStorage#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	void testIndexOfItem() {
		StaticArrayItemStorage storage = create();
		for(Item item : items) {
			assertTrue(storage.indexOfItem(null, item) > UNSET_LONG);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticArrayItemStorage#getBeginOffset(de.ims.icarus2.model.api.members.container.Container)}.
	 */
	@SuppressWarnings("boxing")
	@Test
	void testGetBeginOffset() {
		long size = items.length;
		for (int i = 0; i < items.length; i++) {
			when(items[i].getBeginOffset()).thenReturn(size-i);
			when(items[i].getEndOffset()).thenReturn(size-i);
		}

		StaticArrayItemStorage storage = new StaticArrayItemStorage(items);

		assertEquals(1L, storage.getBeginOffset(null));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticArrayItemStorage#getEndOffset(de.ims.icarus2.model.api.members.container.Container)}.
	 */
	@SuppressWarnings("boxing")
	@Test
	void testGetEndOffset() {
		long size = items.length;
		for (int i = 0; i < items.length; i++) {
			when(items[i].getBeginOffset()).thenReturn(size-i);
			when(items[i].getEndOffset()).thenReturn(size-i);
		}

		StaticArrayItemStorage storage = new StaticArrayItemStorage(items);

		assertEquals(size, storage.getEndOffset(null));
	}
}
