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
package de.ims.icarus2.query.api.exp;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Nested;

import de.ims.icarus2.query.api.exp.ArrayLiterals;
import de.ims.icarus2.query.api.exp.TypeInfo;
import de.ims.icarus2.query.api.exp.ArrayLiterals.BooleanArray;
import de.ims.icarus2.query.api.exp.ArrayLiterals.ByteArray;
import de.ims.icarus2.query.api.exp.ArrayLiterals.DelegatingIntArray;
import de.ims.icarus2.query.api.exp.ArrayLiterals.DoubleArray;
import de.ims.icarus2.query.api.exp.ArrayLiterals.FloatArray;
import de.ims.icarus2.query.api.exp.ArrayLiterals.IntArray;
import de.ims.icarus2.query.api.exp.ArrayLiterals.LongArray;
import de.ims.icarus2.query.api.exp.ArrayLiterals.ObjectArray;
import de.ims.icarus2.query.api.exp.ArrayLiterals.ShortArray;
import de.ims.icarus2.query.api.exp.Expression.ListExpression;
import de.ims.icarus2.query.api.exp.ExpressionTest.ListExpressionTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.MutablePrimitives.Primitive;

/**
 * @author Markus Gärtner
 *
 */
class ArrayLiteralsTest {

	@Nested
	class ForObjectArray implements ListExpressionTest<String[], String> {

		@Override
		public String[] constant() {
			return new String[]{"test", "x", CodePointUtilsTest.test_mixed};
		}

		@Override
		public String[] random(RandomGenerator rng) {
			return IntStream.range(0, 7)
					.mapToObj(i -> rng.randomUnicodeString(10+i))
					.toArray(String[]::new);
		}

		@Override
		public String[] sized(int size) {
			return IntStream.range(0, size)
					.mapToObj(i -> "item_"+i)
					.toArray(String[]::new);
		}

		@Override
		public TypeInfo getExpectedType() { return TypeInfo.of(String[].class, true); }

		@Override
		public boolean nativeConstant() { return true; }

		@Override
		public Class<?> getTestTargetClass() { return ObjectArray.class; }

		@Override
		public TypeInfo getExpectedElementType() { return TypeInfo.of(String.class); }

		@Override
		public ListExpression<String[], String> createWithValue(String[] value) {
			return ArrayLiterals.ofGeneric(value);
		}

	}

	@Nested
	class ForByteArray implements ListExpressionTest<byte[], Primitive<Long>> {

		@Override
		public byte[] constant() {
			return new byte[]{0, 1, 10, -100, Byte.MAX_VALUE};
		}

		@Override
		public byte[] random(RandomGenerator rng) {
			byte[] b = new byte[10];
			rng.nextBytes(b);
			return b;
		}

		@Override
		public byte[] sized(int size) {
			byte[] b = new byte[size];
			for (int i = 0; i < b.length; i++) {
				b[i] = (byte)i;
			}
			return b;
		}

		@Override
		public TypeInfo getExpectedType() { return TypeInfo.of(byte[].class, true); }

		@Override
		public boolean nativeConstant() { return true; }

		@Override
		public Class<?> getTestTargetClass() { return ByteArray.class; }

		@Override
		public TypeInfo getExpectedElementType() { return TypeInfo.INTEGER; }

		@Override
		public ListExpression<byte[], Primitive<Long>> createWithValue(byte[] value) {
			return ArrayLiterals.of(value);
		}

	}

	@Nested
	class ForShortArray implements ListExpressionTest<short[], Primitive<Long>> {

		@Override
		public short[] constant() {
			return new short[]{0, 1, 10, -100, Short.MAX_VALUE};
		}

		@Override
		public short[] random(RandomGenerator rng) {
			short[] b = new short[10];
			for (int i = 0; i < b.length; i++) {
				b[i] = (short)rng.nextInt();
			}
			return b;
		}

		@Override
		public short[] sized(int size) {
			short[] b = new short[size];
			for (int i = 0; i < b.length; i++) {
				b[i] = (short)i;
			}
			return b;
		}

		@Override
		public TypeInfo getExpectedType() { return TypeInfo.of(short[].class, true); }

		@Override
		public boolean nativeConstant() { return true; }

		@Override
		public Class<?> getTestTargetClass() { return ShortArray.class; }

		@Override
		public TypeInfo getExpectedElementType() { return TypeInfo.INTEGER; }

		@Override
		public ListExpression<short[], Primitive<Long>> createWithValue(short[] value) {
			return ArrayLiterals.of(value);
		}

	}

	@Nested
	class ForIntArray implements ListExpressionTest<int[], Primitive<Long>> {

		@Override
		public int[] constant() {
			return new int[]{0, 1, 10, -100, Integer.MAX_VALUE};
		}

		@Override
		public int[] random(RandomGenerator rng) {
			int[] b = new int[10];
			for (int i = 0; i < b.length; i++) {
				b[i] = rng.nextInt();
			}
			return b;
		}

		@Override
		public int[] sized(int size) {
			int[] b = new int[size];
			for (int i = 0; i < b.length; i++) {
				b[i] = i;
			}
			return b;
		}

		@Override
		public TypeInfo getExpectedType() { return TypeInfo.of(int[].class, true); }

		@Override
		public boolean nativeConstant() { return true; }

		@Override
		public Class<?> getTestTargetClass() { return IntArray.class; }

		@Override
		public TypeInfo getExpectedElementType() { return TypeInfo.INTEGER; }

		@Override
		public ListExpression<int[], Primitive<Long>> createWithValue(int[] value) {
			return ArrayLiterals.of(value);
		}

	}

	@Nested
	class ForLongArray implements ListExpressionTest<long[], Primitive<Long>> {

