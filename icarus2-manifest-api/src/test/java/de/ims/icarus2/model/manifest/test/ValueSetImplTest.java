/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.

 * $Revision: 332 $
 * $Date: 2014-12-16 13:55:39 +0100 (Di, 16 Dez 2014) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.model/test/de/ims/icarus/language/model/test/manifest/ValueSetImplTest.java $
 *
 * $LastChangedDate: 2014-12-16 13:55:39 +0100 (Di, 16 Dez 2014) $
 * $LastChangedRevision: 332 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.test;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.getTestValues;
import static de.ims.icarus2.model.manifest.xml.ManifestXmlTestUtils.assertSerializationEquals;
import static de.ims.icarus2.util.TestUtils.assertHashContract;
import static de.ims.icarus2.util.TestUtils.assertObjectContract;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.standard.ValueSetImpl;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.xml.delegates.ValueSetXmlDelegate;

/**
 * @author Markus Gärtner
 * @version $Id: ValueSetImplTest.java 332 2014-12-16 12:55:39Z mcgaerty $
 *
 */
public class ValueSetImplTest {

	private void testAdd(ValueType valueType) throws Exception {

		ValueSetImpl valueSet = new ValueSetImpl(valueType);

		assertSame(valueType, valueSet.getValueType(), "Value type corrupted"); //$NON-NLS-1$

		Object[] values = getTestValues(valueType);

		assertNotNull(values, "Test value array empty"); //$NON-NLS-1$

		for(Object value : values) {
			valueSet.addValue(value);
		}

		assertEquals(values.length, valueSet.valueCount());

		for(int i=0; i<values.length; i++) {
			assertSame(values[i], valueSet.getValueAt(i), "Mismatching item at index "+i); //$NON-NLS-1$
		}
	}

	private void testXml(ValueType valueType) throws Exception {

		ValueSetImpl valueSet = new ValueSetImpl(valueType);
		Object[] values = getTestValues(valueType);

		assertNotNull(values, "Test value array empty"); //$NON-NLS-1$

		for(Object value : values) {
			valueSet.addValue(value);
		}

		ValueSetImpl newValueSet = new ValueSetImpl(valueType);

		assertSerializationEquals("Value type: "+valueType, valueSet, newValueSet, new ValueSetXmlDelegate());
	}

	@Test
	public void testGeneral() throws Exception {
		ValueSetImpl valueSet1 = new ValueSetImpl(ValueType.STRING);
		ValueSetImpl valueSet2 = new ValueSetImpl(ValueType.INTEGER);

		assertHashContract(valueSet1, valueSet2);
		assertHashContract(valueSet1, valueSet1);
	}

	@Test
	public void testObjectContract() throws Exception {
		assertObjectContract(new ValueSetImpl(ValueType.STRING));
	}

	@Test
	public void testAddNull() throws Exception {
		ValueSetImpl valueSet = new ValueSetImpl(ValueType.STRING);

		assertThrows(NullPointerException.class, () -> valueSet.addValue(null));
	}

	// CONSTRUCTION

	@Test
	public void testStringSet() throws Exception {
		testAdd(ValueType.STRING);
	}

	@Test
	public void testIntegerSet() throws Exception {
		testAdd(ValueType.INTEGER);
	}

	@Test
	public void testLongSet() throws Exception {
		testAdd(ValueType.LONG);
	}

	@Test
	public void testFloatSet() throws Exception {
		testAdd(ValueType.FLOAT);
	}

	@Test
	public void testDoubleSet() throws Exception {
		testAdd(ValueType.DOUBLE);
	}

	@Test
	public void testEnumSet() throws Exception {
		testAdd(ValueType.ENUM);
	}

	@Test
	public void testExtensionSet() throws Exception {
		testAdd(ValueType.EXTENSION);
	}

	@Test
	public void testImageSet() throws Exception {
		testAdd(ValueType.IMAGE);
	}

	@Test
	public void testImageResourceSet() throws Exception {
		testAdd(ValueType.IMAGE_RESOURCE);
	}

	@Test
	public void testBooleanSet() throws Exception {
		testAdd(ValueType.BOOLEAN);
	}

	@Test
	public void testUrlSet() throws Exception {
		testAdd(ValueType.URL);
	}

	@Test
	public void testUrlResourceSet() throws Exception {
		testAdd(ValueType.URL_RESOURCE);
	}

	@Test
	public void testUnknownSet() throws Exception {
		testAdd(ValueType.UNKNOWN);
	}

	@Test
	public void testCustomSet() throws Exception {
		testAdd(ValueType.CUSTOM);
	}

	//SERIALIZATION

	@Test
	public void testXmlStringSet() throws Exception {
		testXml(ValueType.STRING);
	}

	@Test
	public void testXmlIntegerSet() throws Exception {
		testXml(ValueType.INTEGER);
	}

	@Test
	public void testXmlLongSet() throws Exception {
		testXml(ValueType.LONG);
	}

	@Test
	public void testXmlFloatSet() throws Exception {
		testXml(ValueType.FLOAT);
	}

	@Test
	public void testXmlDoubleSet() throws Exception {
		testXml(ValueType.DOUBLE);
	}

	@Test
	public void testXmlEnumSet() throws Exception {
		testXml(ValueType.ENUM);
	}

	@Test
	public void testXmlExtensionSet() throws Exception {
		testXml(ValueType.EXTENSION);

		// Use modified value type that bypasses the plugin engine
//		testXml(TestUtils.EXTENSION_TYPE);
	}

	@Test
	public void testXmlImageSet() throws Exception {
		testXml(ValueType.IMAGE);
	}

	@Test
	public void testXmlImageResourceSet() throws Exception {
		assertThrows(UnsupportedOperationException.class, () -> testXml(ValueType.IMAGE_RESOURCE));
	}

	@Test
	public void testXmlBooleanSet() throws Exception {
		testXml(ValueType.BOOLEAN);
	}

	@Test
	public void testXmlUrlSet() throws Exception {
		testXml(ValueType.URL);
	}

	@Test
	public void testXmlUrlResourceSet() throws Exception {
		assertThrows(UnsupportedOperationException.class, () -> testXml(ValueType.URL_RESOURCE));
	}

	@Test
	public void testXmlUnknownSet() throws Exception {
		assertThrows(UnsupportedOperationException.class, () -> testXml(ValueType.UNKNOWN));
	}

	@Test
	public void testXmlCustomSet() throws Exception {
		assertThrows(UnsupportedOperationException.class, () -> testXml(ValueType.CUSTOM));
	}
}
