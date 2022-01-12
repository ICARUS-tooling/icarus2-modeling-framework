/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.model.manifest.ManifestTestUtils.tryGetTestValue;
import static de.ims.icarus2.test.TestUtils.assertNotPresent;
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

/**
 * @author Markus Gärtner
 *
 */
public interface ValueManifestTest extends DocumentableTest<ValueManifest>,
		ModifiableIdentityTest<ValueManifest>, TypedManifestTest<ValueManifest> {

	public static final Set<ValueType> LEGAL_VALUE_TYPES = ValueManifest.SUPPORTED_VALUE_TYPES;
	public static final Set<ValueType> ILLEGAL_VALUE_TYPES = Collections.unmodifiableSet(
			ValueType.filterWithout(LEGAL_VALUE_TYPES::contains));

	@Provider
	ValueManifest createWithType(TestSettings settings, ValueType valueType);

	/**
	 * @see de.ims.icarus2.test.GenericTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	@Provider
	default ValueManifest createTestInstance(TestSettings settings) {
		return createWithType(settings, ValueType.STRING);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ModifiableIdentityTest#createFromIdentity(java.lang.String, java.lang.String, java.lang.String, javax.swing.Icon)
	 */
	@Override
	@Provider
	default ValueManifest createFromIdentity(String id, String name, String description) {
		ValueManifest manifest = createWithType(settings(), ValueType.STRING);
		manifest.setId(id);
		manifest.setName(name);
		manifest.setDescription(description);
		return manifest;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	default ManifestType getExpectedType() {
		return ManifestType.VALUE_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.ManifestApiTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
	 */
	@Override
	default void configureApiGuard(ApiGuard<ValueManifest> apiGuard) {
		DocumentableTest.super.configureApiGuard(apiGuard);

		apiGuard.parameterResolver(Object.class,
				manifest -> tryGetTestValue(manifest.getValueType()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueManifest#getValue()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetValue() {
		return LEGAL_VALUE_TYPES.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						ValueManifest empty = createWithType(settings(), valueType);
						assertNotPresent(empty.getValue());
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueManifest#getValueType()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetValueType() {
		return LEGAL_VALUE_TYPES.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						ValueManifest empty = createWithType(settings(), valueType);
						assertEquals(valueType, empty.getValueType());
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueManifest#setValue(java.lang.Object)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testSetValue() {
		return LEGAL_VALUE_TYPES.stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						ValueManifest manifest = createWithType(settings(), valueType);

						Object testValue = ManifestTestUtils.getTestValue(valueType);
						Object illegalValue = ManifestTestUtils.getIllegalValue(valueType);

						LockableTest.assertLockableSetter(
								settings(), manifest,
								ValueManifest::setValue, testValue, true, ManifestTestUtils.TYPE_CAST_CHECK, illegalValue);
					}));
	}

	@Test
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
