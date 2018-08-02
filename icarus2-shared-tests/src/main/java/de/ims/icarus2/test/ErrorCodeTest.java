/**
 *
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
