/**
 *
 */
package de.ims.icarus2.util.id;

import static de.ims.icarus2.test.TestUtils.assertPresent;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.GenericTest;

/**
 * @author Markus Gärtner
 *
 */
public interface IdentityTest<I extends Identity> extends GenericTest<I> {

	/**
	 * Test method for {@link de.ims.icarus2.util.id.Identity#getId()}.
	 */
	@Test
	default void testGetId() {
		I instance = create();
		assertNotNull(instance.getId());
		assertPresent(instance.getId());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.id.Identity#getName()}.
	 */
	@Test
	default void testGetName() {
		assertNotNull(create().getName());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.id.Identity#getDescription()}.
	 */
	@Test
	default void testGetDescription() {
		assertNotNull(create().getDescription());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.id.Identity#getIcon()}.
	 */
	@Test
	default void testGetIcon() {
		assertNotNull(create().getIcon());
	}

}
