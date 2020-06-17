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
package de.ims.icarus2.query.api.exp;

import static de.ims.icarus2.query.api.exp.EvaluationUtils.castBoolean;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.castBooleanList;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.castFloatingPoint;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.castInteger;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.castIntegerList;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.castList;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.forUnsupportedCast;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static java.util.Objects.requireNonNull;

import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.ims.icarus2.query.api.exp.Expression.BooleanListExpression;
import de.ims.icarus2.query.api.exp.Expression.FloatingPointListExpression;
import de.ims.icarus2.query.api.exp.Expression.IntegerListExpression;
import de.ims.icarus2.query.api.exp.Expression.ListExpression;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.MutableDouble;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.Primitive;

/**
 * Provides various utility methods and implementations related to the
 * way arrays are modeled and expressed in IQL.
 *
 * @author Markus Gärtner
 *
 */
public final class ListAccess {

	private ListAccess() { /* no-op */ }

	// GENERIC METHODS

	/** Wraps a collection of individual expressions into a single list expression */
	public static ListExpression<?, ?> wrap(Expression<?>[] source) {
		requireNonNull(source);
		checkArgument("Source expressions array must not be empty", source.length>0);

		// TODO maybe switch to a more elaborate strategy for computing the result type
		TypeInfo elementType = source[0].getResultType();

		if(TypeInfo.isBoolean(elementType)) {
			return new BooleanListWrapper(EvaluationUtils.ensureBoolean(source));
		} else if(TypeInfo.isFloatingPoint(elementType)) {
			return new FloatingPointListWrapper(EvaluationUtils.ensureFloatingPoint(source));
		} else if(TypeInfo.isNumerical(elementType)) {
			return new IntegerListWrapper(EvaluationUtils.ensureInteger(source));
		}

		// Let object wrapper
		@SuppressWarnings({ "unchecked", "rawtypes" })
		ListExpression<?, ?> result = new ObjectWrapper(elementType, source);
		return result;
	}

