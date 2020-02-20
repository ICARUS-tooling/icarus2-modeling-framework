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

import static de.ims.icarus2.query.api.eval.EvaluationUtils.float2Boolean;
import static de.ims.icarus2.query.api.eval.EvaluationUtils.forUnsupportedCast;
import static de.ims.icarus2.query.api.eval.EvaluationUtils.int2Boolean;
import static de.ims.icarus2.query.api.eval.EvaluationUtils.object2Boolean;
import static de.ims.icarus2.query.api.eval.EvaluationUtils.string2Boolean;
import static java.util.Objects.requireNonNull;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.eval.Expression.BooleanListExpression;
import de.ims.icarus2.query.api.eval.Expression.FloatingPointListExpression;
import de.ims.icarus2.query.api.eval.Expression.IntegerListExpression;
import de.ims.icarus2.query.api.eval.Expression.ListExpression;
import de.ims.icarus2.query.api.eval.Expression.PrimitiveExpression;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.MutableDouble;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.Primitive;
import de.ims.icarus2.util.strings.StringPrimitives;

/**
 * Provides conversion and/or casting expressions to convert between different
 * types of expressions.
 * <p>
 * Casting scenarios (cast from row to column):
 *
 * <pre>
 *         |  text  |  boolean  |  integer  |  float  |
 * --------+--------+-----------+-----------+---------+
 * text    |   -    |     p     |     p     |    p    |
 * boolean |   s    |     -     |     f     |    f    |
 * integer |   s    |   f(b*)   |     -     |    c    |
 * float   |   s    |   f(b*)   |     c     |    -    |
 * generic |   s    |   f(b*)   |     f     |    f    |
 * --------+--------+-----------+-----------+---------+
 *
 * c = primitive cast
 * p = primitive parse
 * s = String.valueOf(x) equivalent
 * f = failure
 * b = boolean switch
 * * = depending on engine configuration
 * <pre>
 * @author Markus Gärtner
 *
 */
public class Conversions {

	@SuppressWarnings("unchecked")
	public static Expression<CharSequence> toText(Expression<?> source) {
		if(source.isText()) {
			return (Expression<CharSequence>) source;
		}
		return new TextCast(source, converterFrom(source));
	}

	@SuppressWarnings("unchecked")
	public static ListExpression<?, CharSequence> toTextList(ListExpression<?, ?> source) {
		if(TypeInfo.isText(source.getElementType())) {
			return (ListExpression<?, CharSequence>) source;
		}
		return new TextListCast(source, converterFromList(source));
	}

	@SuppressWarnings("unchecked")
	public static Expression<Primitive<Boolean>> toBoolean(Expression<?> source) {
		if(source.isBoolean()) {
			return (Expression<Primitive<Boolean>>) source;
		}
		return new BooleanCast(source, converterFrom(source));
	}

	public static BooleanListExpression<?> toBooleanList(ListExpression<?, ?> source) {
		if(source instanceof BooleanListExpression) {
			return (BooleanListExpression<?>) source;
		}
		return new BooleanListCast(source, converterFromList(source));
	}

	public static Expression<?> toInteger(Expression<?> source) {
		if(source.isInteger()) {
			return source;
		}
		return new IntegerCast(source, converterFrom(source));
	}

	public static IntegerListExpression<?> toIntegerList(ListExpression<?, ?> source) {
		if(source instanceof IntegerListExpression) {
			return (IntegerListExpression<?>) source;
		}
		return new IntegerListCast(source, converterFromList(source));
	}

	public static Expression<?> toFloatingPoint(Expression<?> source) {
		if(source.isFloatingPoint()) {
			return source;
		}
		return new FloatingPointCast(source, converterFrom(source));
	}

	public static FloatingPointListExpression<?> toFloatingPointList(ListExpression<?, ?> source) {
		if(source instanceof FloatingPointListExpression) {
			return (FloatingPointListExpression<?>) source;
		}
		return new FloatingPointListCast(source, converterFromList(source));
	}

