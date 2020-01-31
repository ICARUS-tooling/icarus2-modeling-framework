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

import de.ims.icarus2.query.api.eval.Expression.BooleanExpression;
import de.ims.icarus2.query.api.eval.Expression.NumericalExpression;
import de.ims.icarus2.util.MutablePrimitives.MutableDouble;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.Primitive;

/**
 * @author Markus Gärtner
 *
 */
public final class Literals {

	private Literals() { /* no-op */ }

	public static abstract class Literal<T> implements Expression<T> {
		private final TypeInfo type;

		protected Literal(TypeInfo type) {
			this.type = requireNonNull(type);
		}

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public Expression<T> duplicate(EvaluationContext ctx) { return this; }

		/** Literals are inherently constant */
		@Override
		public boolean isConstant() { return true; }
	}

	public static class NullLiteral extends Literal<Object> {

		public NullLiteral() { super(TypeInfo.NULL); }

		@Override
		public Object compute() { return null; }
	}

	public static class StringLiteral extends Literal<String> {

		private final String value;

		public StringLiteral(String value) {
			super(TypeInfo.STRING);
			this.value = requireNonNull(value);
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.STRING; }

		@Override
		public String compute() { return value; }
	}

	public static class BooleanLiteral extends Literal<Boolean> implements BooleanExpression {

		private final boolean value;

		public BooleanLiteral(boolean value) {
			super(TypeInfo.BOOLEAN);
			this.value = value;
		}

		@Override
		public Boolean compute() { return Boolean.valueOf(value); }

		@Override
		public boolean computeAsBoolean() { return value; }
	}

	public static class IntegerLiteral extends Literal<Primitive<Long>> implements NumericalExpression<Long> {

		private final MutableLong value;

		public IntegerLiteral(long value) {
			super(TypeInfo.LONG);
			this.value = new MutableLong(value);
		}

		@Override
		public Primitive<Long> compute() { return value; }

		@Override
		public long computeAsLong() { return value.longValue(); }

		@Override
		public double computeAsDouble() { return value.doubleValue(); }
	}

	public static class FloatingPointLiteral extends Literal<Primitive<Double>> implements NumericalExpression<Double> {

		private final MutableDouble value;

		public FloatingPointLiteral(double value) {
			super(TypeInfo.DOUBLE);
			this.value = new MutableDouble(value);
		}

		@Override
		public Primitive<Double> compute() { return value; }

		@Override
		public long computeAsLong() { return value.longValue(); }

		@Override
		public double computeAsDouble() { return value.doubleValue(); }
	}
}
