/**
 *
 */
package de.ims.icarus2.model.manifest.xml.delegates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import de.ims.icarus2.model.manifest.api.Documentable;
import de.ims.icarus2.model.manifest.api.Documentation;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest;

/**
 * @author Markus Gärtner
 *
 */
class DocumentationXmlDelegateTest implements ManifestXmlDelegateTest<Documentation, DocumentationXmlDelegate> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends DocumentationXmlDelegate> getTestTargetClass() {
		return DocumentationXmlDelegate.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#getHandledType()
	 */
	@Override
	public ManifestType getHandledType() {
		return ManifestType.DOCUMENTATION;
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#testMandatoryConstructors()
	 */
	@Override
	public void testMandatoryConstructors() throws Exception {
		createNoArgs();

		Documentation documentation = mock(Documentation.class);
		assertEquals(documentation, new DocumentationXmlDelegate(documentation).getInstance());

		assertNotNull(new DocumentationXmlDelegate(mock(Documentable.class)).getInstance());
	}
}
