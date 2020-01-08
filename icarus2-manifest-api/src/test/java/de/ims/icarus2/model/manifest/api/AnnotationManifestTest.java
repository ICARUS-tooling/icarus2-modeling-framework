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
package de.ims.icarus2.model.manifest.api;

import static de.ims.icarus2.test.TestUtils.DEFAULT;
import static de.ims.icarus2.test.TestUtils.assertNotPresent;
import static de.ims.icarus2.test.TestUtils.assertPresent;
import static de.ims.icarus2.test.TestUtils.settings;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.guard.ApiGuard;
import de.ims.icarus2.util.data.ContentType;

/**
 * @author Markus Gärtner
 *
 */
public interface AnnotationManifestTest extends EmbeddedMemberManifestTest<AnnotationManifest> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	default ManifestType getExpectedType() {
		return ManifestType.ANNOTATION_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#getAllowedHostTypes()
	 */
	@Override
	default Set<ManifestType> getAllowedHostTypes() {
		return Collections.singleton(ManifestType.ANNOTATION_LAYER_MANIFEST);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.ManifestApiTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
	 */
	@Override
	default void configureApiGuard(ApiGuard<AnnotationManifest> apiGuard) {
		EmbeddedMemberManifestTest.super.configureApiGuard(apiGuard);

		apiGuard.defaultReturnValue("allowUnknownValues",
				Boolean.valueOf(AnnotationManifest.DEFAULT_ALLOW_UNKNOWN_VALUES));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getLayerManifest()}.
	 */
	@Test
	default void testGetLayerManifest() {
		assertPresent(createUnlocked().getLayerManifest());
		assertNotPresent(createTemplate(settings()).getLayerManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getKey()}.
	 */
	@Test
	default void testGetKey() {
		assertDerivativeOptGetter(settings(),
				"key1", "key2", TestUtils.NO_DEFAULT(),
				AnnotationManifest::getKey,
				AnnotationManifest::setKey);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalKey()}.
	 */
	@Test
	default void testIsLocalKey() {
		assertDerivativeIsLocal(settings(),
				"key1", "key2",
				AnnotationManifest::isLocalKey,
				AnnotationManifest::setKey);
	}


	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#forEachAlias(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachAlias() {
		EmbeddedMemberManifestTest.super.<String>assertDerivativeForEach(
				settings(),
				"alias1", "alias2",
				AnnotationManifest::forEachAlias,
				AnnotationManifest::addAlias);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalAlias(java.lang.String)}.
	 */
	@Test
	default void testIsLocalAlias() {
		assertDerivativeAccumulativeIsLocal(
				settings(),
				"alias1", "alias2",
				AnnotationManifest::isLocalAlias,
				AnnotationManifest::addAlias);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#forEachLocalAlias(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLocalAlias() {
		EmbeddedMemberManifestTest.super.<String>assertDerivativeForEachLocal(settings(),
				"alias1", "alias2",
				AnnotationManifest::forEachLocalAlias,
				AnnotationManifest::addAlias);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getAliases()}.
	 */
	@Test
	default void testGetAliases() {
		assertDerivativeAccumulativeGetter(settings(),
				"alias1", "alias2",
				AnnotationManifest::getAliases,
				AnnotationManifest::addAlias);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getLocalAliases()}.
	 */
	@Test
	default void testGetLocalAliases() {
		assertDerivativeAccumulativeLocalGetter(settings(),
				"alias1", "alias2",
				AnnotationManifest::getLocalAliases,
				AnnotationManifest::addAlias);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#isAllowUnknownValues()}.
	 */
	@SuppressWarnings("boxing")
	@Test
	default void testIsAllowUnknownValues() {
		assertDerivativeFlagGetter(settings(),
				AnnotationManifest.DEFAULT_ALLOW_UNKNOWN_VALUES,
				AnnotationManifest::isAllowUnknownValues,
				AnnotationManifest::setAllowUnknownValues);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getValueRange()}.
	 */
	@Test
	default void testGetValueRange() {
		assertDerivativeOptGetter(settings(),
				mock(ValueRange.class), mock(ValueRange.class), null,
				AnnotationManifest::getValueRange,
				AnnotationManifest::setValueRange);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalValueRange()}.
	 */
	@Test
	default void testIsLocalValueRange() {
		assertDerivativeIsLocal(settings(),
				mock(ValueRange.class), mock(ValueRange.class),
				AnnotationManifest::isLocalValueRange,
				AnnotationManifest::setValueRange);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getValueSet()}.
	 */
	@Test
	default void testGetValueSet() {
		assertDerivativeOptGetter(settings(),
				mock(ValueSet.class), mock(ValueSet.class),
				TestUtils.NO_DEFAULT(),
				AnnotationManifest::getValueSet,
				AnnotationManifest::setValueSet);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalValueSet()}.
	 */
	@Test
	default void testIsLocalValueSet() {
		assertDerivativeIsLocal(settings(), mock(ValueSet.class), mock(ValueSet.class),
				AnnotationManifest::isLocalValueSet, AnnotationManifest::setValueSet);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getValueType()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetValueType() {
		ValueType dummyType = mock(ValueType.class);
		return ManifestTestUtils.getAvailableTestTypes().stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
					assertDerivativeGetter(settings(),
							valueType, dummyType, DEFAULT(ValueType.STRING),
							AnnotationManifest::getValueType,
							AnnotationManifest::setValueType);
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalValueType()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIsLocalValueType() {
		ValueType dummyType = mock(ValueType.class);
		return ManifestTestUtils.getAvailableTestTypes().stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
					assertDerivativeIsLocal(settings(),
							valueType, dummyType,
							AnnotationManifest::isLocalValueType,
							AnnotationManifest::setValueType);
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getNoEntryValue()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetNoEntryValue() {
		return ManifestTestUtils.getAvailableTestTypes().stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						Object[] values = ManifestTestUtils.getTestValues(valueType);
						assertTrue(values.length>1, "Insufficient test values for type: "+valueType);
						Object value1 = values[0];
						Object value2 = values[1];

						assertDerivativeOptGetter(settings(), value1, value2, TestUtils.NO_DEFAULT(),
								AnnotationManifest::getNoEntryValue, AnnotationManifest::setNoEntryValue);
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalNoEntryValue()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIsLocalNoEntryValue() {
		return ManifestTestUtils.getAvailableTestTypes().stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						Object[] values = ManifestTestUtils.getTestValues(valueType);
						assertTrue(values.length>1, "Insufficient test valeus for type: "+valueType);
						Object value1 = values[0];
						Object value2 = values[1];

						assertDerivativeIsLocal(settings(), value1, value2,
								AnnotationManifest::isLocalNoEntryValue, AnnotationManifest::setNoEntryValue);
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getContentType()}.
	 */
	@Test
	default void testGetContentType() {
		assertDerivativeOptGetter(settings(),
				mock(ContentType.class), mock(ContentType.class),
				TestUtils.NO_DEFAULT(),
				AnnotationManifest::getContentType,
				AnnotationManifest::setContentType);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalContentType()}.
	 */
	@Test
	default void testIsLocalContentType() {
		assertDerivativeIsLocal(settings(),
				mock(ContentType.class), mock(ContentType.class),
				AnnotationManifest::isLocalContentType,
				AnnotationManifest::setContentType);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#setKey(java.lang.String)}.
	 */
	@Test
	default void testSetKey() {
		assertLockableSetter(settings(),AnnotationManifest::setKey, "key", true, ManifestTestUtils.TYPE_CAST_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#addAlias(java.lang.String)}.
	 */
	@Test
	default void testAddAlias() {
		assertLockableAccumulativeAdd(settings(),
				AnnotationManifest::addAlias, TestUtils.NO_ILLEGAL(),
				ManifestTestUtils.TYPE_CAST_CHECK, true, ManifestTestUtils.INVALID_INPUT_CHECK,
				"alias1", "alias2", "alias3");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#removeAlias(java.lang.String)}.
	 */
	@Test
	default void testRemoveAlias() {
		assertLockableAccumulativeRemove(settings(),
				AnnotationManifest::addAlias, AnnotationManifest::removeAlias,
				AnnotationManifest::getAliases,
				true, ManifestTestUtils.INVALID_INPUT_CHECK, "alias1", "alias2", "alias3");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#setValueRange(de.ims.icarus2.model.manifest.api.ValueRange)}.
	 */
	@Test
	default void testSetValueRange() {
		assertLockableSetter(settings(),
				AnnotationManifest::setValueRange,
				mock(ValueRange.class), false, ManifestTestUtils.TYPE_CAST_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#setValueSet(de.ims.icarus2.model.manifest.api.ValueSet)}.
	 */
	@Test
	default void testSetValueSet() {
		assertLockableSetter(settings(),
				AnnotationManifest::setValueSet,
				mock(ValueSet.class), false, ManifestTestUtils.TYPE_CAST_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#setValueType(de.ims.icarus2.model.manifest.types.ValueType)}.
	 */
	@Test
	default void testSetValueType() {
		assertLockableSetter(settings(),
				AnnotationManifest::setValueType,
				mock(ValueType.class), true, ManifestTestUtils.TYPE_CAST_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#setContentType(de.ims.icarus2.util.data.ContentType)}.
	 */
	@Test
	default void testSetContentType() {
		assertLockableSetter(settings(),
				AnnotationManifest::setContentType,
				mock(ContentType.class), false, ManifestTestUtils.TYPE_CAST_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#setNoEntryValue(java.lang.Object)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testSetNoEntryValue() {
		return ManifestTestUtils.getAvailableTestTypes().stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						Object value = ManifestTestUtils.getTestValue(valueType);
						//TODO verify if we need value check for noEntryValue field
//						Object illegalValue = ManifestTestUtils.getIllegalValue(valueType);
						assertLockableSetter(settings(),
								AnnotationManifest::setNoEntryValue,
								value, false, ManifestTestUtils.TYPE_CAST_CHECK);
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#setAllowUnknownValues(boolean)}.
	 */
	@Test
	default void testSetAllowUnknownValues() {
		assertLockableSetter(settings(),AnnotationManifest::setAllowUnknownValues);
	}

}
