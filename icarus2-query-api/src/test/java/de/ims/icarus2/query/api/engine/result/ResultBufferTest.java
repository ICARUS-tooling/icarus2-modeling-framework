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
/**
 *
 */
package de.ims.icarus2.query.api.engine.result;

import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.query.api.engine.ThreadVerifier;
import de.ims.icarus2.query.api.engine.result.ResultBuffer.BestN;
import de.ims.icarus2.query.api.engine.result.ResultBuffer.FirstN;
import de.ims.icarus2.query.api.engine.result.ResultBuffer.Limited;
import de.ims.icarus2.query.api.engine.result.ResultBuffer.Sorted;
import de.ims.icarus2.query.api.engine.result.ResultBuffer.Unlimited;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
class ResultBufferTest {

	static final class MatchImpl implements Match {

		private static final int[] EMPTY = {};

		private final int index;

		MatchImpl(int index) { this.index = index; }

		@Override
		public void drainTo(MatchSink sink) { sink.consume(index, 0, 0, EMPTY, EMPTY); }

		@Override
		public long getIndex() { return index; }

		@Override
		public int getMapCount() { return 0; }

		@Override
		public int getNode(int index) { throw new ArrayIndexOutOfBoundsException(); }

		@Override
		public int getIndex(int index) { throw new ArrayIndexOutOfBoundsException(); }

		@Override
		public String toString() { return String.valueOf(index); }
	}

	static final IntFunction<ResultEntry> ENTRY_GEN = i -> new ResultEntry(new MatchImpl(i), 0);

	static final Comparator<Match> MATCH_NATURAL_ORDER =
			(m1, m2) -> strictToInt(m1.getIndex()-m2.getIndex());

	static final Comparator<Match> MATCH_REVERSE_ORDER =
			(m1, m2) -> -MATCH_NATURAL_ORDER.compare(m1, m2);

	static final Comparator<ResultEntry> ENTRY_NATURAL_ORDER =
			(r1, r2) -> strictToInt(r1.getMatch().getIndex()-r2.getMatch().getIndex());

	static final Comparator<ResultEntry> ENTRY_REVERSE_ORDER =
			(r1, r2) -> -ENTRY_NATURAL_ORDER.compare(r1, r2);

	static final int DEFAULT_TIMEOUT = 20;

	static class ThreadedTest<T> {
		private ResultBuffer<T> buffer;
		private int matchCount = UNSET_INT;
		private int threadCount = UNSET_INT;
		private IntFunction<T> matchGen;
		private int timeout = UNSET_INT;
		private boolean expectLimitReached;
		private boolean shuffleInput;
		private int expectedResultSize = UNSET_INT;
		private Comparator<? super T> sorter;
		private RandomGenerator rng;

		ThreadedTest<T> rng(RandomGenerator rng) {
			checkState("random generator already set", this.rng==null);
			this.rng = requireNonNull(rng);
			return this;
		}

		ThreadedTest<T> sorter(Comparator<? super T> sorter) {
			checkState("sorter already set", this.sorter==null);
			this.sorter = requireNonNull(sorter);
			return this;
		}

		ThreadedTest<T> buffer(ResultBuffer<T> buffer) {
			checkState("buffer already set", this.buffer==null);
			this.buffer = requireNonNull(buffer);
			return this;
		}

		ThreadedTest<T> matchGen(IntFunction<T> matchGen) {
			checkState("match generator already set", this.matchGen==null);
			this.matchGen = requireNonNull(matchGen);
			return this;
		}

		ThreadedTest<T> matchCount(int matchCount) {
			checkState("match count already set", this.matchCount==UNSET_INT);
			this.matchCount = matchCount;
			return this;
		}

		ThreadedTest<T> threadCount(int threadCount) {
			checkState("thread count already set", this.threadCount==UNSET_INT);
			this.threadCount = threadCount;
			return this;
		}

		ThreadedTest<T> timeout(int timeout) {
			checkState("timeout already set", this.timeout==UNSET_INT);
			this.timeout = timeout;
			return this;
		}

		ThreadedTest<T> expectedResultSize(int expectedResultSize) {
			checkState("expected result size already set", this.expectedResultSize==UNSET_INT);
			this.expectedResultSize = expectedResultSize;
			return this;
		}

