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
package de.ims.icarus2.model.standard.registry.metadata;

import java.nio.file.Paths;

import de.ims.icarus2.model.api.registry.MetadataRegistryTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.util.io.resource.VirtualIOResource;

/**
 * @author Markus Gärtner
 *
 */
class PlainMetadataRegistryTest implements MetadataRegistryTest<PlainMetadataRegistry> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends PlainMetadataRegistry> getTestTargetClass() {
		return PlainMetadataRegistry.class;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public PlainMetadataRegistry createTestInstance(TestSettings settings) {
		return settings.process(new PlainMetadataRegistry(new VirtualIOResource(Paths.get("."))));
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistryTest#createReadingCopy(de.ims.icarus2.model.api.registry.MetadataRegistry)
	 */
	@Override
	public PlainMetadataRegistry createReadingCopy(PlainMetadataRegistry original) {
		return new PlainMetadataRegistry(original.getResource());
	}
}
