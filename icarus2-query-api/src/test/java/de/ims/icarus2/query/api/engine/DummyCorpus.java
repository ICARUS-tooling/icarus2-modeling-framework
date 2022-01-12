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
/**
 *
 */
package de.ims.icarus2.query.api.engine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.xml.sax.SAXException;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest.PathType;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.standard.LocationManifestImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlReader;
import de.ims.icarus2.model.standard.registry.DefaultCorpusManager;

/**
 * @author Markus Gärtner
 *
 */
public class DummyCorpus {

	public static final String CONTEXT = "context0";
	public static final String LAYER_TOKEN = "token";
	public static final String LAYER_TOKEN_2 = "token2";
	public static final String LAYER_SENTENCE = "sentence";
	public static final String LAYER_SENTENCE_2 = "sentence2";
	public static final String LAYER_SYNTAX = "syntax";
	public static final String LAYER_SYNTAX_2 = "syntax2";
	public static final String LAYER_ANNO = "anno";
	public static final String LAYER_ANNO_2 = "anno2";

	public static final String ANNO_COUNTER = "counter";
	public static final String ANNO_1 = "anno1";
	public static final String ANNO_2 = "anno2";

	public enum DummyType {
		FLAT("tpl_flat_corpus.imf.xml"),
		HIERARCHICAL("tpl_hierarchical_corpus.imf.xml"),
		FULL("tpl_full_corpus.imf.xml"),
		;
		private final String path;

		private DummyType(String path) {
			this.path = path;
		}
	}

	public static Path createCorpusFile(Path folder, int...setup) throws IOException {
		StringBuilder sb = new StringBuilder(setup.length * 100);
		for (int i = 0; i < setup.length; i++) {
			if(i>0) {
				sb.append('\n');
			}
			for (int j = 0; j < setup[i]; j++) {
				sb.append(j)
					.append('\t').append("anno1_").append(i).append('_').append(j)
					.append('\t').append("anno2_").append(i).append('_').append(j)
					.append('\n');
			}
		}

		Path file = Files.createTempFile(folder, "test_corpus", ".imf.xml");
		Files.write(file, sb.toString().getBytes(StandardCharsets.UTF_8));
		return file;
	}

	/**
	 * Creates a test corpus that contains a number of sentences equal to the
	 * size oft he given {@code setup} array. Individual sentences will contain
	 * equal to the respective value in the array.
	 * Each token t<sub>ij</sub> (located at index j in sentence i) will have
	 * 3 annotations assigned to it:
	 * <ul>
	 * <li>"counter" will be equal to the sentence-internal position j</li>
	 * <li>"anno1" will read "anno1_i_j"</li>
	 * <li>"anno2" will read "anno2_i_j"</li>
	 * </ul>
	 * @throws IOException
	 * @throws SAXException
	 * @throws InterruptedException
	 */
	public static Corpus createDummyCorpus(Path folder, DummyType type, int...setup) throws SAXException, IOException, InterruptedException {
		Path file = createCorpusFile(folder, setup);
		Consumer<CorpusManifest> processor = corpus -> {
			LocationManifest loc = new LocationManifestImpl(corpus.getManifestLocation(), corpus.getRegistry());
			loc.setRootPathType(PathType.FILE);
			loc.setRootPath(file.toString());
			corpus.getRootContextManifest().get().addLocationManifest(loc);
		};


		CorpusManager manager = DefaultCorpusManager.builder()
				.defaultEnvironment()
				.build();
		ManifestRegistry registry = manager.getManifestRegistry();
		ManifestXmlReader reader = ManifestXmlReader.builder()
				.registry(registry)
				.useImplementationDefaults()
				.corpusProcessor(processor)
				.source(ManifestLocation.builder()
						.utf8()
						.url(DummyCorpus.class.getResource(type.path))
						.build())
				.build();

		CorpusManifest manifest = reader.parseCorpora().get(0);
		registry.addCorpusManifest(manifest);

		return manager.connect(manifest);
	}
}
