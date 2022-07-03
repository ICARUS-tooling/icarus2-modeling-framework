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
package de.ims.icarus2.common.formats.conll;

import static de.ims.icarus2.util.lang.Primitives._int;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.common.formats.conll.CoNLL2009Converter.CoNLL09Config;
import de.ims.icarus2.common.formats.conll.CoNLL2009Converter.LayerConfig;
import de.ims.icarus2.filedriver.Converter;
import de.ims.icarus2.filedriver.ElementFlag;
import de.ims.icarus2.filedriver.FileDataStates;
import de.ims.icarus2.filedriver.FileDataStates.ContainerInfo;
import de.ims.icarus2.filedriver.FileDataStates.FileInfo;
import de.ims.icarus2.filedriver.FileDataStates.LayerInfo;
import de.ims.icarus2.filedriver.FileDataStates.NumericalStats;
import de.ims.icarus2.filedriver.FileDriver;
import de.ims.icarus2.filedriver.schema.tabular.TableConverter;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.driver.ChunkState;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingReader;
import de.ims.icarus2.model.api.driver.mapping.RequestSettings;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.StructureLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest.PathType;
import de.ims.icarus2.model.manifest.api.ManifestFactory;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.standard.DefaultManifestFactory;
import de.ims.icarus2.model.standard.registry.DefaultCorpusManager;
import de.ims.icarus2.util.Counter;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.collections.ArrayUtils;

/**
 * @author Markus Gärtner
 *
 */
class CoNLL2009SchemaConverterTest {

	@Nested
	class WithFullCorpus {

		private TableConverter converter;
		private CorpusManager manager;
		private Corpus corpus;

		private <L extends Layer> L getLayer(LayerConfig c) {
			return converter.getDriver().getContext().getLayer(c.defaultValue);
		}

		private Container getSentence(int index) {
			ItemLayer layer = getLayer(CoNLL2009Converter.CoNLL09Config.SENTENCE);
			return (Container) converter.getDriver().getItem(layer, index);
		}

		private Structure getStructure(int index, LayerConfig c) {
			StructureLayer layer = getLayer(c);
			return (Structure) converter.getDriver().getItem(layer, index);
		}

		private Edge getHead(int index, LayerConfig c, Item node) {
			Structure syntax = getStructure(index, c);
			Edge edge = syntax.getEdgeAt(node, 0, false);
			assertThat(edge).isNotNull();
			return edge;
		}

		@BeforeEach
		void setUp() throws Exception {

			manager = DefaultCorpusManager.builder()
					.defaultEnvironment()
					.build();

			ManifestRegistry registry = manager.getManifestRegistry();
			CoNLLTemplates.registerTeplates(registry);

			ManifestFactory factory = new DefaultManifestFactory(
					ManifestLocation.builder()
					.input()
					.content("")
					.virtual()
					.build(), registry);

			CorpusManifest manifest = factory.create(CorpusManifest.class)
					.addRootContextManifest((ContextManifest) factory.create(ContextManifest.class)
							.setId("root")
							.addLocationManifest(factory.create(LocationManifest.class)
									.setRootPathType(PathType.FILE)
									.setRootPath(Paths.get(CoNLLUtils.getCorpusUrl().toURI()).toString()))
							.setTemplateId(CoNLLTemplates.CONLL09_SCHEMA_TEMPLATE))
					.setId("test")
					.setName("Test Corpus");

			registry.addCorpusManifest(manifest);

			corpus = manager.connect(manifest);

			Driver driver = corpus.getRootContext().getDriver();
			assertThat(driver).isInstanceOf(FileDriver.class);

			Converter converter = ((FileDriver)driver).getConverter();
			assertThat(converter).isInstanceOf(TableConverter.class);

			this.converter = (TableConverter) converter;
		}

		@AfterEach
		void tearDown() throws Exception {
			manager.shutdown();
			manager = null;
			converter = null;
			corpus = null;
		}


		private void loadAll() throws IOException, InterruptedException, IcarusApiException {
			converter.getDriver().loadAllFiles(info -> { /* no-op */ });
		}


		@Test
		void testConverterInitialized() {
			assertThat(converter.isAdded()).isTrue();
			assertThat(converter.isReady()).isTrue();
			assertThat(converter.isBusy()).isFalse();
		}

