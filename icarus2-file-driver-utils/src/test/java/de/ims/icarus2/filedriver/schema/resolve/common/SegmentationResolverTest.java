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
package de.ims.icarus2.filedriver.schema.resolve.common;

import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.api.ModelTestUtils.stubId;
import static de.ims.icarus2.model.api.ModelTestUtils.stubIndex;
import static de.ims.icarus2.model.standard.ModelDefaultsTestUtils.makeFillableContainer;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;
import java.util.stream.LongStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.filedriver.ComponentSupplier;
import de.ims.icarus2.filedriver.Converter;
import de.ims.icarus2.filedriver.Converter.ReadMode;
import de.ims.icarus2.filedriver.FileDriver;
import de.ims.icarus2.filedriver.schema.resolve.ResolverContext;
import de.ims.icarus2.filedriver.schema.resolve.ResolverOptions;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingReader;
import de.ims.icarus2.model.api.driver.mapping.MappingWriter;
import de.ims.icarus2.model.api.driver.mapping.RequestSettings;
import de.ims.icarus2.model.api.driver.mapping.WritableMapping;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.xml.ManifestXmlReader;
import de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache;
import de.ims.icarus2.model.standard.registry.DefaultCorpusManager;
import de.ims.icarus2.test.annotations.IntMatrixArg;
import de.ims.icarus2.test.annotations.StringArrayArg;
import de.ims.icarus2.test.annotations.StringMatrixArg;
import de.ims.icarus2.util.Mutable.MutableObject;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
class SegmentationResolverTest {

	@Nested
	class WithFullCorpus {
		private CorpusManager manager;
		private ItemLayer tokenLayer, rhymeLayer, verseLayer;
		Context context;

		@BeforeEach
		void setUp() throws Exception {

			manager = DefaultCorpusManager.builder()
					.defaultEnvironment()
					.build();

			ManifestRegistry registry = manager.getManifestRegistry();
			ManifestXmlReader reader = ManifestXmlReader.builder()
				.registry(registry)
				.useImplementationDefaults()
				.build()
				.addSource(ManifestLocation.builder()
						.url(SegmentationResolverTest.class.getResource("segmentation_test_schema.imf.xml"))
						.build());

			CorpusManifest manifest = reader.parseCorpora().get(0);

			registry.addCorpusManifest(manifest);

			Corpus corpus = manager.connect(manifest);

			tokenLayer = corpus.getLayer("token", false);
			rhymeLayer = corpus.getLayer("rhyme", false);
			verseLayer = corpus.getLayer("verse", false);

			context = corpus.getRootContext();
		}

		@AfterEach
		void tearDown() throws Exception {
			manager.shutdown();
			manager = null;
			context = null;
		}

		@Nested
		class ForRhymes {
			/** Length in tokens of each rhyme */
			private final int[] sizes = {
				8, 9, 11, 8,
				9, 9, 9, 8,
				9, 10, 7, 10,
				5,
			};

			@Test
			void testRhymeCount() {
				Container root = rhymeLayer.getProxyContainer();
				assertThat(root.getItemCount()).isEqualTo(13);
			}

			@Test
			void testRhymeSizes() {
				Container root = rhymeLayer.getProxyContainer();
				for (int i = 0; i < sizes.length; i++) {
					Container rhyme = (Container) root.getItemAt(i);
					assertThat(rhyme.getItemCount()).as("size if rhyme %d",_int(i)).isEqualTo(sizes[i]);
				}
			}

			@Test
			void testMapping() throws Exception {
				Mapping mapping = context.getDriver().getMapping(rhymeLayer, tokenLayer);
				MappingReader reader = mapping.newReader();
				IndexBuffer buffer = new IndexBuffer(100);

				reader.begin();
				try {
					int begin = 0;
					for (int rhymeIndex = 0; rhymeIndex < sizes.length; rhymeIndex++) {
						assertThat(reader.lookup(rhymeIndex, buffer, RequestSettings.none()))
							.as("lookup for rhyme %d",_int(rhymeIndex)).isTrue();
						int size = sizes[rhymeIndex];
						assertThat(buffer.size()).as("size of rhyme %d",_int(rhymeIndex)).isEqualTo(size);

						for (int i = 0; i < size; i++) {
							assertThat(buffer.indexAt(i)).as("mapping %d in rhyme %d",_int(i),_int(rhymeIndex)).isEqualTo(begin+i);
						}
						begin += size;
						buffer.clear();
					}
				} finally {
					reader.end();
				}
			}
		}

		@Nested
		class ForVerses {
			/** Length in tokens of each rhyme */
			private final int[] sizes = {
				8+9+11+8,
				9+9+9+8,
				9+10+7+10,
				5,
			};

			@Test
			void testVerseCount() {
				Container root = verseLayer.getProxyContainer();
				assertThat(root.getItemCount()).isEqualTo(4);
			}

			@Test
			void testVerseSizes() {
				Container root = verseLayer.getProxyContainer();
				for (int i = 0; i < sizes.length; i++) {
					Container verse = (Container) root.getItemAt(i);
					assertThat(verse.getItemCount()).as("size if verse %d",_int(i)).isEqualTo(sizes[i]);
				}
			}

