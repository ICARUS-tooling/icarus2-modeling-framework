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
public interface LongIntOperator extends BiFunction<Long, Integer, Long> {

	long applyAsLong(long v0, int v1);

	/**
	 * @see java.util.function.BiFunction#apply(java.lang.Object, java.lang.Object)
	 */
	@Override
	default Long apply(Long t, Integer u) {
		return Long.valueOf(applyAsLong(t.longValue(), u.intValue()));
	}
}
