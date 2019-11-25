/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.iql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.function.Function;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

/**
 * @author Markus Gärtner
 *
 */
class IQLParserTest {

	/** Helper class that always throws an exception on syntax errors */
	static class SyntaxErrorReporter extends BaseErrorListener {
		String offendingToken;

		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
				int charPositionInLine, String msg, RecognitionException e) {
			if(this.offendingToken!=null)
				throw new IllegalStateException("Offending symbol already assigned: "+this.offendingToken);

			if(e!=null) {
				Token token = e.getOffendingToken();
				this.offendingToken = token==null ? "<???>" : token.getText();
				throw e;
			}

			// Do not throw exception if parser managed to recover
		}
	}

	private static IQLParser parser(String text, String description, SyntaxErrorReporter reporter) {
		IQLLexer lexer = new IQLLexer(CharStreams.fromString(text, description));
		lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);

		IQLParser parser = new IQLParser(new BufferedTokenStream(lexer));
		parser.removeErrorListener(ConsoleErrorListener.INSTANCE);

		if(reporter==null) {
			reporter = new SyntaxErrorReporter();
		}

		parser.addErrorListener(reporter);
//		parser.setErrorHandler(new BailErrorStrategy());

		return parser;
	}

	/**
	 * Verifies that specified parse rule matches the given input and consumes it all.
	 * Note that the parser skips whitespases, so do NOT test the freedom of defining
	 * multiline queries and such with this method!!
	 */
	private static <C extends RuleContext> void assertValidParse(String text, String description,
			Function<IQLParser, C> rule) {
		IQLParser parser = parser(text, description, null);
		C ctx = rule.apply(parser);
		assertThat(ctx)
			.as("Unable to parse '%s'", text)
			.isNotNull();
		assertThat(ctx.getText())
			.as("Premature end of rule for %s in '%s'", description, text)
			.isEqualTo(text.trim());
	}

	private static <C extends ParserRuleContext> void parseAll(String text, IQLParser parser,
			SyntaxErrorReporter reporter, Function<IQLParser, C> rule) {
		C ctx = rule.apply(parser); // this should/might fail

		String matchedText = ctx.getText();
		if(!matchedText.equals(text.trim())) {
			reporter.offendingToken = text.substring(matchedText.length());
			throw new RecognitionException("Some input was ignored", parser, parser.getInputStream(), ctx);
		}
	}

	private static <C extends ParserRuleContext> void assertInvalidParse(
			String text, String description, String offendingToken,
			Function<IQLParser, C> rule) {
		SyntaxErrorReporter reporter = new SyntaxErrorReporter();
		IQLParser parser = parser(text, description, reporter);

		Throwable throwable = catchThrowable(() -> parseAll(text, parser, reporter, rule));
		assertThat(throwable)
			.as("Expected parse error for input %s", text)
			.isNotNull()
			.isInstanceOfAny(RecognitionException.class, ParseCancellationException.class);

		if(throwable instanceof RecognitionException) {
			assertThat(reporter.offendingToken)
				.as("Unexpected offending symbol")
				.startsWith(offendingToken);
		}
	}

	// ACTUAL TESTS

	@ParameterizedTest(name="{1}: {0}")
	@CsvFileSource(resources={"parserTests_unsignedQuantifier.csv"}, numLinesToSkip=1)
	void testUnsignedQuantifer(String text, String description) {
		assertValidParse(text, description, IQLParser::unsignedQuantifier);
	}

	@ParameterizedTest(name="{2}: {0} [offending: {1}]")
	@CsvFileSource(resources={"parserTests_unsignedQuantifier_invalid.csv"}, numLinesToSkip=1)
	void testInvalidUnsignedQuantifer(String text, String offendingToken, String description) {
		assertInvalidParse(text, description, offendingToken, IQLParser::unsignedQuantifier);
	}

	@ParameterizedTest(name="{1}: {0}")
	@CsvFileSource(resources={"parserTests_quantifier.csv"}, numLinesToSkip=1)
	void testQuantifer(String text, String description) {
		assertValidParse(text, description, IQLParser::quantifier);
	}

	@ParameterizedTest(name="{1}: {0}")
	@CsvFileSource(resources={"parserTests_path.csv"}, numLinesToSkip=1)
	void testPath(String text, String description) {
		assertValidParse(text, description, IQLParser::path);
	}

//	@PostponedTest //TODO
	@ParameterizedTest(name="{2}: {0} [offending: {1}]")
	@CsvFileSource(resources={"parserTests_path_invalid.csv"}, numLinesToSkip=1)
	void testInvalidPath(String text, String offendingToken, String description) {
		assertInvalidParse(text, description, offendingToken, IQLParser::path);
	}

	@ParameterizedTest(name="{1}: {0}")
	@CsvFileSource(resources={"parserTests_call.csv"}, numLinesToSkip=1)
	void testCall(String text, String description) {
		assertValidParse(text, description, IQLParser::call);
	}

	@ParameterizedTest(name="{1}: {0}")
	@CsvFileSource(resources={"parserTests_versionDeclaration.csv"}, numLinesToSkip=1)
	void testVersionDeclaration(String text, String description) {
		assertValidParse(text, description, IQLParser::versionDeclaration);
	}
}
