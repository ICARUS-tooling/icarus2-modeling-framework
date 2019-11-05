/**
 *
 */
package de.ims.icarus2.filedriver.mapping;

import static de.ims.icarus2.test.util.Pair.longPair;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives._long;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.ThrowingConsumer;

import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.util.Pair;

/**
 * @author Markus Gärtner
 *
 */
class IndexBlockStorageTest {

	static Stream<DynamicNode> baseTests(ThrowingConsumer<IndexBlockStorage> task) {
		return Stream.of(IndexBlockStorage.values()).map(storage -> dynamicTest(storage.name(),
				() -> task.accept(storage)));
	}

	static Object array(IndexBlockStorage storage, int size) {
		return storage.createBuffer(storage.getValueType().bytesPerValue() * size);
	}

	static ByteBuffer buffer(IndexBlockStorage storage, int size) {
		return ByteBuffer.allocate(storage.getValueType().bytesPerValue() * size);
	}

	static long rand(RandomGenerator rng, IndexBlockStorage storage) {
		return rng.random(0, storage.maxValue());
	}

	/** Creates random size in [5, 100) */
	static int randSize(RandomGenerator rng) {
		return rng.random(5, 100);
	}

	static long[] randData(RandomGenerator rng, IndexBlockStorage storage, int size) {
		return rng.randomLongs(size, 0L, storage.maxValue());
	}

	static Pair<Long, Long> randSpan(RandomGenerator rng, IndexBlockStorage storage) {
		return longPair(rand(rng, storage), rand(rng, storage));
	}

	static Pair<Long, Long>[] randSpans(RandomGenerator rng, IndexBlockStorage storage, int size) {
		return Stream.generate(() -> randSpan(rng, storage))
				.limit(size)
				.toArray(Pair[]::new);
	}

	static final Pair<Long, Long> NO_SPAN = null;

	@SafeVarargs
	static void writeSpans(IndexBlockStorage storage, Object array, Pair<Long, Long>...spans) {
		for (int i = 0; i < spans.length; i++) {
			if(spans[i]!=NO_SPAN) {
				storage.setSpanBegin(array, i, spans[i].first.longValue());
				storage.setSpanEnd(array, i, spans[i].second.longValue());
			}
		}
	}

	@Nested
	class ForFactory {

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#forValueType(de.ims.icarus2.model.api.driver.indices.IndexValueType)}.
		 */
		@TestFactory
		Stream<DynamicNode> testForValueType() {
			return baseTests(storage -> assertSame(storage,
					IndexBlockStorage.forValueType(storage.getValueType())));
		}
	}

