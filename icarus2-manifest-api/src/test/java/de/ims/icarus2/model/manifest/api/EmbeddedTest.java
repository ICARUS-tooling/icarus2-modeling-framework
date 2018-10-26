/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static de.ims.icarus2.test.TestUtils.settings;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface EmbeddedTest<E extends Embedded> extends GenericTest<E> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	@Provider
	default E createTestInstance(TestSettings settings) {
		Set<ManifestType> allowedHosts = getAllowedHostTypes();
		assertFalse(allowedHosts.isEmpty());

		ManifestType type = allowedHosts.iterator().next();
		TypedManifest host = createMockedHost(type);

		return createEmbedded(settings, host);
	}

	default TypedManifest createMockedHost(ManifestType type) {
		return ManifestTestUtils.mockTypedManifest(type);
	}

	/**
	 * Return all the allowed host types or an empty set in case there
	 * is no limitation on host types.
	 * @return
	 */
	Set<ManifestType> getAllowedHostTypes();

	/**
	 * Create the {@link Embedded} instance under test with the specified host.
	 * @param settings TODO
	 *
	 * @return
	 * @throws Exception
	 */
	@Provider
	E createEmbedded(TestSettings settings, TypedManifest host);

	default <T extends TypedManifest> void assertHostGetter(Function<E, Optional<T>> hostGetter) {
		Set<ManifestType> allowedHostTypes = getAllowedHostTypes();

		for(ManifestType hostType : allowedHostTypes) {
			T host = ManifestTestUtils.mockTypedManifest(hostType);
			E embedded = createEmbedded(settings(), host);
			assertOptionalEquals(host, hostGetter.apply(embedded));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Embedded#getHost()}.
	 * @throws Exception
	 */
	@Test
	default void testGetHost() throws Exception {
		assertHostGetter(Embedded::getHost);
	}
}