	private static Converter converterFrom(Expression<?> expression) {
		if(expression.isNumerical()) {
			return expression.isFloatingPoint() ?
					Converter.FROM_FLOAT : Converter.FROM_INTEGER;
		} else if(expression.isBoolean()) {
			return Converter.FROM_BOOLEAN;
		} else if(expression.isText()) {
			return Converter.FROM_TEXT;
		}

		return Converter.FROM_GENERIC;
	}

	private static ListConverter converterFromList(ListExpression<?,?> expression) {
		TypeInfo type = expression.getElementType();
		if(TypeInfo.isNumerical(type)) {
			return TypeInfo.isFloatingPoint(type) ?
					ListConverter.FROM_FLOAT : ListConverter.FROM_INTEGER;
		} else if(TypeInfo.isBoolean(type)) {
			return ListConverter.FROM_BOOLEAN;
		} else if(TypeInfo.isText(type)) {
			return ListConverter.FROM_TEXT;
		}

		return ListConverter.FROM_GENERIC;
	}

	private static abstract class CastExpression<T> implements Expression<T> {

		private final TypeInfo type;
		protected final Expression<?> source;

		public CastExpression(TypeInfo type, Expression<?> source) {
			this.type = requireNonNull(type);
			this.source = requireNonNull(source);
		}

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public Expression<T> optimize(EvaluationContext context) {
			Expression<?> newSource = source.optimize(context);
			if(newSource.isConstant()) {
				return toConstant(newSource);
			}
			return this;
		}

		protected abstract Expression<T> toConstant(Expression<?> source);

	}

	private static abstract class ListCastExpression<T,E> implements ListExpression<T,E> {

		private final TypeInfo type;
		protected final ListExpression<?,?> source;

		public ListCastExpression(TypeInfo type, ListExpression<?,?> source) {
			this.type = requireNonNull(type);
			this.source = requireNonNull(source);
		}

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public ListExpression<T,E> optimize(EvaluationContext context) {
			ListExpression<?,?> newSource = (ListExpression<?, ?>) source.optimize(context);
			if(newSource.isConstant()) {
				return toConstant(newSource);
			}
			return this;
		}

		@Override
		public boolean isFixedSize() { return source.isFixedSize(); }

		protected abstract ListExpression<T,E> toConstant(ListExpression<?,?> source);

	}

	static final class IntegerCast extends CastExpression<Primitive<Long>>
			implements PrimitiveExpression {

		private final MutableLong value;
		private final ToLongFunction<Expression<?>> cast;

		public IntegerCast(Expression<?> source, Converter converter) {
			super(TypeInfo.INTEGER, source);
			cast = converter.getToInt();
			value = new MutableLong();
		}

		/** Copy constructor */
		private IntegerCast(Expression<?> source, ToLongFunction<Expression<?>> cast) {
			super(TypeInfo.INTEGER, source);
			this.cast = requireNonNull(cast);
			value = new MutableLong();
		}

		@Override
		public Primitive<Long> compute() {
			value.setLong(computeAsLong());
			return value;
		}

		@Override
		public long computeAsLong() { return cast.applyAsLong(source); }

		@Override
		public double computeAsDouble() { return computeAsLong(); }

		@Override
		public Expression<Primitive<Long>> duplicate(EvaluationContext context) {
			return new IntegerCast(source.duplicate(context), cast);
		}

		@Override
		protected Expression<Primitive<Long>> toConstant(Expression<?> source) {
			return Literals.of(cast.applyAsLong(source));
		}

	}