		@Test
		void testScan()  throws Exception{

			assertThat(converter.getDriver().scanAllFiles()).isTrue();

			FileDataStates fileStates = converter.getDriver().getFileStates();
			FileInfo fileInfo = fileStates.getFileInfo(0);
			assertThat(fileInfo.getIndex()).as("index").isEqualTo(0);
			assertThat(fileInfo.isFlagSet(ElementFlag.SCANNED)).as("SCANNED flag").isTrue();


			//TODO assert that all FileInfo objects list SCANNED as state
		}

		@Test
		void testLoading()  throws Exception{

			MutableLong chunkCount = new MutableLong();

			Counter<ChunkState> states = new Counter<>();

			converter.getDriver().loadAllFiles(info -> {
				chunkCount.incrementAndGet(info.chunkCount());
				for (int i = 0; i < info.chunkCount(); i++) {
					states.increment(info.getState(i));
				}
			});

			// Test corpus contains exactly 10 sentences
			assertThat(states.getCount(ChunkState.CORRUPTED)).isEqualTo(0);
			assertThat(states.getCount(ChunkState.MODIFIED)).isEqualTo(0);
			assertThat(states.getCount(ChunkState.VALID)).isEqualTo(10);
		}

		@Test
		void testRepeatedLoading()  throws Exception{

			// First full loading pass
			loadAll();

			MutableLong chunkCount = new MutableLong();

			converter.getDriver().loadAllFiles(info -> {
				chunkCount.incrementAndGet(info.chunkCount());
			});

			assertThat(chunkCount.longValue()).isEqualTo(0);
		}

		@Nested
		class ForMetadata {
			FileDataStates fileStates;

			@SuppressWarnings("boxing")
			private void assertStats(String msg, NumericalStats stats, long expectedMin,
					long expectedMax, double expectedAvg) {
				assertThat(stats.getMin()).as(msg+" - min").isEqualTo(expectedMin);
				assertThat(stats.getMax()).as(msg+" - max").isEqualTo(expectedMax);
				assertThat(stats.getAvg()).as(msg+" - avg").isCloseTo(expectedAvg, offset(0.1));
			}

			@BeforeEach
			void setUp() throws Exception {
				loadAll();
				fileStates = converter.getDriver().getFileStates();
			}

			@Test
			void testTokenInfo()  throws Exception{
				ItemLayer layer = getLayer(CoNLL2009Converter.CoNLL09Config.TOKEN);
				LayerInfo info = fileStates.getLayerInfo(layer.getManifest());
				assertThat(info.getSize())
					.as("total sentence count")
					.isEqualTo(211);
			}

			@Test
			void testSentenceInfo()  throws Exception{
				ItemLayer layer = getLayer(CoNLL2009Converter.CoNLL09Config.SENTENCE);
				LayerInfo layerIinfo = fileStates.getLayerInfo(layer.getManifest());
				ContainerInfo info = layerIinfo.getRootContainerInfo();
				assertThat(layerIinfo.getSize())
					.as("total sentence count")
					.isEqualTo(10);
				assertThat(info.getEncounteredContainerTypes())
					.as("encountered container types")
					.containsExactly(ContainerType.SPAN);
				assertThat(info.getEncounteredStructureTypes())
					.as("encoutnered structure types")
					.isEmpty();
				assertStats("item count", info.getItemCountStats(), 7, 36, 21.1);
			}
		}

		@Nested
		class ForStructure {

			@BeforeEach
			void setUp() throws Exception {
				loadAll();
			}

			private void assertStructure(int sentenceIndex, LayerConfig c, int...heads) {
				String prefix = c.toString();
				Structure syntax = getStructure(sentenceIndex, c);
				Container sentence = getSentence(sentenceIndex);
				assertThat(syntax.getItemCount()).as(prefix+" item count").isEqualTo(heads.length);
				assertThat(syntax.getEdgeCount()).as(prefix+" edge count").isEqualTo(heads.length);

				for (int i = 0; i < heads.length; i++) {
					String msg = String.format("%s node %d", prefix, _int(i));
					Item node = syntax.getItemAt(i);
					assertThat(node).as(msg+" identity").isSameAs(sentence.getItemAt(i));
					assertThat(syntax.getIncomingEdgeCount(node)).as(msg+" headless", prefix, _int(i)).isEqualTo(1);
					Edge edge = syntax.getEdgeAt(node, 0, false);
					assertThat(edge).as(msg+" missing head edge").isNotNull();
					Item parent = edge.getSource();
					int head = heads[i];
					if(head==0) {
						assertThat(parent).as(msg+" not root").isSameAs(syntax.getVirtualRoot());
					} else {
						assertThat(parent).as(msg+" parent").isSameAs(syntax.getItemAt(head-1));
					}
				}
			}

