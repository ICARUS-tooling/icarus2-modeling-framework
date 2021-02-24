/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.driver.indices;

import static de.ims.icarus2.SharedTestUtils.assertIcarusException;
import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.test.TestUtils.other;
import static de.ims.icarus2.util.IcarusUtils.notEq;
import static de.ims.icarus2.util.function.FunctionUtils.not;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives.strictToByte;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static de.ims.icarus2.util.lang.Primitives.strictToShort;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.lang.reflect.Array;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.function.BiFunction;
import java.util.function.ObjLongConsumer;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.ThrowingConsumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
class IndexValueTypeTest {

	static Stream<IndexValueType> types() {
		return Stream.of(IndexValueType.values());
	}

	static Stream<DynamicNode> typeTests(ThrowingConsumer<IndexValueType> task) {
		return types().map(type -> dynamicTest(type.name(), () -> task.accept(type)));
	}

	static Stream<DynamicNode> typeTests(Predicate<? super IndexValueType> filter,
			ThrowingConsumer<IndexValueType> task) {
		return types()
				.filter(filter)
				.map(type -> dynamicTest(type.name(), () -> task.accept(type)));
	}

	static Stream<DynamicNode> valueTests(
			RandomGenerator rng,
			BiFunction<RandomGenerator, IndexValueType, LongStream> valueGen,
			ObjLongConsumer<IndexValueType> task) {
		return types().map(type -> dynamicContainer(type.name(), valueGen.apply(rng, type).mapToObj(
				value -> dynamicTest(String.valueOf(value), () -> task.accept(type, value)))));
	}

	static Object array(IndexValueType type) {
		return Array.newInstance(type.getValueClass(), 1);
	}

	static LongStream legal(RandomGenerator rng, IndexValueType type) {
		return LongStream.concat(LongStream.of(0, type.maxValue()),
				LongStream.generate(() -> rng.random(1, type.maxValue())).limit(5));
	}

	static LongStream invalid(RandomGenerator rng, IndexValueType type) {
		return LongStream.of(-1, Long.MIN_VALUE);
	}

	static LongStream overflow(RandomGenerator rng, IndexValueType type) {
		return LongStream.of(type.maxValue()+1, Long.MAX_VALUE);
	}

	static Object randomArray(RandomGenerator rng, IndexValueType type, int size) {
		Object array = type.newArray(size);
		for(int i=0; i<size; i++) {
			type.set(array, i, rng.random(0, type.maxValue()));
		}
		return array;
	}

