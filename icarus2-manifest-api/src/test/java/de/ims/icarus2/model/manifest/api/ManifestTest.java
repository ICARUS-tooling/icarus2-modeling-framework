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

import static de.ims.icarus2.model.manifest.ManifestTestUtils.assertAccumulativeGetter;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.assertAccumulativeIsLocal;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.assertAccumulativeLocalGetter;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.assertAccumulativeLookup;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.assertFlagGetter;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.assertForEach;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.assertForEachLocal;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.assertGetter;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.assertIsLocal;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.assertManifestException;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.assertSetter;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockManifestLocation;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockManifestRegistry;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.stubTemplateContext;
import static de.ims.icarus2.test.TestUtils.settings;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.ManifestTestFeature;
import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.annotations.OverrideTest;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.util.function.ObjBoolConsumer;

/**
 * @author Markus Gärtner
 *
 */
public interface ManifestTest <M extends Manifest> extends ManifestFragmentTest<M> {

	@Provider
	M createTestInstance(TestSettings settings, ManifestLocation location, ManifestRegistry registry);

	/**
	 * @see de.ims.icarus2.test.GenericTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Provider
	@Override
	default M createTestInstance(TestSettings settings) {
		ManifestLocation location = mockManifestLocation(settings.hasFeature(ManifestTestFeature.TEMPLATE));
		ManifestRegistry registry = mockManifestRegistry();
		return createTestInstance(settings, location, registry);
	}

	/**
	 * Creates a manifest instance for testing that is registered as a template
	 * with {@link Manifest#getId() id} {@code templateId} with the underlying
	 * registry.
	 *
	 * @return
	 */
	@Provider
	@SuppressWarnings("boxing")
	default M createTemplate(TestSettings settings) {
		ManifestRegistry registry = mock(ManifestRegistry.class);

		String id = "templateId";
		M template = createTestInstance(settings.clone()
				.withFeatures(
					ManifestTestFeature.UNLOCKED,
					ManifestTestFeature.TEMPLATE),
				ManifestTestUtils.mockManifestLocation(true),
				registry);
		template.setIsTemplate(true);
		template.setId(id);

		when(registry.getTemplate(id)).thenReturn(template);
		when(registry.hasTemplate(id)).thenReturn(true);

		return template;
	}

	/**
	 * Create a manifest that is derived from the given template.
	 *
	 * @return
	 */
	@Provider
	default M createDerived(TestSettings settings, M template) {
		// Sanity check for 'template' actually being a template manifest
		assertNotNull(template.getId());
		assertTrue(template.isTemplate());

		// Sanity checks for proper template resolution
		assertTrue(template.getRegistry().hasTemplate(template.getId()));
		assertSame(template, template.getRegistry().getTemplate(template.getId()));

		M derived = createTestInstance(settings.clone().withFeatures(
				ManifestTestFeature.UNLOCKED,
				ManifestTestFeature.DERIVED),
				ManifestTestUtils.mockManifestLocation(false), template.getRegistry());
		derived.setTemplateId(template.getId());

		assertSame(template, derived.getTemplate());

		return derived;
	}

	/**
	 * Attempts to call a dual argument constructor with the following signature: <br>
	 * ({@link ManifestLocation}, {@link ManifestRegistry})
	 *
	 * @throws Exception
	 */
	default void assertConstructorManifestLocationManifestRegistry() throws Exception {
		ManifestLocation location = mockManifestLocation(true);
		ManifestRegistry registry = mockManifestRegistry();

		M manifest = create(new Class<?>[]{ManifestLocation.class, ManifestRegistry.class}, location, registry);

		assertSame(location, manifest.getManifestLocation());
		assertSame(registry, manifest.getRegistry());
	}

	/**
	 * Calls {@link GenericTest#testMandatoryConstructors()} and then
	 * asserts the validity of the following constructors:
	 *
	 * {@link #assertConstructorManifestLocationManifestRegistry()}
	 *
	 * @see de.ims.icarus2.test.GenericTest#testMandatoryConstructors()
	 */
	@OverrideTest
	@Override
	@Test
	default void testMandatoryConstructors() throws Exception {
		ManifestFragmentTest.super.testMandatoryConstructors();
		assertConstructorManifestLocationManifestRegistry();
	}

	public static <K extends Object> Function<K, K> IDENTITY(){
		return k -> k;
	}

