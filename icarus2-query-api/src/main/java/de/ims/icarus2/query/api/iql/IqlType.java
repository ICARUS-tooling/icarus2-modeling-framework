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
	QUERY("Query", null, IqlQuery.class),
	IMPORT("Import", "import", IqlImport.class),
	PROPERTY("Property", "prop", IqlProperty.class),
	CORPUS("Corpus", "corpus", IqlCorpus.class),
	LAYER("Layer", "layer", IqlLayer.class),
	SCOPE("Scope", "scope", IqlScope.class),
	PAYLOAD("Payload", null, IqlPayload.class),
	GROUP("Group", "group", IqlGroup.class),
	DATA("Data", "data", IqlData.class),
	BINDING("Binding", null, IqlBinding.class),
	VARIABLE("Variable", "var", IqlReference.IqlVariable.class),
	MEMBER("Member", "member", IqlReference.IqlMember.class),
	EXPRESSION("Expression", null, IqlExpression.class),
	PREDICATE("Predicate", "pred", IqlConstraint.IqlPredicate.class),
	TERM("Term", "term", IqlConstraint.IqlTerm.class),
	QUANTIFIER("Quantifier", null, IqlQuantifier.class),
	EDGE("Edge", "edge", IqlElement.IqlEdge.class),
	NODE("Node", "node", IqlElement.IqlNode.class),
	TREE_NODE("TreeNode", "tree", IqlElement.IqlTreeNode.class),
	RESULT_INSTRUCTION("ResultInstruction", "res", IqlResultInstruction.class),
	;

	private IqlType(String id, String uidPrefix, Class<?> type) {
		this.id = IqlConstants.IQL_PREFIX+requireNonNull(id);
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