		ThreadedTest<T> expectLimitReached(boolean expectLimitReached) {
			this.expectLimitReached = expectLimitReached;
			return this;
		}

		ThreadedTest<T> shuffleInput(boolean shuffleInput) {
			this.shuffleInput = shuffleInput;
			return this;
		}

		private boolean fillSingleThreaded(List<T> matches) {
			Predicate<T> collector = buffer.createCollector(ThreadVerifier.forCurrentThread("test"));

			boolean limitReached = false;

			for (int i = 0; i < matchCount; i++) {
				limitReached |= !collector.test(matches.get(i));
			}

			return limitReached;
		}

		private boolean fillMultiThreaded(List<T> matches) throws InterruptedException {
			AtomicBoolean limitReached = new AtomicBoolean(false);
			final Iterator<T> it = matches.iterator();
			final Supplier<T> input = () -> {
				synchronized (it) {
					return it.hasNext() ? it.next() : null;
				}
			};

			CountDownLatch latch = new CountDownLatch(1);
			CyclicBarrier barrier = new CyclicBarrier(threadCount);
			ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
			for (int i = 0; i < threadCount; i++) {
				executorService.execute(() -> {
					final Predicate<T> collector = buffer.createCollector(ThreadVerifier.forCurrentThread("test"));

					try {
						barrier.await();

						// Push items till we reach a limit or run out of supplies
						T item;
						while((item=input.get()) != null) {
							if(!collector.test(item)) {
								limitReached.set(true);
								break;
							}
						}

						if(barrier.await()==0) {
							latch.countDown();
						}
					} catch (InterruptedException | BrokenBarrierException e) {
						throw new AssertionError("Synchronization failed", e);
					}
				});
			}

			assertThat(latch.await(timeout, TimeUnit.SECONDS)).as("failed regular termination").isTrue();

			return limitReached.get();
		}

		void assertCollection() throws InterruptedException {
			checkState("buffer not set", buffer!=null);
			checkState("match count not set", matchCount!=UNSET_INT);
			checkState("thread count not set", matchCount!=UNSET_INT);
			checkState("match generator not set", matchGen!=null);
			checkState("expected result size not set", expectedResultSize!=UNSET_INT);

			final List<T> matches = IntStream.range(0, matchCount)
					.mapToObj(matchGen)
					.collect(Collectors.toList());

			if(shuffleInput) {
				checkState("random generator not set", rng!=null);
				rng.shuffle(matches);
			}

			final boolean limitReached;
			if(threadCount==1) {
				limitReached = fillSingleThreaded(matches);
			} else {
				checkState("timeout not set", timeout!=UNSET_INT);
				limitReached = fillMultiThreaded(matches);
			}

			buffer.finish();

			assertThat(limitReached).as("limit reached").isEqualTo(expectLimitReached);

			assertThat(buffer.size()).as("result size mismatch").isEqualTo(expectedResultSize);

			if(sorter!=null && buffer.size()>0) {
				List<T> items = buffer.items();
				assertThat(items).isSortedAccordingTo(sorter);
			}
		}
	}

	@Nested
	class ForUnlimited {

		@ParameterizedTest(name="{0} matches")
		@ValueSource(ints = {10, 100, 1000})
		public void testSingleThreaded(int matchCount) throws Exception {
			Unlimited<Match> buffer = Unlimited.builder(Match.class)
					.initialGlobalSize(10)
					.collectorBufferSize(2)
					.build();

			new ThreadedTest<Match>()
				.buffer(buffer)
				.threadCount(1)
				.matchCount(matchCount)
				.matchGen(MatchImpl::new)
				.expectedResultSize(matchCount)
				.assertCollection();
		}

		@ParameterizedTest(name="{0} threads with {1} matches")
		@CsvSource({
			"2, 10",
			"2, 100",
			"2, 1000",
			"2, 10000",
			"5, 10",
			"5, 100",
			"5, 1000",
			"5, 10000",
			"10, 10",
			"10, 100",
			"10, 1000",
			"10, 10000",
		})
		public void testMultiThreaded(int threadCount, int matchCount) throws Exception {
			Unlimited<Match> buffer = Unlimited.builder(Match.class)
					.initialGlobalSize(10)
					.collectorBufferSize(2)
					.build();

			new ThreadedTest<Match>()
				.buffer(buffer)
				.threadCount(threadCount)
				.matchCount(matchCount)
				.matchGen(MatchImpl::new)
				.expectedResultSize(matchCount)
				.timeout(DEFAULT_TIMEOUT)
				.assertCollection();
		}
	}

