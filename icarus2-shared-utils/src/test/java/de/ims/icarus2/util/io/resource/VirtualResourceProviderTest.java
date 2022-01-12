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
package de.ims.icarus2.util.io.resource;

import java.nio.file.Path;
import java.nio.file.Paths;

import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class VirtualResourceProviderTest implements ResourceProviderTest<VirtualResourceProvider> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends VirtualResourceProvider> getTestTargetClass() {
		return VirtualResourceProvider.class;
	}

	/**
	 * @see de.ims.icarus2.util.io.resource.ResourceProviderTest#cleanup(de.ims.icarus2.util.io.resource.ResourceProvider, java.nio.file.Path[])
	 */
	@Override
	public void cleanup(VirtualResourceProvider provider, Path... paths) {
		provider.clear();
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public VirtualResourceProvider createTestInstance(TestSettings settings) {
		return settings.process(new VirtualResourceProvider());
	}

	/**
	 * @see de.ims.icarus2.util.io.resource.ResourceProviderTest#createRoot()
	 */
	@Override
	public Path createRoot() {
		return Paths.get("root");
	}

}
