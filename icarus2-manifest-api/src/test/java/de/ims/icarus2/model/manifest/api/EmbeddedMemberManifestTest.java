/**
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

import static de.ims.icarus2.model.manifest.ManifestTestUtils.getOrMockManifestLocation;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.getOrMockManifestRegistry;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockManifestLocation;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockManifestRegistry;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.stubTemplateContext;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static de.ims.icarus2.test.TestUtils.settings;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestFeature;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.OverrideTest;
import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface EmbeddedMemberManifestTest<M extends MemberManifest<?> & Embedded> extends MemberManifestTest<M>, EmbeddedTest<M> {


	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 *
	 * @see MemberManifestTest#createTestInstance(TestSettings)
	 */
	@Provider
	@Override
	default M createTestInstance(TestSettings settings) {
		return MemberManifestTest.super.createTestInstance(settings);
	}

	@Provider
	M createHosted(TestSettings settings, ManifestLocation manifestLocation, ManifestRegistry registry, TypedManifest host);

	@Provider
	default TypedManifest createMockedHost(ManifestLocation location, ManifestRegistry registry, ManifestType preferredType) {
		return mockTypedManifest(preferredType, true);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#createEmbedded(TestSettings, de.ims.icarus2.model.manifest.api.TypedManifest)
	 */
	@Provider
	@Override
	default M createEmbedded(TestSettings settings, TypedManifest host) {
		return createHosted(settings(ManifestTestFeature.EMBEDDED),
				mockManifestLocation(false), mockManifestRegistry(), host);
	}

	/**
	 * Ensures that an appropriate host manifest is created and used for
	 * {@link #createHosted(TestSettings, ManifestLocation, ManifestRegistry, TypedManifest)}
	 * in case the given {@link ManifestLocation} is declared to hold
	 * {@link ManifestLocation#isTemplate() templates}.
	 *
	 * @see de.ims.icarus2.model.manifest.api.ManifestTest#createTestInstance(TestSettings, de.ims.icarus2.model.manifest.api.ManifestLocation, de.ims.icarus2.model.manifest.api.ManifestRegistry)
	 */
	@Provider
	@Override
	default M createTestInstance(TestSettings settings, ManifestLocation location, ManifestRegistry registry) {
		TypedManifest host = null;
		Set<ManifestType> hostTypes = getAllowedHostTypes();

		boolean hostRequested = settings.hasFeature(ManifestTestFeature.EMBEDDED);
		boolean templateContextRequested = settings.hasFeatures(ManifestTestFeature.TEMPLATE, ManifestTestFeature.EMBED_TEMPLATE);

		if(!hostTypes.isEmpty() &&
				(!location.isTemplate() || hostRequested || templateContextRequested)) {

			host = createMockedHost(location, registry, hostTypes.iterator().next());
		}

		M manifest = createHosted(settings.features(ManifestTestFeature.EMBEDDED), location, registry, host);

		if(templateContextRequested) {
			assertTrue(stubTemplateContext(manifest), "Host chain cannot be stubbed as template");
		}

		return manifest;
	}

	/**
	 * Attempts to call a triple argument constructor with mocks of all
	 * the {@link #getAllowedHostTypes() allowed host types}. The constructor
	 * call has the following signature: <br>
	 * ({@link ManifestLocation}, {@link ManifestRegistry}, {@code mockedHost})
	 *
	 * @throws Exception
	 */
	default void assertConstructorManifestLocationManifestRegistryHost() throws Exception {

		for(ManifestType hostType : getAllowedHostTypes()) {

			TypedManifest host = mockTypedManifest(hostType, true);
			ManifestLocation location = getOrMockManifestLocation(host, false);
			ManifestRegistry registry = getOrMockManifestRegistry(host);

			M manifest = create(
					new Class<?>[]{ManifestLocation.class, ManifestRegistry.class, hostType.getGenericBaseClass()},
					location, registry, host);

			assertSame(location, manifest.getManifestLocation());
			assertSame(registry, manifest.getRegistry());
			assertOptionalEquals(host, manifest.getHost());
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
			assertOptionalEquals(host, manifest.getHost());
		}
	}

	/**
	 * Attempts to call a single argument constructor with mocks of all
	 * the {@link #getAllowedHostTypes() allowed host types}.
	 *
	 * @throws Exception
	 */
	default void assertConstructorHost() throws Exception {

		for(ManifestType hostType : getAllowedHostTypes()) {

			TypedManifest host = mockTypedManifest(hostType, true);

			M manifest = create(
					new Class<?>[]{hostType.getGenericBaseClass()}, host);

			assertOptionalEquals(host, manifest.getHost());
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

			assertOptionalEquals(host, manifest.getHost());
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
	@OverrideTest
	@Override
	@Test
	default void testMandatoryConstructors() throws Exception {
		MemberManifestTest.super.testMandatoryConstructors();

		assertConstructorHost();
		assertConstructorManifestLocationManifestRegistryHost();
	}
}
