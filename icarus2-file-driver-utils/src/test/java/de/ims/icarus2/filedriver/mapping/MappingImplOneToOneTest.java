/**
 *
 */
package de.ims.icarus2.filedriver.mapping;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.matcher;
import static de.ims.icarus2.model.api.ModelTestUtils.set;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.wrap;
import static de.ims.icarus2.test.util.Pair.pair;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;
import de.ims.icarus2.filedriver.io.RUBlockCache;
import de.ims.icarus2.filedriver.io.UnlimitedBlockCache;
import de.ims.icarus2.filedriver.mapping.AbstractStoredMappingTest.AbstractConfig;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet;
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
import de.ims.icarus2.test.annotations.PostponedTest;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.util.io.resource.VirtualIOResource;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

/**
 * @author Markus Gärtner
 *
 */
class MappingImplOneToOneTest implements WritableMappingTest<MappingImplOneToOne> {

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.mapping.MappingImplOneToOne#builder()}.
	 */
	@Test
	void testBuilder() {
		assertNotNull(MappingImplOneToOne.builder());
	}

	@Override
	public Stream<ConfigImpl> configurations() {
		return
			Stream.of(IndexValueType.values()).flatMap(valueType ->
			IntStream.of(3, 6, 10).boxed().flatMap(blockPower ->
			Stream.<Pair<String,Supplier<BlockCache>>>of(pair("unlimited", UnlimitedBlockCache::new),
					pair("LRU", RUBlockCache::newLeastRecentlyUsedCache)).flatMap(pCache ->
			IntStream.of(BlockCache.MIN_CAPACITY, 1024).boxed().map(cacheSize -> {
				ConfigImpl config = new ConfigImpl();
				config.label = String.format("type=%s blockPower=%d cache=%s cacheSize=%d",
						valueType, blockPower, pCache.first, cacheSize);

				config.blockCacheGen = pCache.second;
				config.blockPower = blockPower.intValue();
				config.cacheSize = cacheSize.intValue();
				config.valueType = valueType;
				config.resourceGen = VirtualIOResource::new;

				config.driver = mock(Driver.class);
				config.sourceLayer = mock(ItemLayerManifestBase.class);
				config.targetLayer = mock(ItemLayerManifestBase.class);
				config.manifest = mock(MappingManifest.class);

				return config;
			}))));
	}

