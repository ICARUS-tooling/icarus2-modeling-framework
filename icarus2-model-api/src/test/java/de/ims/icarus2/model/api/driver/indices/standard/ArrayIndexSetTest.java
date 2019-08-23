/**
 *
 */
package de.ims.icarus2.model.api.driver.indices.standard;

import static de.ims.icarus2.model.api.ModelTestUtils.assertIndicesEqualsExact;
import static de.ims.icarus2.model.api.ModelTestUtils.set;
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.test.TestUtils.randomBytes;
import static de.ims.icarus2.test.TestUtils.randomInts;
import static de.ims.icarus2.test.TestUtils.randomLongs;
import static de.ims.icarus2.test.TestUtils.randomShorts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.RandomAccessIndexSetTest;
import de.ims.icarus2.test.TestSettings;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;

/**
 * @author Markus Gärtner
 *
 */
class ArrayIndexSetTest implements RandomAccessIndexSetTest<ArrayIndexSet> {

	private static Function<Config, IndexSet> constructor = config -> {
		long[] indices = config.getIndices();
		IndexValueType type = config.getValueType();
		Object array = type.newArray(indices.length);
		type.copyFrom(indices, 0, array, 0, indices.length);
		if(config.isSorted()) {
			return new ArrayIndexSet(type, array, 0, indices.length-1, true);
		}

		return new ArrayIndexSet(type, array);
	};

	private static int randomSize() {
		return random(10, 100);
	}

	@Override
	public Stream<Config> configurations() {
		Config base = new Config()
				.features(IndexSet.DEFAULT_FEATURES);

		return Stream.of(IndexValueType.values())
				.map(type -> base.clone().valueType(type).set(constructor))
				.flatMap(config -> Stream.of(
						// Sorted version
						config.clone()
							.label(config.getValueType()+" sorted")
							.sortedIndices(randomSize())
							.sorted(true),
						// Random version
						config.clone()
							.label(config.getValueType()+" random")
							.randomIndices(randomSize())
						));
	}

	@Override
	public Class<?> getTestTargetClass() {
		return ArrayIndexSet.class;
	}

	@Override
	public ArrayIndexSet createTestInstance(TestSettings settings) {
		return settings.process(new ArrayIndexSet(randomInts(10, 0, Integer.MAX_VALUE)));
	}

	@Nested
	class Constructors {

