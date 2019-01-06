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
package de.ims.icarus2.model.manifest.standard;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.ManifestFactory;
import de.ims.icarus2.model.manifest.api.ManifestFactoryTest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;

/**
 * @author Markus Gärtner
 *
 */
class DefaultManifestFactoryTest implements ManifestFactoryTest {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.standard.DefaultManifestFactory#DefaultManifestFactory(de.ims.icarus2.model.manifest.api.ManifestLocation, de.ims.icarus2.model.manifest.api.ManifestRegistry)}.
	 */
	@Test
	void testDefaultManifestFactory() {
		ManifestLocation manifestLocation = mock(ManifestLocation.class);
		ManifestRegistry registry = mock(ManifestRegistry.class);

		assertThrows(NullPointerException.class, () -> new DefaultManifestFactory(null, registry));
		assertThrows(NullPointerException.class, () -> new DefaultManifestFactory(manifestLocation, null));

		ManifestFactory factory = new DefaultManifestFactory(manifestLocation, registry);

		assertSame(manifestLocation, factory.getManifestLocation());
		assertSame(registry, factory.getRegistry());
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestFactoryTest#createFactory()
	 */
	@Override
	public ManifestFactory createFactory() {
		ManifestLocation manifestLocation = mock(ManifestLocation.class);
		ManifestRegistry registry = mock(ManifestRegistry.class);

		return new DefaultManifestFactory(manifestLocation, registry);
	}

}
