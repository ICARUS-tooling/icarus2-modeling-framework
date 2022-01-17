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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.xml.sax.SAXException;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest.PathType;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.standard.DefaultManifestRegistry;
import de.ims.icarus2.model.manifest.standard.LocationManifestImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlReader;
import de.ims.icarus2.model.standard.io.DefaultFileManager;
import de.ims.icarus2.model.standard.registry.DefaultCorpusManager;
import de.ims.icarus2.model.standard.registry.metadata.VirtualMetadataRegistry;
import de.ims.icarus2.util.io.resource.VirtualIOResource;
import de.ims.icarus2.util.io.resource.VirtualResourceProvider;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

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

	public static final String UNKNOWN_LAYER = "not-there";

	public static final String ANNO_COUNTER = "counter";
	public static final String ANNO_1 = "anno1";
	public static final String ANNO_2 = "anno2";

	public static final String UNKNOWN_KEY = "anno3";

	private static final Map<String, ManifestType> typeLookup = new Object2ObjectOpenHashMap<>();
	static {
		for(String layer : new String[] {LAYER_TOKEN, LAYER_TOKEN_2, LAYER_SENTENCE, LAYER_SENTENCE_2}) {
			typeLookup.put(layer, ManifestType.ITEM_LAYER_MANIFEST);
		}
		for(String layer : new String[] {LAYER_ANNO, LAYER_ANNO_2}) {
			typeLookup.put(layer, ManifestType.STRUCTURE_LAYER_MANIFEST);
		}
		for(String layer : new String[] {LAYER_SYNTAX, LAYER_SYNTAX_2}) {
			typeLookup.put(layer, ManifestType.ANNOTATION_LAYER_MANIFEST);
		}
	}

	public enum DummyType {
		FLAT("tpl_flat_corpus.imf.xml", LAYER_TOKEN, LAYER_ANNO),
		HIERARCHICAL("tpl_hierarchical_corpus.imf.xml", LAYER_TOKEN, LAYER_SENTENCE, LAYER_ANNO),
		HIERARCHICAL_BLANK("tpl_hierarchical_blank_corpus.imf.xml", LAYER_TOKEN, LAYER_SENTENCE, LAYER_ANNO),
		FULL("tpl_full_corpus.imf.xml", LAYER_TOKEN, LAYER_TOKEN_2, LAYER_SENTENCE, LAYER_SENTENCE_2,
				LAYER_ANNO, LAYER_ANNO_2, LAYER_SYNTAX, LAYER_SYNTAX_2),
		;
		public final String path;
		private final Set<String> layers = new ObjectOpenHashSet<>();

		private DummyType(String path, String...layers) {
			this.path = path;
			Collections.addAll(this.layers, layers);
		}

		public Map<String, ManifestType> getLayers() {
			Map<String, ManifestType> map = new Object2ObjectOpenHashMap<>();
			for(String layer : layers) {
				map.put(layer, typeLookup.get(layer));
			}
			return map;
		}
	}

	private static void fillFlatContent(StringBuilder sb, int size) {
		for (int i = 0; i < size; i++) {
			sb.append(i)
				.append('\t').append("anno1_").append(0).append('_').append(i)
				.append('\t').append("anno2_").append(0).append('_').append(i)
				.append('\n');
		}
	}

	private static void fillHierarchicalContent(StringBuilder sb, int...setup) {
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
	}

	private static void fillHierarchicalBlankContent(StringBuilder sb, int...setup) {
		for (int i = 0; i < setup.length; i++) {
			if(i>0) {
				sb.append('\n');
			}
			for (int j = 0; j < setup[i]; j++) {
				sb.append(j).append('\n');
			}
		}
	}

	private static void fillFullContent(StringBuilder sb, int...setup) {
		//TODO implement and remove delegation
		fillHierarchicalContent(sb, setup);
	}

	public static void createCorpusFile(VirtualResourceProvider resourceProvider,
			Path file, DummyType type, int...setup) throws IOException {
		StringBuilder sb = new StringBuilder(setup.length * 100);

		switch (type) {
		case FLAT:
			assertThat(setup).hasSize(1);
			fillFlatContent(sb, setup[0]);
			break;

		case HIERARCHICAL:
			fillHierarchicalContent(sb, setup);
			break;

		case HIERARCHICAL_BLANK:
			fillHierarchicalBlankContent(sb, setup);
			break;

		case FULL:
			fillFullContent(sb, setup);
			break;

		default:
			break;
		}

		resourceProvider.create(file, false);
		VirtualIOResource resource = resourceProvider.getResource(file);
		resource.prepare();
		try(WritableByteChannel ch = resource.getWriteChannel()) {
			ch.write(ByteBuffer.wrap(sb.toString().getBytes(StandardCharsets.UTF_8)));
		}
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
	public static Corpus createDummyCorpus(DummyType type, int...setup) throws SAXException, IOException, InterruptedException {
		VirtualResourceProvider resourceProvider = new VirtualResourceProvider();
		Path file = Paths.get("test_corpus");
		createCorpusFile(resourceProvider, file, type, setup);
		Consumer<CorpusManifest> processor = corpus -> {
			LocationManifest loc = new LocationManifestImpl(corpus.getManifestLocation(), corpus.getRegistry());
			loc.setRootPathType(PathType.FILE);
			loc.setRootPath(file.toString());
			corpus.getRootContextManifest().get().addLocationManifest(loc);
		};


		CorpusManager manager = DefaultCorpusManager.builder()
				.fileManager(new DefaultFileManager(Paths.get(".")))
				.resourceProvider(resourceProvider)
				.metadataRegistry(new VirtualMetadataRegistry())
				.manifestRegistry(new DefaultManifestRegistry())
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
