/**
 *
 */
package de.ims.icarus2.model.standard.members.structure;

import static de.ims.icarus2.model.util.ModelUtils.getName;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.item.Edge;

/**
 * Utility class to create exceptions for the handling of structure-related
 * error conditions.
 *
 * @author Markus Gärtner
 *
 */
class Nodes {

	private static String s(boolean incoming) {
		return incoming ? "incoming" : "outgoing";
	}

	private static String S(boolean incoming) {
		return incoming ? "Incoming" : "Outgoing";
	}

	static ModelException edgeAlreadySet(boolean incoming, Edge edge) {
		return new ModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
				S(incoming)+" edge already set: "+getName(edge));
	}

	static ModelException edgeAlreadyPresent(boolean incoming, Edge edge) {
		return new ModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
				S(incoming)+" edge already present: "+getName(edge));
	}

	static ModelException noEdgeForIndex(boolean incoming, long index) {
		return new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
				"No "+s(incoming)+" edge for index: "+index);
	}

	static ModelException noEdgeDefined(boolean incoming, Edge edge) {
		return new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
				"No "+s(incoming)+" edge defined - cannot remove "+getName(edge));
	}

	static ModelException unknownEdge(boolean incoming, Edge edge) {
		return new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
				"Unknown "+s(incoming)+" edge: "+getName(edge));
	}
}
