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
package de.ims.icarus2.query.api.iql;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;

import de.ims.icarus2.query.api.iql.antlr.IQL_TestParser;

/**
 * @author Markus Gärtner
 *
 */
class IQLParserTest {



	// ACTUAL TESTS

	@ParameterizedTest(name="{1}: {0}")
	@CsvFileSource(resources={"parserTests_unsignedFloatingPointLiteral.csv"}, numLinesToSkip=1)
	void testUnsignedFloatingPointLiteral(String text, String description) {
		IQLTestUtils.assertValidParse(text, null, description, IQL_TestParser::unsignedFloatingPointLiteralTest);
	}

	@ParameterizedTest(name="{1}: {0}")
	@CsvFileSource(resources={"parserTests_unsignedIntegerLiteral.csv"}, numLinesToSkip=1)
	void testUnsignedIntegerLiteral(String text, String description) {
		IQLTestUtils.assertValidParse(text, null, description, IQL_TestParser::unsignedIntegerLiteralTest);
	}

	@ParameterizedTest(name="{1}: {0}")
	@CsvFileSource(resources={"parserTests_floatingPointLiteral.csv"}, numLinesToSkip=1)
	void testFloatingPointLiteral(String text, String description) {
		IQLTestUtils.assertValidParse(text, null, description, IQL_TestParser::floatingPointLiteralTest);
	}

	@ParameterizedTest(name="{1}: {0}")
	@CsvFileSource(resources={"parserTests_integerLiteral.csv"}, numLinesToSkip=1)
	void testIntegerLiteral(String text, String description) {
		IQLTestUtils.assertValidParse(text, null, description, IQL_TestParser::integerLiteralTest);
	}

	@ParameterizedTest(name="{1}: {0}")
	@CsvFileSource(resources={"parserTests_unsignedQuantifier.csv"}, numLinesToSkip=1)
	void testUnsignedQuantifer(String text, String description) {
		IQLTestUtils.assertValidParse(text, null, description, IQL_TestParser::unsignedSimpleQuantifierTest);
	}

	@ParameterizedTest(name="{2}: {0} [offending: {1}]")
	@CsvFileSource(resources={"parserTests_unsignedQuantifier_invalid.csv"}, numLinesToSkip=1)
	void testInvalidUnsignedQuantifer(String text, String offendingToken, String description) {
		IQLTestUtils.assertInvalidParse(text, description, offendingToken, IQL_TestParser::unsignedSimpleQuantifierTest);
	}

	@ParameterizedTest(name="{1}: {0}")
	@CsvFileSource(resources={"parserTests_quantifier.csv"}, numLinesToSkip=1)
	void testQuantifer(String text, String description) {
		IQLTestUtils.assertValidParse(text, null, description, IQL_TestParser::quantifierTest);
	}

	public static Stream<Arguments> createExpressions() {
		return Stream.of(
				arguments("\"random text stuff...\nnot really ending soonish\"",
						"\"randomtextstuff...notreallyendingsoonish\"", "string literal with linebreak")
		);
	}

	@ParameterizedTest(name="{2}: {0}")
	@CsvFileSource(resources={"parserTests_expression.csv"}, numLinesToSkip=1)
	@MethodSource("createExpressions")
	@Deprecated // IQLExpressionTest now contains exhaustive tests for expressions without the need of CSV files
	void testExpression(String text, String expected, String description) {
		IQLTestUtils.assertValidParse(text, expected, description, IQL_TestParser::standaloneExpression);
	}

	@ParameterizedTest(name="{2}: {0} [offending: {1}]")
	@CsvFileSource(resources={"parserTests_expression_invalid.csv"}, numLinesToSkip=1)
	@Deprecated // IQLExpressionTest now contains exhaustive tests for expressions without the need of CSV files
	void testInvalidExpression(String text, String offendingToken, String description) {
		IQLTestUtils.assertInvalidParse(text, description, offendingToken, IQL_TestParser::standaloneExpression);
	}

	@ParameterizedTest(name="{1}: {0}")
	@CsvFileSource(resources={"parserTests_versionDeclaration.csv"}, numLinesToSkip=1)
	void testVersionDeclaration(String text, String description) {
		IQLTestUtils.assertValidParse(text, null, description, IQL_TestParser::versionDeclarationTest);
	}
}