	@Nested
	class ForFactory {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#parseIndexValueType(java.lang.String)}.
		 */
		@TestFactory
		Stream<DynamicNode> testParseIndexValueType() {
			return typeTests(type -> assertSame(type, IndexValueType.parseIndexValueType(
					type.getStringValue())));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#forValue(long)}.
		 */
		@TestFactory
		Stream<DynamicNode> testForValue() {
			return typeTests(type -> assertSame(type, IndexValueType.forValue(type.maxValue())));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#forArray(java.lang.Object)}.
		 */
		@TestFactory
		Stream<DynamicNode> testForArray() {
			return typeTests(type -> assertSame(type, IndexValueType.forArray(array(type))));
		}

	}

	@Nested
	class ForProperties {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#getStringValue()}.
		 */
		@TestFactory
		Stream<DynamicNode> testGetStringValue() {
			return typeTests(type -> assertNotNull(type.getStringValue()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#getValueClass()}.
		 */
		@TestFactory
		Stream<DynamicNode> testGetValueClass() {
			return typeTests(type -> assertNotNull(type.getValueClass()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#getArrayClass()}.
		 */
		@TestFactory
		Stream<DynamicNode> testGetArrayClass() {
			return typeTests(type -> assertNotNull(type.getArrayClass()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#isValidBuffer(java.lang.Object)}.
		 */
		@TestFactory
		Stream<DynamicNode> testIsValidBuffer() {
			return typeTests(type -> assertTrue(type.isValidBuffer(array(type))));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#isValidBuffer(java.lang.Object)}.
		 */
		@TestFactory
		Stream<DynamicNode> testIsValidBufferForeign() {
			return typeTests(type -> assertFalse(type.isValidBuffer(array(other(type)))));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#isValidBuffer(java.lang.Object)}.
		 */
		@TestFactory
		Stream<DynamicNode> testIsValidBufferNonNumeric() {
			return typeTests(type -> assertFalse(type.isValidBuffer(new Object[1])));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#bytesPerValue()}.
		 */
		@TestFactory
		Stream<DynamicNode> testBytesPerValue() {
			return typeTests(type -> assertThat(type.bytesPerValue()).isGreaterThan(0));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#isValidSubstitute(de.ims.icarus2.model.api.driver.indices.IndexValueType)}.
		 */
		@TestFactory
		Stream<DynamicNode> testIsValidSubstitute() {
			return typeTests(type -> {
				// self
				assertThat(type.isValidSubstitute(type)).isTrue();
				// compatible
				IndexValueType lesser = type;
				while((lesser = lesser.smaller())!=null) {
					assertThat(type.isValidSubstitute(lesser)).isTrue();
				}
				//incompatible
				IndexValueType larger = type;
				while((larger = larger.larger())!=null) {
					assertThat(type.isValidSubstitute(larger)).isFalse();
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#smaller()}.
		 */
		@Test
		void testSmaller() {
			assertThat(IndexValueType.BYTE.smaller()).isNull();
			assertThat(IndexValueType.SHORT.smaller()).isSameAs(IndexValueType.BYTE);
			assertThat(IndexValueType.INTEGER.smaller()).isSameAs(IndexValueType.SHORT);
			assertThat(IndexValueType.LONG.smaller()).isSameAs(IndexValueType.INTEGER);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#larger()}.
		 */
		@Test
		void testLarger() {
			assertThat(IndexValueType.LONG.larger()).isNull();
			assertThat(IndexValueType.INTEGER.larger()).isSameAs(IndexValueType.LONG);
			assertThat(IndexValueType.SHORT.larger()).isSameAs(IndexValueType.INTEGER);
			assertThat(IndexValueType.BYTE.larger()).isSameAs(IndexValueType.SHORT);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#maxValue()}.
		 */
		@TestFactory
		Stream<DynamicNode> testMaxValue() {
			return typeTests(type -> assertThat(type.maxValue()).isGreaterThan(0L));
		}

	}

	@Nested
	class Verification {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#checkBuffer(java.lang.Object)}.
		 */
		@TestFactory
		Stream<DynamicNode> testCheckBufferSuccess() {
			return typeTests(type -> type.checkBuffer(array(type)));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#checkBuffer(java.lang.Object)}.
		 */
		@TestFactory
		Stream<DynamicNode> testCheckBufferForeign() {
			return typeTests(type -> assertModelException(GlobalErrorCode.INVALID_INPUT, () -> type.checkBuffer(array(other(type)))));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#checkValue(long)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testCheckValueSuccess(RandomGenerator rng) {
			return valueTests(rng, IndexValueTypeTest::legal,
					(type, value) -> type.checkValue(value));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#checkValue(long)}.
		 */
		@TestFactory
		Stream<DynamicNode> testCheckValueInvalidInput() {
			return valueTests(null, IndexValueTypeTest::invalid, (type, value) -> assertModelException(
									GlobalErrorCode.INVALID_INPUT, () -> type.checkValue(value)));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#checkValue(long)}.
		 */
		@TestFactory
		Stream<DynamicNode> testCheckValueOverflow() {
			return types()
					.filter(notEq(IndexValueType.LONG))
					.map(type -> dynamicContainer(type.name(),
					overflow(null, type).mapToObj(value ->
							dynamicTest(String.valueOf(value), () -> assertModelException(
									GlobalErrorCode.VALUE_OVERFLOW, () -> type.checkValue(value))))));
		}

	}

	@Nested
	class ReadWriteMethods {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#get(java.nio.ByteBuffer)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testGetByteBufferSingle(RandomGenerator rng) {
			return valueTests(rng, IndexValueTypeTest::legal, (type, value) -> {
				ByteBuffer bb = ByteBuffer.allocate(type.bytesPerValue());
				type.set(bb, value);
				bb.flip();
				assertThat(type.get(bb)).isEqualTo(value);
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#get(java.nio.ByteBuffer)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testGetByteBufferBatch(RandomGenerator rng) {
			return valueTests(rng, IndexValueTypeTest::legal, (type, value) -> {
				int count = rng.random(5, 20);
				ByteBuffer bb = ByteBuffer.allocate(type.bytesPerValue()*count);
				for (int i = 0; i < count; i++) {
					type.set(bb, value);
				}
				bb.flip();
				for (int i = 0; i < count; i++) {
					assertThat(type.get(bb)).isEqualTo(value);
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#get(java.nio.ByteBuffer)}.
		 */
		@TestFactory
		Stream<DynamicNode> testGetByteBufferUnderflow() {
			return typeTests(type -> assertThrows(BufferUnderflowException.class,
					() -> type.get(ByteBuffer.allocate(0))));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#binarySearch(java.lang.Object, long, int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testBinarySearch(RandomGenerator rng) {
			return typeTests(type -> {
				int size = rng.random(10, 100);
				Object array = type.newArray(size);
				// Fill array
				for (int i = 0; i < size; i++) {
					type.set(array, i, i);
				}
				// Search array
				for (int i = 0; i < size; i++) {
					assertThat(type.binarySearch(array, i, 0, size)).isEqualTo(i);
				}

				assertThat(type.binarySearch(array, -1, 0, size)).isLessThan(0);
				assertThat(type.binarySearch(array, size, 0, size)).isLessThan(0);
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#length(java.lang.Object)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testLength(RandomGenerator rng) {
			return typeTests(type -> {
				int size = rng.random(1, 100);
				Object array = Array.newInstance(type.getValueClass(), size);
				assertThat(type.length(array)).isEqualTo(size);
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#get(java.lang.Object, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testGetObjectInt(RandomGenerator rng) {
			return valueTests(rng, IndexValueTypeTest::legal, (type, value) -> {
				int size = rng.random(5, 20);
				Object array = Array.newInstance(type.getValueClass(), size);
				for (int i = 0; i < size; i++) {
					type.set(array, i, value);
				}
				for (int i = 0; i < size; i++) {
					assertThat(type.get(array, i)).isEqualTo(value);
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#set(java.nio.ByteBuffer, long)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testSetByteBufferLong(RandomGenerator rng) {
			return valueTests(rng, IndexValueTypeTest::legal, (type, value) ->
					type.set(ByteBuffer.allocate(type.bytesPerValue()), value));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#set(java.nio.ByteBuffer, long)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testSetByteBufferLongOverflow(RandomGenerator rng) {
			return types()
					.filter(notEq(IndexValueType.LONG))
					.map(type -> dynamicContainer(type.name(),
					overflow(rng, type).mapToObj(value ->
							dynamicTest(String.valueOf(value), () -> assertIcarusException(
									GlobalErrorCode.VALUE_OVERFLOW,
									() -> type.set(ByteBuffer.allocate(type.bytesPerValue()), value))))));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#sort(java.lang.Object, int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testSort(RandomGenerator rng) {
			return typeTests(type -> {
				Object array = type.newArray(10);
				// Create reverse sorted array
				for (int i = 0; i < Array.getLength(array); i++) {
					type.set(array, i, 10-i-1);
				}
				int from = rng.random(0, 4);
				int to = rng.random(6, 10);
				// Sort random range
				type.sort(array, from, to);
				// Verify
				for (int i = 0; i < Array.getLength(array); i++) {
					if(i<from || i>=to) {
						assertThat(type.get(array, i)).isEqualTo(10-i-1);
					} else if(i==from) {
						assertThat(type.get(array, i)).isEqualTo(10-to);
					} else {
						assertThat(type.get(array, i)).isEqualTo(type.get(array, i-1) + 1);
					}
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#fill(java.lang.Object, long, int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testFillObjectLongIntInt(RandomGenerator rng) {
			return valueTests(rng, IndexValueTypeTest::legal, (type, value) -> {
				Object array = type.newArray(25);
				// Reset all values to something "invalid"
				type.fill(array, -1L);
				int from = rng.random(0, 5);
				int to = rng.random(10, 25);
				// Fill random range
				type.fill(array, value, from, to);
				// Verify
				for (int i = 0; i < Array.getLength(array); i++) {
					long expected = i<from || i>=to ? -1L : value;
					assertThat(type.get(array, i)).isEqualTo(expected);
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#fill(java.lang.Object, long)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testFillObjectLong(RandomGenerator rng) {
			return valueTests(rng, IndexValueTypeTest::legal, (type, value) -> {
				Object array = type.newArray(10);
				type.fill(array, value);
				for (int i = 0; i < Array.getLength(array); i++) {
					assertThat(type.get(array, i)).isEqualTo(value);
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#set(java.lang.Object, int, long)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testSetObjectIntLong(RandomGenerator rng) {
			return valueTests(rng, IndexValueTypeTest::legal,
					(type, value) -> type.set(ByteBuffer.allocate(type.bytesPerValue()), value));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#newArray(int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testNewArray(RandomGenerator rng) {
			return typeTests(type -> {
				int size = rng.random(1, 100);
				Object array = type.newArray(size);
				assertThat(Array.getLength(array)).isEqualTo(size);
			});
		}

	}

	@Nested
	class CopyMethods {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#copyOf(java.lang.Object, int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testCopyOf(RandomGenerator rng) {
			return typeTests(type -> {
				int size = rng.random(5, 25);
				Object array = randomArray(rng, type, size);
				int pos = rng.random(0, size/2);
				int len = rng.random(0, size/2);
				// Create slice
				Object slice = type.copyOf(array, pos, len);
				// Verify
				for (int i = 0; i < len; i++) {
					assertThat(type.get(slice, i)).as("Mismatch at index %d", _int(i))
						.isEqualTo(type.get(array, pos+i));
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#copyTo(java.lang.Object, int, byte[], int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testCopyToObjectIntByteArrayIntInt(RandomGenerator rng) {
			return typeTests(IndexValueType.BYTE::isValidSubstitute, type -> {
				int size = rng.random(5, 25);
				Object array = randomArray(rng, type, size);
				byte[] desto = new byte[size*2];
				int len = rng.random(0, size/2);
				int sourcePos = rng.random(0, size-len);
				int destPos = rng.random(0, desto.length-len);
				// Copy region
				type.copyTo(array, sourcePos, desto, destPos, len);
				// Verify
				for (int i = 0; i < len; i++) {
					assertThat(desto[destPos+i]).as("Mismatch at index %d", _int(i))
						.isEqualTo(strictToByte(type.get(array, sourcePos+i)));
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#copyTo(java.lang.Object, int, short[], int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testCopyToObjectIntShortArrayIntInt(RandomGenerator rng) {
			return typeTests(IndexValueType.SHORT::isValidSubstitute, type -> {
				int size = rng.random(5, 25);
				Object array = randomArray(rng, type, size);
				short[] desto = new short[size*2];
				int len = rng.random(0, size/2);
				int sourcePos = rng.random(0, size-len);
				int destPos = rng.random(0, desto.length-len);
				// Copy region
				type.copyTo(array, sourcePos, desto, destPos, len);
				// Verify
				for (int i = 0; i < len; i++) {
					assertThat(desto[destPos+i]).as("Mismatch at index %d", _int(i))
						.isEqualTo(strictToShort(type.get(array, sourcePos+i)));
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#copyTo(java.lang.Object, int, int[], int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testCopyToObjectIntIntArrayIntInt(RandomGenerator rng) {
			return typeTests(IndexValueType.INTEGER::isValidSubstitute, type -> {
				int size = rng.random(5, 25);
				Object array = randomArray(rng, type, size);
				int[] desto = new int[size*2];
				int len = rng.random(0, size/2);
				int sourcePos = rng.random(0, size-len);
				int destPos = rng.random(0, desto.length-len);
				// Copy region
				type.copyTo(array, sourcePos, desto, destPos, len);
				// Verify
				for (int i = 0; i < len; i++) {
					assertThat(desto[destPos+i]).as("Mismatch at index %d", _int(i))
						.isEqualTo(strictToInt(type.get(array, sourcePos+i)));
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#copyTo(java.lang.Object, int, long[], int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testCopyToObjectIntLongArrayIntInt(RandomGenerator rng) {
			return typeTests(type -> {
				int size = rng.random(5, 25);
				Object array = randomArray(rng, type, size);
				long[] desto = new long[size*2];
				int len = rng.random(0, size/2);
				int sourcePos = rng.random(0, size-len);
				int destPos = rng.random(0, desto.length-len);
				// Copy region
				type.copyTo(array, sourcePos, desto, destPos, len);
				// Verify
				for (int i = 0; i < len; i++) {
					assertThat(desto[destPos+i]).as("Mismatch at index %d", _int(i))
						.isEqualTo(type.get(array, sourcePos+i));
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#copyTo(java.lang.Object, int, java.util.function.LongBinaryOperator, int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testCopyToObjectIntLongBinaryOperatorIntInt(RandomGenerator rng) {
			return typeTests(type -> {
				int size = rng.random(5, 25);
				Object array = randomArray(rng, type, size);
				long[] desto = new long[size*2];
				int len = rng.random(0, size/2);
				int sourcePos = rng.random(0, size-len);
				int destPos = rng.random(0, desto.length-len);
				// Copy region
				type.copyTo(array, sourcePos, (idx, val) -> desto[strictToInt(idx)] = val, destPos, len);
				// Verify
				for (int i = 0; i < len; i++) {
					assertThat(desto[destPos+i]).as("Mismatch at index %d", _int(i))
						.isEqualTo(type.get(array, sourcePos+i));
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#copyFrom(byte[], int, java.lang.Object, int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testCopyFromByteArrayIntObjectIntInt(RandomGenerator rng) {
			return typeTests(type -> {
				int size = rng.random(5, 25);
				Object array = randomArray(rng, type, size);
				byte[] source = rng.randomBytes(size*2, (byte)0, Byte.MAX_VALUE);
				int len = rng.random(0, size/2);
				int sourcePos = rng.random(0, source.length-len);
				int destPos = rng.random(0, size-len);
				// Copy region
				type.copyFrom(source, sourcePos, array, destPos, len);
				// Verify
				for (int i = 0; i < len; i++) {
					assertThat(strictToByte(type.get(array, destPos+i)))
						.as("Mismatch at index %d", _int(i))
						.isEqualTo(source[sourcePos+i]);
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#copyFrom(short[], int, java.lang.Object, int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testCopyFromShortArrayIntObjectIntInt(RandomGenerator rng) {
			return typeTests(not(IndexValueType.BYTE::isValidSubstitute), type -> {
				int size = rng.random(5, 25);
				Object array = randomArray(rng, type, size);
				short[] source = rng.randomShorts(size*2, (short)0, Short.MAX_VALUE);
				int len = rng.random(0, size/2);
				int sourcePos = rng.random(0, source.length-len);
				int destPos = rng.random(0, size-len);
				// Copy region
				type.copyFrom(source, sourcePos, array, destPos, len);
				// Verify
				for (int i = 0; i < len; i++) {
					assertThat(strictToShort(type.get(array, destPos+i)))
						.as("Mismatch at index %d", _int(i))
						.isEqualTo(source[sourcePos+i]);
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#copyFrom(int[], int, java.lang.Object, int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testCopyFromIntArrayIntObjectIntInt(RandomGenerator rng) {
			return typeTests(not(IndexValueType.INTEGER::isValidSubstitute), type -> {
				int size = rng.random(5, 25);
				Object array = randomArray(rng, type, size);
				int[] source = rng.randomInts(size*2, 0, Integer.MAX_VALUE);
				int len = rng.random(0, size/2);
				int sourcePos = rng.random(0, source.length-len);
				int destPos = rng.random(0, size-len);
				// Copy region
				type.copyFrom(source, sourcePos, array, destPos, len);
				// Verify
				for (int i = 0; i < len; i++) {
					assertThat(strictToInt(type.get(array, destPos+i)))
						.as("Mismatch at index %d", _int(i))
						.isEqualTo(source[sourcePos+i]);
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#copyFrom(long[], int, java.lang.Object, int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testCopyFromLongArrayIntObjectIntInt(RandomGenerator rng) {
			return typeTests(not(IndexValueType.LONG::isValidSubstitute), type -> {
				int size = rng.random(5, 25);
				Object array = randomArray(rng, type, size);
				long[] source = rng.randomLongs(size*2, 0, Long.MAX_VALUE);
				int len = rng.random(0, size/2);
				int sourcePos = rng.random(0, source.length-len);
				int destPos = rng.random(0, size-len);
				// Copy region
				type.copyFrom(source, sourcePos, array, destPos, len);
				// Verify
				for (int i = 0; i < len; i++) {
					assertThat(type.get(array, destPos+i))
						.as("Mismatch at index %d", _int(i))
						.isEqualTo(source[sourcePos+i]);
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexValueType#copyFrom(java.util.function.IntToLongFunction, int, java.lang.Object, int, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testCopyFromIntToLongFunctionIntObjectIntInt(RandomGenerator rng) {
			return typeTests(type -> {
				int size = rng.random(5, 25);
				Object array = randomArray(rng, type, size);
				long[] source = rng.randomLongs(size*2, 0, type.maxValue());
				int len = rng.random(0, size/2);
				int sourcePos = rng.random(0, source.length-len);
				int destPos = rng.random(0, size-len);
				// Copy region
				type.copyFrom(idx -> source[idx], sourcePos, array, destPos, len);
				// Verify
				for (int i = 0; i < len; i++) {
					assertThat(type.get(array, destPos+i))
						.as("Mismatch at index %d", _int(i))
						.isEqualTo(source[sourcePos+i]);
				}
			});
		}

	}

}
