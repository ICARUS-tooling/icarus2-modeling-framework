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