	/**
	 * Wraps a collection of individual expressions into a single list expression
	 * with an explicitly specified element type
	 */
	public static ListExpression<?, ?> wrap(TypeInfo elementType, Expression<?>[] source) {
		requireNonNull(elementType);
		requireNonNull(source);
		checkArgument("Source expressions array must not be empty", source.length>0);

		if(TypeInfo.isBoolean(elementType)) {
			return new BooleanListWrapper(EvaluationUtils.ensureBoolean(source));
		} else if(TypeInfo.isFloatingPoint(elementType)) {
			return new FloatingPointListWrapper(EvaluationUtils.ensureFloatingPoint(source));
		} else if(TypeInfo.isNumerical(elementType)) {
			return new IntegerListWrapper(EvaluationUtils.ensureInteger(source));
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		ListExpression<?, ?> result = new ObjectWrapper(elementType, source);
		return result;
	}

	/** Unwraps a list expression into an array of individual expressions */
	public static <E> Expression<E>[] unwrap(ListExpression<?, E> source) {
		requireNonNull(source);
		if(source.size()==0) {
			return EvaluationUtils.noArgs();
		}

		IntFunction<Expression<?>> mapper;

		TypeInfo type = source.getElementType();
		if(TypeInfo.isBoolean(type)) {
			BooleanListExpression<?> bl = (BooleanListExpression<?>) source;
			mapper = i -> new BooleanAccess(bl, Literals.of(i));
		} else if(TypeInfo.isFloatingPoint(type)) {
			FloatingPointListExpression<?> fpl = (FloatingPointListExpression<?>) source;
			mapper = i -> new FloatingPointAccess(fpl, Literals.of(i));
		} else if(TypeInfo.isNumerical(type)) {
			IntegerListExpression<?> il = (IntegerListExpression<?>) source;
			mapper = i -> new IntegerAccess(il, Literals.of(i));
		} else {
			mapper = i -> new ObjectAccess<>(source, Literals.of(i));
		}

		return IntStream.range(0, source.size())
				.mapToObj(mapper)
				.toArray(Expression[]::new);
	}

	// INTEGER METHODS

	public static Expression<?> atIndex(IntegerListExpression<?> source,
			Expression<?> index) {
		return new IntegerAccess(source, index);
	}

	public static IntegerListExpression<long[]> filter(
			IntegerListExpression<?> source, IntegerListExpression<?> index) {
		return new IntegerBatchAccess(source, index);
	}

	public static IntegerListExpression<long[]> wrapIndices(Expression<?>...source) {
		return new IntegerListWrapper(source);
	}

	// FLOATING POINT METHODS

	public static Expression<?> atIndex(FloatingPointListExpression<?> source,
			Expression<?> index) {
		return new FloatingPointAccess(source, index);
	}

	public static FloatingPointListExpression<double[]> filter(
			FloatingPointListExpression<?> source, IntegerListExpression<?> index) {
		return new FloatingPointBatchAccess(source, index);
	}

	// BOOLEAN METHODS

	public static Expression<Primitive<Boolean>> atIndex(BooleanListExpression<?> source,
			Expression<?> index) {
		return new BooleanAccess(source, index);
	}

	public static BooleanListExpression<boolean[]> filter(
			BooleanListExpression<?> source, IntegerListExpression<?> index) {
		return new BooleanBatchAccess(source, index);
	}

	// OBJECT METHODS

	public static Expression<?> atIndex(ListExpression<?, ?> source,
			Expression<?> index) {
		TypeInfo type = source.getElementType();
		if(TypeInfo.isBoolean(type)) {
			return atIndex((BooleanListExpression<?>)source, index);
		} else if(TypeInfo.isFloatingPoint(type)) {
			return atIndex((FloatingPointListExpression<?>)source, index);
		} else if(TypeInfo.isNumerical(type)) {
			return atIndex((IntegerListExpression<?>)source, index);
		}

		return new ObjectAccess<>(source, index);
	}

	public static ListExpression<?, ?> filter(
			ListExpression<?, ?> source, IntegerListExpression<?> index) {
		TypeInfo type = source.getElementType();
		if(TypeInfo.isBoolean(type)) {
			return filter((BooleanListExpression<?>)source, index);
		} else if(TypeInfo.isFloatingPoint(type)) {
			return filter((FloatingPointListExpression<?>)source, index);
		} else if(TypeInfo.isNumerical(type)) {
			return filter((IntegerListExpression<?>)source, index);
		}

		return new ObjectBatchAccess<>(source, index);
	}

	private static int index(ListExpression<?, ?> source, Expression<?> index) {
		return index(source, index.computeAsInt());
	}

	private static int index(ListExpression<?, ?> source, int idx) {
		return index(source.size(), idx);
	}

	private static int index(int size, int idx) {
		if(idx<0) {
			idx = size+idx;
		}
		return idx;
	}

	/** Provides access to a single array element */
	static final class IntegerAccess implements Expression<Primitive<Long>> {
		private final IntegerListExpression<?> source;
		private final Expression<?> index;

		public IntegerAccess(IntegerListExpression<?> source, Expression<?> index) {
			this.source = requireNonNull(source);
			this.index = requireNonNull(index);
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.INTEGER; }

		@Override
		public Primitive<Long> compute() { return source.get(index(source, index)); }

		@Override
		public long computeAsLong() { return source.getAsLong(index(source, index)); }

		@Override
		public double computeAsDouble() { return computeAsLong(); }

		@Override
		public Expression<Primitive<Long>> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new IntegerAccess(
					(IntegerListExpression<?>)context.duplicate(source),
					context.duplicate(index));
		}

		@Override
		public Expression<Primitive<Long>> optimize(EvaluationContext context) {
			requireNonNull(context);
			IntegerListExpression<?> newSource = (IntegerListExpression<?>) context.optimize(source);
			Expression<?> newIndex = context.optimize(index);

			if(newSource.isConstant() && newIndex.isConstant()) {
				return Literals.of(newSource.getAsLong(index(newSource, newIndex)));
			}

			if(newSource!=source || newIndex!=index) {
				return new IntegerAccess(newSource, newIndex);
			}

			return this;
		}
	}

	/** Provides access to a list of array elements */
	static final class IntegerBatchAccess implements IntegerListExpression<long[]> {
		private final IntegerListExpression<?> source;
		private final IntegerListExpression<?> index;
		private final long[] buffer;
		private final MutableLong value;

		private static final TypeInfo type = TypeInfo.of(long[].class, true);

