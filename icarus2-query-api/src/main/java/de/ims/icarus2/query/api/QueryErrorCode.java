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
package de.ims.icarus2.query.api;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.ims.icarus2.ErrorCode;
import de.ims.icarus2.ErrorCodeScope;
import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.Report;
import de.ims.icarus2.query.api.exp.EvaluationContext;
import de.ims.icarus2.query.api.iql.IqlQueryElement;

/**
 * @author Markus Gärtner
 *
 */
public enum QueryErrorCode implements ErrorCode {

	//**************************************************
	//       1xx  GENERAL ERRORS
	//**************************************************

	/**
	 * Signals that an instance of {@link IqlQueryElement} failed its
	 * {@link IqlQueryElement#checkIntegrity() integrity check}.
	 */
	CORRUPTED_QUERY(101),

	/**
	 * Wraps a {@link JsonProcessingException} that occurred
	 * during reading or writing of a query.
	 */
	JSON_ERROR(102),

	/**
	 * Dummy code to signal a more complex {@link Report} containing detailed
	 * and aggregated error descriptions.
	 */
	REPORT(103),

	//**************************************************
	//       2xx  INNER IQL SYNTAX ERRORS
	//**************************************************

	/**
	 * An inner expression of an IQL query contains syntax errors.
	 * The associated exception will be wrapped as cause for inspection.
	 */
	SYNTAX_ERROR(200),

	/**
	 * Processing the abstract syntax tree (AST) of a parsed query or
	 * query subpart yielded a construction that cannot be handled.
	 * This error is reserved for indicating discrepancies between the
	 * ANTLR grammar for IQL and the actual processing code and hints
	 * at internal bugs on either side.
	 */
	AST_ERROR(201),

	/**
	 * Signals that a token is expected to be in a continuous form,
	 * but was written in chunks. The default IQL grammar for example
	 * defines rules for various edge expressions that are constructed
	 * from '<','>' and '-' symbols to make it possible for the lexer to
	 * not overly complicate the construction of tokens and let the parser
	 * do the semantic checks based on context.
	 */
	NON_CONTINUOUS_TOKEN(203),

	/**
	 * More like a warning: Some part of the query is not actually needed
	 * for an unambiguous evaluation.
	 * For instance, a wrappable part (e.g. an expression) has been
	 * surrounded by multiple (redundant) wrappings of parentheses.
	 */
	SUPERFLUOUS_DECLARATION(204),

	/**
	 * A literal (number, string, boolean) has been detected to hold a value
	 * that is invalid for the given context. This covers all the situations
	 * where value validation can be performed during the first process pass.
	 */
	INVALID_LITERAL(205),

	/**
	 * An identifier (member name, variable, marker, etc...) could not be resolved
	 * within the scope it was used.
	 */
	UNKNOWN_IDENTIFIER(210),

	/**
	 * Used when an unknown type definition '@type' in the JSON query
	 * has been encountered.
	 */
	UNKNOWN_TYPE(211),

	/**
	 * An optional feature of IQL was used in the query that is currently not
	 * supported. This error code is also used for situations where the engine
	 * detects a problem too complex to handle.
	 */
	UNSUPPORTED_FEATURE(220),

	/**
	 * Signals that an IQL feature has been used incorrectly. This is sometimes
	 * shown as a mere warning, depending on the actual severity.
	 */
	INCORRECT_USE(221),


	//**************************************************
	//       3xx  MATCHER ERRORS
	//**************************************************

	/**
	 * An expression encountered an unsupported value type.
	 */
	TYPE_MISMATCH(301),

	/**
	 * An identifier or other reference was resolved to a target that is
	 * incompatible within the current context.
	 */
	INCOMPATIBLE_REFERENCE(302),

	/**
	 * An expression that only exists as proxy has been survived till
	 * the actual evaluation phase and one of its unsupported methods
	 * has been called.
	 */
	PROXY_CALL(303),

	/**
	 * Constructing a matcher failed due to the respective (and obligatory)
	 * element store missing from the associated {@link EvaluationContext}.
	 */
	MISSING_ELEMENT_STORE(304),

	/**
	 * Converting a {@link Query} instance into a matcher state machine failed due
	 * to some fragments not being supported or being used incorrectly.
	 */
	UNSUPPORTED_QUERY_FRAGMENT(305),
	;

	private static volatile ErrorCodeScope SCOPE;

	public static ErrorCodeScope getScope() {
		ErrorCodeScope scope = SCOPE;
		if(scope==null) {
			synchronized (QueryErrorCode.class) {
				if((scope = SCOPE) == null) {
					scope = SCOPE = ErrorCodeScope.newScope(4000, QueryErrorCode.class.getSimpleName());
				}
			}
		}
		return scope;
	}

	private final int code;

	private QueryErrorCode(int code) {
		this.code = code;

		ErrorCode.register(this);
	}

	/**
	 * @see de.ims.icarus2.ErrorCode#code()
	 */
	@Override
	public int code() {
		return code+getScope().getCode();
	}

	/**
	 * @see de.ims.icarus2.ErrorCode#scope()
	 */
	@Override
	public ErrorCodeScope scope() {
		return getScope();
	}

	/**
	 * Resolves the given error code to the matching enum constant.
	 * {@code Code} can be given both as an internal id or global code.
	 *
	 * @param code
	 * @return
	 */
	public static QueryErrorCode forCode(int code) {
		getScope().checkCode(code);

		ErrorCode error = ErrorCode.forCode(code);

		if(error==null)
			throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT, "Unknown error code: "+code);
		if(!QueryErrorCode.class.isInstance(error))
			throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE, "Corrupted mapping for error code: "+code);

		return QueryErrorCode.class.cast(error);
	}
}
