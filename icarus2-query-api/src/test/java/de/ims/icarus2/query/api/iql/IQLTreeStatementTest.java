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
/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static de.ims.icarus2.query.api.iql.IQLTestUtils.assertParsedTree;
import static de.ims.icarus2.query.api.iql.IQLTestUtils.f1;
import static de.ims.icarus2.query.api.iql.IQLTestUtils.f1Tree;
import static de.ims.icarus2.query.api.iql.IQLTestUtils.f2;
import static de.ims.icarus2.query.api.iql.IQLTestUtils.randomExpressions;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.ims.icarus2.query.api.iql.antlr.IQL_TestParser;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
public class IQLTreeStatementTest {

	private static void assertParsedTreeStatament(String statement, String expected, String desc) {
		assertParsedTree(statement, expected, desc, IQL_TestParser::standaloneTreeStatement, true);
	}

	private static DynamicTest makeTreeStatementTreeTest(String statement, String expected, String desc) {
		return dynamicTest(desc+": "+statement+"  ->  "+expected, () ->
			assertParsedTreeStatament(statement, expected, desc));
	}

	@CsvSource({
		"[], [[[\\[][\\]]]], single empty tree",
		"[] [], [[\\[\\]][\\[\\]]], two empty trees",
	})
	@ParameterizedTest
	void testEmptyTrees(String statement, String expected, String desc) {
		assertParsedTreeStatament(statement, expected, desc);
	}

	@CsvSource({
		"[[]], [[[\\[][\\[\\]][\\]]]], single nested tree",
		"[[] []], [[[\\[][[\\[\\]][\\[\\]]][\\]]]], two nested trees",
	})
	@ParameterizedTest
	void testBestedTrees(String statement, String expected, String desc) {
		assertParsedTreeStatament(statement, expected, desc);
	}

	@CsvSource({
		"[$a:], [[[\\[][$a:][\\]]]], single named tree",
		"[$a:] [$b:], [[\\[$a:\\]][\\[$b:\\]]], two named trees",
		"[$a: [$b:]], [[[\\[][$a:][\\[$b:\\]][\\]]]], two named nested trees",
	})
	@ParameterizedTest
	void testNamedNodes(String statement, String expected, String desc) {
		assertParsedTreeStatament(statement, expected, desc);
	}

	@CsvSource({
		"1[], 				[[[1][\\[][\\]]]], 					specifically quantified tree",
		"2+[], 				[[[2+][\\[][\\]]]], 				lower bound quantified tree",
		"4-[], 				[[[4-][\\[][\\]]]],					upper bound quantified tree",
		"<2+>[], 			[[[<2+>][\\[][\\]]]], 				lower bound quantified tree",
		"<4->[], 			[[[<4->][\\[][\\]]]], 				upper bound quantified tree",
		"<4-|6..10|13+>[], 	[[[<4-|6..10|13+>][\\[][\\]]]], 	complex quantified tree",
		"*[], 				[[[*][\\[][\\]]]], 					universally quantified tree",
		"all[], 			[[[all][\\[][\\]]]], 				universally (keyword)quantified tree",
		"![], 				[[[!][\\[][\\]]]], 					existentially negated tree",
	})
	@ParameterizedTest
	void testQuantifiedEmptyTrees(String statement, String expected, String desc) {
		assertParsedTreeStatament(statement, expected, desc);
	}

	@CsvSource({
		"[a>3], [[[\\[][a>3][\\]]]], single node with simple constraint",
		"[a>3 && pos~\"N[NP]\"], [[[\\[][a>3&&pos~\"N\\[NP\\]\"][\\]]]], single node with complex constraint",
		"[pos==\"NN\"] [chars()<7], [[\\[pos==\"NN\"\\]][\\[chars()<7\\]]], two trees with constraints",
		"[pos==\"NN\" [chars()<7]], [[[\\[][pos==\"NN\"][[\\[chars()<7\\]]][\\]]]], two nested trees with constraints",
	})
	@ParameterizedTest
	void testNodesWithInnerConstraints(String statement, String expected, String desc) {
		assertParsedTreeStatament(statement, expected, desc);
	}

	@RandomizedTest
	@TestFactory
	Stream<DynamicNode> testNodeWithRandomInnerConstraints(RandomGenerator rng) {
		return randomExpressions(rng, 10, false).stream().map(pExp -> makeTreeStatementTreeTest(
				f1("[{1}]", pExp),
				f1Tree("[[[\\[][{1}][\\]]]]", pExp),
				f2("single node with {1}", pExp)));
	}

	//TODO add tests for braced or disjunctive tree statements
}
