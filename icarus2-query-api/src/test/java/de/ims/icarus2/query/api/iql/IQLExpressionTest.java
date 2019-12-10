/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static de.ims.icarus2.query.api.iql.IQLTestUtils.assertParsedTree;
import static de.ims.icarus2.query.api.iql.IQLTestUtils.createParser;
import static de.ims.icarus2.query.api.iql.IQLTestUtils.simplify;
import static de.ims.icarus2.test.util.Pair.pair;
import static de.ims.icarus2.util.IcarusUtils.notEq;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.provider.Arguments;

import de.ims.icarus2.query.api.iql.IQL_TestParser.ExpressionTestContext;
import de.ims.icarus2.test.util.Pair;

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
	private List<List<Pair<String, String>>> binaryOpsHierarchy() {
		return list(
				list(pair("*", "multiplication"), pair("/", "division"), pair("%", "modulo")),
				list(pair("+", "addition"), pair("-", "subtraction")),
				list(pair("<<", "shift left"), pair(">>", "shift right"), pair("&", "bitwise and"), pair("|", "bitwise or"), pair("^", "bitwise xor")),
				list(pair("<", "less than"), pair("<=", "less than or equal"), pair(">", "greater than"), pair(">=", "greater than or equal")),
				list(pair("~", "matches"), pair("!~", "matches not"), pair("#", "contains"), pair("!#", "contains not")),
				list(pair("==", "equals"), pair("!=", "not equal")),
				list(pair("&&", "and"), pair("and", "and (keyword)")),
				list(pair("||", "or"), pair("or", "or (keyword)"))
		);
	}

	private Stream<Pair<String, String>> binaryOps() {
		return binaryOpsHierarchy().stream().flatMap(l -> l.stream());
	}

	private Stream<Pair<String, String>> comparisons() {
		return Stream.of(
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
	}

	private Stream<Pair<String, String>> literals() {
		return Stream.of(
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
	}

	private Stream<Pair<String, String>> strings() {
		return Stream.of(
				pair("\"\"", "empty string"),
				pair("\"test\"", "string"),
				pair("\"test with stuff\"", "string with whitespaces")
		);
	}

	private Stream<Pair<String, String>> indices() {
		return Stream.of(
				pair("123", "unsigned int"),
				pair("-123", "signed negative int"),
				pair("+123", "signed positive int")
		);
	}

	private Stream<Pair<String, String>> references() {
		return Stream.of(
				pair("ref", "named ref"),
				pair("path.to.ref", "path"),
				pair("@var", "named var"),
				pair("@var.to.path", "path of var")
		);
	}

	private Stream<Pair<String, String>> callableReferences() {
		return Stream.of(
				pair("ref", "named ref"),
				pair("path.to.ref", "path"),
				pair("@var.to.path", "path of var")
		);
	}

	private Stream<Pair<String, String>> multiArgs() {
		return Stream.of(
				pair("123, 456, 789", "multi ints"),
				pair("@var, \"test\", 4.5", "var, string, float"),
				pair("-123, 765.89", "negative int, float"),
				pair("123, -765.89", "int, negative float")
		);
	}

	/** Literals, references, array lookups, calls and annotations */
	private Stream<Pair<String, String>> elements() {
		return Stream.of(
				literals(),
				references(),
				callableReferences().map(pRef -> func(pRef, noArgs)),
				callableReferences().flatMap(pRef -> literals().map(pLit -> func(pRef, pLit))),
				callableReferences().flatMap(pRef -> multiArgs().map(pArgs -> func(pRef, pArgs))),
				references().flatMap(pRef -> indices().map(pIdx -> array(pRef, pIdx))),
				references().flatMap(pRef -> strings().map(pAnno -> annotation(pRef, pAnno)))
			).reduce(Stream::concat).get();
	}

	/** Combine a bunch of values to produce argument lists */
	private Pair<String, String> arguments(Pair<String, String> ...elements) {
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

	private Pair<String, String> func(Pair<String, String> ref, Pair<String, String> args) {
		return pair(ref.first+"("+args.first+")", "call on '"+ref.second+"' with '"+args.second+"' args");
	}

	private Pair<String, String> array(Pair<String, String> ref, Pair<String, String> index) {
		return pair(ref.first+"["+index.first+"]", "index of '"+ref.second+"' with "+index.second);
	}

	private Pair<String, String> annotation(Pair<String, String> ref, Pair<String, String> index) {
		return pair(ref.first+"{"+index.first+"}", "annotation of '"+ref.second+"' with "+index.second);
	}

	private static final String dummy = "123";
	private static final Pair<String, String> noArgs = pair("","<none>");

	Stream<Pair<String, String>> variateBinary(Pair<String, String> op,
			Pair<String, String> arg) {
		return Stream.of(
				pair(arg.first+op.first+dummy, "left "+op.second+" of "+arg.second),
				pair(dummy+op.first+arg.first, "right "+op.second+" of "+arg.second),
				pair(arg.first+op.first+arg.first, "dual "+op.second+" of "+arg.second)
		);
	}

	@TestFactory
	@DisplayName("test binary operations: <left><op><right>")
	Stream<DynamicNode> testBinaryOps() {
		return binaryOps()
				.map(pOp -> dynamicContainer(pOp.second, elements()
					.map(pArg -> dynamicContainer(pArg.second, variateBinary(pOp, pArg)
						.map(pTest -> dynamicTest(pTest.second+": "+pTest.first, () -> {
							String text = pTest.first;
							String description = pTest.second;
							IQL_TestParser parser = createParser(text, description, null, false);
							ExpressionTestContext ctx = parser.expressionTest();
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
	@DisplayName("test binary comparisons: <left><comp><right>")
	Stream<DynamicNode> testBinaryComparisons() {
		return comparisons()
				.map(pComp -> dynamicContainer(pComp.second, elements()
					.map(pArg -> dynamicContainer(pArg.second, variateBinary(pComp, pArg)
						.map(pTest -> dynamicTest(pTest.second+": "+pTest.first, () -> {
							String text = pTest.first;
							String description = pTest.second;
							IQL_TestParser parser = createParser(text, description, null, false);
							ExpressionTestContext ctx = parser.expressionTest();
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

	/**
	 * Order of operands:  ^, *, /, %, +, -, &, |
	 */
	public static Stream<Arguments> createNestedExpressions() {
		return Stream.of(
				// SUB nested in ADD
				Arguments.of("123-456+789",     "[[[123][-][456]][+][789]]", "SUB in ADD"),
				Arguments.of("123 - 456 + 789", "[[[123][-][456]][+][789]]", "SUB in ADD with spaces"),
				Arguments.of("(123 - 456) + 789", "[[(123-456)][+][789]]", "SUB in ADD with brackets"),
				// ADD nested in sub
				Arguments.of("123 - (456 + 789)", "[[123][-][(456+789)]]", "ADD in SUB with brackets")
				//TODO maybe move this to csv file?
				//TODO add more expressions to test
		);
	}

	private static String f(String format, Object...args) {
		return String.format(format, args);
	}

	private static boolean isKeywordOp(Pair<String, String> op) {
		return Character.isLetter(op.first.charAt(0));
	}

	private static DynamicTest testNestedOp(String expression, String expected, String desc) {
		return dynamicTest(desc+": "+expression+"  ->  "+expected, () ->
			assertParsedTree(expression, expected, desc, IQL_TestParser::expressionTest, true));
	}

	private static DynamicNode testPrivilegedNestedOp(Pair<String, String> op, Pair<String, String> nested) {
		List<DynamicTest> tests = new ArrayList<>();

		// Only skip whitespace if we have no keyword-based operators
		if(!isKeywordOp(op) && !isKeywordOp(nested)) {
			// No brackets and no whitespaces
			tests.add(testNestedOp(f("123%s456%s789", nested.first, op.first),
						f("[[123][%s][[456][%s][789]]]", nested.first, op.first),
						f("'%s' in '%s' without spaces", nested.second, op.second)));

			// Brackets to promote the nested op
			tests.add(testNestedOp(f("(123%s456)%s789", nested.first, op.first),
						f("[[(123%s456)][%s][789]]", nested.first, op.first),
						f("'%s' in '%s' with brackets and no spaces", nested.second, op.second)));
		}

		// Whitespace variant is always expected to work!
		tests.add(testNestedOp(f("123 %s 456 %s 789", nested.first, op.first),
					f("[[123][%s][[456][%s][789]]]", nested.first, op.first),
					f("'%s' in '%s' with spaces", nested.second, op.second)));

		// Brackets to promote the nested op
		tests.add(testNestedOp(f("(123 %s 456) %s 789", nested.first, op.first),
					f("[[(123%s456)][%s][789]]", nested.first, op.first),
					f("'%s' in '%s' with brackets and spaces", nested.second, op.second)));

		return dynamicContainer(f("%s in %s", nested.second, op.second), tests);
	}

	private static DynamicNode testEqualNestedOp(Pair<String, String> op, Pair<String, String> nested) {
		List<DynamicTest> tests = new ArrayList<>();

		// Only skip whitespace if we have no keyword-based operators
		if(!isKeywordOp(op) && !isKeywordOp(nested)) {
			tests.add(testNestedOp(f("123%s456%s789", nested.first, op.first),
						f("[[[123][%s][456]][%s][789]]", nested.first, op.first),
						f("'%s' in '%s' without spaces", nested.second, op.second)));
		}

		// Whitespace variant is always expected to work!
		tests.add(testNestedOp(f("123 %s 456 %s 789", nested.first, op.first),
					f("[[[123][%s][456]][%s][789]]", nested.first, op.first),
					f("'%s' in '%s' with spaces", nested.second, op.second)));

		return dynamicContainer(f("'%s' in '%s'", nested.second, op.second), tests);
	}

	@TestFactory
	@DisplayName("test direct nesting of binary operators according to their priorities")
	List<DynamicNode> testOpNesting() {
		List<List<Pair<String, String>>> hierarchy = binaryOpsHierarchy();
		int levels = hierarchy.size();

		List<DynamicNode> tests = new ArrayList<>();

		for (int level = 0; level < levels; level++) {
			List<Pair<String, String>> ops = hierarchy.get(level);
			int opCount = ops.size();

			for (int opIndex = 0; opIndex < opCount; opIndex++) {
				Pair<String, String> op = ops.get(opIndex);

				tests.add(dynamicContainer(op.second+" <equal level>",
						ops.stream().filter(notEq(op)).map(eqOp -> testEqualNestedOp(op, eqOp))));

				// If we have a privileged op, verify against all ops of lower precedence
				if(level<levels-1) {
					tests.add(dynamicContainer(op.second+" <lower levels>",
							IntStream.range(level+1, levels) // all lower levels
							.mapToObj(l -> hierarchy.get(l)) // fetch group for elvel
							.flatMap(group -> group.stream()) // expand all groups
							.map(lesserOp -> testPrivilegedNestedOp(op, lesserOp))));
				}
			}
		}

		return tests;
	}
}
