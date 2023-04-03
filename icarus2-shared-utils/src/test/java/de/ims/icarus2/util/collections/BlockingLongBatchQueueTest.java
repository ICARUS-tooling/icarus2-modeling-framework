/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.collections;

import static de.ims.icarus2.util.UtilAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.LongStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.test.annotations.ArrayFormat;
import de.ims.icarus2.test.annotations.LongArrayArg;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.AccumulatingException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
class BlockingLongBatchQueueTest {

	private static final Comparator<long[]> ARRAY_COMP = (a1, a2) -> Long.compare(a1[0], a2[0]);

	@Nested
	class ForConstructor {

		@Test
		void testNegativeCapacity() {
			assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new BlockingLongBatchQueue(-1));
		}

		@Test
		void testNegativeCapacity2() {
			assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new BlockingLongBatchQueue(-1, true));
		}
	}

	@Nested
	class ForInstance {

		private BlockingLongBatchQueue instance;

		@AfterEach
		void tearDown() throws InterruptedException {
			instance.close();
			instance = null;
		}

		@Nested
		class Prefilled {

			private final int CAPACITY = 4;

			@BeforeEach
			void setUp() {
				instance = new BlockingLongBatchQueue(CAPACITY);
			}

			@Nested
			class WithoutBlocking {

				@ParameterizedTest
				@ValueSource(longs = {1, 11, 999_999_999, Long.MIN_VALUE, Long.MAX_VALUE})
				void testWriteSingle(long value) throws InterruptedException {
					instance.write(value);
					assertThat(instance)
						.hasCount(1)
						.hasPutIndex(1)
						.hasTakeIndex(0)
						.itemsStartWith(value);
				}

				@ParameterizedTest
				@CsvSource({
					"'{1, 2}'",
					"'{1, 2, 3}'",
					"'{3, 2, 1, 0}'",
				})
				void testWriteSequence(@LongArrayArg @ArrayFormat(delimiter=",") long[] values) throws InterruptedException {
					for(long value : values) {
						instance.write(value);
					}
					assertThat(instance)
						.hasCount(values.length)
						.hasPutIndex(values.length%CAPACITY)
						.hasTakeIndex(0)
						.itemsStartWith(values);
				}

				@ParameterizedTest
				@CsvSource({
					"'{1, 2}'",
					"'{1, 2, 3}'",
					"'{3, 2, 1, 0}'",
				})
				void testWriteBatch(@LongArrayArg @ArrayFormat(delimiter=",") long[] values) throws InterruptedException {
					instance.write(values);
					assertThat(instance)
						.hasCount(values.length)
						.hasPutIndex(values.length%CAPACITY)
						.hasTakeIndex(0)
						.itemsStartWith(values);
				}

				@ParameterizedTest
				@ValueSource(longs = {2, 22, 99_999_999, Long.MIN_VALUE, Long.MAX_VALUE})
				void testWriteReadSingle(long value) throws InterruptedException {
					instance.write(value);
					assertThat(instance)
						.hasCount(1)
						.hasPutIndex(1)
						.hasTakeIndex(0)
						.itemsStartWith(value);

					assertThat(instance.read()).isEqualTo(value);
					assertThat(instance)
						.hasCount(0)
						.hasPutIndex(1)
						.hasTakeIndex(1)
						.itemsStartWith(value);
				}

				@ParameterizedTest
				@CsvSource({
					"'{1, 2}'",
					"'{1, 2, 3}'",
					"'{3, 2, 1, 0}'",
				})
				void testReadBatch(@LongArrayArg @ArrayFormat(delimiter=",") long[] values) throws InterruptedException {
					instance.write(values);
					long[] buffer = new long[values.length];
					assertThat(instance.read(buffer)).isEqualTo(values.length);
					assertThat(instance)
						.hasCount(0)
						.hasPutIndex(values.length%CAPACITY)
						.hasTakeIndex(values.length%CAPACITY);
				}

				@ParameterizedTest
				@CsvSource({
					"'{1, 2}'",
					"'{1, 2, 3}'",
					"'{3, 2, 1, 0}'",
				})
				void testRepeatedWriteSingle(@LongArrayArg @ArrayFormat(delimiter=",") long[] values) throws InterruptedException {
					for (int i = 0; i < values.length; i++) {
						long value = values[i];
						instance.write(value);
						int next = (i+1) % CAPACITY;
					assertThat(instance)
						.hasCount(i+1)
						.hasPutIndex(next)
						.hasTakeIndex(0)
						.itemsStartWith(Arrays.copyOfRange(values, 0, i+1));
					}
				}

				@Test
				void testChunkedWrite() throws InterruptedException {

					// Move cursors towards end of internal buffer array
					for (int i = 0; i < 2; i++) {
						instance.write(1);
						instance.read();
					}
					assertThat(instance)
						.hasCount(0)
						.hasPutIndex(2)
						.hasTakeIndex(2)
						.hasItems(1, 1, 0, 0);

					instance.write(new long[] {2, 3, 4});
					assertThat(instance)
						.hasCount(3)
						.hasPutIndex(1)
						.hasTakeIndex(2)
						.hasItems(4, 1, 2, 3);
				}

				@Test
				void testChunkedRead() throws InterruptedException {

					// Move cursors towards end of internal buffer array
					for (int i = 0; i < 2; i++) {
						instance.write(1);
						instance.read();
					}
					assertThat(instance)
						.hasCount(0)
						.hasPutIndex(2)
						.hasTakeIndex(2)
						.hasItems(1, 1, 0, 0);

					instance.write(new long[] {2, 3, 4, 5});
					assertThat(instance)
						.hasCount(4)
						.hasPutIndex(2)
						.hasTakeIndex(2)
						.hasItems(4, 5, 2, 3);

					long[] buffer = new long[3];
					assertThat(instance.read(buffer)).isEqualTo(buffer.length);
					assertThat(instance)
						.hasCount(1)
						.hasPutIndex(2)
						.hasTakeIndex(1)
						.hasItems(4, 5, 2, 3);
				}
			}

		}

		@Nested
		class WithBlocking {

			private ExecutorService exec;

			@AfterEach
			void tearDown() throws InterruptedException {
				assertThat(exec.shutdownNow()).as("unexecuted tasks").isEmpty();
				assertThat(exec.awaitTermination(1000, TimeUnit.MILLISECONDS)).as("shutdown timeout elapsed").isTrue();
			}

			private void prepQueue(int capacity) {
				instance = new BlockingLongBatchQueue(capacity);
			}

			private void prepExec(int threadCount) {
				exec = Executors.newFixedThreadPool(threadCount);
			}

			@Test
			void testConcurrentWrite() throws InterruptedException {
				prepExec(2);
				prepQueue(4);

				final long[] source = {3, 5, 4};
				final long[] target = new long[source.length];
				final CountDownLatch latch = new CountDownLatch(2);

				Runnable writeTask = () -> {
					try {
						instance.write(source);
					} catch (InterruptedException e) {
						throw new AssertionError("Unexpected interruption during writing", e);
					}
					latch.countDown();
				};

				Runnable readTask = () -> {
					try {
						assertThat(instance.read(target)).as("number of read values").isEqualTo(source.length);
					} catch (InterruptedException e) {
						throw new AssertionError("Unexpected interruption during reading", e);
					}
					latch.countDown();
				};

				exec.execute(writeTask);
				exec.execute(readTask);

				assertThat(latch.await(1000, TimeUnit.MILLISECONDS)).as("waiting").isTrue();

				assertThat(target).as("read values").isEqualTo(source);

			}

			@ParameterizedTest
			@RandomizedTest
			@CsvSource({
				// reader_count, capacity, read_buffer_size, writer_count, max_batch_write, count

				// Single writer, multiple readers
				"2, 4, 2, 1, 2, 10",
				"2, 4, 2, 1, 2, 100",
				"2, 4, 2, 1, 10, 1000",
				"4, 4, 2, 1, 2, 100",
				"4, 4, 2, 1, 10, 1000",
				"4, 4, 2, 1, 4, 1000",
				"2, 8, 4, 1, 6, 100",
				"2, 8, 4, 1, 10, 1000",
				"4, 20, 4, 1, 40, 1000",
				"4, 20, 4, 1, 40, 10000",

				// 2 writers, multiple readers
				"2, 4, 2, 2, 2, 10",
				"2, 4, 2, 2, 2, 100",
				"2, 4, 2, 2, 2, 1000",
				"4, 4, 2, 2, 2, 100",
				"4, 4, 2, 2, 2, 1000",
				"4, 4, 2, 2, 4, 1000",
				"2, 8, 4, 2, 6, 100",
				"2, 8, 4, 2, 6, 1000",
				"4, 20, 4, 2, 40, 1000",
				"4, 20, 4, 2, 40, 10000",

				// multiple writers, multiple readers
				"2, 4, 2, 4, 3, 10",
				"2, 4, 2, 4, 3, 100",
				"2, 4, 2, 4, 3, 1000",
				"4, 20, 4, 10, 5, 100",
				"4, 20, 4, 10, 5, 1000",
				"5, 100, 20, 10, 8, 100",
				"5, 100, 20, 10, 8, 1000",
				"10, 100, 20, 20, 10, 10000",
			})
			void testReadMultiple(int readerCount, int capacity, int readBufferSize,
					int writerCount, int maxBatchSize, int itemsPerWriter, RandomGenerator rng) throws InterruptedException, AccumulatingException {
				prepExec(readerCount+1);
				prepQueue(capacity);

				final CountDownLatch start = new CountDownLatch(1);
				final CountDownLatch end = new CountDownLatch(readerCount + writerCount);
				final AtomicInteger flush = new AtomicInteger(writerCount);
				final List<long[]> merge = Collections.synchronizedList(new ObjectArrayList<>());
				final List<Throwable> errors = Collections.synchronizedList(new ObjectArrayList<>());

				for (int i = 0; i < readerCount; i++) {
					exec.execute(new Runnable() {
						// can only contain non-empty array fragments
						private final List<long[]> tmp = new ObjectArrayList<>();

						@Override
						public void run() {
							try {
								long[] buffer = new long[readBufferSize];
								start.await();

								@SuppressWarnings("unused")
								int read = 0;
								int len;
								while((len = instance.read(buffer))>0) {
									long[] data = Arrays.copyOf(buffer, len);
//									System.out.println("reading "+Arrays.toString(data));
									tmp.add(data);
									read += data.length;
								}

//								System.out.printf("read %d elements on thread %s%n", _int(read), Thread.currentThread().getName());

								if(writerCount==1) {
									assertThat(tmp).isSortedAccordingTo(ARRAY_COMP);
								}
							} catch (InterruptedException e) {
								errors.add(e);
								e.printStackTrace();
							} catch(AssertionError e) {
								errors.add(e);
								e.printStackTrace();
							} finally {
								merge.addAll(tmp);
								end.countDown();
							}
						}
					});
				}

				for (int i = 0; i < writerCount; i++) {

					long begin = i * itemsPerWriter;
					exec.execute(() -> {
						try {
							start.await();
							long[] buffer = new long[maxBatchSize];
							int cursor = 0;
							int len = rng.nextInt(maxBatchSize)+1;
							int written = 0;
							for (int j = 0; j < itemsPerWriter; j++) {
								buffer[cursor++] = begin + j;
								if(cursor>=len || j==itemsPerWriter-1) {
									long[] data = Arrays.copyOf(buffer, cursor);
//									System.out.println("writing "+Arrays.toString(data));
									instance.write(data);
									written += data.length;
									len = rng.nextInt(maxBatchSize)+1;
									cursor = 0;
								}
							}
							assertThat(written).as("written items").isEqualTo(itemsPerWriter);
//							System.out.printf("written %d elements on thread %s%n", _int(written), Thread.currentThread().getName());

							if(flush.decrementAndGet()==0) {
								instance.close();
							}
						} catch (InterruptedException e) {
							errors.add(e);
							e.printStackTrace();
						} finally {
							end.countDown();
						}
					});
				}

				start.countDown();

				assertThat(end.await(3000, TimeUnit.MILLISECONDS)).as("await thread finish").isTrue();

				if(!errors.isEmpty())
					throw AccumulatingException.buffer()
						.setMessage("Side threads encountered errors - check the logs or System.out output.")
						.addExceptions(errors)
						.toException();

				assertThat(merge).isNotEmpty();

				long[] result = merge.stream()
						.flatMapToLong(LongStream::of)
						.sorted()
						.toArray();

				assertThat(result).as("queue output")
					.hasSize(itemsPerWriter * writerCount)
					.startsWith(0)
					.endsWith((itemsPerWriter * writerCount) - 1);
			}
		}
	}
}
