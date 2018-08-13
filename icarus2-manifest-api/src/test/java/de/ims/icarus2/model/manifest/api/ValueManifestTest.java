/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.swing.Icon;

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
public interface ValueManifestTest<M extends ValueManifest> extends DocumentableTest<M>, ModifiableIdentityTest, TypedManifestTest<M> {

	@Provider
	M createWithType(ValueType valueType);

	/**
	 * @see de.ims.icarus2.test.GenericTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	@Provider
	default M createTestInstance(TestSettings settings) {
		return createWithType(ValueType.STRING);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ModifiableIdentityTest#createEmpty()
	 */
	@Override
	@Provider
	default ModifiableIdentity createEmpty() {
		return createWithType(ValueType.STRING);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ModifiableIdentityTest#createFromIdentity(java.lang.String, java.lang.String, java.lang.String, javax.swing.Icon)
	 */
	@Override
	@Provider
	default ModifiableIdentity createFromIdentity(String id, String name, String description, Icon icon) {
		ModifiableIdentity modifiableIdentity = createWithType(ValueType.STRING);
		modifiableIdentity.setId(id);
		modifiableIdentity.setName(name);
		modifiableIdentity.setDescription(description);
		modifiableIdentity.setIcon(icon);
		return modifiableIdentity;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	default ManifestType getExpectedType() {
		return ManifestType.VALUE_MANIFEST;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueManifest#getValue()}.
	 */
	@Test
	default void testGetValue() {
		for(ValueType valueType : ValueManifest.SUPPORTED_VALUE_TYPES) {

			M empty = createWithType(valueType);
			assertNull(empty.getValue());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueManifest#getValueType()}.
	 */
	@Test
	default void testGetValueType() {
		for(ValueType valueType : ValueManifest.SUPPORTED_VALUE_TYPES) {

			M empty = createWithType(valueType);
			assertEquals(valueType, empty.getValueType());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueManifest#setValue(java.lang.Object)}.
	 */
	@Test
	default void testSetValue() {
		for(ValueType valueType : ValueManifest.SUPPORTED_VALUE_TYPES) {

			M manifest = createWithType(valueType);

			Object testValue = ManifestTestUtils.getTestValue(valueType);
			Object illegalValue = ManifestTestUtils.getIllegalValue(valueType);

			LockableTest.assertLockableSetter(manifest,
					ValueManifest::setValue, testValue, true, TYPE_CAST_CHECK, illegalValue);
		}
	}

	@Test
	default void testUnsupportedValueTypes() {
		for(ValueType valueType : ValueType.valueTypes()) {
			if(!ValueManifest.SUPPORTED_VALUE_TYPES.contains(valueType)) {
				UnsupportedValueTypeException exception = assertThrows(UnsupportedValueTypeException.class,
						() -> createWithType(valueType));

				assertEquals(ManifestErrorCode.MANIFEST_UNSUPPORTED_TYPE, exception.getErrorCode());
				assertEquals(valueType, exception.getValueType());
			}
		}
	}
}
