/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import de.ims.icarus2.model.manifest.standard.DefaultManifestFactory;
import de.ims.icarus2.model.manifest.standard.DefaultManifestRegistry;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.ManifestBuilder;

/**
 * @author Markus Gärtner
 *
 */
public class ICARUS2Sample_02_CreateManifestsWithBuilder {

	public static void main(String[] args) {
		// Set up the factory
		ManifestRegistry registry = new DefaultManifestRegistry();
		ManifestLocation location = ManifestLocation.builder().virtual().build();
		ManifestFactory factory = new DefaultManifestFactory(location, registry);

		ContextManifest contextManifest;

		try(ManifestBuilder builder = new ManifestBuilder(factory)) {
			// Start creating and assembling manifests
			contextManifest = builder.create(ContextManifest.class, "myContext")
					.addLayerGroup(builder.create(LayerGroupManifest.class, "myGroup", "myContext")
							.addLayerManifest(builder.create(ItemLayerManifest.class, "tokens", "myGroup"))
							.addLayerManifest(builder.create(ItemLayerManifest.class, "sentences", "myGroup")
									.setFoundationLayerId("tokens", DO_NOTHING())
									.addBaseLayerId("tokens", DO_NOTHING()))
							.addLayerManifest(builder.create(AnnotationLayerManifest.class, "surface", "myGroup")
									.addBaseLayerId("tokens", DO_NOTHING())
									.addAnnotationManifest(builder.create(AnnotationManifest.class, "forms", "surface")
											.setKey("forms")
											.setValueType(ValueType.STRING)
											.setAllowUnknownValues(true))
									.setDefaultKey("forms")));
		}

		// Do stuff with manifest
		System.out.println(contextManifest);
	}
}
