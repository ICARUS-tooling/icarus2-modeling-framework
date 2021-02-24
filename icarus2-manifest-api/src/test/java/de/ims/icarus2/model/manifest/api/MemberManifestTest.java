/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.test.TestUtils.assertNotPresent;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static de.ims.icarus2.test.TestUtils.settings;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.api.MemberManifest.Property;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.annotations.OverrideTest;
import de.ims.icarus2.util.Options;

/**
 * @author Markus Gärtner
 *
 */
public interface MemberManifestTest<M extends MemberManifest<?>> extends ModifiableIdentityTest<M>,
	CategorizableTest<M>, DocumentableTest<M>, ManifestTest<M> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.ModifiableIdentityTest#testGetId()
	 */
	@OverrideTest
	@Override
	@Test
	default void testGetId() {
		ModifiableIdentityTest.super.testGetId();
		ManifestTest.super.testGetId();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ModifiableIdentityTest#testSetId()
	 */
	@OverrideTest
	@Override
	@Test
	default void testSetId() {
		ModifiableIdentityTest.super.testSetId();
		ManifestTest.super.testSetId();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#getOptionsManifest()}.
	 */
	@Test
	default void testGetOptionsManifest() {
		assertNotPresent(createUnlocked().getOptionsManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#getPropertyValue(java.lang.String)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetPropertyValue() {
		String name = "property123";
		String name2 = "property123_1";

		return ManifestTestUtils.getAvailableTestTypes().stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						M manifest = createUnlocked();

						assertNotPresent(manifest.getPropertyValue("no-such-property"));
						TestUtils.assertNPE(() -> manifest.getPropertyValue(null));

						Object value = ManifestTestUtils.getTestValue(valueType);

						Property property = mockProperty(name, ValueType.STRING, false, value);
						manifest.addProperty(property);

						assertOptionalEquals(value, manifest.getPropertyValue(name));

						if(getExpectedType().isSupportTemplating()) {
							M template = createTemplate(settings());
							template.addProperty(property);
							M derived = createDerived(settings(), template);

							assertOptionalEquals(value, derived.getPropertyValue(name));

							Property property2 = mockProperty(name2, ValueType.STRING, false, value);
							derived.addProperty(property2);

							assertNotPresent(template.getPropertyValue(name2));
							assertOptionalEquals(value, derived.getPropertyValue(name2));
						}
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#addProperty(java.lang.String, de.ims.icarus2.model.manifest.types.ValueType, boolean, java.lang.Object)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testAddPropertyStringValueTypeBooleanObject() {

		String name = "property123";
		String nameM = "property123Mult";

		return ManifestTestUtils.getAvailableTestTypes().stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						M manifest = createUnlocked();
						Object value = ManifestTestUtils.getTestValue(valueType);
						Object illegalValue = ManifestTestUtils.getIllegalValue(valueType);

						if(illegalValue!=null) {
							ManifestTestUtils.assertIllegalValue(() -> manifest.addProperty(
									nameM, valueType, false, illegalValue), illegalValue);
						}

						TestUtils.assertNPE(() -> manifest.addProperty(null, valueType, false, value));
						TestUtils.assertNPE(() -> manifest.addProperty(name, null, false, value));
						TestUtils.assertNPE(() -> manifest.addProperty(name, valueType, false, null));

						Property property = manifest.addProperty(name, valueType, false, value);
						assertNotNull(property);
						assertEquals(valueType, property.getValueType());
						assertNotPresent(property.getOption());
						assertOptionalEquals(value, property.getValue());

						Object valueM = Collections.singleton(value);
						Property propertyM = manifest.addProperty(nameM, valueType, true, valueM);
						assertNotNull(propertyM);
						assertEquals(valueType, propertyM.getValueType());
						assertNotPresent(propertyM.getOption());
						assertOptionalEquals(valueM, propertyM.getValue());

						manifest.lock();
						LockableTest.assertLocked(() -> manifest.addProperty(name+"_1", valueType, false, value));
					}));
	}

	public static Property mockProperty(String name) {

		Property property = mock(Property.class);
		when(property.getName()).thenReturn(name);

		return property;
	}

	@SuppressWarnings("boxing")
	public static Property mockProperty(String name, ValueType valueType, boolean multiValue, Object value) {

		Property property = mockProperty(name);
		when(property.getValueType()).thenReturn(valueType);
		when(property.getValue()).thenReturn(Optional.ofNullable(value));
		when(property.isMultiValue()) .thenReturn(multiValue);

		return property;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#addProperty(de.ims.icarus2.model.manifest.api.MemberManifest.Property)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testAddPropertyProperty() {

		String name = "property123";

		return ManifestTestUtils.getAvailableTestTypes().stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						M manifest = createUnlocked();
						Object value = ManifestTestUtils.getTestValue(valueType);

						Property property = mockProperty(name, valueType, false, value);

						TestUtils.assertNPE(() -> manifest.addProperty(null));

						manifest.addProperty(property);

						ManifestTestUtils.assertManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID,
								() -> manifest.addProperty(property),
								"Teating duplicate property id");

						manifest.lock();
						LockableTest.assertLocked(() -> manifest.addProperty(property));
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#getProperty(java.lang.String)}.
	 */
	@Test
	default void testGetProperty() {
		M manifest = createUnlocked();
		String name = "property123";

		TestUtils.assertNPE(() -> manifest.getProperty(null));
		assertNotPresent(manifest.getProperty(name));

		Property property = mockProperty(name, ValueType.STRING, false, "test");
		manifest.addProperty(property);

		assertOptionalEquals(property, manifest.getProperty(name));

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate(settings());
			template.addProperty(property);
			M derived = createDerived(settings(), template);

			assertOptionalEquals(property, derived.getProperty(name));

			String name2 = "property123_1";
			Property property2 = mockProperty(name2, ValueType.STRING, false, "test");
			derived.addProperty(property2);

			assertNotPresent(template.getProperty(name2));
			assertOptionalEquals(property2, derived.getProperty(name2));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#hasProperty(java.lang.String)}.
	 */
	@Test
	default void testHasProperty() {
		M manifest = createUnlocked();
		String name = "property123";

		TestUtils.assertNPE(() -> manifest.hasProperty(null));
		assertFalse(manifest.hasProperty(name));

		Property property = mockProperty(name, ValueType.STRING, false, "test");
		manifest.addProperty(property);

		assertTrue(manifest.hasProperty(name));

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate(settings());
			template.addProperty(property);
			M derived = createDerived(settings(), template);

			assertTrue(derived.hasProperty(name));

			String name2 = "property123_1";
			Property property2 = mockProperty(name2, ValueType.STRING, false, "test");
			derived.addProperty(property2);

			assertFalse(template.hasProperty(name2));
			assertTrue(derived.hasProperty(name2));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#getPropertyNames()}.
	 */
	@Test
	default void testGetPropertyNames() {
		M manifest = createUnlocked();
		String name = "property123";

		assertTrue(manifest.getPropertyNames().isEmpty());

		Property property = mockProperty(name, ValueType.STRING, false, "test");
		manifest.addProperty(property);

		assertTrue(manifest.getPropertyNames().contains(name));

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate(settings());
			template.addProperty(property);
			M derived = createDerived(settings(), template);

			assertTrue(derived.getPropertyNames().contains(name));

			String name2 = "property123_1";
			Property property2 = mockProperty(name2, ValueType.STRING, false, "test");
			derived.addProperty(property2);

			assertFalse(template.getPropertyNames().contains(name2));
			assertTrue(derived.getPropertyNames().contains(name2));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#forEachProperty(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachProperty() {
		assertDerivativeForEach(settings(),
				mockProperty("property1"), mockProperty("property2"),
				MemberManifest::forEachProperty,
				MemberManifest::addProperty);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#forEachLocalProperty(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLocalProperty() {
		assertDerivativeForEachLocal(settings(),
				mockProperty("property1"), mockProperty("property2"),
				MemberManifest::forEachLocalProperty,
				MemberManifest::addProperty);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#isLocalProperty(java.lang.String)}.
	 */
	@Test
	default void testIsLocalProperty() {
		assertDerivativeAccumulativeIsLocal(settings(),
				mockProperty("property1"), mockProperty("property2"),
				(m, p) -> m.isLocalProperty(p.getName()),
				MemberManifest::addProperty);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#getProperties()}.
	 */
	@Test
	default void testGetProperties() {
		assertDerivativeAccumulativeGetter(settings(),
				mockProperty("property1"), mockProperty("property2"),
				MemberManifest::getProperties,
				MemberManifest::addProperty);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#getLocalProperties()}.
	 */
	@Test
	default void testGetLocalProperties() {
		assertDerivativeAccumulativeLocalGetter(settings(),
				mockProperty("property1"), mockProperty("property2"),
				MemberManifest::getLocalProperties,
				MemberManifest::addProperty);
	}

	public static void assertPropertiesAsOptions(Options options, Property...properties) {
		assertNotNull(options);
		assertEquals(properties.length, options.size());

		for(Property property : properties) {
			assertEquals(property.getValue(), options.get(property.getName()), "Mismatch for property: "+property.getName());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#getPropertiesAsOptions()}.
	 */
	@Test
	default void testGetPropertiesAsOptions() {
		M manifest = createUnlocked();
		String name = "property123";

		assertTrue(manifest.getPropertiesAsOptions().isEmpty());

		Property property = mockProperty(name, ValueType.STRING, false, "test");
		manifest.addProperty(property);

		assertPropertiesAsOptions(manifest.getPropertiesAsOptions(), property);

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate(settings());
			template.addProperty(property);
			M derived = createDerived(settings(), template);

			assertPropertiesAsOptions(derived.getPropertiesAsOptions(), property);

			String name2 = "property123_1";
			Property property2 = mockProperty(name2, ValueType.STRING, false, "test");
			derived.addProperty(property2);

			assertFalse(template.getPropertiesAsOptions().containsKey(name2));
			assertPropertiesAsOptions(derived.getPropertiesAsOptions(), property, property2);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#setPropertyValue(java.lang.String, java.lang.Object)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testSetPropertyValue() {
		String name = "property123";

		return ManifestTestUtils.getAvailableTestTypes().stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
						M manifest = createUnlocked();
						Object[] values = ManifestTestUtils.getTestValues(valueType);
						assertTrue(values.length>1);

						Object value = values[0];
						Object value2 = values[1];
						Object illegalValue = ManifestTestUtils.getIllegalValue(valueType);

						TestUtils.assertNPE(() -> manifest.setPropertyValue(null, value));
						ManifestTestUtils.assertManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
								() -> manifest.setPropertyValue(name, value2),
								"Test modification attempt on unknown property id");

						manifest.addProperty(name, valueType, false, value);

						if(illegalValue!=null) {
							ManifestTestUtils.assertIllegalValue(() -> manifest.setPropertyValue(name, illegalValue), illegalValue);
						}

						manifest.lock();
						LockableTest.assertLocked(() -> manifest.setPropertyValue(name, value2));

						if(getExpectedType().isSupportTemplating()) {
							M template = createTemplate(settings());
							template.addProperty(name, valueType, false, value);
							M derived = createDerived(settings(), template);

							derived.setPropertyValue(name, value2);

							assertOptionalEquals(value, template.getPropertyValue(name));
							assertOptionalEquals(value2, derived.getPropertyValue(name));
						}
					}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#setOptionsManifest(de.ims.icarus2.model.manifest.api.OptionsManifest)}.
	 */
	@Test
	default void testSetOptionsManifest() {
		assertLockableSetter(settings(), MemberManifest::setOptionsManifest,
				mock(OptionsManifest.class), false, ManifestTestUtils.TYPE_CAST_CHECK);
	}

}
