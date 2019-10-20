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
package de.ims.icarus2.model.api.driver.indices.standard;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.api.ModelTestUtils.set;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.collections.ArrayUtils.fillAscending;
import static de.ims.icarus2.util.lang.Primitives.strictToByte;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static de.ims.icarus2.util.lang.Primitives.strictToShort;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.doAnswer;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexSetTest;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.RandomAccessIndexSetTest;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.MutablePrimitives.MutableInteger;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.lang.Primitives;

/**
 * @author Markus Gärtner
 *
 */
@RandomizedTest
class IndexBufferTest implements RandomAccessIndexSetTest<IndexBuffer> {

	static RandomGenerator rand;

	private static final Function<Config, IndexSet> constructor = config -> {
		long[] indices = config.getIndices();
		IndexBuffer buffer = new IndexBuffer(config.getValueType(), indices.length);
		buffer.add(indices);
		return buffer;
	};

	private static final int MAX_SIZE = 100;

	private static int randomSize() {
		return rand.random(10, MAX_SIZE);
	}

	@Override
	public Stream<Config> configurations() {
		return Stream.of(IndexValueType.values())
				.map(type -> new Config()
						.valueType(type)
						.rand(rand)
						.defaultFeatures())
				.flatMap(config -> Stream.of(
						// Empty
						config.clone()
							.label(config.getValueType()+" empty")
							.indices()
							.sorted(true)
							.set(new IndexBuffer(config.getValueType(), randomSize())),
						// Sorted
						config.clone()
							.label(config.getValueType()+" sorted")
							.sortedIndices(randomSize())
							.sorted(true)
							.set(constructor),
						// Random
						config.clone()
							.label(config.getValueType()+" random")
							.randomIndices(randomSize())
							.set(constructor)
						));
	}

	@Override
	public Class<?> getTestTargetClass() {
		return IndexBuffer.class;
	}

