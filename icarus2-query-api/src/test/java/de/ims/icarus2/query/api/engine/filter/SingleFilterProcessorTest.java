/**
 *
 */
package de.ims.icarus2.query.api.engine.filter;

import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.model.api.ModelTestUtils.stubId;
import static de.ims.icarus2.model.api.ModelTestUtils.stubIndex;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.query.api.iql.IqlStream;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.ArrayFormat;
import de.ims.icarus2.test.annotations.IntArrayArg;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.ChangeableTest;
import de.ims.icarus2.util.collections.ArrayUtils;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.lang.Primitives;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
class SingleFilterProcessorTest implements ChangeableTest<SingleFilterProcessor> {

	@Override
	public SingleFilterProcessor createTestInstance(TestSettings settings) {
		return settings.process(SingleFilterProcessor.builder()
				.stream(mock(IqlStream.class))
				.candidateLookup(mock(LongFunction.class))
				.context(mock(Context.class))
				.query(mock(IqlQuery.class))
				.filter(mock(QueryFilter.class))
				.executor(mock(ExecutorService.class))
				.build());
	}

	@Override
	public void invokeChange(SingleFilterProcessor instance) {
		instance.fireStateChanged();
	}

	@Nested
	class ForInstance {
		private SingleFilterProcessor instance;
		private ExecutorService readerExec, writerExec;
		private List<Container> items;

		@BeforeEach
		void setUp() {
			readerExec = Executors.newFixedThreadPool(10);
			writerExec = Executors.newFixedThreadPool(1);
		}

		@AfterEach
		void tearDown() {
			assertThat(readerExec.shutdownNow()).as("pending reader tasks").isEmpty();
			assertThat(writerExec.shutdownNow()).as("pending writer task").isEmpty();
			readerExec = null;
			writerExec = null;
		}

		void initItems(int size) {
			items = IntStream.rangeClosed(0, size)
					.mapToObj(i -> stubId(stubIndex(mockContainer(), i), i))
					.collect(Collectors.toList());
		}

		void initProc(int capacity, QueryFilter filter) {
			instance = SingleFilterProcessor.builder()
					.candidateLookup(index -> items.get(strictToInt(index)))
					.capacity(capacity)
					.context(mock(Context.class))
					.stream(mock(IqlStream.class))
					.query(mock(IqlQuery.class))
					.executor(writerExec)
					.filter(filter)
					.build();
		}

		@ParameterizedTest
		@CsvSource({
			"3, 3, '{1, 2, 3, 4, 5}'",
			"4, 2, '{1, 2, 3, 4, 5}'",
			"2, 4, '{1, 2, 3, 4, 5}'",
			"5, 5, '{1, 2, 3, 4, 5}'",

			"3, 3, '{2, 1, 4, 3, 5}'",
			"4, 2, '{2, 1, 4, 3, 5}'",
			"2, 4, '{2, 1, 4, 3, 5}'",
			"5, 5, '{2, 1, 4, 3, 5}'",

			"3, 3, '{1, 2, 3, 4, 5, 6, 7, 8, 9}'",
			"2, 4, '{1, 2, 3, 4, 5, 6, 7, 8, 9}'",
			"4, 2, '{1, 2, 3, 4, 5, 6, 7, 8, 9}'",
			"5, 5, '{1, 2, 3, 4, 5, 6, 7, 8, 9}'",

			"3, 3, '{8, 2, 6, 4, 3, 9, 7, 1, 5}'",
			"2, 4, '{8, 2, 6, 4, 3, 9, 7, 1, 5}'",
			"4, 2, '{8, 2, 6, 4, 3, 9, 7, 1, 5}'",
			"5, 5, '{8, 2, 6, 4, 3, 9, 7, 1, 5}'",
			"8, 8, '{8, 2, 6, 4, 3, 9, 7, 1, 5}'",
		})
		void testSequentialWrite(int queueCapacity, int readerCapacity, @IntArrayArg @ArrayFormat(delimiter=",") int[] values)
				throws QueryException, IcarusApiException, InterruptedException {
			initItems(10);
			QueryFilter filter = mock(QueryFilter.class);

			CountDownLatch end = new CountDownLatch(1);

			doAnswer(invoc -> {
				FilterContext ctx = invoc.getArgument(0);
				CandidateSink sink = ctx.getSink();
				sink.prepare();
				for(long value : values) {
					sink.add(value);
				}
				sink.finish();
				return null;
			}).when(filter).filter(any());

			initProc(queueCapacity, filter);

			final List<Container> tmp = new ObjectArrayList<>();
			final AtomicBoolean interrupted = new AtomicBoolean(false);

			readerExec.execute(() -> {
				Container[] buffer = new Container[readerCapacity];
				try {
					int len;
					while((len = instance.load(buffer)) > 0) {
						CollectionUtils.feedItems(tmp, buffer, 0, len);
					}
				} catch (InterruptedException e) {
					interrupted.set(true);
				} finally {
					end.countDown();
				}
			});

			assertThat(end.await(1000, TimeUnit.MILLISECONDS)).as("timeout while waiting for end of process").isTrue();

			assertThat(interrupted).as("interruption of loading process").isFalse();

			assertThat(tmp).hasSize(values.length);
			for (int i = 0; i < values.length; i++) {
				assertThat(tmp.get(i))
					.as("Loaded container mismatch at index %d", _int(i))
					.isSameAs(items.get(values[i]));
			}
		}

