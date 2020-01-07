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
package de.ims.icarus2.util.strings;

import static de.ims.icarus2.test.TestUtils.assertIOOB;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
public class SubSequenceTest {

	@Test
	public void testConstructorFail() throws Exception {
		assertThrows(NullPointerException.class, () -> new SubSequence(null, 0, 0));

		assertIOOB(() -> new SubSequence("", 0, 0));
		assertIOOB(() -> new SubSequence("x", -1, 0));
		assertIOOB(() -> new SubSequence("x", 2, 0));

		assertThrows(IllegalArgumentException.class, () -> new SubSequence("x", 0, -1));
		assertThrows(IllegalArgumentException.class, () -> new SubSequence("x", 0, 2));
	}

	@Test
	public void testLength() throws Exception {
		assertEquals(0, new SubSequence("x", 0, 0).length());
		assertEquals(1, new SubSequence("x", 0, 1).length());
		assertEquals(1, new SubSequence("xx", 1, 1).length());
		assertEquals(3, new SubSequence("x123x", 1, 3).length());
	}

	@Test
	public void testCharAt() throws Exception {
		assertIOOB(() -> new SubSequence("x", 0, 0).charAt(1));
		assertEquals('x', new SubSequence("x", 0, 1).charAt(0));
		assertEquals('y', new SubSequence("xy", 1, 1).charAt(0));
		assertEquals('y', new SubSequence("xy", 0, 2).charAt(1));
		assertEquals('2', new SubSequence("x123x", 1, 3).charAt(1));
	}
}
