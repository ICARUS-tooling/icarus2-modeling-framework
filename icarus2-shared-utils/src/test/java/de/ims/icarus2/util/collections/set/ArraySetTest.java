/**
 *
 */
package de.ims.icarus2.util.collections.set;

import static de.ims.icarus2.SharedTestUtils.assertIcarusException;
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.test.TestUtils.randomContent;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;

/**
 * @author Markus Gärtner
 *
 */
class ArraySetTest implements DataSetTest<ArraySet<Object>> {

	@Override
	public Class<?> getTestTargetClass() {
		return ArraySet.class;
	}

	@Override
	public ArraySet<Object> createEmpty() {
		return new ArraySet<>(new Object[0]);
	}

	@Override
	public ArraySet<Object> createFilled(Object... items) {
		return new ArraySet<>(items);
	}

	public ArraySet<Object> createEmpty(int capacity) {
		return new ArraySet<>(new Object[capacity]);
	}

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.set.ArraySet#ArraySet()}.
		 */
		@Test
		void testArraySet() {
			assertNotNull(new ArraySet<>());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.set.ArraySet#ArraySet(E[])}.
		 */
		@Test
		void testArraySetEArray() {
			assertNotNull(new ArraySet<>(randomContent()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.collections.set.ArraySet#ArraySet(java.util.List)}.
		 */
		@Test
		void testArraySetListOfQextendsE() {
			Object[] items = randomContent();
			ArraySet<Object> set = new ArraySet<>(list(items));
			assertArrayEquals(items, set.toArray());
		}

	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.ArraySet#add(java.lang.Object)}.
	 */
	@Test
	void testAdd() {
		Object[] items = new Object[10];
		ArraySet<Object> set = new ArraySet<>(items);
		for (int i = 0; i < items.length; i++) {
			Object item = new Object();
			set.add(item);
			assertSame(item, set.entryAt(i));
		}
		assertArrayEquals(items, set.toArray());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.ArraySet#add(java.lang.Object)}.
	 */
	@Test
	void testAdd_NPE() {
		assertNPE(() -> createEmpty(1).add(null));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.ArraySet#reset(int)}.
	 */
	@RepeatedTest(RUNS)
	void testResetInt() {
		int size = random(1, 100);
		ArraySet<Object> set = create();
		set.reset(size);
		assertEquals(size, set.entryCount());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.ArraySet#reset(int)}.
	 */
	@ParameterizedTest
	@ValueSource(ints = {-1, 0})
	void testResetInt_InvalidSize(int size) {
		assertIcarusException(GlobalErrorCode.INVALID_INPUT, () -> create().reset(size));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.ArraySet#set(int, java.lang.Object)}.
	 */
	@Test
	void testSet() {
		ArraySet<Object> set = createFilled(randomContent());
		for (int i = 0; i < 10; i++) {
			Object item = new Object();
			int index = random(0, set.entryCount());
			set.set(index, item);
			assertSame(item, set.entryAt(index));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.ArraySet#reset(java.lang.Object[])}.
	 */
	@Test
	void testResetObjectArray() {
		ArraySet<Object> set = createEmpty();
		Object[] items = randomContent();

		set.reset(items);
		assertArrayEquals(items, set.toArray());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.ArraySet#reset(java.util.List)}.
	 */
	@Test
	void testResetListOfQextendsE() {
		ArraySet<Object> set = createEmpty();
		Object[] items = randomContent();

		set.reset(list(items));
		assertArrayEquals(items, set.toArray());
	}

}
