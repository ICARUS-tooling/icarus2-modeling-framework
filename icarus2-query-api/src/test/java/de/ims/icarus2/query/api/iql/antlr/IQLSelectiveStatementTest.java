/**
 *
 */
package de.ims.icarus2.query.api.iql.antlr;

import static de.ims.icarus2.query.api.iql.antlr.AntlrTestUtils.assertParsedTree;
import static de.ims.icarus2.query.api.iql.antlr.AntlrTestUtils.f1Tree;
import static de.ims.icarus2.query.api.iql.antlr.AntlrTestUtils.randomExpressions;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
public class IQLSelectiveStatementTest {

	private static void assertSelectiveStatament(String statement, String expected, String desc) {
		assertParsedTree(statement, expected, desc, IQL_TestParser::standaloneSelectiveStatement, true);
	}

	private static DynamicTest makeStatementTreeTest(String statement, String expected, String desc) {
		return dynamicTest(desc+": "+statement+"  ->  "+expected, () ->
			assertSelectiveStatament(statement, expected, desc));
	}

	@RandomizedTest
	@TestFactory
	Stream<DynamicNode> testFlatConstraint(RandomGenerator rng) {
		return randomExpressions(rng, 10, false).stream().map(pExp -> makeStatementTreeTest(
				pExp.first, f1Tree("[{1}]", pExp), pExp.second));
	}
}
