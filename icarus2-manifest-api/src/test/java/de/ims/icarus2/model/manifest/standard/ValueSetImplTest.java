/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.standard;

import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.test.TestUtils.settings;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.ValueSetTest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.OverrideTest;

/**
 * @author Markus Gärtner
 *
 */
class ValueSetImplTest implements ValueSetTest<ValueSetImpl> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends ValueSetImpl> getTestTargetClass() {
		return ValueSetImpl.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	public ManifestType getExpectedType() {
		return ManifestType.VALUE_SET;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ValueSetTest#createWithType(de.ims.icarus2.test.TestSettings, de.ims.icarus2.model.manifest.types.ValueType)
	 */
	@Override
	public ValueSetImpl createWithType(TestSettings settings, ValueType valueType) {
		return settings.process(new ValueSetImpl(valueType));
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#testMandatoryConstructors()
	 */
	@Override
	@OverrideTest
	@Test
	public void testMandatoryConstructors() throws Exception {
		for(ValueType valueType : ValueType.valueTypes()) {
			createWithType(settings(), valueType);
		}

		assertNPE(() -> createWithType(settings(), null));
	}

//	private void testAdd(ValueType valueType) throws Exception {
//
//		ValueSetImpl valueSet = new ValueSetImpl(valueType);
//
//		assertSame(valueType, valueSet.getValueType(), "Value type corrupted"); //$NON-NLS-1$
//
//		Object[] values = getTestValues(valueType);
//
//		assertNotNull(values, "Test value array empty"); //$NON-NLS-1$
//
//		for(Object value : values) {
//			valueSet.addValue(value);
//		}
//
//		assertEquals(values.length, valueSet.valueCount());
//
//		for(int i=0; i<values.length; i++) {
//			assertSame(values[i], valueSet.getValueAt(i), "Mismatching item at index "+i); //$NON-NLS-1$
//		}
//	}
//
//	private void testXml(ValueType valueType) throws Exception {
//
//		ValueSetImpl valueSet = new ValueSetImpl(valueType);
//		Object[] values = getTestValues(valueType);
//
//		assertNotNull(values, "Test value array empty"); //$NON-NLS-1$
//
//		for(Object value : values) {
//			valueSet.addValue(value);
//		}
//
//		ValueSetImpl newValueSet = new ValueSetImpl(valueType);
//
//		assertSerializationEquals("Value type: "+valueType, valueSet, newValueSet,
//				new ValueSetXmlDelegate(), false, false);
//	}
//
//	@Test
//	public void testGeneral() throws Exception {
//		ValueSetImpl valueSet1 = new ValueSetImpl(ValueType.STRING);
//		ValueSetImpl valueSet2 = new ValueSetImpl(ValueType.INTEGER);
//
//		assertHashContract(valueSet1, valueSet2);
//		assertHashContract(valueSet1, valueSet1);
//	}
//
//	@Test
//	public void testObjectContract() throws Exception {
//		assertObjectContract(new ValueSetImpl(ValueType.STRING));
//	}
//
//	@Test
//	public void testAddNull() throws Exception {
//		ValueSetImpl valueSet = new ValueSetImpl(ValueType.STRING);
//
//		assertThrows(NullPointerException.class, () -> valueSet.addValue(null));
//	}
//
//	// CONSTRUCTION
//
//	@Test
//	public void testStringSet() throws Exception {
//		testAdd(ValueType.STRING);
//	}
//
//	@Test
//	public void testIntegerSet() throws Exception {
//		testAdd(ValueType.INTEGER);
//	}
//
//	@Test
//	public void testLongSet() throws Exception {
//		testAdd(ValueType.LONG);
//	}
//
//	@Test
//	public void testFloatSet() throws Exception {
//		testAdd(ValueType.FLOAT);
//	}
//
//	@Test
//	public void testDoubleSet() throws Exception {
//		testAdd(ValueType.DOUBLE);
//	}
//
//	@Test
//	public void testEnumSet() throws Exception {
//		testAdd(ValueType.ENUM);
//	}
//
//	@Test
//	public void testExtensionSet() throws Exception {
//		testAdd(ValueType.EXTENSION);
//	}
//
//	@Test
//	public void testImageSet() throws Exception {
//		testAdd(ValueType.IMAGE);
//	}
//
//	@Test
//	public void testImageResourceSet() throws Exception {
//		testAdd(ValueType.IMAGE_RESOURCE);
//	}
//
//	@Test
//	public void testBooleanSet() throws Exception {
//		testAdd(ValueType.BOOLEAN);
//	}
//
//	@Test
//	public void testUrlSet() throws Exception {
//		testAdd(ValueType.URL);
//	}
//
//	@Test
//	public void testUrlResourceSet() throws Exception {
//		testAdd(ValueType.URL_RESOURCE);
//	}
//
//	@Test
//	public void testUnknownSet() throws Exception {
//		testAdd(ValueType.UNKNOWN);
//	}
//
//	@Test
//	public void testCustomSet() throws Exception {
//		testAdd(ValueType.CUSTOM);
//	}
//
//	//SERIALIZATION
//
//	@Test
//	public void testXmlStringSet() throws Exception {
//		testXml(ValueType.STRING);
//	}
//
//	@Test
//	public void testXmlIntegerSet() throws Exception {
//		testXml(ValueType.INTEGER);
//	}
//
//	@Test
//	public void testXmlLongSet() throws Exception {
//		testXml(ValueType.LONG);
//	}
//
//	@Test
//	public void testXmlFloatSet() throws Exception {
//		testXml(ValueType.FLOAT);
//	}
//
//	@Test
//	public void testXmlDoubleSet() throws Exception {
//		testXml(ValueType.DOUBLE);
//	}
//
//	@Test
//	public void testXmlEnumSet() throws Exception {
//		testXml(ValueType.ENUM);
//	}
//
//	@Test
//	public void testXmlExtensionSet() throws Exception {
//		testXml(ValueType.EXTENSION);
//
//		// Use modified value type that bypasses the plugin engine
////		testXml(TestUtils.EXTENSION_TYPE);
//	}
//
//	@Test
//	public void testXmlImageSet() throws Exception {
//		testXml(ValueType.IMAGE);
//	}
//
//	@Test
//	public void testXmlImageResourceSet() throws Exception {
//		assertThrows(UnsupportedOperationException.class, () -> testXml(ValueType.IMAGE_RESOURCE));
//	}
//
//	@Test
//	public void testXmlBooleanSet() throws Exception {
//		testXml(ValueType.BOOLEAN);
//	}
//
//	@Test
//	public void testXmlUrlSet() throws Exception {
//		testXml(ValueType.URL);
//	}
//
//	@Test
//	public void testXmlUrlResourceSet() throws Exception {
//		assertThrows(UnsupportedOperationException.class, () -> testXml(ValueType.URL_RESOURCE));
//	}
//
//	@Test
//	public void testXmlUnknownSet() throws Exception {
//		assertThrows(UnsupportedOperationException.class, () -> testXml(ValueType.UNKNOWN));
//	}
//
//	@Test
//	public void testXmlCustomSet() throws Exception {
//		assertThrows(UnsupportedOperationException.class, () -> testXml(ValueType.CUSTOM));
//	}
}
