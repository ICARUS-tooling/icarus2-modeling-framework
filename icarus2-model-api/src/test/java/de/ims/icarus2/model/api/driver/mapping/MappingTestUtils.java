/**
 *
 */
package de.ims.icarus2.model.api.driver.mapping;

import static de.ims.icarus2.test.util.Pair.longPair;
import static de.ims.icarus2.test.util.Pair.pair;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.google.common.collect.Streams;

import de.ims.icarus2.model.api.driver.indices.IndexValueType;
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

	private static LongStream rands(RandomGenerator rng, Pair<Long, Long> range, int size) {
		return LongStream.generate(() -> rand(rng, range)).distinct().limit(size);
	}

	private static Stream<Pair<Long, Long>> join(LongStream source, LongStream target) {
		return Streams.zip(source.boxed(), target.boxed(), Pair::pair);
	}

	private static List<Pair<Long, Long>> toList(Stream<Pair<Long, Long>> stream) {
		return stream.collect(Collectors.toList());
	}

	/** Picks a random index inside the size of the given {@code range} */
	private static int randIdx(RandomGenerator rng, Pair<Long, Long>  range) {
		return rng.random(0, strictToInt(range.second.longValue()-range.first.longValue()));
	}

	/** Picks a random value between 25% and 100% of the given count */
	private static int fraction(RandomGenerator rng, int count) {
		assert count>0;
		return Math.max(1, rng.random(count>>2, count));
	}

	private static int range(Pair<Long, Long>  range) {
		long dif = range.second.longValue() - range.first.longValue();
		assertTrue(dif>0, "Difference must be positive "+ dif);
		return strictToInt(dif);
	}

	public static final int TEST_LIMIT = 1000;

	public static Pair<Long, Long> randRange(RandomGenerator rng, IndexValueType valueType,
			int count, int multiplier) {
		long max = Math.min(TEST_LIMIT, valueType.maxValue()+1);
		if(max<0) max = TEST_LIMIT; // overflow
		long range = Math.min(count*multiplier, valueType.maxValue());
		long min = Math.max(0, range);
		assert min<max;
		long end = rng.random(min, max);
		long begin = Math.max(0, end - range);
		assert end-begin+1 >= count;
		return longPair(begin, end);
	}

	@SuppressWarnings("unchecked")
	public static List<Pair<Long, Long>> fixed1to1Mappings(Coverage coverage) {

		switch (coverage) {
		case PARTIAL: return list(
				longPair(1, 4),
				longPair(11, 2),
				longPair(99, 33),
				longPair(66, 4),
				longPair(2, 0)
			);
		case TOTAL: return list(
				longPair(0, 1),
				longPair(1, 3),
				longPair(3, 0),
				longPair(10, 4),
				longPair(12, 2)
			);
		case MONOTONIC: return list(
				longPair(0, 1),
				longPair(1, 3),
				longPair(3, 4),
				longPair(10, 10),
				longPair(12, 99)
			);
		case TOTAL_MONOTONIC: return list(
				longPair(0, 0),
				longPair(1, 1),
				longPair(3, 2),
				longPair(10, 3),
				longPair(12, 4)
			);

		default:
			throw new InternalError("Coverage type not handled: "+coverage);
		}
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
	public static Stream<Pair<String,List<Pair<Long, Long>>>> random1to1Mappings(RandomGenerator rng,
			Coverage coverage, int count, Pair<Long, Long> source, Pair<Long, Long> target) {

		int rs = range(source);
		int rt = range(target);
		assertTrue(rs>=count, "Source range too small");

		long sourceBegin = source.first.longValue();
		long targetBegin = target.first.longValue();

		switch (coverage) {

		case PARTIAL: {
			int part = fraction(rng, count);
			// (random, random)
			return Stream.of(
					pair("random full", toList(join(rands(rng, source, count), rands(rng, target, count)))),
					pair("random partial", toList(join(rands(rng, source, part), rands(rng, target, part))))
			);
		}

		case TOTAL_MONOTONIC: {
			return Stream.of(pair("full", toList(IntStream.range(0, count)
					.mapToObj(offset -> longPair(
							sourceBegin+offset,
							targetBegin+offset)))));
		}

		case MONOTONIC: {
			assertTrue(rt>=rs, "Target range too small");
			return Stream.of(pair("random equally sized", toList(join(
					rands(rng, source, count).sorted(),
					rands(rng, target, count).sorted()))));
		}

		case TOTAL: {
			assertTrue(rt>=count, "Target range too small");
			return Stream.of(pair("random", toList(join(
					rands(rng, source, count),
					LongStream.range(0, count)))));
		}

		default:
			throw new InternalError("Coverage type not handled: "+coverage);
		}
	}

	//TODO produce mapping sequences and verifications

	public void extractMapping(List<Pair<Long, Long>> entries, long[] sources, long[] targets) {
		for (int i = 0; i < sources.length; i++) {
			sources[i] = entries.get(i).first.longValue();
			targets[i] = entries.get(i).second.longValue();
		}
	}
}
