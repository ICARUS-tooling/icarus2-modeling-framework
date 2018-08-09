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

import static de.ims.icarus2.model.manifest.ManifestTestUtils.getOrMockManifestLocation;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.getOrMockManifestRegistry;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockManifestLocation;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockManifestRegistry;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

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
	CategorizableTest<M>, DocumentableTest<M>, ManifestTest<M>, EmbeddedTest<M> {


	M createHosted(ManifestLocation manifestLocation, ManifestRegistry registry, TypedManifest host);

	/**
	 * Attempts to call a triple argument constructor with mocks of all
	 * the {@link #getAllowedHostTypes() allowed host types}. The constructor
	 * call has the following signature: <br>
	 * ({@link ManifestLocation}, {@link ManifestRegistry}, {@code mockedHost})
	 *
	 * @throws Exception
	 */
	default void assertConstructorManifestLocationManifestRegistryHost() throws Exception {

		for(ManifestType manifestType : getAllowedHostTypes()) {

			TypedManifest host = mockTypedManifest(manifestType, true);
			ManifestLocation location = getOrMockManifestLocation(host, false);
			ManifestRegistry registry = getOrMockManifestRegistry(host);

			M manifest = create(
					new Class<?>[]{ManifestLocation.class, ManifestRegistry.class, manifestType.getBaseClass()},
					location, registry, host);

			assertSame(location, manifest.getManifestLocation());
			assertSame(registry, manifest.getRegistry());
			assertSame(host, manifest.getHost());
		}
	}

	@SuppressWarnings("unchecked")
	default void assertConstructorManifestLocationManifestRegistryHost(
			Class<? extends TypedManifest>...hostClasses) throws Exception {

		for(Class<? extends TypedManifest> hostClass : hostClasses) {

			TypedManifest host = mockTypedManifest(hostClass);
			ManifestLocation location = getOrMockManifestLocation(host, false);
			ManifestRegistry registry = getOrMockManifestRegistry(host);

			M manifest = create(
					new Class<?>[]{ManifestLocation.class, ManifestRegistry.class, hostClass},
					location, registry, host);

			assertSame(location, manifest.getManifestLocation());
			assertSame(registry, manifest.getRegistry());
			assertSame(host, manifest.getHost());
		}
	}

	/**
	 * Attempts to call a single argument constructor with mocks of all
	 * the {@link #getAllowedHostTypes() allowed host types}.
	 *
	 * @throws Exception
	 */
	default void assertConstructorHost() throws Exception {

		for(ManifestType manifestType : getAllowedHostTypes()) {

			TypedManifest host = mockTypedManifest(manifestType, true);

			M manifest = create(
					new Class<?>[]{manifestType.getBaseClass()}, host);

			assertSame(host, manifest.getHost());
			assertNotNull(manifest.getRegistry());
			assertNotNull(manifest.getManifestLocation());
		}
	}

	@SuppressWarnings("unchecked")
	default void assertConstructorHost(Class<? extends TypedManifest>...hostClasses) throws Exception {

		for(Class<? extends TypedManifest> hostClass : hostClasses) {

			TypedManifest host = mockTypedManifest(hostClass);

			M manifest = create(
					new Class<?>[]{hostClass}, host);

			assertSame(host, manifest.getHost());
			assertNotNull(manifest.getRegistry());
			assertNotNull(manifest.getManifestLocation());
		}
	}

	/**
	 * Calls {@link ManifestTest#testMandatoryConstructors()} and then
	 * asserts the validity of the following constructors:
	 *
	 * {@link #assertConstructorHost()}
	 * {@link #assertConstructorManifestLocationManifestRegistryHost()}
	 *
	 * @see de.ims.icarus2.model.manifest.api.ManifestTest#testMandatoryConstructors()
	 */
	@Override
	default void testMandatoryConstructors() throws Exception {
		ManifestTest.super.testMandatoryConstructors();

		assertConstructorHost();
		assertConstructorManifestLocationManifestRegistryHost();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#createEmbedded(de.ims.icarus2.model.manifest.api.TypedManifest)
	 */
	@Override
	default M createEmbedded(TypedManifest host) {
		return createHosted(mockManifestLocation(false), mockManifestRegistry(), host);
	}

	@Override
	default M createUnlocked() {
		// Need explicit declaration due to unifying methods from ManifestTet and CategorizableTest
		return ManifestTest.super.createUnlocked();
	}

	/**
	 * Ensures that an appropriate host manifest is created and used for
	 * {@link #createHosted(ManifestLocation, ManifestRegistry, TypedManifest)}
	 * in case the given {@link ManifestLocation} is declared to hold
	 * {@link ManifestLocation#isTemplate() templates}.
	 *
	 * @see de.ims.icarus2.model.manifest.api.ManifestTest#createUnlocked(de.ims.icarus2.model.manifest.api.ManifestLocation, de.ims.icarus2.model.manifest.api.ManifestRegistry)
	 */
	@Override
	default M createUnlocked(ManifestLocation location, ManifestRegistry registry) {
		TypedManifest host = null;
		Set<ManifestType> hostTypes = getAllowedHostTypes();
		if(!location.isTemplate() && !hostTypes.isEmpty()) {
			host = mockTypedManifest(hostTypes.iterator().next(), true);
		}

		return createHosted(location, registry, host);
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
			assertNull(property.getOption());
			assertEquals(value, property.getValue());

			Object valueM = Collections.singleton(value);
			Property propertyM = manifest.addProperty(nameM, valueType, true, valueM);
			assertNotNull(propertyM);
			assertEquals(valueType, propertyM.getValueType());
			assertNull(propertyM.getOption());
			assertEquals(valueM, propertyM.getValue());

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
					() -> manifest.addProperty(property),
					"Teating duplicate property id");

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
				() -> manifest.getProperty(name),
				"Testing retrieval of unknown property id");

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
					() -> template.getProperty(name2),
					"Testing retrieval of unknown property id on template");

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
		assertDerivativeForEach(mockProperty("property1"), mockProperty("property2"),
				m -> m::forEachProperty, MemberManifest::addProperty);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#forEachLocalProperty(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLocalProperty() {
		assertDerivativeForEachLocal(mockProperty("property1"), mockProperty("property2"),
				m -> m::forEachLocalProperty, MemberManifest::addProperty);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#isLocalProperty(java.lang.String)}.
	 */
	@Test
	default void testIsLocalProperty() {
		assertDerivativeAccumulativeIsLocal(mockProperty("property1"), mockProperty("property2"),
				(m, p) -> m.isLocalProperty(p.getName()), MemberManifest::addProperty);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#getProperties()}.
	 */
	@Test
	default void testGetProperties() {
		assertDerivativeAccumulativeGetter(mockProperty("property1"), mockProperty("property2"),
				MemberManifest::getProperties, MemberManifest::addProperty);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MemberManifest#getLocalProperties()}.
	 */
	@Test
	default void testGetLocalProperties() {
		assertDerivativeAccumulativeLocalGetter(mockProperty("property1"), mockProperty("property2"),
				MemberManifest::getLocalProperties, MemberManifest::addProperty);
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
					() -> manifest.setPropertyValue(name, value2),
					"Test modification attempt on unknown property id");

			manifest.addProperty(name, valueType, false, value);

			if(illegalValue!=null) {
				ManifestTestUtils.assertIllegalValue(() -> manifest.setPropertyValue(name, illegalValue), illegalValue);
			}

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
		assertLockableSetter(MemberManifest::setOptionsManifest, mock(OptionsManifest.class), false, TYPE_CAST_CHECK);
	}

}