	static final class IntegerListCast extends ListCastExpression<long[], Primitive<Long>>
			implements IntegerListExpression<long[]> {

		private static final TypeInfo type = TypeInfo.of(long[].class, true);

		private final MutableLong value;
		private final ToLongIndexFunction cast;

		public IntegerListCast(ListExpression<?,?> source, ListConverter converter) {
			super(type, source);
			cast = converter.getToInt();
			value = new MutableLong();
		}

		/** Copy constructor */
		private IntegerListCast(ListExpression<?,?> source, ToLongIndexFunction cast) {
			super(type, source);
			this.cast = requireNonNull(cast);
			value = new MutableLong();
		}

		//TODO suuuper expensive and inefficient, but we can't rly do anything about it?
		@Override
		public long[] compute() {
			long[] array = new long[source.size()];
			for (int i = 0; i < array.length; i++) {
				array[i] = cast.applyAsLong(source, i);
			}
			return array;
		}

		@Override
		public Expression<long[]> duplicate(EvaluationContext context) {
			return new IntegerListCast((ListExpression<?, ?>)source.duplicate(context), cast);
		}

		@Override
		protected ListExpression<long[], Primitive<Long>> toConstant(ListExpression<?,?> source) {
			return ArrayLiterals.of(compute());
		}

		@Override
		public Primitive<Long> get(int index) {
			value.setLong(getAsLong(index));
			return value;
		}

		@Override
		public long getAsLong(int index) { return cast.applyAsLong(source, index); }

		@Override
		public int size() { return source.size(); }
	}

	static final class FloatingPointCast extends CastExpression<Primitive<Double>>
			implements PrimitiveExpression {

		private final MutableDouble value;
		private final ToDoubleFunction<Expression<?>> cast;

		public FloatingPointCast(Expression<?> source, Converter converter) {
			super(TypeInfo.FLOATING_POINT, source);
			cast = converter.getToDouble();
			value = new MutableDouble();
		}

		/** Copy constructor */
		private FloatingPointCast(Expression<?> source, ToDoubleFunction<Expression<?>> cast) {
			super(TypeInfo.FLOATING_POINT, source);
			this.cast = requireNonNull(cast);
			value = new MutableDouble();
		}

		@Override
		public Primitive<Double> compute() {
			value.setDouble(computeAsDouble());
			return value;
		}

		@Override
		public long computeAsLong() { throw forUnsupportedCast(TypeInfo.FLOATING_POINT, TypeInfo.INTEGER); }

		@Override
		public double computeAsDouble() { return cast.applyAsDouble(source); }

		@Override
		public Expression<Primitive<Double>> duplicate(EvaluationContext context) {
			return new FloatingPointCast(source.duplicate(context), cast);
		}

		@Override
		protected Expression<Primitive<Double>> toConstant(Expression<?> source) {
			return Literals.of(cast.applyAsDouble(source));
		}

	}

	static final class FloatingPointListCast extends ListCastExpression<double[], Primitive<Double>>
			implements FloatingPointListExpression<double[]> {

		private static final TypeInfo type = TypeInfo.of(double[].class, true);

		private final MutableDouble value;
		private final ToDoubleIndexFunction cast;

		public FloatingPointListCast(ListExpression<?,?> source, ListConverter converter) {
			super(type, source);
			cast = converter.getToDouble();
			value = new MutableDouble();
		}

		/** Copy constructor */
		private FloatingPointListCast(ListExpression<?,?> source, ToDoubleIndexFunction cast) {
			super(type, source);
			this.cast = requireNonNull(cast);
			value = new MutableDouble();
		}

		//TODO suuuper expensive and inefficient, but we can't rly do anything about it?
		@Override
		public double[] compute() {
			double[] array = new double[source.size()];
			for (int i = 0; i < array.length; i++) {
				array[i] = cast.applyAsDouble(source, i);
			}
			return array;
		}

		@Override
		public Expression<double[]> duplicate(EvaluationContext context) {
			return new FloatingPointListCast((ListExpression<?, ?>)source.duplicate(context), cast);
		}

		@Override
		protected ListExpression<double[], Primitive<Double>> toConstant(ListExpression<?,?> source) {
			return ArrayLiterals.of(compute());
		}

		@Override
		public Primitive<Double> get(int index) {
			value.setDouble(getAsDouble(index));
			return value;
		}

		@Override
		public double getAsDouble(int index) { return cast.applyAsDouble(source, index); }

		@Override
		public int size() { return source.size(); }
	}

