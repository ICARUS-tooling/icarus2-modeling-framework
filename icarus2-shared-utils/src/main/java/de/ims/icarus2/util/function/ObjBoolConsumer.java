/**
 *
 */
package de.ims.icarus2.util.function;

/**
 * @author Markus Gärtner
 *
 * @param <T> the type of the object argument to the operation
 *
 */
@FunctionalInterface
public interface ObjBoolConsumer<T extends Object> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param value the second input argument
     */
    void accept(T t, boolean value);
}
