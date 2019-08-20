/**
 *
 */
package de.ims.icarus2.model.api.driver.indices;

import static de.ims.icarus2.model.api.ModelTestUtils.overflowAsserter;
import static de.ims.icarus2.test.TestUtils.ioobAsserter;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

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
						int size = config.indices.length;
						// Make target buffer too small
						tests.add(dynamicTest("too small", ioobAsserter(
									() -> config.set.export(new byte[size/2], 0))));
						// Normal match
						tests.add(dynamicTest("fitting", () -> {
							byte[] buffer = new byte[size];
							config.set.export(buffer, 0);
							assertEquals(size, buffer.length);
							for (int i = 0; i < buffer.length; i++) {
								assertEquals(config.indices[i], buffer[i]);
							}
						}));
						// Buffer bigger than needed
						tests.add(dynamicTest("spare room", () -> {
							byte[] buffer = new byte[size*2];
							Arrays.fill(buffer, (byte)-1);
							int offset = random(0, size/2);
							config.set.export(buffer, offset);
							for (int i = 0; i < buffer.length; i++) {
								if(i<offset || i>=offset+size) {
									assertEquals(-1, buffer[i]);
								} else {
									assertEquals(config.indices[i-offset], buffer[i]);
								}
							}
						}));
					} else {
						// Export to target type not supported
						tests.add(dynamicTest("overflow", overflowAsserter(
								() -> config.set.export(new byte[config.indices.length], 0))));
					}

					return dynamicContainer(config.label, tests);
				});
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(short[], int)}.
	 */
	@TestFactory
	default Stream<DynamicNode> testExportShortArrayInt() {
		return configurations()
				.map(Config::validate)
				.map(config -> {
					List<DynamicTest> tests = new ArrayList<>();
					if(IndexValueType.SHORT.isValidSubstitute(config.valueType)) {
						int size = config.indices.length;
						// Make target buffer too small
						tests.add(dynamicTest("too small", ioobAsserter(
									() -> config.set.export(new short[size/2], 0))));
						// Normal match
						tests.add(dynamicTest("fitting", () -> {
							short[] buffer = new short[size];
							config.set.export(buffer, 0);
							assertEquals(size, buffer.length);
							for (int i = 0; i < buffer.length; i++) {
								assertEquals(config.indices[i], buffer[i]);
							}
						}));
						// Buffer bigger than needed
						tests.add(dynamicTest("spare room", () -> {
							short[] buffer = new short[size*2];
							Arrays.fill(buffer, (short)-1);
							int offset = random(0, size/2);
							config.set.export(buffer, offset);
							for (int i = 0; i < buffer.length; i++) {
								if(i<offset || i>=offset+size) {
									assertEquals(-1, buffer[i]);
								} else {
									assertEquals(config.indices[i-offset], buffer[i]);
								}
							}
						}));
					} else {
						// Export to target type not supported
						tests.add(dynamicTest("overflow", overflowAsserter(
								() -> config.set.export(new short[config.indices.length], 0))));
					}

					return dynamicContainer(config.label, tests);
				});
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(int[], int)}.
	 */
	@TestFactory
	default Stream<DynamicNode> testExportIntArrayInt() {
		return configurations()
				.map(Config::validate)
				.map(config -> {
					List<DynamicTest> tests = new ArrayList<>();
					if(IndexValueType.INTEGER.isValidSubstitute(config.valueType)) {
						int size = config.indices.length;
						// Make target buffer too small
						tests.add(dynamicTest("too small", ioobAsserter(
									() -> config.set.export(new int[size/2], 0))));
						// Normal match
						tests.add(dynamicTest("fitting", () -> {
							int[] buffer = new int[size];
							config.set.export(buffer, 0);
							assertEquals(size, buffer.length);
							for (int i = 0; i < buffer.length; i++) {
								assertEquals(config.indices[i], buffer[i]);
							}
						}));
						// Buffer bigger than needed
						tests.add(dynamicTest("spare room", () -> {
							int[] buffer = new int[size*2];
							Arrays.fill(buffer, -1);
							int offset = random(0, size/2);
							config.set.export(buffer, offset);
							for (int i = 0; i < buffer.length; i++) {
								if(i<offset || i>=offset+size) {
									assertEquals(-1, buffer[i]);
								} else {
									assertEquals(config.indices[i-offset], buffer[i]);
								}
							}
						}));
					} else {
						// Export to target type not supported
						tests.add(dynamicTest("overflow", overflowAsserter(
								() -> config.set.export(new int[config.indices.length], 0))));
					}

					return dynamicContainer(config.label, tests);
				});
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(long[], int)}.
	 */
	@TestFactory
	default Stream<DynamicNode> testExportLongArrayInt() {
		return configurations()
				.map(Config::validate)
				.map(config -> {
					List<DynamicTest> tests = new ArrayList<>();
					if(IndexValueType.LONG.isValidSubstitute(config.valueType)) {
						int size = config.indices.length;
						// Make target buffer too small
						tests.add(dynamicTest("too small", ioobAsserter(
									() -> config.set.export(new long[size/2], 0))));
						// Normal match
						tests.add(dynamicTest("fitting", () -> {
							long[] buffer = new long[size];
							config.set.export(buffer, 0);
							assertEquals(size, buffer.length);
							for (int i = 0; i < buffer.length; i++) {
								assertEquals(config.indices[i], buffer[i]);
							}
						}));
						// Buffer bigger than needed
						tests.add(dynamicTest("spare room", () -> {
							long[] buffer = new long[size*2];
							Arrays.fill(buffer, -1);
							int offset = random(0, size/2);
							config.set.export(buffer, offset);
							for (int i = 0; i < buffer.length; i++) {
								if(i<offset || i>=offset+size) {
									assertEquals(-1, buffer[i]);
								} else {
									assertEquals(config.indices[i-offset], buffer[i]);
								}
							}
						}));
					} else {
						// Export to target type not supported
						tests.add(dynamicTest("overflow", overflowAsserter(
								() -> config.set.export(new long[config.indices.length], 0))));
					}

					return dynamicContainer(config.label, tests);
				});
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(int, int, byte[], int)}.
	 */
	@TestFactory
	default Stream<DynamicNode> testExportIntIntByteArrayInt() {
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
	default Stream<DynamicNode> testExportIntIntShortArrayInt() {
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
	default Stream<DynamicNode> testExportIntIntIntArrayInt() {
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
	default Stream<DynamicNode> testExportIntIntLongArrayInt() {
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
