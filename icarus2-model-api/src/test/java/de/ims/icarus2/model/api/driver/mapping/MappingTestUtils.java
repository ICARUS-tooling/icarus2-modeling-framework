/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.model.api.driver.mapping;

import static de.ims.icarus2.model.api.ModelTestUtils.assertIndicesEquals;
import static de.ims.icarus2.model.api.ModelTestUtils.matcher;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.wrap;
import static de.ims.icarus2.test.util.Pair.pair;
import static de.ims.icarus2.test.util.Triple.triple;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.google.common.collect.Streams;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.test.util.Triple;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

/**
 * @author Markus Gärtner
 *
 */
public class MappingTestUtils {

//	public static class LongPair {
//		public final int first, second;
//
//		LongPair(int first, int second) {
//			this.first = first;
//			this.second = second;
//		}
//	}
//
//	public static LongPair paur(int first, int second) {
//		return new LongPair(first, second);
//	}

	/** Picks a random value inside the specified range (inclusive, exclusive) */
	private static int rand(RandomGenerator rng, Pair<Integer, Integer>  range) {
		assertThat(range.second.intValue()).isLessThan(Integer.MAX_VALUE);
		return rng.random(range.first.intValue(), range.second.intValue()+1);
	}

	private static IntStream rands(RandomGenerator rng, Pair<Integer, Integer> range, int size) {
		assertThat(range(range)).isGreaterThanOrEqualTo(size);
		return IntStream.generate(() -> rand(rng, range))
				.distinct()
				.limit(size);
	}

	/** Do a pairwise join of 2 int streams into a single stream containing Pair objects */
	private static Stream<Pair<Integer, Integer>> join(IntStream source, IntStream target) {
		return Streams.zip(source.boxed(), target.boxed(), Pair::pair);
	}

	/** Do a pairwise join of single index and span streams into triples */
	private static Stream<Triple<Integer, Integer, Integer>> join(
			IntStream source, Stream<Pair<Integer, Integer>> target) {
		return Streams.zip(source.boxed(), target, (s, p) -> triple(s, p.first, p.second));
	}

	@SuppressWarnings("boxing")
	private static Stream<Pair<Integer, Integer>> spans(RandomGenerator rng, int count,
			Pair<Integer, Integer> range, boolean shuffle, boolean total) {
		List<Pair<Integer, Integer>> tmp = new ArrayList<>();

		int min = range.first.intValue();
		int max = range.second.intValue();

		assertThat(max-min+1).as("Not enough space for %d spans", count).isGreaterThanOrEqualTo(count*2);

		int reserved = total ? 1 : 0;

		while(count-- > reserved) {
			// Keep space for remaining spans
			int limit = max - (count * 2);
			assertThat(limit).as("Out of space").isGreaterThan(min);
			int begin = total? min : rng.random(min, limit);
			int end = rng.random(begin, limit);
			tmp.add(pair(begin, end));
			min = end+1;
		}

		if(total) {
			assertThat(min).isLessThan(max);
			tmp.add(pair(min, max-1));
		}

		if(shuffle) {
			rng.shuffle(tmp);
		}
		return tmp.stream();
	}

	/**
	 * Shorthand method to {@link Stream#collect(java.util.stream.Collector)} collect content
	 * of stream into a list structure.
	 */
	private static <T> List<T> toList(Stream<T> stream) {
		return stream.collect(Collectors.toList());
	}

	/** Picks a random value between 25% and 100% of the given count */
	private static int fraction(RandomGenerator rng, int count) {
		assert count>0;
		return Math.max(1, rng.random(count>>2, count));
	}

	private static int range(Pair<Integer, Integer>  range) {
		int dif = range.second.intValue() - range.first.intValue() + 1;
		assertThat(dif).as("Difference must be positive").isGreaterThan(0);
		return strictToInt(dif);
	}

	public static IntStream testableSizes(IndexValueType valueType) {
		return IntStream.of(1, 60, 255)
				.filter(count -> count<valueType.maxValue()/2);
	}

