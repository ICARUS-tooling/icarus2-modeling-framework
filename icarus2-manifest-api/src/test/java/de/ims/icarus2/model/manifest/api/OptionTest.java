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
package de.ims.icarus2.model.manifest.api;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.getIllegalValue;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.getTestValue;
import static de.ims.icarus2.test.TestUtils.DEFAULT;
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.settings;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.api.OptionsManifest.Option;
import de.ims.icarus2.model.manifest.types.UnsupportedValueTypeException;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface OptionTest extends ModifiableIdentityTest<Option>, LockableTest<Option>, TypedManifestTest<Option> {

	public static final BiConsumer<Executable, String> UNSUPPORTED_TYPE_CHECK = (ex, msg) -> {
		UnsupportedValueTypeException exception = assertThrows(UnsupportedValueTypeException.class, ex, msg);
		assertEquals(ManifestErrorCode.MANIFEST_UNSUPPORTED_TYPE, exception.getErrorCode());
	};

	public static final Set<ValueType> LEGAL_VALUE_TYPES = Option.SUPPORTED_VALUE_TYPES;
	public static final Set<ValueType> ILLEGAL_VALUE_TYPES = Collections
			.unmodifiableSet(ValueType.filterWithout(LEGAL_VALUE_TYPES::contains));

	public static ValueSet mockValueSet(ValueType valueType) {
		ValueSet valueSet = mock(ValueSet.class);
		when(valueSet.getValueType()).thenReturn(valueType);
		return valueSet;
	}

	public static ValueRange mockValueRange(ValueType valueType) {
		ValueRange valueSet = mock(ValueRange.class);
		when(valueSet.getValueType()).thenReturn(valueType);
		return valueSet;
	}

	@Provider
	Option createWithType(TestSettings settings, ValueType valueType);

	/**
	 * @see de.ims.icarus2.test.GenericTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	@Provider
	default Option createTestInstance(TestSettings settings) {
		return createWithType(settings, ValueType.STRING);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	default ManifestType getExpectedType() {
		return ManifestType.OPTION;
	}

	/**
	 * Test method for
	 * {@link de.ims.icarus2.model.manifest.api.OptionsManifest.Option#getDefaultValue()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetDefaultValue() {
		return LEGAL_VALUE_TYPES.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						Object[] values = ManifestTestUtils.getTestValues(valueType);
						TestUtils.assertOptGetter(createWithType(settings(), valueType),
								values[0], values[1], NO_DEFAULT(),
								Option::getDefaultValue,
								Option::setDefaultValue);
					}));
	}

	/**
	 * Test method for
	 * {@link de.ims.icarus2.model.manifest.api.OptionsManifest.Option#getValueType()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetValueType() {
		return LEGAL_VALUE_TYPES.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						TestUtils.assertGetter(createWithType(settings(), valueType),
								valueType, valueType,
								DEFAULT(valueType),
								Option::getValueType,
								Option::setValueType);
					}));
	}

	/**
	 * Test method for
	 * {@link de.ims.icarus2.model.manifest.api.OptionsManifest.Option#getSupportedValues()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetSupportedValues() {
		return LEGAL_VALUE_TYPES.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						TestUtils.assertOptGetter(createWithType(settings(), valueType),
								mockValueSet(valueType), mockValueSet(valueType), NO_DEFAULT(),
								Option::getSupportedValues, Option::setSupportedValues);
					}));
	}

	/**
	 * Test method for
	 * {@link de.ims.icarus2.model.manifest.api.OptionsManifest.Option#getSupportedRange()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetSupportedRange() {
		return LEGAL_VALUE_TYPES.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						TestUtils.assertOptGetter(createWithType(settings(), valueType),
								mockValueRange(valueType), mockValueRange(valueType),
								NO_DEFAULT(),
								Option::getSupportedRange, Option::setSupportedRange);
					}));
	}

	/**
	 * Test method for
	 * {@link de.ims.icarus2.model.manifest.api.OptionsManifest.Option#getExtensionPointUid()}.
	 */
	@Test
	default void testGetExtensionPointUid() {
		TestUtils.assertOptGetter(createWithType(settings(), ValueType.EXTENSION),
				"uid1", "uid2", NO_DEFAULT(),
				Option::getExtensionPointUid, Option::setExtensionPointUid);
	}

	/**
	 * Test method for
	 * {@link de.ims.icarus2.model.manifest.api.OptionsManifest.Option#getOptionGroupId()}.
	 */
	@Test
	default void testGetOptionGroupId() {
		TestUtils.assertOptGetter(createUnlocked(),
				"group1", "group2", NO_DEFAULT(),
				Option::getOptionGroupId, Option::setOptionGroup);
	}

	/**
	 * Test method for
	 * {@link de.ims.icarus2.model.manifest.api.OptionsManifest.Option#isPublished()}.
	 */
	@SuppressWarnings("boxing")
	@Test
	default void testIsPublished() {
		TestUtils.assertFlagGetter(createUnlocked(),
				Option.DEFAULT_PUBLISHED_VALUE, Option::isPublished, Option::setPublished);
	}

	/**
	 * Test method for
	 * {@link de.ims.icarus2.model.manifest.api.OptionsManifest.Option#isMultiValue()}.
	 */
	@SuppressWarnings("boxing")
	@Test
	default void testIsMultiValue() {
		TestUtils.assertFlagGetter(createUnlocked(),
				Option.DEFAULT_MULTIVALUE_VALUE, Option::isMultiValue,
				Option::setMultiValue);
	}

	/**
	 * Test method for
	 * {@link de.ims.icarus2.model.manifest.api.OptionsManifest.Option#isAllowNull()}.
	 */
	@SuppressWarnings("boxing")
	@Test
	default void testIsAllowNull() {
		TestUtils.assertFlagGetter(createUnlocked(), Option.DEFAULT_ALLOW_NULL, Option::isAllowNull, Option::setAllowNull);
	}

	/**
	 * Test method for
	 * {@link de.ims.icarus2.model.manifest.api.OptionsManifest.Option#setAllowNull(boolean)}.
	 */
	@Test
	default void testSetAllowNull() {
		assertLockableSetter(settings(), Option::setAllowNull);
	}

	/**
	 * Test method for
	 * {@link de.ims.icarus2.model.manifest.api.OptionsManifest.Option#setMultiValue(boolean)}.
	 */
	@Test
	default void testSetMultiValue() {
		assertLockableSetter(settings(), Option::setMultiValue);
	}

	/**
	 * Test method for
	 * {@link de.ims.icarus2.model.manifest.api.OptionsManifest.Option#setPublished(boolean)}.
	 */
	@Test
	default void testSetPublished() {
		assertLockableSetter(settings(), Option::setPublished);
	}

	/**
	 * Test method for
	 * {@link de.ims.icarus2.model.manifest.api.OptionsManifest.Option#setExtensionPointUid(java.lang.String)}.
	 */
	@Test
	default void testSetExtensionPointUid() {
		assertLockableSetter(settings(), Option::setExtensionPointUid, "uid1", false, TestUtils.NO_CHECK);
	}

	/**
	 * Test method for
	 * {@link de.ims.icarus2.model.manifest.api.OptionsManifest.Option#setSupportedRange(de.ims.icarus2.model.manifest.api.ValueRange)}.
	 */
	@Test
	default void testSetSupportedRange() {
		assertLockableSetter(settings(), Option::setSupportedRange,
				mockValueRange(ValueType.STRING), false, TestUtils.NO_CHECK);
	}

	/**
	 * Test method for
	 * {@link de.ims.icarus2.model.manifest.api.OptionsManifest.Option#setSupportedValues(de.ims.icarus2.model.manifest.api.ValueSet)}.
	 */
	@Test
	default void testSetSupportedValues() {
		assertLockableSetter(settings(), Option::setSupportedValues,
				mockValueSet(ValueType.STRING), false, TestUtils.NO_CHECK);
	}

	/**
	 * Test method for
	 * {@link de.ims.icarus2.model.manifest.api.OptionsManifest.Option#setOptionGroup(java.lang.String)}.
	 */
	@Test
	default void testSetOptionGroup() {
		assertLockableSetterBatch(settings(), Option::setOptionGroup,
				ManifestTestUtils.getLegalIdValues(), true, INVALID_ID_CHECK,
				ManifestTestUtils.getIllegalIdValues());
	}

	/**
	 * Test method for
	 * {@link de.ims.icarus2.model.manifest.api.OptionsManifest.Option#setValueType(de.ims.icarus2.model.manifest.types.ValueType)}.
	 */
	@Test
	default void testSetValueType() {
		ValueType[] legalValues = LEGAL_VALUE_TYPES.toArray(new ValueType[0]);
		ValueType[] illegalValues = ILLEGAL_VALUE_TYPES.toArray(new ValueType[0]);

		assertLockableSetterBatch(settings(), Option::setValueType,
				legalValues, true, UNSUPPORTED_TYPE_CHECK, illegalValues);
	}

	/**
	 * Test method for
	 * {@link de.ims.icarus2.model.manifest.api.OptionsManifest.Option#setDefaultValue(java.lang.Object)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testSetDefaultValue() {
		return LEGAL_VALUE_TYPES.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						LockableTest.assertLockableSetter(settings(), createWithType(settings(), valueType),
								Option::setDefaultValue, getTestValue(valueType),
								false, TYPE_CAST_CHECK, getIllegalValue(valueType));
					}));
	}

	@TestFactory
	default Stream<DynamicTest> testUnsupportedValueTypes() {
		return ILLEGAL_VALUE_TYPES.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						UnsupportedValueTypeException exception = assertThrows(UnsupportedValueTypeException.class,
								() -> createWithType(settings(), valueType));

						assertEquals(ManifestErrorCode.MANIFEST_UNSUPPORTED_TYPE, exception.getErrorCode());
						assertEquals(valueType, exception.getValueType());
					}));
	}

}