			/**
			 * 1st sentence
			 *
			  	1	Icarus	icarus	icarus	NN	NN	_	_	2	2	SBJ	SBJ	_	_	_
				2	was	be	be	VBD	VBD	_	_	0	0	ROOT	ROOT	_	_	_
				3	the	the	the	DT	DT	_	_	4	4	NMOD	NMOD	_	_	_
				4	son	son	son	NN	NN	_	_	2	2	PRD	PRD	Y	son.01	A0
				5	of	of	of	IN	IN	_	_	4	4	NMOD	NMOD	_	_	A1
				6	Daedalus	daedalus	daedalus	NNP	NNP	_	_	5	5	PMOD	PMOD	_	_	_
				7	.	.	.	.	.	_	_	2	2	P	P	_	_	_
			 */
			@Test
			void testSentence_01() {

				assertStructure(0, CoNLL09Config.GOLD_SYNTAX, 2, 0, 4, 2, 4, 5, 2);
				assertStructure(0, CoNLL09Config.PREDICTED_SYNTAX, 2, 0, 4, 2, 4, 5, 2);
			}

			//TODO test more sentences
		}

		@Nested
		class ForAnnotations {

			@BeforeEach
			void setUp() throws Exception {
				loadAll();
			}

			private void assertAnnotation(Item item, LayerConfig c, String value) {
				AnnotationLayer layer = getLayer(c);
				assertThat(layer).isNotNull();
				String actual = layer.getAnnotationStorage().getString(item, layer.getManifest().getDefaultKey().get());
				assertThat(actual).as(c.toString()).isNotNull();
				assertThat(actual).as(c.toString()).isEqualTo(value);
			}

			private void assertAnnotations(int sentenceIndex, int tokenIndex, String form,
					String lemma, String plemma, String pos, String ppos, int head, int phead,
					String depRel, String pdepRel) throws Exception {

				Container sentence = getSentence(sentenceIndex);
				Item token = sentence.getItemAt(tokenIndex);
				assertAnnotation(token, CoNLL09Config.FORM, form);
				assertAnnotation(token, CoNLL09Config.GOLD_LEMMA, lemma);
				assertAnnotation(token, CoNLL09Config.PREDICTED_LEMMA, plemma);
				assertAnnotation(token, CoNLL09Config.GOLD_POS, pos);
				assertAnnotation(token, CoNLL09Config.PREDICTED_POS, ppos);

				//TODO enable edge annotation assertions again
				Edge edge = getHead(sentenceIndex, CoNLL09Config.GOLD_SYNTAX, token);
				assertAnnotation(edge, CoNLL09Config.GOLD_DEPENDENCY_RELATION, depRel);
				Edge pedge = getHead(sentenceIndex, CoNLL09Config.PREDICTED_SYNTAX, token);
				assertAnnotation(pedge, CoNLL09Config.PREDICTED_DEPENDENCY_RELATION, pdepRel);
			}

			/**
			 * 1st sentence
			 *
			  	1	Icarus	icarus	icarus	NN	NN	_	_	2	2	SBJ	SBJ	_	_	_
				2	was	be	be	VBD	VBD	_	_	0	0	ROOT	ROOT	_	_	_
				3	the	the	the	DT	DT	_	_	4	4	NMOD	NMOD	_	_	_
				4	son	son	son	NN	NN	_	_	2	2	PRD	PRD	Y	son.01	A0
				5	of	of	of	IN	IN	_	_	4	4	NMOD	NMOD	_	_	A1
				6	Daedalus	daedalus	daedalus	NNP	NNP	_	_	5	5	PMOD	PMOD	_	_	_
				7	.	.	.	.	.	_	_	2	2	P	P	_	_	_
			 */
			@CsvSource(delimiter = '\t', value = {
				"1	Icarus	icarus	icarus	NN	NN	_	_	2	2	SBJ	SBJ",
				"2	was	be	be	VBD	VBD	_	_	0	0	ROOT	ROOT",
				"3	the	the	the	DT	DT	_	_	4	4	NMOD	NMOD",
				"4	son	son	son	NN	NN	_	_	2	2	PRD	PRD",
				"5	of	of	of	IN	IN	_	_	4	4	NMOD	NMOD",
				"6	Daedalus	daedalus	daedalus	NNP	NNP	_	_	5	5	PMOD	PMOD",
				"7	.	.	.	.	.	_	_	2	2	P	P",
			})
			@ParameterizedTest
			void testSentence_01(int tokenIndex, String form,
					String lemma, String plemma, String pos, String ppos,
					String feats, String pfeats, int head, int phead,
					String depRel, String pdepRel) throws Exception {
				final int sentenceIndex = 0;
				assertAnnotations(sentenceIndex, tokenIndex-1, form, lemma, plemma, pos, ppos, head, phead, depRel, pdepRel);
			}

