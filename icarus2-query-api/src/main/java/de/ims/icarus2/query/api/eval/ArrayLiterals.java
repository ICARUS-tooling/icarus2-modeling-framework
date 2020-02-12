/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.eval;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import de.ims.icarus2.query.api.eval.Expression.BooleanListExpression;
import de.ims.icarus2.query.api.eval.Expression.FloatingPointListExpression;
import de.ims.icarus2.query.api.eval.Expression.IntegerListExpression;
import de.ims.icarus2.query.api.eval.Expression.ListExpression;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.MutableDouble;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.Primitive;

/**
 * @author Markus Gärtner
 *
 */
public final class ArrayLiterals {

	private ArrayLiterals() { /* no-op */ }

	public static IntegerListExpression<long[]> of(long...array) {
		return new LongArray(array);
	}

	public static IntegerListExpression<int[]> of(int...array) {
		return new IntArray(array);
	}

	public static IntegerListExpression<short[]> of(short...array) {
		return new ShortArray(array);
	}

	public static IntegerListExpression<byte[]> of(byte...array) {
		return new ByteArray(array);
	}

	public static FloatingPointListExpression<double[]> of(double...array) {
		return new DoubleArray(array);
	}

	public static BooleanListExpression<boolean[]> of(boolean...array) {
		return new BooleanArray(array);
	}

	public static <E> ListExpression<E[], E> ofGeneric(
			@SuppressWarnings("unchecked") E...array) {
		return new ObjectArray<>(array);
	}

	public static IntegerListExpression<int[]> of(Supplier<int[]> source) {
		return new DelegatingIntArray(source);
	}

	static abstract class FixedArray<T, E> implements ListExpression<T, E> {

		@Override
		public boolean isConstant() { return true; }

		@Override
		public Expression<T> duplicate(EvaluationContext context) { return this; }

		@Override
		public boolean isFixedSize() { return true; }
	}

	static final class ObjectArray<E> extends FixedArray<E[], E> {

		private final TypeInfo listType, elementType;

		private final E[] array;

		ObjectArray(E[] array) {
			this.array = requireNonNull(array);
			listType = TypeInfo.of(array.getClass(), true);
			elementType = TypeInfo.of(array.getClass().getComponentType());
		}

		@Override
		public TypeInfo getResultType() { return listType; }

		@Override
		public TypeInfo getElementType() { return elementType; }

		@Override
		public int size() { return array.length; }

		@Override
		public E get(int index) { return array[index]; }

		@Override
		public E[] compute() { return array; }
	}

	static final class LongArray extends FixedArray<long[], Primitive<Long>>
			implements IntegerListExpression<long[]> {

		private static final TypeInfo listType = TypeInfo.of(long[].class, true);

		private final long[] array;

		private final MutableLong value = new MutableLong();

		LongArray(long[] array) {
			this.array = requireNonNull(array);
		}

		@Override
		public TypeInfo getResultType() { return listType; }

		@Override
		public int size() { return array.length; }

		@Override
		public Primitive<Long> get(int index) {
			value.setLong(array[index]);
			return value;
		}

		@Override
		public long getAsLong(int index) { return array[index]; }

		@Override
		public long[] compute() { return array; }
	}

	static final class IntArray extends FixedArray<int[], Primitive<Long>>
			implements IntegerListExpression<int[]> {

		private static final TypeInfo listType = TypeInfo.of(int[].class, true);

		private final int[] array;

		private final MutableLong value = new MutableLong();

		IntArray(int[] array) {
			this.array = requireNonNull(array);
		}

		@Override
		public TypeInfo getResultType() { return listType; }

		@Override
		public int size() { return array.length; }

		@Override
		public Primitive<Long> get(int index) {
			value.setInt(array[index]);
			return value;
		}

		@Override
		public long getAsLong(int index) { return array[index]; }

		@Override
		public int[] compute() { return array; }
	}

	static final class DelegatingIntArray extends FixedArray<int[], Primitive<Long>>
			implements IntegerListExpression<int[]> {

		private static final TypeInfo listType = TypeInfo.of(int[].class, true);

		private final Supplier<int[]> source;

		private final MutableLong value = new MutableLong();

		DelegatingIntArray(Supplier<int[]> source) {
			this.source = requireNonNull(source);
		}

		@Override
		public TypeInfo getResultType() { return listType; }

		@Override
		public boolean isFixedSize() { return false; }

		@Override
		public int size() { return source.get().length; }

		@Override
		public Primitive<Long> get(int index) {
			value.setInt(source.get()[index]);
			return value;
		}

		@Override
		public long getAsLong(int index) { return source.get()[index]; }

		@Override
		public int[] compute() { return source.get(); }
	}

	static final class ShortArray extends FixedArray<short[], Primitive<Long>>
			implements IntegerListExpression<short[]> {

		private static final TypeInfo listType = TypeInfo.of(short[].class, true);

		private final short[] array;

		private final MutableLong value = new MutableLong();

		ShortArray(short[] array) {
			this.array = requireNonNull(array);
		}

		@Override
		public TypeInfo getResultType() { return listType; }

		@Override
		public int size() { return array.length; }

		@Override
		public Primitive<Long> get(int index) {
			value.setShort(array[index]);
			return value;
		}

		@Override
		public long getAsLong(int index) { return array[index]; }

		@Override
		public short[] compute() { return array; }
	}

	static final class ByteArray extends FixedArray<byte[], Primitive<Long>>
			implements IntegerListExpression<byte[]> {

		private static final TypeInfo listType = TypeInfo.of(byte[].class, true);

		private final byte[] array;

		private final MutableLong value = new MutableLong();

		ByteArray(byte[] array) {
			this.array = requireNonNull(array);
		}

		@Override
		public TypeInfo getResultType() { return listType; }

		@Override
		public int size() { return array.length; }

		@Override
		public Primitive<Long> get(int index) {
			value.setByte(array[index]);
			return value;
		}

		@Override
		public long getAsLong(int index) { return array[index]; }

		@Override
		public byte[] compute() { return array; }
	}

	static final class DoubleArray extends FixedArray<double[], Primitive<Double>>
			implements FloatingPointListExpression<double[]> {

		private static final TypeInfo listType = TypeInfo.of(double[].class, true);

		private final double[] array;

		private final MutableDouble value = new MutableDouble();

		DoubleArray(double[] array) {
			this.array = requireNonNull(array);
		}

		@Override
		public TypeInfo getResultType() { return listType; }

		@Override
		public int size() { return array.length; }

		@Override
		public Primitive<Double> get(int index) {
			value.setDouble(array[index]);
			return value;
		}

		@Override
		public double getAsDouble(int index) { return array[index]; }

		@Override
		public double[] compute() { return array; }
	}

	static final class BooleanArray extends FixedArray<boolean[], Primitive<Boolean>>
			implements BooleanListExpression<boolean[]> {

		private static final TypeInfo listType = TypeInfo.of(boolean[].class, true);

		private final boolean[] array;

		private final MutableBoolean value = new MutableBoolean();

		BooleanArray(boolean[] array) {
			this.array = requireNonNull(array);
		}

		@Override
		public TypeInfo getResultType() { return listType; }

		@Override
		public int size() { return array.length; }

		@Override
		public Primitive<Boolean> get(int index) {
			value.setBoolean(array[index]);
			return value;
		}

		@Override
		public boolean getAsBoolean(int index) { return array[index]; }

		@Override
		public boolean[] compute() { return array; }
	}
}
