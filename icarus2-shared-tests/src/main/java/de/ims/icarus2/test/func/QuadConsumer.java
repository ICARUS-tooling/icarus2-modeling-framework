/**
 *
 */
package de.ims.icarus2.test.func;

/**
 * @author Markus Gärtner
 *
 * @param <T_1> type of the first argument
 * @param <T_2> type of the second argument
 * @param <T_3> type of the third argument
 * @param <T_4> type of the forth argument
 */
@FunctionalInterface
public interface QuadConsumer<T_1, T_2, T_3, T_4> {

	void accept(T_1 first, T_2 seconds, T_3 third,T_4 forth);
}
