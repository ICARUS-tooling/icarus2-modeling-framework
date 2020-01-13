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
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.ims.icarus2.query.api.iql.antlr.IQL_TestParser;

/**
 * @author Markus Gärtner
 *
 */
public class IQLGraphStatementTest {


	private static void assertParsedGraphStatament(String statement, String expected, String desc) {
		assertParsedTree(statement, expected, desc, IQL_TestParser::standaloneGraphStatement, true);
	}

	private static DynamicTest makeGraphStatementTreeTest(String statement, String expected, String desc) {
		return dynamicTest(desc+": "+statement+"  ->  "+expected, () ->
		assertParsedGraphStatament(statement, expected, desc));
	}

	@CsvSource(delimiter=';', value={
		// nodes
		"[]; [[[[\\[][\\]]]]]; single node",
		"[],[]; [[[\\[\\]]][,][[\\[\\]]]]; two nodes",
		// edges
		"[]---[]; [[[\\[\\]][---][\\[\\]]]]; undirected empty edge",
	})
	@ParameterizedTest
	void testSImpleGraphElements(String statement, String expected, String desc) {
		assertParsedGraphStatament(statement, expected, desc);
	}

	//TODO add tests for braced or disjunctive graph statements
}
