/**
 *
 */
package de.ims.icarus2.query.api.eval;

/**
 * @author Markus Gärtner
 *
 */
public interface Namespace {

	default Namespace getParent() {
		return null;
	}

	/**
	 * Try to resolve the specified {@code name} to an entry
	 * in this namespace.
	 *
	 * @param name
	 * @return
	 */
	<T> T lookup(String name);
}
