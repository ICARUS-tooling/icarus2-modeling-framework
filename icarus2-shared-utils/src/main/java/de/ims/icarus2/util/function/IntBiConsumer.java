/**
 *
 */
package de.ims.icarus2.util.function;

import java.util.function.BiConsumer;

/**
 * Primitive specialization of {@link BiConsumer} for {@code int} values.
 *
 * @author Markus Gärtner
 *
 */
@FunctionalInterface
public interface IntBiConsumer extends BiConsumer<Integer, Integer> {

	void accept(int v1, int v2);

	/**
	 * @see java.util.function.BiConsumer#accept(java.lang.Object, java.lang.Object)
	 */
	@Override
	default void accept(Integer t, Integer u) {
		accept(t.intValue(), u.intValue());
	}
}
