/**
 *
 */
package de.ims.icarus2.util.collections.set;

import static de.ims.icarus2.SharedTestUtils.assertIcarusException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.GlobalErrorCode;

/**
 * @author Markus Gärtner
 *
 */
class SingletonSetTest implements DataSetTest<SingletonSet<Object>> {

	@Override
	public Class<?> getTestTargetClass() {
		return SingletonSet.class;
	}

	@Override
	public SingletonSet<Object> createEmpty() {
		return new SingletonSet<>();
	}

	/**
	 * @see de.ims.icarus2.util.collections.set.DataSetTest#randomContent()
	 */
	@Override
	public Object[] randomContent() {
		return new Object[] {new Object()};
	}

	@Override
	public SingletonSet<Object> createFilled(Object... items) {
		assertTrue(items.length<=1);
		return new SingletonSet<>(items[0]);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.SingletonSet#SingletonSet()}.
	 */
	@Test
	void testSingletonSet() {
		assertNotNull(new SingletonSet<>());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.SingletonSet#SingletonSet(java.lang.Object)}.
	 */
	@Test
	void testSingletonSetE() {
		Object item = new Object();
		SingletonSet<Object> set = new SingletonSet<Object>(item);
		assertEquals(1, set.entryCount());
		assertSame(item, set.entryAt(0));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.SingletonSet#reset(java.lang.Object)}.
	 */
	@Test
	void testReset() {
		Object item = new Object();
		SingletonSet<Object> set = createEmpty();
		set.reset(item);
		assertEquals(1, set.entryCount());
		assertSame(item, set.entryAt(0));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.set.SingletonSet#add(Object)}.
	 */
	@Test
	void testAdd() {
		Object item = new Object();
		SingletonSet<Object> set = createEmpty();
		set.add(item);
		assertEquals(1, set.entryCount());
		assertSame(item, set.entryAt(0));

		assertIcarusException(GlobalErrorCode.ILLEGAL_STATE, () -> set.add(new Object()));
	}

}
