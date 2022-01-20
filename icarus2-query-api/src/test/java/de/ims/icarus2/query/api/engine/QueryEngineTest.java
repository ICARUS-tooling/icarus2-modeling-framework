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

import static de.ims.icarus2.test.TestUtils.assertDeepEqual;
import static de.ims.icarus2.util.lang.Primitives._int;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.manifest.standard.DefaultManifestRegistry;
import de.ims.icarus2.model.standard.io.DefaultFileManager;
import de.ims.icarus2.model.standard.registry.DefaultCorpusManager;
import de.ims.icarus2.model.standard.registry.metadata.VirtualMetadataRegistry;
import de.ims.icarus2.query.api.Query;
import de.ims.icarus2.query.api.engine.QueryJob.JobController;
import de.ims.icarus2.query.api.engine.result.BufferedResultSink;
import de.ims.icarus2.query.api.engine.result.Match;
import de.ims.icarus2.query.api.iql.IqlCorpus;
import de.ims.icarus2.query.api.iql.IqlLayer;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.query.api.iql.IqlQueryGenerator;
import de.ims.icarus2.query.api.iql.IqlQueryGenerator.IncrementalBuild;
import de.ims.icarus2.query.api.iql.IqlResult;
import de.ims.icarus2.query.api.iql.IqlResult.ResultType;
import de.ims.icarus2.query.api.iql.IqlStream;
import de.ims.icarus2.query.api.iql.IqlType;
import de.ims.icarus2.query.api.iql.IqlUtils;
import de.ims.icarus2.test.annotations.IntArrayArg;
import de.ims.icarus2.test.annotations.IntMatrixArg;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.io.resource.VirtualResourceProvider;

/**
 * @author Markus Gärtner
 *
 */
class QueryEngineTest {

	private static final boolean DEBUG = false;

	@Nested
	class Internal {

		@Test
		void builder() {
			assertThat(QueryEngine.builder()).isNotNull();
		}
	}


	@Nested
	class ForReadQuery {

		/**
		 * Test method for {@link QueryEngine#readQuery(de.ims.icarus2.query.api.Query)}.
		 */
		@RandomizedTest
		@TestFactory
		@DisplayName("read incrementally built query")
		Stream<DynamicTest> testIncremental(RandomGenerator rng) {
			IqlQueryGenerator generator = new IqlQueryGenerator(rng);
			IncrementalBuild<IqlQuery> build = generator.build(IqlType.QUERY, IqlQueryGenerator.config());
			ObjectMapper mapper = IqlUtils.createMapper();

			return IntStream.rangeClosed(0, build.getChangeCount())
					.mapToObj(i -> dynamicTest(build.currentLabel(), () -> {
						if(i>0) {
							assertThat(build.applyNextChange()).isTrue();
						}
						IqlQuery original = build.getInstance();
						String json = mapper.writeValueAsString(original);

						if(DEBUG)
							System.out.println(json);

						QueryEngine engine = QueryEngine.builder()
								.mapper(mapper)
								.corpusManager(mock(CorpusManager.class))
								.useDefaultSettings()
								.build();
						IqlQuery query = engine.readQuery(new Query(json));
						assertDeepEqual(null, original, query, json);
					}));
		}

	}

	@Nested
	class ForEvaluateQuery {

		@Nested
		class ForVirtual {

			private CorpusManager manager;
			private VirtualResourceProvider provider;

			@BeforeEach
			void setUp() {
				provider = new VirtualResourceProvider();
				manager = DefaultCorpusManager.builder()
						.fileManager(new DefaultFileManager(Paths.get(".")))
						.resourceProvider(provider)
						.manifestRegistry(new DefaultManifestRegistry())
						.metadataRegistry(new VirtualMetadataRegistry())
						.build();
			}

			@AfterEach
			void tearDown() throws Exception {
				manager.shutdown();
				manager = null;

				provider.clear();
				provider = null;
			}

