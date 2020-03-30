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
package de.ims.icarus2.query.api.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ims.icarus2.query.api.QuerySwitch;
import de.ims.icarus2.query.api.iql.IqlConstraint;
import de.ims.icarus2.query.api.iql.IqlConstraint.IqlPredicate;
import de.ims.icarus2.query.api.iql.IqlCorpus;
import de.ims.icarus2.query.api.iql.IqlElement.IqlNode;
import de.ims.icarus2.query.api.iql.IqlElement.IqlNodeSet;
import de.ims.icarus2.query.api.iql.IqlExpression;
import de.ims.icarus2.query.api.iql.IqlImport;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.query.api.iql.IqlLane.LaneType;
import de.ims.icarus2.query.api.iql.IqlObjectIdGenerator;
import de.ims.icarus2.query.api.iql.IqlPayload;
import de.ims.icarus2.query.api.iql.IqlPayload.QueryType;
import de.ims.icarus2.query.api.iql.IqlProperty;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.query.api.iql.IqlResult;
import de.ims.icarus2.query.api.iql.IqlResult.ResultType;
import de.ims.icarus2.query.api.iql.IqlStream;
import de.ims.icarus2.query.api.iql.IqlUtils;
import de.ims.icarus2.query.api.iql.NodeArrangement;

/**
 * @author Markus Gärtner
 *
 */
public class ExampleGen {

	public static void main(String[] args) throws Exception {
		IqlObjectIdGenerator gen = new IqlObjectIdGenerator();

		IqlQuery query = new IqlQuery();
		query.setId(gen.generateId(query));

		IqlProperty property = new IqlProperty();
		property.setKey(QuerySwitch.STRING_CASE_OFF.getKey());
		query.addSetup(property);

		IqlImport imp = new IqlImport();
		imp.setId(gen.generateId(imp));
		imp.setName("common.tagsets.stts");
		query.addImport(imp);

		IqlStream stream = new IqlStream();
		stream.setId(gen.generateId(stream));

		IqlCorpus corpus = new IqlCorpus();
		corpus.setId(gen.generateId(corpus));
		corpus.setName("TIGER v2");
		stream.setCorpus(corpus);

		IqlResult result = new IqlResult();
		result.setLimit(1000);
		result.addResultType(ResultType.KWIC);
//		IqlSorting sorting = new IqlSorting();
//		sorting.setOrder(Order.ASCENDING);
//		sorting.setExpression(exp("$np.size"));
//		result.addSorting(sorting);
		stream.setResult(result);

		stream.setRawPayload("FIND ADJACENT [pos==stts.ADJ][form==\"test\"]");

		IqlPayload payload = new IqlPayload();
		payload.setQueryType(QueryType.SINGLE_LANE);
		IqlLane lane = new IqlLane();
		lane.setLaneType(LaneType.SEQUENCE);
		IqlNodeSet nodeSet = new IqlNodeSet();
		nodeSet.setNodeArrangement(NodeArrangement.ADJACENT);
		IqlNode node1 = new IqlNode();
		node1.setConstraint(pred("pos==stts.ADJ"));
		nodeSet.addNode(node1);
		IqlNode node2 = new IqlNode();
		node2.setConstraint(pred("[form==\"test\"]"));
		nodeSet.addNode(node2);
		lane.setElements(nodeSet);
		payload.addLane(lane);

		stream.setPayload(payload);

		query.addStream(stream);

		ObjectMapper mapper = IqlUtils.createMapper();

		mapper.writerWithDefaultPrettyPrinter().writeValue(System.out, payload);
	}

	private static IqlExpression exp(String s) {
		IqlExpression expression = new IqlExpression();
		expression.setContent(s);
		return expression;
	}

	private static IqlConstraint pred(String s) {
		IqlPredicate pred = new IqlPredicate();
		pred.setExpression(exp(s));
		return pred;
	}
}
