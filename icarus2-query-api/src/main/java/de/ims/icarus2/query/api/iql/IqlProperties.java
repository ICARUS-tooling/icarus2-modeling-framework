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

import static de.ims.icarus2.query.api.iql.IqlConstants.IQL_NS_PREFIX;

/**
 * @author Markus Gärtner
 *
 */
public final class IqlProperties {

	public static final String DIALECT = IQL_NS_PREFIX+"dialect";

	public static final String IMPORTS = IQL_NS_PREFIX+"imports";

	public static final String SETUP = IQL_NS_PREFIX+"setup";

	public static final String NAME = IQL_NS_PREFIX+"name";

	public static final String PID = IQL_NS_PREFIX+"pid";

	public static final String ALIAS = IQL_NS_PREFIX+"alias";

	public static final String OPTIONAL = IQL_NS_PREFIX+"optional";

	public static final String BINDINGS = IQL_NS_PREFIX+"bindings";

	public static final String FILTER = IQL_NS_PREFIX+"filter";

	public static final String LANES = IQL_NS_PREFIX+"lanes";

	public static final String KEY = IQL_NS_PREFIX+"key";

	public static final String VALUE = IQL_NS_PREFIX+"value";

	public static final String EMBEDDED_DATA = IQL_NS_PREFIX+"embeddedData";

	public static final String CONTENT = IQL_NS_PREFIX+"content";

	public static final String VARIABLE = IQL_NS_PREFIX+"variable";

	public static final String CODEC = IQL_NS_PREFIX+"codec";

	public static final String CHECKSUM = IQL_NS_PREFIX+"checksum";

	public static final String CHECKSUM_TYPE = IQL_NS_PREFIX+"checksumType";

	public static final String CORPUS = IQL_NS_PREFIX+"corpus";

	public static final String STREAMS = IQL_NS_PREFIX+"streams";

	public static final String LAYERS = IQL_NS_PREFIX+"layers";

	public static final String PRIMARY = IQL_NS_PREFIX+"primary";

	public static final String MARKER = IQL_NS_PREFIX+"marker";

	public static final String ALL_MEMBERS = IQL_NS_PREFIX+"allMembers";

	public static final String SCOPE = IQL_NS_PREFIX+"scope";

	public static final String RAW_PAYLOAD = IQL_NS_PREFIX+"rawPayload";

	public static final String PAYLOAD = IQL_NS_PREFIX+"payload";

	public static final String RAW_GROUPING = IQL_NS_PREFIX+"rawGrouping";

	public static final String GROUPING = IQL_NS_PREFIX+"grouping";

	public static final String RAW_RESULT = IQL_NS_PREFIX+"rawResult";

	public static final String RESULT = IQL_NS_PREFIX+"result";

	public static final String RESULT_TYPES = IQL_NS_PREFIX+"resultTypes";

	public static final String RESULT_INSTRUCTIONS = IQL_NS_PREFIX+"resultInstructions";

	public static final String GROUP_BY = IQL_NS_PREFIX+"groupBy";

	public static final String FILTER_ON = IQL_NS_PREFIX+"filterOn";

	public static final String LABEL = IQL_NS_PREFIX+"label";

	public static final String CONSUMED = IQL_NS_PREFIX+"consumed";

	public static final String DEFAULT_VALUE = IQL_NS_PREFIX+"defaultValue";

	public static final String DISTINCT = IQL_NS_PREFIX+"distinct";

	public static final String EDGES = IQL_NS_PREFIX+"edges";

	public static final String MEMBERS = IQL_NS_PREFIX+"members";

	public static final String TARGET = IQL_NS_PREFIX+"target";

	public static final String QUANTIFIER_TYPE = IQL_NS_PREFIX+"quantifierType";

	public static final String QUANTIFIER_MODIFIER = IQL_NS_PREFIX+"quantifierModifier";

	public static final String LOWER_BOUND = IQL_NS_PREFIX+"lowerBound";

	public static final String UPPER_BOUND = IQL_NS_PREFIX+"upperBound";

	public static final String ELEMENTS = IQL_NS_PREFIX+"elements";

	public static final String ELEMENT = IQL_NS_PREFIX+"element";

	public static final String ARRANGEMENT = IQL_NS_PREFIX+"arrangement";

	public static final String CONSTRAINT = IQL_NS_PREFIX+"constraint";

	public static final String QUANTIFIERS = IQL_NS_PREFIX+"quantifiers";

	public static final String CHILDREN = IQL_NS_PREFIX+"children";

	public static final String NODES = IQL_NS_PREFIX+"nodes";

	public static final String ALTERNATIVES = IQL_NS_PREFIX+"alternatives";

	public static final String EDGE_TYPE = IQL_NS_PREFIX+"edgeType";

	public static final String REFERENCE_TYPE = IQL_NS_PREFIX+"referenceType";

	public static final String QUERY_TYPE = IQL_NS_PREFIX+"queryType";

	public static final String QUERY_MODIFIER = IQL_NS_PREFIX+"queryModifier";

	public static final String LANE_TYPE = IQL_NS_PREFIX+"laneType";

	public static final String SOURCE = IQL_NS_PREFIX+"source";

	public static final String RETURN_TYPE = IQL_NS_PREFIX+"returnType";

	public static final String SOLVED = IQL_NS_PREFIX+"solved";

	public static final String SOLVED_AS = IQL_NS_PREFIX+"solvedAs";

	public static final String EXPRESSION = IQL_NS_PREFIX+"expression";

	public static final String ITEMS = IQL_NS_PREFIX+"items";

	public static final String OPERATION = IQL_NS_PREFIX+"operation";

	public static final String ORDER = IQL_NS_PREFIX+"order";

	public static final String SORTINGS = IQL_NS_PREFIX+"sortings";

	public static final String LIMIT = IQL_NS_PREFIX+"limit";

	public static final String PERCENT = IQL_NS_PREFIX+"percent";

	public static final String START = IQL_NS_PREFIX+"start";

	public static final String STOP = IQL_NS_PREFIX+"stop";

	public static final String FRAGMENT = IQL_NS_PREFIX+"fragment";

	public static final String ARGUMENTS = IQL_NS_PREFIX+"arguments";

	public static final String EXPRESSION_TYPE = IQL_NS_PREFIX+"expressionType";
}