	/**
	 * Utility method to simplify testing of common patterns in the manifest
	 * framework.
	 *
	 * This method will perform the following steps in order:
	 * <ol>
	 * <li>Create {@link #createUnlocked() unlocked} instance for testing</li>
	 * <li>Check that {@code getter} result is {@code null} or equal to {@code defaultValue}</li>
	 * <li>Call {@code getter} with  a mocked value</li>
	 * <li>Assert that {@code getter} returns previously set value</li>
	 * <li>Check if {@code #getExpectedType() manifest type} supports {@link ManifestType#isSupportTemplating() templating}
	 *  and exists if it doesn't</li>
	 * <li>Create {@link #createTemplate() template} instance for testing</li>
	 * <li>Assign mocked property via {@code getter} to template</li>
	 * <li>Create {@link #createDerived(Manifest) derived} manifest from template</li>
	 * <li>Assert that {@code getter} on derived manifest returns value from {@code template}</li>
	 * <li>Set another mocked value via {@code getter} on derived manifest</li>
	 * <li>Assert that derived {@code getter} returns new value</li>
	 * <li>Assert that template {@code getter} result is unaffected by changes to derived manifest</li>
	 * </ol>
	 *
	 * @param settings environmental settings that are passed to {@link #createTemplate(TestSettings)} and
	 * {@link #createDerived(TestSettings, Manifest)}
	 * @param value1 value to be used for {@code getter} on the main test instance and template
	 * @param value2 value to be used for {@code getter} on the derived manifest
	 * @param defaultValue internal default value for the tested property if available
	 * @param getter getter method to obtain current value
	 * @param setter setter method to set new value
	 *
	 */
	default <K extends Object> void assertDerivativeGetter(
			TestSettings settings, K value1, K value2, K defaultValue, Function<M,K> getter, BiConsumer<M, K> setter) {

		assertGetter(createUnlocked(settings), value1, value2, defaultValue, getter, setter);

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate(settings);
			setter.accept(template, value1);
			M derived = createDerived(settings, template);

			assertEquals(value1, getter.apply(derived));

			setter.accept(derived, value2);
			assertEquals(value2, getter.apply(derived));
			assertEquals(value1, getter.apply(template));
		}
	}

	@SuppressWarnings("unchecked")
	default <K extends Object> void assertDerivativeAccumulativeGetter(
			TestSettings settings, K value1, K value2, Function<M, ? extends Collection<K>> getter, BiConsumer<M, K> adder) {

		assertAccumulativeGetter(createUnlocked(settings), value1, value2, getter, adder);

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate(settings);
			adder.accept(template, value1);
			M derived = createDerived(settings, template);

			TestUtils.assertCollectionEquals(getter.apply(derived), value1);

			adder.accept(derived, value2);
			TestUtils.assertCollectionEquals(getter.apply(derived), value1, value2);
			TestUtils.assertCollectionEquals(getter.apply(template), value1);
		}
	}

	@SuppressWarnings("unchecked")
	default <K extends Object> void assertDerivativeAccumulativeLocalGetter(
			TestSettings settings, K value1, K value2, Function<M,? extends Collection<K>> getter, BiConsumer<M, K> adder) {

		assertAccumulativeLocalGetter(createUnlocked(settings), value1, value2, getter, adder);

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate(settings);
			adder.accept(template, value1);
			M derived = createDerived(settings, template);

			assertTrue(getter.apply(derived).isEmpty());

			adder.accept(derived, value2);
			TestUtils.assertCollectionEquals(getter.apply(derived), value2);
			TestUtils.assertCollectionEquals(getter.apply(template), value1);
		}
	}

	@SuppressWarnings("unchecked")
	default <K extends Object, A extends Consumer<? super K>> void assertDerivativeForEach(
			TestSettings settings, K value1, K value2, Function<M,Consumer<A>> forEachGen, BiConsumer<M, K> adder) {

		assertForEach(createUnlocked(settings), value1, value2, forEachGen, adder);

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate(settings);
			adder.accept(template, value1);
			M derived = createDerived(settings, template);

			TestUtils.assertForEachUnsorted(forEachGen.apply(derived), value1);

			adder.accept(derived, value2);

			TestUtils.assertForEachUnsorted(forEachGen.apply(derived), value1, value2);
			TestUtils.assertForEachUnsorted(forEachGen.apply(template), value1);
		}
	}

	@SuppressWarnings("unchecked")
	default <K extends Object, A extends Consumer<? super K>> void assertDerivativeForEachLocal(
			TestSettings settings, K value1, K value2, Function<M,Consumer<A>> forEachLocalGen, BiConsumer<M, K> adder) {

		assertForEachLocal(createUnlocked(settings), value1, value2, forEachLocalGen, adder);

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate(settings);
			adder.accept(template, value1);
			M derived = createDerived(settings, template);

			TestUtils.assertForEachEmpty(forEachLocalGen.apply(derived));

			adder.accept(derived, value2);

			TestUtils.assertForEachUnsorted(forEachLocalGen.apply(derived), value2);
			TestUtils.assertForEachUnsorted(forEachLocalGen.apply(template), value1);
		}
	}

	/**
	 * @see #assertDerivativeGetter(Class, Object, Object, Object, Function, BiConsumer)
	 */
	default <K extends Object> void assertDerivativeIsLocal(
			TestSettings settings, K value1, K value2, Predicate<M> isLocalCheck, BiConsumer<M, K> setter) {

		assertIsLocal(createUnlocked(settings), value1, value2, isLocalCheck, setter);

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate(settings);
			setter.accept(template, value1);
			M derived = createDerived(settings, template);

			assertFalse(isLocalCheck.test(derived));

			setter.accept(derived, value2);
			assertTrue(isLocalCheck.test(derived));
		}
	}

	default <K extends Object> void assertDerivativeAccumulativeIsLocal(
			TestSettings settings, K value1, K value2, BiPredicate<M, K> isLocalCheck, BiConsumer<M, K> adder) {

		assertAccumulativeIsLocal(createUnlocked(settings), value1, value2, isLocalCheck, adder);

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate(settings);
			adder.accept(template, value1);
			M derived = createDerived(settings, template);

			assertFalse(isLocalCheck.test(derived, value1));

			adder.accept(derived, value2);
			assertTrue(isLocalCheck.test(derived, value2));
		}
	}

	default void assertDerivativeFlagGetter(TestSettings settings, Boolean defaultValue,
			Predicate<M> getter, ObjBoolConsumer<M> setter) {

		assertFlagGetter(createUnlocked(settings), defaultValue, getter, setter);

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate(settings);
			M derived = createDerived(settings, template);

			setter.accept(template, true);
			assertTrue(getter.test(derived));
			setter.accept(template, false);
			assertFalse(getter.test(derived));

			setter.accept(derived, true);
			assertTrue(getter.test(derived));

			setter.accept(derived, false);
			assertFalse(getter.test(derived));
		}
	}

	default void assertDerivativeLocalFlagGetter(TestSettings settings, Boolean defaultValue,
			Predicate<M> getter, ObjBoolConsumer<M> setter) {

		assertFlagGetter(createUnlocked(settings), defaultValue, getter, setter);

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate(settings);
			M derived = createDerived(settings, template);

			setter.accept(template, true);
			if(defaultValue!=null) {
				assertTrue(getter.test(derived)==defaultValue.booleanValue());
			} else {
				assertFalse(getter.test(derived));
			}
			setter.accept(template, false);
			if(defaultValue!=null) {
				assertTrue(getter.test(derived)==defaultValue.booleanValue());
			} else {
				assertFalse(getter.test(derived));
			}

			setter.accept(derived, true);
			assertTrue(getter.test(derived));

			setter.accept(derived, false);
			assertFalse(getter.test(derived));
		}
	}


	default <K extends Object, I extends Object> void assertDerivativeAccumulativeLookup(
			K value1, K value2, BiFunction<M, I, K> lookup,
			boolean checkNPE, BiConsumer<Executable, String> invalidLookupCheck,
			BiConsumer<M, K> adder, Function<K, I> keyGen, @SuppressWarnings("unchecked") I...invalidLookups) {

		assertAccumulativeLookup(createUnlocked(), value1, value2, lookup, checkNPE,
				invalidLookupCheck, adder, keyGen, invalidLookups);

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate(settings());
			adder.accept(template, value1);

			M derived = createDerived(settings(), template);

			assertSame(value1, lookup.apply(derived, keyGen.apply(value1)));
			invalidLookupCheck.accept(() -> lookup.apply(derived, keyGen.apply(value2)),
					"Test unknown lookup for derived manifest");

			adder.accept(derived, value2);
			assertSame(value2, lookup.apply(derived, keyGen.apply(value2)));
			invalidLookupCheck.accept(() -> lookup.apply(template, keyGen.apply(value2)),
					"Test unknown lookup for template manifest");
		}
	}

	default <K extends Object, I extends Object> void assertDerivativeAccumulativeLookupContains(
			K value1, K value2, BiPredicate<M, I> check,
			boolean checkNPE, BiConsumer<M, K> adder, Function<K, I> keyGen) {

		ManifestTestUtils.assertAccumulativeLookupContains(createUnlocked(), value1, value2,
				check, checkNPE, adder, keyGen);


		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate(settings());
			adder.accept(template, value1);

			M derived = createDerived(settings(), template);

			assertTrue(check.test(derived, keyGen.apply(value1)));
			assertFalse(check.test(derived, keyGen.apply(value2)));

			adder.accept(derived, value2);
			assertTrue(check.test(derived, keyGen.apply(value2)));
			assertFalse(check.test(template, keyGen.apply(value2)));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#getUID()}.
	 */
	@Test
	default void testGetUID() {
		assertTrue(createUnlocked().getUID()>0);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#hasTemplateContext()}.
	 */
	@Test
	default void testHasTemplateContext() {
		// Takes care of embedding depth of 0
		assertFalse(createUnlocked().hasTemplateContext());
		assertTrue(createTemplate(settings()).hasTemplateContext());

		// Complex hierarchy
		int embeddingDepth = 1;
		while(true) {
			M manifest = createTestInstance(settings().withFeatures(ManifestTestFeature.EMBEDDED),
					mockManifestLocation(true), mockManifestRegistry());
			if(!stubTemplateContext(manifest, embeddingDepth)) {
				break;
			}

			assertTrue(manifest.hasTemplateContext(), "Expecting template context for embedding depths "+embeddingDepth);

			embeddingDepth++;
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#isTemplate()}.
	 */
	@Test
	default void testIsTemplate() {
		assertFalse(createUnlocked().isTemplate());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#isValidTemplate()}.
	 */
	@Test
	default void testIsValidTemplate() {
		assertFalse(createUnlocked().isValidTemplate());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#hasTemplate()}.
	 */
	@Test
	default void testHasTemplate() {
		assertFalse(createUnlocked().hasTemplate());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#getTemplate()}.
	 */
	@Test
	default void testGetTemplate() {
		assertNull(createUnlocked().getTemplate());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#isEmpty()}.
	 */
	@Test
	default void testIsEmpty() {
		assertTrue(createUnlocked().isEmpty());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#setId(java.lang.String)}.
	 *
	 * @see ManifestUtils#isValidId(String)
	 */
	@Test
	default void testSetId() {
		assertSetter(createUnlocked(), Manifest::setId, ManifestTestUtils.getLegalIdValues(),
				true, INVALID_ID_CHECK, ManifestTestUtils.getIllegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#setIsTemplate(boolean)}.
	 */
	@Test
	default void testSetIsTemplate() {
		M manifest = createTestInstance(settings().withFeatures(ManifestTestFeature.TEMPLATE),
				ManifestTestUtils.mockManifestLocation(true), mock(ManifestRegistry.class));

		manifest.setIsTemplate(true);
		assertTrue(manifest.isTemplate());

		manifest.lock();
		LockableTest.assertLocked(() -> manifest.setIsTemplate(true));

		Manifest liveContextManifest = createUnlocked();

		ManifestTestUtils.assertManifestException(ManifestErrorCode.MANIFEST_ERROR,
				() -> liveContextManifest.setIsTemplate(true),
				"Testing setIsTemplate(String) on embedded manifest");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#setTemplateId(java.lang.String)}.
	 */
	@Test
	default void testSetTemplateId() {
		M manifest = createUnlocked();

		String id = "test123";

		M template = createTemplate(settings());

		ManifestRegistry registry = manifest.getRegistry();
		when(registry.getTemplate(id)).thenReturn(template);

		manifest.setTemplateId(id);
		assertSame(template, manifest.getTemplate());

		manifest.setTemplateId(null);
		assertNull(manifest.getTemplate());

		manifest.lock();
		LockableTest.assertLocked(() -> manifest.setTemplateId(id));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#getRegistry()}.
	 */
	@Test
	default void testGetRegistry() {
		assertNotNull(createUnlocked().getRegistry());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#getManifestLocation()}.
	 */
	@Test
	default void testGetManifestLocation() {
		M unlocked = createUnlocked();

		assertNotNull(unlocked.getManifestLocation());

		assertFalse(unlocked.getManifestLocation().isTemplate());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#getVersionManifest()}.
	 */
	@Test
	default void testGetVersionManifest() {
		assertNull(createUnlocked().getVersionManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#setVersionManifest(de.ims.icarus2.model.manifest.api.VersionManifest)}.
	 */
	@Test
	default void testSetVersionManifest() {
		assertLockableSetter(settings(),Manifest::setVersionManifest, mock(VersionManifest.class), true, TYPE_CAST_CHECK);

		/*
		 *  Additional test, since contract requires that any attempt
		 *  after the first to set a version manifest must fail.
		 */
		M manifest = createUnlocked();
		manifest.setVersionManifest(mock(VersionManifest.class));
		assertManifestException(ManifestErrorCode.MANIFEST_CORRUPTED_STATE,
				() -> manifest.setVersionManifest(mock(VersionManifest.class)),
				"Testing repeated attempt to set version manifest");
	}

}