			@Test
			void testMapping() throws Exception {
				Mapping mapping = context.getDriver().getMapping(verseLayer, tokenLayer);
				MappingReader reader = mapping.newReader();
				IndexBuffer buffer = new IndexBuffer(100);

				reader.begin();
				try {
					int begin = 0;
					for (int verseIndex = 0; verseIndex < sizes.length; verseIndex++) {
						assertThat(reader.lookup(verseIndex, buffer, RequestSettings.none()))
							.as("lookup for verse %d",_int(verseIndex)).isTrue();
						int size = sizes[verseIndex];
						assertThat(buffer.size()).as("size of verse %d",_int(verseIndex)).isEqualTo(size);

						for (int i = 0; i < size; i++) {
							assertThat(buffer.indexAt(i)).as("mapping %d in verse %d",_int(i),_int(verseIndex)).isEqualTo(begin+i);
						}
						begin += size;
						buffer.clear();
					}
				} finally {
					reader.end();
				}
			}
		}
	}

	@Nested
	class InIsolation {

		private SegmentationResolver resolver;

		@BeforeEach
		void setUp() {
			resolver = new SegmentationResolver();
		}

		@AfterEach
		void tearDown() {
			resolver.close();
			resolver = null;
		}

		@Nested
		class ForInvalidInput {

			@Test
			void testNullConverter() {
				assertThatNullPointerException().isThrownBy(() ->
					resolver.prepareForReading(null, ReadMode.FILE, mock(ResolverContext.class), Options.none()));
			}

			@Test
			void testNullMode() {
				assertThatNullPointerException().isThrownBy(() ->
					resolver.prepareForReading(mock(Converter.class), null, mock(ResolverContext.class), Options.none()));
			}

			@Test
			void testNullContext() {
				assertThatNullPointerException().isThrownBy(() ->
					resolver.prepareForReading(mock(Converter.class), ReadMode.FILE, null, Options.none()));
			}

			@Test
			void testNullOptions() {
				assertThatNullPointerException().isThrownBy(() ->
					resolver.prepareForReading(mock(Converter.class), ReadMode.FILE, mock(ResolverContext.class), null));
			}

			@Test
			void testUnresolvableStrategy() {
				Options options = new Options();
				options.put(ResolverOptions.LAYER, mock(ItemLayer.class));

				Converter converter = mock(Converter.class);
				FileDriver driver = mock(FileDriver.class);
				Corpus corpus = mock(Corpus.class);
				when(driver.getCorpus()).thenReturn(corpus);
				when(converter.getDriver()).thenReturn(driver);

				assertThatExceptionOfType(ModelException.class).isThrownBy(() ->
					resolver.prepareForReading(converter, ReadMode.FILE, mock(ResolverContext.class), options));
			}

		}

		@Nested
		class WithContext {
			private FileDriver driver;
			private Converter converter;
			private ResolverContext context;
			private ItemLayer tokenLayer;
			private ItemLayer sentenceLayer;
			private ItemLayer segmentLayer;
			private Options options;

			@SuppressWarnings("unchecked")
			private ItemLayer makeLayer(String id) {
				@SuppressWarnings("rawtypes")
				ItemLayerManifestBase manifest = mock(ItemLayerManifest.class);
				when(manifest.getId()).thenReturn(Optional.of(id));
				ItemLayer layer = mock(ItemLayer.class);
				when(layer.getManifest()).thenReturn(manifest);
				return layer;
			}

			@BeforeEach
			void setUp() {
				tokenLayer = makeLayer("token");
				sentenceLayer = makeLayer("sentence");
				segmentLayer = makeLayer("segment");

				context = mock(ResolverContext.class);

				converter = mock(Converter.class);
				driver = mock(FileDriver.class);
				when(converter.getDriver()).thenReturn(driver);

				options = new Options();
				options.put(ResolverOptions.LAYER, segmentLayer);

				ItemLayerManifestBase<?> manifest = tokenLayer.getManifest();
				TargetLayerManifest tlm = mock(TargetLayerManifest.class);
				when(tlm.getResolvedLayerManifest()).thenReturn(Optional.of(manifest));
				List<TargetLayerManifest> targetManifests = list(tlm);
				when(segmentLayer.getManifest().getBaseLayerManifests()).thenReturn(targetManifests);
			}

			@AfterEach
			void tearDown() {
				tokenLayer = null;
				segmentLayer = null;
				sentenceLayer = null;
				context = null;
				converter = null;
				options = null;
			}

			private void mockSegmentCache() {
				when(context.getCache(eq(segmentLayer))).then(invoc -> mock(InputCache.class));
			}

			@ParameterizedTest
			@EnumSource(ReadMode.class)
			void testPrepareForReading_autosement(ReadMode mode) {
				options.put(SegmentationResolver.OPTION_AUTO_SEGMENT, Boolean.TRUE);
				mockSegmentCache();

				resolver.prepareForReading(converter, mode, context, options);

				assertThat(resolver.getStrategy()).isInstanceOf(SegmentationResolver.Alternating.class);
			}

			@ParameterizedTest
			@EnumSource(ReadMode.class)
			void testPrepareForReading_discontinuous(ReadMode mode) {
				options.put(SegmentationResolver.OPTION_SEGMENT_BEGIN, "x");
				options.put(SegmentationResolver.OPTION_SEGMENT_END, "y");
				mockSegmentCache();

				resolver.prepareForReading(converter, mode, context, options);

				assertThat(resolver.getStrategy()).isInstanceOf(SegmentationResolver.Discontinuous.class);
			}

