/**
 *
 */
package de.ims.icarus2.filedriver.mapping;

import static de.ims.icarus2.model.api.driver.indices.IndexUtils.EMPTY;
import static de.ims.icarus2.test.TestUtils.mockDelegate;
import static de.ims.icarus2.test.util.Pair.pair;
import static de.ims.icarus2.test.util.Triple.triple;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static de.ims.icarus2.util.lang.Primitives._int;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingReader;
import de.ims.icarus2.model.api.driver.mapping.MappingReaderTest;
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
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.test.util.Triple;
import de.ims.icarus2.util.io.resource.IOResource;
import de.ims.icarus2.util.io.resource.VirtualIOResource;

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

	private static ConfigImpl basicConfig() {
		return config(IndexValueType.INTEGER, 4, 4,
				RUBlockCache::newLeastRecentlyUsedCache, "LRU", 128);
	}

	private void writeMapping(MappingImplSpanManyToOne mapping,
			int sourceFrom, int sourceTo, int targetIndex) {
		// Our mapping
		try(MappingWriter writer = mapping.newWriter()) {
			writer.begin();
			try {
				writer.map(sourceFrom, sourceTo, targetIndex, targetIndex);
			} finally {
				writer.end();
			}
		}

		// Inverse mapping
		try(MappingWriter writer = ((WritableMapping)mapping.getInverseMapping()).newWriter()) {
			writer.begin();
			try {
				writer.map(targetIndex, targetIndex, sourceFrom, sourceTo);
			} finally {
				writer.end();
			}
		}
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
				writeMapping(mapping, sourceFrom, sourceTo, targetIndex);

				try(MappingReader reader = mapping.newReader()) {
					reader.begin();
					try {
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
					} finally {
						reader.end();
					}
				}
			}
		}
	}

	@Test
	void testSearch() throws Exception {
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
