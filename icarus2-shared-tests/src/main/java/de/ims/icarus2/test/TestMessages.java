/**
 *
 */
package de.ims.icarus2.test;

/**
 * @author Markus Gärtner
 *
 */
class TestMessages {

	static final String unexpectedDefaultValue = "unexpected default value";
	static final String unexpectedNullDefaultValue = "unexpected null default value";
	static final String unexpectedNonNullDefaultValue = "unexpected non-null default value";
	static final String notHonoringValueChange1 = "not honoring first value change";
	static final String notHonoringValueChange2 = "not honoring second value change";

	static final String expectedNPE = "expected NPE";

	/**
	 * Arguments: required number and then the supplied number
	 */
	static final String insufficientElements = "insufficient elements: needs %d - got %d";

	/**
	 * Arguments: first and second numerical index and then first and second value
	 */
	static final String unexpectedPairwiseEqual =
			"expected values at indices %d and %d to not be equal: '%s' vs '%s'";

	/**
	 * Arguments: string representation of object
	 */
	static final String notAMock = "given object is not a mock %s";
	static final String mockIsNull = "given mock is null";
}