			@ParameterizedTest
			@EnumSource(ReadMode.class)
			void testPrepareForReading_beginning(ReadMode mode) {
				options.put(SegmentationResolver.OPTION_SEGMENT_BEGIN, "x");
				mockSegmentCache();

				resolver.prepareForReading(converter, mode, context, options);

				assertThat(resolver.getStrategy()).isInstanceOf(SegmentationResolver.Beginning.class);
			}

			@ParameterizedTest
			@EnumSource(ReadMode.class)
			void testPrepareForReading_ending(ReadMode mode) {
				options.put(SegmentationResolver.OPTION_SEGMENT_END, "y");
				mockSegmentCache();

				resolver.prepareForReading(converter, mode, context, options);

				assertThat(resolver.getStrategy()).isInstanceOf(SegmentationResolver.Ending.class);
			}

			@Nested
			class WithTokens {

				@SuppressWarnings("boxing")
				private void feedData(Container sentence, Item[] tokens, String[] data, boolean continuation) throws IcarusApiException {
					assert tokens.length==data.length;
					assert sentence.getItemCount()==tokens.length;

					when(context.currentContainer()).thenReturn(sentence);

					for (int i = 0; i < data.length; i++) {
						when(context.currentIndex()).thenReturn(Long.valueOf(i));
						when(context.currentItem()).thenReturn(tokens[i]);
						when(context.rawData()).thenReturn(data[i]);

						assertThat(resolver.isProbing()).as("probing flag at step %d", _int(i)).isEqualTo(i==0 && !continuation);

						resolver.process(context);
					}
				}

				@SuppressWarnings("boxing")
				private List<Container> mockSentenceProducer() {
					List<Container> storage = new ObjectArrayList<>();
					ComponentSupplier cs = mock(ComponentSupplier.class);
					final MutableLong indexGen = new MutableLong(UNSET_LONG);
					final MutableObject<Container> sent = new MutableObject<>();
					when(cs.next()).thenAnswer(invoc -> {
						indexGen.incrementAndGet();
						sent.clear();
						return Boolean.TRUE;
					});
					when(cs.currentIndex()).thenReturn(Long.valueOf(indexGen.longValue()));
					when(cs.currentId()).thenReturn(Long.valueOf(indexGen.longValue()));
					when(cs.currentItem()).thenAnswer(invoc -> {
						if(sent.isEmpty()) {
							Container c = makeFillableContainer(null, indexGen.longValue());
							sent.set(c);
							storage.add(c);
						}
						return sent.get();
					});

					when(context.getComponentSupplier(eq(segmentLayer))).thenReturn(cs);

					return storage;
				}

				private void setMarkers(String segBegin, String segEnd, String segSingle, boolean auto) {
					if(!"-".equals(segBegin)) options.put(SegmentationResolver.OPTION_SEGMENT_BEGIN, segBegin);
					if(!"-".equals(segEnd)) options.put(SegmentationResolver.OPTION_SEGMENT_END, segEnd);
					if(!"-".equals(segSingle)) options.put(SegmentationResolver.OPTION_SEGMENT_SINGLETON, segSingle);
					options.put(SegmentationResolver.OPTION_AUTO_SEGMENT, Boolean.valueOf(auto));
				}

				private Item[] mockItems(int count, long beginIndex) {
					return LongStream.range(0, count)
							.map(idx -> idx+beginIndex)
							.mapToObj(idx -> {
								Item item = mockItem();
								stubId(item, idx);
								stubIndex(item, idx);
								when(item.getLayer()).thenReturn(tokenLayer);
								return item;
							})
							.toArray(Item[]::new);
				}

