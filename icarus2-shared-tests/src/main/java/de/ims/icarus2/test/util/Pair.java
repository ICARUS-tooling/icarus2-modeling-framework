/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 */
package de.ims.icarus2.test.util;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;

import javax.annotation.Nullable;

import de.ims.icarus2.test.TestUtils;

public class Pair<E_1 extends Object, E_2 extends Object> {

	public static <E_1, E_2> Pair<E_1, E_2> pair(E_1 first, E_2 second) {
		return new Pair<>(first, second);
	}

	public static <E_1, E_2> Pair<E_1, E_2> nullablePair(
			@Nullable E_1 first, @Nullable E_2 second) {
		return new Pair<>(first, second, true);
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
		this(first, second, false);
	}

	private Pair(E_1 first, E_2 second, boolean allowNull) {
		this.first = allowNull ? first : requireNonNull(first);
		this.second = allowNull ? second : requireNonNull(second);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return TestUtils.displayString("<%s,%s>", first, second);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj==this) {
			return true;
		} else if(obj instanceof Pair) {
			Pair<?,?> other = (Pair<?, ?>) obj;
			return Objects.equals(first, other.first)
					&& Objects.equals(second, other.second);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}
}