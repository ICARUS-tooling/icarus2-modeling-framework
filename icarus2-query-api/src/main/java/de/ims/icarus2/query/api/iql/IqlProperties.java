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

import static de.ims.icarus2.query.api.iql.IqlConstants.IQL_PREFIX;

/**
 * @author Markus Gärtner
 *
 */
public final class IqlProperties {

	public static final String DIALECT = IQL_PREFIX+"dialect";

	public static final String IMPORTS = IQL_PREFIX+"imports";

	public static final String SETUP = IQL_PREFIX+"setup";

	public static final String NAME = IQL_PREFIX+"name";

	public static final String ALIAS = IQL_PREFIX+"alias";

	public static final String OPTIONAL = IQL_PREFIX+"optional";

	public static final String BINDINGS = IQL_PREFIX+"bindings";

	public static final String KEY = IQL_PREFIX+"key";

	public static final String VALUE = IQL_PREFIX+"value";

	public static final String EMBEDDED_DATA = IQL_PREFIX+"embeddedData";

	public static final String CONTENT = IQL_PREFIX+"content";

	public static final String VARIABLE = IQL_PREFIX+"variable";

	public static final String CHECKSUM = IQL_PREFIX+"checksum";

	public static final String CORPORA = IQL_PREFIX+"corpora";

	public static final String LAYERS = IQL_PREFIX+"layers";

	public static final String PRIMARY = IQL_PREFIX+"primary";

	public static final String ALL_MEMBERS = IQL_PREFIX+"allMembers";

	public static final String SCOPES = IQL_PREFIX+"scopes";

	public static final String RAW_PAYLOAD = IQL_PREFIX+"rawPayload";

	public static final String PAYLOAD = IQL_PREFIX+"payload";

	public static final String RAW_GROUPING = IQL_PREFIX+"rawGrouping";

	public static final String GROUPING = IQL_PREFIX+"grouping";

	public static final String RAW_RESULT = IQL_PREFIX+"rawResult";

	public static final String RESULT = IQL_PREFIX+"result";

	public static final String RESULT_TYPES = IQL_PREFIX+"resultTypes";

	public static final String RESULT_INSTRUCTIONS = IQL_PREFIX+"resultInstructions";

	public static final String GROUP_BY = IQL_PREFIX+"groupBy";

	public static final String FILTER_ON = IQL_PREFIX+"filterOn";

	public static final String LABEL = IQL_PREFIX+"label";

	public static final String DEFAULT_VALUE = IQL_PREFIX+"defaultValue";

	public static final String DISTINCT = IQL_PREFIX+"distinct";

	public static final String MEMBERS = IQL_PREFIX+"members";

	public static final String TARGET = IQL_PREFIX+"target";

	public static final String QUANTIFIER_TYPE = IQL_PREFIX+"quantifierType";

	public static final String LOWER_BOUND = IQL_PREFIX+"lowerBound";

	public static final String UPPER_BOUND = IQL_PREFIX+"upperBound";

	public static final String ELEMENTS = IQL_PREFIX+"elements";

	public static final String CONSTRAINT = IQL_PREFIX+"constraint";

	public static final String QUANTIFIERS = IQL_PREFIX+"quantifiers";

	public static final String CHILDREN = IQL_PREFIX+"children";

	public static final String EDGE_TYPE = IQL_PREFIX+"edgeType";

	public static final String REFERENCE_TYPE = IQL_PREFIX+"referenceType";

	public static final String QUERY_TYPE = IQL_PREFIX+"queryType";

	public static final String SOURCE = IQL_PREFIX+"source";

	public static final String RETURN_TYPE = IQL_PREFIX+"returnType";

	public static final String SOLVED = IQL_PREFIX+"solved";

	public static final String SOLVED_AS = IQL_PREFIX+"solvedAs";

	public static final String EXPRESSION = IQL_PREFIX+"expression";

	public static final String LEFT = IQL_PREFIX+"left";

	public static final String RIGHT = IQL_PREFIX+"right";

	public static final String OPERATION = IQL_PREFIX+"operation";

	public static final String ORDER = IQL_PREFIX+"order";

	public static final String SORTINGS = IQL_PREFIX+"sortings";

	public static final String LIMIT = IQL_PREFIX+"limit";

	public static final String PERCENT = IQL_PREFIX+"percent";

	public static final String START = IQL_PREFIX+"start";

	public static final String STOP = IQL_PREFIX+"stop";

	public static final String FRAGMENT = IQL_PREFIX+"fragment";
}