	@Nested
	class ForLimited {

		@ParameterizedTest(name="{0} matches, limited to {1}")
		@CsvSource({
			"10, 10",
			"10, 5",
			"10, 100",
			"100, 100",
			"100, 25",
			"100, 1000",
			"1000, 1000",
			"1000, 100",
			"1000, 25",
		})
		public void testSingleThreaded(int matchCount, int limit) throws Exception {
			Limited<Match> buffer = Limited.builder(Match.class)
					.initialGlobalSize(10)
					.collectorBufferSize(2)
					.limit(limit)
					.build();

			new ThreadedTest<Match>()
				.buffer(buffer)
				.threadCount(1)
				.matchCount(matchCount)
				.matchGen(MatchImpl::new)
				.expectLimitReached(matchCount >= limit)
				.expectedResultSize(Math.min(matchCount, limit))
				.assertCollection();
		}

		@ParameterizedTest(name="{0} threads with {1} matches, limited to {2}")
		@CsvSource({
			"2, 10, 10",
			"2, 10, 5",
			"2, 10, 100",
			"2, 100, 100",
			"2, 100, 25",
			"2, 100, 1000",
			"2, 1000, 1000",
			"2, 1000, 100",
			"2, 10000, 1000",
			"2, 10000, 10000",
			"5, 10, 10",
			"5, 10, 5",
			"5, 100, 100",
			"5, 100, 25",
			"5, 100, 1000",
			"5, 1000, 1000",
			"5, 1000, 100",
			"5, 1000, 10000",
			"5, 10000, 10000",
			"5, 10000, 1000",
			"10, 10, 10",
			"10, 10, 5",
			"10, 10, 100",
			"10, 100, 100",
			"10, 100, 25",
			"10, 100, 1000",
			"10, 1000, 1000",
			"10, 1000, 100",
			"10, 10000, 10000",
			"10, 10000, 1000",
		})
		public void testMultiThreaded(int threadCount, int matchCount, int limit) throws Exception {
			Limited<Match> buffer = Limited.builder(Match.class)
					.initialGlobalSize(10)
					.collectorBufferSize(2)
					.limit(limit)
					.build();

			new ThreadedTest<Match>()
				.buffer(buffer)
				.threadCount(1)
				.matchCount(matchCount)
				.matchGen(MatchImpl::new)
				.timeout(DEFAULT_TIMEOUT)
				.expectLimitReached(matchCount >= limit)
				.expectedResultSize(Math.min(matchCount, limit))
				.assertCollection();
		}
	}

	@Nested
	class ForSorted {

		@ParameterizedTest(name="{0} matches")
		@ValueSource(ints = {10, 100, 1000})
		public void testSingleThreadedNaturalOrder(int matchCount) throws Exception {
			Sorted buffer = Sorted.builder()
					.initialGlobalSize(10)
					.initialTmpSize(10)
					.collectorBufferSize(2)
					.sorter(ENTRY_NATURAL_ORDER)
					.build();

			new ThreadedTest<ResultEntry>()
				.buffer(buffer)
				.threadCount(1)
				.matchCount(matchCount)
				.sorter(ENTRY_NATURAL_ORDER)
				.matchGen(ENTRY_GEN)
				.expectedResultSize(matchCount)
				.assertCollection();
		}

		@ParameterizedTest(name="{0} matches")
		@ValueSource(ints = {10, 100, 1000})
		@RandomizedTest
		public void testSingleThreadedNaturalOrderShuffled(int matchCount, RandomGenerator rng) throws Exception {
			Sorted buffer = Sorted.builder()
					.initialGlobalSize(10)
					.initialTmpSize(10)
					.collectorBufferSize(2)
					.sorter(ENTRY_NATURAL_ORDER)
					.build();

			new ThreadedTest<ResultEntry>()
				.buffer(buffer)
				.threadCount(1)
				.shuffleInput(true)
				.rng(rng)
				.matchCount(matchCount)
				.sorter(ENTRY_NATURAL_ORDER)
				.matchGen(ENTRY_GEN)
				.expectedResultSize(matchCount)
				.assertCollection();
		}