			/**
			 * 1st sentence
			 *
				1	Daedalus	daedalus	daedalus	NNP	NNP	_	_	2	2	SBJ	SBJ	_	_	A1	_	_	_	_	_	_
				2	was	be	be	VBD	VBD	_	_	0	0	ROOT	ROOT	_	_	_	_	_	_	_	_	_
				3	imprisoned	imprison	imprison	VBN	VBN	_	_	2	2	VC	VC	Y	imprison.01	_	_	_	_	_	_	_
				4	in	in	in	IN	IN	_	_	3	3	LOC	LOC	_	_	AM-LOC	_	_	_	_	_	_
				5	his	his	his	PRP$	PRP$	_	_	7	7	NMOD	NMOD	_	_	_	A0	_	_	_	_	_
				6	own	own	own	JJ	JJ	_	_	7	7	NMOD	NMOD	_	_	_	_	_	_	_	_	_
				7	invention	invention	invention	NN	NN	_	_	4	4	PMOD	PMOD	Y	invention.01	_	_	_	_	_	_	_
				8	,	,	,	,	,	_	_	3	3	P	P	_	_	_	_	_	_	_	_	_
				9	the	the	the	DT	DT	_	_	10	10	NMOD	NMOD	_	_	_	_	_	_	_	_	_
				10	labyrinth	labyrinth	labyrinth	NN	NN	_	_	3	3	ADV	ADV	_	_	A1	_	_	_	_	_	_
				11	because	because	because	IN	IN	_	_	3	3	PRP	PRP	_	_	AM-CAU	_	_	_	_	_	_
				12	he	he	he	PRP	PRP	_	_	13	13	SBJ	SBJ	_	_	_	_	A0	A0	_	_	_
				13	helped	help	help	VBD	VBD	_	_	11	11	SUB	SUB	Y	help.01	_	_	_	_	_	_	_
				14	the	the	the	DT	DT	_	_	16	16	NMOD	NMOD	_	_	_	_	_	_	_	_	_
				15	hero	hero	hero	NN	NN	_	_	16	16	NMOD	NMOD	_	_	_	_	_	A1	_	_	_
				16	Theses	these	these	NNS	NNS	_	_	13	13	OBJ	OBJ	Y	these.01	_	_	A1	_	A0	_	_
				17	kill	kill	kill	VBP	VBP	_	_	13	13	OPRD	OPRD	Y	kill.01	_	_	C-A1	_	_	_	_
				18	King	king	king	NNP	NNP	_	_	19	19	TITLE	TITLE	_	_	_	_	_	_	_	_	_
				19	Minos	minos	minos	NNP	NNP	_	_	21	21	NMOD	NMOD	_	_	_	_	_	_	_	_	_
				20	's	's	's	POS	POS	_	_	19	19	SUFFIX	SUFFIX	_	_	_	_	_	_	_	_	_
				21	beast	beast	beast	NN	NN	_	_	17	17	OBJ	OBJ	_	_	_	_	_	_	A1	_	_
				22	,	,	,	,	,	_	_	21	21	P	P	_	_	_	_	_	_	_	_	_
				23	the	the	the	DT	DT	_	_	24	24	NMOD	NMOD	_	_	_	_	_	_	_	_	_
				24	Minotaur	minotaur	minotaur	NNP	NNP	_	_	21	21	APPO	APPO	_	_	_	_	_	_	_	_	_
				25	,	,	,	,	,	_	_	21	21	P	P	_	_	_	_	_	_	_	_	_
				26	and	and	and	CC	CC	_	_	17	17	COORD	COORD	_	_	_	_	_	_	_	_	_
				27	run	run	run	VBP	VBP	_	_	26	26	CONJ	CONJ	Y	run.01	_	_	_	_	_	_	_
				28	away	away	away	RB	RB	_	_	27	27	DIR	DIR	_	_	_	_	_	_	_	AM-DIR	_
				29	with	with	with	IN	IN	_	_	27	27	ADV	ADV	_	_	_	_	_	_	_	AM-MNR	_
				30	King	king	king	NNP	NNP	_	_	31	31	TITLE	TITLE	_	_	_	_	_	_	_	_	_
				31	Minos	minos	minos	NNP	NNP	_	_	33	33	NMOD	NMOD	_	_	_	_	_	_	_	_	A1
				32	's	's	's	POS	POS	_	_	31	31	SUFFIX	SUFFIX	_	_	_	_	_	_	_	_	_
				33	daughter	daughter	daughter	NN	NN	_	_	29	29	PMOD	PMOD	Y	daughter.01	_	_	_	_	_	_	A0
				34	,	,	,	,	,	_	_	33	33	P	P	_	_	_	_	_	_	_	_	_
				35	Ariadne	ariadne	ariadne	NNP	NNP	_	_	33	33	APPO	APPO	_	_	_	_	_	_	_	_	A1
				36	.	.	.	.	.	_	_	2	2	P	P	_	_	_	_	_	_	_	_	_
			 */
			@CsvSource(delimiter = '\t', quoteCharacter = '"', value = {
				"1	Daedalus	daedalus	daedalus	NNP	NNP	_	_	2	2	SBJ	SBJ",
				"2	was	be	be	VBD	VBD	_	_	0	0	ROOT	ROOT",
				"3	imprisoned	imprison	imprison	VBN	VBN	_	_	2	2	VC	VC",
				"4	in	in	in	IN	IN	_	_	3	3	LOC	LOC",
				"5	his	his	his	PRP$	PRP$	_	_	7	7	NMOD	NMOD",
				"6	own	own	own	JJ	JJ	_	_	7	7	NMOD	NMOD",
				"7	invention	invention	invention	NN	NN	_	_	4	4	PMOD	PMOD",
				"8	,	,	,	,	,	_	_	3	3	P	P",
				"9	the	the	the	DT	DT	_	_	10	10	NMOD	NMOD",
				"10	labyrinth	labyrinth	labyrinth	NN	NN	_	_	3	3	ADV	ADV",
				"11	because	because	because	IN	IN	_	_	3	3	PRP	PRP",
				"12	he	he	he	PRP	PRP	_	_	13	13	SBJ	SBJ",
				"13	helped	help	help	VBD	VBD	_	_	11	11	SUB	SUB",
				"14	the	the	the	DT	DT	_	_	16	16	NMOD	NMOD",
				"15	hero	hero	hero	NN	NN	_	_	16	16	NMOD	NMOD",
				"16	Theses	these	these	NNS	NNS	_	_	13	13	OBJ	OBJ",
				"17	kill	kill	kill	VBP	VBP	_	_	13	13	OPRD	OPRD",
				"18	King	king	king	NNP	NNP	_	_	19	19	TITLE	TITLE",
				"19	Minos	minos	minos	NNP	NNP	_	_	21	21	NMOD	NMOD",
				"20	's	's	's	POS	POS	_	_	19	19	SUFFIX	SUFFIX",
				"21	beast	beast	beast	NN	NN	_	_	17	17	OBJ	OBJ",
				"22	,	,	,	,	,	_	_	21	21	P	P",
				"23	the	the	the	DT	DT	_	_	24	24	NMOD	NMOD",
				"24	Minotaur	minotaur	minotaur	NNP	NNP	_	_	21	21	APPO	APPO",
				"25	,	,	,	,	,	_	_	21	21	P	P",
				"26	and	and	and	CC	CC	_	_	17	17	COORD	COORD",
				"27	run	run	run	VBP	VBP	_	_	26	26	CONJ	CONJ",
				"28	away	away	away	RB	RB	_	_	27	27	DIR	DIR",
				"29	with	with	with	IN	IN	_	_	27	27	ADV	ADV",
				"30	King	king	king	NNP	NNP	_	_	31	31	TITLE	TITLE",
				"31	Minos	minos	minos	NNP	NNP	_	_	33	33	NMOD	NMOD",
				"32	's	's	's	POS	POS	_	_	31	31	SUFFIX	SUFFIX",
				"33	daughter	daughter	daughter	NN	NN	_	_	29	29	PMOD	PMOD",
				"34	,	,	,	,	,	_	_	33	33	P	P",
				"35	Ariadne	ariadne	ariadne	NNP	NNP	_	_	33	33	APPO	APPO",
				"36	.	.	.	.	.	_	_	2	2	P	P",
			})
			@ParameterizedTest
			void testSentence_02(int tokenIndex, String form,
					String lemma, String plemma, String pos, String ppos,
					String feats, String pfeats, int head, int phead,
					String depRel, String pdepRel) throws Exception {
				final int sentenceIndex = 1;
				assertAnnotations(sentenceIndex, tokenIndex-1, form, lemma, plemma, pos, ppos, head, phead, depRel, pdepRel);
			}

