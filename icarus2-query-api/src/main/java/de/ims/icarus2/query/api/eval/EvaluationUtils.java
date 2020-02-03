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

import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;

/**
 * @author Markus Gärtner
 *
 */
public class EvaluationUtils {

	static void checkNumericalType(Expression<?> exp) {
		if(!exp.isNumerical())
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not a proper numerical type: "+exp.getResultType());
	}

	static void checkComparableType(Expression<?> exp) {
		if(!TypeInfo.isComparable(exp.getResultType()))
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not a type compatible with java.lang.Comparable: "+exp.getResultType());
	}

	static boolean requiresFloatingPointOp(
			Expression<?> left, Expression<?> right) {
		return left.getResultType()==TypeInfo.DOUBLE
				|| right.getResultType()==TypeInfo.DOUBLE;
	}

	static QueryException forUnsupportedFloatingPoint(String op) {
		return new QueryException(QueryErrorCode.TYPE_MISMATCH,
				"Operation does not support floating point types: "+op);
	}

	static QueryException forUnsupportedCast(TypeInfo source, TypeInfo target) {
		return new QueryException(QueryErrorCode.TYPE_MISMATCH,
				String.format("Cannot return %s as %s", source, target));
	}

	public static boolean string2Boolean(String value) {
		return value!=null && !"".equals(value);
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
}
