/**
 *
 */
package de.ims.icarus2.util.collections.seq;

import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Collections;

import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
class ListSequenceTest implements DataSequenceTest<ListSequence<Object>> {

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.seq.ListSequence#ListSequence(java.util.List)}.
	 */
	@Test
	void testListSequence() {
		Object[] items = randomContent();
		ListSequence<Object> seq = new ListSequence<>(list(items));
		for (int i = 0; i < items.length; i++) {
			assertSame(items[i], seq.elementAt(i));
		}
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<?> getTestTargetClass() {
		return ListSequence.class;
	}

	/**
	 * @see de.ims.icarus2.util.collections.IterableTest#createEmpty()
	 */
	@Override
	public ListSequence<Object> createEmpty() {
		return new ListSequence<>(Collections.emptyList());
	}

	/**
	 * @see de.ims.icarus2.util.collections.IterableTest#createFilled(java.lang.Object[])
	 */
	@Override
	public ListSequence<Object> createFilled(Object... items) {
		return new ListSequence<>(list(items));
	}

}
