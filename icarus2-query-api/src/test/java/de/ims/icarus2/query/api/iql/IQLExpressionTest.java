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

import static de.ims.icarus2.query.api.iql.IQLTestUtils.assertInvalidParse;
import static de.ims.icarus2.query.api.iql.IQLTestUtils.assertParsedTree;
import static de.ims.icarus2.query.api.iql.IQLTestUtils.createParser;
import static de.ims.icarus2.query.api.iql.IQLTestUtils.simplify;
import static de.ims.icarus2.test.TestTags.SLOW;
import static de.ims.icarus2.test.util.Pair.pair;
import static de.ims.icarus2.util.IcarusUtils.notEq;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static de.ims.icarus2.util.collections.CollectionUtils.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.ims.icarus2.query.api.iql.antlr.IQL_TestParser;
import de.ims.icarus2.query.api.iql.antlr.IQL_TestParser.StandaloneExpressionContext;
import de.ims.icarus2.test.annotations.DisabledOnCi;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.util.strings.StringUtil;

/**
 * @author Markus Gärtner
 *
 */
public class IQLExpressionTest {

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
	private static final List<List<Pair<String, String>>> binaryOpsHierarchy = list(
			list(pair("*", "multiplication"), pair("/", "division"), pair("%", "modulo")),
			list(pair("+", "addition"), pair("-", "subtraction")),
			list(pair("<<", "shift left"), pair(">>", "shift right"), pair("&", "bitwise and"), pair("|", "bitwise or"), pair("^", "bitwise xor")),
			list(pair("<", "less than"), pair("<=", "less than or equal"), pair(">", "greater than"), pair(">=", "greater than or equal")),
			list(pair("~", "matches"), pair("!~", "matches not"), pair("#", "contains"), pair("!#", "contains not")),
			list(pair("==", "equals"), pair("!=", "not equal")),
			list(pair("&&", "and"), pair("and", "and (keyword)")),
			list(pair("||", "or"), pair("or", "or (keyword)"))
	);

	private static final List<Pair<String, String>> binaryOps
		= toList(binaryOpsHierarchy.stream().flatMap(l -> l.stream()));

