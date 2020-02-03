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
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.MutableDouble;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.Primitive;

/**
 * @author Markus Gärtner
 *
 */
public class UnaryOperations {

	public static NumericalExpression minus(NumericalExpression source) {
		requireNonNull(source);
		if(source.isFPE()) {
			if(source.isConstant()) {
				return Literals.of(-source.computeAsDouble());
			}
			return new FloatingPointNegation(source);
		} else if(source.isConstant()) {
			return Literals.of(-source.computeAsLong());
		}
		return new IntegerNegation(source);
	}

	public static BooleanExpression not(BooleanExpression source) {
		requireNonNull(source);
		if(source.isConstant()) {
			return Literals.of(!source.computeAsBoolean());
		}
		return new BooleanNegation(source);
	}

	public static NumericalExpression bitwiseNot(NumericalExpression source) {
		requireNonNull(source);
		if(source.isConstant()) {
			return Literals.of(~source.computeAsLong());
		}
		return new IntegerBitwiseNegation(source);
	}

	static class IntegerBitwiseNegation implements NumericalExpression {

		private final NumericalExpression source;
		private final MutableLong value;

		IntegerBitwiseNegation(NumericalExpression source) {
			this.source = requireNonNull(source);
			value = new MutableLong();
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.LONG; }

		@Override
		public Primitive<? extends Number> compute() {
			value.setLong(computeAsLong());
			return value;
		}

		@Override
		public NumericalExpression duplicate(EvaluationContext context) {
			return new IntegerBitwiseNegation((NumericalExpression) source.duplicate(context));
		}

		@Override
		public long computeAsLong() { return ~source.computeAsLong(); }

		@Override
		public double computeAsDouble() { return computeAsLong(); }

	}

	static class IntegerNegation implements NumericalExpression {

		private final NumericalExpression source;
		private final MutableLong value;

		IntegerNegation(NumericalExpression source) {
			this.source = requireNonNull(source);
			value = new MutableLong();
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.LONG; }

		@Override
		public Primitive<? extends Number> compute() {
			value.setLong(computeAsLong());
			return value;
		}

		@Override
		public NumericalExpression duplicate(EvaluationContext context) {
			return new IntegerNegation((NumericalExpression) source.duplicate(context));
		}

		@Override
		public long computeAsLong() { return -source.computeAsLong(); }

		@Override
		public double computeAsDouble() { return -source.computeAsDouble(); }

	}

	static class FloatingPointNegation implements NumericalExpression {

		private final NumericalExpression source;
		private final MutableDouble value;

		FloatingPointNegation(NumericalExpression source) {
			this.source = requireNonNull(source);
			value = new MutableDouble();
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.DOUBLE; }

		@Override
		public Primitive<? extends Number> compute() {
			value.setDouble(computeAsDouble());
			return value;
		}

		@Override
		public NumericalExpression duplicate(EvaluationContext context) {
			return new FloatingPointNegation((NumericalExpression) source.duplicate(context));
		}

		@Override
		public long computeAsLong() { return -source.computeAsLong(); }

		@Override
		public double computeAsDouble() { return -source.computeAsDouble(); }

	}

	static class BooleanNegation implements BooleanExpression {

		private final BooleanExpression source;
		private final MutableBoolean value;

		BooleanNegation(BooleanExpression source) {
			this.source = requireNonNull(source);
			value = new MutableBoolean();
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.BOOLEAN; }

		@Override
		public Primitive<Boolean> compute() {
			value.setBoolean(computeAsBoolean());
			return value;
		}

		@Override
		public BooleanExpression duplicate(EvaluationContext context) {
			return new BooleanNegation((BooleanExpression) source.duplicate(context));
		}

		@Override
		public boolean computeAsBoolean() { return !source.computeAsBoolean(); }

	}
}