		@Override
		public long[] constant() {
			return new long[]{0, 1, 10, -100, Integer.MAX_VALUE};
		}

		@Override
		public long[] random(RandomGenerator rng) {
			long[] b = new long[10];
			for (int i = 0; i < b.length; i++) {
				b[i] = rng.nextLong();
			}
			return b;
		}

		@Override
		public long[] sized(int size) {
			long[] b = new long[size];
			for (int i = 0; i < b.length; i++) {
				b[i] = i;
			}
			return b;
		}

		@Override
		public TypeInfo getExpectedType() { return TypeInfo.of(long[].class, true); }

		@Override
		public boolean nativeConstant() { return true; }

		@Override
		public Class<?> getTestTargetClass() { return LongArray.class; }

		@Override
		public TypeInfo getExpectedElementType() { return TypeInfo.INTEGER; }

		@Override
		public ListExpression<long[], Primitive<Long>> createWithValue(long[] value) {
			return ArrayLiterals.of(value);
		}

	}

	@Nested
	class ForFloatArray implements ListExpressionTest<float[], Primitive<Double>> {

		@Override
		public float[] constant() {
			return new float[]{0.1F, 1.01F, 10.456F, -100.5F, Float.MAX_VALUE};
		}

		@Override
		public float[] random(RandomGenerator rng) {
			float[] b = new float[10];
			for (int i = 0; i < b.length; i++) {
				b[i] = rng.nextFloat();
			}
			return b;
		}

		@Override
		public float[] sized(int size) {
			float[] b = new float[size];
			for (int i = 0; i < b.length; i++) {
				b[i] = i+0.5F;
			}
			return b;
		}

		@Override
		public TypeInfo getExpectedType() { return TypeInfo.of(float[].class, true); }

		@Override
		public boolean nativeConstant() { return true; }

		@Override
		public Class<?> getTestTargetClass() { return FloatArray.class; }

		@Override
		public TypeInfo getExpectedElementType() { return TypeInfo.FLOATING_POINT; }

		@Override
		public ListExpression<float[], Primitive<Double>> createWithValue(float[] value) {
			return ArrayLiterals.of(value);
		}

	}

	@Nested
	class ForDoubleArray implements ListExpressionTest<double[], Primitive<Double>> {

		@Override
		public double[] constant() {
			return new double[]{0.1, 1.01, 10.456, -100.5, Double.MAX_VALUE};
		}

		@Override
		public double[] random(RandomGenerator rng) {
			double[] b = new double[10];
			for (int i = 0; i < b.length; i++) {
				b[i] = rng.nextDouble();
			}
			return b;
		}

		@Override
		public double[] sized(int size) {
			double[] b = new double[size];
			for (int i = 0; i < b.length; i++) {
				b[i] = i+0.5;
			}
			return b;
		}

		@Override
		public TypeInfo getExpectedType() { return TypeInfo.of(double[].class, true); }

		@Override
		public boolean nativeConstant() { return true; }

		@Override
		public Class<?> getTestTargetClass() { return DoubleArray.class; }

		@Override
		public TypeInfo getExpectedElementType() { return TypeInfo.FLOATING_POINT; }

		@Override
		public ListExpression<double[], Primitive<Double>> createWithValue(double[] value) {
			return ArrayLiterals.of(value);
		}

	}

	@Nested
	class ForBooleanArray implements ListExpressionTest<boolean[], Primitive<Boolean>> {

		@Override
		public boolean[] constant() {
			return new boolean[]{true, false, true, true, false};
		}

		@Override
		public boolean[] random(RandomGenerator rng) {
			boolean[] b = new boolean[10];
			for (int i = 0; i < b.length; i++) {
				b[i] = rng.nextBoolean();
			}
			return b;
		}

		@Override
		public boolean[] sized(int size) {
			boolean[] b = new boolean[size];
			for (int i = 0; i < b.length; i++) {
				b[i] = i%2==0;
			}
			return b;
		}

		@Override
		public TypeInfo getExpectedType() { return TypeInfo.of(boolean[].class, true); }

		@Override
		public boolean nativeConstant() { return true; }

		@Override
		public Class<?> getTestTargetClass() { return BooleanArray.class; }

		@Override
		public TypeInfo getExpectedElementType() { return TypeInfo.BOOLEAN; }

		@Override
		public ListExpression<boolean[], Primitive<Boolean>> createWithValue(boolean[] value) {
			return ArrayLiterals.of(value);
		}

	}

	@Nested
	class ForDelegatingIntArray implements ListExpressionTest<int[], Primitive<Long>> {

		@Override
		public int[] constant() {
			return new int[]{0, 1, 10, -100, Integer.MAX_VALUE};
		}

		@Override
		public int[] random(RandomGenerator rng) {
			int[] b = new int[10];
			for (int i = 0; i < b.length; i++) {
				b[i] = rng.nextInt();
			}
			return b;
		}

		@Override
		public int[] sized(int size) {
			int[] b = new int[size];
			for (int i = 0; i < b.length; i++) {
				b[i] = i;
			}
			return b;
		}

		@Override
		public TypeInfo getExpectedType() { return TypeInfo.of(int[].class, true); }

		@Override
		public boolean nativeConstant() { return true; }

		@Override
		public boolean expectFixedSize() { return false; }

		@Override
		public Class<?> getTestTargetClass() { return DelegatingIntArray.class; }

		@Override
		public TypeInfo getExpectedElementType() { return TypeInfo.INTEGER; }

		@Override
		public ListExpression<int[], Primitive<Long>> createWithValue(int[] value) {
			return ArrayLiterals.of(() -> value);
		}

		//TODO add tests to verify actual delegation
	}
}