			//TODO test other sentences!

			/**
			 * 2nd to last sentence (id 8)
			 *
			  	1	Daedalus	daedalus	daedalus	NNP	NNP	_	_	2	2	SBJ	SBJ	_	_	A0	A0	_	_	_
				2	looked	look	look	VBD	VBD	_	_	0	0	ROOT	ROOT	Y	look.01	_	_	_	_	_
				3	down	down	down	RP	RP	_	_	2	2	ADV	ADV	_	_	AM-DIR	_	_	_	_
				4	towards	towards	towards	IN	IN	_	_	2	2	ADV	ADV	_	_	A1	_	_	_	_
				5	the	the	the	DT	DT	_	_	6	6	NMOD	NMOD	_	_	_	_	_	_	_
				6	sea	sea	sea	NN	NN	_	_	4	4	PMOD	PMOD	_	_	_	_	_	_	_
				7	and	and	and	CC	CC	_	_	2	2	COORD	COORD	_	_	_	_	_	_	_
				8	saw	saw	saw	VBD	VBD	_	_	7	7	CONJ	CONJ	Y	see.01	_	_	_	_	_
				9	the	the	the	DT	DT	_	_	10	10	NMOD	NMOD	_	_	_	_	_	_	_
				10	feathers	feather	feather	NNS	NNS	_	_	8	8	OBJ	OBJ	_	_	_	A1	A1	_	_
				11	floating	floating	floating	VBG	VBG	_	_	8	8	OPRD	OPRD	Y	floating.01	_	C-A1	_	_	_
				12	on	on	on	IN	IN	_	_	11	11	LOC	LOC	_	_	_	_	AM-LOC	_	_
				13	the	the	the	DT	DT	_	_	14	14	NMOD	NMOD	_	_	_	_	_	_	_
				14	water	water	water	NN	NN	_	_	12	12	PMOD	PMOD	_	_	_	_	_	_	_
				15	and	and	and	CC	CC	_	_	8	8	COORD	COORD	_	_	_	_	_	_	_
				16	figured	figure	figure	VBD	VBD	_	_	15	15	CONJ	CONJ	Y	figure.05	_	_	_	_	_
				17	out	out	out	RP	RP	_	_	16	16	PRT	PRT	_	_	_	_	_	_	_
				18	what	what	what	WP	WP	_	_	19	19	SBJ	SBJ	_	_	_	_	_	_	R-A1
				19	happened	happen	happen	VBD	VBD	_	_	16	16	OBJ	OBJ	Y	happen.01	_	_	_	A1	_
				20	.	.	.	.	.	_	_	2	2	P	P	_	_	_	_	_	_	_
			 */
			@CsvSource(delimiter = '\t', quoteCharacter = '"', value = {
				"1	Daedalus	daedalus	daedalus	NNP	NNP	_	_	2	2	SBJ	SBJ",
				"2	was	be	be	VBD	VBD	_	_	0	0	ROOT	ROOT",
				"3	imprisoned	imprison	imprison	VBN	VBN	_	_	2	2	VC	VC",
				"4	in	in	in	IN	IN	_	_	3	3	LOC	LOC",
				"5	his	his	his	PRP$	PRP$	_	_	7	7	NMOD	NMOD",
				"6	own	own	own	JJ	JJ	_	_	7	7	NMOD	NMOD",
				"7	invention	invention	invention	NN	NN	_	_	4	4	PMOD	PMOD",
				"8	,	,	,	,	,	_	_	3	3	P	P",
				"9	the	the	the	DT	DT	_	_	10	10	NMOD	NMOD",
				"10	labyrinth	labyrinth	labyrinth	NN	NN	_	_	3	3	ADV	ADV",
				"11	because	because	because	IN	IN	_	_	3	3	PRP	PRP",
				"12	he	he	he	PRP	PRP	_	_	13	13	SBJ	SBJ",
				"13	helped	help	help	VBD	VBD	_	_	11	11	SUB	SUB",
				"14	the	the	the	DT	DT	_	_	16	16	NMOD	NMOD",
				"15	hero	hero	hero	NN	NN	_	_	16	16	NMOD	NMOD",
				"16	Theses	these	these	NNS	NNS	_	_	13	13	OBJ	OBJ",
				"17	kill	kill	kill	VBP	VBP	_	_	13	13	OPRD	OPRD",
				"18	King	king	king	NNP	NNP	_	_	19	19	TITLE	TITLE",
				"19	Minos	minos	minos	NNP	NNP	_	_	21	21	NMOD	NMOD",
				"20	's	's	's	POS	POS	_	_	19	19	SUFFIX	SUFFIX",
				"21	beast	beast	beast	NN	NN	_	_	17	17	OBJ	OBJ",
				"22	,	,	,	,	,	_	_	21	21	P	P",
				"23	the	the	the	DT	DT	_	_	24	24	NMOD	NMOD",
				"24	Minotaur	minotaur	minotaur	NNP	NNP	_	_	21	21	APPO	APPO",
				"25	,	,	,	,	,	_	_	21	21	P	P",
				"26	and	and	and	CC	CC	_	_	17	17	COORD	COORD",
				"27	run	run	run	VBP	VBP	_	_	26	26	CONJ	CONJ",
				"28	away	away	away	RB	RB	_	_	27	27	DIR	DIR",
				"29	with	with	with	IN	IN	_	_	27	27	ADV	ADV",
				"30	King	king	king	NNP	NNP	_	_	31	31	TITLE	TITLE",
				"31	Minos	minos	minos	NNP	NNP	_	_	33	33	NMOD	NMOD",
				"32	's	's	's	POS	POS	_	_	31	31	SUFFIX	SUFFIX",
				"33	daughter	daughter	daughter	NN	NN	_	_	29	29	PMOD	PMOD",
				"34	,	,	,	,	,	_	_	33	33	P	P",
				"35	Ariadne	ariadne	ariadne	NNP	NNP	_	_	33	33	APPO	APPO",
				"36	.	.	.	.	.	_	_	2	2	P	P",
			})
			@ParameterizedTest
			void testSentence_08(int tokenIndex, String form,
					String lemma, String plemma, String pos, String ppos,
					String feats, String pfeats, int head, int phead,
					String depRel, String pdepRel) throws Exception {
				final int sentenceIndex = 1;
				assertAnnotations(sentenceIndex, tokenIndex-1, form, lemma, plemma, pos, ppos, head, phead, depRel, pdepRel);
			}

		}

