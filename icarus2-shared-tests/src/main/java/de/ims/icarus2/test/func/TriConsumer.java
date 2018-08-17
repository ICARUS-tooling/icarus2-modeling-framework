/**
 *
 */
package de.ims.icarus2.test.func;

/**
 * @author Markus Gärtner
 *
 * @param <T> type of the first argument
 * @param <U> type of the second argument
 * @param <K> type of the third argument
 */
@FunctionalInterface
public interface TriConsumer<T extends Object, U extends Object, K extends Object> {

	void accept(T first, U seconds, K third);
}
