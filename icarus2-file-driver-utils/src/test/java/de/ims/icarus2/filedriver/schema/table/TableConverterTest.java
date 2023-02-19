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
package de.ims.icarus2.filedriver.schema.table;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.view.paged.CorpusModel;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.xml.ManifestXmlReader;
import de.ims.icarus2.model.standard.registry.DefaultCorpusManager;
import de.ims.icarus2.test.annotations.ResourceTest;
import de.ims.icarus2.util.AccessMode;

/**
 * @author Markus Gärtner
 *
 */
public class TableConverterTest {

	@Test
	@ResourceTest
	public void test1TierSchema() throws Exception {

		CorpusManager corpusManager = DefaultCorpusManager.builder()
			.defaultEnvironment()
			.build();

		// Get template
		ManifestXmlReader manifestXmlReader = ManifestXmlReader.builder()
				.registry(corpusManager.getManifestRegistry())
				.useImplementationDefaults()
				.build();
		manifestXmlReader.addSource(new ManifestLocation.URLManifestLocation(
				TableConverterTest.class.getResource("tier1.imf.xml"),
				getClass().getClassLoader(), true, false));
		manifestXmlReader.readAndRegisterAll();

		CorpusManifest corpusManifest = corpusManager.getManifestRegistry()
				.getCorpusManifest("testCorpus").get();

		Corpus corpus = corpusManager.connect(corpusManifest);
		PagedCorpusView view = corpus.createFullView(AccessMode.READ, null);

		// Reference file in tabular format
		List<String> lines = Files.readAllLines(
				Paths.get(getClass().getResource("singleBlock.txt").toURI()),
				StandardCharsets.UTF_8);

		assertEquals(lines.size(), view.getSize(), "Incorrect size of read corpus (view)");

		PageControl pageControl = view.getPageControl();
		pageControl.load();

		CorpusModel model = view.getModel();

		Container rootContainer = model.getRootContainer();
		assertEquals(lines.size(), rootContainer.getItemCount(),
				"Incorrect size of root container");

		AnnotationLayer formLayer = view.fetchLayer("form");
		AnnotationLayer lemmaLayer = view.fetchLayer("lemma");

		for(int i=0; i<rootContainer.getItemCount(); i++) {
			// Format per raw line: ID FORM LEMMA
			String[] line = lines.get(i).split("\\s+");

			Item item = rootContainer.getItemAt(i);
			Object form = formLayer.getValue(item);
			Object lemma = lemmaLayer.getValue(item);

			assertEquals(line[1], form, "Form mismatch in row "+(i+1));
			assertEquals(line[2], lemma, "Lemma mismatch in row "+(i+1));

//			System.out.printf("index=%d form=%s lemma=%s\n",item.getIndex(),form, lemma);
		}
	}
}