		@Nested
		class ForMapping {

			private MappingReader reader(LayerConfig source, LayerConfig target) {
				ItemLayer sourceLayer = getLayer(source);
				ItemLayer targetLayer = getLayer(target);
				Mapping mapping = converter.getDriver().getMapping(sourceLayer, targetLayer);
				return mapping.newReader();
			}

			@Test
			void testSent2GoldSyntax() throws InterruptedException {
				MappingReader reader = reader(CoNLL09Config.SENTENCE, CoNLL09Config.GOLD_SYNTAX);
				reader.begin();
				try {
					RequestSettings settings = RequestSettings.none();
					for (int sentenceIndex = 0; sentenceIndex < 10; sentenceIndex++) {
						assertThat(reader.getIndicesCount(sentenceIndex, settings)).as("length").isEqualTo(1);
						assertThat(reader.getBeginIndex(sentenceIndex, settings)).as("begin index").isEqualTo(sentenceIndex);
						assertThat(reader.getEndIndex(sentenceIndex, settings)).as("end index").isEqualTo(sentenceIndex);
						IndexSet[] indices = reader.lookup(sentenceIndex, settings);
						assertThat(indices).hasSize(1);
						assertThat(IndexUtils.asArray(indices[0])).as("indices").containsExactly(sentenceIndex);
					}
				} finally {
					reader.end();
				}
			}

