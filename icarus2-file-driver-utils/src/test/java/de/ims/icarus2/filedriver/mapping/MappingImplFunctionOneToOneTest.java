/**
 *
 */
package de.ims.icarus2.filedriver.mapping;

import static de.ims.icarus2.model.api.ModelTestUtils.assertIndicesEquals;
import static de.ims.icarus2.model.api.ModelTestUtils.matcher;
import static de.ims.icarus2.model.api.ModelTestUtils.set;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.firstIndex;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.lastIndex;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.span;
import static de.ims.icarus2.test.TestUtils.mockDelegate;
import static de.ims.icarus2.test.util.Pair.nullablePair;
import static de.ims.icarus2.test.util.Pair.pair;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.LongUnaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.filedriver.mapping.MappingImplFunctionOneToOne.Builder;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingReader;
import de.ims.icarus2.model.api.driver.mapping.MappingReaderTest;
import de.ims.icarus2.model.api.driver.mapping.MappingTest;
import de.ims.icarus2.model.api.driver.mapping.RequestSettings;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;
import de.ims.icarus2.model.manifest.api.MappingManifest.Relation;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.util.BuilderTest;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

/**
 * @author Markus Gärtner
 *
 */
class MappingImplFunctionOneToOneTest implements MappingTest<MappingImplFunctionOneToOne, MappingImplFunctionOneToOneTest.ConfigImpl> {

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.mapping.MappingImplFunctionOneToOne#builder()}.
	 */
	@Test
	void testBuilder() {
		assertThat(MappingImplFunctionOneToOne.builder()).isNotNull();
	}

	@Override
	public Stream<ConfigImpl> configurations() {
		return coverages().flatMap(coverage ->
			Stream.of(IndexValueType.values()).flatMap(valueType ->
			Stream.<Pair<String, LongUnaryOperator>>of(pair("identity", IDENTITY),
					pair("double", (long v) -> v*2)).flatMap(pUnary ->
			Stream.<Pair<String, UnaryOperator<IndexSet>>>of(nullablePair("no batch", null),
					pair("batch identity", BATCH_IDENTITY)).map(pBatch ->
					config(coverage, valueType, pUnary, pBatch)))));
	}

	private static ConfigImpl config(Coverage coverage, IndexValueType valueType,
			Pair<String, LongUnaryOperator> pFunc, Pair<String, UnaryOperator<IndexSet>> pBatchFunc) {
		ConfigImpl config = new ConfigImpl();

		config.label = String.format("coverage=%s valueType=%s func=%s batch=%s",
				coverage, valueType, pFunc.first, pBatchFunc.first);

		config.valueType = valueType;
		config.unaryFunction = pFunc.second;
		config.batchFunction = pBatchFunc.second;

		config.driver = mock(Driver.class);
		config.sourceLayer = mock(ItemLayerManifestBase.class);
		config.targetLayer = mock(ItemLayerManifestBase.class);
		config.manifest = mock(MappingManifest.class);

		config.prepareManifest(Coverage.PARTIAL);

		return config;
	}

	@SuppressWarnings("boxing")
	@RandomizedTest
	@TestFactory
	Stream<DynamicNode> testUnaryLookups(RandomGenerator rng) {
		return readerTests((config, reader) -> dynamicTest(config.label, () -> {
			RequestSettings settings = RequestSettings.none();

			LongStream fixed = LongStream.of(0, 1, 100, 999_999);
			LongStream random = rng.longs(10, 0, config.valueType.maxValue());

			LongUnaryOperator func = config.unaryFunction;

			for(long value : LongStream.concat(fixed, random).toArray()) {
				assertThat(reader.getIndicesCount(value, settings))
					.as("Single map size for %d", value).isEqualTo(1);

				long expected = func.applyAsLong(value);
				IndexSet[] indices = reader.lookup(value, settings);
				// Single lookup
				assertThat(indices).hasSize(1)
					.as("Single lookup for %d", value).allMatch(matcher(expected));

				// Boundaries
				assertThat(reader.getBeginIndex(value, settings))
					.as("Single lookup (begin) for %d", value).isEqualTo(expected);
				assertThat(reader.getEndIndex(value, settings))
					.as("Single lookup (end) for %d", value).isEqualTo(expected);

				// Collector
				LongList list1 = new LongArrayList();
				assertThat(reader.lookup(value, list1::add, settings))
					.as("Single lookup (collector) for %d", value).isTrue();
				assertThat(list1.toLongArray()).containsExactly(expected);

				// Reverse lookup
				assertThat(reader.find(value, value, value, settings))
					.as("Single reverse lookup for %d", value).isEqualTo(expected);
			}
		}));
	}

