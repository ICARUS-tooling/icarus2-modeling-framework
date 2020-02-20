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
 * @author Markus Gärtner
 *
 */
public class UnaryOperations {

	public static Expression<?> minus(Expression<?> source) {
		requireNonNull(source);
		if(source.isFloatingPoint()) {
			if(source.isConstant()) {
				return Literals.of(-source.computeAsDouble());
			}
			return new FloatingPointNegation(source);
		} else if(source.isConstant()) {
			return Literals.of(-source.computeAsLong());
		}
		return new IntegerNegation(source);
	}

	public static Expression<Primitive<Boolean>> not(Expression<Primitive<Boolean>> source) {
		requireNonNull(source);
		if(source.isConstant()) {
			return Literals.of(!source.computeAsBoolean());
		}
		return new BooleanNegation(source);
	}

	public static Expression<?> bitwiseNot(Expression<?> source) {
		requireNonNull(source);
		if(source.isConstant()) {
			return Literals.of(~source.computeAsLong());
		}
		return new IntegerBitwiseNegation(source);
	}

	static class IntegerBitwiseNegation implements Expression<Primitive<Long>>, PrimitiveExpression {

		private final Expression<?> source;
		private final MutableLong value;

		IntegerBitwiseNegation(Expression<?> source) {
			this.source = requireNonNull(source);
			value = new MutableLong();
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.INTEGER; }

		@Override
		public Primitive<Long> compute() {
			value.setLong(computeAsLong());
			return value;
		}

		@Override
		public Expression<Primitive<Long>> duplicate(EvaluationContext context) {
			return new IntegerBitwiseNegation(source.duplicate(context));
		}

		@Override
		public long computeAsLong() { return ~source.computeAsLong(); }

		@Override
		public double computeAsDouble() { return computeAsLong(); }

		@Override
		public Expression<Primitive<Long>> optimize(EvaluationContext context) {
			Expression<?> newSource = source.optimize(context);
			if(newSource.isConstant()) {
				return Literals.of(~newSource.computeAsLong());
			}

			return this;
		}

	}

	static class IntegerNegation implements Expression<Primitive<Long>>, PrimitiveExpression {

		private final Expression<?> source;
		private final MutableLong value;

		IntegerNegation(Expression<?> source) {
			this.source = requireNonNull(source);
			value = new MutableLong();
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.INTEGER; }

		@Override
		public Primitive<Long> compute() {
			value.setLong(computeAsLong());
			return value;
		}

		@Override
		public Expression<Primitive<Long>> duplicate(EvaluationContext context) {
			return new IntegerNegation(source.duplicate(context));
		}

		@Override
		public long computeAsLong() { return -source.computeAsLong(); }

		@Override
		public double computeAsDouble() { return -source.computeAsDouble(); }

		@Override
		public Expression<Primitive<Long>> optimize(EvaluationContext context) {
			Expression<?> newSource = source.optimize(context);
			if(newSource.isConstant()) {
				return Literals.of(-newSource.computeAsLong());
			}

			return this;
		}

	}

	static class FloatingPointNegation implements Expression<Primitive<Double>>, PrimitiveExpression {

		private final Expression<?> source;
		private final MutableDouble value;

		FloatingPointNegation(Expression<?> source) {
			this.source = requireNonNull(source);
			value = new MutableDouble();
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.FLOATING_POINT; }

		@Override
		public Primitive<Double> compute() {
			value.setDouble(computeAsDouble());
			return value;
		}

		@Override
		public Expression<Primitive<Double>> duplicate(EvaluationContext context) {
			return new FloatingPointNegation(source.duplicate(context));
		}

		@Override
		public long computeAsLong() { throw EvaluationUtils.forUnsupportedCast(
				TypeInfo.FLOATING_POINT, TypeInfo.INTEGER); }

		@Override
		public double computeAsDouble() { return -source.computeAsDouble(); }

		@Override
		public Expression<Primitive<Double>> optimize(EvaluationContext context) {
			Expression<?> newSource =source.optimize(context);
			if(newSource.isConstant()) {
				return Literals.of(-newSource.computeAsDouble());
			}

			return this;
		}

	}

	static class BooleanNegation implements Expression<Primitive<Boolean>>, PrimitiveExpression {

		private final Expression<Primitive<Boolean>> source;
		private final MutableBoolean value;

		BooleanNegation(Expression<Primitive<Boolean>> source) {
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
		public Expression<Primitive<Boolean>> duplicate(EvaluationContext context) {
			return new BooleanNegation(source.duplicate(context));
		}

		@Override
		public boolean computeAsBoolean() { return !source.computeAsBoolean(); }

		@Override
		public Expression<Primitive<Boolean>> optimize(EvaluationContext context) {
			Expression<Primitive<Boolean>> newSource = source.optimize(context);
			if(newSource.isConstant()) {
				return Literals.of(!newSource.computeAsBoolean());
			}

			return this;
		}

	}
}