	static final class BooleanCast extends CastExpression<Primitive<Boolean>>
			implements PrimitiveExpression {

		private final MutableBoolean value;
		private final Predicate<Expression<?>> cast;

		public BooleanCast(Expression<?> source, Converter converter) {
			super(TypeInfo.BOOLEAN, source);
			cast = converter.getToBoolean();
			value = new MutableBoolean();
		}

		/** Copy constructor */
		private BooleanCast(Expression<?> source, Predicate<Expression<?>> cast) {
			super(TypeInfo.BOOLEAN, source);
			this.cast = requireNonNull(cast);
			value = new MutableBoolean();
		}

		@Override
		public boolean computeAsBoolean() { return cast.test(source); }

		@Override
		public Primitive<Boolean> compute() {
			value.setBoolean(computeAsBoolean());
			return value;
		}

		@Override
		public Expression<Primitive<Boolean>> duplicate(EvaluationContext context) {
			return new BooleanCast(source.duplicate(context), cast);
		}

		@Override
		protected Expression<Primitive<Boolean>> toConstant(Expression<?> source) {
			return Literals.of(cast.test(source));
		}

	}

	static final class BooleanListCast extends ListCastExpression<boolean[], Primitive<Boolean>>
			implements BooleanListExpression<boolean[]> {

		private static final TypeInfo type = TypeInfo.of(boolean[].class, true);

		private final MutableBoolean value;
		private final ListPredicate cast;

		public BooleanListCast(ListExpression<?,?> source, ListConverter converter) {
			super(type, source);
			cast = converter.getToBoolean();
			value = new MutableBoolean();
		}

		/** Copy constructor */
		private BooleanListCast(ListExpression<?,?> source, ListPredicate cast) {
			super(type, source);
			this.cast = requireNonNull(cast);
			value = new MutableBoolean();
		}

		//TODO suuuper expensive and inefficient, but we can't rly do anything about it?
		@Override
		public boolean[] compute() {
			boolean[] array = new boolean[source.size()];
			for (int i = 0; i < array.length; i++) {
				array[i] = cast.test(source, i);
			}
			return array;
		}

		@Override
		public Expression<boolean[]> duplicate(EvaluationContext context) {
			return new BooleanListCast((ListExpression<?, ?>)source.duplicate(context), cast);
		}

		@Override
		protected ListExpression<boolean[], Primitive<Boolean>> toConstant(ListExpression<?,?> source) {
			return ArrayLiterals.of(compute());
		}

		@Override
		public Primitive<Boolean> get(int index) {
			value.setBoolean(getAsBoolean(index));
			return value;
		}

		@Override
		public boolean getAsBoolean(int index) { return cast.test(source, index); }

		@Override
		public int size() { return source.size(); }
	}

	static final class TextCast extends CastExpression<CharSequence> implements Expression<CharSequence> {

		private final Function<Expression<?>, CharSequence> cast;

		public TextCast(Expression<?> source, Converter converter) {
			super(TypeInfo.TEXT, source);
			cast = converter.getToString();
		}

		/** Copy constructor */
		private TextCast(Expression<?> source, Function<Expression<?>, CharSequence> cast) {
			super(TypeInfo.TEXT, source);
			this.cast = requireNonNull(cast);
		}

		@Override
		public CharSequence compute() {
			return cast.apply(source);
		}

		@Override
		public Expression<CharSequence> duplicate(EvaluationContext context) {
			return new TextCast(source.duplicate(context), cast);
		}

		@Override
		protected Expression<CharSequence> toConstant(Expression<?> source) {
			return Literals.of(cast.apply(source));
		}

	}

	static final class TextListCast extends ListCastExpression<CharSequence[], CharSequence> {