	@Nested
	class ForProperties {

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#getValueType()}.
		 */
		@TestFactory
		Stream<DynamicNode> testGetValueType() {
			return baseTests(storage -> assertNotNull(storage.getValueType()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#spanSize()}.
		 */
		@TestFactory
		Stream<DynamicNode> testSpanSize() {
			return baseTests(storage -> assertThat(storage.spanSize()).isGreaterThan(0));
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#createBuffer(int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testCreateBuffer(RandomGenerator rng) {
			return baseTests(storage -> {
				int size = rng.random(10, 100);
				Object array = array(storage, size);

				assertThat(Array.getLength(array)).isEqualTo(size);
				for (int i = 0; i < size; i++) {
					assertThat(storage.getEntry(array, i)).isEqualTo(-1L);
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#entrySize()}.
		 */
		@TestFactory
		Stream<DynamicNode> testEntrySize() {
			return baseTests(storage -> assertThat(storage.entrySize())
					.isEqualTo(storage.getValueType().bytesPerValue()));
		}

	}

	@Nested
	class ReadWriteMethods {

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#read(java.lang.Object, java.nio.ByteBuffer, int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testReadSingle(RandomGenerator rng) {
			return baseTests(storage -> {
				Object array = array(storage, 1);
				long expected = rand(rng, storage);
				ByteBuffer bb = ByteBuffer.allocate(100);
				// Write data
				storage.getValueType().set(bb, expected);
				bb.flip();
				// Verify
				storage.read(array, bb, 0, 1);
				assertThat(storage.getValueType().get(array, 0)).isEqualTo(expected);
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#read(java.lang.Object, java.nio.ByteBuffer, int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testReadNoOffset(RandomGenerator rng) {
			return baseTests(storage -> {
				int size = randSize(rng);
				Object array = array(storage, size);
				long[] data = randData(rng, storage, size);
				ByteBuffer bb = buffer(storage, size);
				// Write data
				for(long value : data)
					storage.getValueType().set(bb, value);
				bb.flip();
				// Verify
				storage.read(array, bb, 0, size);
				for (int i = 0; i < data.length; i++) {
					long expected = data[i];
					assertThat(storage.getValueType().get(array, i))
						.as("Mismatch at index %d", _int(i)).isEqualTo(expected);
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#read(java.lang.Object, java.nio.ByteBuffer, int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testReadWithOffset(RandomGenerator rng) {
			return baseTests(storage -> {
				int size = randSize(rng);
				int offset = rng.nextInt(size);
				Object array = array(storage, size*2);
				long[] data = randData(rng, storage, size);
				ByteBuffer bb = buffer(storage, size);
				// Write data
				for(long value : data)
					storage.getValueType().set(bb, value);
				bb.flip();
				// Verify
				storage.read(array, bb, offset, size);
				for (int i = 0; i < data.length; i++) {
					long expected = data[i];
					assertThat(storage.getValueType().get(array, offset+i))
						.as("Mismatch at index %d", _int(i)).isEqualTo(expected);
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#write(java.lang.Object, java.nio.ByteBuffer, int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testWriteSingle(RandomGenerator rng) {
			return baseTests(storage -> {
				Object array = array(storage, 1);
				long expected = rand(rng, storage);
				ByteBuffer bb = ByteBuffer.allocate(100);
				// Write data
				storage.getValueType().set(array, 0, expected);
				// Verify
				storage.write(array, bb, 0, 1);
				bb.flip();
				assertThat(storage.getValueType().get(bb)).isEqualTo(expected);
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#write(java.lang.Object, java.nio.ByteBuffer, int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testWriteNoOffset(RandomGenerator rng) {
			return baseTests(storage -> {
				int size = randSize(rng);
				Object array = array(storage, size);
				long[] data = randData(rng, storage, size);
				ByteBuffer bb = buffer(storage, size);
				// Write data
				for(int i = 0; i < data.length; i++)
					storage.getValueType().set(array, i, data[i]);
				// Verify
				storage.write(array, bb, 0, size);
				bb.flip();
				for (int i = 0; i < data.length; i++) {
					long expected = data[i];
					assertThat(storage.getValueType().get(bb))
						.as("Mismatch at index %d", _int(i)).isEqualTo(expected);
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#write(java.lang.Object, java.nio.ByteBuffer, int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testWriteWithOffset(RandomGenerator rng) {
			return baseTests(storage -> {
				int size = randSize(rng);
				int offset = rng.nextInt(size);
				Object array = array(storage, size*2);
				long[] data = randData(rng, storage, size);
				ByteBuffer bb = buffer(storage, size);
				// Write data
				for(int i = 0; i < data.length; i++)
					storage.getValueType().set(array, offset+i, data[i]);
				// Verify
				storage.write(array, bb, offset, size);
				bb.flip();
				for (int i = 0; i < data.length; i++) {
					long expected = data[i];
					assertThat(storage.getValueType().get(bb))
						.as("Mismatch at index %d", _int(i)).isEqualTo(expected);
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#entryCount(java.lang.Object)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testEntryCount(RandomGenerator rng) {
			return baseTests(storage -> {
				int size = randSize(rng);
				Object array = array(storage, size);
				assertThat(storage.entryCount(array)).isEqualTo(size);
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#spanCount(java.lang.Object)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testSpanCount(RandomGenerator rng) {
			return baseTests(storage -> {
				int spans = randSize(rng);
				Object array = array(storage, spans * 2);
				assertThat(storage.spanCount(array)).isEqualTo(spans);
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#getEntry(java.lang.Object, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testGetEntrySingle(RandomGenerator rng) {
			return baseTests(storage -> {
				Object array = array(storage, 1);
				long expected = rand(rng, storage);
				storage.setEntry(array, 0, expected);
				// Read
				long actual = storage.getEntry(array, 0);
				// Verify
				assertThat(actual).isEqualTo(expected);
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#getEntry(java.lang.Object, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testGetEntry(RandomGenerator rng) {
			return baseTests(storage -> {
				int size = randSize(rng);
				Object array = array(storage, size);
				// Prepare
				long[] data = randData(rng, storage, size);
				for (int i = 0; i < data.length; i++) {
					storage.setEntry(array, i, data[i]);
				}
				// Verify
				for (int i = 0; i < data.length; i++) {
					long actual = storage.getEntry(array, i);
					assertThat(actual).isEqualTo(data[i]);
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#getSpanBegin(java.lang.Object, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testGetSpanBeginSingle(RandomGenerator rng) {
			return baseTests(storage -> {
				Object array = array(storage, 2);
				// Prepare
				Pair<Long, Long> span = randSpan(rng, storage);
				writeSpans(storage, array, span);
				// Verify
				assertThat(storage.getSpanBegin(array, 0)).isEqualTo(span.first.longValue());
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#getSpanBegin(java.lang.Object, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testGetSpanBegin(RandomGenerator rng) {
			return baseTests(storage -> {
				int size = randSize(rng);
				Object array = array(storage, size * 2);
				// Prepare
				Pair<Long, Long>[] spans = randSpans(rng, storage, size);
				writeSpans(storage, array, spans);
				// Verify
				for (int i = 0; i < spans.length; i++) {
					long actual = storage.getSpanBegin(array, i);
					assertThat(actual).as("Mismatch at index %d", _int(i))
						.isEqualTo(spans[i].first.longValue());
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#getSpanEnd(java.lang.Object, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testGetSpanEndSingle(RandomGenerator rng) {
			return baseTests(storage -> {
				Object array = array(storage, 2);
				// Prepare
				Pair<Long, Long> span = randSpan(rng, storage);
				writeSpans(storage, array, span);
				// Verify
				assertThat(storage.getSpanEnd(array, 0)).isEqualTo(span.second.longValue());
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#getSpanEnd(java.lang.Object, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testGetSpanEnd(RandomGenerator rng) {
			return baseTests(storage -> {
				int size = randSize(rng);
				Object array = array(storage, size * 2);
				// Prepare
				Pair<Long, Long>[] spans = randSpans(rng, storage, size);
				writeSpans(storage, array, spans);
				// Verify
				for (int i = 0; i < spans.length; i++) {
					long actual = storage.getSpanEnd(array, i);
					assertThat(actual).as("Mismatch at index %d", _int(i))
						.isEqualTo(spans[i].second.longValue());
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#setEntry(java.lang.Object, int, long)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testSetEntry(RandomGenerator rng) {
			return baseTests(storage -> {
				int size = randSize(rng);
				Object array = array(storage, size);
				// Prepare
				long[] data = randData(rng, storage, size);
				for (int i = 0; i < data.length; i++) {
					storage.setEntry(array, i, data[i]);
				}
				// Verify
				for (int i = 0; i < data.length; i++) {
					long actual = storage.getEntry(array, i);
					assertThat(actual).isEqualTo(data[i]);
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#setSpanBegin(java.lang.Object, int, long)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testSetSpanBegin(RandomGenerator rng) {
			return baseTests(storage -> {
				int size = randSize(rng);
				Object array = array(storage, size * 2);
				// Prepare
				Pair<Long, Long>[] spans = randSpans(rng, storage, size);
				for (int i = 0; i < spans.length; i++) {
					storage.setSpanBegin(array, i, spans[i].first.longValue());
				}
				// Verify
				for (int i = 0; i < spans.length; i++) {
					long actual = storage.getSpanBegin(array, i);
					assertThat(actual).as("Mismatch at index %d", _int(i))
						.isEqualTo(spans[i].first.longValue());

					assertThat(storage.getSpanEnd(array, i))
						.as("Illegal write at %d", _int(i))
						.isEqualTo(-1L);
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#setSpanEnd(java.lang.Object, int, long)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testSetSpanEnd(RandomGenerator rng) {
			return baseTests(storage -> {
				int size = randSize(rng);
				Object array = array(storage, size * 2);
				// Prepare
				Pair<Long, Long>[] spans = randSpans(rng, storage, size);
				for (int i = 0; i < spans.length; i++) {
					storage.setSpanEnd(array, i, spans[i].second.longValue());
				}
				// Verify
				for (int i = 0; i < spans.length; i++) {
					long actual = storage.getSpanEnd(array, i);
					assertThat(actual).as("Mismatch at index %d", _int(i))
						.isEqualTo(spans[i].second.longValue());

					assertThat(storage.getSpanBegin(array, i))
						.as("Illegal write at %d", _int(i))
						.isEqualTo(-1L);
				}
			});
		}

	}

	@Nested
	class FindMethods {

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#findSorted(java.lang.Object, int, int, long)}.
		 */
		@TestFactory
		Stream<DynamicNode> testFindSortedFixed() {
			return baseTests(storage -> {
				long[] sequence = LongStream.range(0, 10).toArray();
				Object array = array(storage, sequence.length);
				// Prepare
				for (int i = 0; i < sequence.length; i++) {
					storage.setEntry(array, i, i);
				}
				// Verify
				for (int i = 0; i < sequence.length; i++) {
					// Global search -> hit
					assertThat(storage.findSorted(array, 0, sequence.length, i))
						.as("Not found (Global) %d", _int(i)).isEqualTo(i);
					// Single search -> hit
					assertThat(storage.findSorted(array, i, sequence.length, i))
						.as("Not found (limited) %d", _int(i)).isEqualTo(i);
					// Exclude value by window -> miss
					assertThat(storage.findSorted(array, i>0 ? 0 : i+1, i>0 ? i : sequence.length, i))
						.as("Not found (limited) %d", _int(i)).isEqualTo(-1L);
				}
				assertThat(storage.findSorted(array, 0, sequence.length, 100))
					.as("Found foreign %d", _int(100)).isEqualTo(-1L);
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#sparseFindSorted(Object, int, int, long)}.
		 */
		@TestFactory
		Stream<DynamicNode> testSparseFindSortedFixed() {
			return baseTests(storage -> {
				long[] sequence = LongStream.range(0, 10).toArray();
				Object array = array(storage, sequence.length);
				// Prepare
				for (int i = 1; i < sequence.length; i++) {
					if(i%2==1)
						storage.setEntry(array, i, i);
				}
				// Verify
				for (int i = 0; i < sequence.length; i++) {
					if(i%2==0) {
						assertThat(storage.sparseFindSorted(array, 0, sequence.length, i))
							.as("Found empty %d", _int(i)).isEqualTo(-1L);
					} else {
						// Global search -> hit
						assertThat(storage.sparseFindSorted(array, 0, sequence.length, i))
							.as("Not found (Global) %d", _int(i)).isEqualTo(i);
						// Single search -> hit
						assertThat(storage.sparseFindSorted(array, i, sequence.length, i))
							.as("Not found (limited) %d", _int(i)).isEqualTo(i);
						// Exclude value by window -> miss
						assertThat(storage.sparseFindSorted(array, i>0 ? 0 : i+1, i>0 ? i : sequence.length, i))
							.as("Not found (limited) %d", _int(i)).isEqualTo(-1L);
					}
				}
				assertThat(storage.sparseFindSorted(array, 0, sequence.length, 100))
					.as("Found foreign %d", _int(100)).isEqualTo(-1L);
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#findSorted(java.lang.Object, int, int, long)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testFindSortedRandom(RandomGenerator rng) {
			return baseTests(storage -> {
				int size = randSize(rng);
				long[] sequence = rng.longs(0, storage.maxValue())
						.distinct()
						.limit(size)
						.sorted()
						.toArray();
				Object array = array(storage, sequence.length);
				// Prepare
				for (int i = 0; i < sequence.length; i++) {
					storage.setEntry(array, i, sequence[i]);
				}
				// Verify
				for (int i = 0; i < sequence.length; i++) {
					// Global search -> hit
					assertThat(storage.findSorted(array, 0, sequence.length, sequence[i]))
						.as("Not found (Global) %d", _int(i)).isEqualTo(i);
					// Single search -> hit
					assertThat(storage.findSorted(array, i, sequence.length, sequence[i]))
						.as("Not found (limited) %d", _int(i)).isEqualTo(i);
					// Exclude value by window -> miss
					assertThat(storage.findSorted(array, i>0 ? 0 : i+1, i>0 ? i : sequence.length, sequence[i]))
						.as("Not found (limited) %d", _int(i)).isEqualTo(-1L);
				}
				// The way we generate the random data excludes type.maxValue()
				long notPresent = storage.maxValue();
				assertThat(storage.findSorted(array, 0, sequence.length, notPresent))
					.as("Found foreign %d", _long(notPresent)).isEqualTo(-1L);
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#sparseFindSorted(Object, int, int, long)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testSparseFindSortedRandom(RandomGenerator rng) {
			return baseTests(storage -> {
				int size = randSize(rng);
				long[] sequence = rng.longs(0, storage.maxValue())
						.distinct()
						.limit(size)
						.sorted()
						.toArray();
				BitSet used = BitSet.valueOf(rng.longs(2).toArray());
				Object array = array(storage, sequence.length);
				// Prepare
				for (int i = 0; i < sequence.length; i++) {
					if(used.get(i))
						storage.setEntry(array, i, sequence[i]);
				}
				// Verify
				for (int i = 0; i < sequence.length; i++) {
					if(!used.get(i)) {
						assertThat(storage.sparseFindSorted(array, 0, sequence.length, sequence[i]))
						.as("Found unused %d", _long(sequence[i])).isEqualTo(-1L);
					} else {
						// Global search -> hit
						assertThat(storage.sparseFindSorted(array, 0, sequence.length, sequence[i]))
							.as("Not found (Global) %d", _int(i)).isEqualTo(i);
						// Single search -> hit
						assertThat(storage.sparseFindSorted(array, i, sequence.length, sequence[i]))
							.as("Not found (limited) %d", _int(i)).isEqualTo(i);
						// Exclude value by window -> miss
						assertThat(storage.sparseFindSorted(array, i>0 ? 0 : i+1, i>0 ? i : sequence.length, sequence[i]))
							.as("Not found (limited) %d", _int(i)).isEqualTo(-1L);
					}
				}
				// The way we generate the random data excludes type.maxValue()
				long notPresent = storage.maxValue();
				assertThat(storage.sparseFindSorted(array, 0, sequence.length, notPresent))
					.as("Found foreign %d", _long(notPresent)).isEqualTo(-1L);
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#find(java.lang.Object, int, int, long)}.
		 */
		@TestFactory
		Stream<DynamicNode> testFindFixed() {
			return baseTests(storage -> {
				long[] sequence = {1, 3, 6, 4, 2, 7, 9, 8};
				Object array = array(storage, sequence.length);
				// Prepare
				for (int i = 0; i < sequence.length; i++) {
					storage.setEntry(array, i, sequence[i]);
				}
				// Verify
				for (int i = 0; i < sequence.length; i++) {
					// Global search -> hit
					assertThat(storage.find(array, 0, sequence.length, sequence[i]))
						.as("Not found (Global) %d", _int(i)).isEqualTo(i);
					// Single search -> hit
					assertThat(storage.find(array, i, sequence.length, sequence[i]))
						.as("Not found (limited) %d", _int(i)).isEqualTo(i);
					// Exclude value by window -> miss
					assertThat(storage.find(array, i>0 ? 0 : i+1, i>0 ? i : sequence.length, sequence[i]))
						.as("Not found (limited) %d", _int(i)).isEqualTo(-1L);
				}
				assertThat(storage.find(array, 0, sequence.length, 100))
					.as("Found foreign %d", _int(100)).isEqualTo(-1L);
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#find(java.lang.Object, int, int, long)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testFindRandom(RandomGenerator rng) {
			return baseTests(storage -> {
				int size = randSize(rng);
				long[] sequence = rng.longs(0, storage.maxValue())
						.distinct()
						.limit(size)
						.toArray();
				Object array = array(storage, sequence.length);
				// Prepare
				for (int i = 0; i < sequence.length; i++) {
					storage.setEntry(array, i, sequence[i]);
				}
				// Verify
				for (int i = 0; i < sequence.length; i++) {
					// Global search -> hit
					assertThat(storage.find(array, 0, sequence.length, sequence[i]))
						.as("Not found (Global) %d", _int(i)).isEqualTo(i);
					// Single search -> hit
					assertThat(storage.find(array, i, sequence.length, sequence[i]))
						.as("Not found (limited) %d", _int(i)).isEqualTo(i);
					// Exclude value by window -> miss
					assertThat(storage.find(array, i>0 ? 0 : i+1, i>0 ? i : sequence.length, sequence[i]))
						.as("Not found (limited) %d", _int(i)).isEqualTo(-1L);
				}
				assertThat(storage.find(array, 0, sequence.length, 100))
					.as("Found foreign %d", _int(100)).isEqualTo(-1L);
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#findSortedSpan(java.lang.Object, int, int, long)}.
		 */
		@TestFactory
		Stream<DynamicNode> testFindSortedSpanFixed() {
			return baseTests(storage -> {
				@SuppressWarnings("unchecked")
				Pair<Long, Long>[] spans = new Pair[]{
						longPair(0, 1),
						longPair(2, 4),
						longPair(5, 8),
						longPair(10, 20)
				};
				Object array = array(storage, spans.length*2);
				writeSpans(storage, array, spans);
				// Verify
				for (int i = 0; i < spans.length; i++) {
					Pair<Long, Long> span = spans[i];
					// Ensure we find our actual spans
					assertThat(storage.findSortedSpan(array, 0, spans.length, span.first.longValue()))
						.as("Not found span begin %d", _int(i)).isEqualTo(i);
					assertThat(storage.findSortedSpan(array, 0, spans.length, span.second.longValue()))
						.as("Not found span end %d", _int(i)).isEqualTo(i);

					// Verify we don't find them in excluded spans
					assertThat(storage.findSortedSpan(array, i>0 ? 0 : i+1, i>0 ? i : spans.length, span.second.longValue()))
						.as("Found excluded %d", _int(i)).isEqualTo(-1L);
				}
				assertThat(storage.findSortedSpan(array, 0, spans.length, 100))
					.as("Found foreign").isEqualTo(-1L);
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#sparseFindSortedSpan(Object, int, int, long)}.
		 */
		@TestFactory
		Stream<DynamicNode> testSparseFindSortedSpanFixed() {
			return baseTests(storage -> {
				@SuppressWarnings("unchecked")
				Pair<Long, Long>[] spans = new Pair[]{
						longPair(1, 3),
						null,
						longPair(4, 5),
						longPair(7, 8),
						null,
						null,
						longPair(10, 20),
						null
				};
				long[] unused = {0, 6, 9, 21, 100};
				Object array = array(storage, spans.length*2);
				writeSpans(storage, array, spans);
				// Verify
				for (int i = 0; i < spans.length; i++) {
					Pair<Long, Long> span = spans[i];
					if(span==NO_SPAN) {
						continue;
					}
					// Ensure we find our actual spans
					assertThat(storage.sparseFindSortedSpan(array, 0, spans.length, span.first.longValue()))
						.as("Not found span begin %d", _int(i)).isEqualTo(i);
					assertThat(storage.sparseFindSortedSpan(array, 0, spans.length, span.second.longValue()))
						.as("Not found span end %d", _int(i)).isEqualTo(i);

					// Verify we don't find them in excluded spans
					assertThat(storage.sparseFindSortedSpan(array, i>0 ? 0 : i+1, i>0 ? i : spans.length, span.second.longValue()))
						.as("Found excluded %d", _int(i)).isEqualTo(-1L);
				}
				// Verify some unused values aren't found
				for(long value : unused) {
				assertThat(storage.sparseFindSortedSpan(array, 0, spans.length, value))
					.as("Found foreign %d", _long(value)).isEqualTo(-1L);
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.IndexBlockStorage#findSpan(java.lang.Object, int, int, long)}.
		 */
		@TestFactory
		Stream<DynamicNode> testFindSpan() {
			return baseTests(storage -> {
				@SuppressWarnings("unchecked")
				Pair<Long, Long>[] spans = new Pair[]{
						longPair(4, 7),
						longPair(2, 4),
						longPair(5, 6),
						longPair(10, 12)
				};
				Object array = array(storage, spans.length*2);
				writeSpans(storage, array, spans);

				// For every value i contains the span that should be found
				int[] val2Span = {
						-1, -1, 1, 1, 0, 0, 0, 0, -1, -1, 3, 3, 3
				};

				// Full searches
				for (int i = 0; i < val2Span.length; i++) {
					int expected = val2Span[i];
					assertThat(storage.findSpan(array, 0, spans.length, i))
						.as("Not found %d in span %d", _int(i), _int(expected)).isEqualTo(expected);
				}

				// Now ensure we can actually find previously "hidden" spans
				assertThat(storage.findSpan(array, 1, spans.length, 4))
					.as("Not found %d in span %d", _int(4), _int(1)).isEqualTo(1);
				assertThat(storage.findSpan(array, 1, spans.length, 6))
					.as("Not found %d in span %d", _int(6), _int(2)).isEqualTo(2);

				// Foreign stuff
				assertThat(storage.findSpan(array, 0, spans.length, 100))
					.as("Found foreign").isEqualTo(-1L);
			});
		}

	}

}