	private static final List<Pair<String, String>> comparisons = list(
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

	private static final List<Pair<String, String>> literals = list(
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

	private static final List<Pair<String, String>> strings = list(
			pair("\"\"", "empty string"),
			pair("\"test\"", "string"),
			pair("\"test with stuff\"", "string with whitespaces")
	);

	private static final List<Pair<String, String>> types = list(
			pair("boolean", "Boolean"),
			pair("int", "Integer"),
			pair("long", "Long integer"),
			pair("float", "Float"),
			pair("double", "Double"),
			pair("string", "String")
	);

	private static final List<Pair<String, String>> indices = list(
			pair("123", "unsigned int"),
			pair("-123", "signed negative int"),
			pair("+123", "signed positive int")
	);

	private static final List<Pair<String, String>> references = list(
			pair("ref", "named ref"),
			pair("path.to.ref", "path"),
			pair("@var", "named var"),
			pair("@var.to.path", "path of var"),
			pair("$xyz", "named member"),
			pair("$xyz.to.path", "path from member"),
			pair("$xyz(123)", "named member with index"),
			pair("$xyz(123).to.path", "path from member with index")
	);

	private static final List<Pair<String, String>> callableReferences = list(
			pair("ref", "named ref"),
			pair("path.to.ref", "path"),
			pair("@var.to.path", "path of var")
	);

	private static final List<Pair<String, String>> multiArgs = list(
			pair("123,456,789", "multi ints"),
			pair("@var,\"test\",4.5", "var, string, float"),
			pair("-123,765.89", "negative int, float"),
			pair("123,-765.89", "int, negative float")
	);

	private static final String dummy = "123";
	private static final Pair<String, String> noArgs = pair("","<none>");

	/** Literals, references, array lookups, calls and annotations. Literally all available 'things' */
	private static final List<Pair<String, String>> elements = toList(Stream.of(
			literals.stream(),
			references.stream(),
			callableReferences.stream().map(pRef -> func(pRef, noArgs)),
			callableReferences.stream().flatMap(pRef -> literals.stream().map(pLit -> func(pRef, pLit))),
			callableReferences.stream().flatMap(pRef -> multiArgs.stream().map(pArgs -> func(pRef, pArgs))),
			references.stream().flatMap(pRef -> indices.stream().map(pIdx -> array(pRef, pIdx))),
			references.stream().flatMap(pRef -> strings.stream().map(pAnno -> annotation(pRef, pAnno)))
		).reduce(Stream::concat).get());

	/** Combine a bunch of values to produce argument lists */
	private static Pair<String, String> arguments(Pair<String, String> ...elements) {
		StringBuilder sbContent = new StringBuilder();
		StringBuilder sbDesc = new StringBuilder();
		for (int i = 0; i < elements.length; i++) {
			if(i>0) {
				sbContent.append(", ");
				sbDesc.append(" and ");
			}
			sbContent.append(elements[i].first);
			sbDesc.append(elements[i].second);
		}

		return pair(sbContent.toString(), sbDesc.toString());
	}

	/** Make function call from a ref and arguments list */
	private static Pair<String, String> func(Pair<String, String> ref, Pair<String, String> args) {
		return pair(ref.first+"("+args.first+")", "call on '"+ref.second+"' with '"+args.second+"' args");
	}

	/** Make array access from ref and singular index */
	private static Pair<String, String> array(Pair<String, String> ref, Pair<String, String> index) {
		return pair(ref.first+"["+index.first+"]", "index of '"+ref.second+"' with "+index.second);
	}

	/** Make annotation access from ref and key */
	private static Pair<String, String> annotation(Pair<String, String> ref, Pair<String, String>  key) {
		return pair(ref.first+"{"+key.first+"}", "annotation of '"+ref.second+"' with "+key.second);
	}

	private static Pair<String, String> padOp(Pair<String, String> op) {
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

	@TestFactory
	@DisplayName("binary operations: <left><op><right>")
	Stream<DynamicNode> testBinaryOps() {
		return binaryOps.stream()
				.map(pOp -> dynamicContainer(pOp.second, elements.stream()
					.map(pArg -> dynamicContainer(pArg.second, variateBinary(pOp, pArg)
						.map(pTest -> dynamicTest(pTest.second+": "+pTest.first, () -> {
							String text = pTest.first;
							String description = pTest.second;
							IQL_TestParser parser = createParser(text, description, null, false);
							StandaloneExpressionContext ctx = parser.standaloneExpression();
							assertThat(ctx.getChildCount())
								.as("%s: expecting <expression><EOF> construct",text)
								.isEqualTo(2);

							// Now check the actual parse tree
							ParseTree expression = ctx.getChild(0);
							// Check that we encounter a binary expression
							assertThat(expression.getChildCount())
								.as("%s: expecting <left><op><right> construct",text)
								.isEqualTo(3);
							// Check that the operator is centered
							assertThat(expression.getChild(1).getText())
								.as("%s: failed to parse operator",text)
								.isEqualTo(pOp.first);
							// Check first  operand
							assertThat(expression.getChild(0).getText())
								.as("%s: failed to parse first operand",text)
								.isIn(dummy,simplify(pArg.first));
							// Check second operand
							assertThat(expression.getChild(2).getText())
								.as("%s: failed to parse second operand",text)
								.isIn(dummy,simplify(pArg.first));
						}))))));
	}

	@TestFactory
	@DisplayName("binary comparisons: <left><comp><right>")
	Stream<DynamicNode> testBinaryComparisons() {
		return comparisons.stream()
				.map(pComp -> dynamicContainer(pComp.second, elements.stream()
					.map(pArg -> dynamicContainer(pArg.second, variateBinary(pComp, pArg)
						.map(pTest -> dynamicTest(pTest.second+": "+pTest.first, () -> {
							String text = pTest.first;
							String description = pTest.second;
							IQL_TestParser parser = createParser(text, description, null, false);
							StandaloneExpressionContext ctx = parser.standaloneExpression();
							assertThat(ctx.getChildCount())
								.as("%s: expecting <expression><EOF> construct",text)
								.isEqualTo(2);

							// Now check the actual parse tree
							ParseTree expression = ctx.getChild(0);
							// Check that we encounter a binary comparison
							assertThat(expression.getChildCount())
								.as("%s: expecting <left><comp><right> construct",text)
								.isEqualTo(3);
							// Check that the operator is centered
							assertThat(expression.getChild(1).getText())
								.as("%s: failed to parse comparator",text)
								.isEqualTo(pComp.first);
							// Check first  operand
							assertThat(expression.getChild(0).getText())
								.as("%s: failed to parse first operand",text)
								.isIn(dummy,simplify(pArg.first));
							// Check second operand
							assertThat(expression.getChild(2).getText())
								.as("%s: failed to parse second operand",text)
								.isIn(dummy,simplify(pArg.first));
						}))))));
	}

	private static String f(String format, Object...args) {
		return StringUtil.format(format, args);
//		return String.format(format, args);
	}

	private static String treeEscape(String s) {
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

	private static String f1(String format, Pair<?, ?>...ops) {
		return f(format, Stream.of(ops).map(p -> p.first).toArray());
	}

	private static String f1Tree(String format, Pair<?, ?>...ops) {
		return f(format, Stream.of(ops).map(p -> treeEscape(p.first.toString())).toArray());
	}

	private static String f2(String format, Pair<?, ?>...ops) {
		return f(format, Stream.of(ops).map(p -> p.second).toArray());
	}

	private static boolean isKeywordOp(Pair<String, String> op) {
		return Character.isLetter(op.first.charAt(0));
	}

	private static DynamicTest makeExpressionTreeTest(String expression, String expected, String desc) {
		return dynamicTest(desc+": "+expression+"  ->  "+expected, () ->
			assertParsedTree(expression, expected, desc, IQL_TestParser::standaloneExpression, true));
	}

	/**
	 * Create expression {@code <a> nested <b> op <c>}, assuming {@code op} has higher priority
	 * compared to {@code nested} and verify that the parse tree is {@code [<a> nested [<b> op <c>]]}.
	 */
	private static DynamicNode makePrivilegedNestedOpTests(Pair<String, String> op, Pair<String, String> nested) {
		List<DynamicTest> tests = new ArrayList<>();

		// Only skip whitespace if we have no keyword-based operators
		if(!isKeywordOp(op) && !isKeywordOp(nested)) {
			// No brackets and no whitespace
			tests.add(makeExpressionTreeTest(f1("123{1}456{2}789", nested, op),
					f1Tree("[[123][{1}][[456][{2}][789]]]", nested, op),
						f2("'{1}' in '{2}' without spaces", nested, op)));

			// Brackets to promote the nested op
			tests.add(makeExpressionTreeTest(f1("(123{1}456){2}789", nested, op),
					f1Tree("[[(123{1}456)][{2}][789]]", nested, op),
						f2("'{1}' in '{2}' with brackets and no spaces", nested, op)));
		}

		// Whitespace variant is always expected to work!
		tests.add(makeExpressionTreeTest(f1("123 {1} 456 {2} 789", nested, op),
				f1Tree("[[123][{1}][[456][{2}][789]]]", nested, op),
					f2("'{1}' in '{2}' with spaces", nested, op)));

		// Brackets to promote the nested op
		tests.add(makeExpressionTreeTest(f1("(123 {1} 456) {2} 789", nested, op),
				f1Tree("[[(123{1}456)][{2}][789]]", nested, op),
					f2("'{1}' in '{2}' with brackets and spaces", nested, op)));

		return dynamicContainer(f2("{1} in {2}", nested, op), tests);
	}

	/**
	 * Create expression {@code <a> nested <b> op <c>}, assuming {@code op} has equal priority
	 * compared to {@code nested} and verify that the parse tree is {@code [[<a> nested <b>] op <c>]]}.
	 */
	private static DynamicNode makeEqualNestedOpTests(Pair<String, String> op, Pair<String, String> nested) {
		List<DynamicTest> tests = new ArrayList<>();

		// Only skip whitespace if we have no keyword-based operators
		if(!isKeywordOp(op) && !isKeywordOp(nested)) {
			tests.add(makeExpressionTreeTest(f1("123{1}456{2}789", nested, op),
					f1Tree("[[[123][{1}][456]][{2}][789]]", nested, op),
						f2("'{1}' in '{2}' without spaces", nested, op)));
		}

		// Whitespace variant is always expected to work!
		tests.add(makeExpressionTreeTest(f1("123 {1} 456 {2} 789", nested, op),
				f1Tree("[[[123][{1}][456]][{2}][789]]", nested, op),
					f2("'{1}' in '{2}' with spaces", nested, op)));

		return dynamicContainer(f2("'{1}' in '{2}'", nested, op), tests);
	}

	@TestFactory
	@DisplayName("direct nesting of two binary operators according to their priorities")
	List<DynamicNode> testDualOpNesting() {
		List<List<Pair<String, String>>> hierarchy = binaryOpsHierarchy;
		int levels = hierarchy.size();

		List<DynamicNode> tests = new ArrayList<>();

		for (int level = 0; level < levels; level++) {
			List<Pair<String, String>> ops = hierarchy.get(level);
			int opCount = ops.size();

			for (int opIndex = 0; opIndex < opCount; opIndex++) {
				Pair<String, String> op = ops.get(opIndex);

				tests.add(dynamicContainer(op.second+" <equal level>",
						ops.stream().filter(notEq(op)).map(eqOp -> makeEqualNestedOpTests(op, eqOp))));

				// If we have a privileged op, verify against all ops of lower precedence
				if(level<levels-1) {
					tests.add(dynamicContainer(op.second+" <lower levels>",
							IntStream.range(level+1, levels) // all lower levels
							.mapToObj(l -> hierarchy.get(l)) // fetch group for level
							.flatMap(group -> group.stream()) // expand all groups
							.map(lesserOp -> makePrivilegedNestedOpTests(op, lesserOp))));
				}
			}
		}

		return tests;
	}

	/**
	 * Create expression {@code <a> op1 <b> op2 <c> op3 <d>}, with operator priority
	 * {@code op1 > op2 > op3}, additionally tests various other ways of nesting them
	 * with and without bracketing.
	 */
	private static DynamicNode makePrivilegedTripleNestedOpsTests(String header,
			Pair<String, String> op1, Pair<String, String> op2, Pair<String, String> op3) {
		List<DynamicTest> tests = new ArrayList<>();

		/*
		 * scenarios:
		 * [c[b[a]]]
		 * [[a]c[b]]
		 * [[b]c[a]]
		 * [[[a]b]c]
		 *
		 * bracketed:
		 * [a([b([c])])]
		 * [[a]b([c])]
		 */

		// Only skip whitespace if we have no keyword-based operators
		if(!isKeywordOp(op1) && !isKeywordOp(op2) && !isKeywordOp(op3)) {
			// General

			// [c[b[a]]]
			tests.add(makeExpressionTreeTest(f1("12{3}34{2}56{1}78", op1, op2, op3),
					f1Tree("[[12][{3}][[34][{2}][[56][{1}][78]]]]", op1, op2, op3),
						f2("'{3}' in '{2}' in '{1}' without spaces", op1, op2, op3)));
			// [[a]c[b]]
			tests.add(makeExpressionTreeTest(f1("12{1}34{3}56{2}78", op1, op2, op3),
					f1Tree("[[[12][{1}][34]][{3}][[56][{2}][78]]]", op1, op2, op3),
						f2("'{1}' and '{2}' in '{3}' without spaces", op1, op2, op3)));
			// [[b]c[a]]
			tests.add(makeExpressionTreeTest(f1("12{2}34{3}56{1}78", op1, op2, op3),
					f1Tree("[[[12][{2}][34]][{3}][[56][{1}][78]]]", op1, op2, op3),
						f2("'{2}' and '{1}' in '{3}' without spaces", op1, op2, op3)));
			// [[[a]b]c]
			tests.add(makeExpressionTreeTest(f1("12{1}34{2}56{3}78", op1, op2, op3),
					f1Tree("[[[[12][{1}][34]][{2}][56]][{3}][78]]", op1, op2, op3),
						f2("'{1}' in '{2}' in '{3}' without spaces", op1, op2, op3)));

			// Bracketed

			// [a([b([c])])]
			tests.add(makeExpressionTreeTest(f1("12{1}(34{2}(56{3}78))", op1, op2, op3),
					f1Tree("[[12][{1}][[(][[34][{2}][(56{3}78)]][)]]]", op1, op2, op3),
						f2("'{1}' ('{2}' ('{3}')) without spaces", op1, op2, op3)));

			// [[a]b([c])]
			tests.add(makeExpressionTreeTest(f1("12{1}34{2}(56{3}78)", op1, op2, op3),
					f1Tree("[[[12][{1}][34]][{2}][(56{3}78)]]", op1, op2, op3),
						f2("'{1}' in '{2}' ('{3}') without spaces", op1, op2, op3)));

			//TODO make more bracketed test cases!!!
		}

		// Whitespace variant is always expected to work!

		// [c[b[a]]]
		tests.add(makeExpressionTreeTest(f1("12 {3} 34 {2} 56 {1} 78", op1, op2, op3),
				f1Tree("[[12][{3}][[34][{2}][[56][{1}][78]]]]", op1, op2, op3),
					f2("'{3}' in '{2}' in '{1}' with spaces", op1, op2, op3)));
		// [[a]c[b]]
		tests.add(makeExpressionTreeTest(f1("12 {1} 34 {3} 56 {2} 78", op1, op2, op3),
				f1Tree("[[[12][{1}][34]][{3}][[56][{2}][78]]]", op1, op2, op3),
					f2("'{1}' and '{2}' in '{3}' with spaces", op1, op2, op3)));
		// [[b]c[a]]
		tests.add(makeExpressionTreeTest(f1("12 {2} 34 {3} 56 {1} 78", op1, op2, op3),
				f1Tree("[[[12][{2}][34]][{3}][[56][{1}][78]]]", op1, op2, op3),
					f2("'{2}' and '{1}' in '{3}' with spaces", op1, op2, op3)));
		// [[[a]b]c]
		tests.add(makeExpressionTreeTest(f1("12 {1} 34 {2} 56 {3} 78", op1, op2, op3),
				f1Tree("[[[[12][{1}][34]][{2}][56]][{3}][78]]", op1, op2, op3),
					f2("'{1}' in '{2}' in '{3}' with spaces", op1, op2, op3)));

		// Bracketed

		// [a([b([c])])]
		tests.add(makeExpressionTreeTest(f1("12 {1} (34 {2} (56 {3} 78))", op1, op2, op3),
				f1Tree("[[12][{1}][[(][[34][{2}][(56{3}78)]][)]]]", op1, op2, op3),
					f2("'{1}' ('{2}' ('{3}')) with spaces", op1, op2, op3)));

		// [[a]b([c])]
		tests.add(makeExpressionTreeTest(f1("12 {1} 34 {2} (56 {3} 78)", op1, op2, op3),
				f1Tree("[[[12][{1}][34]][{2}][(56{3}78)]]", op1, op2, op3),
					f2("'{1}' in '{2}' ('{3}') with spaces", op1, op2, op3)));

		return dynamicContainer(header, tests);
	}

	@SuppressWarnings("boxing")
	@TestFactory
	@RandomizedTest
	@DisplayName("direct nesting of three binary operators according to their priorities")
	List<DynamicNode> testTripleOpNesting(RandomGenerator rng) {
		List<List<Pair<String, String>>> hierarchy = binaryOpsHierarchy;
		int levels = hierarchy.size();

		List<DynamicNode> tests = new ArrayList<>();

		for (int level1 = 0; level1 < levels-2; level1++) {
			int level2 = rng.random(level1+1, levels-1);
			int level3 = rng.random(level2+1, levels);

			Pair<String, String> op1 = rng.random(hierarchy.get(level1));
			Pair<String, String> op2 = rng.random(hierarchy.get(level2));
			Pair<String, String> op3 = rng.random(hierarchy.get(level3));

			/*
			 * scenarios:
			 * [c[b[a]]]
			 * [[a]c[b]]
			 * [[b]c[a]]
			 * [[[a]b]c]
			 *
			 * bracketed:
			 * [a([b([c])])]
			 */
			tests.add(makePrivilegedTripleNestedOpsTests(String.format("%d-%d-%d: '%s'",
					level1+1, level2+1, level3+1, op1.second), op1, op2, op3));
		}

		return tests;
	}

	private static void assertTernaryOp(Pair<String, String> check,
			Pair<String, String> opt1, Pair<String, String> opt2) {

		try {
			// No WS
			assertParsedTree(
					f1("{1}?{2}:{3}", check, opt1, opt2),
					f1Tree("[[{1}][?][{2}][:][{3}]]", check, opt1, opt2),
					f2("'{1}' ? '{2}' : '{3}'", check, opt1, opt2),
					IQL_TestParser::standaloneExpression, true);

			// With WS
			assertParsedTree(
					f1("{1} ? {2} : {3}", check, opt1, opt2),
					f1Tree("[[{1}][?][{2}][:][{3}]]", check, opt1, opt2),
					f2("'{1}' ? '{2}' : '{3}' with whitespace", check, opt1, opt2),
					IQL_TestParser::standaloneExpression, true);
		} catch(RecognitionException e) {
			fail(f2("'{1}' ? '{2}' : '{3}'", check, opt1, opt2), e);
		}
	}

	/**
	 * Exhaustively test the ternary operation pattern with all variations from the
	 * {@link #elements()} source for every slot in the pattern.
	 * <p>
	 * Since this will result in slightly under 1 million cases, we do not want to
	 * create separate test instances for all of them.
	 */
	@SuppressWarnings("boxing")
	@Test
	@DisplayName("flat ternary op 'x ? y : z'")
	@Tag(SLOW)
	@DisabledOnCi
	void testTernaryOp() {

		List<Pair<String, String>> variations = elements;
		int size = variations.size();

		for (int i = 0; i < size; i++) {
			Pair<String, String> pCheck = variations.get(i);
			for (int j = 0; j < size; j++) {
				Pair<String, String> pOpt1 = variations.get(j);
				for (int k = 0; k < size; k++) {
					Pair<String, String> pOpt2 = variations.get(k);
					assertTernaryOp(pCheck, pOpt1, pOpt2);
				}
			}
			System.out.printf("%d/%d root variations done: %s%n",i+1,size,pCheck.second);
		}
	}

	@TestFactory
	@DisplayName("cast primary expressions directly to specific types")
	Stream<DynamicNode> testDirectCast() {
		return types.stream().map(pType -> dynamicContainer(pType.second,
				elements.stream().map(pVal -> dynamicContainer(pVal.second, Stream.of(
						// No WS
						makeExpressionTreeTest(f1("({1}){2}", pType, pVal),
								f1Tree("[[(][{1}][)][{2}]]", pType, pVal),
								f2("cast '{2}' to {1}", pType, pVal)),
						// With WS
						makeExpressionTreeTest(f1("({1}) {2}", pType, pVal),
								f1Tree("[[(][{1}][)][{2}]]", pType, pVal),
								f2("cast '{2}' to {1} with whitespace", pType, pVal))
				)))));
	}

	@TestFactory
	@RandomizedTest
	@DisplayName("cast expressions wrapped in brackets")
	Stream<DynamicNode> testWrappedCast(RandomGenerator rng) {
		return types.stream().map(pType -> dynamicContainer(pType.second,
				rng.ints(0, elements.size()).distinct().limit(10)
				.mapToObj(i -> {
					Pair<String, String> arg1 = rng.random(elements);
					Pair<String, String> op = rng.random(binaryOps);
					Pair<String, String> arg2 = rng.random(elements);

					Pair<String, String> exp = pair(
							f1("{1}{2}{3}", arg1, padOp(op), arg2),
							f2("[{1} {2} {3}]", arg1, padOp(op), arg2));

					return makeExpressionTreeTest(f1("({1})({2})", pType, exp),
							f1Tree("[[(][{1}][)][[(][{2}{3}{4}][)]]", pType, arg1, op, arg2),
							f2("cast '{2}' to {1} with brackets", pType, exp));
				})));
	}

	/** Produce various paths variants with different depths */
	public static Stream<Arguments> pathArguments() {
		return Stream.of(
				// Simple paths
				Arguments.of("direct path (1 level)",
						"root.element1", "[[root][.][element1]]"),
				Arguments.of("direct path (2 levels)",
						"root.element1.element2", "[[[root][.][element1]][.][element2]]"),

				// Paths with method invocations
				Arguments.of("method invocation path (1 call)",
						"root.method1()", "[[[root][.][method1]][(][)]]"),
				Arguments.of("method invocation path (multiple calls)",
						"root.method1().method2()", "[[[[[root][.][method1]][(][)]][.][method2]][(][)]]"),
				Arguments.of("method invocation path (single call with arguments)",
						"root.method1(123, 456)", "[[[root][.][method1]][(][123,456][)]]"),
				Arguments.of("method invocation path (multiple calls with arguments)",
						"root.method1(123, 456).method2(-78.90)",
						"[[[[[root][.][method1]][(][123,456][)]][.][method2]][(][-78.90][)]]"),

				// Paths with array access
				Arguments.of("array access path (1 dimension)",
						"root.array1[index1]", "[[[root][.][array1]][\\[][index1][\\]]]"),
				Arguments.of("array access path (2 dimensions)",
						"root.array1[index1][index2]", "[[[[root][.][array1]][\\[][index1][\\]]][\\[][index2][\\]]]"),

				// Paths with annotation access
				Arguments.of("annotation access path",
						"root.anno1{key1}", "[[[root][.][anno1]][{][key1][}]]")
		);
	}

	@ParameterizedTest
	@MethodSource("pathArguments")
	void testPaths(String desc, String expression, String expected) {
		assertParsedTree(expression, expected, desc,
				IQL_TestParser::standaloneExpression, true);
	}

	/** Produce variations of method, array and annotation access expressions */
	public static Stream<Arguments> mixedAccessArguments() {
		return Stream.of(
				// Method begin
				Arguments.of("method+array",
						"func1()[index1]", "[[func1()][\\[][index1][\\]]]"),
				Arguments.of("method+method",
						"func1().func2()", "[[[func1()][.][func2]][(][)]]"),
				Arguments.of("method+args+array",
						"func1(arg1, arg2)[index1]", "[[func1(arg1,arg2)][\\[][index1][\\]]]"),
				Arguments.of("method+annotation",
						"func1(){key1}", "[[func1()][{][key1][}]]"),
				Arguments.of("method+args+annotation",
						"func1(arg1, arg2){key1}", "[[func1(arg1,arg2)][{][key1][}]]"),
				Arguments.of("method+annotation+array",
						"func1(){key1}[index1]", "[[[func1()][{][key1][}]][\\[][index1][\\]]]"),

				// Array begin
				Arguments.of("array+array",
						"array1[index1][index2]", "[[array1\\[index1\\]][\\[][index2][\\]]]"),
				Arguments.of("array+annotation",
						"array1[index1]{key1}", "[[array1\\[index1\\]][{][key1][}]]"),
				Arguments.of("array+method",
						"array1[index1].func1()", "[[[array1\\[index1\\]][.][func1]][(][)]]"),

				// Annotation begin
				Arguments.of("annotation+array",
						"anno1{key1}[index1]", "[[anno1{key1}][\\[][index1][\\]]]"),
				Arguments.of("annotation+method",
						"anno1{key1}.func1()", "[[[anno1{key1}][.][func1]][(][)]]")
		);
	}

	@ParameterizedTest
	@MethodSource("mixedAccessArguments")
	void testMixedAccessExpressions(String desc, String expression, String expected) {
		assertParsedTree(expression, expected, desc,
				IQL_TestParser::standaloneExpression, true);
	}

	/** Produce certain illegal expressions */
	public static Stream<Arguments> invalidExpressions() {
		return Stream.of(
				Arguments.of("double func", "func1()()", "("),
				Arguments.of("func on array", "array1[123]()", "("),
				Arguments.of("func on annotation", "anno{\"key\"}()", "("),
				Arguments.of("func on integer", "123()", "("),
				Arguments.of("func on float", "1.234()", "("),
				Arguments.of("func on boolean (true)", "true()", "("),
				Arguments.of("func on boolean (false)", "false()", "("),
				Arguments.of("func on string", "\"test\"()", "("),
				Arguments.of("func on incomplete path", "path.()", "(")
				//TODO more examples
		);
	}

	@ParameterizedTest
	@MethodSource("invalidExpressions")
	void testInvalidExpressions(String desc, String expression, String offendingToken) {
		assertInvalidParse(expression, desc, offendingToken, IQL_TestParser::standaloneExpression);
	}
}
