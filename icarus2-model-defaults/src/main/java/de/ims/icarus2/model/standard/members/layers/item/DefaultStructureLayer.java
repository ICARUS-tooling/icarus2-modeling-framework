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
package de.ims.icarus2.model.standard.members.layers.item;

import de.ims.icarus2.model.api.layer.StructureLayer;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.util.annotations.TestableImplementation;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(StructureLayer.class)
public class DefaultStructureLayer extends DefaultItemLayer implements StructureLayer {

	/**
	 *
	 * @param manifest
	 */
	public DefaultStructureLayer(StructureLayerManifest manifest) {
		super(manifest);
	}

	/**
	 * @see de.ims.icarus2.model.api.standard.layer.AbstractLayer#getManifest()
	 */
	@Override
	public StructureLayerManifest getManifest() {
		return (StructureLayerManifest) super.getManifest();
	}
}
