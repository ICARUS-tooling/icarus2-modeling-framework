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

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockManifestLocation;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockManifestRegistry;
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
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.ManifestFrameworkTest;
import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.util.function.ObjBoolConsumer;

/**
 * @author Markus Gärtner
 *
 */
public interface ManifestTest <M extends Manifest> extends ManifestFragmentTest<M>, GenericTest<M> {

	M createUnlocked(ManifestLocation location, ManifestRegistry registry);

	@Test
	default void testConstructorManifestLocationManifestRegistry() throws Exception {
		ManifestLocation location = mockManifestLocation(true);
		ManifestRegistry registry = mockManifestRegistry();

		M manifest = create(new Class<?>[]{ManifestLocation.class, ManifestRegistry.class}, location, registry);

		assertSame(location, manifest.getManifestLocation());
		assertSame(registry, manifest.getRegistry());
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
	 * <li>Assign mocked property via {@code getter}</li>
	 * <li>Create {@link #createDerived(Manifest) derived} manifest from template</li>
	 * <li>Assert that {@code getter} on derived manifest returns value from {@code template}</li>
	 * <li>Set another mocked value via {@code getter} on derived manifest</li>
	 * <li>Assert that derived {@code getter} returns new value</li>
	 * <li>Assert that template {@code getter} result is unaffected by changes to derived manifest</li>
	 * </ol>
	 *
	 * @param value1 value to be used for {@code getter} on the main test instance and template
	 * @param value2 value to be used for {@code getter} on the derived manifest
	 * @param defaultValue internal default value for the tested property if available
	 * @param getter getter method to obtain current value
	 * @param setter setter method to set new value
	 */
	default <K extends Object> void assertDerivativeGetter(
			K value1, K value2, K defaultValue, Function<M,K> getter, BiConsumer<M, K> setter) {

		ManifestFrameworkTest.assertGetter(createUnlocked(), value1, value2, defaultValue, getter, setter);

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate();
			setter.accept(template, value1);
			M derived = createDerived(template);

			assertEquals(value1, getter.apply(derived));

			setter.accept(derived, value2);
			assertEquals(value2, getter.apply(derived));
			assertEquals(value1, getter.apply(template));
		}
	}

	@SuppressWarnings("unchecked")
	default <K extends Object> void assertDerivativeAccumulativeGetter(
			K value1, K value2, Function<M,? extends Collection<K>> getter, BiConsumer<M, K> adder) {

		ManifestFrameworkTest.assertAccumulativeGetter(createUnlocked(), value1, value2, getter, adder);

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate();
			adder.accept(template, value1);
			M derived = createDerived(template);

			TestUtils.assertCollectionEquals(getter.apply(derived), value1);

			adder.accept(derived, value2);
			TestUtils.assertCollectionEquals(getter.apply(derived), value1, value2);
			TestUtils.assertCollectionEquals(getter.apply(template), value1);
		}
	}

