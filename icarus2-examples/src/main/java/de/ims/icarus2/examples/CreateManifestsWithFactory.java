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
package de.ims.icarus2.examples;

import static de.ims.icarus2.util.IcarusUtils.DO_NOTHING;

import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestFactory;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.standard.DefaultManifestFactory;
import de.ims.icarus2.model.manifest.standard.DefaultManifestRegistry;

/**
 * @author Markus Gärtner
 *
 */
public class CreateManifestsWithFactory {

	public static void main(String[] args) {
		// Set up the factory
		ManifestRegistry registry = new DefaultManifestRegistry();
		ManifestLocation location = ManifestLocation.newBuilder().virtual().build();
		ManifestFactory factory = new DefaultManifestFactory(location, registry);

		// Start creating and assembling manifests
		ContextManifest contextManifest = factory.create(ManifestType.CONTEXT_MANIFEST);
		contextManifest.setId("myContext");

		LayerGroupManifest group = factory.create(ManifestType.LAYER_GROUP_MANIFEST, contextManifest);
		contextManifest.addLayerGroup(group);

		ItemLayerManifest tokenLayer = factory
				.<ItemLayerManifest>create(ManifestType.ITEM_LAYER_MANIFEST, group)
				.setId("tokens");

		ItemLayerManifest sentenceLayer = factory
				.<ItemLayerManifest>create(ManifestType.ITEM_LAYER_MANIFEST, group)
				.setId("sentences")
				.setFoundationLayerId("tokens", DO_NOTHING())
				.addBaseLayerId("tokens", DO_NOTHING());

		AnnotationLayerManifest annoLayer = factory
				.<AnnotationLayerManifest>create(ManifestType.ANNOTATION_LAYER_MANIFEST, group)
				.addBaseLayerId("tokens", DO_NOTHING());

		AnnotationManifest forms = factory
				.<AnnotationManifest>create(ManifestType.ANNOTATION_MANIFEST, annoLayer)
				.setKey("forms");
		annoLayer.addAnnotationManifest(forms);
		annoLayer.setDefaultKey("forms");

		group.addLayerManifest(tokenLayer);
		group.addLayerManifest(sentenceLayer);
		group.addLayerManifest(annoLayer);

		// Do stuff with manifest
		System.out.println(contextManifest);
	}
}