			@Test
			void testSent2PredictedSyntax() throws InterruptedException {
				MappingReader reader = reader(CoNLL09Config.SENTENCE, CoNLL09Config.PREDICTED_SYNTAX);
				reader.begin();
				try {
					RequestSettings settings = RequestSettings.none();
					for (int sentenceIndex = 0; sentenceIndex < 10; sentenceIndex++) {
						assertThat(reader.getIndicesCount(sentenceIndex, settings)).as("length").isEqualTo(1);
						assertThat(reader.getBeginIndex(sentenceIndex, settings)).as("begin index").isEqualTo(sentenceIndex);
						assertThat(reader.getEndIndex(sentenceIndex, settings)).as("end index").isEqualTo(sentenceIndex);
						IndexSet[] indices = reader.lookup(sentenceIndex, settings);
						assertThat(indices).hasSize(1);
						assertThat(IndexUtils.asArray(indices[0])).as("indices").containsExactly(sentenceIndex);
					}
				} finally {
					reader.end();
				}
			}

			@CsvSource({
				"0, 0, 6",
				"1, 7, 42",
				"2, 43, 49",
				"3, 50, 70",
				"4, 71, 78",
				"5, 79, 94",
				"6, 95, 126",
				"7, 127, 162",
				"8, 163, 182",
				"9, 183, 210",
			})
			@ParameterizedTest
			void testTok2Sent(int sentenceIndex, int tokenStart, int tokenEnd) throws InterruptedException {
				MappingReader reader = reader(CoNLL09Config.TOKEN, CoNLL09Config.SENTENCE);
				reader.begin();
				try {
					RequestSettings settings = RequestSettings.none();
					for (int tokenIndex = tokenStart; tokenIndex <= tokenEnd; tokenIndex++) {
						assertThat(reader.getIndicesCount(tokenIndex, settings)).as("length for %d",_int(tokenIndex)).isEqualTo(1);
						assertThat(reader.getBeginIndex(tokenIndex, settings)).as("begin index for %d",_int(tokenIndex)).isEqualTo(sentenceIndex);
						assertThat(reader.getEndIndex(tokenIndex, settings)).as("end index for %d",_int(tokenIndex)).isEqualTo(sentenceIndex);
						IndexSet[] indices = reader.lookup(tokenIndex, settings);
						assertThat(indices).hasSize(1);
						assertThat(IndexUtils.asArray(indices[0])).as("indices for %d",_int(tokenIndex)).containsExactly(sentenceIndex);
					}
				} finally {
					reader.end();
				}
			}