		public IntegerBatchAccess(IntegerListExpression<?> source, IntegerListExpression<?> index) {
			this.source = requireNonNull(source);
			this.index = requireNonNull(index);
			buffer = index.isFixedSize() ? new long[index.size()] : null;
			value = new MutableLong();
		}

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public int size() { return index.size(); }

		@Override
		public boolean isFixedSize() { return index.isFixedSize(); }

		private static void fillBuffer(long[] buffer, IntegerListExpression<?> source,
				IntegerListExpression<?> index) {
			for (int i = 0; i < buffer.length; i++) {
				buffer[i] = source.getAsLong(index(source, index.getAsInt(i)));
			}
		}

		/**
		 * If the underlying index expression is not of fixed size, this method will
		 * allocate a new long array for every invocation!
		 */
		@Override
		public long[] compute() {
			long[] buffer = index.isFixedSize() ? this.buffer : new long[index.size()];
			fillBuffer(buffer, source, index);
			return buffer;
		}

		@Override
		public Primitive<Long> get(int index) {
			value.setLong(getAsLong(index));
			return value;
		}

		@Override
		public long getAsLong(int index) {
			return source.getAsLong(strictToInt(this.index.getAsLong(index)));
		}

		@Override
		public Expression<long[]> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new IntegerBatchAccess(
					(IntegerListExpression<?>)context.duplicate(source),
					(IntegerListExpression<?>)context.duplicate(index));
		}

