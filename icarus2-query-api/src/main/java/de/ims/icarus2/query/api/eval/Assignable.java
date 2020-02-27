/**
 *
 */
package de.ims.icarus2.query.api.eval;

import javax.annotation.Nullable;

/**
 * @author Markus Gärtner
 *
 */
public interface Assignable<T> extends Expression<T> {

	void assign(@Nullable T value);

	//TODO primitive specializations for 'assign()' ?

	void clear();
}
