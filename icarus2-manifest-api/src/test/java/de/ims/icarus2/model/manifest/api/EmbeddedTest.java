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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Set;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface EmbeddedTest {

	/**
	 * Return all the allowed host types or an empty set in case there
	 * is no limitation on host types.
	 * @return
	 */
	Set<ManifestType> getAllowedHostTypes();

	/**
	 * Create the {@link Embedded} instance under test with the specified host.
	 *
	 * @return
	 * @throws Exception
	 */
	Embedded createEmbedded(TypedManifest host) throws Exception;

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Embedded#getHost()}.
	 * @throws Exception
	 */
	@Test
	default void testGetHost() throws Exception {
		Set<ManifestType> allowedHostTypes = getAllowedHostTypes();

		for(ManifestType hostType : allowedHostTypes) {
			TypedManifest host = ManifestTestUtils.mockTypedManifest(hostType);
			Embedded embedded = createEmbedded(host);
			assertNotNull(embedded.getHost());
			assertSame(host, embedded.getHost());
		}
	}
}
