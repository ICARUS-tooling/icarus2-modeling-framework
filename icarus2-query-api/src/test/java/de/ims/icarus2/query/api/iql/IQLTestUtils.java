/**
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

import static de.ims.icarus2.test.util.Triple.triple;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Stack;
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
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.util.strings.BracketStyle;
import de.ims.icarus2.util.tree.Tree;
import de.ims.icarus2.util.tree.TreeParser;

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

	public static IQL_TestParser createParser(String text, String description,
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
		parser.setTrace(false);

		return parser;
	}

	public static <C extends ParserRuleContext> void assertParsedTree(String text, String expected,
			String description, Function<IQL_TestParser, C> rule, boolean expectEOF) {
		C ctx;

		try {
			ctx= assertValidParse0(text, description, rule);
		} catch (RecognitionException e) {
			fail(description+": "+text, e);
			return;
		}

		ParseTree root = ctx;
		if(expectEOF) {
			// If we expect EOF, it has to be the right child of the root node
			assertThat(ctx.getChildCount())
				.as("test rule is expected to have 1 main child plus EOF marker")
				.isEqualTo(2);
			assertThat(ctx.getChild(1).getText())
				.as("expected second node of root to be EOF marker")
				.isEqualTo("<EOF>");
			root = ctx.getChild(0);
		}

		Tree<String> expectedTree = TreeParser.forStringPayload(BracketStyle.SQUARE).parseTree(expected);

		matchParseNodePlain(expectedTree, root, new Stack<>());
	}

	private static void matchParseNodePlain(Tree<String> expected, ParseTree actual,
			Stack<String> trace) {
		String text = expected.getData();

		/*
		 *  If we have a designated text, we know that this is either a terminal or
		 *  that the structure below this node doesn't matter
		 */
		if(text!=null) {
			assertThat(actual.getText())
				.as("parsed text mismatch: %s", trace)
				.isEqualTo(text);
			return;
		}

		// Continue structural check
		assertThat(actual.getChildCount())
			.as("rule subtree mismatch for '%s': '%s' vs. '%s'", actual.getText(), trace, expected)
			.isEqualTo(expected.childCount());

		// If needed go down the tree
		for (int i = 0; i < expected.childCount(); i++) {
			Tree<String> child = expected.childAt(i);
			String payload = child.getData();
			trace.push(payload==null ? child.toString() : payload);
			matchParseNodePlain(child, actual.getChild(i), trace);
			trace.pop();
		}
	}

	public static <C extends ParserRuleContext> void assertParsedTree(String text, Tree<NodeExpectation> expected,
			String description, Function<IQL_TestParser, C> rule, boolean expectEOF) {
		C ctx = assertValidParse0(text, description, rule);

		ParseTree root = ctx;
		if(expectEOF) {
			// If we expect EOF, it has to be the right child of the root node
			assertThat(ctx.getChildCount())
				.as("test rule is expected to have 1 main child plus EOF marker")
				.isEqualTo(2);
			assertThat(ctx.getChild(1).getText())
				.as("expected second node of root to be EOF marker")
				.isEqualTo("<EOF>");
			root = ctx.getChild(0);
		}

		matchParseRule(expected, root, new Stack<>());
	}

	private static void matchParseRule(Tree<NodeExpectation> expected, ParseTree actual,
			Stack<NodeExpectation> trace) {
		assertThat(actual)
			.as("parse rule mismatch: %s", trace)
			.isInstanceOf(expected.getData().nodeClass);
		assertThat(actual.getChildCount())
			.as("rule subtree mismatch: %s", trace)
			.isEqualTo(expected.childCount());

		for (int i = 0; i < expected.childCount(); i++) {
			Tree<NodeExpectation> child = expected.childAt(i);
			trace.push(child.getData());
			matchParseRule(child, actual.getChild(i), trace);
			trace.pop();
		}
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
	public static <C extends ParserRuleContext> void assertValidParse(
			String text, String expected, String description,
			Function<IQL_TestParser, C> rule) {
		if(expected==null || expected.isEmpty()) {
			expected = text.trim();
		}
		C ctx = assertValidParse0(text, description, rule);
		assertThat(parseToString(ctx))
			.as("Premature end of rule for %s in '%s'", description, text)
			.isEqualTo(expected);
	}

	private static <C extends ParserRuleContext> C assertValidParse0(
			String text, String description, Function<IQL_TestParser, C> rule) {
		IQL_TestParser parser = createParser(text, description, null, false);
		C ctx = rule.apply(parser);
		assertThat(ctx)
			.as("Unable to parse '%s'", text)
			.isNotNull();
		return ctx;
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

//		assert reporter.offendingToken!=null : "no offending token reported!";
	}

	public static <C extends ParserRuleContext> void assertInvalidParse(
			String text, String description, String offendingToken,
			Function<IQL_TestParser, C> rule) {
		SyntaxErrorReporter reporter = new SyntaxErrorReporter();
		IQL_TestParser parser = createParser(text, description, reporter, true);

		Throwable throwable = catchThrowable(() -> parseAll(text, parser, reporter, rule));
		assertThat(throwable)
			.as("Expected parse error for input %s", text)
			.isNotNull()
			.isInstanceOfAny(RecognitionException.class, ParseCancellationException.class);

		if(throwable instanceof ParseCancellationException) {
			throwable = throwable.getCause();
		}

		if(throwable instanceof RecognitionException) {
			if(reporter.offendingToken==null) {
				reporter.offendingToken = ((RecognitionException)throwable).getOffendingToken().getText();
			}
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
