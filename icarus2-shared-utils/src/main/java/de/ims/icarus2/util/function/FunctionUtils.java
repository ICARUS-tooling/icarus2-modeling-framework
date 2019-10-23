/**
 *
 */
package de.ims.icarus2.util.function;

import java.util.function.Predicate;

/**
 * @author Markus Gärtner
 *
 */
public class FunctionUtils {

	public static <T> Predicate<T> not(Predicate<T> source) {
		return t -> !source.test(t);
	}
}
