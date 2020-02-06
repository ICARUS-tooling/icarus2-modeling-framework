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

import de.ims.icarus2.query.api.eval.Expression.TextExpression;

/**
 * @author Markus Gärtner
 *
 */
public class ExpressionTestUtils {

	static Expression<Object> generic(String toStringValue) {
		Object dummy = new Object() {
			@Override
			public String toString() { return toStringValue; }
		};
		return new Expression<Object>() {

			@Override
			public TypeInfo getResultType() { return TypeInfo.GENERIC; }

			@Override
			public Object compute() { return dummy; }

			@Override
			public Expression<Object> duplicate(EvaluationContext context) { return this; }

			@Override
			public boolean isConstant() { return true; }
		};
	}

	static TextExpression fixed(String text) {
		return new TextExpression() {
			final CharSequence value = text;

			@Override
			public Expression<CharSequence> duplicate(EvaluationContext context) {
				return this;
			}

			@Override
			public CharSequence compute() { return value; }
		};
	}

	static TextExpression optimizable(String text) {
		return new TextExpression() {
			final CharSequence value = text;

			@Override
			public Expression<CharSequence> duplicate(EvaluationContext context) {
				return this;
			}

			@Override
			public CharSequence compute() { return value; }

			@Override
			public Expression<CharSequence> optimize(EvaluationContext context) {
				return Literals.of(value);
			}
		};
	}

	static TextExpression dynamic(Object dummy) {
		Expression<Object> expression = new Expression<Object>() {

			@Override
			public TypeInfo getResultType() { return TypeInfo.of(dummy.getClass()); }

			@Override
			public Expression<Object> duplicate(EvaluationContext context) {
				return this;
			}

			@Override
			public Object compute() { return dummy; }
		};

		return Conversions.toText(expression);
	}

	static Expression<?> raw(Object dummy) {
		return new Expression<Object>() {

			@Override
			public TypeInfo getResultType() { return TypeInfo.of(dummy.getClass()); }

			@Override
			public Expression<Object> duplicate(EvaluationContext context) {
				return this;
			}

			@Override
			public Object compute() { return dummy; }
		};
	}

}