		private static final TypeInfo type = TypeInfo.of(CharSequence[].class, true);

		private final ToTextIndexFunction cast;

		public TextListCast(ListExpression<?,?> source, ListConverter converter) {
			super(type, source);
			cast = converter.getToString();
		}

		/** Copy constructor */
		private TextListCast(ListExpression<?,?> source, ToTextIndexFunction cast) {
			super(type, source);
			this.cast = requireNonNull(cast);
		}

		//TODO suuuper expensive and inefficient, but we can't rly do anything about it?
		@Override
		public CharSequence[] compute() {
			CharSequence[] array = new CharSequence[source.size()];
			for (int i = 0; i < array.length; i++) {
				array[i] = cast.applyAsText(source, i);
			}
			return array;
		}

		@Override
		public TypeInfo getElementType() { return TypeInfo.TEXT; }

		@Override
		public Expression<CharSequence[]> duplicate(EvaluationContext context) {
			return new TextListCast((ListExpression<?, ?>)source.duplicate(context), cast);
		}

		@Override
		protected ListExpression<CharSequence[], CharSequence> toConstant(ListExpression<?,?> source) {
			return ArrayLiterals.ofGeneric(compute());
		}

		@Override
		public CharSequence get(int index) { return cast.applyAsText(source, index); }

		@Override
		public int size() { return source.size(); }
	}

	/** Provides conversion from a specific type to the 4 casting targets */
	private enum Converter {
		@SuppressWarnings("unchecked")
		FROM_TEXT(TypeInfo.TEXT,
				exp -> StringPrimitives.parseLong(((Expression<CharSequence>)exp).compute()),
				exp -> StringPrimitives.parseDouble(((Expression<CharSequence>)exp).compute()),
				exp -> string2Boolean(((Expression<CharSequence>)exp).compute()),
				null
		),

		FROM_INTEGER(TypeInfo.INTEGER,
				null,
				exp -> (double)exp.computeAsLong(),
				exp -> int2Boolean(exp.computeAsLong()),
				exp -> String.valueOf(exp.computeAsLong())
		),

		FROM_FLOAT(TypeInfo.FLOATING_POINT,
				exp -> (long)exp.computeAsDouble(),
				null,
				exp -> float2Boolean(exp.computeAsDouble()),
				exp -> String.valueOf(exp.computeAsDouble())
		),

		FROM_BOOLEAN(TypeInfo.BOOLEAN,
				null,
				null,
				null,
				exp -> String.valueOf(exp.computeAsBoolean())
		),

		FROM_GENERIC(TypeInfo.GENERIC,
				null,
				null,
				exp -> object2Boolean(exp.compute()),
				exp -> String.valueOf(exp.compute())
		),
		;

		private final TypeInfo origin;

		private final ToLongFunction<Expression<?>> toInt;
		private final ToDoubleFunction<Expression<?>> toDouble;
		private final Predicate<Expression<?>> toBoolean;
		private final Function<Expression<?>, CharSequence> toString;

		private Converter(TypeInfo origin, ToLongFunction<Expression<?>> toInt, ToDoubleFunction<Expression<?>> toDouble,
				Predicate<Expression<?>> toBoolean, Function<Expression<?>, CharSequence> toString) {
			this.origin = requireNonNull(origin);
			this.toInt = toInt;
			this.toDouble = toDouble;
			this.toBoolean = toBoolean;
			this.toString = toString;
		}

		private <T> T expect(T cast, TypeInfo target) {
			if(cast==null)
				throw new QueryException(QueryErrorCode.INCORRECT_USE,
						String.format("Cannot cast %s to target type %s", origin, target));
			return cast;
		}

		public ToLongFunction<Expression<?>> getToInt() { return expect(toInt, TypeInfo.INTEGER); }

		public ToDoubleFunction<Expression<?>> getToDouble() { return expect(toDouble, TypeInfo.FLOATING_POINT); }

