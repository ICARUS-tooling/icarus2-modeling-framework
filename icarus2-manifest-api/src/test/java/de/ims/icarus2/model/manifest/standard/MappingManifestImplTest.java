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
package de.ims.icarus2.model.manifest.standard;

import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.api.MappingManifestTest;
import de.ims.icarus2.model.manifest.api.TypedManifest;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class MappingManifestImplTest implements MappingManifestTest {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends MappingManifest> getTestTargetClass() {
		return MappingManifestImpl.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#createEmbedded(de.ims.icarus2.test.TestSettings, de.ims.icarus2.model.manifest.api.TypedManifest)
	 */
	@Override
	public MappingManifest createEmbedded(TestSettings settings, TypedManifest host) {
		return settings.process(new MappingManifestImpl((DriverManifest) host));
	}

}
