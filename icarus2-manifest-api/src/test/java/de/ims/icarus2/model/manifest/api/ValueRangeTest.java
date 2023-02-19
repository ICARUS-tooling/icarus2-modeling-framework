/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.api;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.assertManifestException;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.getTestValue;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockExpression;
import static de.ims.icarus2.test.TestUtils.DEFAULT;
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.assertGetter;
import static de.ims.icarus2.test.TestUtils.assertOptGetter;
import static de.ims.icarus2.test.TestUtils.settings;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.types.UnsupportedValueTypeException;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.test.guard.ApiGuard;
import de.ims.icarus2.util.eval.Expression;

/**
 * @author Markus Gärtner
 *
 */
public interface ValueRangeTest
		extends LockableTest<ValueRange>, TypedManifestTest<ValueRange> {

	public static final Set<ValueType> LEGAL_VALUE_TYPES = ValueRange.SUPPORTED_VALUE_TYPES;
	public static final Set<ValueType> ILLEGAL_VALUE_TYPES = Collections.unmodifiableSet(
			ValueType.filterWithout(LEGAL_VALUE_TYPES::contains));

	public static final ValueType DEFAULT_VALUE_TYPE = ValueType.INTEGER;

	@Provider
	ValueRange createWithType(TestSettings settings, ValueType valueType);

	/**
	 * @see de.ims.icarus2.model.manifest.ManifestApiTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
	 */
	@Override
	default void configureApiGuard(ApiGuard<ValueRange> apiGuard) {
		LockableTest.super.configureApiGuard(apiGuard);

		apiGuard.defaultReturnValue("lowerBoundInclusive",
				Boolean.valueOf(ValueRange.DEFAULT_LOWER_INCLUSIVE_VALUE));
		apiGuard.defaultReturnValue("upperBoundInclusive",
				Boolean.valueOf(ValueRange.DEFAULT_UPPER_INCLUSIVE_VALUE));

		apiGuard.parameterResolver(Object.class,
				valueRange -> getTestValue(valueRange.getValueType()));
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default ValueRange createTestInstance(TestSettings settings) {
		return createWithType(settings, DEFAULT_VALUE_TYPE);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	default ManifestType getExpectedType() {
		return ManifestType.VALUE_RANGE;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#getLowerBound()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetLowerBound() {
		return LEGAL_VALUE_TYPES.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						Object[] values = ManifestTestUtils.getTestValues(valueType);
						assertOptGetter(createWithType(settings(), valueType),
								(Comparable<?>)values[0], (Comparable<?>)values[1],
								NO_DEFAULT(),
								ValueRange::getLowerBound,
								ValueRange::setLowerBound);
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#getLowerBoundComparable()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetLowerBoundComparable() {
		return LEGAL_VALUE_TYPES.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						Object[] values = ManifestTestUtils.getTestValues(valueType);
						assertOptGetter(createWithType(settings(), valueType),
								(Comparable<?>)values[0], (Comparable<?>)values[1],
								NO_DEFAULT(),
								ValueRange::getLowerBoundComparable,
								ValueRange::setLowerBound);
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#getLowerBoundComparable()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetLowerBoundComparable_Exception() {
		return LEGAL_VALUE_TYPES.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						Expression expression = mockExpression(valueType);
						ValueRange valueRange = createWithType(settings(), valueType);
						valueRange.setLowerBound(expression);

						assertManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
								() -> valueRange.getLowerBoundComparable());
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#getUpperBound()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetUpperBound() {
		return LEGAL_VALUE_TYPES.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						Object[] values = ManifestTestUtils.getTestValues(valueType);
						assertOptGetter(createWithType(settings(), valueType),
								(Comparable<?>)values[0], (Comparable<?>)values[1],
								NO_DEFAULT(),
								ValueRange::getUpperBound,
								ValueRange::setUpperBound);
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#getUpperBoundComparable()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetUpperBoundComparable() {
		return LEGAL_VALUE_TYPES.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						Object[] values = ManifestTestUtils.getTestValues(valueType);
						assertOptGetter(createWithType(settings(), valueType),
								(Comparable<?>)values[0], (Comparable<?>)values[1],
								NO_DEFAULT(),
								ValueRange::getUpperBoundComparable,
								ValueRange::setUpperBound);
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#getUpperBoundComparable()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetUpperBoundComparable_Exception() {
		return LEGAL_VALUE_TYPES.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						Expression expression = mockExpression(valueType);
						ValueRange valueRange = createWithType(settings(), valueType);
						valueRange.setUpperBound(expression);

						assertManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
								() -> valueRange.getUpperBoundComparable());
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#getStepSize()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetStepSize() {
		return LEGAL_VALUE_TYPES.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						Object[] values = ManifestTestUtils.getTestValues(valueType);
						assertOptGetter(createWithType(settings(), valueType),
								(Comparable<?>)values[0], (Comparable<?>)values[1],
								NO_DEFAULT(),
								ValueRange::getStepSize,
								ValueRange::setStepSize);
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#getStepSizeComparable()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetStepSizeComparable() {
		return LEGAL_VALUE_TYPES.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						Object[] values = ManifestTestUtils.getTestValues(valueType);
						assertOptGetter(createWithType(settings(), valueType),
								(Comparable<?>)values[0], (Comparable<?>)values[1],
								NO_DEFAULT(),
								ValueRange::getStepSizeComparable,
								ValueRange::setStepSize);
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#getStepSizeComparable()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetStepSizeComparable_Exception() {
		return LEGAL_VALUE_TYPES.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						Expression expression = mockExpression(valueType);
						ValueRange valueRange = createWithType(settings(), valueType);
						valueRange.setStepSize(expression);

						assertManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
								() -> valueRange.getStepSizeComparable());
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#isLowerBoundInclusive()}.
	 */
	@Test
	default void testIsLowerBoundInclusive() {
		assertGetter(createUnlocked(),
				Boolean.TRUE, Boolean.FALSE,
				DEFAULT(ValueRange.DEFAULT_LOWER_INCLUSIVE_VALUE),
				ValueRange::isLowerBoundInclusive,
				ValueRange::setLowerBoundInclusive);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#isUpperBoundInclusive()}.
	 */
	@Test
	default void testIsUpperBoundInclusive() {
		assertGetter(createUnlocked(),
				Boolean.TRUE, Boolean.FALSE,
				DEFAULT(ValueRange.DEFAULT_UPPER_INCLUSIVE_VALUE),
				ValueRange::isUpperBoundInclusive,
				ValueRange::setUpperBoundInclusive);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#getValueType()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetValueType() {
		return LEGAL_VALUE_TYPES.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						assertEquals(valueType, createWithType(settings(), valueType).getValueType());
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#setLowerBound(java.lang.Object)}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	default void testSetLowerBound() {
		assertLockableSetter(settings(),
				ValueRange::setLowerBound,
				(Comparable<?>)ManifestTestUtils.getTestValue(DEFAULT_VALUE_TYPE),
				NPE_CHECK, ManifestTestUtils.TYPE_CAST_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#setUpperBound(java.lang.Object)}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	default void testSetUpperBound() {
		assertLockableSetter(settings(),
				ValueRange::setUpperBound,
				(Comparable<?>)ManifestTestUtils.getTestValue(DEFAULT_VALUE_TYPE),
				NPE_CHECK, ManifestTestUtils.TYPE_CAST_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#setStepSize(java.lang.Object)}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	default void testSetStepSize() {
		assertLockableSetter(settings(),
				ValueRange::setStepSize,
				(Comparable<?>)ManifestTestUtils.getTestValue(DEFAULT_VALUE_TYPE),
				NPE_CHECK, ManifestTestUtils.TYPE_CAST_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#setLowerBoundInclusive(boolean)}.
	 */
	@Test
	default void testSetLowerBoundInclusive() {
		assertLockableSetter(settings(), ValueRange::setLowerBoundInclusive);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#setUpperBoundInclusive(boolean)}.
	 */
	@Test
	default void testSetUpperBoundInclusive() {
		assertLockableSetter(settings(), ValueRange::setUpperBoundInclusive);
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
