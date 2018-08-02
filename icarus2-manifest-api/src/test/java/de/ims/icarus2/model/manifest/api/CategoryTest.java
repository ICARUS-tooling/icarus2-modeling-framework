/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
public interface CategoryTest {

	Category createCategory();

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Category#getId()}.
	 */
	@Test
	default void testGetId() {
		assertNotNull(createCategory().getId());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Category#getNamespace()}.
	 */
	@Test
	default void testGetNamespace() {
		assertNotNull(createCategory().getNamespace());
	}

}
