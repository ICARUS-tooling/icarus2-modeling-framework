/**
 *
 */
package de.ims.icarus2.test.util;

import static java.util.Objects.requireNonNull;

public class Pair<E_1 extends Object, E_2 extends Object> {

	public static <E_1, E_2> Pair<E_1, E_2> pair(E_1 first, E_2 second) {
		return new Pair<>(first, second);
	}

	@SuppressWarnings("boxing")
	public static Pair<Integer, Integer> intPair(int first, int second) {
		return new Pair<>(first, second);
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

}