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

import de.ims.icarus2.query.api.eval.Expression.PrimitiveExpression;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.MutableDouble;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.Primitive;

/**
 * Provides methods to obtain constant literals for every defined IQL type.
 *
 * @author Markus Gärtner
 *
 */
public final class Literals {

	private Literals() { /* no-op */ }

	public static boolean isLiteral(Expression<?> expression) {
		return expression instanceof Literal;
	}

	static abstract class Literal<T> implements Expression<T> {

		@Override
		public Expression<T> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return this;
		}

		/** Literals are inherently constant */
		@Override
		public boolean isConstant() { return true; }

		@Override
		public String toString() { return String.valueOf(compute()); }
	}

	public static Expression<Object> ofNull() {
		return NULL_LITERAL;
	}

	private static final NullLiteral NULL_LITERAL = new NullLiteral();

	static final class NullLiteral extends Literal<Object> {

		@Override
		public TypeInfo getResultType() { return TypeInfo.NULL; }

		@Override
		public Object compute() { return null; }
	}

	public static Expression<CharSequence> of(CharSequence value) {
		return new StringLiteral(value);
	}

	static final class StringLiteral extends Literal<CharSequence> {

		private final CharSequence value;

		StringLiteral(CharSequence value) {
			this.value = requireNonNull(value);
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.TEXT; }

		@Override
		public CharSequence compute() { return value; }
	}

	public static Expression<Primitive<Boolean>> of(boolean value) {
		return value ? TRUE : FALSE;
	}

	private static final BooleanLiteral TRUE = new BooleanLiteral(true);
	private static final BooleanLiteral FALSE = new BooleanLiteral(false);

	static final class BooleanLiteral extends Literal<Primitive<Boolean>>
			implements PrimitiveExpression  {

		private final MutableBoolean value;

		BooleanLiteral(boolean value) {
			this.value = new MutableBoolean(value);
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.BOOLEAN; }

		@Override
		public Primitive<Boolean> compute() { return value; }

		@Override
		public boolean computeAsBoolean() { return value.booleanValue(); }
	}

	public static Expression<Primitive<Long>> of(long value) {
		return new IntegerLiteral(value);
	}

	static final class IntegerLiteral extends Literal<Primitive<Long>>
			implements PrimitiveExpression {

		private final MutableLong value;

		IntegerLiteral(long value) {
			this.value = new MutableLong(value);
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.INTEGER; }

		@Override
		public Primitive<Long> compute() { return value; }

		@Override
		public long computeAsLong() { return value.longValue(); }

		@Override
		public double computeAsDouble() { return value.doubleValue(); }
	}

	public static Expression<Primitive<Double>> of(double value) {
		return new FloatingPointLiteral(value);
	}

	static final class FloatingPointLiteral extends Literal<Primitive<Double>>
			implements PrimitiveExpression  {

		private final MutableDouble value;

		FloatingPointLiteral(double value) {
			this.value = new MutableDouble(value);
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.FLOATING_POINT; }

		@Override
		public Primitive<Double> compute() { return value; }

		@Override
		public double computeAsDouble() { return value.doubleValue(); }
	}
}
