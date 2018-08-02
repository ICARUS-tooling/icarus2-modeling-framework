/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.util.Flag;

/**
 * Test case for evaluating enums implementing {@link Flag}.
 *
 * @author Markus Gärtner
 *
 */
public interface FlagTest<F extends Flag> {

	F[] createFlags();

	@Test
	default void testName() {
		F[] flags = createFlags();
		for(F flag : flags)
			assertNotNull(flag.name());
	}

	@Test
	default void testUniqueNames() {
		F[] flags = createFlags();
		Set<String> names = new HashSet<>();
		for(F flag : flags) {
			String name = flag.name();
			assertTrue(names.add(name), "Duplicate name: "+name);
		}
	}
}
