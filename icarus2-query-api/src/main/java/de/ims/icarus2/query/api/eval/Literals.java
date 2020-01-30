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

	public static class NumericalLiteral <N extends Number> extends Literal<N> implements NumericalExpression<N> {

		private final N value;

		public NumericalLiteral(TypeInfo type, N value) {
			super(type);
			this.value = value;
		}

		@Override
		public N compute() { return value; }

//		@Override
//		public int computeAsInt() { return value.intValue(); }

		@Override
		public long computeAsLong() { return value.longValue(); }

//		@Override
//		public float computeAsFloat() { return value.floatValue(); }

		@Override
		public double computeAsDouble() { return value.doubleValue(); }
	}
}