		@ParameterizedTest(name="{0} matches")
		@ValueSource(ints = {10, 100, 1000})
		public void testSingleThreadedReverseOrder(int matchCount) throws Exception {
			Sorted buffer = Sorted.builder()
					.initialGlobalSize(10)
					.initialTmpSize(10)
					.collectorBufferSize(2)
					.sorter(ENTRY_REVERSE_ORDER)
					.build();

			new ThreadedTest<ResultEntry>()
				.buffer(buffer)
				.threadCount(1)
				.matchCount(matchCount)
				.sorter(ENTRY_REVERSE_ORDER)
				.matchGen(ENTRY_GEN)
				.expectedResultSize(matchCount)
				.assertCollection();
		}

		@ParameterizedTest(name="{0} matches")
		@ValueSource(ints = {10, 100, 1000})
		@RandomizedTest
		public void testSingleThreadedReverseOrderShuffled(int matchCount, RandomGenerator rng) throws Exception {
			Sorted buffer = Sorted.builder()
					.initialGlobalSize(10)
					.initialTmpSize(10)
					.collectorBufferSize(2)
					.sorter(ENTRY_REVERSE_ORDER)
					.build();

			new ThreadedTest<ResultEntry>()
				.buffer(buffer)
				.threadCount(1)
				.shuffleInput(true)
				.rng(rng)
				.matchCount(matchCount)
				.sorter(ENTRY_REVERSE_ORDER)
				.matchGen(ENTRY_GEN)
				.expectedResultSize(matchCount)
				.assertCollection();
		}

		@ParameterizedTest(name="{0} threads with {1} matches")
		@CsvSource({
			"2, 10",
			"2, 100",
			"2, 1000",
			"2, 10000",
			"5, 10",
			"5, 100",
			"5, 1000",
			"5, 10000",
			"10, 10",
			"10, 100",
			"10, 1000",
			"10, 10000",
		})
		public void testMultiThreadedNaturalOrder(int threadCount, int matchCount) throws Exception {
			Sorted buffer = Sorted.builder()
					.initialGlobalSize(10)
					.initialTmpSize(10)
					.collectorBufferSize(2)
					.sorter(ENTRY_NATURAL_ORDER)
					.build();

			new ThreadedTest<ResultEntry>()
				.buffer(buffer)
				.threadCount(threadCount)
				.matchCount(matchCount)
				.sorter(ENTRY_NATURAL_ORDER)
				.matchGen(ENTRY_GEN)
				.expectedResultSize(matchCount)
				.timeout(DEFAULT_TIMEOUT)
				.assertCollection();
		}

		@ParameterizedTest(name="{0} threads with {1} matches")
		@CsvSource({
			"2, 10",
			"2, 100",
			"2, 1000",
			"2, 10000",
			"5, 10",
			"5, 100",
			"5, 1000",
			"5, 10000",
			"10, 10",
			"10, 100",
			"10, 1000",
			"10, 10000",
		})
		@RandomizedTest
		public void testMultiThreadedNaturalOrderShuffled(int threadCount, int matchCount, RandomGenerator rng) throws Exception {
			Sorted buffer = Sorted.builder()
					.initialGlobalSize(10)
					.initialTmpSize(10)
					.collectorBufferSize(2)
					.sorter(ENTRY_NATURAL_ORDER)
					.build();

			new ThreadedTest<ResultEntry>()
				.buffer(buffer)
				.threadCount(threadCount)
				.matchCount(matchCount)
				.shuffleInput(true)
				.rng(rng)
				.sorter(ENTRY_NATURAL_ORDER)
				.matchGen(ENTRY_GEN)
				.expectedResultSize(matchCount)
				.timeout(DEFAULT_TIMEOUT)
				.assertCollection();
		}

