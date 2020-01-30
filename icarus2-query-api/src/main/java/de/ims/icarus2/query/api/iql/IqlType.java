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

import static java.util.Objects.requireNonNull;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.query.api.QueryErrorCode;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
@JsonIgnoreType
public enum IqlType {
	QUERY("Query", "query", IqlQuery.class),
	IMPORT("Import", "import", IqlImport.class),
	PROPERTY("Property", "prop", IqlProperty.class),
	CORPUS("Corpus", "corpus", IqlCorpus.class),
	LAYER("Layer", "layer", IqlLayer.class),
	SCOPE("Scope", "scope", IqlScope.class),
	PAYLOAD("Payload", "stream", IqlPayload.class),
	GROUP("Group", "group", IqlGroup.class),
	DATA("Data", "data", IqlData.class),
	BINDING("Binding", null, IqlBinding.class),
	REFERENCE("Reference", "ref", IqlReference.class),
	EXPRESSION("Expression", null, IqlExpression.class),
	PREDICATE("Predicate", "pred", IqlConstraint.IqlPredicate.class),
	TERM("Term", "term", IqlConstraint.IqlTerm.class),
	QUANTIFIER("Quantifier", null, IqlQuantifier.class),
	EDGE("Edge", "edge", IqlElement.IqlEdge.class),
	NODE("Node", "node", IqlElement.IqlNode.class),
	TREE_NODE("TreeNode", "tree", IqlElement.IqlTreeNode.class),
	ELEMENT_DISJUNCTION("ElementDisjunction", "choice", IqlElement.IqlElementDisjunction.class),
	RESULT("Result", null, IqlResult.class),
	SORTING("Sorting", null, IqlSorting.class),
	RESULT_INSTRUCTION("ResultInstruction", "res", IqlResultInstruction.class),
	;

	private IqlType(String id, String uidPrefix, Class<?> type) {
		this.id = IqlConstants.IQL_NS_PREFIX+requireNonNull(id);
		this.uidPrefix = uidPrefix;
		this.type = requireNonNull(type);
	}

	private final String id;
	private final String uidPrefix;
	private final Class<?> type;

	public String getId() {
		return id;
	}
	public String getUidPrefix() {
		return uidPrefix;
	}
	public Class<?> getType() {
		return type;
	}

	private static final Map<String, IqlType> idMap = new Object2ObjectOpenHashMap<>();
	static {
		for(IqlType _type : values()) {
			IqlType present = idMap.putIfAbsent(_type.id, _type);
			if(present!=null)
				throw new InternalError("Duplicate id: "+_type.id);
		}
	}

	public static IqlType forId(String id) {
		requireNonNull(id);
		IqlType type = idMap.get(id);
		if(type==null)
			throw new IcarusRuntimeException(QueryErrorCode.UNKNOWN_TYPE, "Unknown type id: "+id);
		return type;
	}
}