	@SuppressWarnings("unchecked")
	default <K extends Object> void assertDerivativeAccumulativeLocalGetter(
			K value1, K value2, Function<M,? extends Collection<K>> getter, BiConsumer<M, K> adder) {

		ManifestFrameworkTest.assertAccumulativeLocalGetter(createUnlocked(), value1, value2, getter, adder);

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate();
			adder.accept(template, value1);
			M derived = createDerived(template);

			assertTrue(getter.apply(derived).isEmpty());

			adder.accept(derived, value2);
			TestUtils.assertCollectionEquals(getter.apply(derived), value2);
			TestUtils.assertCollectionEquals(getter.apply(template), value1);
		}
	}

	@SuppressWarnings("unchecked")
	default <K extends Object, A extends Consumer<? super K>> void assertDerivativeForEach(
			K value1, K value2, Function<M,Consumer<A>> forEachGen, BiConsumer<M, K> adder) {

		ManifestFrameworkTest.assertForEach(createUnlocked(), value1, value2, forEachGen, adder);

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate();
			adder.accept(template, value1);
			M derived = createDerived(template);

			TestUtils.assertForEachUnsorted(forEachGen.apply(derived), value1);

			adder.accept(derived, value2);

			TestUtils.assertForEachUnsorted(forEachGen.apply(derived), value1, value2);
			TestUtils.assertForEachUnsorted(forEachGen.apply(template), value1);
		}
	}

	@SuppressWarnings("unchecked")
	default <K extends Object, A extends Consumer<? super K>> void assertDerivativeForEachLocal(
			K value1, K value2, Function<M,Consumer<A>> forEachLocalGen, BiConsumer<M, K> adder) {

		ManifestFrameworkTest.assertForEachLocal(createUnlocked(), value1, value2, forEachLocalGen, adder);

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate();
			adder.accept(template, value1);
			M derived = createDerived(template);

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
			K value1, K value2, Predicate<M> isLocalCheck, BiConsumer<M, K> setter) {

		ManifestFrameworkTest.assertIsLocal(createUnlocked(), value1, value2, isLocalCheck, setter);

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate();
			setter.accept(template, value1);
			M derived = createDerived(template);

			assertFalse(isLocalCheck.test(derived));

			setter.accept(derived, value2);
			assertTrue(isLocalCheck.test(derived));
		}
	}

	default <K extends Object> void assertDerivativeAccumulativeIsLocal(
			K value1, K value2, BiPredicate<M, K> isLocalCheck, BiConsumer<M, K> adder) {

		ManifestFrameworkTest.assertAccumulativeIsLocal(createUnlocked(), value1, value2, isLocalCheck, adder);

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate();
			adder.accept(template, value1);
			M derived = createDerived(template);

			assertFalse(isLocalCheck.test(derived, value1));

			adder.accept(derived, value2);
			assertTrue(isLocalCheck.test(derived, value2));
		}
	}

	default void assertDerivativeFlagGetter(Boolean defaultValue, Predicate<M> getter, ObjBoolConsumer<M> setter) {

		ManifestFrameworkTest.assertFlagGetter(createUnlocked(), defaultValue, getter, setter);

		if(getExpectedType().isSupportTemplating()) {
			M template = createTemplate();
			M derived = createDerived(template);

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

	/**
	 * Create an empty and unlocked manifest for testing.
	 * The used {@link ManifestLocation} will report to not host templates.
	 *
	 * @see de.ims.icarus2.model.manifest.api.ManifestFragmentTest#createUnlocked()
	 */
	@Override
	default M createUnlocked() {
		return createUnlocked(ManifestTestUtils.mockManifestLocation(false), ManifestTestUtils.mockManifestRegistry());
	}

	/**
	 * Creates a manifest instance for testing that is registered as a template
	 * with {@link Manifest#getId() id} {@code templateId} with the underlying
	 * registry.
	 *
	 * @return
	 */
	@SuppressWarnings("boxing")
	default M createTemplate() {
		ManifestRegistry registry = mock(ManifestRegistry.class);

		String id = "templateId";
		M template = createUnlocked(ManifestTestUtils.mockManifestLocation(true), registry);
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
	default M createDerived(M template) {
		// Sanity check for 'template' actually being a template manifest
		assertNotNull(template.getId());
		assertTrue(template.isTemplate());

		// Sanity checks for proper template resolution
		assertTrue(template.getRegistry().hasTemplate(template.getId()));
		assertSame(template, template.getRegistry().getTemplate(template.getId()));

		M derived = createUnlocked(ManifestTestUtils.mockManifestLocation(false), template.getRegistry());
		derived.setTemplateId(template.getId());

		assertSame(template, derived.getTemplate());

		return derived;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#getUID()}.
	 */
	@Test
	default void testGetUID() {
		assertTrue(createUnlocked().getUID()>0);
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
		Manifest manifest = createUnlocked();

		ManifestTestUtils.assertMalformedId(manifest, "");
		ManifestTestUtils.assertMalformedId(manifest, "a");
		ManifestTestUtils.assertMalformedId(manifest, "aa");
		ManifestTestUtils.assertMalformedId(manifest, "123");
		ManifestTestUtils.assertMalformedId(manifest, "123abc");
		ManifestTestUtils.assertMalformedId(manifest, "%$§!()");
		ManifestTestUtils.assertMalformedId(manifest, "abc:def");
		ManifestTestUtils.assertMalformedId(manifest, "abc/def");
		ManifestTestUtils.assertMalformedId(manifest, "abc@def");
		ManifestTestUtils.assertMalformedId(manifest, "abc+def");
		ManifestTestUtils.assertMalformedId(manifest, "abc~def");
		ManifestTestUtils.assertMalformedId(manifest, "abc#def");
		ManifestTestUtils.assertMalformedId(manifest, "abc'def");
		ManifestTestUtils.assertMalformedId(manifest, "abc*def");
		ManifestTestUtils.assertMalformedId(manifest, "abc=def");
		ManifestTestUtils.assertMalformedId(manifest, "abc&def");
		ManifestTestUtils.assertMalformedId(manifest, "abc%def");
		ManifestTestUtils.assertMalformedId(manifest, "abc$def");
		ManifestTestUtils.assertMalformedId(manifest, "abc§def");
		ManifestTestUtils.assertMalformedId(manifest, "abc\"def");
		ManifestTestUtils.assertMalformedId(manifest, "abc!def");
		ManifestTestUtils.assertMalformedId(manifest, "abc{def");
		ManifestTestUtils.assertMalformedId(manifest, "abc}def");
		ManifestTestUtils.assertMalformedId(manifest, "abc[def");
		ManifestTestUtils.assertMalformedId(manifest, "abc]def");
		ManifestTestUtils.assertMalformedId(manifest, "abc(def");
		ManifestTestUtils.assertMalformedId(manifest, "abc)def");
		ManifestTestUtils.assertMalformedId(manifest, "abc\\def");
		ManifestTestUtils.assertMalformedId(manifest, "abc`def");
		ManifestTestUtils.assertMalformedId(manifest, "abc´def");
		ManifestTestUtils.assertMalformedId(manifest, "abc°def");
		ManifestTestUtils.assertMalformedId(manifest, "abc^def");
		ManifestTestUtils.assertMalformedId(manifest, "abc<def");
		ManifestTestUtils.assertMalformedId(manifest, "abc>def");
		ManifestTestUtils.assertMalformedId(manifest, "abc|def");
		ManifestTestUtils.assertMalformedId(manifest, "abc;def");
		ManifestTestUtils.assertMalformedId(manifest, "abc"+TestUtils.EMOJI+"def");

		ManifestTestUtils.assertValidId(manifest, "abc");
		ManifestTestUtils.assertValidId(manifest, "abc123");
		ManifestTestUtils.assertValidId(manifest, "abcdef");
		ManifestTestUtils.assertValidId(manifest, "abc-def");
		ManifestTestUtils.assertValidId(manifest, "abc_def");
		ManifestTestUtils.assertValidId(manifest, "abc.def");
		ManifestTestUtils.assertValidId(manifest, "abc-def123");
		ManifestTestUtils.assertValidId(manifest, "abc_def123");
		ManifestTestUtils.assertValidId(manifest, "abc.def123");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#setIsTemplate(boolean)}.
	 */
	@Test
	default void testSetIsTemplate() {
		Manifest manifest = createUnlocked(ManifestTestUtils.mockManifestLocation(true), mock(ManifestRegistry.class));

		manifest.setIsTemplate(true);
		assertTrue(manifest.isTemplate());

		manifest.lock();
		LockableTest.assertLocked(() -> manifest.setIsTemplate(true));

		Manifest liveContextManifest = createUnlocked();

		ManifestTestUtils.assertManifestException(ManifestErrorCode.MANIFEST_ERROR,
				() -> liveContextManifest.setIsTemplate(true));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#setTemplateId(java.lang.String)}.
	 */
	@Test
	default void testSetTemplateId() {
		Manifest manifest = createUnlocked();

		String id = "test123";

		Manifest template = mock(Manifest.class);

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
		Manifest unlocked = createUnlocked();

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
		Manifest manifest = createUnlocked();

		VersionManifest versionManifest = mock(VersionManifest.class);
		manifest.setVersionManifest(versionManifest);
		assertSame(versionManifest, manifest.getVersionManifest());

		TestUtils.assertNPE(() -> manifest.setVersionManifest(null));

		ManifestTestUtils.assertManifestException(ManifestErrorCode.MANIFEST_CORRUPTED_STATE,
				() -> manifest.setVersionManifest(versionManifest));

		Manifest lockedManifest = createUnlocked();
		lockedManifest.lock();
		LockableTest.assertLocked(() -> lockedManifest.setVersionManifest(versionManifest));
	}

}