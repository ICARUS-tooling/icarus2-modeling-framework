/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static de.ims.icarus2.test.util.Triple.triple;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.function.Function;
import java.util.stream.Stream;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

/**
 * @author Markus Gärtner
 *
 */
public class IQLTestUtils {

	/*
	 * gradlew.bat icarus2-query-api:clean icarus2-query-api:generateGrammarSource icarus2-query-api:generateTestGrammarSource
	 */

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

			this.offendingToken = ((Token)offendingSymbol).getText();

			throw new RecognitionException(msg, recognizer,
					recognizer.getInputStream(), ((Parser)recognizer).getContext());

//			System.out.printf("%s pos=[%d,%d] offending=%s%n", msg, line, charPositionInLine, offendingSymbol);
			// Do not throw exception if parser managed to recover
		}

//		/**
//		 * @see org.antlr.v4.runtime.BaseErrorListener#reportAmbiguity(org.antlr.v4.runtime.Parser, org.antlr.v4.runtime.dfa.DFA, int, int, boolean, java.util.BitSet, org.antlr.v4.runtime.atn.ATNConfigSet)
//		 */
//		@Override
//		public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact,
//				BitSet ambigAlts, ATNConfigSet configs) {
//			fail();
//		}
	}

	static IQL_TestParser createParser(String text, String description,
			SyntaxErrorReporter reporter, boolean strict) {
		IQLLexer lexer = new IQLLexer(CharStreams.fromString(text, description));
		lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);

		IQL_TestParser parser = new IQL_TestParser(new BufferedTokenStream(lexer));
		parser.removeErrorListener(ConsoleErrorListener.INSTANCE);

		if(reporter==null) {
			reporter = new SyntaxErrorReporter();
		}

		if(strict) {
			parser.setErrorHandler(new BailErrorStrategy());
		}

		parser.addErrorListener(reporter);
		parser.getInterpreter().setPredictionMode(PredictionMode.LL);
		parser.setProfile(true);
		parser.setTrace(true);

		return parser;
	}

	static String parseToString(ParserRuleContext ctx) {
		String s = ctx.getText();
		if(ctx.getStop().getType()==Recognizer.EOF) {
			s = s.substring(0, s.length()-5);
		}
		return s;
	}

	/**
	 * Verifies that specified parse rule matches the given input and consumes it all.
	 * Note that the parser skips whitespaces, so do NOT test the freedom of defining
	 * multiline queries and such with this method!!
	 */
	static <C extends ParserRuleContext> void assertValidParse(
			String text, String expected, String description,
			Function<IQL_TestParser, C> rule) {
		if(expected==null || expected.isEmpty()) {
			expected = text.trim();
		}

		IQL_TestParser parser = createParser(text, description, null, false);
		C ctx = rule.apply(parser);
		assertThat(ctx)
			.as("Unable to parse '%s'", text)
			.isNotNull();
		assertThat(parseToString(ctx))
			.as("Premature end of rule for %s in '%s'", description, text)
			.isEqualTo(expected);
	}

	static <C extends ParserRuleContext, P extends Parser> void parseAll(
			String text, P parser,
			SyntaxErrorReporter reporter, Function<P, C> rule) {
		C ctx = rule.apply(parser); // this should/might fail

		String matchedText = parseToString(ctx);

		//TODO remove '<EOF>' from matchedText
		if(!matchedText.equals(text.trim())) {
			reporter.offendingToken = text.substring(matchedText.length());
			throw new RecognitionException(
					"Some input was ignored", parser, parser.getInputStream(), ctx);
		}
	}

	static <C extends ParserRuleContext> void assertInvalidParse(
			String text, String description, String offendingToken,
			Function<IQL_TestParser, C> rule) {
		SyntaxErrorReporter reporter = new SyntaxErrorReporter();
		IQL_TestParser parser = createParser(text, description, reporter, true);

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

	//TODO upgrade handling to code points
	static String simplify(String s) {
		StringBuilder sb = new StringBuilder(s.length());

		boolean insideStringLiteral = false;
		boolean escaped = false;

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if(escaped) {
				escaped = false;
			} else if(c=='\\') {
				escaped = true;
			} else if(c=='"') {
				insideStringLiteral = !insideStringLiteral;
			}

			if(insideStringLiteral || !Character.isWhitespace(c)) {
				sb.append(c);
			}
		}

		return sb.toString();
	}

	@TestFactory
	Stream<DynamicNode> testSimplify() {
		return Stream.of(
				triple("", "", "empty"),
				triple("test", "test", "no ws"),
				triple("  test", "test", "leading ws"),
				triple("test  ", "test", "trailing ws"),
				triple("test1 test2", "test1test2", "intermediate ws"),
				triple("test1 \"test2\"", "test1\"test2\"", "ws before quote"),
				triple("\"test1 test2\"", "\"test1 test2\"", "ws inside quote"),
				triple("\"test1 test2\" test3", "\"test1 test2\"test3", "ws after and inside quote")
		).map(tData -> dynamicTest(tData.third, () -> {
			String input = tData.first;
			String expected = tData.second;
			String actual = simplify(input);
			assertThat(actual)
				.isNotNull()
				.isEqualTo(expected);
		}));
	}
}
