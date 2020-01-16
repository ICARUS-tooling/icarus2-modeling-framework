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
package de.ims.icarus2.query.api.iql.antlr;

import static de.ims.icarus2.query.api.iql.antlr.IQLTestUtils.assertParsedTree;
import static de.ims.icarus2.query.api.iql.antlr.IQLTestUtils.f1;
import static de.ims.icarus2.query.api.iql.antlr.IQLTestUtils.f1Tree;
import static de.ims.icarus2.query.api.iql.antlr.IQLTestUtils.f2;
import static de.ims.icarus2.query.api.iql.antlr.IQLTestUtils.randomExpressions;
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
public class IQLFlatStatementTest {

	private static void assertParsedStatament(String statement, String expected, String desc) {
		assertParsedTree(statement, expected, desc, IQL_TestParser::standaloneFlatStatement, true);
	}

	private static DynamicTest makeStatementTreeTest(String statement, String expected, String desc) {
		return dynamicTest(desc+": "+statement+"  ->  "+expected, () ->
			assertParsedStatament(statement, expected, desc));
	}

	@RandomizedTest
	@TestFactory
	Stream<DynamicNode> testFlatConstraint(RandomGenerator rng) {
		return randomExpressions(rng, 10, false).stream().map(pExp -> makeStatementTreeTest(
				pExp.first, f1Tree("[{1}]", pExp), pExp.second));
	}

	@CsvSource({
		"[], [[[\\[][\\]]]], single empty node",
		"[] [], [[\\[\\]][\\[\\]]], two empty nodes",
	})
	@ParameterizedTest
	void testEmptyNodes(String statement, String expected, String desc) {
		assertParsedStatament(statement, expected, desc);
	}

	@CsvSource({
		"[$a:], [[[\\[][$a:][\\]]]], single named node",
		"[$a:] [$b:], [[\\[$a:\\]][\\[$b:\\]]], two named nodes",
	})
	@ParameterizedTest
	void testNamedNodes(String statement, String expected, String desc) {
		assertParsedStatament(statement, expected, desc);
	}

	@CsvSource({
		"1[], 				[[[1][\\[][\\]]]], 					specifically quantified node",
		"2+[], 				[[[2+][\\[][\\]]]], 				lower bound quantified node",
		"4-[], 				[[[4-][\\[][\\]]]],					upper bound quantified node",
		"<2+>[], 			[[[<2+>][\\[][\\]]]], 				lower bound quantified node",
		"<4->[], 			[[[<4->][\\[][\\]]]], 				upper bound quantified node",
		"<4-|6..10|13+>[], 	[[[<4-|6..10|13+>][\\[][\\]]]], 	complex quantified node",
		"*[], 				[[[*][\\[][\\]]]], 					universally quantified node",
		"all[], 			[[[all][\\[][\\]]]], 				universally (keyword) quantified node",
		"![], 				[[[!][\\[][\\]]]], 					existentially negated node",
	})
	@ParameterizedTest
	void testQuantifiedEmptyNodes(String statement, String expected, String desc) {
		assertParsedStatament(statement, expected, desc);
	}

	@CsvSource({
		"[a>3], [[[\\[][a>3][\\]]]], single node with simple constraint",
		"[a>3 && pos~\"N[NP]\"], [[[\\[][a>3&&pos~\"N\\[NP\\]\"][\\]]]], single node with complex constraint",
		"[pos==\"NN\"] [chars()<7], [[\\[pos==\"NN\"\\]][\\[chars()<7\\]]], two nodes with constraints",
	})
	@ParameterizedTest
	void testNodesWithInnerConstraints(String statement, String expected, String desc) {
		assertParsedStatament(statement, expected, desc);
	}

	@RandomizedTest
	@TestFactory
	Stream<DynamicNode> testNodeWithRandomInnerConstraints(RandomGenerator rng) {
		return randomExpressions(rng, 10, false).stream().map(pExp -> makeStatementTreeTest(
				f1("[{1}]", pExp),
				f1Tree("[[[\\[][{1}][\\]]]]", pExp),
				f2("single node with {1}", pExp)));
	}
}
