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

import static de.ims.icarus2.query.api.eval.EvaluationUtils.forUnsupportedCast;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static java.util.Objects.requireNonNull;

import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.ims.icarus2.query.api.eval.Expression.BooleanListExpression;
import de.ims.icarus2.query.api.eval.Expression.FloatingPointListExpression;
import de.ims.icarus2.query.api.eval.Expression.IntegerListExpression;
import de.ims.icarus2.query.api.eval.Expression.ListExpression;
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
		TypeInfo type = source[0].getResultType();

		if(TypeInfo.isBoolean(type)) {
			return new BooleanListWrapper(EvaluationUtils.ensureBoolean(source));
		} else if(TypeInfo.isFloatingPoint(type)) {
			return new FloatingPointListWrapper(EvaluationUtils.ensureFloatingPoint(source));
		} else if(TypeInfo.isNumerical(type)) {
			return new IntegerListWrapper(EvaluationUtils.ensureInteger(source));
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		ListExpression<?, ?> result = new ObjectWrapper(source);
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

		private int index() {
			return strictToInt(index.computeAsLong());
		}

		@Override
		public Primitive<Long> compute() { return source.get(index()); }

		@Override
		public long computeAsLong() { return source.getAsLong(index()); }

		@Override
		public double computeAsDouble() { return computeAsLong(); }

		@Override
		public Expression<Primitive<Long>> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new IntegerAccess(
					(IntegerListExpression<?>)source.duplicate(context),
					index.duplicate(context));
		}

		@Override
		public Expression<Primitive<Long>> optimize(EvaluationContext context) {
			requireNonNull(context);
			IntegerListExpression<?> newSource = (IntegerListExpression<?>) source.optimize(context);
			Expression<?> newIndex = index.optimize(context);

			if(newSource.isConstant() && newIndex.isConstant()) {
				return Literals.of(newSource.getAsLong(strictToInt(newIndex.computeAsLong())));
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
				buffer[i] = source.getAsLong(strictToInt(index.getAsLong(i)));
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
					(IntegerListExpression<?>)source.duplicate(context),
					(IntegerListExpression<?>)index.duplicate(context));
		}

		@Override
		public Expression<long[]> optimize(EvaluationContext context) {
			requireNonNull(context);
			IntegerListExpression<?> newSource = (IntegerListExpression<?>) source.optimize(context);
			IntegerListExpression<?> newIndex = (IntegerListExpression<?>) index.optimize(context);

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

		@SuppressWarnings("unchecked")
		@Override
		public Primitive<Long> get(int index) { return (Primitive<Long>) source[index].compute(); }

		@Override
		public long getAsLong(int index) { return source[index].computeAsLong(); }

		@Override
		public Expression<long[]> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new IntegerListWrapper(Stream.of(source)
						.map(ne -> ne.duplicate(context))
						.toArray(Expression<?>[]::new));
		}

		@Override
		public Expression<long[]> optimize(EvaluationContext context) {
			requireNonNull(context);
			Expression<?>[] newSource = Stream.of(source)
					.map(ne -> ne.optimize(context))
					.toArray(Expression<?>[]::new);

			// Optimize to constant
			if(Stream.of(newSource).allMatch(Expression::isConstant)) {
				long[] array = new long[newSource.length];
				fillBuffer(array, newSource);
				return ArrayLiterals.of(array);
			}

			// We could check whether any of the index expressions has changed...
			return new IntegerListWrapper(newSource);
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

		private int index() {
			return strictToInt(index.computeAsLong());
		}

		@Override
		public Primitive<Double> compute() { return source.get(index()); }

		@Override
		public long computeAsLong() { throw forUnsupportedCast(TypeInfo.FLOATING_POINT, TypeInfo.INTEGER); }

		@Override
		public double computeAsDouble() { return source.getAsDouble(index()); }

		@Override
		public Expression<Primitive<Double>> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new FloatingPointAccess(
					(FloatingPointListExpression<?>)source.duplicate(context),
					index.duplicate(context));
		}

		@Override
		public Expression<Primitive<Double>> optimize(EvaluationContext context) {
			requireNonNull(context);
			FloatingPointListExpression<?> newSource = (FloatingPointListExpression<?>) source.optimize(context);
			Expression<?> newIndex = index.optimize(context);

			if(newSource.isConstant() && newIndex.isConstant()) {
				return Literals.of(newSource.getAsDouble(strictToInt(newIndex.computeAsLong())));
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
				buffer[i] = source.getAsDouble(strictToInt(index.getAsLong(i)));
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
					(FloatingPointListExpression<?>)source.duplicate(context),
					(IntegerListExpression<?>)index.duplicate(context));
		}

		@Override
		public Expression<double[]> optimize(EvaluationContext context) {
			requireNonNull(context);
			FloatingPointListExpression<?> newSource = (FloatingPointListExpression<?>) source.optimize(context);
			IntegerListExpression<?> newIndex = (IntegerListExpression<?>) index.optimize(context);

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

		@SuppressWarnings("unchecked")
		@Override
		public Primitive<Double> get(int index) { return (Primitive<Double>) source[index].compute(); }

		@Override
		public double getAsDouble(int index) { return source[index].computeAsDouble(); }

		@Override
		public Expression<double[]> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new FloatingPointListWrapper(Stream.of(source)
						.map(ne -> ne.duplicate(context))
						.toArray(Expression<?>[]::new));
		}

		@Override
		public Expression<double[]> optimize(EvaluationContext context) {
			requireNonNull(context);
			Expression<?>[] newSource = Stream.of(source)
					.map(ne -> ne.optimize(context))
					.toArray(Expression<?>[]::new);

			// Optimize to constant
			if(Stream.of(newSource).allMatch(Expression::isConstant)) {
				double[] array = new double[newSource.length];
				fillBuffer(array, newSource);
				return ArrayLiterals.of(array);
			}

			// We could check whether any of the index expressions has changed...
			return new FloatingPointListWrapper(newSource);
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

		private int index() {
			return strictToInt(index.computeAsLong());
		}

		@Override
		public Primitive<Boolean> compute() { return source.get(index()); }

		@Override
		public boolean computeAsBoolean() { return source.getAsBoolean(index()); }

		@Override
		public Expression<Primitive<Boolean>> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new BooleanAccess(
					(BooleanListExpression<?>)source.duplicate(context),
					(Expression<?>)index.duplicate(context));
		}

		@Override
		public Expression<Primitive<Boolean>> optimize(EvaluationContext context) {
			requireNonNull(context);
			BooleanListExpression<?> newSource = (BooleanListExpression<?>) source.optimize(context);
			Expression<?> newIndex = index.optimize(context);

			if(newSource.isConstant() && newIndex.isConstant()) {
				return Literals.of(newSource.getAsBoolean(strictToInt(newIndex.computeAsLong())));
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
				buffer[i] = source.getAsBoolean(strictToInt(index.getAsLong(i)));
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
			return new BooleanBatchAccess(
					(BooleanListExpression<?>)source.duplicate(context),
					(IntegerListExpression<?>)index.duplicate(context));
		}

		@Override
		public Expression<boolean[]> optimize(EvaluationContext context) {
			requireNonNull(context);
			BooleanListExpression<?> newSource = (BooleanListExpression<?>) source.optimize(context);
			IntegerListExpression<?> newIndex = (IntegerListExpression<?>) index.optimize(context);

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

		@SuppressWarnings("unchecked")
		@Override
		public Primitive<Boolean> get(int index) { return (Primitive<Boolean>) source[index].compute(); }

		@Override
		public boolean getAsBoolean(int index) { return source[index].computeAsBoolean(); }

		@Override
		public Expression<boolean[]> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new BooleanListWrapper(Stream.of(source)
						.map(ne -> ne.duplicate(context))
						.toArray(Expression[]::new));
		}

		@Override
		public Expression<boolean[]> optimize(EvaluationContext context) {
			requireNonNull(context);
			Expression<Primitive<Boolean>>[] newSource = Stream.of(source)
					.map(ne -> ne.optimize(context))
					.toArray(Expression[]::new);

			// Optimize to constant
			if(Stream.of(newSource).allMatch(Expression::isConstant)) {
				boolean[] array = new boolean[newSource.length];
				fillBuffer(array, newSource);
				return ArrayLiterals.of(array);
			}

			// We could check whether any of the index expressions has changed...
			return new BooleanListWrapper(newSource);
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
		public T compute() { return source.get(strictToInt(index.computeAsLong())); }

		@SuppressWarnings("unchecked")
		@Override
		public Expression<T> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new ObjectAccess<>(
					(ListExpression<?, T>)source.duplicate(context),
					(Expression<?>)index.duplicate(context));
		}

		@Override
		public Expression<T> optimize(EvaluationContext context) {
			requireNonNull(context);
			@SuppressWarnings("unchecked")
			ListExpression<?, T> newSource = (ListExpression<?, T>) source.optimize(context);
			Expression<?> newIndex = index.optimize(context);

			if(newSource.isConstant() && newIndex.isConstant()) {
				return Expressions.constant(newSource.get(strictToInt(newIndex.computeAsLong())));
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
				buffer[i] = source.get(strictToInt(index.getAsLong(i)));
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

		@SuppressWarnings("unchecked")
		@Override
		public ListExpression<E[], E> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new ObjectBatchAccess<>(
					(ListExpression<?, E>)source.duplicate(context),
					(IntegerListExpression<?>)index.duplicate(context));
		}

		@Override
		public ListExpression<E[], E> optimize(EvaluationContext context) {
			requireNonNull(context);
			@SuppressWarnings("unchecked")
			ListExpression<?, E> newSource = (ListExpression<?, E>) source.optimize(context);
			IntegerListExpression<?> newIndex = (IntegerListExpression<?>) index.optimize(context);

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

		public ObjectWrapper(Expression<E>[] source) {
			this.source = requireNonNull(source);
			elementType = source[0].getResultType();
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
		public E get(int index) { return source[index].compute(); }

		@Override
		public ListExpression<E[], E> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new ObjectWrapper<>(Stream.of(source)
						.map(ne -> ne.duplicate(context))
						.map(Expression.class::cast)
						.toArray(Expression[]::new));
		}

		@Override
		public ListExpression<E[], E> optimize(EvaluationContext context) {
			requireNonNull(context);
			Expression<E>[] newSource = Stream.of(source)
					.map(ne -> ne.optimize(context))
					.map(Expression.class::cast)
					.toArray(Expression[]::new);

			// Optimize to constant
			if(Stream.of(newSource).allMatch(Expression::isConstant)) {
				E[] array = EvaluationUtils.arrayOf(elementType, newSource.length);
				fillBuffer(array, newSource);
				return ArrayLiterals.ofGeneric(array);
			}

			// We could check whether any of the index expressions has changed...
			return new ObjectWrapper<>(newSource);
		}
	}
}
