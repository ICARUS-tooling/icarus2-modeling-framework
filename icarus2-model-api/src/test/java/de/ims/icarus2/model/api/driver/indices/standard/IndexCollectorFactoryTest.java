/**
 *
 */
package de.ims.icarus2.model.api.driver.indices.standard;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.randomIndices;
import static de.ims.icarus2.model.api.ModelTestUtils.sortedIndices;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.wrap;
import static de.ims.icarus2.test.TestTags.RANDOMIZED;
import static de.ims.icarus2.test.TestTags.SLOW;
import static de.ims.icarus2.test.TestUtils.MAX_INTEGER_INDEX;
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.test.TestUtils.displayString;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.lang.Primitives._int;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.LongConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.driver.indices.IndexCollector;
import de.ims.icarus2.model.api.driver.indices.IndexCollectorTest;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.BucketSetBuilder;
import de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.IndexSetBuilder;
import de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.LimitedSortedSetBuilder;
import de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.LimitedUnsortedSetBuilderInt;
import de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.LimitedUnsortedSetBuilderLong;
import de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.LimitedUnsortedSetBuilderShort;
import de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.UnlimitedSortedSetBuilder;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * @author Markus Gärtner
 *
 */
class IndexCollectorFactoryTest {

	/**
	 * Constant to limit the randomized size of index sets for testing.
	 *
	 * Set to {@code ~131 k}
	 */
	private static final int MAX_TEST_SIZE = 1<<17;

	private static Stream<TestParams> params(int cycles) {
		return Stream.of(IndexValueType.values())
				.filter(type -> type!=IndexValueType.BYTE)
				.map(valueType -> new TestParams(valueType, cycles));
	}

	private static Executable wrapAssertUnsorted(Executable executable) {
		return () -> {
			assertModelException(ModelErrorCode.MODEL_UNSORTED_INPUT, executable);
		};
	}

	private static Executable wrapAssertUnsortedIndices(Executable executable) {
		return () -> {
			assertModelException(ModelErrorCode.MODEL_UNSORTED_INDEX_SET, executable);
		};
	}

	private static Stream<DynamicNode> defaultCreateTests(int cycles,
			boolean sorted, boolean random,
			Function<TestParams, IndexSetBuilder> builderGen) {
		return params(cycles).map(params -> dynamicContainer(
					displayString(
							"%s [size=%s, buffer=%s, chunkSize=%s, cycles=%s]",
							params.valueType, _int(params.size), _int(params.bufferSize),
							_int(params.chunkSize), _int(params.cycles)),
					config()
					.indexValueType(params.valueType)
					.sorted(sorted)
					.random(random)
					.start(params.start)
					.size(params.size)
					.cycles(params.cycles)
					.createTests(() -> builderGen.apply(params))));
	}

	@TestFactory
	private static List<DynamicTest> createUnsortedInputTests(
			Supplier<IndexCollector> collectorGen) {
		return Arrays.asList(
				dynamicTest("long + long", wrapAssertUnsorted(() -> {
					IndexCollector collector = collectorGen.get();
					collector.add(2);
					collector.add(1);
				})),
				dynamicTest("IndexSet + IndexSet", wrapAssertUnsorted(() -> {
					IndexCollector collector = collectorGen.get();
					collector.add(IndexUtils.wrapSpan(1, 3));
					collector.add(IndexUtils.wrapSpan(2, 4));

				})),
				dynamicTest("long + IndexSet", wrapAssertUnsorted(() -> {
					IndexCollector collector = collectorGen.get();
					collector.add(4);
					collector.add(IndexUtils.wrapSpan(1, 3));
				})),
				dynamicTest("IndexSet + long", wrapAssertUnsorted(() -> {
					IndexCollector collector = collectorGen.get();
					collector.add(IndexUtils.wrapSpan(1, 3));
					collector.add(2);
				}))
		);

	}

	/**
	 * @see LimitedSortedSetBuilder
	 *
	 * @author Markus Gärtner
	 *
	 */
	@Nested
	class ForLimitedSortedSetBuilder {

		@Nested
		@DisplayName("invalid arguments")
		class ForInvalidArguments implements IndexCollectorTest<LimitedSortedSetBuilder> {

			/**
			 * @see de.ims.icarus2.model.api.driver.indices.IndexCollectorTest#create()
			 */
			@Override
			public LimitedSortedSetBuilder create() {
				return new LimitedSortedSetBuilder(IndexValueType.INTEGER, 8, 16);
			}