			private String createCorpusContent(String[] anno1, int[] sizes) {
				StringBuilder sb = new StringBuilder();

				int idx = 0;
				for (int i=0; i<sizes.length; i++) {
					if(i>0) {
						sb.append('\n');
					}
					for (int j = 0; j < sizes[i]; j++) {
						// <counter>\t<anno1>\t<empty>\n
						sb.append(j).append('\t')
							.append(anno1[idx++]).append('\t')
							.append('_').append('\n');
					}
				}

//				System.out.println(sb.toString());

				return sb.toString();

			}

			private IqlQuery createQuery(String rawPayload) {
				IqlCorpus corpus = new IqlCorpus();
				corpus.setId("corpus01");
				corpus.setName(DummyCorpus.CORPUS_ID);

				IqlResult result = new IqlResult();
				result.addResultType(ResultType.ID);

				IqlLayer layer = new IqlLayer();
				layer.setId("layer01");
				layer.setPrimary(true);
				layer.setName("sentence");

				IqlStream stream = new IqlStream();
				stream.setId("stream01");
				stream.setCorpus(corpus);
				stream.setRawPayload(rawPayload);
				stream.setResult(result);
				stream.addLayer(layer);

				IqlQuery query = new IqlQuery();
				query.setId("query01");
				query.addStream(stream);

				return query;
			}


			@ParameterizedTest
			@CsvSource({
				"'WITH $x FROM token FIND [$x:]', 10, {2;4;3;1}, {{0;1}{0;1;2;3}{0;1;2}{0}}",
				"'WITH $x FROM token FIND FIRST [$x:]', 10, {2;4;3;1}, {{0}{0}{0}{0}}",
				"'WITH $x FROM token FIND 2 HITS [$x:]', 10, {2;4;3;1}, {{0;1}{0;1}{0;1}{0}}",
			})
			public void testFlat(String constraint, int tokens, @IntArrayArg int[] containerSetup,
					// [container_id][global_id]
					@IntMatrixArg int[][] containerHits) throws Exception {

				String[] anno1 = IntStream.range(0, tokens)
						.mapToObj(i -> "tok"+i)
						.toArray(String[]::new);

				Corpus corpus = DummyCorpus.createDummyCorpus(Templates.HIERARCHICAL, createCorpusContent(anno1, containerSetup));

				QueryEngine engine = QueryEngine.builder()
						.corpusManager(corpus.getManager())
						.useDefaultMapper()
						.useDefaultSettings()
						.build();

				IqlQuery query = createQuery(constraint);

				BufferedResultSink resultSink = new BufferedResultSink(engine.getSettings());

				QueryJob job = engine.evaluateQuery(query, resultSink);

				JobController controller = job.execute(1);
				controller.start();
				controller.awaitFinish(5, TimeUnit.SECONDS);

				List<Throwable> exceptions = controller.getExceptions();
				if(!exceptions.isEmpty()) {
					for(Throwable t : exceptions) {
						t.printStackTrace(System.err);
					}
					fail("Unexpected internal errors");
				}

				int expectedMatchCount = Stream.of(containerHits)
						.mapToInt(hits -> hits.length)
						.sum();
				List<Match> matches = resultSink.getMatches();
				assertThat(matches).hasSize(expectedMatchCount);
				int idx = 0;
				for(int containerId = 0; containerId < containerHits.length; containerId++) {
					int[] hits = containerHits[containerId];
					if(hits==null || hits.length==0) {
						continue;
					}
					for(int hit : hits) {
						Match match = matches.get(idx);
						assertThat(match.getIndex())
							.as("Container index mismatch for match %d", _int(idx))
							.isEqualTo(containerId);
						assertThat(match.getMapCount()).isEqualTo(1);
						assertThat(match.getNode(0))
							.as("Mapping id mismatch for match %d", _int(idx))
							.isEqualTo(0);
						assertThat(match.getIndex(0))
							.as("Item index mismatch for match %d", _int(idx))
							.isEqualTo(hit);

						idx++;
					}
				}
			}
		}

		//TODO add full-stack tests for real corpora

		private Corpus createDummyCorpus() {
			throw new UnsupportedOperationException();
		}
	}
}