		public Predicate<Expression<?>> getToBoolean() { return expect(toBoolean, TypeInfo.BOOLEAN); }

		public Function<Expression<?>, CharSequence> getToString() { return expect(toString, TypeInfo.TEXT); }
	}

	@FunctionalInterface
	private interface ToLongIndexFunction {
		long applyAsLong(ListExpression<?, ?> expression, int index);
	}

	@FunctionalInterface
	private interface ToDoubleIndexFunction {
		double applyAsDouble(ListExpression<?, ?> expression, int index);
	}

	@FunctionalInterface
	private interface ToTextIndexFunction {
		CharSequence applyAsText(ListExpression<?, ?> expression, int index);
	}

	@FunctionalInterface
	private interface ListPredicate {
		boolean test(ListExpression<?, ?> expression, int index);
	}

	/** Provides conversion from a specific type to the 4 casting targets */
	private enum ListConverter {
		@SuppressWarnings("unchecked")
		FROM_TEXT(TypeInfo.TEXT,
				(exp, i) -> StringPrimitives.parseLong(((ListExpression<?, CharSequence>)exp).get(i)),
				(exp, i) -> StringPrimitives.parseDouble(((ListExpression<?, CharSequence>)exp).get(i)),
				(exp, i) -> string2Boolean(((ListExpression<?, CharSequence>)exp).get(i)),
				null
		),

		FROM_INTEGER(TypeInfo.INTEGER,
				null,
				(exp, i) -> (double)((IntegerListExpression<?>)exp).getAsLong(i),
				(exp, i) -> int2Boolean(((IntegerListExpression<?>)exp).getAsLong(i)),
				(exp, i) -> String.valueOf(((IntegerListExpression<?>)exp).getAsLong(i))
		),

		FROM_FLOAT(TypeInfo.FLOATING_POINT,
				(exp, i) -> (long)((FloatingPointListExpression<?>)exp).getAsDouble(i),
				null,
				(exp, i) -> float2Boolean(((FloatingPointListExpression<?>)exp).getAsDouble(i)),
				(exp, i) -> String.valueOf(((FloatingPointListExpression<?>)exp).getAsDouble(i))
		),

		FROM_BOOLEAN(TypeInfo.BOOLEAN,
				null,
				null,
				null,
				(exp, i) -> String.valueOf(((BooleanListExpression<?>)exp).getAsBoolean(i))
		),

		FROM_GENERIC(TypeInfo.GENERIC,
				null,
				null,
				(exp, i) -> object2Boolean(exp.get(i)),
				(exp, i) -> String.valueOf(exp.get(i))
		),
		;

		private final TypeInfo origin;

		private final ToLongIndexFunction toInt;
		private final ToDoubleIndexFunction toDouble;
		private final ListPredicate toBoolean;
		private final ToTextIndexFunction toString;

		private ListConverter(TypeInfo origin,
				ToLongIndexFunction toInt,
				ToDoubleIndexFunction toDouble,
				ListPredicate toBoolean,
				ToTextIndexFunction toString) {
			this.origin = requireNonNull(origin);
			this.toInt = toInt;
			this.toDouble = toDouble;
			this.toBoolean = toBoolean;
			this.toString = toString;
		}

		private <T> T expect(T cast, TypeInfo target) {
			if(cast==null)
				throw new QueryException(QueryErrorCode.INCORRECT_USE,
						String.format("Cannot cast list of %s to target list type %s", origin, target));
			return cast;
		}

		public ToLongIndexFunction getToInt() { return expect(toInt, TypeInfo.INTEGER); }

		public ToDoubleIndexFunction getToDouble() { return expect(toDouble, TypeInfo.FLOATING_POINT); }

		public ListPredicate getToBoolean() { return expect(toBoolean, TypeInfo.BOOLEAN); }

		public ToTextIndexFunction getToString() { return expect(toString, TypeInfo.TEXT); }
	}
}
