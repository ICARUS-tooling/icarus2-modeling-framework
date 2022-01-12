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
package de.ims.icarus2.filedriver.mapping;

import static de.ims.icarus2.model.api.ModelTestUtils.matcher;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.EMPTY;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.wrap;
import static de.ims.icarus2.test.TestUtils.mockDelegate;
import static de.ims.icarus2.test.util.Pair.pair;
import static de.ims.icarus2.test.util.Triple.triple;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static de.ims.icarus2.util.lang.Primitives._int;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.apiguard.OptionalMethodNotSupported;
import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;
import de.ims.icarus2.filedriver.io.RUBlockCache;
import de.ims.icarus2.filedriver.io.UnlimitedBlockCache;
import de.ims.icarus2.filedriver.mapping.MappingImplSpanManyToOne.Builder;
import de.ims.icarus2.filedriver.mapping.StoredMappingTest.AbstractConfig;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.IndexCollector;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingReader;
import de.ims.icarus2.model.api.driver.mapping.MappingReaderTest;
import de.ims.icarus2.model.api.driver.mapping.MappingTestUtils;
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
import it.unimi.dsi.fastutil.longs.LongArrayList;

/**
 * @author Markus Gärtner
 *
 */
class MappingImplSpanManyToOneTest implements WritableMappingTest<MappingImplSpanManyToOne, MappingImplSpanManyToOneTest.ConfigImpl> {

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.mapping.MappingImplSpanManyToOne#builder()}.
	 */
	@Test
	void testBuilder() {
		assertThat(MappingImplSpanManyToOne.builder()).isNotNull();
	}

