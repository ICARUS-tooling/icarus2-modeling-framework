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
package de.ims.icarus2.filedriver.mapping;

import static de.ims.icarus2.model.api.ModelAssertions.assertThat;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.wrap;
import static de.ims.icarus2.test.TestUtils.mockDelegate;
import static de.ims.icarus2.test.util.Pair.pair;
import static de.ims.icarus2.test.util.Triple.triple;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static de.ims.icarus2.util.lang.Primitives._int;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;
import de.ims.icarus2.filedriver.io.RUBlockCache;
import de.ims.icarus2.filedriver.io.UnlimitedBlockCache;
import de.ims.icarus2.filedriver.mapping.MappingImplSpanOneToMany.Builder;
import de.ims.icarus2.filedriver.mapping.StoredMappingTest.AbstractConfig;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingReader;
import de.ims.icarus2.model.api.driver.mapping.MappingReaderTest;
import de.ims.icarus2.model.api.driver.mapping.MappingTestUtils;
import de.ims.icarus2.model.api.driver.mapping.MappingWriter;
import de.ims.icarus2.model.api.driver.mapping.MappingWriterTest;
import de.ims.icarus2.model.api.driver.mapping.RequestSettings;
import de.ims.icarus2.model.api.driver.mapping.WritableMapping;
import de.ims.icarus2.model.api.driver.mapping.WritableMappingTest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;
import de.ims.icarus2.model.manifest.api.MappingManifest.Relation;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.test.util.Triple;
import de.ims.icarus2.util.io.resource.IOResource;
import de.ims.icarus2.util.io.resource.VirtualIOResource;

/**
 * @author Markus Gärtner
 *
 */
class MappingImplSpanOneToManyTest implements WritableMappingTest<MappingImplSpanOneToMany, MappingImplSpanOneToManyTest.ConfigImpl> {

	private static final Path DEFAULT_PATH = Paths.get(".");

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.mapping.MappingImplSpanOneToMany#builder()}.
	 */
	@Test
	void testBuilder() {
		assertThat(MappingImplSpanOneToMany.builder()).isNotNull();
	}

