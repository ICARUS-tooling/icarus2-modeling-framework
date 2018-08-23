/**
 *
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.TestUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface HierarchyTest<E extends Object, H extends Hierarchy<E>> extends LockableTest<H> {

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
			E item = h.getRoot();
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
	default void testAtLevel() {
		assertListAtIndex(createUnlocked(),
				Hierarchy::add,
				Hierarchy::remove,
				Hierarchy::atLevel,
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
				NO_ILLEGAL(), NO_CHECK, true, DUPLICATE_ID_CHECK,
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
				true, UNKNOWN_ID_CHECK,
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
	default void testLevelOf() {
		assertListIndexOf(createUnlocked(),
				Hierarchy::add,
				Hierarchy::remove,
				Hierarchy::levelOf,
				mockItem(), mockItem(), mockItem(), mockItem());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Hierarchy#forEachItem(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachItem() {
		TestUtils.assertForEach(createUnlocked(),
				mockItem(),
				mockItem(),
				(Function<H, Consumer<Consumer<? super E>>>)m -> m::forEachItem,
				Hierarchy::add);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Hierarchy#getItems()}.
	 */
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