	public static long[] extractSources(List<Pair<Integer, Integer>> list) {
		return list.stream()
				.mapToLong(p -> p.first.intValue())
				.toArray();
	}

	public static long[] extractTargets(List<Pair<Integer, Integer>> list) {
		return list.stream()
				.mapToLong(p -> p.second.intValue())
				.toArray();
	}

	public static final int TEST_LIMIT = 2000;

	public static Pair<Integer, Integer> randRange(RandomGenerator rng, IndexValueType valueType,
			int count, int multiplier) {
//		count = Math.min(count, TEST_LIMIT);
//		assertThat((int)count).isLessThan(valueType.maxValue());
//		int max = valueType.maxValue()-1-count;
//		assertThat(max).isGreaterThan(0L);
//		int begin = rng.random(0, max);
//		int end = begin+count;
//		assertThat(end-begin+1).isGreaterThan(count);
//		return longPair(begin, end);
		int max = strictToInt(Math.min(TEST_LIMIT, valueType.maxValue()));
		int range = Math.min(count*multiplier, max);
		assertThat(range).isGreaterThanOrEqualTo(count);
		int end = rng.random(count-1, max);
		int begin = Math.max(0, end - range);
		assertThat(end-begin+1).isGreaterThanOrEqualTo(count);
		assertThat(end-begin+1).isLessThanOrEqualTo(TEST_LIMIT);
		return pair(begin, end);
	}

	public static Pair<Integer, Integer> randSpanRange(RandomGenerator rng, IndexValueType valueType,
			int count, int multiplier) {
		return randRange(rng, valueType, count*2, multiplier*2);
	}

