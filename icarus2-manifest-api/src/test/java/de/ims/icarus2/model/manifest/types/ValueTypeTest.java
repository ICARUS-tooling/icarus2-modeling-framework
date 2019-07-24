/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.model.manifest.types;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.assertManifestException;
import static de.ims.icarus2.test.TestUtils.assertCollectionNotEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.jupiter.api.DynamicTest.stream;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.types.ValueType.MatrixType;
import de.ims.icarus2.model.manifest.types.ValueType.VectorType;

/**
 * Test suite for {@link ValueType}
 *
 * @author Markus Gärtner
 *
 */
class ValueTypeTest {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#parseValueType(java.lang.String)}.
	 */
	@TestFactory
	Stream<DynamicTest> testParseValueType() {
		return ValueType.valueTypes()
				.stream()
				.flatMap(valueType -> Stream.of(
						DynamicTest.dynamicTest(valueType.getName(), () -> {
							assertEquals(valueType, ValueType.parseValueType(valueType.getStringValue()));
						}),
						DynamicTest.dynamicTest(valueType.getName()+" as vector", () -> {
							VectorType vectorType1 = VectorType.withUndefinedSize(valueType);
							assertEquals(vectorType1, ValueType.parseValueType(vectorType1.getStringValue()));

							VectorType vectorType2 = VectorType.withSize(valueType, 5);
							assertEquals(vectorType2, ValueType.parseValueType(vectorType2.getStringValue()));
						}),
						DynamicTest.dynamicTest(valueType.getName()+" as matrix", () -> {
							MatrixType matrixType = MatrixType.withSize(valueType, 4, 7);
							assertEquals(matrixType, ValueType.parseValueType(matrixType.getStringValue()));
						})));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#toChars(java.lang.Object)}.
	 */
	@TestFactory
	Stream<DynamicTest> testToChars() {
		return ValueType.serializableValueTypes()
				.stream()
				.filter(ManifestTestUtils::hasTestValues)
				.map(valueType -> dynamicTest(valueType.getName(), () -> {
					Object value = ManifestTestUtils.getTestValue(valueType);
					assertNotNull(valueType.toChars(value));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#parse(java.lang.CharSequence, java.lang.ClassLoader)}.
	 */
	@TestFactory
	Stream<DynamicTest> testParse() {
		return ValueType.serializableValueTypes()
				.stream()
				.filter(ManifestTestUtils::hasTestValues)
				.map(valueType -> dynamicTest(valueType.getName(), () -> {
					Object value = ManifestTestUtils.getTestValue(valueType);
					CharSequence serializedForm = valueType.toChars(value);
					Object parsedValue = valueType.parse(serializedForm, valueType.getClass().getClassLoader());
					assertNotNull(parsedValue);
					assertEquals(value, parsedValue);
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#persist(java.lang.Object)}.
	 */
	@TestFactory
	Stream<DynamicTest> testPersist() {
		return ValueType.serializableValueTypes()
				.stream()
				.filter(ManifestTestUtils::hasTestValues)
				.map(valueType -> dynamicTest(valueType.getName(), () -> {
					Object value = ManifestTestUtils.getTestValue(valueType);
					assertEquals(value, valueType.persist(value));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#parseAndPersist(java.lang.CharSequence, java.lang.ClassLoader)}.
	 */
	@TestFactory
	Stream<DynamicTest> testParseAndPersist() {
		return ValueType.serializableValueTypes()
				.stream()
				.filter(ManifestTestUtils::hasTestValues)
				.map(valueType -> dynamicTest(valueType.getName(), () -> {
					Object value = ManifestTestUtils.getTestValue(valueType);
					Object reparsedValue = valueType.parseAndPersist(valueType.toChars(value), valueType.getClass().getClassLoader());
					assertEquals(value, reparsedValue);
					assertEquals(reparsedValue, valueType.persist(reparsedValue));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#getStringValue()}.
	 */
	@TestFactory
	Stream<DynamicTest> testGetStringValue() {
		return DynamicTest.stream(ValueType.valueTypes().iterator(),
				valueType -> valueType.getClass().getName(),
				valueType -> assertNotNull(valueType.getStringValue()));

	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#getName()}.
	 */
	@TestFactory
	Stream<DynamicTest> testGetName() {
		return stream(ValueType.valueTypes().iterator(),
				valueType -> valueType.getClass().getName(),
				valueType -> assertNotNull(valueType.getName()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#getBaseClass()}.
	 */
	@TestFactory
	Stream<DynamicTest> testGetBaseClass() {
		return stream(ValueType.valueTypes().iterator(),
				valueType -> valueType.getClass().getName(),
				valueType -> assertNotNull(valueType.getBaseClass()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#isValidValue(java.lang.Object)}.
	 */
	@TestFactory
	Stream<DynamicTest> testIsValidValue() {
		return stream(ValueType.valueTypes().iterator(),
				ValueType::getName,
				valueType -> {
					Object value = ManifestTestUtils.getOrMockTestValue(valueType);
					assertTrue(valueType.isValidValue(value));

					if(!Object.class.equals(valueType.getBaseClass())) {
						Object invalidValue = mock(ManifestTestUtils.Dummy.class);
						assertFalse(valueType.isValidValue(invalidValue));
					}
				});
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#isValidType(java.lang.Class)}.
	 */
	@TestFactory
	Stream<DynamicTest> testIsValidType() {
		return stream(ValueType.valueTypes().iterator(),
				ValueType::getName,
				valueType -> {
					assertTrue(valueType.isValidType(valueType.getBaseClass()));

					/*
					 *  For any more specific type we need to check that arbitrary
					 *  foreign classes don't get accepted
					 */
					if(!Object.class.equals(valueType.getBaseClass())) {
						assertFalse(valueType.isValidType(ManifestTestUtils.Dummy.class));
					}
				});
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#valueTypes()}.
	 */
	@Test
	void testValueTypes() {
		assertCollectionNotEmpty(ValueType.valueTypes());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#basicValueTypes()}.
	 */
	@Test
	void testBasicValueTypes() {
		Collection<ValueType> types = ValueType.basicValueTypes();
		assertCollectionNotEmpty(types);

		types.forEach(type -> assertTrue(type.isBasicType(), "Not a proper basic type: "+type));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#serializableValueTypes()}.
	 */
	@Test
	void testSserializableValueTypes() {
		Collection<ValueType> types = ValueType.serializableValueTypes();
		assertCollectionNotEmpty(types);

		types.forEach(type -> assertTrue(type.isSerializable(), "Not a proper serializable type: "+type));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#checkValue(java.lang.Object)}.
	 */
	@TestFactory
	Stream<DynamicTest> testCheckValue() {
		return stream(ValueType.valueTypes().iterator(),
				ValueType::getName,
				valueType -> {
					Object validValue = ManifestTestUtils.getOrMockTestValue(valueType);
					valueType.checkValue(validValue);

					if(!Object.class.equals(valueType.getBaseClass())) {
						Object invalidValue = mock(ManifestTestUtils.Dummy.class);
						assertManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
								() -> valueType.checkValue(invalidValue),
								"Checking invalid type for "+valueType);
					}
				});
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#checkValues(java.util.Collection)}.
	 */
	@TestFactory
	Stream<DynamicTest> testCheckValuesCollectionOfQ() {
		return stream(ValueType.valueTypes().iterator(),
				ValueType::getName,
				valueType -> {
					Object validValue = ManifestTestUtils.getOrMockTestValue(valueType);
					valueType.checkValues(Collections.singleton(validValue));
					valueType.checkValues(Arrays.asList(validValue, validValue, validValue));

					if(!Object.class.equals(valueType.getBaseClass())) {
						Object invalidValue = mock(ManifestTestUtils.Dummy.class);

						// Test with various combinations
						assertManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
								() -> valueType.checkValues(Collections.singleton(invalidValue)),
								"Checking invalid values (singleton) for "+valueType);

						assertManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
								() -> valueType.checkValues(Arrays.asList(invalidValue, invalidValue)),
								"Checking invalid values (2 invalid) for "+valueType);

						assertManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
								() -> valueType.checkValues(Arrays.asList(validValue, invalidValue, validValue)),
								"Checking invalid values (inv,val,inv) for "+valueType);
					}
				});
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#checkValues(java.lang.Object[])}.
	 */
	@TestFactory
	Stream<DynamicTest> testCheckValuesObjectArray() {
		return stream(ValueType.valueTypes().iterator(),
				ValueType::getName,
				valueType -> {
					Object validValue = ManifestTestUtils.getOrMockTestValue(valueType);
					valueType.checkValues(new Object[] {validValue});
					valueType.checkValues(new Object[] {validValue, validValue, validValue});

					if(!Object.class.equals(valueType.getBaseClass())) {
						Object invalidValue = mock(ManifestTestUtils.Dummy.class);

						// Test with various combinations
						assertManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
								() -> valueType.checkValues(new Object[] {invalidValue}),
								"Checking invalid values (singleton) for "+valueType);

						assertManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
								() -> valueType.checkValues(new Object[] {invalidValue, invalidValue}),
								"Checking invalid values (2 invalid) for "+valueType);

						assertManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
								() -> valueType.checkValues(new Object[] {validValue, invalidValue, validValue}),
								"Checking invalid values (inv,val,inv) for "+valueType);
					}
				});
	}

	@Nested
	class VectorTypeTest {
		//TODO
	}

	@Nested
	class MatrixTypeTest {
		//TODO
	}
}