	//READER TESTS

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(long, RequestSettings)}.
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.MappingReader#getBeginIndex(de.ims.icarus2.model.api.driver.indices.IndexSet[], RequestSettings)}.
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.MappingReader#getEndIndex(long, RequestSettings)}.
	 */
	@TestFactory
	Stream<DynamicNode> testRead1To1Fixed() {
		return configurations().map(config -> dynamicContainer(config.label,
				coverages().map(coverage -> dynamicTest(coverage.name(), () -> {
					config.prepareManifest(coverage, Relation.ONE_TO_ONE);
					List<Pair<Long, Long>> entries = MappingTestUtils.fixed1to1Mappings(coverage);

					try(MappingImplOneToOne mapping = config.create()) {
						assert1to1Mapping(mapping, entries, null);
					}
				}))));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(long, RequestSettings)}.
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.MappingReader#getBeginIndex(de.ims.icarus2.model.api.driver.indices.IndexSet[], RequestSettings)}.
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.MappingReader#getEndIndex(long, RequestSettings)}.
	 */
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
									config.prepareManifest(coverage, Relation.ONE_TO_ONE);
									List<Pair<Long, Long>> entries = data.second;

									try(MappingImplOneToOne mapping = config.create()) {
										assert1to1Mapping(mapping, entries, rng);
									}
						})))))));
	}

	// WRITER TESTS

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.MappingWriter#map(long, long)}.
	 */
	@TestFactory
	@RandomizedTest
	Stream<DynamicNode> testMap1To1(RandomGenerator rng) {
		return configurations().map(config -> dynamicContainer(config.label,
				// grab a couple values for every test variable
				coverages().flatMap(coverage ->
				IntStream.of(1, 10, 100).boxed().flatMap(count ->
				IntStream.of(1, 2, 4).boxed().flatMap(multiplier ->
				// create randomized sequences of mappings
				MappingTestUtils.random1to1Mappings(rng, coverage, count.intValue(),
						MappingTestUtils.randRange(rng, config.valueType, count.intValue(), multiplier.intValue()),
						MappingTestUtils.randRange(rng, config.valueType, count.intValue(), multiplier.intValue())).map(data ->
						// test each sequence in isolation
						dynamicTest(String.format("cov=%s count=%d mult=%d mode=%s",
								coverage, count, multiplier, data.first), () -> {
									try(MappingImplOneToOne mapping = config.create();
											MappingWriter writer = mapping.newWriter()) {
										try {
											writer.begin();
											// in this test we only write the data, no read verification!
											data.second.forEach(p -> {
												long source = p.first.longValue();
												long target = p.second.longValue();
												// Direct 1-1
												writer.map(source, target);
											});
										} finally {
											writer.end();
										}
									}
						})))))));
	}

	// WRITER TESTS

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
									try(MappingImplOneToOne mapping = config.create();
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

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.MappingWriter#map(long, long, long, long)}.
	 */
	@TestFactory
	Stream<DynamicNode> testIllegalSpanMapping() {
		return writerTests((config, writer) -> {
			writer.begin();
			try {
				assertModelException(GlobalErrorCode.INVALID_INPUT, () -> writer.map(0, 1, 1, 1)); // source span
				assertModelException(GlobalErrorCode.INVALID_INPUT, () -> writer.map(1, 1, 0, 1)); // target span
				//TODO any more cases to cover here?
			} finally {
				writer.end();
			}
		});
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.MappingWriter#map(de.ims.icarus2.model.api.driver.indices.IndexSet, de.ims.icarus2.model.api.driver.indices.IndexSet)}.
	 */
	@TestFactory
	@RandomizedTest
	Stream<DynamicNode> testMapIndexSetIndexSet(RandomGenerator rng) {
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
									try(MappingImplOneToOne mapping = config.create();
											MappingWriter writer = mapping.newWriter()) {
										try {
											writer.begin();
											long[] sources = MappingTestUtils.extractSources(data.second);
											long[] targets = MappingTestUtils.extractTargets(data.second);
											// in this test we only write the data, no read verification!
											writer.map(set(sources), set(targets));
										} finally {
											writer.end();
										}
									}
						})))))));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.MappingWriter#map(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
	 */
	@TestFactory
	@RandomizedTest
	Stream<DynamicNode> testMapIndexSetArrayIndexSetArray(RandomGenerator rng) {
		return configurations().map(config -> dynamicContainer(config.label,
				// grab a couple values for every test variable
				coverages().flatMap(coverage ->
				IntStream.of(10, 100).boxed().flatMap(count ->
				IntStream.of(1, 4).boxed().flatMap(multiplier ->
				// create randomized sequences of mappings
				MappingTestUtils.random1to1Mappings(rng, coverage, count.intValue(),
						MappingTestUtils.randRange(rng, config.valueType, count.intValue(), multiplier.intValue()),
						MappingTestUtils.randRange(rng, config.valueType, count.intValue(), multiplier.intValue())).map(data ->
						// test each sequence in isolation
						dynamicTest(String.format("cov=%s count=%d mult=%d mode=%s",
								coverage, count, multiplier, data.first), () -> {
									try(MappingImplOneToOne mapping = config.create();
											MappingWriter writer = mapping.newWriter()) {
										try {
											writer.begin();
											long[] sources = MappingTestUtils.extractSources(data.second);
											long[] targets = MappingTestUtils.extractTargets(data.second);
											int split = sources.length/2;
											// in this test we only write the data, no read verification!
											writer.map(new ArrayIndexSet(sources).split(split),
													new ArrayIndexSet(targets).split(split));
										} finally {
											writer.end();
										}
									}
						})))))));
	}

	static <M extends WritableMapping> void assert1to1Mapping(M mapping,
			List<Pair<Long, Long>> entries, RandomGenerator rng) throws Exception	{
		assert !entries.isEmpty() : "test data is empty";

		try(MappingWriter writer = mapping.newWriter()) {
			writer.begin();
			try {
				// write all mappings
				entries.forEach(p -> writer.map(
						p.first.longValue(), p.second.longValue()));
			} finally {
				writer.end();
			}
		}

		// DEBUG
//		System.out.println(entries);

		try(MappingReader reader = mapping.newReader()) {
			reader.begin();
			try {
				// run sequential read and assert (source -> target)
				for(Pair<Long, Long> entry : entries) {
					RequestSettings settings = RequestSettings.none();
					long source = entry.first.longValue();
					long target = entry.second.longValue();

					// Size queries
					assertThat(reader.getIndicesCount(source, settings))
						.as("Indices count").isEqualTo(1);

					// Single-value lookups
					assertThat(reader.getBeginIndex(source, settings))
						.as("Begin index").isEqualTo(target);
					assertThat(reader.getEndIndex(source, settings))
						.as("End index").isEqualTo(target);

					// Bulk lookups
					assertThat(reader.lookup(source, settings))
						.as("Bulk lookup (single source)")
						.hasSize(1).doesNotContainNull()
						.allMatch(set -> set.size()==1, "Size mismatch")
						.allMatch(set -> set.firstIndex()==target, "Target mismatch");
					assertThat(reader.lookup(wrap(source), settings))
						.as("Bulk lookup")
						.hasSize(1).doesNotContainNull()
						.allMatch(set -> set.size()==1, "Size mismatch")
						.allMatch(set -> set.firstIndex()==target, "Target mismatch");

					// Collector tests
					LongList valueBuffer1 = new LongArrayList();
					assertThat(reader.lookup(source, valueBuffer1::add, settings))
						.as("Collector lookup (single source)").isTrue();
					assertThat(valueBuffer1.toLongArray())
						.as("Target mismatch").containsExactly(target);

					LongList valueBuffer2 = new LongArrayList();
					assertThat(reader.lookup(wrap(source), valueBuffer2::add, settings))
						.as("Collector lookup (bulk source)").isTrue();
					assertThat(valueBuffer2.toLongArray())
						.as("Target mismatch").containsExactly(target);

					// Bulk span boundaries
					assertThat(reader.getBeginIndex(wrap(source), settings))
						.as("Span begin (bulk source)").isEqualTo(target);
					assertThat(reader.getEndIndex(wrap(source), settings))
						.as("Span end (bulk source)").isEqualTo(target);

					// Reverse lookup
					assertThat(reader.find(source, source, target, settings))
						.as("Single reverse lookup (explicit)").isEqualTo(source);
					assertThat(reader.find(source, source, wrap(target), settings))
						.as("Bulk reverse lookup (explicit)")
						.hasSize(1).allMatch(matcher(source));

					// Reverse lookup
					assertThat(reader.find(Math.max(0, source-1), source, target, settings))
						.as("Single reverse lookup (open begin)").isEqualTo(source);
					assertThat(reader.find(Math.max(0, source-1), source, wrap(target), settings))
						.as("Bulk reverse lookup (open begin)")
						.hasSize(1).allMatch(matcher(source));
					assertThat(reader.find(source, Long.MAX_VALUE, target, settings))
						.as("Single reverse lookup (open end)").isEqualTo(source);
					assertThat(reader.find(source, Long.MAX_VALUE, wrap(target), settings))
						.as("Bulk reverse lookup (open end)")
						.hasSize(1).allMatch(matcher(source));
				}

				// do some real bulk lookups and searches

			} finally {
				reader.end();
			}
		}
	}

	@Nested
	@PostponedTest("need to revise strategy for efficiently testing builder behavior in general")
	class ForBuilder {

		MappingImplOneToOne.Builder builder;

		@BeforeEach
		void setUp() {
			builder = MappingImplOneToOne.builder();
		}

		//TODO verify builder methods and check below 2 methods for consistency

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.MappingImplOneToOne#getBlockPower()}.
		 */
		@Test
		void testGetBlockPower() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.MappingImplOneToOne#getEntriesPerBlock()}.
		 */
		@Test
		void testGetEntriesPerBlock() {
			fail("Not yet implemented"); // TODO
		}

	}

	private MappingImplOneToOne createMapping() {
		MappingManifest manifest = mock(MappingManifest.class);
		when(manifest.getCoverage()).thenReturn(Optional.of(Coverage.PARTIAL));
		return MappingImplOneToOne.builder()
				.blockCache(new UnlimitedBlockCache())
				.driver(mock(Driver.class))
				.sourceLayer(mock(ItemLayerManifestBase.class))
				.targetLayer(mock(ItemLayerManifestBase.class))
				.manifest(manifest)
				.resource(new VirtualIOResource())
				.valueType(IndexValueType.LONG)
				.cacheSize(100)
				.build();
	}

	@Nested
	class ForReaderApi implements MappingReaderTest {

		/**
		 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
		 */
		@Override
		public Class<?> getTestTargetClass() {
			return MappingImplOneToOne.Reader.class;
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
			return MappingImplOneToOne.Writer.class;
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessorTest#createSource()
		 */
		@Override
		public WritableMapping createSource() {
			return createMapping();
		}
	}

	static class ConfigImpl extends AbstractConfig<MappingImplOneToOne> {

		public int blockPower;

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingTest.Config#create()
		 */
		@Override
		public MappingImplOneToOne create() {
			return MappingImplOneToOne.builder()
					.blockPower(blockPower)
					.cacheSize(cacheSize)
					.driver(driver)
					.manifest(manifest)
					.sourceLayer(sourceLayer)
					.targetLayer(targetLayer)
					.valueType(valueType)
					// Dynamic "per invocation" fields
					.resource(resourceGen.get())
					.blockCache(blockCacheGen.get())
					.build();
		}

	}
}
