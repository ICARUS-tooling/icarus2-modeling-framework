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
package de.ims.icarus2.model.manifest.xml.delegates;

import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.standard.ItemLayerManifestImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;

/**
 * @author Markus Gärtner
 *
 */
public class ItemLayerManifestXmlDelegate extends AbstractItemLayerManifestBaseXmlDelegate<ItemLayerManifest> {

	public ItemLayerManifestXmlDelegate() {
		// no-op
	}

	public ItemLayerManifestXmlDelegate(ItemLayerManifest manifest) {
		setInstance(manifest);
	}

	public ItemLayerManifestXmlDelegate(LayerGroupManifest groupManifest) {
		setInstance(new ItemLayerManifestImpl(groupManifest));
	}

	public ItemLayerManifestXmlDelegate reset(LayerGroupManifest groupManifest) {
		reset();
		setInstance(new ItemLayerManifestImpl(groupManifest));

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return ManifestXmlTags.ITEM_LAYER;
	}
}
