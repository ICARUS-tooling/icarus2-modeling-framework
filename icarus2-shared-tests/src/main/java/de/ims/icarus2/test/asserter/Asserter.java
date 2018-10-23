/**
 *
 */
package de.ims.icarus2.test.asserter;

/**
 * @author Markus Gärtner
 *
 */
public abstract class Asserter<A extends Asserter<A>> {

	/**
	 * Prevents public constructor from leaking
	 */
	protected Asserter() {
		// no-op
	}

	@SuppressWarnings("unchecked")
	protected final A thisAsCast() {
		return (A) this;
	}

	/**
	 *
	 */
	public abstract void test();
}
