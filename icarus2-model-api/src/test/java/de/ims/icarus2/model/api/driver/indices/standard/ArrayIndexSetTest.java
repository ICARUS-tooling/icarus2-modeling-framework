/**
 *
 */
package de.ims.icarus2.model.api.driver.indices.standard;

import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.test.TestUtils.randomInts;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.RandomAccessIndexSetTest;
import de.ims.icarus2.test.TestSettings;

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
				.map(type -> base.clone().valueType(type))
				.flatMap(config -> Stream.of(
						// Sorted version
						config.clone()
							.label(config.getValueType()+" sorted")
							.sortedIndices(randomSize())
							.sorted(true)
							.set(constructor),
						// Random version
						config.clone()
							.label(config.getValueType()+" random")
							.randomIndices(randomSize())
							.set(constructor)
						));

//		int[] indices = randomInts(100, 0, Integer.MAX_VALUE);
//		return Stream.of(new Config()
//				.label("default")
//				.indices(indices)
//				.sorted(false)
//				.valueType(IndexValueType.INTEGER)
//				.features(IndexSet.DEFAULT_FEATURES)
//				.set(new ArrayIndexSet(indices)));
	}

	@Override
	public Class<?> getTestTargetClass() {
		return ArrayIndexSet.class;
	}

	@Override
	public ArrayIndexSet createTestInstance(TestSettings settings) {
		return settings.process(new ArrayIndexSet(randomInts(10, 0, Integer.MAX_VALUE)));
	}

	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#ArrayIndexSet(java.lang.Object)}.
		 */
		@Test
		void testArrayIndexSetObject() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#ArrayIndexSet(de.ims.icarus2.model.api.driver.indices.IndexValueType, java.lang.Object)}.
		 */
		@Test
		void testArrayIndexSetIndexValueTypeObject() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#ArrayIndexSet(de.ims.icarus2.model.api.driver.indices.IndexValueType, java.lang.Object, boolean)}.
		 */
		@Test
		void testArrayIndexSetIndexValueTypeObjectBoolean() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#ArrayIndexSet(de.ims.icarus2.model.api.driver.indices.IndexValueType, java.lang.Object, int)}.
		 */
		@Test
		void testArrayIndexSetIndexValueTypeObjectInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#ArrayIndexSet(de.ims.icarus2.model.api.driver.indices.IndexValueType, java.lang.Object, int, int)}.
		 */
		@Test
		void testArrayIndexSetIndexValueTypeObjectIntInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#ArrayIndexSet(de.ims.icarus2.model.api.driver.indices.IndexValueType, java.lang.Object, int, int, boolean)}.
		 */
		@Test
		void testArrayIndexSetIndexValueTypeObjectIntIntBoolean() {
			fail("Not yet implemented"); // TODO
		}

	}

	class FactoryMethods {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#copyOf(de.ims.icarus2.model.api.driver.indices.IndexSet)}.
		 */
		@Test
		void testCopyOfIndexSet() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#copyOf(de.ims.icarus2.model.api.driver.indices.IndexSet, int, int)}.
		 */
		@Test
		void testCopyOfIndexSetIntInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#copyOf(it.unimi.dsi.fastutil.bytes.ByteList)}.
		 */
		@Test
		void testCopyOfByteList() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#copyOf(it.unimi.dsi.fastutil.shorts.ShortList)}.
		 */
		@Test
		void testCopyOfShortList() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#copyOf(it.unimi.dsi.fastutil.ints.IntList)}.
		 */
		@Test
		void testCopyOfIntList() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#copyOf(it.unimi.dsi.fastutil.longs.LongList)}.
		 */
		@Test
		void testCopyOfLongList() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#fromIterator(java.util.PrimitiveIterator.OfInt)}.
		 */
		@Test
		void testFromIteratorOfInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet#fromIterator(java.util.PrimitiveIterator.OfLong)}.
		 */
		@Test
		void testFromIteratorOfLong() {
			fail("Not yet implemented"); // TODO
		}

	}

}
