/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static org.assertj.core.api.Assertions.assertThat;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;


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
	void testStringListerals(String text, String description) {
		// To keep our csv file readable, we artificially add the surrounding quotation marks here
		text = "\""+text+"\"";

		assertToken(text, description, IQLLexer.StringLiteral);
	}

	@ParameterizedTest(name="{1}: {0}")
	@CsvFileSource(resources={"lexerTests_integerLiteral.csv"}, numLinesToSkip=1)
	void testIntegerListerals(String text, String description) {
		assertToken(text, description, IQLLexer.IntegerLiteral);
	}

	@ParameterizedTest(name="{1}: {0}")
	@CsvFileSource(resources={"lexerTests_unsignedIntegerLiteral.csv"}, numLinesToSkip=1)
	void testUnsignedIntegerListerals(String text, String description) {
		assertToken(text, description, IQLLexer.UnsignedIntegerLiteral);
	}

	@ParameterizedTest(name="{1}: {0}")
	@CsvFileSource(resources={"lexerTests_floatingPointLiteral.csv"}, numLinesToSkip=1)
	void testFloatingPointListerals(String text, String description) {
		assertToken(text, description, IQLLexer.FloatingPointLiteral);
	}

	@ParameterizedTest(name="{1}: {0}")
	@CsvFileSource(resources={"lexerTests_unsignedFloatingPointLiteral.csv"}, numLinesToSkip=1)
	void testUnsignedFloatingPointListerals(String text, String description) {
		assertToken(text, description, IQLLexer.UnsignedFloatingPointLiteral);
	}

	@ParameterizedTest(name="{1}: {0}")
	@CsvFileSource(resources={"lexerTests_identifier.csv"}, numLinesToSkip=1)
	void testIdentifer(String text, String description) {
		assertToken(text, description, IQLLexer.Identifier);
	}
}
