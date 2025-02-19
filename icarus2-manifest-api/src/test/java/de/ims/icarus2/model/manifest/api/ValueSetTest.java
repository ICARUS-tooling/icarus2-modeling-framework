/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.test.TestUtils.INDEX_OUT_OF_BOUNDS_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.assertAccumulativeArrayGetter;
import static de.ims.icarus2.test.TestUtils.assertAccumulativeCount;
import static de.ims.icarus2.test.TestUtils.assertAccumulativeGetter;
import static de.ims.icarus2.test.TestUtils.assertAccumulativeLookup;
import static de.ims.icarus2.test.TestUtils.assertListIndexOf;
import static de.ims.icarus2.test.TestUtils.settings;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.guard.ApiGuard;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.collections.ArrayUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface ValueSetTest extends LockableTest<ValueSet>, TypedManifestTest<ValueSet> {

	/**
	 * @see de.ims.icarus2.model.manifest.ManifestApiTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
	 */
	@Override
	default void configureApiGuard(ApiGuard<ValueSet> apiGuard) {
		LockableTest.super.configureApiGuard(apiGuard);

		/*
		 *  For the addAll(Class) method we need to provide an actual
		 *  enum class as the APiGuard can't mock final classes.
		 *  We simply use the ManifestType enum here, which is as good
		 *  as any other...
		 */
		apiGuard.parameterResolver(Class.class, set -> ManifestType.class);
	}
	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	default ManifestType getExpectedType() {
		return ManifestType.VALUE_SET;
	}

	@Provider
	ValueSet createWithType(TestSettings settings, ValueType valueType);

	/**
	 * @see de.ims.icarus2.test.GenericTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default ValueSet createTestInstance(TestSettings settings) {
		return createWithType(settings, ValueType.STRING);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueSet#getValuesAsSet()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetValuesAsSet() {
		return ManifestTestUtils.getAvailableTestTypes()
			.stream()
			.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
					Object[] values = ManifestTestUtils.getTestValues(valueType);
					assertAccumulativeArrayGetter(createWithType(settings(), valueType),
							values[0], values[1],
							ValueSet::getValues,
							ValueSet::addValue);
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueSet#getValuesAsList()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetValuesAsList() {
		return ManifestTestUtils.getAvailableTestTypes()
				.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						Object[] values = ManifestTestUtils.getTestValues(valueType);
						ValueSet instance = createWithType(settings(), valueType);
						assertAccumulativeGetter(instance,
								values[0], values[1],
								ValueSet::getValuesAsList,
								ValueSet::addValue);

						assertThat(instance.getValuesAsList()).containsExactly(values[0], values[1]);
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueSet#valueCount()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testValueCount() {
		return ManifestTestUtils.getAvailableTestTypes()
				.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						assertAccumulativeCount(createWithType(settings(), valueType),
								ValueSet::addValue,
								ValueSet::removeValue,
								ValueSet::valueCount,
								ManifestTestUtils.getTestValues(valueType));
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueSet#getValueAt(int)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default Stream<DynamicTest> testGetValueAt() {
		return ManifestTestUtils.getAvailableTestTypes()
				.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						Object[] values = ManifestTestUtils.getTestValues(valueType);
						assertAccumulativeLookup(createWithType(settings(), valueType),
								values[0], values[1],
								ValueSet::getValueAt,
								NPE_CHECK, INDEX_OUT_OF_BOUNDS_CHECK,
								ValueSet::addValue,
								k -> ArrayUtils.indexOf(values, k),
								2, 3, -1);
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueSet#forEach(java.util.function.Consumer)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEach() {
		return ManifestTestUtils.getAvailableTestTypes()
				.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						Object[] values = ManifestTestUtils.getTestValues(valueType);
						TestUtils.<ValueSet, Object>assertForEach(
								createWithType(settings(), valueType),
								values[0], values[1],
								ValueSet::forEach,
								ValueSet::addValue);
					}));
	}

	/**
	 * Test method for {@link ValueSet#forEachUntil(java.util.function.Predicate)}
	 * @return
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachUntil() {
		return ManifestTestUtils.getAvailableTestTypes()
				.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						TestUtils.<ValueSet, Object>assertForEachUntil(
								createWithType(settings(), valueType),
								ValueSet::forEachUntil,
								ValueSet::addValue,
								ManifestTestUtils.getTestValues(valueType));
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueSet#getValueType()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetValueType() {
		return ManifestTestUtils.getAvailableTestTypes()
				.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						assertEquals(valueType, createWithType(settings(), valueType).getValueType());
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueSet#indexOfValue(java.lang.Object)}.
	 */
	@TestFactory
	@RandomizedTest
	default Stream<DynamicTest> testIndexOfValue(RandomGenerator rand) {
		return ManifestTestUtils.getAvailableTestTypes()
				.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						rand.reset(); // enforces same conditions for every test
						assertListIndexOf(createWithType(settings(), valueType),
								ValueSet::addValue,
								ValueSet::removeValue,
								ValueSet::indexOfValue,
								rand,
								ManifestTestUtils.getTestValues(valueType));
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueSet#addValue(java.lang.Object)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testAddValueObject() {
		return ManifestTestUtils.getAvailableTestTypes()
				.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						TestSettings settings = settings();
						Object[] illegalValues = ManifestTestUtils.getIllegalValues(valueType);
						LockableTest.assertLockableAccumulativeAdd(
								settings, createWithType(settings(), valueType),
								ValueSet::addValue,
								illegalValues,
								illegalValues[0]==null ? NO_CHECK : ManifestTestUtils.TYPE_CAST_CHECK,
								NPE_CHECK, NO_CHECK,
								ManifestTestUtils.getTestValues(valueType));
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueSet#addValue(java.lang.Object, int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testAddValueObjectInt() {
		return ManifestTestUtils.getAvailableTestTypes()
				.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						TestSettings settings = settings();
						LockableTest.assertLockableListInsertAt(
								settings, createWithType(settings, valueType),
								ValueSet::addValue,
								ValueSet::getValueAt,
								ManifestTestUtils.getTestValues(valueType));
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueSet#removeValue(int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testRemoveValue() {
		return ManifestTestUtils.getAvailableTestTypes()
				.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						TestSettings settings = settings();
						LockableTest.assertLockableListRemoveAt(
								settings, createWithType(settings, valueType),
								ValueSet::addValue,
								ValueSet::removeValueAt,
								ValueSet::getValueAt,
								ManifestTestUtils.getTestValues(valueType));
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueSet#removeValue(Object)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testRemoveValueObject() {
		return ManifestTestUtils.getAvailableTestTypes()
				.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						TestSettings settings = settings();
						LockableTest.assertLockableAccumulativeRemove(
								settings, createWithType(settings, valueType),
								ValueSet::addValue,
								ValueSet::removeValue,
								ValueSet::getValuesAsSet,
								NPE_CHECK, ManifestTestUtils.INVALID_INPUT_CHECK,
								ManifestTestUtils.getTestValues(valueType));
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueSet#removeAllValues()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testRemoveAllValues() {
		return ManifestTestUtils.getAvailableTestTypes()
				.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						ValueSet instance = createWithType(settings(), valueType);

						Stream.of(ManifestTestUtils.getTestValues(valueType))
							.forEach(instance::addValue);

						assertTrue(instance.valueCount()>0);

						instance.removeAllValues();

						assertTrue(instance.valueCount()==0);

						instance.lock();

						LockableTest.assertLocked(() -> instance.removeAllValues());
					}));
	}

}
