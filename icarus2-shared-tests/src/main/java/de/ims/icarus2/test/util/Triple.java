/**
 *
 */
package de.ims.icarus2.test.util;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.test.TestUtils;

/**
 * @author Markus Gärtner
 *
 */
public class Triple<E_1, E_2, E_3> {

	public static <E_1, E_2, E_3> Triple<E_1, E_2, E_3> of(E_1 first, E_2 second, E_3 third) {
		return new Triple<>(first, second, third);
	}

	public final E_1 first;
	public final E_2 second;
	public final E_3 third;

	/**
	 * @param first
	 * @param second
	 * @param third
	 */
	public Triple(E_1 first, E_2 second, E_3 third) {
		this.first = requireNonNull(first);
		this.second = requireNonNull(second);
		this.third = requireNonNull(third);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return TestUtils.displayString("<%s,%s,%s>", first, second, third);
	}
}
