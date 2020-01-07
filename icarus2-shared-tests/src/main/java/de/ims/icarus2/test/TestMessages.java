/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

	static final String expectedNPE = "expected "+NullPointerException.class.getSimpleName();
	static final String expectedIAE = "expected "+IllegalArgumentException.class.getSimpleName();
	static final String expectedISE = "expected "+IllegalStateException.class.getSimpleName();
	static final String expectedIOOB = "expected "+IndexOutOfBoundsException.class.getSimpleName();

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
