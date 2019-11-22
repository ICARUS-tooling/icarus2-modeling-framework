/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.function.Function;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import de.ims.icarus2.test.annotations.PostponedTest;

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

		return parser;
	}

	private static <C extends RuleContext> void assertValidParse(String text, String description,
			Function<IQLParser, C> rule) {
		IQLParser parser = parser(text, description, null);
		assertThat(rule.apply(parser))
			.as("Unable to parse '%s'", text)
			.isNotNull();
	}

	private static <C extends RuleContext> void assertInvalidParse(
			String text, String description, String offendingToken,
			Function<IQLParser, C> rule) {
		SyntaxErrorReporter reporter = new SyntaxErrorReporter();
		IQLParser parser = parser(text, description, reporter);
		assertThatExceptionOfType(RecognitionException.class)
			.as("Expected parse error for input %s", text)
			.isThrownBy(() -> rule.apply(parser));
		assertThat(reporter.offendingToken)
			.as("Unexpected offending symbol")
			.isEqualTo(offendingToken);
	}

	// ACTUAL TESTS

	@ParameterizedTest(name="{1}: {0}")
	@CsvFileSource(resources={"parserTests_specificQuantifier.csv"}, numLinesToSkip=1)
	void testSpecificQuantifer(String text, String description) {
		assertValidParse(text, description, IQLParser::specificQuantifier);
	}

	@ParameterizedTest(name="{2}: {0} [offending: {1}]")
	@CsvFileSource(resources={"parserTests_invalidSpecificQuantifier.csv"}, numLinesToSkip=1)
	void testInvalidSpecificQuantifer(String text, String offendingToken, String description) {
		assertInvalidParse(text, description, offendingToken, IQLParser::specificQuantifier);
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

	@PostponedTest //TODO
	@ParameterizedTest(name="{2}: {0} [offending: {1}]")
	@CsvFileSource(resources={"parserTests_invalidPath.csv"}, numLinesToSkip=1)
	void testInvalidPath(String text, String offendingToken, String description) {
		assertInvalidParse(text, description, offendingToken, IQLParser::path);
	}
}