		@ParameterizedTest(name="{0} threads with {1} matches")
		@CsvSource({
			"2, 10",
			"2, 100",
			"2, 1000",
			"2, 10000",
			"5, 10",
			"5, 100",
			"5, 1000",
			"5, 10000",
			"10, 10",
			"10, 100",
			"10, 1000",
			"10, 10000",
		})
		public void testMultiThreadedReverseOrder(int threadCount, int matchCount) throws Exception {
			Sorted buffer = Sorted.builder()
					.initialGlobalSize(10)
					.initialTmpSize(10)
					.collectorBufferSize(2)
					.sorter(ENTRY_REVERSE_ORDER)
					.build();

			new ThreadedTest<ResultEntry>()
				.buffer(buffer)
				.threadCount(threadCount)
				.matchCount(matchCount)
				.sorter(ENTRY_REVERSE_ORDER)
				.matchGen(ENTRY_GEN)
				.expectedResultSize(matchCount)
				.timeout(DEFAULT_TIMEOUT)
				.assertCollection();
		}

		@ParameterizedTest(name="{0} threads with {1} matches")
		@CsvSource({
			"2, 10",
			"2, 100",
			"2, 1000",
			"2, 10000",
			"5, 10",
			"5, 100",
			"5, 1000",
			"5, 10000",
			"10, 10",
			"10, 100",
			"10, 1000",
			"10, 10000",
		})
		@RandomizedTest
		public void testMultiThreadedReverseOrderShuffled(int threadCount, int matchCount, RandomGenerator rng) throws Exception {
			Sorted buffer = Sorted.builder()
					.initialGlobalSize(10)
					.initialTmpSize(10)
					.collectorBufferSize(2)
					.sorter(ENTRY_REVERSE_ORDER)
					.build();

			new ThreadedTest<ResultEntry>()
				.buffer(buffer)
				.threadCount(threadCount)
				.matchCount(matchCount)
				.shuffleInput(true)
				.rng(rng)
				.sorter(ENTRY_REVERSE_ORDER)
				.matchGen(ENTRY_GEN)
				.expectedResultSize(matchCount)
				.timeout(DEFAULT_TIMEOUT)
				.assertCollection();
		}
	}

	@Nested
	class ForFirstN {

		@ParameterizedTest(name="{0} matches, limited to {1}")
		@CsvSource({
			"10, 10",
			"10, 5",
			"10, 100",
			"100, 100",
			"100, 25",
			"100, 1000",
			"1000, 1000",
			"1000, 100",
			"1000, 25",
		})
		public void testSingleThreadedUnsorted(int matchCount, int limit) throws Exception {
			FirstN buffer = FirstN.builder()
					.initialGlobalSize(10)
					.initialTmpSize(10)
					.limit(limit)
					.collectorBufferSize(2)
					.build();

			new ThreadedTest<ResultEntry>()
				.buffer(buffer)
				.threadCount(1)
				.matchCount(matchCount)
				.matchGen(ENTRY_GEN)
				.expectLimitReached(matchCount >= limit)
				.expectedResultSize(Math.min(matchCount, limit))
				.assertCollection();
		}

		@ParameterizedTest(name="{0} matches, limited to {1}")
		@CsvSource({
			"10, 10",
			"10, 5",
			"10, 100",
			"100, 100",
			"100, 25",
			"100, 1000",
			"1000, 1000",
			"1000, 100",
			"1000, 25",
		})
		public void testSingleThreadedNaturalOrder(int matchCount, int limit) throws Exception {
			FirstN buffer = FirstN.builder()
					.initialGlobalSize(10)
					.initialTmpSize(10)
					.limit(limit)
					.collectorBufferSize(2)
					.sorter(ENTRY_NATURAL_ORDER)
					.build();

			new ThreadedTest<ResultEntry>()
				.buffer(buffer)
				.threadCount(1)
				.matchCount(matchCount)
				.sorter(ENTRY_NATURAL_ORDER)
				.matchGen(ENTRY_GEN)
				.expectLimitReached(matchCount >= limit)
				.expectedResultSize(Math.min(matchCount, limit))
				.assertCollection();
		}

		@ParameterizedTest(name="{0} matches, limited to {1}")
		@CsvSource({
			"10, 10",
			"10, 5",
			"10, 100",
			"100, 100",
			"100, 25",
			"100, 1000",
			"1000, 1000",
			"1000, 100",
			"1000, 25",
		})
		@RandomizedTest
		public void testSingleThreadedNaturalOrderShuffled(int matchCount, int limit, RandomGenerator rng) throws Exception {
			FirstN buffer = FirstN.builder()
					.initialGlobalSize(10)
					.initialTmpSize(10)
					.limit(limit)
					.collectorBufferSize(2)
					.sorter(ENTRY_NATURAL_ORDER)
					.build();

			new ThreadedTest<ResultEntry>()
				.buffer(buffer)
				.threadCount(1)
				.shuffleInput(true)
				.rng(rng)
				.matchCount(matchCount)
				.sorter(ENTRY_NATURAL_ORDER)
				.matchGen(ENTRY_GEN)
				.expectLimitReached(matchCount >= limit)
				.expectedResultSize(Math.min(matchCount, limit))
				.assertCollection();
		}

