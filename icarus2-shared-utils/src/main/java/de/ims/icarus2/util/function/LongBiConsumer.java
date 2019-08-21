/**
 *
 */
package de.ims.icarus2.util.function;

import java.util.function.BiConsumer;

/**
 * Primitive specialization of {@link BiConsumer} for {@code long} values.
 *
 * @author Markus Gärtner
 *
 */
@FunctionalInterface
public interface LongBiConsumer extends BiConsumer<Long, Long> {

	void accept(long v1, long v2);

	/**
	 * @see java.util.function.BiConsumer#accept(java.lang.Object, java.lang.Object)
	 */
	@Override
	default void accept(Long t, Long u) {
		accept(t.longValue(), u.longValue());
	}
}
