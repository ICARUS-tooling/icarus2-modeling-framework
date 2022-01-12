/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockManifestLocation;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockManifestRegistry;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestGenerator.IncrementalBuild;
import de.ims.icarus2.model.manifest.api.Embedded;
import de.ims.icarus2.model.manifest.api.ManifestFactory;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.TypedManifest;
import de.ims.icarus2.model.manifest.standard.DefaultManifestFactory;

/**
 * @author Markus Gärtner
 *
 */
class ManifestGeneratorTest {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.ManifestGenerator#build(de.ims.icarus2.model.manifest.api.ManifestType, de.ims.icarus2.model.manifest.ManifestGenerator.Config)}.
	 */
	@Test
	void testGenerateManifestTypeConfig() {
		for(ManifestType type : ManifestType.values()) {

			// Ignore dummy type(s)
			if(type.getBaseClass()==null) {
				continue;
			}

			// Only test manifest types without obligatory environment
			if(type.requiresEnvironment()) {
				continue;
			}

			ManifestFactory factory = new DefaultManifestFactory(
					mockManifestLocation(type.isSupportTemplating()),
					mockManifestRegistry());
			ManifestGenerator generator = new ManifestGenerator(factory);

			IncrementalBuild<TypedManifest> container = generator.build(type,
					ManifestGenerator.config());
			assertNotNull(container);
			TypedManifest manifest = container.getInstance();
			assertNotNull(manifest);
			assertEquals(type, manifest.getManifestType());
		}
	}


	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.ManifestGenerator#build(ManifestType, TypedManifest, de.ims.icarus2.model.manifest.ManifestGenerator.Config)}
	 */
	@Test
	void testGenerateManifestTypeTypedManifestConfig() {
		for(ManifestType type : ManifestType.values()) {

			// Ignore dummy type(s)
			if(type.getBaseClass()==null) {
				continue;
			}

			if(!type.requiresEnvironment()) {
				continue;
			}

			ManifestFactory factory = new DefaultManifestFactory(
					mockManifestLocation(type.isSupportTemplating()),
					mockManifestRegistry());
			ManifestGenerator generator = new ManifestGenerator(factory);

			for(ManifestType hostType : type.getRequiredEnvironment()) {
				TypedManifest host = mockTypedManifest(hostType, true);

				IncrementalBuild<TypedManifest> container = generator.build(
						type, host,
						ManifestGenerator.config());

				assertNotNull(container);
				TypedManifest manifest = container.getInstance();
				assertNotNull(manifest);
				assertEquals(type, manifest.getManifestType());

				assertTrue(manifest instanceof Embedded);
				assertOptionalEquals(host, ((Embedded)manifest).getHost());
			}
		}
	}

}