		@ParameterizedTest(name="{0} matches, limited to {1}")
		@CsvSource({
			"10, 10",
			"10, 5",
			"10, 100",
			"100, 100",
			"100, 25",
			"100, 1000",
			"1000, 1000",
			"1000, 100",
			"1000, 25",
		})
		public void testSingleThreadedReverseOrder(int matchCount, int limit) throws Exception {
			FirstN buffer = FirstN.builder()
					.initialGlobalSize(10)
					.initialTmpSize(10)
					.limit(limit)
					.collectorBufferSize(2)
					.sorter(ENTRY_REVERSE_ORDER)
					.build();

			new ThreadedTest<ResultEntry>()
				.buffer(buffer)
				.threadCount(1)
				.matchCount(matchCount)
				.sorter(ENTRY_REVERSE_ORDER)
				.matchGen(ENTRY_GEN)
				.expectLimitReached(matchCount >= limit)
				.expectedResultSize(Math.min(matchCount, limit))
				.assertCollection();
		}

		@ParameterizedTest(name="{0} matches, limited to {1}")
		@CsvSource({
			"10, 10",
			"10, 5",
			"10, 100",
			"100, 100",
			"100, 25",
			"100, 1000",
			"1000, 1000",
			"1000, 100",
			"1000, 25",
		})
		@RandomizedTest
		public void testSingleThreadedReverseOrderShuffled(int matchCount, int limit, RandomGenerator rng) throws Exception {
			FirstN buffer = FirstN.builder()
					.initialGlobalSize(10)
					.initialTmpSize(10)
					.limit(limit)
					.collectorBufferSize(2)
					.sorter(ENTRY_REVERSE_ORDER)
					.build();

			new ThreadedTest<ResultEntry>()
				.buffer(buffer)
				.threadCount(1)
				.shuffleInput(true)
				.rng(rng)
				.matchCount(matchCount)
				.sorter(ENTRY_REVERSE_ORDER)
				.matchGen(ENTRY_GEN)
				.expectLimitReached(matchCount >= limit)
				.expectedResultSize(Math.min(matchCount, limit))
				.assertCollection();
		}

		@ParameterizedTest(name="{0} threads with {1} matches, limited to {2}")
		@CsvSource({
			"2, 10, 10",
			"2, 10, 5",
			"2, 10, 100",
			"2, 100, 100",
			"2, 100, 25",
			"2, 100, 1000",
			"2, 1000, 1000",
			"2, 1000, 100",
			"2, 10000, 1000",
			"2, 10000, 10000",
			"5, 10, 10",
			"5, 10, 5",
			"5, 100, 100",
			"5, 100, 25",
			"5, 100, 1000",
			"5, 1000, 1000",
			"5, 1000, 100",
			"5, 1000, 10000",
			"5, 10000, 10000",
			"5, 10000, 1000",
			"10, 10, 10",
			"10, 10, 5",
			"10, 10, 100",
			"10, 100, 100",
			"10, 100, 25",
			"10, 100, 1000",
			"10, 1000, 1000",
			"10, 1000, 100",
			"10, 10000, 10000",
			"10, 10000, 1000",
		})
		public void testMultiThreadedUnsorted(int threadCount, int matchCount, int limit) throws Exception {
			FirstN buffer = FirstN.builder()
					.initialGlobalSize(10)
					.initialTmpSize(10)
					.limit(limit)
					.collectorBufferSize(2)
					.build();

			new ThreadedTest<ResultEntry>()
				.buffer(buffer)
				.threadCount(threadCount)
				.matchCount(matchCount)
				.matchGen(ENTRY_GEN)
				.timeout(DEFAULT_TIMEOUT)
				.expectLimitReached(matchCount >= limit)
				.expectedResultSize(Math.min(matchCount, limit))
				.assertCollection();
		}

