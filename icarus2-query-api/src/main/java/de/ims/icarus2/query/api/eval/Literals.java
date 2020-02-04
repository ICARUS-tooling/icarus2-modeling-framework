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
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;

import de.ims.icarus2.query.api.eval.Expression.BooleanExpression;
import de.ims.icarus2.query.api.eval.Expression.NumericalExpression;
import de.ims.icarus2.query.api.eval.Expression.TextExpression;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.MutableDouble;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.Primitive;
import de.ims.icarus2.util.strings.CodePointSequence;

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
		public Expression<T> duplicate(EvaluationContext ctx) { return this; }

		/** Literals are inherently constant */
		@Override
		public boolean isConstant() { return true; }
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

	public static TextExpression of(CharSequence value) {
		return new StringLiteral(CodePointSequence.fixed(value.toString()));
	}

	public static TextExpression of(String value) {
		return new StringLiteral(CodePointSequence.fixed(value));
	}

	@VisibleForTesting
	static TextExpression of(CodePointSequence codepoints) {
		return new StringLiteral(codepoints);
	}

	static final class StringLiteral extends Literal<CodePointSequence> implements TextExpression {

		private final CodePointSequence value;

		StringLiteral(CodePointSequence value) {
			this.value = requireNonNull(value);
		}

		@Override
		public CodePointSequence compute() { return value; }

		@Override
		public CharSequence computeAsChars() { return value; }
	}

	public static BooleanExpression of(boolean value) {
		return value ? TRUE : FALSE;
	}

	private static final BooleanLiteral TRUE = new BooleanLiteral(true);
	private static final BooleanLiteral FALSE = new BooleanLiteral(false);

	static final class BooleanLiteral extends Literal<Primitive<Boolean>> implements BooleanExpression {

		private final MutableBoolean value;

		BooleanLiteral(boolean value) {
			this.value = new MutableBoolean(value);
		}

		@Override
		public Primitive<Boolean> compute() { return value; }

		@Override
		public boolean computeAsBoolean() { return value.booleanValue(); }
	}

	public static NumericalExpression of(long value) {
		return new IntegerLiteral(value);
	}

	static final class IntegerLiteral extends Literal<Primitive<? extends Number>> implements NumericalExpression {

		private final MutableLong value;

		IntegerLiteral(long value) {
			this.value = new MutableLong(value);
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.LONG; }

		@Override
		public Primitive<Long> compute() { return value; }

		@Override
		public long computeAsLong() { return value.longValue(); }

		@Override
		public double computeAsDouble() { return value.doubleValue(); }
	}

	public static NumericalExpression of(double value) {
		return new FloatingPointLiteral(value);
	}

	static final class FloatingPointLiteral extends Literal<Primitive<? extends Number>> implements NumericalExpression {

		private final MutableDouble value;

		FloatingPointLiteral(double value) {
			this.value = new MutableDouble(value);
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.DOUBLE; }

		@Override
		public Primitive<Double> compute() { return value; }

		@Override
		public long computeAsLong() { throw forUnsupportedCast(TypeInfo.DOUBLE, TypeInfo.LONG); }

		@Override
		public double computeAsDouble() { return value.doubleValue(); }
	}
}
