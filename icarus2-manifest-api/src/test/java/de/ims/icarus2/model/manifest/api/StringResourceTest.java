/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.util.strings.StringResource;

/**
 * Test case for evaluating enums implementing {@link StringResource}.
 *
 * @author Markus Gärtner
 *
 */
public interface StringResourceTest<R extends StringResource> {

	R[] createStringResources();

	Function<String, R> createParser();

	@Test
	default void testGetStringValue() {
		R[] stringResoruces = createStringResources();
		for(R stringResource : stringResoruces)
			assertNotNull(stringResource.getStringValue());
	}


	@Test
	default void testUniqueStringResources() {
		R[] stringResoruces = createStringResources();
		Set<String> values = new HashSet<>();
		for(R stringResource : stringResoruces) {
			String value = stringResource.getStringValue();
			assertTrue(values.add(value), "Duplicate string value: "+value);
		}
	}

	@Test
	default void testParseStringResource() {
		R[] stringResoruces = createStringResources();
		Function<String, R> parser = createParser();

		for(R stringResource : stringResoruces) {
			String stringValue = stringResource.getStringValue();

			R parsedFlag = parser.apply(stringValue);

			assertEquals(stringResource, parsedFlag, "Inconsistent parsing for string resource: "+stringValue);
		}

		assertThrows(NullPointerException.class, () -> parser.apply(null));
		assertThrows(IllegalArgumentException.class, () -> parser.apply(""));
	}
}
