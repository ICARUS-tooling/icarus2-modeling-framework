/**
 *
 */
package de.ims.icarus2.misc;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

/**
 * DUmym tests to verify language features
 *
 * @author Markus Gärtner
 *
 */
class LangTest {

	@Test
	void testAssignmentCompatibility() {
		assertFalse(Long.class.isAssignableFrom(Integer.TYPE));
	}
}
