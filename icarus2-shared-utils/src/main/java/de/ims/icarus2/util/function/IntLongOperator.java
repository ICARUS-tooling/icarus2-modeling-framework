/**
 *
 */
package de.ims.icarus2.util.function;

import java.util.function.BiFunction;

/**
 * @author Markus Gärtner
 *
 */
@FunctionalInterface
public interface IntLongOperator extends BiFunction<Integer, Long, Long> {

	long applyAsLong(int v0, long v1);

	/**
	 * @see java.util.function.BiFunction#apply(java.lang.Object, java.lang.Object)
	 */
	@Override
	default Long apply(Integer t, Long u) {
		return Long.valueOf(applyAsLong(t.intValue(), u.longValue()));
	}
}
