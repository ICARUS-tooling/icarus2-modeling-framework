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
		// empty edges
		"[]---[]; [[[\\[\\]][---][\\[\\]]]]; undirected empty edge",
		"[] ---[]; [[[\\[\\]][---][\\[\\]]]]; undirected empty edge (space left)",
		"[]--- []; [[[\\[\\]][---][\\[\\]]]]; undirected empty edge (space right)",
		"[]-->[]; [[[\\[\\]][-->][\\[\\]]]]; right-directed empty edge",
		"[]<--[]; [[[\\[\\]][<--][\\[\\]]]]; left-directed empty edge",
		"[]<->[]; [[[\\[\\]][<->][\\[\\]]]]; bidirectional empty edge",
		// filled edges
		"[]--[]--[]; [[[\\[\\]][[--][\\[][\\]][--]][\\[\\]]]]; undirected empty edge",
		"[]--[]->[]; [[[\\[\\]][[--][\\[][\\]][->]][\\[\\]]]]; right-directed empty edge",
		"[]<-[]--[]; [[[\\[\\]][[<-][\\[][\\]][--]][\\[\\]]]]; left-directed empty edge",
		"[]<-[]->[]; [[[\\[\\]][[<-][\\[][\\]][->]][\\[\\]]]]; bidirectional empty edge",
	})
	@ParameterizedTest
	void testSimpleGraphElements(String statement, String expected, String desc) {
		assertParsedGraphStatament(statement, expected, desc);
	}

	@CsvSource(delimiter=';', value={
		// nodes
		"[$a:]; [[[[\\[][$a:][\\]]]]]; single node",
		"[$a:],[$b:]; [[[\\[$a:\\]]][,][[\\[$b:\\]]]]; two nodes",
		// empty edges
		"[$a:]---[$b:]; [[[\\[$a:\\]][---][\\[$b:\\]]]]; undirected empty edge",
		"[$a:]-->[$b:]; [[[\\[$a:\\]][-->][\\[$b:\\]]]]; right-directed empty edge",
		"[$a:]<--[$b:]; [[[\\[$a:\\]][<--][\\[$b:\\]]]]; left-directed empty edge",
		"[$a:]<->[$b:]; [[[\\[$a:\\]][<->][\\[$b:\\]]]]; bidirectional empty edge",
		// filled edges
		"[$a:]--[$c:]--[$b:]; [[[\\[$a:\\]][[--][\\[][$c:][\\]][--]][\\[$b:\\]]]]; undirected empty edge",
		"[$a:]--[$c:]->[$b:]; [[[\\[$a:\\]][[--][\\[][$c:][\\]][->]][\\[$b:\\]]]]; right-directed empty edge",
		"[$a:]<-[$c:]--[$b:]; [[[\\[$a:\\]][[<-][\\[][$c:][\\]][--]][\\[$b:\\]]]]; left-directed empty edge",
		"[$a:]<-[$c:]->[$b:]; [[[\\[$a:\\]][[<-][\\[][$c:][\\]][->]][\\[$b:\\]]]]; bidirectional empty edge",
	})
	@ParameterizedTest
	void testSimpleGraphElementsWithLabels(String statement, String expected, String desc) {
		assertParsedGraphStatament(statement, expected, desc);
	}

	@CsvSource(delimiter=';', value={
		// nodes
		"4-[]; [[[[4-][\\[][\\]]]]]; single node",
		"2+[],4|8[]; [[[2+\\[\\]]][,][[4|8\\[\\]]]]; two nodes",
		// empty edges
		"[]---4-[]; [[[\\[\\]][---][4-\\[\\]]]]; undirected empty right-quantified edge",
		"4-[]---[]; [[[4-\\[\\]][---][\\[\\]]]]; undirected empty left-quantified edge",
		// filled edges
		"[]--[]--4-[]; [[[\\[\\]][[--][\\[][\\]][--]][4-\\[\\]]]]; undirected empty right-quantified edge",
		"[]--[]->4-[]; [[[\\[\\]][[--][\\[][\\]][->]][4-\\[\\]]]]; right-directed empty right-quantified edge",
		"[]<-[]--4-[]; [[[\\[\\]][[<-][\\[][\\]][--]][4-\\[\\]]]]; left-directed empty right-quantified edge",
		"[]<-[]->4-[]; [[[\\[\\]][[<-][\\[][\\]][->]][4-\\[\\]]]]; bidirectional empty right-quantified edge",
		"4-[]--[]--[]; [[[4-\\[\\]][[--][\\[][\\]][--]][\\[\\]]]]; undirected empty left-quantified edge",
		"4-[]--[]->[]; [[[4-\\[\\]][[--][\\[][\\]][->]][\\[\\]]]]; right-directed empty left-quantified edge",
		"4-[]<-[]--[]; [[[4-\\[\\]][[<-][\\[][\\]][--]][\\[\\]]]]; left-directed empty left-quantified edge",
		"4-[]<-[]->[]; [[[4-\\[\\]][[<-][\\[][\\]][->]][\\[\\]]]]; bidirectional empty left-quantified edge",
	})
	@ParameterizedTest
	void testQuantifiedGraphElements(String statement, String expected, String desc) {
		assertParsedGraphStatament(statement, expected, desc);
	}

	@CsvSource(delimiter=';', value={
		// nodes
		"[a>23]; [[[[\\[][a>23][\\]]]]]; single node",
		"2+[a>23],4|8[b==0]; [[[2+\\[a>23\\]]][,][[4|8\\[b==0\\]]]]; two nodes",
		// empty edges
		"[a>23]---4-[b==0]; [[[\\[a>23\\]][---][4-\\[b==0\\]]]]; undirected empty right-quantified edge",
		"4-[b==0]---[a<23]; [[[4-\\[b==0\\]][---][\\[a<23\\]]]]; undirected empty left-quantified edge",
		// filled edges
		"[]--[a<23]--4-[]; [[[\\[\\]][[--][\\[][a<23][\\]][--]][4-\\[\\]]]]; undirected empty right-quantified edge",
		"[]--[a<23]->4-[]; [[[\\[\\]][[--][\\[][a<23][\\]][->]][4-\\[\\]]]]; right-directed empty right-quantified edge",
		"[]<-[a<23]--4-[]; [[[\\[\\]][[<-][\\[][a<23][\\]][--]][4-\\[\\]]]]; left-directed empty right-quantified edge",
		"[]<-[a<23]->4-[]; [[[\\[\\]][[<-][\\[][a<23][\\]][->]][4-\\[\\]]]]; bidirectional empty right-quantified edge",
		"4-[]--[a<23]--[]; [[[4-\\[\\]][[--][\\[][a<23][\\]][--]][\\[\\]]]]; undirected empty left-quantified edge",
		"4-[]--[a<23]->[]; [[[4-\\[\\]][[--][\\[][a<23][\\]][->]][\\[\\]]]]; right-directed empty left-quantified edge",
		"4-[]<-[a<23]--[]; [[[4-\\[\\]][[<-][\\[][a<23][\\]][--]][\\[\\]]]]; left-directed empty left-quantified edge",
		"4-[]<-[a<23]->[]; [[[4-\\[\\]][[<-][\\[][a<23][\\]][->]][\\[\\]]]]; bidirectional empty left-quantified edge",
	})
	@ParameterizedTest
	void testGraphElementsWithCosntraints(String statement, String expected, String desc) {
		assertParsedGraphStatament(statement, expected, desc);
	}

	@CsvSource(delimiter=';', value={
		"[x] or [y]; [[\\[x\\]][or][\\[y\\]]]; 2 alternative nodes",
		"[x],[y] or [z]; [[[\\[x\\]][,][\\[y\\]]][or][\\[z\\]]]; node set vs single node",
		"{[x],[y]} or [z]; [[[{][[\\[x\\]][,][\\[y\\]]][}]][or][\\[z\\]]]; node set vs single node with braces",
		//TODO add more tests for braced or disjunctive graph statements
	})
	@ParameterizedTest
	void testGraphDisjunction(String statement, String expected, String desc) {
		assertParsedGraphStatament(statement, expected, desc);
	}

}