		@ParameterizedTest(name="{0} threads with {1} matches, limited to {2}")
		@CsvSource({
			"2, 10, 10",
			"2, 10, 5",
			"2, 10, 100",
			"2, 100, 100",
			"2, 100, 25",
			"2, 100, 1000",
			"2, 1000, 1000",
			"2, 1000, 100",
			"2, 10000, 1000",
			"2, 10000, 10000",
			"5, 10, 10",
			"5, 10, 5",
			"5, 100, 100",
			"5, 100, 25",
			"5, 100, 1000",
			"5, 1000, 1000",
			"5, 1000, 100",
			"5, 1000, 10000",
			"5, 10000, 10000",
			"5, 10000, 1000",
			"10, 10, 10",
			"10, 10, 5",
			"10, 10, 100",
			"10, 100, 100",
			"10, 100, 25",
			"10, 100, 1000",
			"10, 1000, 1000",
			"10, 1000, 100",
			"10, 10000, 10000",
			"10, 10000, 1000",
		})
		public void testMultiThreadedNaturalOrder(int threadCount, int matchCount, int limit) throws Exception {
			FirstN buffer = FirstN.builder()
					.initialGlobalSize(10)
					.initialTmpSize(10)
					.limit(limit)
					.collectorBufferSize(2)
					.sorter(ENTRY_NATURAL_ORDER)
					.build();

			new ThreadedTest<ResultEntry>()
				.buffer(buffer)
				.threadCount(threadCount)
				.matchCount(matchCount)
				.sorter(ENTRY_NATURAL_ORDER)
				.matchGen(ENTRY_GEN)
				.timeout(DEFAULT_TIMEOUT)
				.expectLimitReached(matchCount >= limit)
				.expectedResultSize(Math.min(matchCount, limit))
				.assertCollection();
		}

		@ParameterizedTest(name="{0} threads with {1} matches, limited to {2}")
		@CsvSource({
			"2, 10, 10",
			"2, 10, 5",
			"2, 10, 100",
			"2, 100, 100",
			"2, 100, 25",
			"2, 100, 1000",
			"2, 1000, 1000",
			"2, 1000, 100",
			"2, 10000, 1000",
			"2, 10000, 10000",
			"5, 10, 10",
			"5, 10, 5",
			"5, 100, 100",
			"5, 100, 25",
			"5, 100, 1000",
			"5, 1000, 1000",
			"5, 1000, 100",
			"5, 1000, 10000",
			"5, 10000, 10000",
			"5, 10000, 1000",
			"10, 10, 10",
			"10, 10, 5",
			"10, 10, 100",
			"10, 100, 100",
			"10, 100, 25",
			"10, 100, 1000",
			"10, 1000, 1000",
			"10, 1000, 100",
			"10, 10000, 10000",
			"10, 10000, 1000",
		})
		@RandomizedTest
		public void testMultiThreadedNaturalOrderShuffled(int threadCount, int matchCount,
				int limit, RandomGenerator rng) throws Exception {
			FirstN buffer = FirstN.builder()
					.initialGlobalSize(10)
					.initialTmpSize(10)
					.limit(limit)
					.collectorBufferSize(2)
					.sorter(ENTRY_NATURAL_ORDER)
					.build();

			new ThreadedTest<ResultEntry>()
				.buffer(buffer)
				.threadCount(threadCount)
				.matchCount(matchCount)
				.shuffleInput(true)
				.rng(rng)
				.sorter(ENTRY_NATURAL_ORDER)
				.matchGen(ENTRY_GEN)
				.timeout(DEFAULT_TIMEOUT)
				.expectLimitReached(matchCount >= limit)
				.expectedResultSize(Math.min(matchCount, limit))
				.assertCollection();
		}

