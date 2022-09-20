/**
 *
 */
package de.ims.icarus2.util.function;

/**
 * @author Markus Gärtner
 *
 */
@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {

	void accept(T t) throws E;
}
