/**
 *
 */
package de.ims.icarus2.query.api.eval;

import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;

import de.ims.icarus2.query.api.eval.Expression.IntegerListExpression;
import de.ims.icarus2.query.api.eval.Expression.NumericalExpression;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.Primitive;

/**
 * @author Markus Gärtner
 *
 */
public final class ArrayAccess {

	private ArrayAccess() { /* no-op */ }

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

	/** Provides access to a single array element */
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

	/** Provides access to a single array element */
	static final class IntegerWrapper implements IntegerListExpression<long[]> {
		private final IntegerListExpression<?> source;
		private final NumericalExpression[] index;
		private final long[] buffer;
		private final MutableLong value;

		private static final TypeInfo type = TypeInfo.of(long[].class, true);

		public IntegerWrapper(IntegerListExpression<?> source, NumericalExpression[] index) {
			this.source = requireNonNull(source);
			this.index = requireNonNull(index);
			buffer = new long[index.length];
			value = new MutableLong();
		}

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public int size() { return buffer.length; }

		@Override
		public boolean isFixedSize() { return true; }

		private static void fillBuffer(long[] buffer, IntegerListExpression<?> source,
				NumericalExpression[] index) {
			for (int i = 0; i < buffer.length; i++) {
				buffer[i] = source.getAsLong(strictToInt(index[i].computeAsLong()));
			}
		}

		/**
		 * If the underlying index expression is not of fixed size, this method will
		 * allocate a new long array for every invocation!
		 */
		@Override
		public long[] compute() {
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
			return source.getAsLong(strictToInt(this.index[index].computeAsLong()));
		}

		@Override
		public Expression<long[]> duplicate(EvaluationContext context) {
			return new IntegerWrapper(
					(IntegerListExpression<?>)source.duplicate(context),
					Stream.of(index)
						.map(ne -> ne.duplicate(context))
						.map(NumericalExpression.class::cast)
						.toArray(NumericalExpression[]::new));
		}

		@Override
		public Expression<long[]> optimize(EvaluationContext context) {
			IntegerListExpression<?> newSource = (IntegerListExpression<?>) source.optimize(context);
			NumericalExpression[] newIndex = Stream.of(index)
					.map(ne -> ne.optimize(context))
					.map(NumericalExpression.class::cast)
					.toArray(NumericalExpression[]::new);

			// Optimize to constant
			if(newSource.isConstant() && Stream.of(newIndex).allMatch(Expression::isConstant)) {
				long[] array = new long[newIndex.length];
				fillBuffer(array, newSource, newIndex);
				return ArrayLiterals.of(array);
			}

			// We could check whether any of the index expressions has changed...
			return new IntegerWrapper(newSource, newIndex);
		}
	}
}