	public static List<Pair<Integer, Integer>> fixed1to1Mappings(Coverage coverage) {

		switch (coverage) {
		case PARTIAL: return list(
				pair(1, 4),
				pair(11, 2),
				pair(99, 33),
				pair(66, 4),
				pair(2, 0)
			);
		case TOTAL: return list(
				pair(0, 1),
				pair(1, 3),
				pair(3, 0),
				pair(10, 4),
				pair(12, 2)
			);
		case MONOTONIC: return list(
				pair(0, 1),
				pair(1, 3),
				pair(3, 4),
				pair(10, 10),
				pair(12, 99)
			);
		case TOTAL_MONOTONIC: return list(
				pair(0, 0),
				pair(1, 1),
				pair(3, 2),
				pair(10, 3),
				pair(12, 4)
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
	public static Stream<Pair<String,List<Pair<Integer, Integer>>>> random1to1Mappings(RandomGenerator rng,
			Coverage coverage, int count, Pair<Integer, Integer> source, Pair<Integer, Integer> target) {

		int rs = range(source);
		int rt = range(target);
		assertThat(rs).as("Source range too small").isGreaterThanOrEqualTo(count);

		int sourceBegin = source.first.intValue();
		int targetBegin = target.first.intValue();

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
					.mapToObj(offset -> pair(
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
					pair("random", toList(join(rands(rng, source, count), IntStream.range(0, count)))),
					pair("identity", toList(join(IntStream.range(0, count), IntStream.range(0, count))))
			);
		}

		default:
			throw new InternalError("Coverage type not handled: "+coverage);
		}
	}

	//TODO produce mapping sequences and verifications

	public static List<Triple<Integer, Integer, Integer>> fixed1toNSpanMappings(Coverage coverage) {

		switch (coverage) {
		case PARTIAL: return list(
				triple(1, 6, 7),
				triple(11, 2, 5),
				triple(99, 33, 66),
				triple(66, 14, 16),
				triple(2, 67, 99)
			);
		case TOTAL: return list(
				triple(0, 2, 6),
				triple(1, 12, 13),
				triple(3, 0, 1),
				triple(10, 7, 11),
				triple(12, 14, 19)
			);
		case MONOTONIC: return list(
				triple(0, 1, 2),
				triple(1, 3, 11),
				triple(3, 14, 15),
				triple(10, 18, 20),
				triple(12, 97, 99)
			);
		case TOTAL_MONOTONIC: return list(
				triple( 0,  0,  3),
				triple( 1,  4,  8),
				triple( 3,  9, 10),
				triple(10, 11, 13),
				triple(12, 14, 17)
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
	public static Stream<Pair<String,List<Triple<Integer, Integer, Integer>>>> random1toNSpanMappings(
			RandomGenerator rng, Coverage coverage, int count,
			Pair<Integer, Integer> source, Pair<Integer, Integer> target) {

		int rs = range(source);
		int rt = range(target);
		assertThat(rs).as("Source range too small").isGreaterThanOrEqualTo(count);

		switch (coverage) {

		case PARTIAL: {
			int part = fraction(rng, count);
			// (random, random)
			return Stream.of(
					pair("random full", toList(join(rands(rng, source, count), spans(rng, count, target, true, false)))),
					pair("random partial", toList(join(rands(rng, source, part), spans(rng, part, target, true, false))))
			);
		}

		case TOTAL_MONOTONIC: {
			return Stream.of(pair("full", toList(join(
					rands(rng, source, count).sorted(),
					spans(rng, count, target, false, true)
					))));
		}

		case MONOTONIC: {
			assertThat(rt).as("Target range too small").isGreaterThanOrEqualTo(count);
			return Stream.of(pair("random equally sized", toList(join(
					rands(rng, source, count).sorted(),
					spans(rng, count, target, false, false)
					))));
		}

		case TOTAL: {
			assertThat(rt).as("Target range too small").isGreaterThanOrEqualTo(count);
			return Stream.of(
					pair("random", toList(join(rands(rng, source, count),
							spans(rng, count, target, true, true))))
			);
		}

		default:
			throw new InternalError("Coverage type not handled: "+coverage);
		}
	}

	//TODO produce mapping sequences and verifications

	public static List<Triple<Integer, Integer, Integer>> fixedNto1SpanMappings(Coverage coverage) {
		return revert1ToN(fixed1toNSpanMappings(coverage));
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
	public static Stream<Pair<String,List<Triple<Integer, Integer, Integer>>>> randomNto1SpanMappings(
			RandomGenerator rng, Coverage coverage, int count,
			Pair<Integer, Integer> source, Pair<Integer, Integer> target) {
		return random1toNSpanMappings(rng, coverage, count, source, target)
				.map(pEntries -> pair(pEntries.first, revert1ToN(pEntries.second)));
	}

	public static void extractMapping(List<Pair<Integer, Integer>> entries, int[] sources, int[] targets) {
		for (int i = 0; i < sources.length; i++) {
			sources[i] = entries.get(i).first.intValue();
			targets[i] = entries.get(i).second.intValue();
		}
	}

	public static List<Triple<Integer, Integer, Integer>> revert1ToN(List<Triple<Integer, Integer, Integer>> entries) {
		return entries.stream()
				.map(t -> triple(t.second, t.third, t.first))
				.collect(Collectors.toList());
	}

	public static <M extends WritableMapping, N extends Number> void assert1toNSpanMapping(M mapping,
					List<Triple<N, N, N>> entries) throws Exception	{
			assert !entries.isEmpty() : "test data is empty";

			try(MappingWriter writer = mapping.newWriter()) {
				writer.begin();
				try {
					// write all mappings
					entries.forEach(p -> writer.map(
							p.first.intValue(), p.first.intValue(),
							p.second.intValue(), p.third.intValue()));
				} finally {
					writer.end();
				}
			}

			// DEBUG
	//		System.out.println(entries);

			try(MappingReader reader = mapping.newReader()) {
				reader.begin();
				try {
					assert1toNSpanMappings(reader, entries);
				} finally {
					reader.end();
				}
			}
		}

	static <N extends Number> void assert1toNSpanMappings(MappingReader reader, List<Triple<N, N, N>> entries) throws InterruptedException {
		// run sequential read and assert (source -> target)
		for(Triple<N, N, N> entry : entries) {
			assert1toNSpanMapping(reader, entry);
		}

		// do some real bulk lookups and searches

	}

	static <N extends Number> void assert1toNSpanMapping(MappingReader reader, Triple<N, N, N> entry) throws InterruptedException {
		RequestSettings settings = RequestSettings.none();
		int source = entry.first.intValue();
		int targetFrom = entry.second.intValue();
		int targetTo = entry.third.intValue();
		int span = strictToInt(targetTo-targetFrom+1);
		long[] values = LongStream.rangeClosed(targetFrom, targetTo).toArray();
		IndexSet indices = IndexUtils.span(targetFrom, targetTo);

		// Size queries
		assertThat(reader.getIndicesCount(source, settings))
			.as("Indices count of %s", entry).isEqualTo(span);

		// Single-value lookups
		assertThat(reader.getBeginIndex(source, settings))
			.as("Begin index of %s", entry).isEqualTo(targetFrom);
		assertThat(reader.getEndIndex(source, settings))
			.as("End index of %s", entry).isEqualTo(targetTo);

		// Bulk lookups
		IndexSet[] indices1 = reader.lookup(source, settings);
		assertThat(indices1)
			.as("Bulk lookup (single source) of %s", entry)
			.hasSize(1).doesNotContainNull();
		assertIndicesEquals(indices, indices1[0]);

		IndexSet[] indices2 = reader.lookup(wrap(source), settings);
		assertThat(indices2)
			.as("Bulk lookup of %s", entry)
			.hasSize(1).doesNotContainNull();
		assertIndicesEquals(indices, indices2[0]);

		// Collector tests
		LongList valueBuffer1 = new LongArrayList();
		assertThat(reader.lookup(source, valueBuffer1::add, settings))
			.as("Collector lookup (single source) of %s", entry).isTrue();
		assertThat(valueBuffer1.toLongArray())
			.as("Target mismatch of %s", entry)
			.hasSize(span).containsExactly(values);

		LongList valueBuffer2 = new LongArrayList();
		assertThat(reader.lookup(wrap(source), valueBuffer2::add, settings))
			.as("Collector lookup (bulk source) of %s", entry).isTrue();
		assertThat(valueBuffer2.toLongArray())
			.as("Target mismatch of %s", entry)
			.hasSize(span).containsExactly(values);

		// Bulk span boundaries
		assertThat(reader.getBeginIndex(wrap(source), settings))
			.as("Span begin (bulk source) of %s", entry).isEqualTo(targetFrom);
		assertThat(reader.getEndIndex(wrap(source), settings))
			.as("Span end (bulk source) of %s", entry).isEqualTo(targetTo);

		// Reverse lookup (explicit source range)
		assertThat(reader.find(source, source, targetFrom, settings))
			.as("Single reverse lookup (explicit, begin) of %s", entry).isEqualTo(source);
		assertThat(reader.find(source, source, wrap(targetFrom), settings))
			.as("Bulk reverse lookup (explicit, begin) of %s", entry)
			.hasSize(1).allMatch(matcher(source));
		assertThat(reader.find(source, source, targetTo, settings))
			.as("Single reverse lookup (explicit, end) of %s", entry).isEqualTo(source);
		assertThat(reader.find(source, source, wrap(targetTo), settings))
			.as("Bulk reverse lookup (explicit, end) of %s", entry)
			.hasSize(1).allMatch(matcher(source));

		// Reverse lookup (unbounded source range)
		assertThat(reader.find(Math.max(0, source-1), source, targetFrom, settings))
			.as("Single reverse lookup (open, begin) of %s", entry).isEqualTo(source);
		assertThat(reader.find(Math.max(0, source-1), source, wrap(targetFrom), settings))
			.as("Bulk reverse lookup (open, begin) of %s", entry)
			.hasSize(1).allMatch(matcher(source));
		assertThat(reader.find(Math.max(0, source-1), source, targetTo, settings))
			.as("Single reverse lookup (open, end) of %s", entry).isEqualTo(source);
		assertThat(reader.find(Math.max(0, source-1), source, wrap(targetTo), settings))
			.as("Bulk reverse lookup (open, end) of %s", entry)
			.hasSize(1).allMatch(matcher(source));

		assertThat(reader.find(source, Integer.MAX_VALUE, targetFrom, settings))
			.as("Single reverse lookup (unbounded, begin) of %s", entry).isEqualTo(source);
		assertThat(reader.find(source, Integer.MAX_VALUE, wrap(targetFrom), settings))
			.as("Bulk reverse lookup (unbounded, begin) of %s", entry)
			.hasSize(1).allMatch(matcher(source));
		assertThat(reader.find(source, Integer.MAX_VALUE, targetTo, settings))
			.as("Single reverse lookup (unbounded, end) of %s", entry).isEqualTo(source);
		assertThat(reader.find(source, Integer.MAX_VALUE, wrap(targetTo), settings))
			.as("Bulk reverse lookup (unbounded, end) of %s", entry)
			.hasSize(1).allMatch(matcher(source));
	}

	public static <M extends WritableMapping, N extends Number> void assert1to1Mapping(M mapping,
				List<Pair<N, N>> entries, RandomGenerator rng) throws Exception	{
		assert !entries.isEmpty() : "test data is empty";

		try(MappingWriter writer = mapping.newWriter()) {
			writer.begin();
			try {
				// write all mappings
				entries.forEach(p -> writer.map(p.first.intValue(), p.second.intValue()));
			} finally {
				writer.end();
			}
		}

		// DEBUG
//		System.out.println(entries);

		try(MappingReader reader = mapping.newReader()) {
			reader.begin();
			try {
				assert1to1Mappings(reader, entries);
			} finally {
				reader.end();
			}
		}
	}

	static <N extends Number> void assert1to1Mappings(MappingReader reader, List<Pair<N, N>> entries) throws InterruptedException {
		// run sequential read and assert (source -> target)
		for(Pair<N, N> entry : entries) {
			assert1to1Mapping(reader, entry);
		}

		// do some real bulk lookups and searches

	}

	static <N extends Number> void assert1to1Mapping(MappingReader reader, Pair<N, N> entry) throws InterruptedException {
		RequestSettings settings = RequestSettings.none();
		int source = entry.first.intValue();
		int target = entry.second.intValue();

		// Size queries
		assertThat(reader.getIndicesCount(source, settings))
			.as("Indices count of %s", entry).isEqualTo(1);

		// Single-value lookups
		assertThat(reader.getBeginIndex(source, settings))
			.as("Begin index of %s", entry).isEqualTo(target);
		assertThat(reader.getEndIndex(source, settings))
			.as("End index of %s", entry).isEqualTo(target);

		// Bulk lookups
		assertThat(reader.lookup(source, settings))
			.as("Bulk lookup (single source) of %s", entry)
			.hasSize(1).doesNotContainNull()
			.allMatch(set -> set.size()==1, "Size mismatch")
			.allMatch(set -> set.firstIndex()==target, "Target mismatch");
		assertThat(reader.lookup(wrap(source), settings))
			.as("Bulk lookup of %s", entry)
			.hasSize(1).doesNotContainNull()
			.allMatch(set -> set.size()==1, "Size mismatch")
			.allMatch(set -> set.firstIndex()==target, "Target mismatch");

		// Collector tests
		LongList valueBuffer1 = new LongArrayList();
		assertThat(reader.lookup(source, valueBuffer1::add, settings))
			.as("Collector lookup (single source) of %s", entry).isTrue();
		assertThat(valueBuffer1.toLongArray())
			.as("Target mismatch of %s", entry).containsExactly(target);

		LongList valueBuffer2 = new LongArrayList();
		assertThat(reader.lookup(wrap(source), valueBuffer2::add, settings))
			.as("Collector lookup (bulk source) of %s", entry).isTrue();
		assertThat(valueBuffer2.toLongArray())
			.as("Target mismatch of %s", entry).containsExactly(target);

		// Bulk span boundaries
		assertThat(reader.getBeginIndex(wrap(source), settings))
			.as("Span begin (bulk source) of %s", entry).isEqualTo(target);
		assertThat(reader.getEndIndex(wrap(source), settings))
			.as("Span end (bulk source) of %s", entry).isEqualTo(target);

		// Reverse lookup
		assertThat(reader.find(source, source, target, settings))
			.as("Single reverse lookup (explicit) of %s", entry).isEqualTo(source);
		assertThat(reader.find(source, source, wrap(target), settings))
			.as("Bulk reverse lookup (explicit) of %s", entry)
			.hasSize(1).allMatch(matcher(source));

		// Reverse lookup
		assertThat(reader.find(Math.max(0, source-1), source, target, settings))
			.as("Single reverse lookup (open begin) of %s", entry).isEqualTo(source);
		assertThat(reader.find(Math.max(0, source-1), source, wrap(target), settings))
			.as("Bulk reverse lookup (open begin) of %s", entry)
			.hasSize(1).allMatch(matcher(source));
		assertThat(reader.find(source, Integer.MAX_VALUE, target, settings))
			.as("Single reverse lookup (open end) of %s", entry).isEqualTo(source);
		assertThat(reader.find(source, Integer.MAX_VALUE, wrap(target), settings))
			.as("Bulk reverse lookup (open end) of %s", entry)
			.hasSize(1).allMatch(matcher(source));
	}

	public static <M extends WritableMapping, N extends Number> void assertNto1SpanMapping(M mapping,
				List<Triple<N, N, N>> entries) throws Exception	{
		assert !entries.isEmpty() : "test data is empty";

		writeNto1Mappings(mapping, entries);

		// DEBUG
//		System.out.println(entries);

		try(MappingReader reader = mapping.newReader()) {
			reader.begin();
			try {
				assertNto1SpanMappings(reader, entries);
			} finally {
				reader.end();
			}
		}
	}

	public static <N extends Number> void writeNto1Mapping(
			WritableMapping mapping, int sourceFrom, int sourceTo, int targetIndex) {
		// Our mapping
		try(MappingWriter writer = mapping.newWriter()) {
			writer.begin();
			try {
				writer.map(sourceFrom, sourceTo, targetIndex, targetIndex);
			} finally {
				writer.end();
			}
		}

		if(!(mapping instanceof ReverseMapping)) {
			return;
		}

		Mapping inverseMapping = ((ReverseMapping)mapping).getInverseMapping();
		assertThat(inverseMapping)
			.isNotNull()
			.isInstanceOf(WritableMapping.class);

		// Inverse mapping
		try(MappingWriter writer = ((WritableMapping)inverseMapping).newWriter()) {
			writer.begin();
			try {
				writer.map(targetIndex, targetIndex, sourceFrom, sourceTo);
			} finally {
				writer.end();
			}
		}
	}

	public static <N extends Number> void writeNto1Mappings(
			WritableMapping mapping, List<Triple<N, N, N>> entries) {
		// Our mapping
		try(MappingWriter writer = mapping.newWriter()) {
			writer.begin();
			try {
				entries.forEach(p -> writer.map(
						p.first.intValue(), p.second.intValue(),
						p.third.intValue(), p.third.intValue()));
			} finally {
				writer.end();
			}
		}

		if(!(mapping instanceof ReverseMapping)) {
			return;
		}

		Mapping inverseMapping = ((ReverseMapping)mapping).getInverseMapping();
		assertThat(inverseMapping)
			.isNotNull()
			.isInstanceOf(WritableMapping.class);

		// Inverse mapping
		try(MappingWriter writer = ((WritableMapping)inverseMapping).newWriter()) {
			writer.begin();
			try {
				entries.forEach(p -> writer.map(
						p.third.intValue(), p.third.intValue(),
						p.first.intValue(), p.second.intValue()));
			} finally {
				writer.end();
			}
		}
	}

	static <N extends Number> void assertNto1SpanMappings(MappingReader reader, List<Triple<N, N, N>> entries) throws InterruptedException {
		// run sequential read and assert (source -> target)
		for(Triple<N, N, N> entry : entries) {
			assertNto1SpanMapping(reader, entry);
		}

		// do some real bulk lookups and searches

	}

	static <N extends Number> void assertNto1SpanMapping(MappingReader reader, Triple<N, N, N> entry) throws InterruptedException {
		RequestSettings settings = RequestSettings.none();
		int sourceFrom = entry.first.intValue();
		int sourceTo = entry.second.intValue();
		int target = entry.third.intValue();

		// Size queries
		assertThat(reader.getIndicesCount(sourceFrom, settings))
			.as("Indices count of begin of %s", entry).isEqualTo(1);
		assertThat(reader.getIndicesCount(sourceTo, settings))
			.as("Indices count of end of %s", entry).isEqualTo(1);

		// Single-value lookups
		assertThat(reader.getBeginIndex(sourceFrom, settings))
			.as("Begin index of begin of %s", entry).isEqualTo(target);
		assertThat(reader.getEndIndex(sourceFrom, settings))
			.as("End index of begin of %s", entry).isEqualTo(target);
		assertThat(reader.getBeginIndex(sourceTo, settings))
			.as("Begin index of end of %s", entry).isEqualTo(target);
		assertThat(reader.getEndIndex(sourceTo, settings))
			.as("End index of end of %s", entry).isEqualTo(target);

		// Bulk lookups
		assertThat(reader.lookup(sourceFrom, settings))
			.as("Bulk lookup (single source) of begin of %s", entry)
			.hasSize(1)
			.allMatch(matcher(target));
		assertThat(reader.lookup(sourceTo, settings))
			.as("Bulk lookup (single source) of end of %s", entry)
			.hasSize(1)
			.allMatch(matcher(target));

		// Collector tests
		List<Long> valueBuffer1 = new LongArrayList();
		assertThat(reader.lookup(sourceFrom, valueBuffer1::add, settings))
			.as("Collector lookup (single source) of begin of %s", entry).isTrue();
		assertThat(valueBuffer1)
			.as("Target mismatch of begin of %s", entry)
			.hasSize(1).containsExactly(Long.valueOf(target));

		List<Long> valueBuffer2 = new LongArrayList();
		assertThat(reader.lookup(sourceTo, valueBuffer2::add, settings))
			.as("Collector lookup (single source) of end of %s", entry).isTrue();
		assertThat(valueBuffer2)
			.as("Target mismatch of end of %s", entry)
			.hasSize(1).containsExactly(Long.valueOf(target));

		List<Long> valueBuffer3 = new LongArrayList();
		assertThat(reader.lookup(wrap(sourceFrom), valueBuffer3::add, settings))
			.as("Collector lookup (bulk source) of begin of %s", entry).isTrue();
		assertThat(valueBuffer3)
			.as("Target mismatch of begin of %s", entry)
			.hasSize(1).containsExactly(Long.valueOf(target));

		List<Long> valueBuffer4 = new LongArrayList();
		assertThat(reader.lookup(wrap(sourceTo), valueBuffer4::add, settings))
			.as("Collector lookup (bulk source) of end of %s", entry).isTrue();
		assertThat(valueBuffer4)
			.as("Target mismatch of end of %s", entry)
			.hasSize(1).containsExactly(Long.valueOf(target));

		// Bulk span boundaries
		assertThat(reader.getBeginIndex(wrap(sourceFrom), settings))
			.as("Span begin (bulk source) of begin of %s", entry).isEqualTo(target);
		assertThat(reader.getEndIndex(wrap(sourceFrom), settings))
			.as("Span end (bulk source) of begin of %s", entry).isEqualTo(target);

		assertThat(reader.getBeginIndex(wrap(sourceTo), settings))
			.as("Span begin (bulk source) of end of %s", entry).isEqualTo(target);
		assertThat(reader.getEndIndex(wrap(sourceTo), settings))
			.as("Span end (bulk source) of end of %s", entry).isEqualTo(target);
	}
}
