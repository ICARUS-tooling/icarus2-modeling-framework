/**
 *
 */
package de.ims.icarus2.model.api.driver.indices;

import static de.ims.icarus2.model.api.ModelTestUtils.assertIndicesEquals;
import static de.ims.icarus2.model.api.ModelTestUtils.assertOverflow;
import static de.ims.icarus2.model.api.ModelTestUtils.overflowAsserter;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.wrap;
import static de.ims.icarus2.test.TestUtils.ioobAsserter;
import static de.ims.icarus2.test.TestUtils.noOp;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PrimitiveIterator.OfLong;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.api.driver.indices.IndexSet.Feature;
import de.ims.icarus2.util.MutablePrimitives.MutableInteger;
import de.ims.icarus2.util.function.IntBiConsumer;
import de.ims.icarus2.util.function.IntLongConsumer;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

/**
 * @author Markus Gärtner
 *
 */
public interface RandomAccessIndexSetTest<S extends IndexSet> extends IndexSetTest<S> {

	@TestFactory
	default Stream<DynamicTest> testHasRandomAccessFeatures() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					assertFalse(config.set.hasFeature(Feature.CURSOR_FORWARD_ONLY));
				}));
	}

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
							assertEquals(size, config.set.export(buffer, 0));
							for (int i = 0; i < buffer.length; i++) {
								assertEquals(config.indices[i], buffer[i]);
							}
						}));
						// Buffer bigger than needed
						tests.add(dynamicTest("spare room", () -> {
							byte[] buffer = new byte[size*2];
							Arrays.fill(buffer, (byte)-1);
							int offset = random(0, size/2);
							assertEquals(size, config.set.export(buffer, offset));
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
							assertEquals(size, config.set.export(buffer, 0));
							for (int i = 0; i < buffer.length; i++) {
								assertEquals(config.indices[i], buffer[i]);
							}
						}));
						// Buffer bigger than needed
						tests.add(dynamicTest("spare room", () -> {
							short[] buffer = new short[size*2];
							Arrays.fill(buffer, (short)-1);
							int offset = random(0, size/2);
							assertEquals(size, config.set.export(buffer, offset));
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
							assertEquals(size, config.set.export(buffer, 0));
							for (int i = 0; i < buffer.length; i++) {
								assertEquals(config.indices[i], buffer[i]);
							}
						}));
						// Buffer bigger than needed
						tests.add(dynamicTest("spare room", () -> {
							int[] buffer = new int[size*2];
							Arrays.fill(buffer, -1);
							int offset = random(0, size/2);
							assertEquals(size, config.set.export(buffer, offset));
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
							assertEquals(size, config.set.export(buffer, 0));
							for (int i = 0; i < buffer.length; i++) {
								assertEquals(config.indices[i], buffer[i]);
							}
						}));
						// Buffer bigger than needed
						tests.add(dynamicTest("spare room", () -> {
							long[] buffer = new long[size*2];
							Arrays.fill(buffer, -1);
							int offset = random(0, size/2);
							assertEquals(size, config.set.export(buffer, offset));
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
		return configurations()
				.map(Config::validate)
				.map(config -> {
					List<DynamicTest> tests = new ArrayList<>();
					int size = config.indices.length;
					if(IndexValueType.BYTE.isValidSubstitute(config.valueType)) {
						// Make target buffer too small
						tests.add(dynamicTest("too small (full)", ioobAsserter(
								() -> config.set.export(0, size, new byte[size/2], 0))));
						tests.add(dynamicTest("too small (part)", ioobAsserter(() -> {
							int from = random(0, size-2);
							int to = random(from+1, size+1);
							config.set.export(from, to, new byte[(to-from)/2], 0);
						})));
						// Normal match
						tests.add(dynamicTest("fitting (full)", () -> {
							byte[] buffer = new byte[size];
							config.set.export(0, size, buffer, 0);
							for (int i = 0; i < buffer.length; i++) {
								assertEquals(config.indices[i], buffer[i]);
							}
						}));
						tests.add(dynamicTest("fitting (part)", () -> {
							int from = random(0, size-2);
							int to = random(from, size+1);
							byte[] buffer = new byte[to-from];
							config.set.export(from, to, buffer, 0);
							for (int i = from; i < to; i++) {
								assertEquals(config.indices[i], buffer[i-from]);
							}
						}));
						// Buffer bigger than needed
						tests.add(dynamicTest("spare room", () -> {
							int from = random(0, size-2);
							int to = random(from+1, size+1);
							byte[] buffer = new byte[(to-from)*2];
							Arrays.fill(buffer, (byte)-1);
							int offset = random(0, buffer.length/2);
							config.set.export(from, to, buffer, offset);
							for (int i = 0; i < buffer.length; i++) {
								if(i<offset || i>=offset+to-from) {
									assertEquals(-1, buffer[i]);
								} else {
									assertEquals(config.indices[i-offset+from], buffer[i]);
								}
							}
						}));
					} else {
						// Export to target type not supported
						tests.add(dynamicTest("overflow", overflowAsserter(
								() -> config.set.export(0, size, new byte[size], 0))));
					}

					return dynamicContainer(config.label, tests);
				});
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(int, int, short[], int)}.
	 */
	@TestFactory
	default Stream<DynamicNode> testExportIntIntShortArrayInt() {
		return configurations()
				.map(Config::validate)
				.map(config -> {
					List<DynamicTest> tests = new ArrayList<>();
					int size = config.indices.length;
					if(IndexValueType.SHORT.isValidSubstitute(config.valueType)) {
						// Make target buffer too small
						tests.add(dynamicTest("too small (full)", ioobAsserter(
								() -> config.set.export(0, size, new short[size/2], 0))));
						tests.add(dynamicTest("too small (part)", ioobAsserter(() -> {
							int from = random(0, size-2);
							int to = random(from+1, size+1);
							config.set.export(from, to, new short[(to-from)/2], 0);
						})));
						// Normal match
						tests.add(dynamicTest("fitting (full)", () -> {
							short[] buffer = new short[size];
							config.set.export(0, size, buffer, 0);
							for (int i = 0; i < buffer.length; i++) {
								assertEquals(config.indices[i], buffer[i]);
							}
						}));
						tests.add(dynamicTest("fitting (part)", () -> {
							int from = random(0, size-2);
							int to = random(from, size+1);
							short[] buffer = new short[to-from];
							config.set.export(from, to, buffer, 0);
							for (int i = from; i < to; i++) {
								assertEquals(config.indices[i], buffer[i-from]);
							}
						}));
						// Buffer bigger than needed
						tests.add(dynamicTest("spare room", () -> {
							int from = random(0, size-2);
							int to = random(from+1, size+1);
							short[] buffer = new short[(to-from)*2];
							Arrays.fill(buffer, (short)-1);
							int offset = random(0, buffer.length/2);
							config.set.export(from, to, buffer, offset);
							for (int i = 0; i < buffer.length; i++) {
								if(i<offset || i>=offset+to-from) {
									assertEquals(-1, buffer[i]);
								} else {
									assertEquals(config.indices[i-offset+from], buffer[i]);
								}
							}
						}));
					} else {
						// Export to target type not supported
						tests.add(dynamicTest("overflow", overflowAsserter(
								() -> config.set.export(0, size, new short[size], 0))));
					}

					return dynamicContainer(config.label, tests);
				});
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(int, int, int[], int)}.
	 */
	@TestFactory
	default Stream<DynamicNode> testExportIntIntIntArrayInt() {
		return configurations()
				.map(Config::validate)
				.map(config -> {
					List<DynamicTest> tests = new ArrayList<>();
					int size = config.indices.length;
					if(IndexValueType.INTEGER.isValidSubstitute(config.valueType)) {
						// Make target buffer too small
						tests.add(dynamicTest("too small (full)", ioobAsserter(
								() -> config.set.export(0, size, new int[size/2], 0))));
						tests.add(dynamicTest("too small (part)", ioobAsserter(() -> {
							int from = random(0, size-2);
							int to = random(from+1, size+1);
							config.set.export(from, to, new int[(to-from)/2], 0);
						})));
						// Normal match
						tests.add(dynamicTest("fitting (full)", () -> {
							int[] buffer = new int[size];
							config.set.export(0, size, buffer, 0);
							for (int i = 0; i < buffer.length; i++) {
								assertEquals(config.indices[i], buffer[i]);
							}
						}));
						tests.add(dynamicTest("fitting (part)", () -> {
							int from = random(0, size-2);
							int to = random(from, size+1);
							int[] buffer = new int[to-from];
							config.set.export(from, to, buffer, 0);
							for (int i = from; i < to; i++) {
								assertEquals(config.indices[i], buffer[i-from]);
							}
						}));
						// Buffer bigger than needed
						tests.add(dynamicTest("spare room", () -> {
							int from = random(0, size-2);
							int to = random(from+1, size+1);
							int[] buffer = new int[(to-from)*2];
							Arrays.fill(buffer, -1);
							int offset = random(0, buffer.length/2);
							config.set.export(from, to, buffer, offset);
							for (int i = 0; i < buffer.length; i++) {
								if(i<offset || i>=offset+to-from) {
									assertEquals(-1, buffer[i]);
								} else {
									assertEquals(config.indices[i-offset+from], buffer[i]);
								}
							}
						}));
					} else {
						// Export to target type not supported
						tests.add(dynamicTest("overflow", overflowAsserter(
								() -> config.set.export(0, size, new int[size], 0))));
					}

					return dynamicContainer(config.label, tests);
				});
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(int, int, long[], int)}.
	 */
	@TestFactory
	default Stream<DynamicNode> testExportIntIntLongArrayInt() {
		return configurations()
				.map(Config::validate)
				.map(config -> {
					List<DynamicTest> tests = new ArrayList<>();
					int size = config.indices.length;
					if(IndexValueType.LONG.isValidSubstitute(config.valueType)) {
						// Make target buffer too small
						tests.add(dynamicTest("too small (full)", ioobAsserter(
								() -> config.set.export(0, size, new long[size/2], 0))));
						tests.add(dynamicTest("too small (part)", ioobAsserter(() -> {
							int from = random(0, size-2);
							int to = random(from+1, size+1);
							config.set.export(from, to, new long[(to-from)/2], 0);
						})));
						// Normal match
						tests.add(dynamicTest("fitting (full)", () -> {
							long[] buffer = new long[size];
							config.set.export(0, size, buffer, 0);
							for (int i = 0; i < buffer.length; i++) {
								assertEquals(config.indices[i], buffer[i]);
							}
						}));
						tests.add(dynamicTest("fitting (part)", () -> {
							int from = random(0, size-2);
							int to = random(from, size+1);
							long[] buffer = new long[to-from];
							config.set.export(from, to, buffer, 0);
							for (int i = from; i < to; i++) {
								assertEquals(config.indices[i], buffer[i-from]);
							}
						}));
						// Buffer bigger than needed
						tests.add(dynamicTest("spare room", () -> {
							int from = random(0, size-2);
							int to = random(from+1, size+1);
							long[] buffer = new long[(to-from)*2];
							Arrays.fill(buffer, -1);
							int offset = random(0, buffer.length/2);
							config.set.export(from, to, buffer, offset);
							for (int i = 0; i < buffer.length; i++) {
								if(i<offset || i>=offset+to-from) {
									assertEquals(-1, buffer[i]);
								} else {
									assertEquals(config.indices[i-offset+from], buffer[i]);
								}
							}
						}));
					} else {
						// Export to target type not supported
						tests.add(dynamicTest("overflow", overflowAsserter(
								() -> config.set.export(0, size, new long[size], 0))));
					}

					return dynamicContainer(config.label, tests);
				});
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachIndex(java.util.function.LongConsumer)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachIndexLongConsumer() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					LongList buffer = new LongArrayList();
					config.set.forEachIndex((LongConsumer)buffer::add);
					assertArrayEquals(config.indices, buffer.toLongArray());
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachIndex(java.util.function.LongConsumer, int, int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachIndexLongConsumerIntInt() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					int size = config.indices.length;
					int from = random(0, size-2);
					int to = random(from+1, size+1);
					LongList buffer = new LongArrayList();
					config.set.forEachIndex((LongConsumer)buffer::add, from, to);
					assertEquals(to-from, buffer.size());
					for (int i = 0; i < buffer.size(); i++) {
						assertEquals(config.indices[i+from], buffer.getLong(i));
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachIndex(java.util.function.IntConsumer)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachIndexIntConsumer() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					if(IndexValueType.INTEGER.isValidSubstitute(config.valueType)) {
						IntList buffer = new IntArrayList();
						config.set.forEachIndex((IntConsumer)buffer::add);
						assertEquals(config.indices.length, buffer.size());
						for (int i = 0; i < config.indices.length; i++) {
							assertEquals(config.indices[i], buffer.getInt(i));
						}
					} else {
						assertOverflow(() -> config.set.forEachIndex((int i) -> noOp()));
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachIndex(java.util.function.IntConsumer, int, int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachIndexIntConsumerIntInt() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					int size = config.indices.length;
					if(IndexValueType.INTEGER.isValidSubstitute(config.valueType)) {
						int from = random(0, size-2);
						int to = random(from+1, size+1);
						LongList buffer = new LongArrayList();
						config.set.forEachIndex((IntConsumer)buffer::add, from, to);
						assertEquals(to-from, buffer.size());
						for (int i = 0; i < buffer.size(); i++) {
							assertEquals(config.indices[i+from], buffer.getLong(i));
						}
					} else {
						assertOverflow(() -> config.set.forEachIndex((int i) -> noOp(), 0, size));
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachEntry(IntLongConsumer)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachEntryIntLongConsumer() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					LongList buffer = new LongArrayList();
					config.set.forEachEntry((IntLongConsumer)buffer::add);
					assertArrayEquals(config.indices, buffer.toLongArray());
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachEntry(IntLongConsumer, int, int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachEntryIntLongConsumerIntInt() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					int size = config.indices.length;
					int from = random(0, size-2);
					int to = random(from+1, size+1);
					LongList buffer = new LongArrayList();
					config.set.forEachEntry((int idx, long val)
							-> buffer.add(idx-from, val), from, to);
					assertEquals(to-from, buffer.size());
					for (int i = 0; i < buffer.size(); i++) {
						assertEquals(config.indices[i+from], buffer.getLong(i));
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachEntry(IntBiConsumer)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachEntryIntBiConsumer() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					if(IndexValueType.INTEGER.isValidSubstitute(config.valueType)) {
						int size = config.indices.length;
						IntList buffer = new IntArrayList();
						config.set.forEachEntry((IntBiConsumer)buffer::add);
						assertEquals(size, buffer.size());
						for (int i = 0; i < buffer.size(); i++) {
							assertEquals(config.indices[i], buffer.getInt(i));
						}
					} else {
						assertOverflow(() -> config.set.forEachEntry((int i, int v) -> noOp()));
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachEntry(IntBiConsumer, int, int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachEntryIntBiConsumerIntInt() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					int size = config.indices.length;
					if(IndexValueType.INTEGER.isValidSubstitute(config.valueType)) {
						int from = random(0, size-2);
						int to = random(from+1, size+1);
						LongList buffer = new LongArrayList();
						config.set.forEachEntry((int idx, int val)
								-> buffer.add(idx-from, val), from, to);
						assertEquals(to-from, buffer.size());
						for (int i = 0; i < buffer.size(); i++) {
							assertEquals(config.indices[i+from], buffer.getLong(i));
						}
					} else {
						assertOverflow(() -> config.set.forEachEntry((int i, int v) -> noOp(), 0, size));
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#checkIndices(java.util.function.LongPredicate)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testCheckIndicesLongPredicate() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					MutableInteger index = new MutableInteger(0);
					assertTrue(config.set.checkIndices((long val)
							-> config.indices[index.getAndIncrement()] == val));
					assertEquals(config.indices.length, index.get());

					assertTrue(config.set.checkIndices((long val) -> true));
					assertFalse(config.set.checkIndices((long val) -> false));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#checkIndices(java.util.function.LongPredicate, int, int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testCheckIndicesLongPredicateIntInt() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					int size = config.indices.length;
					int from = random(0, size-2);
					int to = random(from+1, size+1);
					MutableInteger index = new MutableInteger(from);
					assertTrue(config.set.checkIndices((long val)
							-> config.indices[index.getAndIncrement()] == val, from, to));
					assertEquals(to, index.get());

					assertTrue(config.set.checkIndices((long val) -> true, from, to));
					assertFalse(config.set.checkIndices((long val) -> false, from, to));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#checkIndices(java.util.function.IntPredicate)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testCheckIndicesIntPredicate() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					if(IndexValueType.INTEGER.isValidSubstitute(config.valueType)) {
						MutableInteger index = new MutableInteger(0);
						assertTrue(config.set.checkIndices((int val)
								-> config.indices[index.getAndIncrement()] == val));
						assertEquals(config.indices.length, index.get());

						assertTrue(config.set.checkIndices((int val) -> true));
						assertFalse(config.set.checkIndices((int val) -> false));
					} else {
						assertOverflow(() -> config.set.checkIndices((int i) -> false));
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#checkIndices(java.util.function.IntPredicate, int, int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testCheckIndicesIntPredicateIntInt() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					int size = config.indices.length;
					if(IndexValueType.INTEGER.isValidSubstitute(config.valueType)) {
						int from = random(0, size-2);
						int to = random(from+1, size+1);
						MutableInteger index = new MutableInteger(from);
						assertTrue(config.set.checkIndices((int val)
								-> config.indices[index.getAndIncrement()] == val, from, to));
						assertEquals(to, index.get());

						assertTrue(config.set.checkIndices((int val) -> true, from, to));
						assertFalse(config.set.checkIndices((int val) -> false, from, to));
					} else {
						assertOverflow(() -> config.set.checkIndices((int i) -> false, 0, size));
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#checkConsecutiveIndices(de.ims.icarus2.util.function.LongBiPredicate)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testCheckConsecutiveIndicesLongBiPredicate() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					MutableInteger index = new MutableInteger(1);
					assertTrue(config.set.checkConsecutiveIndices((v0, v1)
							-> config.indices[index.intValue()-1] == v0
							&& config.indices[index.getAndIncrement()] == v1));
					assertEquals(config.indices.length, index.get());

					assertTrue(config.set.checkConsecutiveIndices((v0, v1) -> true));
					assertFalse(config.set.checkConsecutiveIndices((v0, v1) -> false));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#checkConsecutiveIndices(de.ims.icarus2.util.function.LongBiPredicate, int, int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testCheckConsecutiveIndicesLongBiPredicateIntInt() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					int size = config.indices.length;
					int from = random(0, size-2);
					int to = random(from+1, size+1);
					MutableInteger index = new MutableInteger(from+1);
					assertTrue(config.set.checkConsecutiveIndices((v0, v1)
							-> config.indices[index.intValue()-1] == v0
							&& config.indices[index.getAndIncrement()] == v1, from, to));
					assertEquals(to, index.get());

					assertTrue(config.set.checkConsecutiveIndices((v0, v1) -> true));
					assertFalse(config.set.checkConsecutiveIndices((v0, v1) -> false));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#split(int)}.
	 */
	@TestFactory
	default Stream<DynamicNode> testSplit() {
		return configurations()
				.map(Config::validate)
				.map(config -> {
					List<DynamicTest> tests = new ArrayList<>();
					int size = config.indices.length;

					tests.add(dynamicTest("complete", () -> {
						IndexSet[] splits = config.set.split(size);
						assertEquals(1, splits.length);
						assertIndicesEquals(config.set, splits[0]);
					}));

					tests.add(dynamicTest("singletons", () -> {
						IndexSet[] splits = config.set.split(1);
						assertEquals(size, splits.length);
						for (int i = 0; i < size; i++) {
							assertEquals(config.indices[i], splits[i].indexAt(0));
						}
					}));

					tests.add(dynamicTest("r", () -> {
						int chukSize = random(3, size-3);
						IndexSet[] splits = config.set.split(chukSize);
						assertTrue(splits.length>1);
						assertIndicesEquals(wrap(config.set), splits);
					}));

					return dynamicContainer(config.label, tests);
				});
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#subSet(int, int)}.
	 */
	@TestFactory
	default Stream<DynamicNode> testSubSet() {
		return configurations()
				.map(Config::validate)
				.map(config ->  {
					List<DynamicTest> tests = new ArrayList<>();
					int size = config.indices.length;

					tests.add(dynamicTest("complete", () ->
							assertIndicesEquals(config.set,
									config.set.subSet(0, size-1))));

					tests.add(dynamicTest("singleton", () -> {
						int index = random(0, size);
						IndexSet subset = config.set.subSet(index, index);
						assertEquals(1, subset.size());
						assertEquals(config.indices[index], subset.indexAt(0));
					}));

					tests.add(dynamicTest("random", () -> {
						int from = random(0, size-2);
						int to = random(from+1, size);

						IndexSet subset = config.set.subSet(from, to);
						assertEquals(to-from+1, subset.size());
						for(int i=from; i<=to; i++) {
							assertEquals(config.indices[i], subset.indexAt(i-from));
						}
					}));

					return dynamicContainer(config.label, tests);
				});
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#iterator()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIterator() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					OfLong iterator = config.set.iterator();
					for(long val : config.indices) {
						assertTrue(iterator.hasNext());
						assertEquals(val, iterator.nextLong());
					}
					assertFalse(iterator.hasNext());
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#iterator(int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIteratorInt() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					int size = config.indices.length;
					int start = random(0, size-1);
					OfLong iterator = config.set.iterator(start);
					for(int i = start; i<size; i++) {
						assertTrue(iterator.hasNext());
						assertEquals(config.indices[i], iterator.nextLong());
					}
					assertFalse(iterator.hasNext());
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#iterator(int, int)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIteratorIntInt() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					int size = config.indices.length;
					int from = random(0, size-2);
					int to = random(from+1, size+1);
					OfLong iterator = config.set.iterator(from, to);
					for(int i = from; i<to; i++) {
						assertTrue(iterator.hasNext());
						assertEquals(config.indices[i], iterator.nextLong());
					}
					assertFalse(iterator.hasNext());
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#intStream()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIntStream() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					if(IndexValueType.INTEGER.isValidSubstitute(config.valueType)) {
						assertArrayEquals(config.indices,
								config.set.intStream().asLongStream().toArray());
					} else {
						assertOverflow(() -> config.set.intStream().toArray());
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#longStream()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testLongStream() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					assertArrayEquals(config.indices, config.set.longStream().toArray());
				}));
	}
}