		@ParameterizedTest
		@RandomizedTest
		@CsvSource({
			"3, 3, 4, 10",
			"4, 2, 4, 10",
			"2, 4, 4, 10",
			"5, 5, 4, 10",

			"3, 3, 5, 20",
			"4, 2, 5, 20",
			"2, 4, 5, 20",
			"5, 5, 5, 20",

			"3, 3, 4, 100",
			"2, 4, 4, 100",
			"4, 2, 4, 100",
			"5, 5, 4, 100",

			"3, 3, 15, 100",
			"2, 4, 15, 100",
			"4, 2, 15, 100",
			"5, 5, 15, 100",
			"8, 8, 15, 100",
		})
		void testBulkWrite(int queueCapacity, int readerCapacity, int writerCapacity, int size, RandomGenerator rng)
				throws QueryException, IcarusApiException, InterruptedException {
			initItems(size);
			QueryFilter filter = mock(QueryFilter.class);

			CountDownLatch end = new CountDownLatch(1);

			long[] values = new long[size];
			ArrayUtils.fillAscending(values);
			rng.shuffle(values);

			doAnswer(invoc -> {
				FilterContext ctx = invoc.getArgument(0);
				CandidateSink sink = ctx.getSink();
				sink.prepare();
				int offset = 0;
				while(offset<values.length) {
					int len = Math.min(rng.nextInt(writerCapacity)+1, values.length-offset);
					sink.add(values, offset, len);
					offset += len;
				}
				sink.finish();
				return null;
			}).when(filter).filter(any());

			initProc(queueCapacity, filter);

			final List<Container> tmp = new ObjectArrayList<>();
			final AtomicBoolean interrupted = new AtomicBoolean(false);

			readerExec.execute(() -> {
				Container[] buffer = new Container[readerCapacity];
				try {
					int len;
					while((len = instance.load(buffer)) > 0) {
						CollectionUtils.feedItems(tmp, buffer, 0, len);
					}
				} catch (InterruptedException e) {
					interrupted.set(true);
				} finally {
					end.countDown();
				}
			});

			assertThat(end.await(1000, TimeUnit.MILLISECONDS)).as("timeout while waiting for end of process").isTrue();

			assertThat(interrupted).as("interruption of loading process").isFalse();

			assertThat(tmp).hasSize(values.length);
			for (int i = 0; i < values.length; i++) {
				assertThat(tmp.get(i))
					.as("Loaded container mismatch at index %d", _int(i))
					.isSameAs(items.get(strictToInt(values[i])));
			}
		}

		@ParameterizedTest
		@RandomizedTest
		@CsvSource({
			"3, 3, 3, 2, 10",
			"3, 3, 3, 3, 10",
			"10, 5, 5, 3, 50",
			"10, 5, 5, 5, 50",
			"10, 5, 5, 8, 50",
			"10, 5, 5, 12, 50", // uses all threads in executor!
		})
		void testConcurrentRead(int queueCapacity, int readerCapacity, int writerCapacity, int readerCount,
				int size, RandomGenerator rng)
				throws QueryException, IcarusApiException, InterruptedException {

			initItems(size);
			QueryFilter filter = mock(QueryFilter.class);

			CountDownLatch start = new CountDownLatch(1);
			CountDownLatch end = new CountDownLatch(readerCount);

			long[] values = new long[size];
			ArrayUtils.fillAscending(values);
			rng.shuffle(values);

			doAnswer(invoc -> {
				FilterContext ctx = invoc.getArgument(0);
				CandidateSink sink = ctx.getSink();
				sink.prepare();
				int offset = 0;
				while(offset<values.length) {
					int len = Math.min(rng.nextInt(writerCapacity)+1, values.length-offset);
					sink.add(values, offset, len);
					offset += len;
				}
				sink.finish();
				return null;
			}).when(filter).filter(any());

			initProc(queueCapacity, filter);

			final List<Container> tmp = Collections.synchronizedList(new ObjectArrayList<>());
			final AtomicBoolean interrupted = new AtomicBoolean(false);

			for (int i = 0; i < readerCount; i++) {
				readerExec.execute(() -> {
					List<Container> tmp0 = new ObjectArrayList<>();
					Container[] buffer = new Container[readerCapacity];
					try {
						assertThat(start.await(1000, TimeUnit.MILLISECONDS)).isTrue();
						int len;
						while((len = instance.load(buffer)) > 0) {
							CollectionUtils.feedItems(tmp0, buffer, 0, len);
						}
						tmp.addAll(tmp0);
					} catch (InterruptedException e) {
						interrupted.set(true);
					} finally {
						end.countDown();
					}
				});
			}

			start.countDown();

			assertThat(end.await(1000, TimeUnit.MILLISECONDS)).as("timeout while waiting for end of process").isTrue();

			assertThat(interrupted).as("interruption of loading process").isFalse();

			assertThat(tmp).hasSize(values.length);
			assertThat(tmp).containsExactlyInAnyOrder(LongStream.of(values)
					.mapToInt(Primitives::strictToInt)
					.mapToObj(items::get)
					.toArray(Container[]::new));
		}

		//TODO add tests for concurrent read/write operations
	}
}
