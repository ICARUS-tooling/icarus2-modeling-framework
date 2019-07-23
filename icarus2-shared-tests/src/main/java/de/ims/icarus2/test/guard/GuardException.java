/**
 *
 */
package de.ims.icarus2.test.guard;

/**
 * @author Markus Gärtner
 *
 */
public class GuardException extends RuntimeException {

	private static final long serialVersionUID = -8368707973333304787L;

	/**
	 * @param message
	 * @param cause
	 */
	public GuardException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public GuardException(String message) {
		super(message);
	}

}
