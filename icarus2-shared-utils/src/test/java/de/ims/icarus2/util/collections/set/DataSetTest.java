/**
 *
 */
package de.ims.icarus2.util.collections.set;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.guard.ApiGuard;

/**
 * @author Markus Gärtner
 *
 */
@Disabled
public interface DataSetTest<S extends DataSet<?>> extends ApiGuardedTest<S> {

	/**
	 * @see de.ims.icarus2.test.ApiGuardedTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
	 */
	@Override
	default void configureApiGuard(ApiGuard<S> apiGuard) {
		ApiGuardedTest.super.configureApiGuard(apiGuard);
		apiGuard.nullGuard(true);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#entryCount()}.
	 */
	@Test
	default void testEntryCount() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#isEmpty()}.
	 */
	@Test
	default void testIsEmpty() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#entryAt(int)}.
	 */
	@Test
	default void testEntryAt() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#contains(java.lang.Object)}.
	 */
	@Test
	default void testContains() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#forEachEntry(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachEntry() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#toSet()}.
	 */
	@Test
	default void testToSet() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#toList()}.
	 */
	@Test
	default void testToList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#toArray()}.
	 */
	@Test
	default void testToArray() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#toArray(T[])}.
	 */
	@Test
	default void testToArrayTArray() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#iterator()}.
	 */
	@Test
	default void testIterator() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.DataSet#emptySet()}.
	 */
	@Test
	default void testEmptySet() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Iterable#iterator()}.
	 */
	@Test
	default void testIterator1() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Iterable#forEach(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEach() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Iterable#spliterator()}.
	 */
	@Test
	default void testSpliterator() {
		fail("Not yet implemented"); // TODO
	}

}