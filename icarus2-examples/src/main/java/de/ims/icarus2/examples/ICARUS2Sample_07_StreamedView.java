/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import de.ims.icarus2.common.formats.Template;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.view.streamed.StreamedCorpusView;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.xml.ManifestXmlReader;
import de.ims.icarus2.model.standard.registry.DefaultCorpusManager;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.Options;

/**
 * @author Markus Gärtner
 *
 */
public class ICARUS2Sample_07_StreamedView {

	public static void main(String[] args) throws Exception {

		// Setup the corpus management
		CorpusManager corpusManager = DefaultCorpusManager.builder()
				/* Setup the manager. This is the place to customize
				 * the environment or inject new implementations.
				 * We use the (virtual) default environment for this demo.
				 */
				.defaultEnvironment()
				.build();

		// Register our corpus manifest
		ManifestRegistry manifestRegistry = corpusManager.getManifestRegistry();
		Template.applyTemplates(manifestRegistry, Template.CONLL);
		ManifestXmlReader.builder()
			.registry(manifestRegistry)
			.useImplementationDefaults()
			.build()
			.addSource(ManifestLocation.builder()
					.input()
					.url(ICARUS2Sample_07_StreamedView.class.getResource("ConnectCorpus01.imf.xml"))
					.build())
			.readAndRegisterAll();

		// Connect to the corpus resource
		CorpusManifest corpusManifest = manifestRegistry.getCorpusManifest("corpus.test.connect")
				.orElseThrow(ManifestException.error("Missing test corpus"));
		Corpus corpus = corpusManager.connect(corpusManifest);

		/*
		 * Obtain stream and iterate elements.
		 * We use the complete scope which automatically uses the corpus' primary
		 * layer as basis of elements in the 'currentItem()' method below.
		 * By customizing the scope we can change what elements to iterate and what
		 * layers will be available. Note however that the default stream implementation
		 * delegates the bulk of the decisions to the underlying driver(s) and so
		 * more data might be loaded than actually intended with the selected scope.
		 */
		try(StreamedCorpusView view = corpus.createStream(corpus.createCompleteScope(), AccessMode.READ, Options.none())) {

			AnnotationLayer formLayer = view.fetchLayer("form");
			AnnotationStorage annoForm = formLayer.getAnnotationStorage();

			while(view.advance()) {
				Container sentence = view.currentItem();
				sentence.forEachItem(item -> {
					System.out.print(annoForm.getString(item, "form"));
					System.out.print(" ");
				});
				System.out.println();
			}
		}
	}
}
