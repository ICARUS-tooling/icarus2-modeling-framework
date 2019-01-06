/**
 *
 */
package de.ims.icarus2.test.util;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.ims.icarus2.test.TestUtils;

public class Pair<E_1 extends Object, E_2 extends Object> {

	public static <E_1, E_2> Pair<E_1, E_2> pair(E_1 first, E_2 second) {
		return new Pair<>(first, second);
	}

	@SuppressWarnings("boxing")
	public static Pair<Integer, Integer> intPair(int first, int second) {
		return new Pair<>(first, second);
	}

	public static Pair<Integer, Integer>[] intChain(int from, int to) {
		@SuppressWarnings("unchecked")
		Pair<Integer, Integer>[] array = new Pair[to-from];

		for(int idx = 0; idx<array.length; idx++) {
			array[idx] = intPair(from+idx, from+idx+1);
		}

		return array;
	}

	public static Pair<Long, Long>[] longChain(long from, long to) {
		long diff = to-from;
		assertTrue(diff<TestUtils.MAX_INTEGER_INDEX);

		@SuppressWarnings("unchecked")
		Pair<Long, Long>[] array = new Pair[(int)diff];

		for(int idx = 0; idx<array.length; idx++) {
			array[idx] = longPair(from+idx, from+idx+1);
		}

		return array;
	}

	@SuppressWarnings("boxing")
	public static Pair<Long, Long> longPair(long first, long second) {
		return new Pair<>(first, second);
	}

	public final E_1 first;
	public final E_2 second;

	public Pair(E_1 first, E_2 second) {
		this.first = requireNonNull(first);
		this.second = requireNonNull(second);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return TestUtils.displayString("<%s,%s>", first, second);
	}
}