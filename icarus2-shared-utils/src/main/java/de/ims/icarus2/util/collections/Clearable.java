/**
 *
 */
package de.ims.icarus2.util.collections;

/**
 * Interface for defining a single capability, that is the ability
 * to clear whatever content an object holds as state.
 *
 * @author Markus Gärtner
 *
 */
public interface Clearable {

	/**
	 * Erase internal state and clear this object.
	 */
	void clear();
}