	@Override
	public Stream<ConfigImpl> configurations() {
		return
			Stream.of(IndexValueType.values()).flatMap(valueType ->
			IntStream.of(3, 10).boxed().flatMap(blockPower ->
			Stream.<Pair<String,Supplier<BlockCache>>>of(pair("unlimited", UnlimitedBlockCache::new),
					pair("LRU", RUBlockCache::newLeastRecentlyUsedCache)).flatMap(pCache ->
			IntStream.of(BlockCache.MIN_CAPACITY, 1024).boxed().map(cacheSize -> {
				return config(valueType, blockPower.intValue(),
						pCache.second, pCache.first, cacheSize.intValue());
			}))));
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.mapping.MappingTest#basicConfiguration()
	 */
	@Override
	public ConfigImpl basicConfiguration() {
		return config(IndexValueType.INTEGER, 4, RUBlockCache::newLeastRecentlyUsedCache, "LRU", 128);
	}

	private static ConfigImpl config(IndexValueType valueType, int blockPower,
			Supplier<BlockCache> cacheGen,
			String cacheLabel, int cacheSize) {
		ConfigImpl config = new ConfigImpl();
		config.label = String.format("type=%s blockPower=%d cache=%s cacheSize=%d",
				valueType, _int(blockPower), cacheLabel, _int(cacheSize));

		config.blockCacheGen = cacheGen;
		config.blockPower = blockPower;
		config.cacheSize = cacheSize;
		config.valueType = valueType;
		config.resourceGen = () -> new VirtualIOResource(DEFAULT_PATH);

		config.driver = mock(Driver.class);
		config.sourceLayer = mock(ItemLayerManifestBase.class);
		config.targetLayer = mock(ItemLayerManifestBase.class);
		config.manifest = mock(MappingManifest.class);

		config.prepareManifest(Coverage.PARTIAL);

		return config;
	}

	@TestFactory
	Stream<DynamicNode> testRead1To1Fixed() {
		return configurations().map(config -> dynamicContainer(config.label,
				coverages().map(coverage -> dynamicTest(coverage.name(), () -> {
					config.prepareManifest(coverage);
					List<Pair<Integer, Integer>> entries = MappingTestUtils.fixed1to1Mappings(coverage);

					try(MappingImplSpanOneToMany mapping = config.create()) {
						MappingTestUtils.assert1to1Mapping(mapping, entries, null);
					}
				}))));
	}

	@TestFactory
	@RandomizedTest
	Stream<DynamicNode> testRead1To1Random(RandomGenerator rng) {
		return configurations().map(config -> dynamicContainer(config.label,
				// grab a couple values for every test variable (reasonably exhaust value space)
				coverages().flatMap(coverage ->
				MappingTestUtils.testableSizes(config.valueType).boxed().flatMap(count ->  //1, 10, min(1000,type.max)
				IntStream.of(1).boxed().flatMap(multiplier ->  //1, 2, 4
				// create randomized sequences of mappings
				MappingTestUtils.random1to1Mappings(rng, coverage, count.intValue(),
						MappingTestUtils.randRange(rng, config.valueType, count.intValue(), multiplier.intValue()),
						MappingTestUtils.randRange(rng, config.valueType, count.intValue(), multiplier.intValue())).map(data ->
						// test each sequence in isolation
						dynamicTest(String.format("cov=%s count=%d mult=%d mode=%s",
								coverage, count, multiplier, data.first), () -> {
									config.prepareManifest(coverage);
									List<Pair<Integer, Integer>> entries = data.second;

									try(MappingImplSpanOneToMany mapping = config.create()) {
										MappingTestUtils.assert1to1Mapping(mapping, entries, rng);
									}
						})))))));
	}

	@TestFactory
	Stream<DynamicNode> testRead1ToNSpanFixed() {
		return configurations().map(config -> dynamicContainer(config.label,
				coverages().map(coverage -> dynamicTest(coverage.name(), () -> {
					config.prepareManifest(coverage);
					List<Triple<Integer, Integer, Integer>> entries = MappingTestUtils.fixed1toNSpanMappings(coverage);

					try(MappingImplSpanOneToMany mapping = config.create()) {
						MappingTestUtils.assert1toNSpanMapping(mapping, entries);
					}
				}))));
	}

	@TestFactory
	@RandomizedTest
	Stream<DynamicNode> testRead1ToNSpanRandom(RandomGenerator rng) {
		return configurations().map(config -> dynamicContainer(config.label,
				// grab a couple values for every test variable (reasonably exhaust value space)
				coverages().flatMap(coverage ->
				MappingTestUtils.testableSizes(config.valueType).boxed().flatMap(count ->  //1, 10, min(1000,type.max)
				IntStream.of(1).boxed().flatMap(multiplier ->  //1, 2, 4
				// create randomized sequences of mappings
				MappingTestUtils.random1toNSpanMappings(rng, coverage, count.intValue(),
						MappingTestUtils.randRange(rng, config.valueType, count.intValue(), multiplier.intValue()),
						MappingTestUtils.randSpanRange(rng, config.valueType, count.intValue(), multiplier.intValue())).map(data ->
						// test each sequence in isolation
						dynamicTest(String.format("cov=%s count=%d mult=%d mode=%s",
								coverage, count, multiplier, data.first), () -> {
									config.prepareManifest(coverage);
									List<Triple<Integer, Integer, Integer>> entries = data.second;

									try(MappingImplSpanOneToMany mapping = config.create()) {
										MappingTestUtils.assert1toNSpanMapping(mapping, entries);
									}
						})))))));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.MappingWriter#map(long, long)}.
	 */
	@TestFactory
	@RandomizedTest
	Stream<DynamicNode> testMapSpanToSpan(RandomGenerator rng) {
		return configurations().map(config -> dynamicContainer(config.label,
				// grab a couple values for every test variable
				coverages().flatMap(coverage ->
				IntStream.of(1, 10, 100).boxed().flatMap(count ->
				IntStream.of(1, 4).boxed().flatMap(multiplier ->
				// create randomized sequences of mappings
				MappingTestUtils.random1to1Mappings(rng, coverage, count.intValue(),
						MappingTestUtils.randRange(rng, config.valueType, count.intValue(), multiplier.intValue()),
						MappingTestUtils.randRange(rng, config.valueType, count.intValue(), multiplier.intValue())).map(data ->
						// test each sequence in isolation
						dynamicTest(String.format("cov=%s count=%d mult=%d mode=%s",
								coverage, count, multiplier, data.first), () -> {
									try(MappingImplSpanOneToMany mapping = config.create();
											MappingWriter writer = mapping.newWriter()) {
										try {
											writer.begin();
											// in this test we only write the data, no read verification!
											data.second.forEach(p -> {
												long source = p.first.longValue();
												long target = p.second.longValue();
												// Span mapping
												writer.map(source, source, target, target);
											});
										} finally {
											writer.end();
										}
									}
						})))))));
	}

	private static ConfigImpl basicConfig() {
		return config(IndexValueType.INTEGER, 4,
				RUBlockCache::newLeastRecentlyUsedCache, "LRU", 128);
	}

	@ParameterizedTest
	@CsvSource({
		"0, 0,  0",
		"0, 0,  1",
		"1, 4, 20",
		"6, 2,  5",
		"10, 12345678, 234567890",
	})
	void testSingleSpan(int sourceIndex, int targetFrom, int targetTo) throws Exception {
		for(Coverage coverage : Coverage.values()) {
			ConfigImpl config = basicConfig();
			config.prepareManifest(coverage);

			try(MappingImplSpanOneToMany mapping = config.create()) {
				try(MappingWriter writer = mapping.newWriter()) {
					writer.begin();
					try {
						writer.map(sourceIndex, sourceIndex, targetFrom, targetTo);
					} finally {
						writer.end();
					}
				}

				try(MappingReader reader = mapping.newReader()) {
					reader.begin();
					try {
						RequestSettings settings = RequestSettings.none();

						assertThat(reader.getIndicesCount(sourceIndex, settings))
							.as("Span length of %s", triple(sourceIndex, targetFrom, targetTo))
							.isEqualTo(targetTo-targetFrom+1);

						assertThat(reader.getBeginIndex(sourceIndex, settings))
							.as("Begin index of %s", triple(sourceIndex, targetFrom, targetTo))
							.isEqualTo(targetFrom);

						assertThat(reader.getEndIndex(sourceIndex, settings))
							.as("End index of %s", triple(sourceIndex, targetFrom, targetTo))
							.isEqualTo(targetTo);
					} finally {
						reader.end();
					}
				}
			}
		}
	}

	@ParameterizedTest
	@CsvSource({
		"0, 0",
		"1, 4",
		"6, 2",
		"7, 0"
	})
	void testSingleBlockLookupSingle(int sourceIndex, int targetIndex) throws Exception {
		for(Coverage coverage : Coverage.values()) {
			ConfigImpl config = basicConfig();
			config.prepareManifest(coverage);

			try(MappingImplSpanOneToMany mapping = config.create()) {
				try(MappingWriter writer = mapping.newWriter()) {
					writer.begin();
					try {
						writer.map(sourceIndex, targetIndex);
					} finally {
						writer.end();
					}
				}

				try(MappingReader reader = mapping.newReader()) {
					reader.begin();
					try {
						RequestSettings settings = RequestSettings.none();

						assertThat(reader.getIndicesCount(sourceIndex, settings))
							.as("Span length of %s", pair(sourceIndex, targetIndex))
							.isEqualTo(1);

						assertThat(reader.getBeginIndex(sourceIndex, settings))
							.as("Begin index of %s", pair(sourceIndex, targetIndex))
							.isEqualTo(targetIndex);

						assertThat(reader.getEndIndex(sourceIndex, settings))
							.as("End index of %s", pair(sourceIndex, targetIndex))
							.isEqualTo(targetIndex);
					} finally {
						reader.end();
					}
				}
			}
		}
	}

	@ParameterizedTest
	@CsvSource({
		"0, 0, 1",
		"1, 4, 100",
		"6, 2, 66"
	})
	void testSingleBlockLookupSpan(int sourceIndex, int targetFrom, int targetTo) throws Exception {
		for(Coverage coverage : Coverage.values()) {
			ConfigImpl config = basicConfig();
			config.prepareManifest(coverage);

			try(MappingImplSpanOneToMany mapping = config.create()) {
				try(MappingWriter writer = mapping.newWriter()) {
					writer.begin();
					try {
						writer.map(sourceIndex, sourceIndex, targetFrom, targetTo);
					} finally {
						writer.end();
					}
				}

				try(MappingReader reader = mapping.newReader()) {
					reader.begin();
					try {
						RequestSettings settings = RequestSettings.none();

						assertThat(reader.getIndicesCount(sourceIndex, settings))
							.as("Span length of %s", triple(sourceIndex, targetFrom, targetTo))
							.isEqualTo(targetTo-targetFrom+1);

						assertThat(reader.getBeginIndex(sourceIndex, settings))
							.as("Begin index of %s", triple(sourceIndex, targetFrom, targetTo))
							.isEqualTo(targetFrom);

						assertThat(reader.getEndIndex(sourceIndex, settings))
							.as("End index of %s", triple(sourceIndex, targetFrom, targetTo))
							.isEqualTo(targetTo);
					} finally {
						reader.end();
					}
				}
			}
		}
	}

	@ParameterizedTest
	@CsvSource({
		"1000,   0,  1", // 1 block
		"1000,   0,  10", // some blocks at the front
		"1000, 976, 999", // some blocks at the very end
		"1000, 472, 721" // intermediate blocks
	})
	@DisplayName("test the find() method for multiple target values")
	void testMultiBlockSearchMulti(int size, int from, int to) throws Exception {
		for(Coverage coverage : Coverage.values()) {
			ConfigImpl config = basicConfig();
			config.prepareManifest(coverage);

			try(MappingImplSpanOneToMany mapping = config.create()) {
				try(MappingWriter writer = mapping.newWriter()) {
					writer.begin();
					try {
						for (int i = 0; i < size; i++) {
							writer.map(i, i);
						}
					} finally {
						writer.end();
					}
				}

				try(MappingReader reader = mapping.newReader()) {
					reader.begin();
					try {
						int span = to-from+1;
						IndexSet[] target = wrap(IndexUtils.span(from, to));
						IndexBuffer buffer = new IndexBuffer(config.valueType, size);
						assertThat(reader.find(0, Long.MAX_VALUE, target, buffer, RequestSettings.none()))
							.as("coverage=%s span=%s", coverage, pair(from, to)).isTrue();
						assertThat(buffer)
							.as("coverage=%s span=%s", coverage, pair(from, to))
							.hasSize(span)
							.hasSameIndicesAs(target[0]);
					} finally {
						reader.end();
					}
				}
			}
		}
	}

	@ParameterizedTest
	@CsvSource({
		"1000,   0,   1,   3,   5", // 1 block
		"1000,   0,  10,  22,  87", // some blocks at the front
		"1000, 976, 979, 985, 999", // some blocks at the very end
		"1000, 472, 521, 599, 721" // intermediate blocks
	})
	@DisplayName("test the find() method for multiple sets of target values")
	void testMultiBlockSearchMultiSets(int size, int from1, int to1, int from2, int to2) throws Exception {
		for(Coverage coverage : Coverage.values()) {
			ConfigImpl config = basicConfig();
			config.prepareManifest(coverage);

			try(MappingImplSpanOneToMany mapping = config.create()) {
				try(MappingWriter writer = mapping.newWriter()) {
					writer.begin();
					try {
						for (int i = 0; i < size; i++) {
							writer.map(i, i);
						}
					} finally {
						writer.end();
					}
				}

				try(MappingReader reader = mapping.newReader()) {
					reader.begin();
					try {
						int span1 = to1-from1+1;
						int span2 = to2-from2+1;
						IndexSet[] target = new IndexSet[] {
								IndexUtils.span(from1, to1), IndexUtils.span(from2, to2)};
						IndexBuffer buffer = new IndexBuffer(config.valueType, size);
						assertThat(reader.find(0, Long.MAX_VALUE, target, buffer, RequestSettings.none())).isTrue();
						assertThat(buffer).hasSize(span1 + span2).containsExactlyIndices(IndexUtils.asIterator(target));
					} finally {
						reader.end();
					}
				}
			}
		}
	}

	@Nested
	class Internals {

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.MappingImplSpanOneToMany#getBlockStorage()}.
		 */
		@TestFactory
		Stream<DynamicNode> testGetBlockStorage() {
			return Stream.of(IndexValueType.values()).map(type -> dynamicTest(type.name(), () -> {
				ConfigImpl config = basicConfig();
				config.valueType = type;
				MappingImplSpanOneToMany mapping = config.create();
				assertThat(mapping.getBlockStorage()).isSameAs(IndexBlockStorage.forValueType(type));
			}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.MappingImplSpanOneToMany#getEntriesPerBlock()}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {3, 10, MappingImplSpanOneToMany.DEFAULT_BLOCK_POWER, 24})
		void testGetEntriesPerBlock(int blockPower) {
			ConfigImpl config = basicConfig();
			config.blockPower = blockPower;
			MappingImplSpanOneToMany mapping = config.create();
			assertThat(mapping.getEntriesPerBlock()).isEqualTo(1<<blockPower);
		}
	}

	@Nested
	class ForBuilder implements StoredMappingBuilderTest<MappingImplSpanOneToMany, MappingImplSpanOneToMany.Builder> {

		/**
		 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
		 */
		@Override
		public Class<?> getTestTargetClass() {
			return MappingImplSpanOneToMany.Builder.class;
		}

		/**
		 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
		 */
		@Override
		public Builder createTestInstance(TestSettings settings) {
			return settings.process(MappingImplSpanOneToMany.builder());
		}

		/**
		 * @see de.ims.icarus2.util.BuilderTest#invalidOps()
		 */
		@Override
		public List<Triple<String, Class<? extends Throwable>, Consumer<? super Builder>>> invalidOps() {
			return list(
					triple("zero cacheSize", IllegalArgumentException.class, b -> b.cacheSize(0)),
					triple("negative cacheSize ", IllegalArgumentException.class, b -> b.cacheSize(-12345)),

					triple("zero blockPower", IllegalArgumentException.class, b -> b.blockPower(0)),
					triple("negative blockPower", IllegalArgumentException.class, b -> b.blockPower(-12367))
			);
		}
	}

	private MappingImplSpanOneToMany createMapping() {
		MappingManifest manifest = mock(MappingManifest.class);
		when(manifest.getCoverage()).thenReturn(Optional.of(Coverage.PARTIAL));
		return MappingImplSpanOneToMany.builder()
				.blockCache(new UnlimitedBlockCache())
				.blockPower(4)
				.driver(mock(Driver.class))
				.sourceLayer(mock(ItemLayerManifestBase.class))
				.targetLayer(mock(ItemLayerManifestBase.class))
				.manifest(manifest)
				.resource(new VirtualIOResource(DEFAULT_PATH))
				.valueType(IndexValueType.LONG)
				.cacheSize(100)
				.build();
	}

	//READER TESTS



	// WRITER TESTS



	// WRITER TESTS

	@Nested
	class ForReaderApi implements MappingReaderTest {

		/**
		 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
		 */
		@Override
		public Class<?> getTestTargetClass() {
			return MappingImplSpanOneToMany.Reader.class;
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessorTest#createSource()
		 */
		@Override
		public Mapping createSource() {
			return createMapping();
		}
	}

	@Nested
	class ForWriterApi implements MappingWriterTest {

		/**
		 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
		 */
		@Override
		public Class<?> getTestTargetClass() {
			return MappingImplSpanOneToMany.Writer.class;
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessorTest#createSource()
		 */
		@Override
		public WritableMapping createSource() {
			return createMapping();
		}
	}

	static class ConfigImpl extends AbstractConfig<MappingImplSpanOneToMany> {

		public int blockPower;

		@Override
		protected Relation relation() { return Relation.ONE_TO_MANY; }

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingTest.Config#create()
		 */
		@Override
		public MappingImplSpanOneToMany create() {
			return MappingImplSpanOneToMany.builder()
					.blockPower(blockPower)
					.cacheSize(cacheSize)
					.driver(driver)
					.manifest(manifest)
					.sourceLayer(sourceLayer)
					.targetLayer(targetLayer)
					.valueType(valueType)
					// Dynamic "per invocation" fields
					.resource(mockDelegate(IOResource.class, resourceGen.get()))
					.blockCache(blockCacheGen.get())
					.build();
		}

	}
}