	@Override
	public IndexBuffer createTestInstance(TestSettings settings) {
		return settings.process(new IndexBuffer(10));
	}

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#IndexBuffer(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {1, 10, 10_000})
		void testIndexBufferInt(int capacity) {
			IndexBuffer buffer = new IndexBuffer(capacity);
			assertEquals(capacity, buffer.remaining());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#IndexBuffer(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -1, Integer.MAX_VALUE})
		void testIndexBufferIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new IndexBuffer(capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#IndexBuffer(de.ims.icarus2.model.api.driver.indices.IndexValueType, int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testIndexBufferIndexValueTypeInt() {
			return Stream.of(IndexValueType.values())
					.map(type -> dynamicContainer(type.name(),
							IntStream.of(1, 10, 10_000).mapToObj(capacity ->
								dynamicTest(String.valueOf(capacity), () -> {
									IndexBuffer buffer = new IndexBuffer(type, capacity);
									assertEquals(capacity, buffer.remaining());
									assertEquals(type, buffer.getIndexValueType());
								}))));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#IndexBuffer(de.ims.icarus2.model.api.driver.indices.IndexValueType, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -1, Integer.MAX_VALUE})
		@RandomizedTest
		void testIndexBufferIndexValueTypeInt(int capacity, RandomGenerator rand) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new IndexBuffer(rand.random(IndexValueType.values()), capacity));
		}

	}

	private static void assertIndices(Config config) {
		IndexSet set = config.getSet();
		long[] indices = config.getIndices();

		assertNotNull(set);
		assertNotNull(indices);

		if(indices.length==0) {
			assertTrue(set.isEmpty());
		} else {
			for (int i = 0; i < indices.length; i++) {
				assertEquals(indices[i], set.indexAt(i), "Mismatch at index "+i);
			}
		}

		if(config.isSorted()) {
			assertTrue(set.isSorted());
		}
	}

	private static void assertBufferFull(Executable executable) {
		assertModelException(GlobalErrorCode.ILLEGAL_STATE, executable);
	}

	@Nested
	class ForModifications {

		private final Function<Config, Config> makeDefaultIndices = config ->
			config.isSorted() ? config.sortedIndices(randomSize())
					: config.randomIndices(randomSize());

		private Function<Config, Config> makeLimitedIndices(IndexValueType t) {
			return config -> {
				IndexValueType type = t;
				if(config.getValueType().compareTo(type)<0) {
					type = config.getValueType();
				}
				return config.indices(config.isSorted() ?
						IndexSetTest.sortedIndices(type, randomSize(), 0)
						: IndexSetTest.randomIndices(config.rand(), type, randomSize()));
			};
		}


		private Function<Config, IndexSet> createEmpty = config ->
				new IndexBuffer(config.getValueType(), config.getIndices().length);

		Stream<Config> configurations() {

			return Stream.of(IndexValueType.values())
					.map(type -> new Config()
							.valueType(type)
							.defaultFeatures()
							.rand(rand)
							.set(createEmpty))
					.flatMap(config -> Stream.of(
							config.clone()
								.label(config.getValueType()+" random")
//								.randomIndices(randomSize())
								,
							config.clone()
								.label(config.getValueType()+" sorted")
								.sorted(true)
//								.sortedIndices(randomSize())
							));
		}

		IndexBuffer buffer(Config config) {
			return (IndexBuffer)config.getSet();
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#clear()}.
		 */
		@TestFactory
		Stream<DynamicTest> testClear(TestInfo info) {
			return configurations()
				.map(makeDefaultIndices)
				.map(Config::validate)
				.map(config -> dynamicTest(config.getLabel(), () -> {
					IndexBuffer buffer = buffer(config);
					buffer.add(config.getIndices());
					assertFalse(buffer.isEmpty());

					buffer.clear();
					assertTrue(buffer.isEmpty());
				}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#snapshot()}.
		 */
		@TestFactory
		Stream<DynamicTest> testSnapshot() {
			return configurations()
					.map(makeDefaultIndices)
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						IndexBuffer buffer = buffer(config);

						assertNull(buffer.snapshot());

						buffer.add(config.getIndices());
						IndexSet snapshot = buffer.snapshot();
						assertNotNull(snapshot);
						assertNotSame(buffer, snapshot);
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#remaining()}.
		 */
		@TestFactory
		Stream<DynamicTest> testRemaining() {
			return configurations()
					.map(makeDefaultIndices)
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						IndexBuffer buffer = buffer(config);
						long[] indices = config.getIndices();

						assertEquals(indices.length, buffer.remaining());
						buffer.add(config.getIndices());
						assertEquals(0, buffer.remaining());
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(long)}.
		 */
		@TestFactory
		Stream<DynamicTest> testAddLong() {
			return configurations()
					.map(makeDefaultIndices)
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						IndexBuffer buffer = buffer(config);
						for(long index : config.getIndices()) {
							buffer.add(index);
						}
						assertIndices(config);

						assertBufferFull(() -> buffer.add(1L));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(long, long)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicTest> testAddLongLong(RandomGenerator rand) {
			return Stream.of(IndexValueType.values())
					.map(type -> new Config()
							.valueType(type)
							.label(type.name())
							.indices(fillAscending(new long[randomSize()]))
							.sorted(true)
							.defaultFeatures()
							.set(createEmpty))
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						rand.reset();
						IndexBuffer buffer = buffer(config);
						long[] indices = config.getIndices();

						int start = 0;
						while(start < indices.length) {
							int count = rand.random(1, indices.length-start+1);
							int end = start + count - 1;
							buffer.add(indices[start], indices[end]);
							start = end+1;
						}
						assertIndices(config);

						assertBufferFull(() -> buffer.add(1, 3));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(java.util.function.IntSupplier)}.
		 */
		@TestFactory
		Stream<DynamicTest> testAddIntSupplier() {
			return configurations()
					.map(makeLimitedIndices(IndexValueType.INTEGER))
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						IndexBuffer buffer = buffer(config);

						long[] indices = config.getIndices();
						MutableInteger index = new MutableInteger(0);

						IntSupplier supplier = () -> index.intValue()<indices.length ?
								strictToInt(indices[index.getAndIncrement()]) : UNSET_INT;

						buffer.add(supplier);
						assertIndices(config);

						index.setInt(0);
						assertBufferFull(() -> buffer.add(supplier));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(java.util.function.LongSupplier)}.
		 */
		@TestFactory
		Stream<DynamicTest> testAddLongSupplier() {
			return configurations()
					.map(makeDefaultIndices)
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						IndexBuffer buffer = buffer(config);

						long[] indices = config.getIndices();
						MutableInteger index = new MutableInteger(0);

						LongSupplier supplier = () -> index.intValue()<indices.length ?
								indices[index.getAndIncrement()] : UNSET_INT;

						buffer.add(supplier);
						assertIndices(config);

						index.setInt(0);
						assertBufferFull(() -> buffer.add(supplier));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(byte[])}.
		 */
		@TestFactory
		Stream<DynamicTest> testAddByteArray() {
			return configurations()
					.map(makeLimitedIndices(IndexValueType.BYTE))
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						IndexBuffer buffer = buffer(config);
						long[] indices = config.getIndices();
						byte[] tmp = new byte[indices.length];
						for (int i = 0; i < indices.length; i++) {
							tmp[i] = strictToByte(indices[i]);
						}
						buffer.add(tmp);

						assertIndices(config);

						assertBufferFull(() -> buffer.add(tmp));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(byte[], int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicTest> testAddByteArrayIntInt(RandomGenerator rand) {
			return configurations()
					.map(makeLimitedIndices(IndexValueType.BYTE))
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						rand.reset();
						IndexBuffer buffer = buffer(config);
						long[] indices = config.getIndices();
						int offset = rand.random(1, indices.length);
						byte[] tmp = new byte[indices.length*2];
						for (int i = 0; i < indices.length; i++) {
							tmp[offset+i] = strictToByte(indices[i]);
						}
						buffer.add(tmp, offset, indices.length);

						assertIndices(config);

						assertBufferFull(() -> buffer.add(tmp, 0, indices.length));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(short[])}.
		 */
		@TestFactory
		Stream<DynamicTest> testAddShortArray() {
			return configurations()
					.map(makeLimitedIndices(IndexValueType.SHORT))
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						IndexBuffer buffer = buffer(config);
						long[] indices = config.getIndices();
						short[] tmp = new short[indices.length];
						for (int i = 0; i < indices.length; i++) {
							tmp[i] = strictToShort(indices[i]);
						}
						buffer.add(tmp);

						assertIndices(config);

						assertBufferFull(() -> buffer.add(tmp));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(short[], int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicTest> testAddShortArrayIntInt(RandomGenerator rand) {
			return configurations()
					.map(makeLimitedIndices(IndexValueType.SHORT))
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						rand.reset();
						IndexBuffer buffer = buffer(config);
						long[] indices = config.getIndices();
						int offset = rand.random(1, indices.length);
						short[] tmp = new short[indices.length*2];
						for (int i = 0; i < indices.length; i++) {
							tmp[offset+i] = strictToShort(indices[i]);
						}
						buffer.add(tmp, offset, indices.length);

						assertIndices(config);

						assertBufferFull(() -> buffer.add(tmp, 0, indices.length));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(int[])}.
		 */
		@TestFactory
		Stream<DynamicTest> testAddIntArray() {
			return configurations()
					.map(makeLimitedIndices(IndexValueType.INTEGER))
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						IndexBuffer buffer = buffer(config);
						long[] indices = config.getIndices();
						int[] tmp = new int[indices.length];
						for (int i = 0; i < indices.length; i++) {
							tmp[i] = strictToInt(indices[i]);
						}
						buffer.add(tmp);

						assertIndices(config);

						assertBufferFull(() -> buffer.add(tmp));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(int[], int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicTest> testAddIntArrayIntInt(RandomGenerator rand) {
			return configurations()
					.map(makeLimitedIndices(IndexValueType.INTEGER))
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						rand.reset();
						IndexBuffer buffer = buffer(config);
						long[] indices = config.getIndices();
						int offset = rand.random(1, indices.length);
						int[] tmp = new int[indices.length*2];
						for (int i = 0; i < indices.length; i++) {
							tmp[offset+i] = strictToInt(indices[i]);
						}
						buffer.add(tmp, offset, indices.length);

						assertIndices(config);

						assertBufferFull(() -> buffer.add(tmp, 0, indices.length));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(long[])}.
		 */
		@TestFactory
		Stream<DynamicTest> testAddLongArray() {
			return configurations()
					.map(makeDefaultIndices)
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						IndexBuffer buffer = buffer(config);
						long[] indices = config.getIndices();
						buffer.add(indices);

						assertIndices(config);

						assertBufferFull(() -> buffer.add(indices));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(long[], int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicTest> testAddLongArrayIntInt(RandomGenerator rand) {
			return configurations()
					.map(makeDefaultIndices)
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						rand.reset();
						IndexBuffer buffer = buffer(config);
						long[] indices = config.getIndices();
						int offset = rand.random(1, indices.length);
						long[] tmp = new long[indices.length*2];
						System.arraycopy(indices, 0, tmp, offset, indices.length);

						buffer.add(tmp, offset, indices.length);

						assertIndices(config);

						assertBufferFull(() -> buffer.add(tmp, offset, indices.length));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(de.ims.icarus2.model.api.driver.indices.IndexSet)}.
		 */
		@TestFactory
		Stream<DynamicTest> testAddIndexSet() {
			return configurations()
					.map(makeDefaultIndices)
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						IndexBuffer buffer = buffer(config);
						IndexSet set = set(config.getIndices());

						buffer.add(set);
						assertIndices(config);

						assertBufferFull(() -> buffer.add(set));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(de.ims.icarus2.model.api.driver.indices.IndexSet, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicTest> testAddIndexSetInt(RandomGenerator rand) {
			return configurations()
					.map(makeDefaultIndices)
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						rand.reset();
						IndexBuffer buffer = buffer(config);
						long[] indices = config.getIndices();
						int offset = rand.random(1, indices.length);
						long[] tmp = new long[indices.length+offset];
						System.arraycopy(indices, 0, tmp, offset, indices.length);
						IndexSet set = set(tmp);

						buffer.add(set, offset);
						assertIndices(config);

						assertBufferFull(() -> buffer.add(set, offset));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(de.ims.icarus2.model.api.driver.indices.IndexSet, int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicTest> testAddIndexSetIntInt(RandomGenerator rand) {
			return configurations()
					.map(makeDefaultIndices)
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						rand.reset();
						IndexBuffer buffer = buffer(config);
						long[] indices = config.getIndices();
						int offset = rand.random(1, indices.length);
						long[] tmp = new long[indices.length*2];
						System.arraycopy(indices, 0, tmp, offset, indices.length);
						IndexSet set = set(tmp);

						buffer.add(set, offset, offset+indices.length);
						assertIndices(config);

						assertBufferFull(() -> buffer.add(set, offset, offset+indices.length));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
		 */
		@TestFactory
		Stream<DynamicTest> testAddIndexSetArray() {
			return configurations()
					.map(makeDefaultIndices)
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						IndexBuffer buffer = buffer(config);
						IndexSet set = set(config.getIndices());

						buffer.add(new IndexSet[]{set});
						assertIndices(config);

						assertBufferFull(() -> buffer.add(new IndexSet[]{set}));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#addFromItem(de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@TestFactory
		Stream<DynamicTest> testAddFromItem() {
			return configurations()
					.map(makeDefaultIndices)
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						IndexBuffer buffer = buffer(config);
						Item item = mockItem();
						MutableLong index = new MutableLong();
						doAnswer(invoc -> {return index.get();}).when(item).getIndex();

						for(long idx : config.getIndices()) {
							index.setLong(idx);
							buffer.addFromItem(item);
						}

						assertIndices(config);

						assertBufferFull(() -> buffer.addFromItem(item));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#addFromItems(java.util.function.Supplier)}.
		 */
		@TestFactory
		Stream<DynamicTest> testAddFromItemsSupplierOfQextendsItem() {
			return configurations()
					.map(makeDefaultIndices)
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						IndexBuffer buffer = buffer(config);
						long[] indices = config.getIndices();
						Item item = mockItem();
						MutableInteger index = new MutableInteger();
						Supplier<Item> supplier = () -> {
							if(index.intValue()>=indices.length) {
								return null;
							}
							return item;
						};
						doAnswer(invoc -> {
							int idx = index.getAndIncrement();
							return idx <indices.length ? Long.valueOf(indices[idx])
									: Long.valueOf(UNSET_LONG);
						}).when(item).getIndex();

						buffer.addFromItems(supplier);

						assertIndices(config);

						index.setInt(0);
						assertBufferFull(() -> buffer.addFromItems(supplier));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#addFromItems(de.ims.icarus2.model.api.members.item.Item[])}.
		 */
		@TestFactory
		Stream<DynamicTest> testAddFromItemsItemArray() {
			return configurations()
					.map(makeDefaultIndices)
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						IndexBuffer buffer = buffer(config);
						Item[] items = items(config);

						buffer.addFromItems(items);
						assertIndices(config);

						assertBufferFull(() -> buffer.addFromItems(items));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#addFromItems(de.ims.icarus2.model.api.members.item.Item[], int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicTest> testAddFromItemsItemArrayIntInt(RandomGenerator rand) {
			return configurations()
					.map(makeDefaultIndices)
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						rand.reset();
						IndexBuffer buffer = buffer(config);
						long[] indices = config.getIndices();
						int offset = rand.random(1, indices.length);
						Item[] items = new Item[indices.length*2];
						for (int i = 0; i < indices.length; i++) {
							items[offset+i] = new DummyItem(indices[i]);
						}

						buffer.addFromItems(items, offset, indices.length);
						assertIndices(config);

						assertBufferFull(() -> buffer.addFromItems(items, offset, indices.length));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#addFromItems(java.util.List)}.
		 */
		@TestFactory
		Stream<DynamicTest> testAddFromItemsListOfQextendsItem() {
			return configurations()
					.map(makeDefaultIndices)
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						IndexBuffer buffer = buffer(config);
						Item[] items = items(config);

						buffer.addFromItems(Arrays.asList(items));
						assertIndices(config);

						assertBufferFull(() -> buffer.addFromItems(Arrays.asList(items)));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(java.util.PrimitiveIterator.OfLong)}.
		 */
		@TestFactory
		Stream<DynamicTest> testAddOfLong() {
			return configurations()
					.map(makeDefaultIndices)
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						IndexBuffer buffer = buffer(config);

						buffer.add(LongStream.of(config.getIndices()).iterator());

						assertIndices(config);

						assertBufferFull(() -> buffer.add(
								LongStream.of(config.getIndices()).iterator()));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(java.util.PrimitiveIterator.OfInt)}.
		 */
		@TestFactory
		Stream<DynamicTest> testAddOfInt() {
			return configurations()
					.map(makeLimitedIndices(IndexValueType.INTEGER))
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						IndexBuffer buffer = buffer(config);

						buffer.add(LongStream.of(config.getIndices())
								.mapToInt(Primitives::strictToInt)
								.iterator());

						assertIndices(config);

						assertBufferFull(() -> buffer.add(
								LongStream.of(config.getIndices())
								.mapToInt(Primitives::strictToInt)
								.iterator()));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(java.util.stream.LongStream)}.
		 */
		@TestFactory
		Stream<DynamicTest> testAddLongStream() {
			return configurations()
					.map(makeDefaultIndices)
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						IndexBuffer buffer = buffer(config);

						buffer.add(LongStream.of(config.getIndices()));

						assertIndices(config);

						assertBufferFull(() -> buffer.add(LongStream.of(config.getIndices())));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(java.util.stream.IntStream)}.
		 */
		@TestFactory
		Stream<DynamicTest> testAddIntStream() {
			return configurations()
					.map(makeLimitedIndices(IndexValueType.INTEGER))
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						IndexBuffer buffer = buffer(config);

						buffer.add(LongStream.of(config.getIndices())
								.mapToInt(Primitives::strictToInt));

						assertIndices(config);

						assertBufferFull(() -> buffer.add(
								LongStream.of(config.getIndices())
								.mapToInt(Primitives::strictToInt)));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#accept(int)}.
		 */
		@TestFactory
		Stream<DynamicTest> testAcceptInt() {
			return configurations()
					.map(makeLimitedIndices(IndexValueType.INTEGER))
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						IndexBuffer buffer = buffer(config);

						LongStream.of(config.getIndices())
						.mapToInt(Primitives::strictToInt)
						.forEach(buffer::accept);

						assertIndices(config);

						assertBufferFull(() -> LongStream.of(config.getIndices())
								.mapToInt(Primitives::strictToInt)
								.forEach(buffer::accept));
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#accept(long)}.
		 */
		@TestFactory
		Stream<DynamicTest> testAcceptLong() {
			return configurations()
					.map(makeDefaultIndices)
					.map(Config::validate)
					.map(config -> dynamicTest(config.getLabel(), () -> {
						IndexBuffer buffer = buffer(config);

						LongStream.of(config.getIndices()).forEach(buffer::accept);

						assertIndices(config);

						assertBufferFull(() -> LongStream.of(config.getIndices())
								.forEach(buffer::accept));
					}));
		}
	}

	private static Item[] items(Config config) {
		return LongStream.of(config.getIndices())
				.mapToObj(DummyItem::new)
				.toArray(Item[]::new);
	}

	private static class DummyItem implements Item {

		private final long index;

		public DummyItem(long index) {
			this.index = index;
		}

		@Override
		public MemberType getMemberType() { return MemberType.ITEM; }

		@Override
		public Container getContainer() { return null; }

		@Override
		public long getIndex() { return index; }

		@Override
		public long getId() { return UNSET_LONG; }

		@Override
		public boolean isAlive() { return true; }

		@Override
		public boolean isLocked() { return false; }

		@Override
		public boolean isDirty() { return false; }

	}
}
