/**
 *
 */
package de.ims.icarus2.model.api.driver.indices;

import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import de.ims.icarus2.test.TestMessages;
import de.ims.icarus2.test.icarusr;

/**
 * @author Markus Gärtner
 *
 */
public interface RandomAccessIndexSetTest<S extends IndexSet> extends IndexSetTest<S> {

	//TODO assert features not containing forward_only

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#indexAt(int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIndexAt() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					for (int i = 0; i < config.indices.length; i++) {
						assertEquals(config.indices[i], config.set.indexAt(i));
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#firstIndex()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testFirstIndex() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					if(config.indices.length==0) {
						assertEquals(UNSET_LONG, config.set.firstIndex());
					} else {
						assertEquals(config.indices[0], config.set.firstIndex());
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#lastIndex()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testLastIndex() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					int size = config.indices.length;
					if(size==0) {
						assertEquals(UNSET_LONG, config.set.lastIndex());
					} else {
						assertEquals(config.indices[size-1], config.set.lastIndex());
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(byte[], int)}.
	 */
	@TestFactory
	default Stream<DynamicNode> testExportByteArrayInt() {
		return configurations()
				.map(Config::validate)
				.map(config -> {
					List<DynamicTest> tests = new ArrayList<>();
					if(IndexValueType.BYTE.isValidSubstitute(config.valueType)) {

					} else {
						tests.add(dynamicTest("overflow", () -> ));
					}

					return dynamicContainer(config.label, Stream.of(
							dynamicTest("too small", () -> assertThrows(IndexOutOfBoundsException.class,
									() -> config.set.export(new byte[1], 0))),
							dynamicTest("fitting", () -> {
								byte[] buffer = new byte[config.set.size()];
								config.set.export(buffer, 0);
								assertEquals(config.indices.length, buffer.length);
								for (int i = 0; i < buffer.length; i++) {
									assertEquals(config.indices[i], buffer[i]);
								}
							})
					))
				});
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(int, int, byte[], int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testExportIntIntByteArrayInt() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(short[], int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testExportShortArrayInt() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(int, int, short[], int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testExportIntIntShortArrayInt() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(int[], int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testExportIntArrayInt() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(int, int, int[], int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testExportIntIntIntArrayInt() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(long[], int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testExportLongArrayInt() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(int, int, long[], int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testExportIntIntLongArrayInt() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachIndex(java.util.function.LongConsumer)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachIndexLongConsumer() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachIndex(java.util.function.LongConsumer, int, int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachIndexLongConsumerIntInt() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachIndex(java.util.function.IntConsumer)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachIndexIntConsumer() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachIndex(java.util.function.IntConsumer, int, int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachIndexIntConsumerIntInt() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachEntry(java.util.function.LongBinaryOperator)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachEntryLongBinaryOperator() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachEntry(java.util.function.LongBinaryOperator, int, int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachEntryLongBinaryOperatorIntInt() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachEntry(java.util.function.IntBinaryOperator)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachEntryIntBinaryOperator() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachEntry(java.util.function.IntBinaryOperator, int, int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachEntryIntBinaryOperatorIntInt() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#checkIndices(java.util.function.LongPredicate)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testCheckIndicesLongPredicate() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#checkIndices(java.util.function.LongPredicate, int, int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testCheckIndicesLongPredicateIntInt() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#checkIndices(java.util.function.IntPredicate)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testCheckIndicesIntPredicate() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#checkIndices(java.util.function.IntPredicate, int, int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testCheckIndicesIntPredicateIntInt() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#checkConsecutiveIndices(de.ims.icarus2.util.function.LongBiPredicate)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testCheckConsecutiveIndicesLongBiPredicate() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#checkConsecutiveIndices(de.ims.icarus2.util.function.LongBiPredicate, int, int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testCheckConsecutiveIndicesLongBiPredicateIntInt() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#split(int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testSplit() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#subSet(int, int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testSubSet() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#iterator()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIterator() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#iterator(int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIteratorInt() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#iterator(int, int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIteratorIntInt() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#intStream()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIntStream() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#longStream()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testLongStream() {
		fail("Not yet implemented"); // TODO
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {

				}));
	}
}
