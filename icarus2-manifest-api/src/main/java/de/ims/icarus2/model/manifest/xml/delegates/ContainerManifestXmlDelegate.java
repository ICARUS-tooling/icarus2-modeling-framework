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
package de.ims.icarus2.model.manifest.xml.delegates;

import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.standard.ContainerManifestImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;

/**
 * @author Markus Gärtner
 *
 */
public class ContainerManifestXmlDelegate extends AbstractContainerManifestBaseXmlDelegate<ContainerManifest> {

	public ContainerManifestXmlDelegate() {
		// no-op
	}

	public ContainerManifestXmlDelegate(ContainerManifest containerManifest) {
		setInstance(containerManifest);
	}

	public ContainerManifestXmlDelegate(ItemLayerManifestBase<?> itemLayerManifest) {
		setInstance(new ContainerManifestImpl(itemLayerManifest));
	}

	public ContainerManifestXmlDelegate reset(ItemLayerManifestBase<?> itemLayerManifest) {
		reset();
		setInstance(new ContainerManifestImpl(itemLayerManifest));

		return this;
	}

	@Override
	protected String xmlTag() {
		return ManifestXmlTags.CONTAINER;
	}
}