	@RandomizedTest
	@TestFactory
	Stream<DynamicNode> testBatchLookups(RandomGenerator rng) {
		return configurations().filter(c -> c.batchFunction!=null).map(
				config -> dynamicTest(config.label, () -> {
					try(MappingImplFunctionOneToOne mapping = config.create();
							MappingReader reader = mapping.newReader()) {
						RequestSettings settings = RequestSettings.none();

						IndexSet set1 = set(0, 1, 2, 3, 4);
						IndexSet set2 = set(10, 100, 999, 999_999_999);
						IndexSet set3 = span(1000, 1300);
						IndexSet set4 = set(rng.randomLongs(10, 0, config.valueType.maxValue()));

						IndexSet[] indices = {set1, set2, set3, set4};

						UnaryOperator<IndexSet> batchFunc = config.batchFunction;
						IndexSet[] expected = {
								batchFunc.apply(set1),
								batchFunc.apply(set2),
								batchFunc.apply(set3),
								batchFunc.apply(set4)
						};

						// Batch lookup
						assertIndicesEquals(expected, reader.lookup(indices, settings));

						// Batch begin/end
						assertThat(reader.getBeginIndex(indices, settings)).isEqualTo(firstIndex(expected));
						assertThat(reader.getEndIndex(indices, settings)).isEqualTo(lastIndex(expected));

						// Batch collector
						LongList list1 = new LongArrayList();
						reader.lookup(indices, list1::add, settings);
						assertIndicesEquals(expected, list1.iterator());

						// Batch reverse lookup
						assertIndicesEquals(expected, reader.find(0, Long.MAX_VALUE, indices, settings));

						// Batch reverse collector
						LongList list2 = new LongArrayList();
						reader.find(0, Long.MAX_VALUE, indices, list2::add, settings);
						assertIndicesEquals(expected, list2.iterator());
					}

		}));
	}

	private static final LongUnaryOperator IDENTITY = v -> v;
	private static final UnaryOperator<IndexSet> BATCH_IDENTITY = set -> set;

	private MappingImplFunctionOneToOne createMapping() {
		MappingManifest manifest = mock(MappingManifest.class);
		when(manifest.getCoverage()).thenReturn(Optional.of(Coverage.PARTIAL));
		return MappingImplFunctionOneToOne.builder()
				.driver(mock(Driver.class))
				.sourceLayer(mock(ItemLayerManifestBase.class))
				.targetLayer(mock(ItemLayerManifestBase.class))
				.manifest(manifest)
				.valueType(IndexValueType.LONG)
				.unaryFunction(IDENTITY)
				.build();
	}

	@Nested
	class ForReaderApi implements MappingReaderTest {

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessorTest#createSource()
		 */
		@Override
		public Mapping createSource() {
			return createMapping();
		}

		/**
		 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
		 */
		@Override
		public Class<?> getTestTargetClass() {
			return MappingImplFunctionOneToOne.Reader.class;
		}

	}

	@Nested
	class ForBuilder implements BuilderTest<MappingImplFunctionOneToOne, MappingImplFunctionOneToOne.Builder> {

		/**
		 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
		 */
		@Override
		public Class<?> getTestTargetClass() {
			return Builder.class;
		}

		/**
		 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
		 */
		@Override
		public Builder createTestInstance(TestSettings settings) {
			return settings.process(MappingImplFunctionOneToOne.builder());
		}
	}

	static class ConfigImpl extends MappingTest.Config<MappingImplFunctionOneToOne> {

		LongUnaryOperator unaryFunction;
		UnaryOperator<IndexSet> batchFunction;

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingTest.Config#create()
		 */
		@Override
		public MappingImplFunctionOneToOne create() {
			Builder builder = MappingImplFunctionOneToOne.builder()
					.driver(driver)
					.manifest(manifest)
					.sourceLayer(sourceLayer)
					.targetLayer(targetLayer)
					.valueType(valueType)
					.unaryFunction(mockDelegate(LongUnaryOperator.class, unaryFunction));
			if(batchFunction!=null) {
				builder.batchFunction(mockDelegate(UnaryOperator.class, batchFunction));
			}
			return builder.build();
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingTest.Config#relation()
		 */
		@Override
		protected Relation relation() {
			return Relation.ONE_TO_ONE;
		}

	}
}
