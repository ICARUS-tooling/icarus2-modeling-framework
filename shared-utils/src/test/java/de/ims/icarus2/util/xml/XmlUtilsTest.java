/**
 *
 */
package de.ims.icarus2.util.xml;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Markus
 *
 */
public class XmlUtilsTest {

	@Test
	public void testIllegalAttributeSymbols() throws Exception {
		assertTrue(XmlUtils.isIllegalAttributeSymbol('\r'));
		assertTrue(XmlUtils.isIllegalAttributeSymbol('\n'));
		assertTrue(XmlUtils.isIllegalAttributeSymbol('\t'));

		assertTrue(XmlUtils.isIllegalAttributeSymbol('\''));
		assertTrue(XmlUtils.isIllegalAttributeSymbol('"'));
		assertTrue(XmlUtils.isIllegalAttributeSymbol('<'));
		assertTrue(XmlUtils.isIllegalAttributeSymbol('>'));
		assertTrue(XmlUtils.isIllegalAttributeSymbol('&'));
	}

	@Test
	public void testReservedXMLSymbol() throws Exception {
		assertTrue(XmlUtils.isReservedXMLSymbol('\''));
		assertTrue(XmlUtils.isReservedXMLSymbol('"'));
		assertTrue(XmlUtils.isReservedXMLSymbol('<'));
		assertTrue(XmlUtils.isReservedXMLSymbol('>'));
		assertTrue(XmlUtils.isReservedXMLSymbol('&'));
	}
}
