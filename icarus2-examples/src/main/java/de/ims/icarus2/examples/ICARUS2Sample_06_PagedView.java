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

import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives._long;

import java.util.Random;

import de.ims.icarus2.common.formats.Template;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.corpus.CorpusOption;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.view.paged.CorpusModel;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.xml.ManifestXmlReader;
import de.ims.icarus2.model.standard.registry.DefaultCorpusManager;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.Options;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * @author Markus Gärtner
 *
 */
public class ICARUS2Sample_06_PagedView {

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
					.url(ICARUS2Sample_06_PagedView.class.getResource("ConnectCorpus01.imf.xml"))
					.build())
			.readAndRegisterAll();

		// Connect to the corpus resource
		CorpusManifest corpusManifest = manifestRegistry.getCorpusManifest("corpus.test.connect")
				.orElseThrow(ManifestException.error("Missing test corpus"));
		Corpus corpus = corpusManager.connect(corpusManifest);

		// Obtain a paged view to access the corpus in blocks

		// Default page size is 1000, so for demonstration purposes we change that down to 2
		Options options = new Options();
		options.put(CorpusOption.PARAM_VIEW_PAGE_SIZE, _int(2));

		try(PagedCorpusView view = corpus.createFullView(AccessMode.READ, options)) {
			PageControl pageControl = view.getPageControl();
			CorpusModel model = view.getModel();
			AnnotationLayer formLayer = view.fetchLayer("form");

			// We simulate a random sampling of blocks here
			IntList remainingPages = new IntArrayList();
			Random rng = new Random();
			for (int i = 0; i < pageControl.getPageCount(); i++) {
				int insertionIndex = remainingPages.isEmpty() ? 0 : rng.nextInt(remainingPages.size()+1);
				remainingPages.add(insertionIndex, i);
			}

			// Now go page by page and access actual content
			for(int pageIndex : remainingPages) {
				pageControl.loadPage(pageIndex); // We could check the return value to make sure the page has loaded
				int pageSize = pageControl.getIndices().size();
				Container rootContainer = model.getRootContainer();

				System.out.printf("Accessing page %d with %d elements%n", _int(pageIndex), _int(pageSize));

				for (int i = 0; i < pageSize; i++) {
					Container sentence = (Container) rootContainer.getItemAt(i);
					System.out.printf("%d: ", _long(sentence.getIndex()));
					sentence.forEachItem(item -> {
						System.out.print(model.getValue(formLayer, item, "form"));
						System.out.print(" ");
					});
					System.out.println();
				}
			}
		}
	}
}
