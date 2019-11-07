/**
 *
 */
package de.ims.icarus2.model.api.driver.mapping;

import static de.ims.icarus2.test.util.Pair.longPair;
import static de.ims.icarus2.test.util.Pair.pair;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static org.assertj.core.api.Assertions.assertThat;

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
		assertThat(range.second.longValue()).isLessThan(Long.MAX_VALUE);
		return rng.random(range.first.longValue(), range.second.longValue()+1);
	}

	private static LongStream rands(RandomGenerator rng, Pair<Long, Long> range, int size) {
		assertThat(range(range)).isGreaterThanOrEqualTo(size);
		return LongStream.generate(() -> rand(rng, range))
				.distinct()
				.limit(size);
	}

	/** Do a pairwise join of 2 long streams into a single stream containing Pair objects */
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
		long dif = range.second.longValue() - range.first.longValue() + 1;
		assertThat(dif).as("Difference must be positive").isGreaterThan(0);
		return strictToInt(dif);
	}

	public static IntStream testableSizes(IndexValueType valueType) {
		return IntStream.of(1, 100, 1000)
				.filter(count -> count<valueType.maxValue());
	}

	public static long[] extractSources(List<Pair<Long, Long>> list) {
		return list.stream()
				.mapToLong(p -> p.first.longValue())
				.toArray();
	}

	public static long[] extractTargets(List<Pair<Long, Long>> list) {
		return list.stream()
				.mapToLong(p -> p.second.longValue())
				.toArray();
	}

	public static final int TEST_LIMIT = 1000;

	public static Pair<Long, Long> randRange(RandomGenerator rng, IndexValueType valueType,
			int count, int multiplier) {
//		count = Math.min(count, TEST_LIMIT);
//		assertThat((long)count).isLessThan(valueType.maxValue());
//		long max = valueType.maxValue()-1-count;
//		assertThat(max).isGreaterThan(0L);
//		long begin = rng.random(0, max);
//		long end = begin+count;
//		assertThat(end-begin+1).isGreaterThan(count);
//		return longPair(begin, end);
		long max = Math.min(TEST_LIMIT, valueType.maxValue());
		long range = Math.min(count*multiplier, max);
		assertThat(range).isGreaterThanOrEqualTo(count);
		long end = rng.random(count-1, max);
		long begin = Math.max(0, end - range);
		assertThat(end-begin+1).isGreaterThanOrEqualTo(count);
		assertThat(end-begin+1).isLessThanOrEqualTo(TEST_LIMIT);
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
		assertThat(rs).as("Source range too small").isGreaterThanOrEqualTo(count);

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
			assertThat(rt).as("Target range too small").isGreaterThanOrEqualTo(count);
			return Stream.of(pair("random equally sized", toList(join(
					rands(rng, source, count).sorted(),
					rands(rng, target, count).sorted()))));
		}

		case TOTAL: {
			assertThat(rt).as("Target range too small").isGreaterThanOrEqualTo(count);
			return Stream.of(
					pair("random", toList(join(rands(rng, source, count), LongStream.range(0, count)))),
					pair("identity", toList(join(LongStream.range(0, count), LongStream.range(0, count))))
			);
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
