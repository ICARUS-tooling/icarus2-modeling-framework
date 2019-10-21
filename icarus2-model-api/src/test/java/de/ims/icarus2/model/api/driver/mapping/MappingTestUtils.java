/**
 *
 */
package de.ims.icarus2.model.api.driver.mapping;

import static de.ims.icarus2.test.util.Pair.longPair;
import static de.ims.icarus2.test.util.Pair.pair;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.Streams;

import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.util.Pair;

/**
 * @author Markus Gärtner
 *
 */
public class MappingTestUtils {

//	public static class LongPair {
//		public final long first, second;
//
//		LongPair(long first, long second) {
//			this.first = first;
//			this.second = second;
//		}
//	}
//
//	public static LongPair paur(long first, long second) {
//		return new LongPair(first, second);
//	}

	/** Picks a random value inside the specified range (inclusive, exclusive) */
	private static long rand(RandomGenerator rng, Pair<Long, Long>  range) {
		return rng.random(range.first.longValue(), range.second.longValue());
	}

	/** Picks a random index inside the size of the given {@code range} */
	private static int randIdx(RandomGenerator rng, Pair<Long, Long>  range) {
		return rng.random(0, strictToInt(range.second.longValue()-range.first.longValue()));
	}

	/** Picks a random value between 25% and 100% of the given count */
	private static int fraction(RandomGenerator rng, int count) {
		return rng.random(count>>2, count);
	}

	private static int range(Pair<Long, Long>  range) {
		long dif = range.second.longValue() - range.first.longValue();
		assertTrue(dif>0, "Difference must be positive "+ dif);
		return strictToInt(dif);
	}

	public static final int TEST_LIMIT = 1_000_000;

	public static Pair<Long, Long> randRange(RandomGenerator rng, int count, int multiplier) {
		long end = rng.random(count*multiplier, TEST_LIMIT);
		return longPair(end- count*multiplier, end);
	}

	/**
	 *
	 * @param rng source of randomness
	 * @param coverage type of mapping
	 * @param count the size of the mapping
	 * @param source the source index space (inclusive, exclusive)
	 * @param target the target index space (inclusive, exclusive)
	 * @return
	 */
	public static Stream<Pair<String,Stream<Pair<Long, Long>>>> create1to1Mappings(RandomGenerator rng,
			Coverage coverage, int count, Pair<Long, Long> source, Pair<Long, Long> target) {

		int rs = range(source);
		int rt = range(target);
		assertTrue(rs>=count, "Source range too small");

		switch (coverage) {

		case PARTIAL: {
			// (random, random)
			return Stream.of(
					pair("random full", Stream.generate(() -> longPair(rand(rng, source), rand(rng, target)))
							.limit(count)),
					pair("random partial", Stream.generate(() -> longPair(rand(rng, source), rand(rng, target)))
							.limit(fraction(rng, count)))
			);
		}

		case TOTAL_MONOTONIC: {
			// (source[i], target[i])
//			assertEquals(count, rs, "Count mismatch");
//			assertEquals(rs, rt, "Range mismatch");
			return Stream.of(pair("full", IntStream.range(0, count)
					.mapToObj(offset -> longPair(
							source.first.longValue()+offset,
							target.first.longValue()+offset))));
		}

		case MONOTONIC: {
			assertTrue(rt>=rs, "Target range too small");
			return Stream.of(pair("random equally sized", Streams.zip(
					IntStream.generate(() -> randIdx(rng, source))
						.distinct()
						.limit(count)
						.sorted()
						.boxed(),
					IntStream.generate(() -> randIdx(rng, target))
						.limit(count)
						.sorted()
						.boxed(),
					(s, t) -> longPair(
							source.first.longValue()+s.intValue(),
							target.first.longValue()+t.intValue())
			)));
		}

		case TOTAL: {
			assertTrue(rt>=count, "Target range too small");
			return Stream.of(pair("random", IntStream.range(0, count)
					.mapToObj(offset -> longPair(
							rand(rng, source),
							target.first.longValue()+offset))));
		}

		default:
			throw new InternalError("Coverage type not handled: "+coverage);
		}
	}

	//TODO produce mapping sequences and verifications
}
