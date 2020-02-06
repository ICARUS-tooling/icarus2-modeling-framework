/**
 *
 */
package de.ims.icarus2.query.api.eval;

import de.ims.icarus2.query.api.eval.Expression.TextExpression;
import de.ims.icarus2.util.strings.CodePointSequence;

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
			final CodePointSequence value = CodePointSequence.fixed(text);

			@Override
			public Expression<CodePointSequence> duplicate(EvaluationContext context) {
				return this;
			}

			@Override
			public CodePointSequence compute() { return value; }

			@Override
			public CharSequence computeAsChars() { return value; }
		};
	}

	static TextExpression optimizable(String text) {
		return new TextExpression() {
			final CodePointSequence value = CodePointSequence.fixed(text);

			@Override
			public Expression<CodePointSequence> duplicate(EvaluationContext context) {
				return this;
			}

			@Override
			public CodePointSequence compute() { return value; }

			@Override
			public CharSequence computeAsChars() { return value; }

			@Override
			public Expression<CodePointSequence> optimize(EvaluationContext context) {
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
