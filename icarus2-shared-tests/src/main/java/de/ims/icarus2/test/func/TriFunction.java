/**
 *
 */
package de.ims.icarus2.test.func;

/**
 * @author Markus Gärtner
 *
 */
@FunctionalInterface
public interface TriFunction<IN_1, IN_2, IN_3, OUT> {

	/**
	 * Applies this function to the given 3 parameters and produces a
	 * result.
	 *
	 * @param val1
	 * @param val2
	 * @param val3
	 * @return
	 */
	OUT apply(IN_1 val1, IN_2 val2, IN_3 val3);
}
