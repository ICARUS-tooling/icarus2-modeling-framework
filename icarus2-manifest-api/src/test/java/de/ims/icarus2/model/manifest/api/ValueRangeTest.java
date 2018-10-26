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
/**
 *
 */
package de.ims.icarus2.model.manifest.api;

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

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.types.UnsupportedValueTypeException;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface ValueRangeTest<R extends ValueRange>
		extends LockableTest<R>, TypedManifestTest<R> {

	public static final Set<ValueType> LEGAL_VALUE_TYPES = ValueRange.SUPPORTED_VALUE_TYPES;
	public static final Set<ValueType> ILLEGAL_VALUE_TYPES = Collections.unmodifiableSet(
			ValueType.filterWithout(LEGAL_VALUE_TYPES::contains));

	public static final ValueType DEFAULT_VALUE_TYPE = ValueType.INTEGER;

	@Provider
	R createWithType(TestSettings settings, ValueType valueType);

	/**
	 * @see de.ims.icarus2.test.GenericTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default R createTestInstance(TestSettings settings) {
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
	@Test
	default void testGetLowerBound() {
		for(ValueType valueType : LEGAL_VALUE_TYPES) {
			Object[] values = ManifestTestUtils.getTestValues(valueType);
			assertOptGetter(createWithType(settings(), valueType),
					(Comparable<?>)values[0], (Comparable<?>)values[1],
					NO_DEFAULT(),
					ValueRange::getLowerBound,
					ValueRange::setLowerBound);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#getUpperBound()}.
	 */
	@Test
	default void testGetUpperBound() {
		for(ValueType valueType : LEGAL_VALUE_TYPES) {
			Object[] values = ManifestTestUtils.getTestValues(valueType);
			assertOptGetter(createWithType(settings(), valueType),
					(Comparable<?>)values[0], (Comparable<?>)values[1],
					NO_DEFAULT(),
					ValueRange::getUpperBound,
					ValueRange::setUpperBound);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#getStepSize()}.
	 */
	@Test
	default void testGetStepSize() {
		for(ValueType valueType : LEGAL_VALUE_TYPES) {
			Object[] values = ManifestTestUtils.getTestValues(valueType);
			assertOptGetter(createWithType(settings(), valueType),
					(Comparable<?>)values[0], (Comparable<?>)values[1],
					NO_DEFAULT(),
					ValueRange::getStepSize,
					ValueRange::setStepSize);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#isLowerBoundInclusive()}.
	 */
	@Test
	default void testIsLowerBoundInclusive() {
		assertGetter(createUnlocked(),
				Boolean.TRUE, Boolean.FALSE,
				DEFAULT(Boolean.valueOf(ValueRange.DEFAULT_LOWER_INCLUSIVE_VALUE)),
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
				DEFAULT(Boolean.valueOf(ValueRange.DEFAULT_UPPER_INCLUSIVE_VALUE)),
				ValueRange::isUpperBoundInclusive,
				ValueRange::setUpperBoundInclusive);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#getValueType()}.
	 */
	@Test
	default void testGetValueType() {
		for(ValueType valueType : LEGAL_VALUE_TYPES) {
			assertEquals(valueType, createWithType(settings(), valueType).getValueType());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#setLowerBound(java.lang.Object)}.
	 */
	@Test
	default void testSetLowerBound() {
		assertLockableSetter(settings(),
				ValueRange::setLowerBound,
				ManifestTestUtils.getTestValue(DEFAULT_VALUE_TYPE),
				NPE_CHECK, TYPE_CAST_CHECK,
				ManifestTestUtils.getIllegalValue(DEFAULT_VALUE_TYPE));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#setUpperBound(java.lang.Object)}.
	 */
	@Test
	default void testSetUpperBound() {
		assertLockableSetter(settings(),
				ValueRange::setUpperBound,
				ManifestTestUtils.getTestValue(DEFAULT_VALUE_TYPE),
				NPE_CHECK, TYPE_CAST_CHECK,
				ManifestTestUtils.getIllegalValue(DEFAULT_VALUE_TYPE));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueRange#setStepSize(java.lang.Object)}.
	 */
	@Test
	default void testSetStepSize() {
		assertLockableSetter(settings(),
				ValueRange::setStepSize,
				ManifestTestUtils.getTestValue(DEFAULT_VALUE_TYPE),
				NPE_CHECK, TYPE_CAST_CHECK,
				ManifestTestUtils.getIllegalValue(DEFAULT_VALUE_TYPE));
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


	@Test
	default void testUnsupportedValueTypes() {
		for(ValueType valueType : ILLEGAL_VALUE_TYPES) {
			UnsupportedValueTypeException exception = assertThrows(UnsupportedValueTypeException.class,
					() -> createWithType(settings(), valueType));

			assertEquals(ManifestErrorCode.MANIFEST_UNSUPPORTED_TYPE, exception.getErrorCode());
			assertEquals(valueType, exception.getValueType());
		}
	}
}
