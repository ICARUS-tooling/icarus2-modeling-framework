/**
 *
 */
package de.ims.icarus2.util.collections.seq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
class SingletonSequenceTest implements DataSequenceTest<SingletonSequence<Object>> {

	/**
	 * @see de.ims.icarus2.util.collections.seq.DataSequenceTest#randomContent()
	 */
	@Override
	public Object[] randomContent() {
		return new Object[] {new Object()};
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.seq.SingletonSequence#SingletonSequence(java.lang.Object)}.
	 */
	@Test
	void testSingletonSequence() {
		Object item = new Object();
		SingletonSequence<Object> seq = new SingletonSequence<Object>(item);
		assertEquals(1, seq.entryCount());
		assertSame(item, seq.elementAt(0));
}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<?> getTestTargetClass() {
		return SingletonSequence.class;
	}

	/**
	 * @see de.ims.icarus2.util.collections.IterableTest#createEmpty()
	 */
	@Override
	public SingletonSequence<Object> createEmpty() {
		return new SingletonSequence<>();
	}

	/**
	 * @see de.ims.icarus2.util.collections.IterableTest#createFilled(java.lang.Object[])
	 */
	@Override
	public SingletonSequence<Object> createFilled(Object... items) {
		assertEquals(1, items.length);
		return new SingletonSequence<Object>(items[0]);
	}

}
