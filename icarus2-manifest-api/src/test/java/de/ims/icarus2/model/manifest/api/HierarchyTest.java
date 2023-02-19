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
package de.ims.icarus2.model.manifest.api;

import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_ILLEGAL;
import static de.ims.icarus2.test.TestUtils.assertGetter;
import static de.ims.icarus2.test.TestUtils.assertListAtIndex;
import static de.ims.icarus2.test.TestUtils.assertListIndexOf;
import static de.ims.icarus2.test.TestUtils.assertPredicate;
import static de.ims.icarus2.test.TestUtils.constant;
import static de.ims.icarus2.test.TestUtils.inject_genericInserter;
import static de.ims.icarus2.test.TestUtils.settings;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
@SuppressWarnings("rawtypes")
public interface HierarchyTest<E extends Object, H extends Hierarchy> extends LockableTest<H> {

	@Provider
	E mockItem();

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Hierarchy#getRoot()}.
	 */
	@SuppressWarnings({ "boxing", "unchecked" })
	@Test
	default void testGetRoot() {
		E root = mockItem();
		E item1 = mockItem();
		E item2 = mockItem();

		// Test basic behavior when constantly changing the root manifest itself
		assertGetter(createUnlocked(),
				item1, item2, null,
				Hierarchy::getRoot,
				inject_genericInserter(Hierarchy::insert, constant(0)));

		Predicate<H> rootCheck = h -> {
			E item = (E) h.getRoot();
			assertNotNull(item);
			return item==root;
		};

		BiFunction<H, E, Boolean> staticModifier = (h, item) -> {
			h.add(item);
			return true;
		};

		// Test with simply adding more containers after initial root
		assertPredicate(createUnlocked(), staticModifier, rootCheck, Object::toString,
				root, item1, item2);

		BiFunction<H, E, Boolean> mixedModifier = (h, item) -> {
			h.insert(item, 0);
			return item==root;
		};

		// Test with shifting the root manifest
		assertPredicate(createUnlocked(), mixedModifier, rootCheck, Object::toString,
				root, item1, item2);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Hierarchy#getDepth()}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	default void testGetDepth() {
		TestUtils.assertAccumulativeCount(createUnlocked(),
				Hierarchy::add,
				Hierarchy::remove,
				Hierarchy::getDepth,
				mockItem(), mockItem());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Hierarchy#atLevel(int)}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	@RandomizedTest
	default void testAtLevel(RandomGenerator rand) {
		assertListAtIndex(createUnlocked(),
				Hierarchy::add,
				Hierarchy::remove,
				Hierarchy::atLevel,
				rand,
				mockItem(), mockItem(), mockItem(), mockItem());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Hierarchy#add(java.lang.Object)}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	default void testAdd() {
		assertLockableAccumulativeAdd(settings(),
				Hierarchy::add,
				NO_ILLEGAL(), NO_CHECK, true, ManifestTestUtils.DUPLICATE_ID_CHECK,
				mockItem(), mockItem(), mockItem(), mockItem());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Hierarchy#remove(java.lang.Object)}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	default void testRemove() {
		assertLockableAccumulativeRemove(settings(),
				Hierarchy::add,
				Hierarchy::remove,
				Hierarchy::getItems,
				true, ManifestTestUtils.UNKNOWN_ID_CHECK,
				mockItem(), mockItem(), mockItem(), mockItem());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Hierarchy#insert(java.lang.Object, int)}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	default void testInsert() {
		assertLockableListInsertAt(settings(),
				Hierarchy::insert,
				Hierarchy::atLevel,
				mockItem(), mockItem(), mockItem(), mockItem());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Hierarchy#levelOf(java.lang.Object)}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	@RandomizedTest
	default void testLevelOf(RandomGenerator rand) {
		assertListIndexOf(createUnlocked(),
				Hierarchy::add,
				Hierarchy::remove,
				Hierarchy::levelOf,
				rand,
				mockItem(), mockItem(), mockItem(), mockItem());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Hierarchy#forEachItem(java.util.function.ObjIntConsumer)}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	default void testForEachItem() {
		H hierarchy = createUnlocked();
		E item1 = mockItem();
		E item2 = mockItem();

		hierarchy.add(item1);
		hierarchy.add(item2);

		hierarchy.forEachItem((item, level) -> {
			if(item==item1) {
				assertThat(level).isEqualTo(0);
			} else if(item==item2) {
				assertThat(level).isEqualTo(1);
			} else {
				fail("unknown element");
			}
		});
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Hierarchy#getItems()}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	default void testGetItems() {
		TestUtils.assertAccumulativeGetter(createUnlocked(),
				mockItem(),
				mockItem(),
				Hierarchy::getItems,
				Hierarchy::add);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Hierarchy#isEmpty()}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	default void testIsEmpty() {
		H hierarchy = createUnlocked();

		assertTrue(hierarchy.isEmpty());

		hierarchy.add(mockItem());
		assertFalse(hierarchy.isEmpty());

		hierarchy.remove(hierarchy.getRoot());
		assertTrue(hierarchy.isEmpty());
	}
}