		@Override
		public Expression<long[]> optimize(EvaluationContext context) {
			requireNonNull(context);
			IntegerListExpression<?> newSource = (IntegerListExpression<?>) context.optimize(source);
			IntegerListExpression<?> newIndex = (IntegerListExpression<?>) context.optimize(index);

			// Optimize to constant
			if(newSource.isConstant() && newIndex.isConstant()) {
				long[] array = new long[newIndex.size()];
				fillBuffer(array, newSource, newIndex);
				return ArrayLiterals.of(array);
			}

			// Include whatever optimization the underlying expressions provide
			if(newSource!=source || newIndex!=index) {
				return new IntegerBatchAccess(newSource, newIndex);
			}

			return this;
		}
	}

	/** Wraps multiple singular expressions into an array */
	static final class IntegerListWrapper implements IntegerListExpression<long[]> {
		private final Expression<?>[] source;
		private final long[] buffer;

		private static final TypeInfo type = TypeInfo.of(long[].class, true);

		public IntegerListWrapper(Expression<?>[] source) {
			this.source = requireNonNull(source);
			buffer = new long[source.length];
		}

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public int size() { return buffer.length; }

		@Override
		public boolean isFixedSize() { return true; }

		private static void fillBuffer(long[] buffer, Expression<?>[] source) {
			for (int i = 0; i < buffer.length; i++) {
				buffer[i] = source[i].computeAsLong();
			}
		}

		@Override
		public long[] compute() {
			fillBuffer(buffer, source);
			return buffer;
		}

		@Override
		public Primitive<Long> get(int index) {
			return castInteger(source[index(source.length, index)]).compute();
		}

		@Override
		public long getAsLong(int index) {
			return source[index(source.length, index)].computeAsLong();
		}

		@Override
		public Expression<long[]> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new IntegerListWrapper(EvaluationUtils.duplicate(source, context));
		}

		@Override
		public Expression<long[]> optimize(EvaluationContext context) {
			requireNonNull(context);
			Expression<?>[] newSource = EvaluationUtils.optimize(source, context);

			boolean sourceChanged = false;
			for (int i = 0; i < newSource.length; i++) {
				if(newSource[i] != source[i]) {
					sourceChanged = true;
					break;
				}
			}

			// Optimize to constant
			if(Stream.of(newSource).allMatch(Expression::isConstant)) {
				long[] array = new long[newSource.length];
				fillBuffer(array, newSource);
				return ArrayLiterals.of(array);
			} else if(sourceChanged) {
				return new IntegerListWrapper(newSource);
			}

			return this;
		}
	}

	/** Provides access to a single array element */
	static final class FloatingPointAccess implements Expression<Primitive<Double>> {
		private final FloatingPointListExpression<?> source;
		private final Expression<?> index;

		public FloatingPointAccess(FloatingPointListExpression<?> source, Expression<?> index) {
			this.source = requireNonNull(source);
			this.index = requireNonNull(index);
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.FLOATING_POINT; }

		@Override
		public Primitive<Double> compute() { return source.get(index(source, index)); }

		@Override
		public long computeAsLong() { throw forUnsupportedCast(TypeInfo.FLOATING_POINT, TypeInfo.INTEGER); }

		@Override
		public double computeAsDouble() { return source.getAsDouble(index(source, index)); }

		@Override
		public Expression<Primitive<Double>> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new FloatingPointAccess(
					(FloatingPointListExpression<?>)context.duplicate(source),
					context.duplicate(index));
		}

		@Override
		public Expression<Primitive<Double>> optimize(EvaluationContext context) {
			requireNonNull(context);
			FloatingPointListExpression<?> newSource = (FloatingPointListExpression<?>) context.optimize(source);
			Expression<?> newIndex = context.optimize(index);

			if(newSource.isConstant() && newIndex.isConstant()) {
				return Literals.of(newSource.getAsDouble(index(newSource, newIndex)));
			}

			if(newSource!=source || newIndex!=index) {
				return new FloatingPointAccess(newSource, newIndex);
			}

			return this;
		}
	}

	/** Provides access to a list of array elements */
	static final class FloatingPointBatchAccess implements FloatingPointListExpression<double[]> {
		private final FloatingPointListExpression<?> source;
		private final IntegerListExpression<?> index;
		private final double[] buffer;
		private final MutableDouble value;

		private static final TypeInfo type = TypeInfo.of(double[].class, true);

		public FloatingPointBatchAccess(FloatingPointListExpression<?> source, IntegerListExpression<?> index) {
			this.source = requireNonNull(source);
			this.index = requireNonNull(index);
			buffer = index.isFixedSize() ? new double[index.size()] : null;
			value = new MutableDouble();
		}

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public int size() { return index.size(); }

		@Override
		public boolean isFixedSize() { return index.isFixedSize(); }

		private static void fillBuffer(double[] buffer, FloatingPointListExpression<?> source,
				IntegerListExpression<?> index) {
			for (int i = 0; i < buffer.length; i++) {
				buffer[i] = source.getAsDouble(index(source, index.getAsInt(i)));
			}
		}

		/**
		 * If the underlying index expression is not of fixed size, this method will
		 * allocate a new long array for every invocation!
		 */
		@Override
		public double[] compute() {
			double[] buffer = index.isFixedSize() ? this.buffer : new double[index.size()];
			fillBuffer(buffer, source, index);
			return buffer;
		}

		@Override
		public Primitive<Double> get(int index) {
			value.setDouble(getAsDouble(index));
			return value;
		}

		@Override
		public double getAsDouble(int index) {
			return source.getAsDouble(strictToInt(this.index.getAsLong(index)));
		}

		@Override
		public Expression<double[]> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new FloatingPointBatchAccess(
					(FloatingPointListExpression<?>)context.duplicate(source),
					(IntegerListExpression<?>)context.duplicate(index));
		}

		@Override
		public Expression<double[]> optimize(EvaluationContext context) {
			requireNonNull(context);
			FloatingPointListExpression<?> newSource = (FloatingPointListExpression<?>) context.optimize(source);
			IntegerListExpression<?> newIndex = (IntegerListExpression<?>) context.optimize(index);

			// Optimize to constant
			if(newSource.isConstant() && newIndex.isConstant()) {
				double[] array = new double[newIndex.size()];
				fillBuffer(array, newSource, newIndex);
				return ArrayLiterals.of(array);
			}

			// Include whatever optimization the underlying expressions provide
			if(newSource!=source || newIndex!=index) {
				return new FloatingPointBatchAccess(newSource, newIndex);
			}

			return this;
		}
	}

	/** Wraps multiple singular expressions into an array */
	static final class FloatingPointListWrapper implements FloatingPointListExpression<double[]> {
		private final Expression<?>[] source;
		private final double[] buffer;

		private static final TypeInfo type = TypeInfo.of(double[].class, true);

		public FloatingPointListWrapper(Expression<?>[] source) {
			this.source = requireNonNull(source);
			buffer = new double[source.length];
		}

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public int size() { return buffer.length; }

		@Override
		public boolean isFixedSize() { return true; }

		private static void fillBuffer(double[] buffer, Expression<?>[] source) {
			for (int i = 0; i < buffer.length; i++) {
				buffer[i] = source[i].computeAsDouble();
			}
		}

		@Override
		public double[] compute() {
			fillBuffer(buffer, source);
			return buffer;
		}

		@Override
		public Primitive<Double> get(int index) {
			return castFloatingPoint(source[index(source.length, index)]).compute();
		}

		@Override
		public double getAsDouble(int index) {
			return source[index(source.length, index)].computeAsDouble();
		}

		@Override
		public Expression<double[]> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new FloatingPointListWrapper(EvaluationUtils.duplicate(source, context));
		}

		@Override
		public Expression<double[]> optimize(EvaluationContext context) {
			requireNonNull(context);
			Expression<?>[] newSource = EvaluationUtils.optimize(source, context);

			boolean sourceChanged = false;
			for (int i = 0; i < newSource.length; i++) {
				if(newSource[i] != source[i]) {
					sourceChanged = true;
					break;
				}
			}

			// Optimize to constant
			if(Stream.of(newSource).allMatch(Expression::isConstant)) {
				double[] array = new double[newSource.length];
				fillBuffer(array, newSource);
				return ArrayLiterals.of(array);
			} else if(sourceChanged) {
				return new FloatingPointListWrapper(newSource);
			}

			return this;
		}
	}

	/** Provides access to a single array element */
	static final class BooleanAccess implements Expression<Primitive<Boolean>> {
		private final BooleanListExpression<?> source;
		private final Expression<?> index;

		public BooleanAccess(BooleanListExpression<?> source, Expression<?> index) {
			this.source = requireNonNull(source);
			this.index = requireNonNull(index);
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.BOOLEAN; }

		@Override
		public Primitive<Boolean> compute() { return source.get(index(source, index)); }

		@Override
		public boolean computeAsBoolean() { return source.getAsBoolean(index(source, index)); }

		@Override
		public Expression<Primitive<Boolean>> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new BooleanAccess(castBooleanList(context.duplicate(source)),context.duplicate(index));
		}

		@Override
		public Expression<Primitive<Boolean>> optimize(EvaluationContext context) {
			requireNonNull(context);
			BooleanListExpression<?> newSource = castBooleanList(context.optimize(source));
			Expression<?> newIndex = context.optimize(index);

			if(newSource.isConstant() && newIndex.isConstant()) {
				return Literals.of(newSource.getAsBoolean(index(newSource, newIndex)));
			}

			if(newSource!=source || newIndex!=index) {
				return new BooleanAccess(newSource, newIndex);
			}

			return this;
		}
	}

	/** Provides access to a list of array elements */
	static final class BooleanBatchAccess implements BooleanListExpression<boolean[]> {
		private final BooleanListExpression<?> source;
		private final IntegerListExpression<?> index;
		private final boolean[] buffer;
		private final MutableBoolean value;

		private static final TypeInfo type = TypeInfo.of(boolean[].class, true);

		public BooleanBatchAccess(BooleanListExpression<?> source, IntegerListExpression<?> index) {
			this.source = requireNonNull(source);
			this.index = requireNonNull(index);
			buffer = index.isFixedSize() ? new boolean[index.size()] : null;
			value = new MutableBoolean();
		}

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public int size() { return index.size(); }

		@Override
		public boolean isFixedSize() { return index.isFixedSize(); }

		private static void fillBuffer(boolean[] buffer, BooleanListExpression<?> source,
				IntegerListExpression<?> index) {
			for (int i = 0; i < buffer.length; i++) {
				buffer[i] = source.getAsBoolean(index(source, index.getAsInt(i)));
			}
		}

		/**
		 * If the underlying index expression is not of fixed size, this method will
		 * allocate a new long array for every invocation!
		 */
		@Override
		public boolean[] compute() {
			boolean[] buffer = index.isFixedSize() ? this.buffer : new boolean[index.size()];
			fillBuffer(buffer, source, index);
			return buffer;
		}

		@Override
		public Primitive<Boolean> get(int index) {
			value.setBoolean(getAsBoolean(index));
			return value;
		}

		@Override
		public boolean getAsBoolean(int index) {
			return source.getAsBoolean(strictToInt(this.index.getAsLong(index)));
		}

		@Override
		public Expression<boolean[]> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new BooleanBatchAccess(castBooleanList(context.duplicate(source)),
					castIntegerList(context.duplicate(index)));
		}

		@Override
		public Expression<boolean[]> optimize(EvaluationContext context) {
			requireNonNull(context);
			BooleanListExpression<?> newSource = castBooleanList(context.optimize(source));
			IntegerListExpression<?> newIndex = castIntegerList(context.optimize(index));

			// Optimize to constant
			if(newSource.isConstant() && newIndex.isConstant()) {
				boolean[] array = new boolean[newIndex.size()];
				fillBuffer(array, newSource, newIndex);
				return ArrayLiterals.of(array);
			}

			// Include whatever optimization the underlying expressions provide
			if(newSource!=source || newIndex!=index) {
				return new BooleanBatchAccess(newSource, newIndex);
			}

			return this;
		}
	}

	/** Wraps multiple singular expressions into an array */
	static final class BooleanListWrapper implements BooleanListExpression<boolean[]> {
		private final Expression<?>[] source;
		private final boolean[] buffer;

		private static final TypeInfo type = TypeInfo.of(boolean[].class, true);

		public BooleanListWrapper(Expression<?>[] source) {
			this.source = requireNonNull(source);
			buffer = new boolean[source.length];
		}

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public int size() { return buffer.length; }

		@Override
		public boolean isFixedSize() { return true; }

		private static void fillBuffer(boolean[] buffer, Expression<?>[] source) {
			for (int i = 0; i < buffer.length; i++) {
				buffer[i] = source[i].computeAsBoolean();
			}
		}

		@Override
		public boolean[] compute() {
			fillBuffer(buffer, source);
			return buffer;
		}

		@Override
		public Primitive<Boolean> get(int index) {
			return castBoolean(source[index(source.length, index)]).compute();
		}

		@Override
		public boolean getAsBoolean(int index) {
			return source[index(source.length, index)].computeAsBoolean();
		}

		@Override
		public Expression<boolean[]> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new BooleanListWrapper(EvaluationUtils.duplicate(source, context));
		}

		@Override
		public Expression<boolean[]> optimize(EvaluationContext context) {
			requireNonNull(context);
			Expression<Primitive<Boolean>>[] newSource = EvaluationUtils.optimize(source, context);

			boolean sourceChanged = false;
			for (int i = 0; i < newSource.length; i++) {
				if(newSource[i] != source[i]) {
					sourceChanged = true;
					break;
				}
			}

			// Optimize to constant
			if(Stream.of(newSource).allMatch(Expression::isConstant)) {
				boolean[] array = new boolean[newSource.length];
				fillBuffer(array, newSource);
				return ArrayLiterals.of(array);
			} else if(sourceChanged) {
				return new BooleanListWrapper(newSource);
			}

			return this;
		}
	}

	/** Provides access to a single array element */
	static final class ObjectAccess<T> implements Expression<T> {
		private final ListExpression<?, T> source;
		private final Expression<?> index;

		public ObjectAccess(ListExpression<?, T> source, Expression<?> index) {
			this.source = requireNonNull(source);
			this.index = requireNonNull(index);
		}

		@Override
		public TypeInfo getResultType() { return source.getElementType(); }

		@Override
		public T compute() { return source.get(index(source, index)); }

		@Override
		public Expression<T> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new ObjectAccess<>(castList(context.duplicate(source)), context.duplicate(index));
		}

		@Override
		public Expression<T> optimize(EvaluationContext context) {
			requireNonNull(context);
			ListExpression<?, T> newSource = castList(context.optimize(source));
			Expression<?> newIndex = context.optimize(index);

			if(newSource.isConstant() && newIndex.isConstant()) {
				return Expressions.constant(newSource.get(index(newSource, newIndex)));
			}

			if(newSource!=source || newIndex!=index) {
				return new ObjectAccess<>(newSource, newIndex);
			}

			return this;
		}
	}

	/** Provides access to a list of array elements */
	static final class ObjectBatchAccess<E> implements ListExpression<E[], E> {
		private final ListExpression<?, E> source;
		private final IntegerListExpression<?> index;
		private final E[] buffer;

		private final TypeInfo type, elementType;

		public ObjectBatchAccess(ListExpression<?, E> source, IntegerListExpression<?> index) {
			this.source = requireNonNull(source);
			this.index = requireNonNull(index);
			elementType = source.getElementType();
			type = EvaluationUtils.arrayType(elementType);
			buffer = index.isFixedSize() ? EvaluationUtils.arrayOf(elementType, index.size()) : null;
		}

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public TypeInfo getElementType() { return elementType; }

		@Override
		public int size() { return index.size(); }

		@Override
		public boolean isFixedSize() { return index.isFixedSize(); }

		private static <E> void fillBuffer(E[] buffer, ListExpression<?, E> source,
				IntegerListExpression<?> index) {
			for (int i = 0; i < buffer.length; i++) {
				buffer[i] = source.get(index(source, index.getAsInt(i)));
			}
		}

		/**
		 * If the underlying index expression is not of fixed size, this method will
		 * allocate a new array for every invocation!
		 */
		@Override
		public E[] compute() {
			E[] buffer = index.isFixedSize() ? this.buffer : EvaluationUtils.arrayOf(elementType, source.size());
			fillBuffer(buffer, source, index);
			return buffer;
		}

		@Override
		public E get(int index) {
			return source.get(strictToInt(this.index.getAsLong(index)));
		}

		@Override
		public ListExpression<E[], E> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new ObjectBatchAccess<>(castList(context.duplicate(source)),
					castIntegerList(context.duplicate(index)));
		}

		@Override
		public ListExpression<E[], E> optimize(EvaluationContext context) {
			requireNonNull(context);
			ListExpression<?, E> newSource = castList(context.optimize(source));
			IntegerListExpression<?> newIndex = castIntegerList(context.optimize(index));

			// Optimize to constant
			if(newSource.isConstant() && newIndex.isConstant()) {
				E[] array = EvaluationUtils.arrayOf(elementType, newIndex.size());
				fillBuffer(array, newSource, newIndex);
				return ArrayLiterals.ofGeneric(array);
			}

			// Include whatever optimization the underlying expressions provide
			if(newSource!=source || newIndex!=index) {
				return new ObjectBatchAccess<>(newSource, newIndex);
			}

			return this;
		}
	}

	/** Wraps multiple singular expressions into an array */
	static final class ObjectWrapper<E> implements ListExpression<E[], E> {
		private final Expression<E>[] source;
		private final E[] buffer;

		private final TypeInfo type, elementType;

//		public ObjectWrapper(Expression<E>[] source) {
//			this.source = requireNonNull(source);
//			elementType = source[0].getResultType();
//			type = EvaluationUtils.arrayType(elementType);
//			buffer = EvaluationUtils.arrayOf(elementType, source.length);
//		}

		public ObjectWrapper(TypeInfo elementType, Expression<E>[] source) {
			this.source = requireNonNull(source);
			this.elementType = requireNonNull(elementType);
			type = EvaluationUtils.arrayType(elementType);
			buffer = EvaluationUtils.arrayOf(elementType, source.length);
		}

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public TypeInfo getElementType() { return elementType; }

		@Override
		public int size() { return buffer.length; }

		@Override
		public boolean isFixedSize() { return true; }

		private static <E> void fillBuffer(E[] buffer, Expression<E>[] source) {
			for (int i = 0; i < buffer.length; i++) {
				buffer[i] = source[i].compute();
			}
		}

		@Override
		public E[] compute() {
			fillBuffer(buffer, source);
			return buffer;
		}

		@Override
		public E get(int index) { return source[index(source.length, index)].compute(); }

		@Override
		public ListExpression<E[], E> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new ObjectWrapper<>(elementType, EvaluationUtils.duplicate(source, context));
		}

		@Override
		public ListExpression<E[], E> optimize(EvaluationContext context) {
			requireNonNull(context);
			Expression<E>[] newSource = EvaluationUtils.optimize(source, context);

			boolean sourceChanged = false;
			for (int i = 0; i < newSource.length; i++) {
				if(newSource[i] != source[i]) {
					sourceChanged = true;
					break;
				}
			}

			if(Stream.of(newSource).allMatch(Expression::isConstant)) {
				// Optimize to constant
				E[] array = EvaluationUtils.arrayOf(elementType, newSource.length);
				fillBuffer(array, newSource);
				return ArrayLiterals.ofGeneric(array);
			} else if(sourceChanged) {
				// Allow underlying optimizations to take effect
				return new ObjectWrapper<>(elementType, newSource);
			}

			// Nothing changed
			return this;
		}
	}
}