	@Override
	public Stream<ConfigImpl> configurations() {
		return
			Stream.of(IndexValueType.values()).flatMap(valueType ->
			IntStream.of(3, 10).boxed().flatMap(blockPower ->
			IntStream.of(3, 10).boxed().flatMap(groupPower ->
			Stream.<Pair<String,Supplier<BlockCache>>>of(pair("unlimited", UnlimitedBlockCache::new),
					pair("LRU", RUBlockCache::newLeastRecentlyUsedCache)).flatMap(pCache ->
			IntStream.of(BlockCache.MIN_CAPACITY, 1024).boxed().map(cacheSize -> {
				return config(valueType, blockPower.intValue(), groupPower.intValue(),
						pCache.second, pCache.first, cacheSize.intValue());
			})))));
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.mapping.MappingTest#basicConfiguration()
	 */
	@Override
	public ConfigImpl basicConfiguration() {
		return config(IndexValueType.INTEGER, 4, 4, RUBlockCache::newLeastRecentlyUsedCache, "LRU", 128);
	}

	private static ConfigImpl config(IndexValueType valueType,
			int blockPower, int groupPower,
			Supplier<BlockCache> cacheGen,
			String cacheLabel, int cacheSize) {
		ConfigImpl config = new ConfigImpl();
		config.label = String.format("type=%s blockPower=%d cache=%s cacheSize=%d",
				valueType, _int(blockPower), cacheLabel, _int(cacheSize));

		config.blockCacheGen = cacheGen;
		config.blockPower = blockPower;
		config.groupPower = groupPower;
		config.cacheSize = cacheSize;
		config.valueType = valueType;
		config.resourceGen = VirtualIOResource::new;

		config.driver = mock(Driver.class);
		config.sourceLayer = mock(ItemLayerManifestBase.class);
		config.targetLayer = mock(ItemLayerManifestBase.class);
		config.manifest = mock(MappingManifest.class);

		config.inverseMappingGen = () -> {
			Optional<Coverage> coverage = config.manifest.getCoverage();
			MappingManifest manifest = mock(MappingManifest.class);
			when(manifest.getCoverage()).thenReturn(coverage);
			when(manifest.getRelation()).thenReturn(Optional.of(Relation.ONE_TO_MANY));

			return MappingImplSpanOneToMany.builder()
				.cacheSize(cacheSize)
				.blockPower(blockPower)
				.resource(new VirtualIOResource())
				.blockCache(cacheGen.get())
				.valueType(valueType)
				.sourceLayer(mock(ItemLayerManifestBase.class))
				.targetLayer(mock(ItemLayerManifestBase.class))
				.driver(mock(Driver.class))
				.manifest(manifest)
				.build();
		};

		config.prepareManifest(Coverage.PARTIAL);

		return config;
	}

	private MappingImplSpanOneToMany createInverseMapping() {
		MappingManifest manifest = mock(MappingManifest.class);
		when(manifest.getCoverage()).thenReturn(Optional.of(Coverage.PARTIAL));

		return MappingImplSpanOneToMany.builder()
				.blockCache(new UnlimitedBlockCache())
				.blockPower(4)
				.driver(mock(Driver.class))
				.sourceLayer(mock(ItemLayerManifestBase.class))
				.targetLayer(mock(ItemLayerManifestBase.class))
				.manifest(manifest)
				.resource(new VirtualIOResource())
				.valueType(IndexValueType.LONG)
				.cacheSize(100)
				.build();
	}

	private MappingImplSpanManyToOne createMapping() {
		MappingManifest manifest = mock(MappingManifest.class);
		when(manifest.getCoverage()).thenReturn(Optional.of(Coverage.PARTIAL));

		return MappingImplSpanManyToOne.builder()
				.blockCache(new UnlimitedBlockCache())
				.blockPower(4)
				.groupPower(4)
				.driver(mock(Driver.class))
				.sourceLayer(mock(ItemLayerManifestBase.class))
				.targetLayer(mock(ItemLayerManifestBase.class))
				.manifest(manifest)
				.resource(new VirtualIOResource())
				.valueType(IndexValueType.LONG)
				.cacheSize(100)
				.inverseMapping(createInverseMapping())
				.build();
	}

	@TestFactory
	Stream<DynamicNode> testReadNTo1SpanFixed() {
		return configurations().map(config -> dynamicContainer(config.label,
				coverages().map(coverage -> dynamicTest(coverage.name(), () -> {
					config.prepareManifest(coverage);
					List<Triple<Integer, Integer, Integer>> entries = MappingTestUtils.fixedNto1SpanMappings(coverage);

					try(MappingImplSpanManyToOne mapping = config.create()) {
						MappingTestUtils.assertNto1SpanMapping(mapping, entries);
					}
				}))));
	}

	@TestFactory
	@RandomizedTest
	Stream<DynamicNode> testReadNTo1SpanRandom(RandomGenerator rng) {
		return configurations().map(config -> dynamicContainer(config.label,
				// grab a couple values for every test variable (reasonably exhaust value space)
				coverages().flatMap(coverage ->
				MappingTestUtils.testableSizes(config.valueType).boxed().flatMap(count ->  //1, 10, min(1000,type.max)
				IntStream.of(1).boxed().flatMap(multiplier ->  //1, 2, 4
				// create randomized sequences of mappings
				MappingTestUtils.randomNto1SpanMappings(rng, coverage, count.intValue(),
						MappingTestUtils.randRange(rng, config.valueType, count.intValue(), multiplier.intValue()),
						MappingTestUtils.randSpanRange(rng, config.valueType, count.intValue(), multiplier.intValue())).map(data ->
						// test each sequence in isolation
						dynamicTest(String.format("cov=%s count=%d mult=%d mode=%s",
								coverage, count, multiplier, data.first), () -> {
									config.prepareManifest(coverage);
									List<Triple<Integer, Integer, Integer>> entries = data.second;

									try(MappingImplSpanManyToOne mapping = config.create()) {
										MappingTestUtils.assertNto1SpanMapping(mapping, entries);
									}
						})))))));
	}

	/** Create a config for LRU cache and INTEGER type */
	private static ConfigImpl basicConfig() {
		return config(IndexValueType.INTEGER, 4, 4,
				RUBlockCache::newLeastRecentlyUsedCache, "LRU", 128);
	}

//	private void writeMapping(MappingImplSpanManyToOne mapping,
//			int sourceFrom, int sourceTo, int targetIndex) {
//		// Our mapping
//		try(MappingWriter writer = mapping.newWriter()) {
//			writer.begin();
//			try {
//				writer.map(sourceFrom, sourceTo, targetIndex, targetIndex);
//			} finally {
//				writer.end();
//			}
//		}
//
//		// Inverse mapping
//		try(MappingWriter writer = ((WritableMapping)mapping.getInverseMapping()).newWriter()) {
//			writer.begin();
//			try {
//				writer.map(targetIndex, targetIndex, sourceFrom, sourceTo);
//			} finally {
//				writer.end();
//			}
//		}
//	}

	private void assertSpan(MappingReader reader, int sourceFrom, int sourceTo, int targetIndex) throws  Exception{
		RequestSettings settings = RequestSettings.none();

		assertThat(reader.getIndicesCount(sourceFrom, settings))
			.as("Span length for begin of %s", triple(sourceFrom, sourceTo, targetIndex))
			.isEqualTo(1);
		assertThat(reader.getIndicesCount(sourceFrom, settings))
			.as("Span length for end of %s", triple(sourceFrom, sourceTo, targetIndex))
			.isEqualTo(1);

		assertThat(reader.getBeginIndex(sourceFrom, settings))
			.as("Begin index for begin of %s", triple(sourceFrom, sourceTo, targetIndex))
			.isEqualTo(targetIndex);
		assertThat(reader.getBeginIndex(sourceFrom, settings))
			.as("Begin index for end of %s", triple(sourceFrom, sourceTo, targetIndex))
			.isEqualTo(targetIndex);

		assertThat(reader.getEndIndex(sourceFrom, settings))
			.as("End index for begin of %s", triple(sourceFrom, sourceTo, targetIndex))
			.isEqualTo(targetIndex);
		assertThat(reader.getEndIndex(sourceFrom, settings))
			.as("End index for end of %s", triple(sourceFrom, sourceTo, targetIndex))
			.isEqualTo(targetIndex);

		assertThat(reader.lookup(sourceFrom, settings))
			.as("Lookup for begin of %s", triple(sourceFrom, sourceTo, targetIndex))
			.hasSize(1)
			.allMatch(matcher(targetIndex));
		List<Long> collector1 = new LongArrayList();
		assertThat(reader.lookup(sourceFrom, collector1::add, settings))
			.as("Collector for begin of %s", triple(sourceFrom, sourceTo, targetIndex))
			.isTrue();
		assertThat(collector1).hasSize(1).containsExactly(Long.valueOf(targetIndex));

		assertThat(reader.lookup(sourceTo, settings))
			.as("Lookup for end of %s", triple(sourceFrom, sourceTo, targetIndex))
			.hasSize(1)
			.allMatch(matcher(targetIndex));
		List<Long> collector2 = new LongArrayList();
		assertThat(reader.lookup(sourceTo, collector2::add, settings))
			.as("Collector for end of %s", triple(sourceFrom, sourceTo, targetIndex))
			.isTrue();
		assertThat(collector2).hasSize(1).containsExactly(Long.valueOf(targetIndex));

		assertThat(reader.lookup(wrap(sourceFrom), settings))
			.as("Batch lookup for begin of %s", triple(sourceFrom, sourceTo, targetIndex))
			.hasSize(1)
			.allMatch(matcher(targetIndex));
		List<Long> collector3 = new LongArrayList();
		assertThat(reader.lookup(wrap(sourceFrom), collector3::add, settings))
			.as("Batch collector for begin of %s", triple(sourceFrom, sourceTo, targetIndex))
			.isTrue();
		assertThat(collector3).hasSize(1).containsExactly(Long.valueOf(targetIndex));

		assertThat(reader.lookup(wrap(sourceTo), settings))
			.as("Batch lookup for end of %s", triple(sourceFrom, sourceTo, targetIndex))
			.hasSize(1)
			.allMatch(matcher(targetIndex));
		List<Long> collector4 = new LongArrayList();
		assertThat(reader.lookup(wrap(sourceTo), collector4::add, settings))
			.as("Batch collector for end of %s", triple(sourceFrom, sourceTo, targetIndex))
			.isTrue();
		assertThat(collector4).hasSize(1).containsExactly(Long.valueOf(targetIndex));
	}

	@ParameterizedTest
	@CsvSource({
		"0, 0,  0",
		"0, 1,  0",
		"4, 20, 1",
		"2,  5, 6",
		"12345678, 234567890, 10",
	})
	void testSingleSpan(int sourceFrom, int sourceTo, int targetIndex) throws Exception {
		for(Coverage coverage : Coverage.values()) {
			ConfigImpl config = basicConfig();
			config.prepareManifest(coverage);

			try(MappingImplSpanManyToOne mapping = config.create()) {
				MappingTestUtils.writeNto1Mapping(mapping, sourceFrom, sourceTo, targetIndex);

				try(MappingReader reader = mapping.newReader()) {
					reader.begin();
					try {
						assertSpan(reader, sourceFrom, sourceTo, targetIndex);
					} finally {
						reader.end();
					}
				}
			}
		}
	}

	//TODO

	@ParameterizedTest
	@CsvSource({
		"MONOTONIC,   0,   0,   1,   1,   3,   5", // 1 block
		"PARTIAL,    20,   0,  10,   4,  22,  87", // some blocks at the front
		"MONOTONIC, 500, 976, 979, 505, 985, 999", // some blocks at the very end
		"PARTIAL,   111, 472, 521,  10, 599, 721" // intermediate blocks
	})
	void testMultiSpanLookup(Coverage coverage, int target1, int from1, int to1, int target2, int from2, int to2) throws Exception {

		ConfigImpl config = basicConfig();
		config.prepareManifest(coverage);

		try(MappingImplSpanManyToOne mapping = config.create()) {
			MappingTestUtils.writeNto1Mapping(mapping, from1, to1, target1);
			MappingTestUtils.writeNto1Mapping(mapping, from2, to2, target2);

			try(MappingReader reader = mapping.newReader()) {
				reader.begin();
				try {
					RequestSettings settings = RequestSettings.none();
					int span1 = to1-from1+1;
					int span2 = to2-from2+1;
					IndexSet[] target = new IndexSet[] {
							IndexUtils.span(from1, to1), IndexUtils.span(from2, to2)};
					IndexBuffer buffer = new IndexBuffer(config.valueType, span1+span2);

					assertSpan(reader, from1, to1, target1);
					assertSpan(reader, from2, to2, target2);
				} finally {
					reader.end();
				}
			}
		}
	}

	@Test
	void testSearchNotSupported() throws Exception {
		for(Coverage coverage : Coverage.values()) {
			ConfigImpl config = basicConfig();
			config.prepareManifest(coverage);

			try(MappingImplSpanManyToOne mapping = config.create()) {
				try(MappingReader reader = mapping.newReader()) {
					RequestSettings settings = RequestSettings.none();

					reader.begin();
					try {
						assertThatExceptionOfType(OptionalMethodNotSupported.class)
							.as("Single lookup")
							.isThrownBy(() -> reader.find(0, Integer.MAX_VALUE, 0, settings));

						assertThatExceptionOfType(OptionalMethodNotSupported.class)
							.as("Batch lookup")
							.isThrownBy(() -> reader.find(0, Integer.MAX_VALUE, EMPTY, settings));

						assertThatExceptionOfType(OptionalMethodNotSupported.class)
							.as("Batch collect")
							.isThrownBy(() -> reader.find(0, Integer.MAX_VALUE, EMPTY, mock(IndexCollector.class), settings));
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
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.MappingImplSpanManyToOne#getBlockStorage()}.
		 */
		@TestFactory
		Stream<DynamicNode> testGetBlockStorage() {
			return Stream.of(IndexValueType.values()).map(type -> dynamicTest(type.name(), () -> {
				ConfigImpl config = basicConfig();
				config.valueType = type;
				MappingImplSpanManyToOne mapping = config.create();
				assertThat(mapping.getBlockStorage()).isSameAs(IndexBlockStorage.forValueType(type));
			}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.MappingImplSpanManyToOne#getInverseMapping()}.
		 */
		@Test
		void testGetInverseMapping() {
			ConfigImpl config = basicConfig();
			Mapping inverseMapping = mock(Mapping.class);
			config.inverseMappingGen = () -> inverseMapping;
			MappingImplSpanManyToOne mapping = config.create();
			assertThat(mapping.getInverseMapping()).isSameAs(inverseMapping);
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.MappingImplSpanManyToOne#getGroupsPerBlock()}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {3, 10, MappingImplSpanManyToOne.DEFAULT_BLOCK_POWER, 24})
		void testGetEntriesPerBlock(int blockPower) {
			ConfigImpl config = basicConfig();
			config.blockPower = blockPower;
			MappingImplSpanManyToOne mapping = config.create();
			assertThat(mapping.getGroupsPerBlock()).isEqualTo(1<<blockPower);
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.MappingImplSpanManyToOne#getEntriesPerGroup()}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {3, 10, MappingImplSpanOneToMany.DEFAULT_BLOCK_POWER, 24})
		void testGetEntriesPerGroup(int groupPower) {
			ConfigImpl config = basicConfig();
			config.groupPower = groupPower;
			MappingImplSpanManyToOne mapping = config.create();
			assertThat(mapping.getEntriesPerGroup()).isEqualTo(1<<groupPower);
		}

	}

	@Nested
	class ForBuilder implements StoredMappingBuilderTest<MappingImplSpanManyToOne, MappingImplSpanManyToOne.Builder> {

		/**
		 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
		 */
		@Override
		public Class<?> getTestTargetClass() {
			return MappingImplSpanManyToOne.Builder.class;
		}

		/**
		 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
		 */
		@Override
		public Builder createTestInstance(TestSettings settings) {
			return settings.process(MappingImplSpanManyToOne.builder());
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
			return MappingImplSpanManyToOne.Reader.class;
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
			return MappingImplSpanManyToOne.Writer.class;
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessorTest#createSource()
		 */
		@Override
		public WritableMapping createSource() {
			return createMapping();
		}
	}

	static class ConfigImpl extends AbstractConfig<MappingImplSpanManyToOne> {

		public int blockPower;
		public int groupPower;
		public Supplier<Mapping> inverseMappingGen;

		@Override
		protected Relation relation() { return Relation.MANY_TO_ONE; }

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingTest.Config#create()
		 */
		@Override
		public MappingImplSpanManyToOne create() {
			return MappingImplSpanManyToOne.builder()
					.blockPower(blockPower)
					.groupPower(groupPower)
					.cacheSize(cacheSize)
					.driver(driver)
					.manifest(manifest)
					.sourceLayer(sourceLayer)
					.targetLayer(targetLayer)
					.valueType(valueType)
					// Dynamic "per invocation" fields
					.resource(mockDelegate(IOResource.class, resourceGen.get()))
					.blockCache(blockCacheGen.get())
					.inverseMapping(inverseMappingGen.get())
					.build();
		}

	}

}
