/**
 *
 */
package de.ims.icarus2.test.func;

/**
 * @author Markus Gärtner
 *
 */
@FunctionalInterface
public interface ThrowingBiConsumer<T, U> {

	void accept(T first, U second) throws Exception;
}
