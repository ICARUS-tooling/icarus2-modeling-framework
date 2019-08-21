/**
 *
 */
package de.ims.icarus2.util.function;

import java.util.function.BiConsumer;

/**
 * THe primitive specialization of {@link BiConsumer}.
 *
 * @author Markus Gärtner
 *
 */
@FunctionalInterface
public interface IntLongConsumer extends BiConsumer<Integer, Long> {

	void accept(int val_i, long val_l);

	/**
	 * @see java.util.function.BiConsumer#accept(java.lang.Object, java.lang.Object)
	 */
	@Override
	default void accept(Integer t, Long u) {
		accept(t.intValue(), u.longValue());
	}
}
