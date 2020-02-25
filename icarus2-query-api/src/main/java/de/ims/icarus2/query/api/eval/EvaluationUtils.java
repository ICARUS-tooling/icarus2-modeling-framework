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

import java.lang.reflect.Array;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.types.ValueType.MatrixType;
import de.ims.icarus2.model.manifest.types.ValueType.VectorType;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.eval.BinaryOperations.StringMode;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class EvaluationUtils {

	private static final Map<ValueType, TypeInfo> typeMap = new Reference2ReferenceOpenHashMap<>();

	private static void registerType(ValueType valueType, TypeInfo type) {
		TypeInfo present = typeMap.putIfAbsent(valueType, type);
		if(present != null)
			throw new InternalError(String.format("Duplicate registration attempt for % - previously %s, now %s",
					valueType, present, type));
	}

	static {
		registerType(ValueType.STRING, TypeInfo.TEXT);
		registerType(ValueType.BOOLEAN, TypeInfo.BOOLEAN);
		registerType(ValueType.DOUBLE, TypeInfo.FLOATING_POINT);
		registerType(ValueType.FLOAT, TypeInfo.FLOATING_POINT);
		registerType(ValueType.INTEGER, TypeInfo.INTEGER);
		registerType(ValueType.LONG, TypeInfo.INTEGER);
		registerType(ValueType.UNKNOWN, TypeInfo.GENERIC);
	}

	public static TypeInfo typeFor(ValueType valueType) {
		if(VectorType.class.isInstance(valueType))
			throw new IcarusRuntimeException(QueryErrorCode.UNSUPPORTED_FEATURE,
					"The query engine does not support vector types: "+valueType);
		if(MatrixType.class.isInstance(valueType))
			throw new IcarusRuntimeException(QueryErrorCode.UNSUPPORTED_FEATURE,
					"The query engine does not support matrix types: "+valueType);

		TypeInfo type = typeMap.get(valueType);
		if(type==null) {
			Class<?> clazz = valueType.isPrimitiveType() ? valueType.getPrimitiveClass() : valueType.getBaseClass();
			type = TypeInfo.of(clazz);
		}
		return type;
	}

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

	/**
	 * Checks that the given {@code text} holds a {@link ManifestUtils#isValidId(String) valid identifier}
	 * to be used in the specified {@code context} and throws a {@link QueryException} of type
	 * {@link QueryErrorCode#INVALID_LITERAL} if the check fails.
	 *
	 * @param text
	 * @param context
	 *
	 * @see #forEmptyStringLiteral(String)
	 * @see ManifestUtils#isValidId(String)
	 */
	static void checkIdentifier(String text, String context) {
		requireNonNull(text);
		if(text.trim().isEmpty())
			throw forEmptyStringLiteral(context);

		if(!ManifestUtils.isValidId(text))
			throw new QueryException(QueryErrorCode.INVALID_LITERAL,
					String.format("Not a valid identifier in context '%s': %s", context, text));
	}

	/**
	 * Returns {@code true} iff either of the two expressions is a
	 * {@link Expression#isFloatingPoint() floating point} expression.
	 */
	static boolean requiresFloatingPointOp(
			Expression<?> left, Expression<?> right) {
		return left.isFloatingPoint() || right.isFloatingPoint();
	}

	public static QueryException forUnsupportedFloatingPoint(String op) {
		return new QueryException(QueryErrorCode.TYPE_MISMATCH,
				"Operation does not support floating point types: "+op);
	}

	public static QueryException forUnsupportedCast(TypeInfo source, TypeInfo target) {
		return new QueryException(QueryErrorCode.TYPE_MISMATCH,
				String.format("Cannot return %s as %s", source, target));
	}

	public static QueryException forEmptyStringLiteral(String context) {
		return new QueryException(QueryErrorCode.INVALID_LITERAL,
				String.format("Emty string not allowed in context: %s", context));
	}

	public static QueryException forUnknownIdentifier(String name, String target) {
		return new QueryException(QueryErrorCode.UNKNOWN_IDENTIFIER,
				String.format("Failed to resolve name to a valid %s: %s", target, name));
	}

	public static QueryException forIncorrectUse(String msg, Object...args) {
		return new QueryException(QueryErrorCode.INCORRECT_USE, String.format(msg, args));
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

	/** Clones the given array of expressions into integer {@link Expression<?>}[] */
	public static Expression<?>[] ensureInteger(Expression<?>...expressions) {
		Stream.of(expressions).forEach(EvaluationUtils::checkIntegerType);
		return expressions;
	}

	/** Clones the given array of expressions into floating point {@link Expression<?>}[] */
	public static Expression<?>[] ensureFloatingPoint(Expression<?>...expressions) {
		Stream.of(expressions).forEach(EvaluationUtils::checkFloatingPointType);
		return expressions;
	}

	/** Clones the given array of expressions into {@link Expression<?>}[] */
	public static Expression<?>[] ensureNumeric(Expression<?>...expressions) {
		Stream.of(expressions).forEach(EvaluationUtils::checkNumericalType);
		return expressions;
	}

	/** Clones the given array of expressions into {@link BooleanExpression}[] */
	public static Expression<?>[] ensureBoolean(Expression<?>...expressions) {
		Stream.of(expressions).forEach(EvaluationUtils::checkBooleanType);
		return expressions;
	}

	/** Clones the given array of expressions into {@link TextExpression}[] */
	public static Expression<?>[] ensureText(Expression<?>...expressions) {
		Stream.of(expressions).forEach(EvaluationUtils::checkTextType);
		return expressions;
	}

	@SuppressWarnings("unchecked")
	public static <E> E[] arrayOf(TypeInfo type, int size) {
		return (E[]) Array.newInstance(type.getType().getComponentType(), size);
	}

	public static TypeInfo arrayType(TypeInfo elementType) {
		Object dummy = Array.newInstance(elementType.getType(), 0);
		return TypeInfo.of(dummy.getClass(), true);
	}

	public static Pattern pattern(String regex, StringMode mode, boolean allowUnicode) {
		int flags = 0;
		if(mode==StringMode.IGNORE_CASE) {
			flags |= Pattern.CASE_INSENSITIVE;
		}

		if(allowUnicode && CodePointUtils.containsSupplementaryCodePoints(regex)) {
			flags |= Pattern.UNICODE_CASE;
		}

		return Pattern.compile(regex, flags);
	}
}
