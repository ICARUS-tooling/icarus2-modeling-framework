/**
 *
 */
package de.ims.icarus2.query.api.eval;

/**
 * A proxy to model arbitrary random-access data structures.
 *
 * @author Markus Gärtner
 *
 */
//TODO for now we restrict indices to int space, but Container for instance uses long
public interface ListProxy<T> {

	int size();

	T get(int index);
}