			@Test
			void testConstructor_null() {
				assertNPE(() -> new LimitedSortedSetBuilder(null, 1, 1));
			}

			@Test
			void testConstructor_invalidBufferSize() {
				assertModelException(GlobalErrorCode.INVALID_INPUT,
						() -> new LimitedSortedSetBuilder(IndexValueType.INTEGER, 0, 1));
			}

			@Test
			void testConstructor_overflowBufferSize() {
				assertModelException(GlobalErrorCode.INDEX_OVERFLOW,
						() -> new LimitedSortedSetBuilder(IndexValueType.INTEGER, MAX_INTEGER_INDEX+1, 1));
			}

			@Test
			void testConstructor_invalidChunkSize() {
				assertModelException(GlobalErrorCode.INVALID_INPUT,
						() -> new LimitedSortedSetBuilder(IndexValueType.INTEGER, 1, 0));
			}

			@Test
			void testUnsortedInput() {
				IndexCollector collector = create();
				wrapAssertUnsortedIndices(() -> collector.add(wrap(1, 2, 4, 3)));
			}

			@TestFactory
			List<DynamicTest> testUnsortedInputSequence() {
				return createUnsortedInputTests(this::create);
			}
		}

		@Nested
		@DisplayName("sorted input")
		class ForSortedInput {

			@Tag(RANDOMIZED)
			@TestFactory
			Stream<DynamicNode> testSortedInputSingleCycle() {
				return defaultCreateTests(1, true, false, params -> new LimitedSortedSetBuilder(
						params.valueType, params.bufferSize, params.chunkSize));
			}

			@Tag(RANDOMIZED)
			@Tag(SLOW)
			@TestFactory
			Stream<DynamicNode> testSortedInputMultiCycle() {
				return defaultCreateTests(RUNS, true, false, params -> new LimitedSortedSetBuilder(
						params.valueType, params.bufferSize, params.chunkSize));
			}
		}
	}

	/**
	 * @see UnlimitedSortedSetBuilder
	 *
	 * @author Markus Gärtner
	 *
	 */
	@Nested
	class ForUnlimitedSortedSetBuilder {

		@Nested
		class ForInvalidArguments implements IndexCollectorTest<UnlimitedSortedSetBuilder> {

			/**
			 * @see de.ims.icarus2.model.api.driver.indices.IndexCollectorTest#create()
			 */
			@Override
			public UnlimitedSortedSetBuilder create() {
				return new UnlimitedSortedSetBuilder(IndexValueType.INTEGER, 8);
			}

			@Test
			void testConstructor_null() {
				assertNPE(() -> new UnlimitedSortedSetBuilder(null, 1));
			}

			@Test
			void testConstructor_invalid() {
				assertModelException(GlobalErrorCode.INVALID_INPUT,
						() -> new UnlimitedSortedSetBuilder(IndexValueType.INTEGER, 0));
			}

			@Test
			void testConstructor_overflow() {
				assertModelException(GlobalErrorCode.INDEX_OVERFLOW,
						() -> new UnlimitedSortedSetBuilder(IndexValueType.INTEGER, MAX_INTEGER_INDEX+1));
			}

			@Test
			void testUnsortedInput() {
				IndexCollector collector = create();
				wrapAssertUnsortedIndices(() -> collector.add(wrap(1, 2, 4, 3)));
			}

			@TestFactory
			List<DynamicTest> testUnsortedInputSequence() {
				return createUnsortedInputTests(this::create);
			}
		}

		@Nested
		@DisplayName("sorted input")
		class ForSortedInput {

			@Tag(RANDOMIZED)
			@TestFactory
			Stream<DynamicNode> testSortedInputSingleCycle() {
				return defaultCreateTests(1, true, false, params -> new UnlimitedSortedSetBuilder(
						params.valueType, params.chunkSize));
			}

			@Tag(RANDOMIZED)
			@Tag(SLOW)
			@TestFactory
			Stream<DynamicNode> testSortedInputMultiCycle() {
				return defaultCreateTests(RUNS, true, false, params -> new UnlimitedSortedSetBuilder(
						params.valueType, params.chunkSize));
			}
		}
	}

	/**
	 * @see LimitedUnsortedSetBuilderLong
	 *
	 * @author Markus Gärtner
	 *
	 */
	@Nested
	class ForLimitedUnsortedSetBuilderLong {

