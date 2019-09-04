/**
 *
 */
package de.ims.icarus2.util.collections.set;

import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
class CachedSetTest implements DataSetTest<CachedSet<Object>> {

	@Override
	public Class<?> getTestTargetClass() {
		return CachedSet.class;
	}

	@Override
	public CachedSet<Object> createEmpty() {
		return new CachedSet<>();
	}

	@Override
	public CachedSet<Object> createFilled(Object... items) {
		return new CachedSet<>(items);
	}

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.set.CachedSet#CachedSet()}.
		 */
		@Test
		void testCachedSet() {
			assertTrue(new CachedSet<>().isEmpty());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.set.CachedSet#CachedSet(E[])}.
		 */
		@Test
		void testCachedSetEArray() {
			Object[] items = randomContent();
			CachedSet<Object> set = new CachedSet<>(items);
			assertArrayEquals(items, set.toArray());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.set.CachedSet#CachedSet(java.util.List)}.
		 */
		@Test
		void testCachedSetListOfQextendsE() {
			Object[] items = randomContent();
			CachedSet<Object> set = new CachedSet<>(list(items));
			assertArrayEquals(items, set.toArray());
		}

	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.CachedSet#add(java.lang.Object)}.
	 */
	@Test
	void testAdd() {
		Object[] items = randomContent();
		CachedSet<Object> set = createEmpty();
		for (int i = 0; i < items.length; i++) {
			Object item = items[i];
			set.add(item);
			assertSame(item, set.entryAt(i));
		}
		assertArrayEquals(items, set.toArray());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.CachedSet#add(java.lang.Object)}.
	 */
	@Test
	void testAdd_NPE() {
		assertNPE(() -> createEmpty().add(null));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.CachedSet#reset(E[])}.
	 */
	@Test
	void testResetEArray() {
		CachedSet<Object> set = createEmpty();
		Object[] items = randomContent();

		set.reset(items);
		assertArrayEquals(items, set.toArray());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.CachedSet#reset(java.util.List)}.
	 */
	@Test
	void testResetListOfQextendsE() {
		CachedSet<Object> set = createEmpty();
		Object[] items = randomContent();

		set.reset(list(items));
		assertArrayEquals(items, set.toArray());
	}

}
