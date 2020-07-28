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
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * @author Markus Gärtner
 *
 */
public class IQLStructuralConstraintTest {

	private static void assertStructuralConstraint(String statement, String expected, String desc) {
		assertParsedTree(statement, expected, desc, IQL_TestParser::standaloneStructuralConstraint, true);
	}

	private static DynamicTest makeConstraintTreeTest(String statement, String expected, String desc) {
		return dynamicTest(desc+": "+statement+"  ->  "+expected, () ->
			assertStructuralConstraint(statement, expected, desc));
	}

	/*
	structuralConstraint
		: nodeStatement+														#structureSequence
		| <assoc=right> left=structuralConstraint or right=structuralConstraint	#structuralAlternatives
		;
	 */

	// SEQUENCES

	@CsvSource({
		"[], [[\\[\\]]], single empty node",
		"[] [], [[\\[\\]][\\[\\]]], two empty nodes",
	})
	@ParameterizedTest
	void testEmptyNodes(String statement, String expected, String desc) {
		assertStructuralConstraint(statement, expected, desc);
	}

	@CsvSource({
		"1[], 				[[1\\[\\]]], 				specifically quantified node",
		"2+[], 				[[2+\\[\\]]], 				lower bound quantified node",
		"4-[], 				[[4-\\[\\]]],				upper bound quantified node",
		"<2+>[], 			[[<2+>\\[\\]]], 			lower bound quantified node",
		"<4->[], 			[[<4->\\[\\]]], 			upper bound quantified node",
		"<4-|6..10|13+>[], 	[[<4-|6..10|13+>\\[\\]]], 	complex quantified node",
		"*[], 				[[*\\[\\]]], 				universally quantified node",
		"all[], 			[[all\\[\\]]], 				universally (keyword) quantified node",
		"![], 				[[!\\[\\]]], 				existentially negated node",
	})
	@ParameterizedTest
	void testQuantifiedEmptyNodes(String statement, String expected, String desc) {
		assertStructuralConstraint(statement, expected, desc);
	}



	// TREES


	@CsvSource({
		"[[]], [[\\[\\[\\]\\]]], single nested tree",
		"[[] []], [[\\[\\[\\]\\[\\]\\]]], two nested trees",
	})
	@ParameterizedTest
	void testNestedTrees(String statement, String expected, String desc) {
		assertStructuralConstraint(statement, expected, desc);
	}

	@CsvSource({
		"[x] or [y], [[\\[x\\]or\\[y\\]]], 2 alternative nodes",
		"[x][y] or [z], [[\\[x\\]][\\[y\\]or\\[z\\]]], node plus disjunction",
		"{[x][y]} or [z], [[{\\[x\\]\\[y\\]}or\\[z\\]]], node set vs single node with braces",
		//TODO add more tests for braced or disjunctive tree statements
	})
	@ParameterizedTest
	void testTreeDisjunction(String statement, String expected, String desc) {
		assertStructuralConstraint(statement, expected, desc);
	}

	@CsvSource(delimiter=';', value={
		"[x] or [y]; [[\\[x\\]or\\[y\\]]]; 2 alternative nodes",
		"[x],[y] or [z]; [[\\[x\\],\\[y\\]or\\[z\\]]]; node set vs single node",
		"{[x],[y]} or [z]; [[{\\[x\\],\\[y\\]}or\\[z\\]]]; node set vs single node with braces",
		//TODO add more tests for braced or disjunctive graph statements
	})
	@ParameterizedTest
	void testGraphDisjunction(String statement, String expected, String desc) {
		assertStructuralConstraint(statement, expected, desc);
	}
}
