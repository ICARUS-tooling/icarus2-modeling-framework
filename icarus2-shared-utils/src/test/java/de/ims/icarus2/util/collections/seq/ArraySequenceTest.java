/**
 *
 */
package de.ims.icarus2.util.collections.seq;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
class ArraySequenceTest implements DataSequenceTest<ArraySequence<Object>> {

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.seq.ArraySequence#ArraySequence(E[])}.
	 */
	@Test
	void testArraySequence() {
		Object[] items = randomContent();
		ArraySequence<Object> seq = createFilled(items);
		for (int i = 0; i < items.length; i++) {
			assertSame(items[i], seq.elementAt(i));
		}
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<?> getTestTargetClass() {
		return ArraySequence.class;
	}

	/**
	 * @see de.ims.icarus2.util.collections.IterableTest#createEmpty()
	 */
	@Override
	public ArraySequence<Object> createEmpty() {
		return new ArraySequence<>(new Object[0]);
	}

	/**
	 * @see de.ims.icarus2.util.collections.IterableTest#createFilled(java.lang.Object[])
	 */
	@Override
	public ArraySequence<Object> createFilled(Object... items) {
		return new ArraySequence<>(items);
	}
}
