/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import static de.ims.icarus2.test.TestUtils.assertPresent;
import static de.ims.icarus2.test.TestUtils.settings;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
public interface PathResolverManifestTest
		extends ForeignImplementationManifestTest<PathResolverManifest>, EmbeddedMemberManifestTest<PathResolverManifest> {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.PathResolverManifest#getLocationManifest()}.
	 */
	@Test
	default void testGetLocationManifest() {
		assertPresent(createUnlocked().getLocationManifest());
		assertNotPresent(createTemplate(settings()).getLocationManifest());
	}


	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	default ManifestType getExpectedType() {
		return ManifestType.PATH_RESOLVER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#getAllowedHostTypes()
	 */
	@Override
	default Set<ManifestType> getAllowedHostTypes() {
		return Collections.singleton(ManifestType.LOCATION_MANIFEST);
	}
}