		private void assertIndexSet(ArrayIndexSet set, Object array,
				int size, IndexValueType type) {
			assertEquals(size, set.size());
			assertSame(array, set.getIndices());
			assertEquals(type, set.getIndexValueType());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#ArrayIndexSet(java.lang.Object)}.
		 */
		@ParameterizedTest
		@EnumSource(IndexValueType.class)
		void testArrayIndexSetObject(IndexValueType type) {
			int size = randomSize();
			Object array = type.newArray(size);
			ArrayIndexSet set = new ArrayIndexSet(array);
			assertIndexSet(set, array, size, type);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#ArrayIndexSet(de.ims.icarus2.model.api.driver.indices.IndexValueType, java.lang.Object)}.
		 */
		@ParameterizedTest
		@EnumSource(IndexValueType.class)
		void testArrayIndexSetIndexValueTypeObject(IndexValueType type) {
			int size = randomSize();
			Object array = type.newArray(size);
			ArrayIndexSet set = new ArrayIndexSet(type, array);
			assertIndexSet(set, array, size, type);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#ArrayIndexSet(de.ims.icarus2.model.api.driver.indices.IndexValueType, java.lang.Object, boolean)}.
		 */
		@TestFactory
		Stream<DynamicTest> testArrayIndexSetIndexValueTypeObjectBoolean() {
			return Stream.of(IndexValueType.values())
					.flatMap(type -> Stream.of(dynamicTest(type+" default", () -> {
						int size = randomSize();
						Object array = type.newArray(size);
						ArrayIndexSet set = new ArrayIndexSet(type, array, true);
						assertIndexSet(set, array, size, type);
						assertTrue(set.isSorted());
					}),
						dynamicTest(type+" default", () -> {
							int size = randomSize();
							Object array = type.newArray(size);
							ArrayIndexSet set = new ArrayIndexSet(type, array, false);
							assertIndexSet(set, array, size, type);
							assertFalse(set.isSorted());
						})
					));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#ArrayIndexSet(de.ims.icarus2.model.api.driver.indices.IndexValueType, java.lang.Object, int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testArrayIndexSetIndexValueTypeObjectInt() {
			return Stream.of(IndexValueType.values())
					.map(type -> dynamicContainer(type.name(),
							random().ints(1, 1000)
								.limit(10)
								.mapToObj(size -> dynamicTest(String.valueOf(size), () -> {
									Object array = type.newArray(size);
									int numIndices = random(1, size+1);
									ArrayIndexSet set = new ArrayIndexSet(type, array, numIndices);
									assertIndexSet(set, array, numIndices, type);
								}))));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#ArrayIndexSet(de.ims.icarus2.model.api.driver.indices.IndexValueType, java.lang.Object, int, int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testArrayIndexSetIndexValueTypeObjectIntInt() {
			return Stream.of(IndexValueType.values())
					.map(type -> dynamicContainer(type.name(),
							random().ints(1, 1000)
								.limit(10)
								.mapToObj(size -> dynamicTest(String.valueOf(size), () -> {
									Object array = type.newArray(size);
									int from = random(0, size);
									int to = random(from, size);
									ArrayIndexSet set = new ArrayIndexSet(type, array, from, to);
									assertIndexSet(set, array, to-from+1, type);
								}))));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#ArrayIndexSet(de.ims.icarus2.model.api.driver.indices.IndexValueType, java.lang.Object, int, int, boolean)}.
		 */
		@SuppressWarnings("boxing")
		@TestFactory
		Stream<DynamicNode> testArrayIndexSetIndexValueTypeObjectIntIntBoolean() {
			return Stream.of(IndexValueType.values())
					.map(type -> dynamicContainer(type.name(),
							random().ints(1, 1000)
								.limit(10)
								.mapToObj(Integer::valueOf)
								.flatMap(size -> Stream.of(
										dynamicTest(String.valueOf(size), () -> {
										Object array = type.newArray(size);
										int from = random(0, size);
										int to = random(from, size);
										ArrayIndexSet set = new ArrayIndexSet(type, array, from, to, true);
										assertIndexSet(set, array, to-from+1, type);
										assertTrue(set.isSorted());
										}),
										dynamicTest(String.valueOf(size), () -> {
										Object array = type.newArray(size);
										int from = random(0, size);
										int to = random(from, size);
										ArrayIndexSet set = new ArrayIndexSet(type, array, from, to, false);
										assertIndexSet(set, array, to-from+1, type);
										assertFalse(set.isSorted());
										})
								))));
		}

	}

	@Nested
	class FactoryMethods {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#copyOf(de.ims.icarus2.model.api.driver.indices.IndexSet)}.
		 */
		@TestFactory
		Stream<DynamicTest> testCopyOfIndexSet() {
			return Stream.of(IndexValueType.values())
					.map(type -> dynamicTest(type.name(), () -> {
						long[] indices = randomLongs(randomSize(), 0, type.maxValue());
						IndexSet source = set(type, indices);
						ArrayIndexSet set = ArrayIndexSet.copyOf(source);
						assertIndicesEqualsExact(source, set);
						assertEquals(type, set.getIndexValueType());
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#copyOf(de.ims.icarus2.model.api.driver.indices.IndexSet, int, int)}.
		 */
		@TestFactory
		Stream<DynamicTest> testCopyOfIndexSetIntInt() {
			return Stream.of(IndexValueType.values())
					.map(type -> dynamicTest(type.name(), () -> {
						int size = randomSize();
						long[] indices = randomLongs(size, 0, type.maxValue());
						IndexSet source = set(type, indices);
						int from = random(0, size);
						int to = random(from, size);
						ArrayIndexSet set = ArrayIndexSet.copyOf(source, from, to);
						assertEquals(to-from+1, set.size());
						assertEquals(type, set.getIndexValueType());
						for (int i = from; i <= to; i++) {
							assertEquals(indices[i], set.indexAt(i-from));
						}
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#copyOf(it.unimi.dsi.fastutil.bytes.ByteList)}.
		 */
		@RepeatedTest(RUNS)
		void testCopyOfByteList() {
			ByteList list = new ByteArrayList(randomBytes(randomSize(), (byte)0, Byte.MAX_VALUE));
			ArrayIndexSet set = ArrayIndexSet.copyOf(list);
			assertEquals(list.size(), set.size());
			assertEquals(IndexValueType.BYTE, set.getIndexValueType());
			for (int i = 0; i < list.size(); i++) {
				assertEquals(list.getByte(i), set.indexAt(i));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#copyOf(it.unimi.dsi.fastutil.shorts.ShortList)}.
		 */
		@RepeatedTest(RUNS)
		void testCopyOfShortList() {
			ShortList list = new ShortArrayList(randomShorts(randomSize(), (short)0, Short.MAX_VALUE));
			ArrayIndexSet set = ArrayIndexSet.copyOf(list);
			assertEquals(list.size(), set.size());
			assertEquals(IndexValueType.SHORT, set.getIndexValueType());
			for (int i = 0; i < list.size(); i++) {
				assertEquals(list.getShort(i), set.indexAt(i));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#copyOf(it.unimi.dsi.fastutil.ints.IntList)}.
		 */
		@RepeatedTest(RUNS)
		void testCopyOfIntList() {
			IntList list = new IntArrayList(randomInts(randomSize(), 0, Integer.MAX_VALUE));
			ArrayIndexSet set = ArrayIndexSet.copyOf(list);
			assertEquals(list.size(), set.size());
			assertEquals(IndexValueType.INTEGER, set.getIndexValueType());
			for (int i = 0; i < list.size(); i++) {
				assertEquals(list.getInt(i), set.indexAt(i));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#copyOf(it.unimi.dsi.fastutil.longs.LongList)}.
		 */
		@RepeatedTest(RUNS)
		void testCopyOfLongList() {
			LongList list = new LongArrayList(randomLongs(randomSize(), 0L, Long.MAX_VALUE));
			ArrayIndexSet set = ArrayIndexSet.copyOf(list);
			assertEquals(list.size(), set.size());
			assertEquals(IndexValueType.LONG, set.getIndexValueType());
			for (int i = 0; i < list.size(); i++) {
				assertEquals(list.getLong(i), set.indexAt(i));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#fromIterator(java.util.PrimitiveIterator.OfInt)}.
		 */
		@RepeatedTest(RUNS)
		void testFromIteratorOfInt() {
			IntList list = new IntArrayList(randomInts(randomSize(), 0, Integer.MAX_VALUE));
			ArrayIndexSet set = ArrayIndexSet.fromIterator(list.iterator());
			assertEquals(list.size(), set.size());
			assertEquals(IndexValueType.INTEGER, set.getIndexValueType());
			for (int i = 0; i < list.size(); i++) {
				assertEquals(list.getInt(i), set.indexAt(i));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#fromIterator(java.util.PrimitiveIterator.OfLong)}.
		 */
		@RepeatedTest(RUNS)
		void testFromIteratorOfLong() {
			LongList list = new LongArrayList(randomLongs(randomSize(), 0L, Long.MAX_VALUE));
			ArrayIndexSet set = ArrayIndexSet.fromIterator(list.iterator());
			assertEquals(list.size(), set.size());
			assertEquals(IndexValueType.LONG, set.getIndexValueType());
			for (int i = 0; i < list.size(); i++) {
				assertEquals(list.getLong(i), set.indexAt(i));
			}
		}

	}

}
