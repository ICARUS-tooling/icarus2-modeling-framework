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

import static org.assertj.core.api.Assertions.assertThat;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import de.ims.icarus2.query.api.iql.antlr.IQLLexer;


/**
 * @author Markus Gärtner
 *
 */
class IQLLexerTest {

	private static void assertToken(String text, String description, int type) {
		IQLLexer lexer = new IQLLexer(CharStreams.fromString(text, description));
		lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);

		Token token = lexer.nextToken();
		assertThat(token.getText())
			.isEqualTo(text.trim());
		assertThat(lexer.getType())
			.as("Input '%s' of type '%s' not recognized as %s",
					text, description, lexer.getVocabulary().getSymbolicName(type))
			.isEqualTo(type);
	}

	@ParameterizedTest(name="{1}: {0}")
	@CsvFileSource(resources={"lexerTests_stringLiteral.csv"}, numLinesToSkip=1)
	void testStringListeral(String text, String description) {
		// To keep our csv file readable, we artificially add the surrounding quotation marks here
		text = "\""+text+"\"";

		assertToken(text, description, IQLLexer.StringLiteral);
	}

//	@ParameterizedTest(name="{1}: {0}")
//	@CsvFileSource(resources={"lexerTests_integerLiteral.csv"}, numLinesToSkip=1)
//	void testIntegerListerals(String text, String description) {
//		assertToken(text, description, IQLLexer.IntegerLiteral);
//	}

//	@ParameterizedTest(name="{1}: {0}")
//	@CsvFileSource(resources={"lexerTests_unsignedIntegerLiteral.csv"}, numLinesToSkip=1)
//	void testUnsignedIntegerListerals(String text, String description) {
//		assertToken(text, description, IQLLexer.UnsignedIntegerLiteral);
//	}
//
//	@ParameterizedTest(name="{1}: {0}")
//	@CsvFileSource(resources={"lexerTests_floatingPointLiteral.csv"}, numLinesToSkip=1)
//	void testFloatingPointListerals(String text, String description) {
//		assertToken(text, description, IQLLexer.FloatingPointLiteral);
//	}
//
//	@ParameterizedTest(name="{1}: {0}")
//	@CsvFileSource(resources={"lexerTests_unsignedFloatingPointLiteral.csv"}, numLinesToSkip=1)
//	void testUnsignedFloatingPointListerals(String text, String description) {
//		assertToken(text, description, IQLLexer.UnsignedFloatingPointLiteral);
//	}

	@ParameterizedTest(name="{1}: {0}")
	@CsvFileSource(resources={"lexerTests_identifier.csv"}, numLinesToSkip=1)
	void testIdentifer(String text, String description) {
		assertToken(text, description, IQLLexer.Identifier);
	}
}