				@ParameterizedTest
				@CsvSource({
					// automatic segmentation
					"-, -, -, true, '{x;x;x;y;y;x}', { {0;1;2}{3;4}{5} }",
					"-, -, -, true, '{x;y;x;y;x;y}', { {0}{1}{2}{3}{4}{5} }",
					"-, -, -, true, '{x;y;y;y;z;z}', { {0}{1;2;3}{4;5} }",
					// begin marker
					"x, -, -, false, '{x;y;y;x;z;z}', { {0;1;2}{3;4;5} }",
					"x, -, -, false, '{a;x;y;x;z;z}', { {1;2}{3;4;5} }",
					"x, -, s, false, '{x;y;y;x;z;s}', { {0;1;2}{3;4}{5} }",
					"x, -, s, false, '{x;y;s;x;z;s}', { {0;1}{2}{3;4}{5} }",
					// end marker
					"-, x, -, false, '{y;y;x;z;z;x}', { {0;1;2}{3;4;5} }",
					"-, x, -, false, '{y;y;x;z;x;y}', { {0;1;2}{3;4} }",
					"-, x, s, false, '{s;y;x;s;z;x}', { {0}{1;2}{3}{4;5} }",
					"-, x, s, false, '{s;y;x;z;x;s}', { {0}{1;2}{3;4}{5} }",
					// begin and end marking
					"x, y, -, false, '{a;x;a;y;b;c}', { {1;2;3} }",
					"x, y, -, false, '{a;x;a;y;b;x}', { {1;2;3} }",
					"x, y, s, false, '{a;x;a;y;s;c}', { {1;2;3}{4} }",
					"x, y, s, false, '{a;x;a;y;c;s}', { {1;2;3}{5} }",
					"x, y, s, false, '{s;x;a;y;c;s}', { {0}{1;2;3}{5} }",
				})
				void testSingleContainer(String segBegin, String segEnd, String segSingle, boolean auto,
						@StringArrayArg String[] data, @IntMatrixArg int[][] segments) throws Exception {
					setMarkers(segBegin, segEnd, segSingle, auto);
					mockSentenceProducer();
					final List<Container> collector = mockSentenceProducer();

					resolver.prepareForReading(converter, ReadMode.SCAN, context, options);

					Item[] tokens = mockItems(data.length, 0);
					Container sentence = mockContainer(tokens);

					feedData(sentence, tokens, data, false);

					resolver.complete();

					assertThat(collector.size()).as("# of segments").isEqualTo(segments.length);

					for (int segmentIndex = 0; segmentIndex < segments.length; segmentIndex++) {
						int[] expected = segments[segmentIndex];
						Container segment = collector.get(segmentIndex);
						assertThat(segment.getItemCount()).as("size of segment %d",_int(segmentIndex)).isEqualTo(expected.length);
						for (int i = 0; i < expected.length; i++) {
							Item token = segment.getItemAt(i);
							assertThat(token).as("token %d in segment %d",_int(i),_int(segmentIndex)).isSameAs(tokens[expected[i]]);
						}
					}
				}

				@ParameterizedTest
				@CsvSource({
					// automatic segmentation
					"-, -, -, true, '{ {x;x}{x;y;y;x} }', { {0;1;2}{3;4}{5} }",
					"-, -, -, true, '{ {x;x}{x;y}{y;x} }', { {0;1;2}{3;4}{5} }",
					"-, -, -, true, '{ {x;y}{x;y;x;y} }', { {0}{1}{2}{3}{4}{5} }",
					"-, -, -, true, '{ {x;y}{y;y;z}{z} }', { {0}{1;2;3}{4;5} }",
					// begin marker
					"x, -, -, false, '{ {x;y}{y;x}{z;z} }', { {0;1;2}{3;4;5} }",
					"x, -, -, false, '{ {x}{y;y}{x}{z;z} }', { {0;1;2}{3;4;5} }",
					"x, -, -, false, '{ {a;x}{y;x}{z;z} }', { {1;2}{3;4;5} }",
					"x, -, -, false, '{ {a}{x;y;x}{z}{z} }', { {1;2}{3;4;5} }",
					"x, -, s, false, '{ {x;y}{y;x}{z;s} }', { {0;1;2}{3;4}{5} }",
					"x, -, s, false, '{ {x;y;y}{x}{z;s} }', { {0;1;2}{3;4}{5} }",
					"x, -, s, false, '{ {x;y}{s;x}{z;s} }', { {0;1}{2}{3;4}{5} }",
					"x, -, s, false, '{ {x}{y;s;x}{z}{s} }', { {0;1}{2}{3;4}{5} }",
					// end marker
					"-, x, -, false, '{ {y;y}{x;z}{z;x} }', { {0;1;2}{3;4;5} }",
					"-, x, -, false, '{ {y;y}{x;z;z}{x} }', { {0;1;2}{3;4;5} }",
					"-, x, -, false, '{ {y;y}{x;z}{x;y} }', { {0;1;2}{3;4} }",
					"-, x, -, false, '{ {y}{y;x;z}{x;y} }', { {0;1;2}{3;4} }",
					"-, x, s, false, '{ {s;y}{x;s}{z;x} }', { {0}{1;2}{3}{4;5} }",
					"-, x, s, false, '{ {s;y}{x;s}{z;x} }', { {0}{1;2}{3}{4;5} }",
					"-, x, s, false, '{ {s}{y;x}{s;z}{x} }', { {0}{1;2}{3}{4;5} }",
					"-, x, s, false, '{ {s;y}{x;z}{x;s} }', { {0}{1;2}{3;4}{5} }",
					"-, x, s, false, '{ {s;y}{x}{z;x}{s} }', { {0}{1;2}{3;4}{5} }",
					// begin and end marking
					"x, y, -, false, '{ {a;x}{a;y}{b;c} }', { {1;2;3} }",
					"x, y, -, false, '{ {a;x}{a}{y;b}{c} }', { {1;2;3} }",
					"x, y, -, false, '{ {a;x}{a;y}{b;x} }', { {1;2;3} }",
					"x, y, -, false, '{ {a}{x;a;y}{b;x} }', { {1;2;3} }",
					"x, y, s, false, '{ {a;x}{a;y}{s;c} }', { {1;2;3}{4} }",
					"x, y, s, false, '{ {a;x}{a;y;s}{c} }', { {1;2;3}{4} }",
					"x, y, s, false, '{ {a;x}{a;y}{c;s} }', { {1;2;3}{5} }",
					"x, y, s, false, '{ {a}{x;a}{y;c}{s} }', { {1;2;3}{5} }",
					"x, y, s, false, '{ {s;x}{a;y}{c;s} }', { {0}{1;2;3}{5} }",
					"x, y, s, false, '{ {s;x}{a;y;c}{s} }', { {0}{1;2;3}{5} }",
				})
				void testMultipleContainers(String segBegin, String segEnd, String segSingle, boolean auto,
						@StringMatrixArg String[][] data, // original containers with tokens
						@IntMatrixArg int[][] segments) throws Exception { // segments referencing tokens in original order
					setMarkers(segBegin, segEnd, segSingle, auto);
					mockSentenceProducer();
					final List<Container> collector = mockSentenceProducer();

					resolver.prepareForReading(converter, ReadMode.SCAN, context, options);

					List<Item> tokens = new ObjectArrayList<>();

					int begin = 0;
					for(int i=0; i< data.length; i++) {
						String[] values = data[i];
						Item[] items = mockItems(values.length, begin);
						Container sentence = mockContainer(items);
						begin += items.length;

						feedData(sentence, items, values, i>0);

						CollectionUtils.feedItems(tokens, items);
					}


					resolver.complete();

					assertThat(collector.size()).as("# of segments").isEqualTo(segments.length);

					for (int segmentIndex = 0; segmentIndex < segments.length; segmentIndex++) {
						int[] expected = segments[segmentIndex];
						Container segment = collector.get(segmentIndex);
						assertThat(segment.getItemCount()).as("size of segment %d",_int(segmentIndex)).isEqualTo(expected.length);
						for (int i = 0; i < expected.length; i++) {
							Item token = segment.getItemAt(i);
							assertThat(token).as("token %d in segment %d",_int(i),_int(segmentIndex)).isSameAs(tokens.get(expected[i]));
						}
					}
				}


