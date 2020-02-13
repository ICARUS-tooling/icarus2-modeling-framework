/**
 *
 */
package de.ims.icarus2.query.api.eval;

import static de.ims.icarus2.query.api.eval.EvaluationUtils.forUnsupportedCast;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Array;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.eval.Expression.BooleanExpression;
import de.ims.icarus2.query.api.eval.Expression.BooleanListExpression;
import de.ims.icarus2.query.api.eval.Expression.FloatingPointListExpression;
import de.ims.icarus2.query.api.eval.Expression.IntegerListExpression;
import de.ims.icarus2.query.api.eval.Expression.ListExpression;
import de.ims.icarus2.query.api.eval.Expression.NumericalExpression;
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
			return new BooleanWrapper(EvaluationUtils.ensureBoolean(source));
		} else if(TypeInfo.isFloatingPoint(type)) {
			return new FloatingPointWrapper(EvaluationUtils.ensureFloatingPoint(source));
		} else if(TypeInfo.isNumerical(type)) {
			return new IntegerWrapper(EvaluationUtils.ensureInteger(source));
		}

		return new ObjectWrapper<>(source);
	}

	/** Unwraps a list expression into an array of individual expressions */
	public static <E> Expression<E>[] unwrap(ListExpression<?, E> source) {
		requireNonNull(source);
		checkArgument("Source list must not be empty", source.size()>0);

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

	public static NumericalExpression atIndex(IntegerListExpression<?> source,
			NumericalExpression index) {
		return new IntegerAccess(source, index);
	}

	public static IntegerListExpression<long[]> filter(
			IntegerListExpression<?> source, IntegerListExpression<?> index) {
		return new IntegerBatchAccess(source, index);
	}

	public static IntegerListExpression<long[]> wrapIndices(NumericalExpression...source) {
		return new IntegerWrapper(source);
	}

	// FLOATING POINT METHODS

	public static NumericalExpression atIndex(FloatingPointListExpression<?> source,
			NumericalExpression index) {
		return new FloatingPointAccess(source, index);
	}

	public static FloatingPointListExpression<double[]> filter(
			FloatingPointListExpression<?> source, IntegerListExpression<?> index) {
		return new FloatingPointBatchAccess(source, index);
	}

	// BOOLEAN METHODS

	public static BooleanExpression atIndex(BooleanListExpression<?> source,
			NumericalExpression index) {
		return new BooleanAccess(source, index);
	}

	public static BooleanListExpression<boolean[]> filter(
			BooleanListExpression<?> source, IntegerListExpression<?> index) {
		return new BooleanBatchAccess(source, index);
	}

	// OBJECT METHODS

	public static Expression<?> atIndex(ListExpression<?, ?> source,
			NumericalExpression index) {
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
	static final class IntegerAccess implements NumericalExpression {
		private final IntegerListExpression<?> source;
		private final NumericalExpression index;

		public IntegerAccess(IntegerListExpression<?> source, NumericalExpression index) {
			this.source = requireNonNull(source);
			this.index = requireNonNull(index);
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.INTEGER; }

		private int index() {
			return strictToInt(index.computeAsLong());
		}

		@Override
		public Primitive<? extends Number> compute() { return source.get(index()); }

		@Override
		public long computeAsLong() { return source.getAsLong(index()); }

		@Override
		public double computeAsDouble() { return computeAsLong(); }

		@Override
		public Expression<Primitive<? extends Number>> duplicate(EvaluationContext context) {
			return new IntegerAccess(
					(IntegerListExpression<?>)source.duplicate(context),
					(NumericalExpression)index.duplicate(context));
		}

		@Override
		public Expression<Primitive<? extends Number>> optimize(EvaluationContext context) {
			IntegerListExpression<?> newSource = (IntegerListExpression<?>) source.optimize(context);
			NumericalExpression newIndex = (NumericalExpression) index.optimize(context);

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
			return new IntegerBatchAccess(
					(IntegerListExpression<?>)source.duplicate(context),
					(IntegerListExpression<?>)index.duplicate(context));
		}

		@Override
		public Expression<long[]> optimize(EvaluationContext context) {
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
	static final class IntegerWrapper implements IntegerListExpression<long[]> {
		private final NumericalExpression[] source;
		private final long[] buffer;

		private static final TypeInfo type = TypeInfo.of(long[].class, true);

		public IntegerWrapper(NumericalExpression[] source) {
			this.source = requireNonNull(source);
			buffer = new long[source.length];
		}

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public int size() { return buffer.length; }

		@Override
		public boolean isFixedSize() { return true; }

		private static void fillBuffer(long[] buffer, NumericalExpression[] source) {
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
			return new IntegerWrapper(Stream.of(source)
						.map(ne -> ne.duplicate(context))
						.map(NumericalExpression.class::cast)
						.toArray(NumericalExpression[]::new));
		}

		@Override
		public Expression<long[]> optimize(EvaluationContext context) {
			NumericalExpression[] newSource = Stream.of(source)
					.map(ne -> ne.optimize(context))
					.map(NumericalExpression.class::cast)
					.toArray(NumericalExpression[]::new);

			// Optimize to constant
			if(Stream.of(newSource).allMatch(Expression::isConstant)) {
				long[] array = new long[newSource.length];
				fillBuffer(array, newSource);
				return ArrayLiterals.of(array);
			}

			// We could check whether any of the index expressions has changed...
			return new IntegerWrapper(newSource);
		}
	}

	/** Provides access to a single array element */
	static final class FloatingPointAccess implements NumericalExpression {
		private final FloatingPointListExpression<?> source;
		private final NumericalExpression index;

		public FloatingPointAccess(FloatingPointListExpression<?> source, NumericalExpression index) {
			this.source = requireNonNull(source);
			this.index = requireNonNull(index);
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.FLOATING_POINT; }

		private int index() {
			return strictToInt(index.computeAsLong());
		}

		@Override
		public Primitive<? extends Number> compute() { return source.get(index()); }

		@Override
		public long computeAsLong() { throw forUnsupportedCast(TypeInfo.FLOATING_POINT, TypeInfo.INTEGER); }

		@Override
		public double computeAsDouble() { return source.getAsDouble(index()); }

		@Override
		public Expression<Primitive<? extends Number>> duplicate(EvaluationContext context) {
			return new FloatingPointAccess(
					(FloatingPointListExpression<?>)source.duplicate(context),
					(NumericalExpression)index.duplicate(context));
		}

		@Override
		public Expression<Primitive<? extends Number>> optimize(EvaluationContext context) {
			FloatingPointListExpression<?> newSource = (FloatingPointListExpression<?>) source.optimize(context);
			NumericalExpression newIndex = (NumericalExpression) index.optimize(context);

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
			return new FloatingPointBatchAccess(
					(FloatingPointListExpression<?>)source.duplicate(context),
					(IntegerListExpression<?>)index.duplicate(context));
		}

		@Override
		public Expression<double[]> optimize(EvaluationContext context) {
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
	static final class FloatingPointWrapper implements FloatingPointListExpression<double[]> {
		private final NumericalExpression[] source;
		private final double[] buffer;

		private static final TypeInfo type = TypeInfo.of(double[].class, true);

		public FloatingPointWrapper(NumericalExpression[] source) {
			this.source = requireNonNull(source);
			buffer = new double[source.length];
		}

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public int size() { return buffer.length; }

		@Override
		public boolean isFixedSize() { return true; }

		private static void fillBuffer(double[] buffer, NumericalExpression[] source) {
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
		public double getAsDouble(int index) { return source[index].computeAsLong(); }

		@Override
		public Expression<double[]> duplicate(EvaluationContext context) {
			return new FloatingPointWrapper(Stream.of(source)
						.map(ne -> ne.duplicate(context))
						.map(NumericalExpression.class::cast)
						.toArray(NumericalExpression[]::new));
		}

		@Override
		public Expression<double[]> optimize(EvaluationContext context) {
			NumericalExpression[] newSource = Stream.of(source)
					.map(ne -> ne.optimize(context))
					.map(NumericalExpression.class::cast)
					.toArray(NumericalExpression[]::new);

			// Optimize to constant
			if(Stream.of(newSource).allMatch(Expression::isConstant)) {
				double[] array = new double[newSource.length];
				fillBuffer(array, newSource);
				return ArrayLiterals.of(array);
			}

			// We could check whether any of the index expressions has changed...
			return new FloatingPointWrapper(newSource);
		}
	}

	/** Provides access to a single array element */
	static final class BooleanAccess implements BooleanExpression {
		private final BooleanListExpression<?> source;
		private final NumericalExpression index;

		public BooleanAccess(BooleanListExpression<?> source, NumericalExpression index) {
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
			return new BooleanAccess(
					(BooleanListExpression<?>)source.duplicate(context),
					(NumericalExpression)index.duplicate(context));
		}

		@Override
		public Expression<Primitive<Boolean>> optimize(EvaluationContext context) {
			BooleanListExpression<?> newSource = (BooleanListExpression<?>) source.optimize(context);
			NumericalExpression newIndex = (NumericalExpression) index.optimize(context);

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
			return new BooleanBatchAccess(
					(BooleanListExpression<?>)source.duplicate(context),
					(IntegerListExpression<?>)index.duplicate(context));
		}

		@Override
		public Expression<boolean[]> optimize(EvaluationContext context) {
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
	static final class BooleanWrapper implements BooleanListExpression<boolean[]> {
		private final BooleanExpression[] source;
		private final boolean[] buffer;

		private static final TypeInfo type = TypeInfo.of(boolean[].class, true);

		public BooleanWrapper(BooleanExpression[] source) {
			this.source = requireNonNull(source);
			buffer = new boolean[source.length];
		}

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public int size() { return buffer.length; }

		@Override
		public boolean isFixedSize() { return true; }

		private static void fillBuffer(boolean[] buffer, BooleanExpression[] source) {
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
		public Primitive<Boolean> get(int index) { return source[index].compute(); }

		@Override
		public boolean getAsBoolean(int index) { return source[index].computeAsBoolean(); }

		@Override
		public Expression<boolean[]> duplicate(EvaluationContext context) {
			return new BooleanWrapper(Stream.of(source)
						.map(ne -> ne.duplicate(context))
						.map(BooleanExpression.class::cast)
						.toArray(BooleanExpression[]::new));
		}

		@Override
		public Expression<boolean[]> optimize(EvaluationContext context) {
			BooleanExpression[] newSource = Stream.of(source)
					.map(ne -> ne.optimize(context))
					.map(BooleanExpression.class::cast)
					.toArray(BooleanExpression[]::new);

			// Optimize to constant
			if(Stream.of(newSource).allMatch(Expression::isConstant)) {
				boolean[] array = new boolean[newSource.length];
				fillBuffer(array, newSource);
				return ArrayLiterals.of(array);
			}

			// We could check whether any of the index expressions has changed...
			return new BooleanWrapper(newSource);
		}
	}

	/** Provides access to a single array element */
	static final class ObjectAccess<T> implements Expression<T> {
		private final ListExpression<?, T> source;
		private final NumericalExpression index;

		public ObjectAccess(ListExpression<?, T> source, NumericalExpression index) {
			this.source = requireNonNull(source);
			this.index = requireNonNull(index);
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.BOOLEAN; }

		@Override
		public T compute() { return source.get(strictToInt(index.computeAsLong())); }

		@SuppressWarnings("unchecked")
		@Override
		public Expression<T> duplicate(EvaluationContext context) {
			return new ObjectAccess<>(
					(ListExpression<?, T>)source.duplicate(context),
					(NumericalExpression)index.duplicate(context));
		}

		@Override
		public Expression<T> optimize(EvaluationContext context) {
			@SuppressWarnings("unchecked")
			ListExpression<?, T> newSource = (ListExpression<?, T>) source.optimize(context);
			NumericalExpression newIndex = (NumericalExpression) index.optimize(context);

			if(newSource.isConstant() && newIndex.isConstant()) {
				return Expressions.constant(newSource.get(strictToInt(newIndex.computeAsLong())));
			}

			if(newSource!=source || newIndex!=index) {
				return new ObjectAccess<>(newSource, newIndex);
			}

			return this;
		}
	}

	@SuppressWarnings("unchecked")
	private static <E> E[] arrayOf(TypeInfo type, int size) {
		return (E[]) Array.newInstance(type.getType().getComponentType(), size);
	}

	private static TypeInfo arrayType(TypeInfo elementType) {
		try {
			return TypeInfo.of(Class.forName("[L"+elementType.getType().getCanonicalName()), true);
		} catch (ClassNotFoundException e) {
			throw new QueryException(GlobalErrorCode.INTERNAL_ERROR,
					"Unable to obtain array type for: "+elementType);
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
			type = arrayType(elementType);
			buffer = index.isFixedSize() ? arrayOf(type, index.size()) : null;
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
		 * allocate a new long array for every invocation!
		 */
		@Override
		public E[] compute() {
			E[] buffer = index.isFixedSize() ? this.buffer : arrayOf(elementType, source.size());
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
			return new ObjectBatchAccess<>(
					(ListExpression<?, E>)source.duplicate(context),
					(IntegerListExpression<?>)index.duplicate(context));
		}

		@Override
		public ListExpression<E[], E> optimize(EvaluationContext context) {
			@SuppressWarnings("unchecked")
			ListExpression<?, E> newSource = (ListExpression<?, E>) source.optimize(context);
			IntegerListExpression<?> newIndex = (IntegerListExpression<?>) index.optimize(context);

			// Optimize to constant
			if(newSource.isConstant() && newIndex.isConstant()) {
				E[] array = arrayOf(elementType, newIndex.size());
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
			type = arrayType(elementType);
			buffer = arrayOf(type, source.length);
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
			return new ObjectWrapper<>(Stream.of(source)
						.map(ne -> ne.duplicate(context))
						.map(Expression.class::cast)
						.toArray(Expression[]::new));
		}

		@Override
		public ListExpression<E[], E> optimize(EvaluationContext context) {
			Expression<E>[] newSource = Stream.of(source)
					.map(ne -> ne.optimize(context))
					.map(Expression.class::cast)
					.toArray(Expression[]::new);

			// Optimize to constant
			if(Stream.of(newSource).allMatch(Expression::isConstant)) {
				E[] array = arrayOf(elementType, newSource.length);
				fillBuffer(array, newSource);
				return ArrayLiterals.ofGeneric(array);
			}

			// We could check whether any of the index expressions has changed...
			return new ObjectWrapper<>(newSource);
		}
	}
}
