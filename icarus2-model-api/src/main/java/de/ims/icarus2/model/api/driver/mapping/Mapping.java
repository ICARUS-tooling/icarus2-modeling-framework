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
package de.ims.icarus2.model.api.driver.mapping;

import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.MappingManifest;

/**
 * @author Markus Gärtner
 *
 */
public interface Mapping extends AutoCloseable {

	/**
	 * Returns the driver that created and manages this mapping.
	 *
	 * @return
	 */
	Driver getDriver();

	/**
	 * Returns the {@code source} layer for the mapping this mapping represents.
	 * Note that the mapping must accept each element in this source layer as a
	 * legal input to methods of its {@link MappingReader} instances!
	 *
	 * @return
	 */
	ItemLayerManifestBase<?> getSourceLayer();

	/**
	 * Returns the {@code target} layer for the mapping this index represents.
	 *
	 * @return
	 */
	ItemLayerManifestBase<?> getTargetLayer();

	/**
	 * Returns the manifest this mapping is based upon.
	 *
	 * @return
	 */
	MappingManifest getManifest();

	/**
	 * Creates a new reader instance to access the data in this mapping.
	 *
	 * @return
	 */
	MappingReader newReader();

	/**
	 * Releases all currently held resources.
	 */
	@Override
	void close();
}