				@Nested
				class WithMapping {

					private Int2ObjectMap<IntList> mockMapping() {
						WritableMapping mapping = mock(WritableMapping.class);
						MappingWriter writer = mock(MappingWriter.class, invoc -> { throw new UnsupportedOperationException(); });
						Int2ObjectMap<IntList> map = new Int2ObjectOpenHashMap<>();
						doAnswer(invoc -> {
							IndexSet source = invoc.getArgument(0);
							IndexSet target = invoc.getArgument(1);
							assert source.size()==1;
							assert target.size()>0;
							int key = strictToInt(source.indexAt(0));
							IntList sink = map.computeIfAbsent(key, i -> new IntArrayList());
							target.forEachIndex((IntConsumer)sink::add);

							return null;
						}).when(writer).map(any(IndexSet.class), any(IndexSet.class));
						doAnswer(invoc -> null).when(writer).close();
						doAnswer(invoc -> null).when(writer).begin();
						doAnswer(invoc -> null).when(writer).end();

						when(mapping.newWriter()).thenReturn(writer);

						when(driver.getMapping(eq(segmentLayer), eq(tokenLayer))).thenReturn(mapping);

						return map;
					}

					@ParameterizedTest
					@CsvSource({
						// automatic segmentation
						"-, -, -, true, '{x;x;x;y;y;x}', { {0;1;2}{3;4}{5} }",
						"-, -, -, true, '{x;y;x;y;x;y}', { {0}{1}{2}{3}{4}{5} }",
						"-, -, -, true, '{x;y;y;y;z;z}', { {0}{1;2;3}{4;5} }",
						// begin marker
						"x, -, -, false, '{x;y;y;x;z;z}', { {0;1;2}{3;4;5} }",
						"x, -, -, false, '{a;x;y;x;z;z}', { {1;2}{3;4;5} }",
						"x, -, s, false, '{x;y;y;x;z;s}', { {0;1;2}{3;4}{5} }",
						"x, -, s, false, '{x;y;s;x;z;s}', { {0;1}{2}{3;4}{5} }",
						// end marker
						"-, x, -, false, '{y;y;x;z;z;x}', { {0;1;2}{3;4;5} }",
						"-, x, -, false, '{y;y;x;z;x;y}', { {0;1;2}{3;4} }",
						"-, x, s, false, '{s;y;x;s;z;x}', { {0}{1;2}{3}{4;5} }",
						"-, x, s, false, '{s;y;x;z;x;s}', { {0}{1;2}{3;4}{5} }",
						// begin and end marking
						"x, y, -, false, '{a;x;a;y;b;c}', { {1;2;3} }",
						"x, y, -, false, '{a;x;a;y;b;x}', { {1;2;3} }",
						"x, y, s, false, '{a;x;a;y;s;c}', { {1;2;3}{4} }",
						"x, y, s, false, '{a;x;a;y;c;s}', { {1;2;3}{5} }",
						"x, y, s, false, '{s;x;a;y;c;s}', { {0}{1;2;3}{5} }",
					})
					void testSingleContainer(String segBegin, String segEnd, String segSingle, boolean auto,
							@StringArrayArg String[] data, @IntMatrixArg int[][] segments) throws Exception {
						setMarkers(segBegin, segEnd, segSingle, auto);
						mockSentenceProducer();
						mockSegmentCache();

						Int2ObjectMap<IntList> mapping = mockMapping();

						resolver.prepareForReading(converter, ReadMode.SCAN, context, options);

						Item[] tokens = mockItems(data.length, 0);
						Container sentence = mockContainer(tokens);

						feedData(sentence, tokens, data, false);

						resolver.complete();

						assertThat(mapping.size()).as("# of mapped segments").isEqualTo(segments.length);

						for (int segmentIndex = 0; segmentIndex < segments.length; segmentIndex++) {
							int[] expected = segments[segmentIndex];
							IntList mappedSegment = mapping.get(segmentIndex);
							assertThat(mappedSegment.size()).as("size of mapped segment %d",_int(segmentIndex)).isEqualTo(expected.length);
							for (int i = 0; i < expected.length; i++) {
								assertThat(mappedSegment.getInt(i)).as("index %d in mapped segment %d",_int(i),_int(segmentIndex)).isEqualTo(expected[i]);
							}
						}
					}

