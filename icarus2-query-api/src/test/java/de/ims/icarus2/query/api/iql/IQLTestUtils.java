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

import static de.ims.icarus2.test.util.Pair.pair;
import static de.ims.icarus2.test.util.Triple.triple;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static de.ims.icarus2.util.collections.CollectionUtils.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.List;
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

import de.ims.icarus2.query.api.iql.antlr.IQLLexer;
import de.ims.icarus2.query.api.iql.antlr.IQL_TestParser;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.util.strings.BracketStyle;
import de.ims.icarus2.util.strings.StringUtil;
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
				throw new IllegalStateException(String.format(
						"Offending symbol already assigned: '%s' - unable to assign '%s'''",
						this.offendingToken, ((Token)offendingSymbol).getText()));

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
//		lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);

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
		parser.setProfile(false);
		parser.setTrace(false);

		return parser;
	}

	public static <C extends ParserRuleContext> void assertParsedTree(String text, String expected,
			String description, Function<IQL_TestParser, C> rule, boolean expectEOF) {
		assertParsedTree(text, expected, BracketStyle.SQUARE, description, rule, expectEOF);
	}

	public static <C extends ParserRuleContext> void assertParsedTree(
			String text, String expected, BracketStyle style,
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

		Tree<String> expectedTree = TreeParser.forStringPayload(style).parseTree(expected);

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

	// DUMMY DATA AND HELPERS

	/**
	 * Returns the entire binary operatir hierarchy from highest to lowest in respective groups.
	 * <pre>
	 *  IQL binary operators, in order from highest to lowest precedence:
	 *  *    /    %
	 *  +    -
	 *  <<   >>   &    |    ^
	 *  <    <=   >    >=
	 *  ~   !~   #   !#
	 *  ==   !=
	 *  &&   AND
	 *  ||   OR
	 * </pre>
	 */
	static final List<List<Pair<String, String>>> binaryOpsHierarchy = list(
			list(pair("*", "multiplication"), pair("/", "division"), pair("%", "modulo")),
			list(pair("+", "addition"), pair("-", "subtraction")),
			list(pair("<<", "shift left"), pair(">>", "shift right"), pair("&", "bitwise and"), pair("|", "bitwise or"), pair("^", "bitwise xor")),
			list(pair("<", "less than"), pair("<=", "less than or equal"), pair(">", "greater than"), pair(">=", "greater than or equal")),
			list(pair("~", "matches"), pair("!~", "matches not"), pair("#", "contains"), pair("!#", "contains not")),
			list(pair("==", "equals"), pair("!=", "not equal")),
			list(pair("&&", "and"), pair("and", "and (keyword)")),
			list(pair("||", "or"), pair("or", "or (keyword)"))
	);

	static final List<Pair<String, String>> binaryOps
		= toList(binaryOpsHierarchy.stream().flatMap(l -> l.stream()));

	static final List<Pair<String, String>> comparisons = list(
			pair("==", "equals"),
			pair("<", "less than"),
			pair("<=", "less than or equal"),
			pair(">", "greater than"),
			pair(">=", "greater than or equal"),
			pair("!=", "not equal"),
			pair("~", "matches"),
			pair("!~", "matches not"),
			pair("#", "contains"),
			pair("!#", "contains not")
	);

	static final List<Pair<String, String>> literals = list(
			pair("123", "unsigned int"),
			pair("-123", "signed negative int"),
			pair("+123", "signed positive int"),
			pair("123.456", "unsigned float"),
			pair("+123.456", "signed positive float"),
			pair("-123.456", "signed negative float"),
			pair("\"\"", "empty string"),
			pair("\"test\"", "string"),
			pair("\"test with stuff\"", "string with whitespaces"),
			pair("true", "boolean true"),
			pair("TRUE", "boolean TRUE"),
			pair("false", "boolean false"),
			pair("FALSE", "boolean FALSE")
	);

	static final List<Pair<String, String>> strings = list(
			pair("\"\"", "empty string"),
			pair("\"test\"", "string"),
			pair("\"test with stuff\"", "string with whitespaces")
	);

	static final List<Pair<String, String>> types = list(
			pair("boolean", "Boolean"),
			pair("int", "Integer"),
			pair("long", "Long integer"),
			pair("float", "Float"),
			pair("double", "Double"),
			pair("string", "String")
	);

	static final List<Pair<String, String>> indices = list(
			pair("123", "unsigned int"),
			pair("-123", "signed negative int"),
			pair("+123", "signed positive int")
	);

	static final List<Pair<String, String>> references = list(
			pair("ref", "named ref"),
			pair("path.to.ref", "path"),
			pair("@var", "named var"),
			pair("@var.to.path", "path of var"),
			pair("$xyz", "named member"),
			pair("$xyz.to.path", "path from member"),
			pair("$xyz(123)", "named member with index"),
			pair("$xyz(123).to.path", "path from member with index")
	);

	static final List<Pair<String, String>> callableReferences = list(
			pair("ref", "named ref"),
			pair("path.to.ref", "path"),
			pair("@var.to.path", "path from var"),
			pair("$xyz.to.path", "path from member")
	);

	static final List<Pair<String, String>> multiArgs = list(
			pair("123,456,789", "multi ints"),
			pair("@var,\"test\",4.5", "var, string, float"),
			pair("-123,765.89", "negative int, float"),
			pair("123,-765.89", "int, negative float")
	);

	static final String dummy = "123";

	static final Pair<String, String> noArgs = pair("","<none>");

	/** Literals, references, array lookups, calls and annotations. Literally all available 'things' */
	static final List<Pair<String, String>> elements = toList(Stream.of(
			literals.stream(),
			references.stream(),
			callableReferences.stream().map(pRef -> func(pRef, noArgs)),
			callableReferences.stream().flatMap(pRef -> literals.stream().map(pLit -> func(pRef, pLit))),
			callableReferences.stream().flatMap(pRef -> multiArgs.stream().map(pArgs -> func(pRef, pArgs))),
			references.stream().flatMap(pRef -> indices.stream().map(pIdx -> array(pRef, pIdx))),
			Stream.of(array(references.get(0), args(indices))),
			references.stream().flatMap(pRef -> strings.stream().map(pAnno -> annotation(pRef, pAnno))),
			Stream.of(annotation(references.get(0), args(strings)))
		).reduce(Stream::concat).get());

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

	/** Combine a bunch of values to produce argument lists */
	static Pair<String, String> args(List<Pair<String, String>> elements) {
		StringBuilder sbContent = new StringBuilder();
		StringBuilder sbDesc = new StringBuilder();
		for (int i = 0; i < elements.size(); i++) {
			if(i>0) {
				sbContent.append(",");
				sbDesc.append(" and ");
			}
			sbContent.append(elements.get(i).first);
			sbDesc.append(elements.get(i).second);
		}

		return pair(sbContent.toString(), sbDesc.toString());
	}

	/** Make function call from a ref and arguments list */
	static Pair<String, String> func(Pair<String, String> ref, Pair<String, String> args) {
		return pair(ref.first+"("+args.first+")", "call on '"+ref.second+"' with '"+args.second+"' args");
	}

	/** Make array access from ref and singular index */
	static Pair<String, String> array(Pair<String, String> ref, Pair<String, String> index) {
		return pair(ref.first+"["+index.first+"]", "index of '"+ref.second+"' with "+index.second);
	}

	/** Make annotation access from ref and key */
	static Pair<String, String> annotation(Pair<String, String> ref, Pair<String, String>  key) {
		return pair(ref.first+"{"+key.first+"}", "annotation of '"+ref.second+"' with "+key.second);
	}

	static Pair<String, String> padOp(Pair<String, String> op) {
		if(isKeywordOp(op)) {
			op = pair(" "+op.first+" ", op.second);
		}
		return op;
	}

	static Stream<Pair<String, String>> variateBinary(Pair<String, String> op,
			Pair<String, String> arg) {
		op = padOp(op);
		return Stream.of(
				pair(arg.first+op.first+dummy, "left "+op.second+" of "+arg.second),
				pair(dummy+op.first+arg.first, "right "+op.second+" of "+arg.second),
				pair(arg.first+op.first+arg.first, "dual "+op.second+" of "+arg.second)
		);
	}

	static String f(String format, Object...args) {
			return StringUtil.format(format, args);
	//		return String.format(format, args);
		}

	static String treeEscape(String s) {
		if(s.indexOf('[')!=-1) {
			StringBuilder sb = new StringBuilder(s.length()*2);
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if(c=='[' || c==']') {
					sb.append('\\');
				}
				sb.append(c);
			}
			s = sb.toString();
		}
		return s;
	}

	static String f1(String format, Pair<?, ?>...ops) {
		return f(format, Stream.of(ops).map(p -> p.first).toArray());
	}

	static String f1Tree(String format, Pair<?, ?>...ops) {
		return f(format, Stream.of(ops).map(p -> treeEscape(p.first.toString())).toArray());
	}

	static String f2(String format, Pair<?, ?>...ops) {
		return f(format, Stream.of(ops).map(p -> p.second).toArray());
	}

	static boolean isKeywordOp(Pair<String, String> op) {
		return Character.isLetter(op.first.charAt(0));
	}

	/** Generates a sequence of randomly constructed binary expressions */
	static List<Pair<String, String>> randomExpressions(RandomGenerator rng, int count, boolean allowKeywordOp) {
		List<Pair<String, String>> result = new ArrayList<>(count);
		int remaining = count;
		while (remaining>0) {
			Pair<String, String> pOp = rng.random(binaryOps);
			if(!allowKeywordOp && isKeywordOp(pOp)) {
				continue;
			}

			Pair<String, String> pFirst = rng.random(elements);
			Pair<String, String> pSecond = rng.random(elements);
			pOp = padOp(pOp);

			result.add(pair(pFirst.first+pOp.first+pSecond.first,
					f2("op '{1}' of '{2}' and '{3}'", pOp, pFirst, pSecond)));
			remaining--;
		}
		return result;
	}
}