			@CsvSource({
				"0, 0, 6",
				"1, 7, 42",
				"2, 43, 49",
				"3, 50, 70",
				"4, 71, 78",
				"5, 79, 94",
				"6, 95, 126",
				"7, 127, 162",
				"8, 163, 182",
				"9, 183, 210",
			})
			@ParameterizedTest
			void testSent2Tok(int sentenceIndex, int tokenStart, int tokenEnd) throws InterruptedException {
				int length = tokenEnd-tokenStart+1;
				MappingReader reader = reader(CoNLL09Config.SENTENCE, CoNLL09Config.TOKEN);
				reader.begin();
				try {
					RequestSettings settings = RequestSettings.none();
					assertThat(reader.getIndicesCount(sentenceIndex, settings)).as("length").isEqualTo(length);
					assertThat(reader.getBeginIndex(sentenceIndex, settings)).as("begin index").isEqualTo(tokenStart);
					assertThat(reader.getEndIndex(sentenceIndex, settings)).as("end index").isEqualTo(tokenEnd);
					IndexSet[] indices = reader.lookup(sentenceIndex, settings);
					assertThat(indices).hasSize(1);
					long[] expected = new long[length];
					ArrayUtils.fillAscending(expected, tokenStart);
					assertThat(IndexUtils.asArray(indices[0])).as("indices").containsExactly(expected);
				} finally {
					reader.end();
				}
			}
		}
	}

}