					@ParameterizedTest
					@CsvSource({
						// automatic segmentation
						"-, -, -, true, '{ {x;x}{x;y;y;x} }', { {0;1;2}{3;4}{5} }",
						"-, -, -, true, '{ {x;x}{x;y}{y;x} }', { {0;1;2}{3;4}{5} }",
						"-, -, -, true, '{ {x;y}{x;y;x;y} }', { {0}{1}{2}{3}{4}{5} }",
						"-, -, -, true, '{ {x;y}{y;y;z}{z} }', { {0}{1;2;3}{4;5} }",
						// begin marker
						"x, -, -, false, '{ {x;y}{y;x}{z;z} }', { {0;1;2}{3;4;5} }",
						"x, -, -, false, '{ {x}{y;y}{x}{z;z} }', { {0;1;2}{3;4;5} }",
						"x, -, -, false, '{ {a;x}{y;x}{z;z} }', { {1;2}{3;4;5} }",
						"x, -, -, false, '{ {a}{x;y;x}{z}{z} }', { {1;2}{3;4;5} }",
						"x, -, s, false, '{ {x;y}{y;x}{z;s} }', { {0;1;2}{3;4}{5} }",
						"x, -, s, false, '{ {x;y;y}{x}{z;s} }', { {0;1;2}{3;4}{5} }",
						"x, -, s, false, '{ {x;y}{s;x}{z;s} }', { {0;1}{2}{3;4}{5} }",
						"x, -, s, false, '{ {x}{y;s;x}{z}{s} }', { {0;1}{2}{3;4}{5} }",
						// end marker
						"-, x, -, false, '{ {y;y}{x;z}{z;x} }', { {0;1;2}{3;4;5} }",
						"-, x, -, false, '{ {y;y}{x;z;z}{x} }', { {0;1;2}{3;4;5} }",
						"-, x, -, false, '{ {y;y}{x;z}{x;y} }', { {0;1;2}{3;4} }",
						"-, x, -, false, '{ {y}{y;x;z}{x;y} }', { {0;1;2}{3;4} }",
						"-, x, s, false, '{ {s;y}{x;s}{z;x} }', { {0}{1;2}{3}{4;5} }",
						"-, x, s, false, '{ {s;y}{x;s}{z;x} }', { {0}{1;2}{3}{4;5} }",
						"-, x, s, false, '{ {s}{y;x}{s;z}{x} }', { {0}{1;2}{3}{4;5} }",
						"-, x, s, false, '{ {s;y}{x;z}{x;s} }', { {0}{1;2}{3;4}{5} }",
						"-, x, s, false, '{ {s;y}{x}{z;x}{s} }', { {0}{1;2}{3;4}{5} }",
						// begin and end marking
						"x, y, -, false, '{ {a;x}{a;y}{b;c} }', { {1;2;3} }",
						"x, y, -, false, '{ {a;x}{a}{y;b}{c} }', { {1;2;3} }",
						"x, y, -, false, '{ {a;x}{a;y}{b;x} }', { {1;2;3} }",
						"x, y, -, false, '{ {a}{x;a;y}{b;x} }', { {1;2;3} }",
						"x, y, s, false, '{ {a;x}{a;y}{s;c} }', { {1;2;3}{4} }",
						"x, y, s, false, '{ {a;x}{a;y;s}{c} }', { {1;2;3}{4} }",
						"x, y, s, false, '{ {a;x}{a;y}{c;s} }', { {1;2;3}{5} }",
						"x, y, s, false, '{ {a}{x;a}{y;c}{s} }', { {1;2;3}{5} }",
						"x, y, s, false, '{ {s;x}{a;y}{c;s} }', { {0}{1;2;3}{5} }",
						"x, y, s, false, '{ {s;x}{a;y;c}{s} }', { {0}{1;2;3}{5} }",
					})
					void testMultipleContainers(String segBegin, String segEnd, String segSingle, boolean auto,
							@StringMatrixArg String[][] data, // original containers with tokens
							@IntMatrixArg int[][] segments) throws Exception { // segments referencing tokens in original order
						setMarkers(segBegin, segEnd, segSingle, auto);
						mockSentenceProducer();
						mockSegmentCache();

						Int2ObjectMap<IntList> mapping = mockMapping();

						resolver.prepareForReading(converter, ReadMode.SCAN, context, options);

						List<Item> tokens = new ObjectArrayList<>();

						int begin = 0;
						for(int i=0; i< data.length; i++) {
							String[] values = data[i];
							Item[] items = mockItems(values.length, begin);
							Container sentence = mockContainer(items);
							begin += items.length;

							feedData(sentence, items, values, i>0);

							CollectionUtils.feedItems(tokens, items);
						}


						resolver.complete();

						assertThat(mapping.size()).as("# of mapped segments").isEqualTo(segments.length);

						for (int segmentIndex = 0; segmentIndex < segments.length; segmentIndex++) {
							int[] expected = segments[segmentIndex];
							IntList mappedSegment = mapping.get(segmentIndex);
							assertThat(mappedSegment.size()).as("size of mapped segment %d",_int(segmentIndex)).isEqualTo(expected.length);
							for (int i = 0; i < expected.length; i++) {
								assertThat(mappedSegment.getInt(i)).as("index %d in mapped segment %d",_int(i),_int(segmentIndex)).isEqualTo(expected[i]);
							}
						}
					}
				}


