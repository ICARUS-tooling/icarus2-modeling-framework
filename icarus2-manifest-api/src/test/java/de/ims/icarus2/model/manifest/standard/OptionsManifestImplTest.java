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
package de.ims.icarus2.model.manifest.standard;

import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.api.OptionsManifestTest;
import de.ims.icarus2.model.manifest.api.TypedManifest;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class OptionsManifestImplTest implements OptionsManifestTest<OptionsManifestImpl>{


	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends OptionsManifestImpl> getTestTargetClass() {
		return OptionsManifestImpl.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#createEmbedded(TestSettings, de.ims.icarus2.model.manifest.api.TypedManifest)
	 */
	@Override
	public OptionsManifestImpl createEmbedded(TestSettings settings, TypedManifest host) {
		return settings.process(new OptionsManifestImpl((MemberManifest) host));
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestTest#createTestInstance(de.ims.icarus2.test.TestSettings, de.ims.icarus2.model.manifest.api.ManifestLocation, de.ims.icarus2.model.manifest.api.ManifestRegistry)
	 */
	@Override
	public OptionsManifestImpl createTestInstance(TestSettings settings, ManifestLocation location,
			ManifestRegistry registry) {
		return settings.process(new OptionsManifestImpl(location, registry));
	}

}
