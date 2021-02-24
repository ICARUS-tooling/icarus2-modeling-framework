/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
public interface ErrorCodeTest<E extends Enum<E>> {

	E[] createErrorCodes();

	IntFunction<E> createCodeParser();

	ToIntFunction<E> createCodeGenerator();

	@Test
	default void testCodeConsistency() throws Exception {
		E[] errorCodes = createErrorCodes();
		IntFunction<E> parser = createCodeParser();
		ToIntFunction<E> generator = createCodeGenerator();

		for(E errorCode : errorCodes) {
			int code = generator.applyAsInt(errorCode);
			assertTrue(code>=0, "Error code must be positive: "+code);

			E parsedCode = parser.apply(code);

			assertNotNull(parsedCode);

			assertEquals(errorCode, parsedCode);
		}
	}
}
