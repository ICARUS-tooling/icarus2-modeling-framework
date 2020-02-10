/**
 *
 */
package de.ims.icarus2.query.api.eval;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import de.ims.icarus2.query.api.eval.Expression.IntegerListExpression;

/**
 * @author Markus Gärtner
 *
 */
public class ArrayLiterals {

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

	public static IntegerListExpression<int[]> of(Supplier<int[]> source) {
		return new DelegatingIntArray(source);
	}

	static final class LongArray implements IntegerListExpression<long[]> {

		private static final TypeInfo listType = TypeInfo.of(long[].class, true);

		private final long[] array;

		LongArray(long[] array) {
			this.array = requireNonNull(array);
		}

		@Override
		public TypeInfo getResultType() { return listType; }

		@Override
		public boolean isConstant() { return true; }

		@Override
		public Expression<long[]> duplicate(EvaluationContext context) { return this; }

		@Override
		public int size() { return array.length; }

		@Override
		public Long get(int index) { return Long.valueOf(array[index]); }

		@Override
		public long getAsLong(int index) { return array[index]; }

		@Override
		public long[] compute() { return array; }
	}

	static final class IntArray implements IntegerListExpression<int[]> {

		private static final TypeInfo listType = TypeInfo.of(int[].class, true);

		private final int[] array;

		IntArray(int[] array) {
			this.array = requireNonNull(array);
		}

		@Override
		public TypeInfo getResultType() { return listType; }

		@Override
		public boolean isConstant() { return true; }

		@Override
		public Expression<int[]> duplicate(EvaluationContext context) { return this; }

		@Override
		public int size() { return array.length; }

		@Override
		public Long get(int index) { return Long.valueOf(array[index]); }

		@Override
		public long getAsLong(int index) { return array[index]; }

		@Override
		public int[] compute() { return array; }
	}

	static final class DelegatingIntArray implements IntegerListExpression<int[]> {

		private static final TypeInfo listType = TypeInfo.of(int[].class, true);

		private final Supplier<int[]> source;

		DelegatingIntArray(Supplier<int[]> source) {
			this.source = requireNonNull(source);
		}

		@Override
		public TypeInfo getResultType() { return listType; }

		@Override
		public boolean isConstant() { return true; }

		@Override
		public Expression<int[]> duplicate(EvaluationContext context) { return this; }

		@Override
		public int size() { return source.get().length; }

		@Override
		public Long get(int index) { return Long.valueOf(source.get()[index]); }

		@Override
		public long getAsLong(int index) { return source.get()[index]; }

		@Override
		public int[] compute() { return source.get(); }
	}

	static final class ShortArray implements IntegerListExpression<short[]> {

		private static final TypeInfo listType = TypeInfo.of(short[].class, true);

		private final short[] array;

		ShortArray(short[] array) {
			this.array = requireNonNull(array);
		}

		@Override
		public TypeInfo getResultType() { return listType; }

		@Override
		public boolean isConstant() { return true; }

		@Override
		public Expression<short[]> duplicate(EvaluationContext context) { return this; }

		@Override
		public int size() { return array.length; }

		@Override
		public Long get(int index) { return Long.valueOf(array[index]); }

		@Override
		public long getAsLong(int index) { return array[index]; }

		@Override
		public short[] compute() { return array; }
	}

	static final class ByteArray implements IntegerListExpression<byte[]> {

		private static final TypeInfo listType = TypeInfo.of(byte[].class, true);

		private final byte[] array;

		ByteArray(byte[] array) {
			this.array = requireNonNull(array);
		}

		@Override
		public TypeInfo getResultType() { return listType; }

		@Override
		public boolean isConstant() { return true; }

		@Override
		public Expression<byte[]> duplicate(EvaluationContext context) { return this; }

		@Override
		public int size() { return array.length; }

		@Override
		public Long get(int index) { return Long.valueOf(array[index]); }

		@Override
		public long getAsLong(int index) { return array[index]; }

		@Override
		public byte[] compute() { return array; }
	}
}
