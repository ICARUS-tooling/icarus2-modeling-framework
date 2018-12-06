/**
 *
 */
package de.ims.icarus2.util;

import java.util.function.Consumer;

/**
 * @author Markus Gärtner
 *
 */
public interface Traversable<T extends Object> {

	void forEach(Consumer<? super T> action);
}
