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

import static de.ims.icarus2.test.TestUtils.DEFAULT;
import static de.ims.icarus2.test.TestUtils.assertGetter;
import static de.ims.icarus2.test.TestUtils.assertNotPresent;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static de.ims.icarus2.test.TestUtils.settings;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.api.MemberManifest.Property;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.test.CloneableTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface PropertyTest extends LockableTest<Property>, CloneableTest<Property> {

	@Provider
	Property createTestInstance(TestSettings settings, String name, ValueType valueType);

	/**
	 * @see de.ims.icarus2.test.CloneableTest#cloneFunction()
	 */
	@Override
	default Function<Property, Object> cloneFunction() {
		return Property::clone;
	}

	/**
	 * @see de.ims.icarus2.test.CloneableTest#assertCloneContentEquals(java.lang.Cloneable, java.lang.Object)
	 */
	@Override
	default void assertCloneContentEquals(Property original, Object other) {
		assertTrue(other instanceof Property);

		Property clone = (Property)other;

		assertEquals(original.getName(), clone.getName());
		assertEquals(original.getOption(), clone.getOption());
		assertEquals(original.getValueType(), clone.getValueType());
		assertEquals(original.getValue(), clone.getValue());
		assertTrue(original.isMultiValue() == clone.isMultiValue());
	}



	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest.Property#getValueType()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetValueType() {
		return ValueType.valueTypes().stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						assertEquals(valueType, createTestInstance(settings(), "property1", valueType).getValueType());
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest.Property#getValue()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetValue() {
		return ManifestTestUtils.getAvailableTestTypes().stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						Object value = ManifestTestUtils.getTestValue(valueType);
						Property instance = createTestInstance(settings(), "property"+valueType.getName(), valueType);
						instance.setValue(value);
						assertOptionalEquals(value, instance.getValue());
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest.Property#getName()}.
	 */
	@Test
	default void testGetName() {
		String name = "property1";

		assertEquals(name, createTestInstance(settings(), name, ValueType.DEFAULT_VALUE_TYPE).getName());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest.Property#getOption()}.
	 */
	@Test
	default void testGetOption() {
		Property instance = create();

		assertNotPresent(instance.getOption());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest.Property#isMultiValue()}.
	 */
	@Test
	default void testIsMultiValue() {
		assertGetter(create(),
				Boolean.TRUE, Boolean.FALSE,
				DEFAULT(Property.DEFAULT_MULTI_VALUE),
				Property::isMultiValue,
				Property::setMultiValue);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest.Property#setValue(java.lang.Object)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testSetValue() {
		return ManifestTestUtils.getAvailableTestTypes().stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						Object value = ManifestTestUtils.getTestValue(valueType);
						Property instance = createTestInstance(settings(), "property"+valueType.getName(), valueType);
						instance.setValue(value);
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest.Property#setMultiValue(boolean)}.
	 */
	@Test
	default void testSetMultiValue() {
		assertLockableSetter(settings(), Property::setMultiValue);
	}

}