		@ParameterizedTest(name="{0} threads with {1} matches, limited to {2}")
		@CsvSource({
			"2, 10, 10",
			"2, 10, 5",
			"2, 10, 100",
			"2, 100, 100",
			"2, 100, 25",
			"2, 100, 1000",
			"2, 1000, 1000",
			"2, 1000, 100",
			"2, 10000, 1000",
			"2, 10000, 10000",
			"5, 10, 10",
			"5, 10, 5",
			"5, 100, 100",
			"5, 100, 25",
			"5, 100, 1000",
			"5, 1000, 1000",
			"5, 1000, 100",
			"5, 1000, 10000",
			"5, 10000, 10000",
			"5, 10000, 1000",
			"10, 10, 10",
			"10, 10, 5",
			"10, 10, 100",
			"10, 100, 100",
			"10, 100, 25",
			"10, 100, 1000",
			"10, 1000, 1000",
			"10, 1000, 100",
			"10, 10000, 10000",
			"10, 10000, 1000",
		})
		public void testMultiThreadedReverseOrder(int threadCount, int matchCount, int limit) throws Exception {
			FirstN buffer = FirstN.builder()
					.initialGlobalSize(10)
					.initialTmpSize(10)
					.limit(limit)
					.collectorBufferSize(2)
					.sorter(ENTRY_REVERSE_ORDER)
					.build();

			new ThreadedTest<ResultEntry>()
				.buffer(buffer)
				.threadCount(threadCount)
				.matchCount(matchCount)
				.sorter(ENTRY_REVERSE_ORDER)
				.matchGen(ENTRY_GEN)
				.timeout(DEFAULT_TIMEOUT)
				.expectLimitReached(matchCount >= limit)
				.expectedResultSize(Math.min(matchCount, limit))
				.assertCollection();
		}

		@ParameterizedTest(name="{0} threads with {1} matches, limited to {2}")
		@CsvSource({
			"2, 10, 10",
			"2, 10, 5",
			"2, 10, 100",
			"2, 100, 100",
			"2, 100, 25",
			"2, 100, 1000",
			"2, 1000, 1000",
			"2, 1000, 100",
			"2, 10000, 1000",
			"2, 10000, 10000",
			"5, 10, 10",
			"5, 10, 5",
			"5, 100, 100",
			"5, 100, 25",
			"5, 100, 1000",
			"5, 1000, 1000",
			"5, 1000, 100",
			"5, 1000, 10000",
			"5, 10000, 10000",
			"5, 10000, 1000",
			"10, 10, 10",
			"10, 10, 5",
			"10, 10, 100",
			"10, 100, 100",
			"10, 100, 25",
			"10, 100, 1000",
			"10, 1000, 1000",
			"10, 1000, 100",
			"10, 10000, 10000",
			"10, 10000, 1000",
		})
		@RandomizedTest
		public void testMultiThreadedReverseOrderShuffled(int threadCount, int matchCount,
				int limit, RandomGenerator rng) throws Exception {
			FirstN buffer = FirstN.builder()
					.initialGlobalSize(10)
					.initialTmpSize(10)
					.limit(limit)
					.collectorBufferSize(2)
					.sorter(ENTRY_REVERSE_ORDER)
					.build();

			new ThreadedTest<ResultEntry>()
				.buffer(buffer)
				.threadCount(threadCount)
				.matchCount(matchCount)
				.shuffleInput(true)
				.rng(rng)
				.sorter(ENTRY_REVERSE_ORDER)
				.matchGen(ENTRY_GEN)
				.timeout(DEFAULT_TIMEOUT)
				.expectLimitReached(matchCount >= limit)
				.expectedResultSize(Math.min(matchCount, limit))
				.assertCollection();
		}

	}

	@Nested
	class ForBestN {

		@ParameterizedTest(name="{0} matches, limited to {1}")
		@CsvSource({
			"10, 10",
			"10, 5",
			"10, 100",
			"100, 100",
			"100, 25",
			"100, 1000",
			"1000, 1000",
			"1000, 100",
			"1000, 25",
		})
		public void testSingleThreadedNaturalOrder(int matchCount, int limit) throws Exception {
			BestN buffer = BestN.builder()
					.initialGlobalSize(10)
					.initialTmpSize(10)
					.limit(limit)
					.collectorBufferSize(2)
					.sorter(ENTRY_NATURAL_ORDER)
					.build();

			new ThreadedTest<ResultEntry>()
				.buffer(buffer)
				.threadCount(1)
				.matchCount(matchCount)
				.sorter(ENTRY_NATURAL_ORDER)
				.matchGen(ENTRY_GEN)
				.expectedResultSize(Math.min(matchCount, limit))
				.assertCollection();
		}

	}
}