		@Nested
		class ForInvalidArguments implements IndexCollectorTest<LimitedUnsortedSetBuilderLong> {

			/**
			 * @see de.ims.icarus2.model.api.driver.indices.IndexCollectorTest#create()
			 */
			@Override
			public LimitedUnsortedSetBuilderLong create() {
				return new LimitedUnsortedSetBuilderLong(1, 1);
			}

			@Test
			void testConstructor_invalidCapacity() {
				assertModelException(GlobalErrorCode.INVALID_INPUT,
						() -> new LimitedUnsortedSetBuilderLong(0, 1));
			}

			@Test
			void testConstructor_invalidChunkSize() {
				assertModelException(GlobalErrorCode.INVALID_INPUT,
						() -> new LimitedUnsortedSetBuilderLong(1, 0));
			}
		}

		@Nested
		@DisplayName("sorted input")
		class ForSortedInput {

			@Tag(RANDOMIZED)
			@TestFactory
			Stream<DynamicNode> testSortedInputSingleCycle() {
				return defaultCreateTests(1, true, false, params ->
					new LimitedUnsortedSetBuilderLong(params.bufferSize, params.chunkSize));
			}

			@Tag(RANDOMIZED)
			@Tag(SLOW)
			@TestFactory
			Stream<DynamicNode> testSortedInputMultiCycle() {
				return defaultCreateTests(RUNS, true, false, params ->
					new LimitedUnsortedSetBuilderLong(params.bufferSize, params.chunkSize));
			}
		}

		@Nested
		@DisplayName("random input")
		class ForRandomInput {

			@Tag(RANDOMIZED)
			@TestFactory
			Stream<DynamicNode> testSortedInputSingleCycle() {
				return defaultCreateTests(1, false, true, params ->
					new LimitedUnsortedSetBuilderLong(params.bufferSize, params.chunkSize));
			}

			@Tag(RANDOMIZED)
			@Tag(SLOW)
			@TestFactory
			Stream<DynamicNode> testSortedInputMultiCycle() {
				return defaultCreateTests(RUNS, false, true, params ->
					new LimitedUnsortedSetBuilderLong(params.bufferSize, params.chunkSize));
			}

		}

		@Nested
		@DisplayName("mixed input")
		class ForMixedInput {

			@Tag(RANDOMIZED)
			@TestFactory
			Stream<DynamicNode> testSortedInputSingleCycle() {
				return defaultCreateTests(1, true, true, params ->
					new LimitedUnsortedSetBuilderLong(params.bufferSize, params.chunkSize));
			}

			@Tag(RANDOMIZED)
			@Tag(SLOW)
			@TestFactory
			Stream<DynamicNode> testSortedInputMultiCycle() {
				return defaultCreateTests(RUNS, true, true, params ->
					new LimitedUnsortedSetBuilderLong(params.bufferSize, params.chunkSize));
			}

		}
	}

	/**
	 * @see LimitedUnsortedSetBuilderInt
	 *
	 * @author Markus Gärtner
	 *
	 */
	@Nested
	class ForLimitedUnsortedSetBuilderInt {
		//TODO
	}

	/**
	 * @see LimitedUnsortedSetBuilderShort
	 *
	 * @author Markus Gärtner
	 *
	 */
	@Nested
	class ForLimitedUnsortedSetBuilderShort {
		//TODO
	}

	/**
	 * @see BucketSetBuilder
	 *
	 * @author Markus Gärtner
	 *
	 */
	@Nested
	class ForBucketSetBuilder {
		//TODO
	}

	/**
	 * Encapsulates numerical parameters for randomized tests,
	 * generated based on the {@link IndexValueType} and number
	 * of cycles for a planned test.
	 *
	 * @author Markus Gärtner
	 *
	 */
	private static class TestParams {
		/** Number of cycles (min 1) */
		final int cycles;
		/** Expected size of data for single cycle */
		final int size;
		/** Required buffer to hold data of all cycles */
		final int bufferSize;
		/** Upper limit of individual chunks in output */
		final int chunkSize;
		/** Start value of sorted input for cycles */
		final long start;
		/** Value type of buffer */
		final IndexValueType valueType;

