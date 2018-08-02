/**
 *
 */
package de.ims.icarus2.model.manifest.standard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.Documentation;
import de.ims.icarus2.model.manifest.api.DocumentationTest;

/**
 * @author Markus Gärtner
 *
 */
class DocumentationImplTest implements DocumentationTest {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.standard.DocumentationImpl#DocumentationImpl()}.
	 */
	@Test
	void testDocumentationImpl() {
		DocumentationImpl impl = new DocumentationImpl();
		assertNull(impl.getContent());
		assertTrue(impl.getResources().isEmpty());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.standard.DocumentationImpl#DocumentationImpl(java.lang.String)}.
	 */
	@Test
	void testDocumentationImplString() {
		DocumentationImpl impl = new DocumentationImpl("content");
		assertEquals("content", impl.getContent());
		assertTrue(impl.getResources().isEmpty());
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DocumentationTest#createUnlocked()
	 */
	@Override
	public Documentation createUnlocked() {
		return new DocumentationImpl();
	}

}
