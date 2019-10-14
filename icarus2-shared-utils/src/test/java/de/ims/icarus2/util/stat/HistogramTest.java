/**
 *
 */
package de.ims.icarus2.util.stat;

import static de.ims.icarus2.test.TestTags.RANDOMIZED;
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.test.TestUtils.displayString;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.test.TestUtils.randomIntStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.util.stat.Histogram.ArrayHistogram;

/**
 * @author Markus Gärtner
 *
 */
class HistogramTest {

	/**
	 * Test method for {@link de.ims.icarus2.util.stat.Histogram#fixedHistogram(int)}.
	 */
	@Test
	void testFixedHistogram() {
		Histogram hist = Histogram.fixedHistogram(100);
		assertNotNull(hist);
		assertEquals(100, hist.bins());
	}

	interface BaseTest<H extends Histogram> extends GenericTest<H> {

		/**
		 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
		 */
		@Provider
		@Override
		default H createTestInstance(TestSettings settings) {
			return settings.process(createForBins(10));
		}

		@Provider
		H createForBins(int bins);

		@Provider
		H createForRange(long from, long to);

		/**
		 * Test method for {@link de.ims.icarus2.util.stat.Histogram#bins()}.
		 */
		@Tag(RANDOMIZED)
		@TestFactory
		default Stream<DynamicTest> testBins() {
			return randomIntStream(100)
					.distinct()
					.limit(RUNS)
					.map(i -> i+1)
					.mapToObj(bins -> dynamicTest(String.valueOf(bins), () -> {
						H histogram = createForBins(bins);
						assertEquals(bins, histogram.bins());
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.stat.Histogram#lowerBound(int)}.
		 */
		@Tag(RANDOMIZED)
		@TestFactory
		default Stream<DynamicTest> testLowerBound() {
			return random()
					.longs(Long.MIN_VALUE/2, Long.MAX_VALUE/2)
					.distinct()
					.limit(RUNS)
					.mapToObj(lowerBound -> dynamicTest(displayString(lowerBound), () -> {
						H histogram = createForRange(lowerBound, lowerBound+100);
						assertEquals(lowerBound, histogram.lowerBound(0));
						assertEquals(lowerBound+99, histogram.lowerBound(99));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.stat.Histogram#higherBound(int)}.
		 */
		@Tag(RANDOMIZED)
		@TestFactory
		default Stream<DynamicTest> testHigherBound() {
			return random()
					.longs(Long.MIN_VALUE/2, Long.MAX_VALUE/2)
					.distinct()
					.limit(RUNS)
					.mapToObj(higherBound -> dynamicTest(displayString(higherBound), () -> {
						H histogram = createForRange(higherBound-100, higherBound);
						assertEquals(higherBound, histogram.higherBound(100));
						assertEquals(higherBound-100, histogram.higherBound(0));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.stat.Histogram#entries()}.
		 */
		@Test
		default void testEntriesEmpty() {
			assertEquals(0, createForBins(100).entries());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.stat.Histogram#entries()}.
		 */
		@RepeatedTest(RUNS)
		default void testEntries() {
			Histogram hist = createForBins(100);

			int count = random(1, 1000);
			for (int i = 0; i < count; i++) {
				hist.accept(10);
			}

			assertEquals(count, hist.entries());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.stat.Histogram#freq(int)}.
		 */
		@Test
		default void testFreqEmpty() {
			Histogram hist = createForBins(100);
			for (int i = 0; i < 100; i++) {
				assertEquals(0, hist.freq(i));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.stat.Histogram#freq(int)}.
		 */
		@Test
		default void testFreq() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.stat.Histogram#bin(long)}.
		 */
		@Test
		default void testBin() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.stat.Histogram#average()}.
		 */
		@Test
		default void testAverage() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.stat.Histogram#min()}.
		 */
		@Test
		default void testMin() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.stat.Histogram#max()}.
		 */
		@Test
		default void testMax() {
			fail("Not yet implemented"); // TODO
		}

	}

	@Nested
	class ForArrayHistogram implements BaseTest<ArrayHistogram> {

		/**
		 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
		 */
		@Override
		public Class<?> getTestTargetClass() {
			return ArrayHistogram.class;
		}

		/**
		 * @see de.ims.icarus2.util.stat.HistogramTest.BaseTest#createForBins(int)
		 */
		@Override
		public ArrayHistogram createForBins(int bins) {
			return ArrayHistogram.fixed(bins);
		}

		/**
		 * @see de.ims.icarus2.util.stat.HistogramTest.BaseTest#createForRange(int, int)
		 */
		@Override
		public ArrayHistogram createForRange(long from, long to) {
			return ArrayHistogram.range(from, to);
		}

	}
}