		TestParams(IndexValueType valueType, int cycles) {
			checkArgument(cycles>0);
			this.valueType = requireNonNull(valueType);
			this.cycles = cycles;

			long rawMax = valueType.maxValue();
			int max = rawMax>MAX_INTEGER_INDEX ? MAX_INTEGER_INDEX : (int) rawMax;
			int sizeMax = cycles>1 ? max/cycles : max;
			size = random(64, Math.min(MAX_TEST_SIZE, sizeMax));
			bufferSize = size * cycles;

			int bytes = Math.min(2, valueType.bytesPerValue());
			int maxPower = bytes*8;
			chunkSize = 1<<random(2, maxPower);

			int maxStart = max - (cycles * size);
			start = random(0, maxStart);
		}
	}

	static TestConfig config() {
		return new TestConfig();
	}

	/**
	 * Encapsulates all the information to test a single
	 * {@link IndexSetBuilder} instance.
	 *
	 * @author Markus Gärtner
	 *
	 */
	private static class TestConfig {
		private IndexSet explicit;
		private boolean random = false;
		private boolean sorted = false;
		private int size;
		private long start = 0;
		private int cycles = 1;
		private IndexValueType indexValueType;

		TestConfig indexValueType(IndexValueType indexValueType) {
			this.indexValueType = requireNonNull(indexValueType);
			return this;
		}

		TestConfig random(boolean random) {
			this.random = random;
			return this;
		}

		TestConfig sorted(boolean sorted) {
			this.sorted = sorted;
			return this;
		}

		TestConfig size(int size) {
			assumeTrue(size>0, "Size must be positive");
			this.size = size;
			return this;
		}

		TestConfig start(long start) {
			this.start = start;
			return this;
		}

		TestConfig explicit(IndexSet explicit) {
			assumeTrue(explicit!=null, "Explicit index set must not be null");
			this.explicit = explicit;
			return this;
		}

		TestConfig cycles(int cycles) {
			assumeTrue(cycles>0, "Size must be positive");
			this.cycles = cycles;
			return this;
		}

		private static DynamicTest createTest(String displayName,
				Supplier<IndexSetBuilder> builderGen,
				Supplier<IndexSet> indicesGen,
				int cycles) {
			assertNotNull(displayName);
			assertNotNull(builderGen);
			assertNotNull(indicesGen);
			assertTrue(cycles>0, "Need at least 1 cycle of indices added");

			return dynamicTest(displayName, () -> {
				IndexSetBuilder builder = builderGen.get();

				assertNotNull(builder);

				LongSet input = new LongOpenHashSet(1<<16);
				LongSet output = new LongOpenHashSet(1<<16);

				for(int cycle = 0; cycle < cycles; cycle++) {
					IndexSet indices = indicesGen.get();
					assertNotNull(indices);
					assertFalse(indices.isEmpty());

					// Store and forward new input data
					indices.forEachIndex((LongConsumer)input::add);
					builder.accept(indices);

					// Make sure 'output' only reflects current cycle
					output.clear();

					// Fetch accumulated output data so far
					IndexSet[] tmp = builder.build();
					assertNotNull(output, "No indices returned in cycle "+cycle);
					assertFalse(tmp.length==0, "Empty result in cycle "+cycle);
					IndexUtils.forEachIndex(tmp, (LongConsumer)output::add);

					// Main check
					assertEquals(input.size(), output.size(), "Size mismatch of created indices");

					// Kind of a backup to make sure we don't have weird artifacts/transformations
					output.removeAll(input);
					assertTrue(output.isEmpty(), () ->
						"Total unknown indices: "+output.size());
				}
			});
		}

		private void checkDynamicReq() {
			assumeTrue(size>0, "Size not set");
			assumeTrue(indexValueType!=null, "Index value type not set");
		}

		Stream<DynamicNode> createTests(Supplier<IndexSetBuilder> builderGen) {
			List<DynamicNode> tests = new ArrayList<>();

			if(explicit!=null) {
				tests.add(createTest("Explicit IndexSet ["+explicit.size()+"]",
						builderGen, () -> explicit, cycles));
			}

			if(random) {
				checkDynamicReq();
				tests.add(createTest("Random indices ["+size+"]",
						builderGen, () -> randomIndices(indexValueType, size), cycles));
			}

			if(sorted) {
				checkDynamicReq();
				MutableLong start = new MutableLong(this.start);
				tests.add(createTest(String.format("Sorted indices (%d-%d)",
							start.get(), _int(start.intValue()+size)),
						builderGen, () -> sortedIndices(size, start.getAndIncrement(size)), cycles));
			}

			assumeFalse(tests.isEmpty(), "Config did not define any tests");

			return tests.stream();
		}
	}
}
