/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ims.icarus2.util.xml;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
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
