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
import de.ims.icarus2.query.api.eval.Expression.BooleanExpression;
import de.ims.icarus2.query.api.eval.Expression.NumericalExpression;
import de.ims.icarus2.query.api.eval.Expression.TextExpression;
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

	public static TextExpression toText(Expression<?> source) {
		if(source instanceof TextExpression) {
			return (TextExpression) source;
		}
		return new TextCast(source, converterFrom(source));
	}

	public static BooleanExpression toBoolean(Expression<?> source) {
		if(source instanceof BooleanExpression) {
			return (BooleanExpression) source;
		}
		return new BooleanCast(source, converterFrom(source));
	}

	public static NumericalExpression toInteger(Expression<?> source) {
		if(source instanceof NumericalExpression && !((NumericalExpression) source).isFPE()) {
			return (NumericalExpression) source;
		}
		return new IntegerCast(source, converterFrom(source));
	}

	public static NumericalExpression toFloatingPoint(Expression<?> source) {
		if(source instanceof NumericalExpression && ((NumericalExpression) source).isFPE()) {
			return (NumericalExpression) source;
		}
		return new FloatingPointCast(source, converterFrom(source));
	}

	private static Converter converterFrom(Expression<?> expression) {
		if(expression.isNumerical()) {
			return ((NumericalExpression)expression).isFPE() ?
					Converter.FROM_FLOAT : Converter.FROM_INTEGER;
		} else if(expression.isBoolean()) {
			return Converter.FROM_BOOLEAN;
		} else if(expression.isText()) {
			return Converter.FROM_TEXT;
		}

		return Converter.FROM_GENERIC;
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

	static final class IntegerCast extends CastExpression<Primitive<? extends Number>> implements NumericalExpression {

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
		public Expression<Primitive<? extends Number>> duplicate(EvaluationContext context) {
			return new IntegerCast(source.duplicate(context), cast);
		}

		@Override
		protected Expression<Primitive<? extends Number>> toConstant(Expression<?> source) {
			return Literals.of(cast.applyAsLong(source));
		}

	}

	static final class FloatingPointCast extends CastExpression<Primitive<? extends Number>> implements NumericalExpression {

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
		public Expression<Primitive<? extends Number>> duplicate(EvaluationContext context) {
			return new FloatingPointCast(source.duplicate(context), cast);
		}

		@Override
		protected Expression<Primitive<? extends Number>> toConstant(Expression<?> source) {
			return Literals.of(cast.applyAsDouble(source));
		}

	}

	static final class BooleanCast extends CastExpression<Primitive<Boolean>> implements BooleanExpression {

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

	static final class TextCast extends CastExpression<CharSequence> implements TextExpression {

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

	/** Provides conversion from a specific type to the 4 casting targets */
	private enum Converter {
		FROM_TEXT(TypeInfo.TEXT,
				exp -> StringPrimitives.parseLong(((TextExpression)exp).compute()),
				exp -> StringPrimitives.parseDouble(((TextExpression)exp).compute()),
				exp -> string2Boolean(((TextExpression)exp).compute()),
				null
		),

		FROM_INTEGER(TypeInfo.INTEGER,
				null,
				exp -> (double)((NumericalExpression)exp).computeAsLong(),
				exp -> int2Boolean(((NumericalExpression)exp).computeAsLong()),
				exp -> String.valueOf(((NumericalExpression)exp).computeAsLong())
		),

		FROM_FLOAT(TypeInfo.FLOATING_POINT,
				exp -> (long)((NumericalExpression)exp).computeAsDouble(),
				null,
				exp -> float2Boolean(((NumericalExpression)exp).computeAsDouble()),
				exp -> String.valueOf(((NumericalExpression)exp).computeAsDouble())
		),

		FROM_BOOLEAN(TypeInfo.BOOLEAN,
				null,
				null,
				null,
				exp -> String.valueOf(((BooleanExpression)exp).computeAsBoolean())
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
}
