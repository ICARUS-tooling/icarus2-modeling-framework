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

import java.util.function.Consumer;
import java.util.function.IntFunction;

import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.eval.Expression.BooleanExpression;
import de.ims.icarus2.query.api.eval.Expression.NumericalExpression;
import de.ims.icarus2.query.api.eval.Expression.TextExpression;

/**
 * @author Markus Gärtner
 *
 */
public class EvaluationUtils {

	static void checkIntegerType(Expression<?> exp) {
		if(exp.getResultType()!=TypeInfo.INTEGER)
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not a proper integer expression: "+exp.getResultType());
	}

	static void checkFloatingPointType(Expression<?> exp) {
		if(exp.getResultType()!=TypeInfo.FLOATING_POINT)
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not a proper floating point expression: "+exp.getResultType());
	}

	static void checkNumericalType(Expression<?> exp) {
		if(!exp.isNumerical())
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not a proper numerical expression: "+exp.getResultType());
	}

	static void checkBooleanType(Expression<?> exp) {
		if(!exp.isBoolean())
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not a proper boolean expression: "+exp.getResultType());
	}

	static void checkTextType(Expression<?> exp) {
		if(!exp.isText())
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not a proper text expression: "+exp.getResultType());
	}

	static void checkListType(Expression<?> exp) {
		if(!exp.getResultType().isList())
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not a proper list expression: "+exp.getResultType());
	}

	static void checkComparableType(Expression<?> exp) {
		if(!TypeInfo.isComparable(exp.getResultType()))
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not an expression compatible with java.lang.Comparable: "+exp.getResultType());
	}

	static boolean requiresFloatingPointOp(
			Expression<?> left, Expression<?> right) {
		return left.getResultType()==TypeInfo.FLOATING_POINT
				|| right.getResultType()==TypeInfo.FLOATING_POINT;
	}

	static QueryException forUnsupportedFloatingPoint(String op) {
		return new QueryException(QueryErrorCode.TYPE_MISMATCH,
				"Operation does not support floating point types: "+op);
	}

	static QueryException forUnsupportedCast(TypeInfo source, TypeInfo target) {
		return new QueryException(QueryErrorCode.TYPE_MISMATCH,
				String.format("Cannot return %s as %s", source, target));
	}

	public static boolean string2Boolean(CharSequence value) {
		return value!=null && value.length()>0;
	}

	public static boolean int2Boolean(long value) {
		return value!=0;
	}

	public static boolean float2Boolean(double value) {
		return Double.compare(value, 0.0)!=0;
	}

	public static boolean object2Boolean(Object value) {
		return value!=null;
	}

	@SuppressWarnings("unchecked")
	private static <T> T[] ensureSpecificType0(Expression<?>[] expressions,
			@SuppressWarnings("rawtypes") Consumer<? super Expression> check, IntFunction<T[]> arrayGen) {
		T[] result = arrayGen.apply(expressions.length);
		for (int i = 0; i < expressions.length; i++) {
			Expression<?> expression = expressions[i];
			check.accept(expression);
			result[i] = (T) expression;
		}
		return result;
	}

	/** Clones the given array of expressions into integer {@link NumericalExpression}[] */
	public static NumericalExpression[] ensureInteger(Expression<?>...expressions) {
		return ensureSpecificType0(expressions, EvaluationUtils::checkIntegerType,
				NumericalExpression[]::new);
	}

	/** Clones the given array of expressions into floating point {@link NumericalExpression}[] */
	public static NumericalExpression[] ensureFloatingPoint(Expression<?>...expressions) {
		return ensureSpecificType0(expressions, EvaluationUtils::checkFloatingPointType,
				NumericalExpression[]::new);
	}

	/** Clones the given array of expressions into {@link NumericalExpression}[] */
	public static NumericalExpression[] ensureNumeric(Expression<?>...expressions) {
		return ensureSpecificType0(expressions, EvaluationUtils::checkNumericalType,
				NumericalExpression[]::new);
	}

	/** Clones the given array of expressions into {@link BooleanExpression}[] */
	public static BooleanExpression[] ensureBoolean(Expression<?>...expressions) {
		return ensureSpecificType0(expressions, EvaluationUtils::checkBooleanType,
				BooleanExpression[]::new);
	}

	/** Clones the given array of expressions into {@link TextExpression}[] */
	public static TextExpression[] ensureText(Expression<?>...expressions) {
		return ensureSpecificType0(expressions, EvaluationUtils::checkTextType,
				TextExpression[]::new);
	}
}
