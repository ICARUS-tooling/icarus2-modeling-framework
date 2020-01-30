/**
 *
 */
package de.ims.icarus2.query.api.eval;

/**
 * @author Markus Gärtner
 *
 */
public final class References {

	private References() { /* no-op */ }

	public static enum ReferenceType {
		REFERENCE(false, true),
		MEMBER(false, false),
		VARIABLE(true, false),
		;

		private final boolean assignable, constant;

		private ReferenceType(boolean assignable, boolean constant) {
			this.assignable = assignable;
			this.constant = constant;
		}
		/** Signals whether or not the underlying object reference can ever be changed. */
		public boolean isAssignable() { return assignable; }

		/** Signals whether or not the underlying object reference will ever change. */
		public boolean isConstant() { return constant; }
	}
}
