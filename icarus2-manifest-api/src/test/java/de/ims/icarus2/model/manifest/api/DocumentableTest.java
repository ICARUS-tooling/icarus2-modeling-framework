/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.TestUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface DocumentableTest extends LockableTest {

	/**
	 * Create an unlocked instance of {@link Documentable} for testing
	 * @return
	 */
	@Override
	Documentable createUnlocked();

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentable#getDocumentation()}.
	 */
	@Test
	default void testGetDocumentation() {
		Documentable documentable = createUnlocked();

		assertNull(documentable.getDocumentation());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentable#setDocumentation(de.ims.icarus2.model.manifest.api.Documentation)}.
	 */
	@Test
	default void testSetDocumentation() {
		Documentable documentable = createUnlocked();

		// Test with null
		TestUtils.assertNPE(() -> documentable.setDocumentation(null));

		// Test with real value
		Documentation documentation = mock(Documentation.class);
		documentable.setDocumentation(documentation);
		assertSame(documentation, documentable.getDocumentation());

		// Test locking
		assertFalse(documentable.isLocked());
		documentable.lock();
		LockableTest.assertLocked(() -> documentable.setDocumentation(null));
	}

}
