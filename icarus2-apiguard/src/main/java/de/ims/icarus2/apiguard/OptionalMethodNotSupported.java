/**
 *
 */
package de.ims.icarus2.apiguard;

/**
 * @author Markus Gärtner
 *
 */
public class OptionalMethodNotSupported extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = -4741809506690431119L;

	/**
	 *
	 */
	public OptionalMethodNotSupported() {
		// no-op
	}

	/**
	 * @param message
	 */
	public OptionalMethodNotSupported(String message) {
		super(message);
	}

}
