/**
 *
 */
package de.ims.icarus2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.function.Executable;

/**
 * @author Markus Gärtner
 *
 */
public class SharedTestUtils {


	public static void assertIcarusException(ErrorCode errorCode, Executable executable, String msg) {
		IcarusException exception = assertThrows(IcarusException.class, executable, msg);
		assertEquals(errorCode, exception.getErrorCode(), msg);
	}
}