				@Nested
				class WithReverseMapping {

					private Int2ObjectMap<IntList> mockReverseMapping() {
						WritableMapping mapping = mock(WritableMapping.class);
						MappingWriter writer = mock(MappingWriter.class, invoc -> { throw new UnsupportedOperationException(); });
						Int2ObjectMap<IntList> map = new Int2ObjectOpenHashMap<>();
						doAnswer(invoc -> {
							IndexSet source = invoc.getArgument(0);
							IndexSet target = invoc.getArgument(1);
							assert source.size()>0;
							assert target.size()==1;
							int key = strictToInt(target.indexAt(0));
							IntList sink = map.computeIfAbsent(key, i -> new IntArrayList());
							source.forEachIndex((IntConsumer)sink::add);

							return null;
						}).when(writer).map(any(IndexSet.class), any(IndexSet.class));
						doAnswer(invoc -> null).when(writer).close();
						doAnswer(invoc -> null).when(writer).begin();
						doAnswer(invoc -> null).when(writer).end();

						when(mapping.newWriter()).thenReturn(writer);

						when(driver.getMapping(eq(tokenLayer), eq(segmentLayer))).thenReturn(mapping);

						return map;
					}

					@ParameterizedTest
					@CsvSource({
						// automatic segmentation
						"-, -, -, true, '{x;x;x;y;y;x}', { {0;1;2}{3;4}{5} }",
						"-, -, -, true, '{x;y;x;y;x;y}', { {0}{1}{2}{3}{4}{5} }",
						"-, -, -, true, '{x;y;y;y;z;z}', { {0}{1;2;3}{4;5} }",
						// begin marker
						"x, -, -, false, '{x;y;y;x;z;z}', { {0;1;2}{3;4;5} }",
						"x, -, -, false, '{a;x;y;x;z;z}', { {1;2}{3;4;5} }",
						"x, -, s, false, '{x;y;y;x;z;s}', { {0;1;2}{3;4}{5} }",
						"x, -, s, false, '{x;y;s;x;z;s}', { {0;1}{2}{3;4}{5} }",
						// end marker
						"-, x, -, false, '{y;y;x;z;z;x}', { {0;1;2}{3;4;5} }",
						"-, x, -, false, '{y;y;x;z;x;y}', { {0;1;2}{3;4} }",
						"-, x, s, false, '{s;y;x;s;z;x}', { {0}{1;2}{3}{4;5} }",
						"-, x, s, false, '{s;y;x;z;x;s}', { {0}{1;2}{3;4}{5} }",
						// begin and end marking
						"x, y, -, false, '{a;x;a;y;b;c}', { {1;2;3} }",
						"x, y, -, false, '{a;x;a;y;b;x}', { {1;2;3} }",
						"x, y, s, false, '{a;x;a;y;s;c}', { {1;2;3}{4} }",
						"x, y, s, false, '{a;x;a;y;c;s}', { {1;2;3}{5} }",
						"x, y, s, false, '{s;x;a;y;c;s}', { {0}{1;2;3}{5} }",
					})
					void testSingleContainer(String segBegin, String segEnd, String segSingle, boolean auto,
							@StringArrayArg String[] data, @IntMatrixArg int[][] segments) throws Exception {
						setMarkers(segBegin, segEnd, segSingle, auto);
						mockSentenceProducer();
						mockSegmentCache();

						Int2ObjectMap<IntList> mapping = mockReverseMapping();

						resolver.prepareForReading(converter, ReadMode.SCAN, context, options);

						Item[] tokens = mockItems(data.length, 0);
						Container sentence = mockContainer(tokens);

						feedData(sentence, tokens, data, false);

						resolver.complete();

						assertThat(mapping.size()).as("# of mapped segments").isEqualTo(segments.length);

						for (int segmentIndex = 0; segmentIndex < segments.length; segmentIndex++) {
							int[] expected = segments[segmentIndex];
							IntList mappedSegment = mapping.get(segmentIndex);
							assertThat(mappedSegment.size()).as("size of mapped segment %d",_int(segmentIndex)).isEqualTo(expected.length);
							for (int i = 0; i < expected.length; i++) {
								assertThat(mappedSegment.getInt(i)).as("index %d in mapped segment %d",_int(i),_int(segmentIndex)).isEqualTo(expected[i]);
							}
						}
					}

