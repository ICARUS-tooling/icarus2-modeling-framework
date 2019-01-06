/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface ManifestFactoryTest {

	ManifestFactory createFactory();

	@Test
	default void testGetSupportedTypes() {
		ManifestFactory factory = createFactory();
		Set<ManifestType> supportedTypes = factory.getSupportedTypes();

		assertNotNull(supportedTypes);
		assertFalse(supportedTypes.isEmpty());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestFactory#create(de.ims.icarus2.model.manifest.api.ManifestType)}.
	 */
	@Test
	default void testCreateManifestType() {
		ManifestFactory factory = createFactory();

		for(ManifestType type : factory.getSupportedTypes()) {

			// Go only for environment free manifest types here
			if(type.requiresEnvironment()) {
				continue;
			}

			ManifestFragment manifest = factory.create(type);

			assertNotNull(manifest);
			assertEquals(type, manifest.getManifestType());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestFactory#create(de.ims.icarus2.model.manifest.api.ManifestType, TypedManifest)}.
	 */
	@Test
	default void testCreateManifestTypeObject() {
		ManifestFactory factory = createFactory();

		for(ManifestType type : factory.getSupportedTypes()) {

			// Go only for environment dependent manifest types here
			if(!type.requiresEnvironment()) {
				continue;
			}

			for(ManifestType hostType : type.getRequiredEnvironment()) {
				TypedManifest host = ManifestTestUtils.mockTypedManifest(hostType);
				ManifestFragment manifest = factory.create(type, host);

				assertNotNull(manifest);
				assertEquals(type, manifest.getManifestType());
			}
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestFactory#create(de.ims.icarus2.model.manifest.api.ManifestType, TypedManifest, de.ims.icarus2.util.Options)}.
	 */
	@Test
	@Disabled("currently the 'options' parameter is unused and as such the testCreateManifestTypeObject() method covers this case already")
	default void testCreateManifestTypeObjectOptions() {
		//TODO add test logic
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestFactory#getManifestLocation()}.
	 */
	@Test
	default void testGetManifestLocation() {
		assertNotNull(createFactory().getManifestLocation());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestFactory#getRegistry()}.
	 */
	@Test
	default void testGetRegistry() {
		assertNotNull(createFactory().getRegistry());
	}

}
