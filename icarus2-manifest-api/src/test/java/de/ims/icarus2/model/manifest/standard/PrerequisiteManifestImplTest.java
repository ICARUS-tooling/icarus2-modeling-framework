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
package de.ims.icarus2.model.manifest.standard;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;

import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.PrerequisiteManifestTest;
import de.ims.icarus2.model.manifest.api.TypedManifest;
import de.ims.icarus2.model.manifest.standard.ContextManifestImpl.PrerequisiteManifestImpl;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class PrerequisiteManifestImplTest implements PrerequisiteManifestTest {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends PrerequisiteManifest> getTestTargetClass() {
		return PrerequisiteManifestImpl.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.PrerequisiteManifestTest#createTestInstance(de.ims.icarus2.test.TestSettings, java.lang.String)
	 */
	@Override
	public PrerequisiteManifest createTestInstance(TestSettings settings, String alias) {
		ContextManifest contextManifest = mockTypedManifest(ManifestType.CONTEXT_MANIFEST);
		return settings.process(new PrerequisiteManifestImpl(contextManifest, alias));
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.PrerequisiteManifestTest#createEmbedded(de.ims.icarus2.test.TestSettings, de.ims.icarus2.model.manifest.api.TypedManifest, java.lang.String)
	 */
	@Override
	public PrerequisiteManifestImpl createEmbedded(TestSettings settings, TypedManifest host, String alias) {
		return settings.process(new PrerequisiteManifestImpl((ContextManifest) host, alias));
	}
}
