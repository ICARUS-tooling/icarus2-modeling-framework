/**
 *
 */
package de.ims.icarus2.util.strings;

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

		assertThrows(IndexOutOfBoundsException.class, () -> new SubSequence("", 0, 0));
		assertThrows(IndexOutOfBoundsException.class, () -> new SubSequence("x", -1, 0));
		assertThrows(IndexOutOfBoundsException.class, () -> new SubSequence("x", 2, 0));

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
		assertThrows(IndexOutOfBoundsException.class, () -> new SubSequence("x", 0, 0).charAt(1));
		assertEquals('x', new SubSequence("x", 0, 1).charAt(0));
		assertEquals('y', new SubSequence("xy", 1, 1).charAt(0));
		assertEquals('y', new SubSequence("xy", 0, 2).charAt(1));
		assertEquals('2', new SubSequence("x123x", 1, 3).charAt(1));
	}
}