					@ParameterizedTest
					@CsvSource({
						// automatic segmentation
						"-, -, -, true, '{ {x;x}{x;y;y;x} }', { {0;1;2}{3;4}{5} }",
						"-, -, -, true, '{ {x;x}{x;y}{y;x} }', { {0;1;2}{3;4}{5} }",
						"-, -, -, true, '{ {x;y}{x;y;x;y} }', { {0}{1}{2}{3}{4}{5} }",
						"-, -, -, true, '{ {x;y}{y;y;z}{z} }', { {0}{1;2;3}{4;5} }",
						// begin marker
						"x, -, -, false, '{ {x;y}{y;x}{z;z} }', { {0;1;2}{3;4;5} }",
						"x, -, -, false, '{ {x}{y;y}{x}{z;z} }', { {0;1;2}{3;4;5} }",
						"x, -, -, false, '{ {a;x}{y;x}{z;z} }', { {1;2}{3;4;5} }",
						"x, -, -, false, '{ {a}{x;y;x}{z}{z} }', { {1;2}{3;4;5} }",
						"x, -, s, false, '{ {x;y}{y;x}{z;s} }', { {0;1;2}{3;4}{5} }",
						"x, -, s, false, '{ {x;y;y}{x}{z;s} }', { {0;1;2}{3;4}{5} }",
						"x, -, s, false, '{ {x;y}{s;x}{z;s} }', { {0;1}{2}{3;4}{5} }",
						"x, -, s, false, '{ {x}{y;s;x}{z}{s} }', { {0;1}{2}{3;4}{5} }",
						// end marker
						"-, x, -, false, '{ {y;y}{x;z}{z;x} }', { {0;1;2}{3;4;5} }",
						"-, x, -, false, '{ {y;y}{x;z;z}{x} }', { {0;1;2}{3;4;5} }",
						"-, x, -, false, '{ {y;y}{x;z}{x;y} }', { {0;1;2}{3;4} }",
						"-, x, -, false, '{ {y}{y;x;z}{x;y} }', { {0;1;2}{3;4} }",
						"-, x, s, false, '{ {s;y}{x;s}{z;x} }', { {0}{1;2}{3}{4;5} }",
						"-, x, s, false, '{ {s;y}{x;s}{z;x} }', { {0}{1;2}{3}{4;5} }",
						"-, x, s, false, '{ {s}{y;x}{s;z}{x} }', { {0}{1;2}{3}{4;5} }",
						"-, x, s, false, '{ {s;y}{x;z}{x;s} }', { {0}{1;2}{3;4}{5} }",
						"-, x, s, false, '{ {s;y}{x}{z;x}{s} }', { {0}{1;2}{3;4}{5} }",
						// begin and end marking
						"x, y, -, false, '{ {a;x}{a;y}{b;c} }', { {1;2;3} }",
						"x, y, -, false, '{ {a;x}{a}{y;b}{c} }', { {1;2;3} }",
						"x, y, -, false, '{ {a;x}{a;y}{b;x} }', { {1;2;3} }",
						"x, y, -, false, '{ {a}{x;a;y}{b;x} }', { {1;2;3} }",
						"x, y, s, false, '{ {a;x}{a;y}{s;c} }', { {1;2;3}{4} }",
						"x, y, s, false, '{ {a;x}{a;y;s}{c} }', { {1;2;3}{4} }",
						"x, y, s, false, '{ {a;x}{a;y}{c;s} }', { {1;2;3}{5} }",
						"x, y, s, false, '{ {a}{x;a}{y;c}{s} }', { {1;2;3}{5} }",
						"x, y, s, false, '{ {s;x}{a;y}{c;s} }', { {0}{1;2;3}{5} }",
						"x, y, s, false, '{ {s;x}{a;y;c}{s} }', { {0}{1;2;3}{5} }",
					})
					void testMultipleContainers(String segBegin, String segEnd, String segSingle, boolean auto,
							@StringMatrixArg String[][] data, // original containers with tokens
							@IntMatrixArg int[][] segments) throws Exception { // segments referencing tokens in original order
						setMarkers(segBegin, segEnd, segSingle, auto);
						mockSentenceProducer();
						mockSegmentCache();

						Int2ObjectMap<IntList> mapping = mockReverseMapping();

						resolver.prepareForReading(converter, ReadMode.SCAN, context, options);

						List<Item> tokens = new ObjectArrayList<>();

						int begin = 0;
						for(int i=0; i< data.length; i++) {
							String[] values = data[i];
							Item[] items = mockItems(values.length, begin);
							Container sentence = mockContainer(items);
							begin += items.length;

							feedData(sentence, items, values, i>0);

							CollectionUtils.feedItems(tokens, items);
						}


						resolver.complete();

						assertThat(mapping.size()).as("# of mapped segments").isEqualTo(segments.length);

						for (int segmentIndex = 0; segmentIndex < segments.length; segmentIndex++) {
							int[] expected = segments[segmentIndex];
							IntList mappedSegment = mapping.get(segmentIndex);
							assertThat(mappedSegment.size()).as("size of mapped segment %d",_int(segmentIndex)).isEqualTo(expected.length);
							for (int i = 0; i < expected.length; i++) {
								assertThat(mappedSegment.getInt(i)).as("index %d in mapped segment %d",_int(i),_int(segmentIndex)).isEqualTo(expected[i]);
							}
						}
					}
				}
			}
		}

	}

}
