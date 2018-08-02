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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.api.MemberManifest.Property;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.util.Options;

/**
 * @author Markus Gärtner
 *
 */
public interface MemberManifestTest<M extends MemberManifest> extends ModifiableIdentityTest,
	CategorizableTest, DocumentableTest, ManifestTest<M>, EmbeddedTest {

	@Override
	default M createUnlocked() {
		// Need explicit declaration due to unifying methods from ManifestTet and CategorizableTest
		return ManifestTest.super.createUnlocked();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ModifiableIdentityTest#createEmpty()
	 */
	@Override
	default ModifiableIdentity createEmpty() {
		return createUnlocked();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ModifiableIdentityTest#testGetId()
	 */
	@Override
	default void testGetId() {
		ModifiableIdentityTest.super.testGetId();
		ManifestTest.super.testGetId();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ModifiableIdentityTest#testSetId()
	 */
	@Override
	default void testSetId() {
		ModifiableIdentityTest.super.testSetId();
		ManifestTest.super.testSetId();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#getOptionsManifest()}.
	 */
	@Test
	default void testGetOptionsManifest() {
		assertNull(createUnlocked().getOptionsManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#getPropertyValue(java.lang.String)}.
	 */
	@Test
	default void testGetPropertyValue() {
		String name = "property123";
		String name2 = "property123_1";

		for(ValueType valueType : ManifestTestUtils.getAvailableTestTypes()) {
			M manifest = createUnlocked();

			assertNull(manifest.getPropertyValue("no-such-property"));
			TestUtils.assertNPE(() -> manifest.getPropertyValue(null));

			Object value = ManifestTestUtils.getTestValue(valueType);

			Property property = mockProperty(name, ValueType.STRING, false, value);
			manifest.addProperty(property);

			assertEquals(value, manifest.getPropertyValue(name));

			if(getExpectedType().isSupportTemplating()) {
				M template = createTemplate();
				template.addProperty(property);
				M derived = createDerived(template);

				assertEquals(value, derived.getPropertyValue(name));

				Property property2 = mockProperty(name2, ValueType.STRING, false, value);
				derived.addProperty(property2);

				assertNull(template.getPropertyValue(name2));
				assertEquals(value, derived.getPropertyValue(name2));
			}
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#addProperty(java.lang.String, de.ims.icarus2.model.manifest.types.ValueType, boolean, java.lang.Object)}.
	 */
	@Test
	default void testAddPropertyStringValueTypeBooleanObject() {

		String name = "property123";
		String nameM = "property123Mult";

		for(ValueType valueType : ManifestTestUtils.getAvailableTestTypes()) {
			M manifest = createUnlocked();
			Object value = ManifestTestUtils.getTestValue(valueType);

			ManifestTestUtils.assertIllegalValue(() -> manifest.addProperty(
					nameM, valueType, false, ManifestTestUtils.getIllegalValue(valueType)));

			TestUtils.assertNPE(() -> manifest.addProperty(null, valueType, false, value));
			TestUtils.assertNPE(() -> manifest.addProperty(name, null, false, value));
			TestUtils.assertNPE(() -> manifest.addProperty(name, valueType, false, null));

			Property property = manifest.addProperty(nameM, valueType, false, value);
			assertNotNull(property);
			assertEquals(valueType, property.getValueType());
			assertNull(property.getOption());
			assertEquals(value, property.getValue());

			Object valueM = Collections.singleton(value);
			Property propertyM = manifest.addProperty(nameM, valueType, true, valueM);
			assertNotNull(propertyM);
			assertEquals(valueType, property.getValueType());
			assertNull(property.getOption());
			assertEquals(valueM, property.getValue());

			manifest.lock();
			LockableTest.assertLocked(() -> manifest.addProperty(name+"_1", valueType, false, value));
		}
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
		when(property.getValue()).thenReturn(value);
		when(property.isMultiValue()) .thenReturn(multiValue);

		return property;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#addProperty(de.ims.icarus2.model.manifest.api.MemberManifest.Property)}.
	 */
	@Test
	default void testAddPropertyProperty() {

		String name = "property123";

		for(ValueType valueType : ManifestTestUtils.getAvailableTestTypes()) {
			M manifest = createUnlocked();
			Object value = ManifestTestUtils.getTestValue(valueType);

			Property property = mockProperty(name, valueType, false, value);

			TestUtils.assertNPE(() -> manifest.addProperty(null));

			manifest.addProperty(property);

			ManifestTestUtils.assertManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID,
					() -> manifest.addProperty(property));

			manifest.lock();
			LockableTest.assertLocked(() -> manifest.addProperty(property));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#getProperty(java.lang.String)}.
	 */
	@Test
	default void testGetProperty() {
		M manifest = createUnlocked();
		String name = "property123";

		TestUtils.assertNPE(() -> manifest.getProperty(null));
		ManifestTestUtils.assertManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
				() -> manifest.getProperty(name));

		Property property = mockProperty(name, ValueType.STRING, false, "test");
		manifest.addProperty(property);

		assertSame(property, manifest.getProperty(name));

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate();
			template.addProperty(property);
			M derived = createDerived(template);

			assertSame(property, derived.getProperty(name));

			String name2 = "property123_1";
			Property property2 = mockProperty(name2, ValueType.STRING, false, "test");
			derived.addProperty(property2);

			ManifestTestUtils.assertManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
					() -> template.getProperty(name2));

			assertSame(property2, derived.getProperty(name2));
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
			M template = createTemplate();
			template.addProperty(property);
			M derived = createDerived(template);

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
			M template = createTemplate();
			template.addProperty(property);
			M derived = createDerived(template);

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
		M manifest = createUnlocked();
		String name = "property123";

		TestUtils.assertNPE(() -> manifest.forEachProperty(null));

		TestUtils.assertForEachEmpty(manifest::forEachProperty);

		Property property = mockProperty(name, ValueType.STRING, false, "test");
		manifest.addProperty(property);

		TestUtils.assertForEachUnsorted(manifest::forEachProperty, property);

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate();
			template.addProperty(property);
			M derived = createDerived(template);

			TestUtils.assertForEachUnsorted(derived::forEachProperty, property);

			String name2 = "property123_1";
			Property property2 = mockProperty(name2, ValueType.STRING, false, "test");
			derived.addProperty(property2);

			TestUtils.assertForEachUnsorted(derived::forEachProperty, property, property2);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#forEachLocalProperty(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLocalProperty() {
		M manifest = createUnlocked();
		String name = "property123";

		TestUtils.assertNPE(() -> manifest.forEachLocalProperty(null));

		TestUtils.assertForEachEmpty(manifest::forEachLocalProperty);

		Property property = mockProperty(name, ValueType.STRING, false, "test");
		manifest.addProperty(property);

		TestUtils.assertForEachUnsorted(manifest::forEachLocalProperty, property);

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate();
			template.addProperty(property);
			M derived = createDerived(template);

			TestUtils.assertForEachEmpty(derived::forEachLocalProperty);

			String name2 = "property123_1";
			Property property2 = mockProperty(name2, ValueType.STRING, false, "test");
			derived.addProperty(property2);

			TestUtils.assertForEachUnsorted(derived::forEachLocalProperty, property2);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#isLocalProperty(java.lang.String)}.
	 */
	@Test
	default void testIsLocalProperty() {
		M manifest = createUnlocked();
		String name = "property123";

		TestUtils.assertNPE(() -> manifest.isLocalProperty(null));

		assertFalse(manifest.isLocalProperty(name));

		Property property = mockProperty(name, ValueType.STRING, false, "test");
		manifest.addProperty(property);

		assertTrue(manifest.isLocalProperty(name));

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate();
			template.addProperty(property);
			M derived = createDerived(template);

			assertFalse(derived.isLocalProperty(name));

			String name2 = "property123_1";
			Property property2 = mockProperty(name2, ValueType.STRING, false, "test");
			derived.addProperty(property2);

			assertTrue(derived.isLocalProperty(name2));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#getProperties()}.
	 */
	@Test
	default void testGetProperties() {
		M manifest = createUnlocked();
		String name = "property123";

		assertTrue(manifest.getProperties().isEmpty());

		Property property = mockProperty(name, ValueType.STRING, false, "test");
		manifest.addProperty(property);

		assertTrue(manifest.getProperties().contains(property));

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate();
			template.addProperty(property);
			M derived = createDerived(template);

			assertTrue(derived.getProperties().contains(property));

			String name2 = "property123_1";
			Property property2 = mockProperty(name2, ValueType.STRING, false, "test");
			derived.addProperty(property2);

			assertFalse(template.getProperties().contains(property2));
			assertTrue(derived.getProperties().contains(property2));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#getLocalProperties()}.
	 */
	@Test
	default void testGetLocalProperties() {
		M manifest = createUnlocked();
		String name = "property123";

		assertTrue(manifest.getLocalProperties().isEmpty());

		Property property = mockProperty(name, ValueType.STRING, false, "test");
		manifest.addProperty(property);

		assertTrue(manifest.getLocalProperties().contains(property));

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate();
			template.addProperty(property);
			M derived = createDerived(template);

			assertTrue(derived.getLocalProperties().isEmpty());

			String name2 = "property123_1";
			Property property2 = mockProperty(name2, ValueType.STRING, false, "test");
			derived.addProperty(property2);

			assertFalse(template.getLocalProperties().contains(property2));
			assertTrue(derived.getLocalProperties().contains(property2));
		}
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
			M template = createTemplate();
			template.addProperty(property);
			M derived = createDerived(template);

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
	@Test
	default void testSetPropertyValue() {
		String name = "property123";

		for(ValueType valueType : ManifestTestUtils.getAvailableTestTypes()) {
			M manifest = createUnlocked();
			Object[] values = ManifestTestUtils.getTestValues(valueType);
			assertTrue(values.length>1);

			Object value = values[0];
			Object value2 = values[1];
			Object illegalValue = ManifestTestUtils.getIllegalValue(valueType);

			TestUtils.assertNPE(() -> manifest.setPropertyValue(null, value));
			ManifestTestUtils.assertManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
					() -> manifest.setPropertyValue(name, value2));
			ManifestTestUtils.assertIllegalValue(() -> manifest.setPropertyValue(name, illegalValue));

			manifest.addProperty(name, valueType, false, value);

			manifest.lock();
			LockableTest.assertLocked(() -> manifest.setPropertyValue(name, value2));

			if(getExpectedType().isSupportTemplating()) {
				M template = createTemplate();
				template.addProperty(name, valueType, false, value);
				M derived = createDerived(template);

				derived.setPropertyValue(name, value2);

				assertEquals(value, template.getPropertyValue(name));
				assertEquals(value2, derived.getPropertyValue(name));
			}
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#setOptionsManifest(de.ims.icarus2.model.manifest.api.OptionsManifest)}.
	 */
	@Test
	default void testSetOptionsManifest() {
		M manifest = createUnlocked();

		manifest.setOptionsManifest(null);

		OptionsManifest optionsManifest = mock(OptionsManifest.class);
		manifest.setOptionsManifest(optionsManifest);

		manifest.lock();
		LockableTest.assertLocked(() -> manifest.setOptionsManifest(optionsManifest));
	}

}
