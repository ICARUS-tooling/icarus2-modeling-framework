/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.structure;

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.manifest.util.Messages;

/**
 * @author Markus Gärtner
 *
 */
public class ChainNodeInfo implements NodeInfo {

	protected Edge in, out;

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#edgeCount()
	 */
	@Override
	public long edgeCount() {
		int count = 0;

		if(in!=null) {
			count++;
		}

		if(out!=null) {
			count++;
		}

		return count;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#edgeCount(boolean)
	 */
	@Override
	public long edgeCount(boolean incoming) {
		if((incoming && in!=null) || (!incoming && out!=null)) {
			return 1;
		}

		return 0;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#edgeAt(long, boolean)
	 */
	@Override
	public Edge edgeAt(long index, boolean incoming) {
		if(incoming) {
			if(in==null || index!=0L)
				throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
						"No incoming edge for index: "+index);

			return in;
		}

		if(out==null || index!=0L)
			throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					"No outgoing edge for index: "+index);

		return out;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#addEdge(de.ims.icarus2.model.api.members.item.Edge, boolean)
	 */
	@Override
	public void addEdge(Edge edge, boolean incoming) {
		requireNonNull(edge);

		if(incoming) {
			if(in!=null)
				throw new ModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
						"Incoming edge already present: "+getName(in));
			in = edge;
		} else {
			if(out!=null)
				throw new ModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
						"Outgoing edge already present: "+getName(in));
			out = edge;
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#removeEdge(de.ims.icarus2.model.api.members.item.Edge, boolean)
	 */
	@Override
	public void removeEdge(Edge edge, boolean incoming) {
		requireNonNull(edge);

		if(incoming) {
			if(in==null)
				throw new ModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
						"No incoming edge defined - cannot remove "+getName(edge));
			if(in!=edge)
				throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
						Messages.mismatch("Unknown incoming edge", getName(in), getName(edge)));
			in = null;
		} else {
			if(out==null)
				throw new ModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
						"No outgoing edge defined - cannot remove "+getName(edge));
			if(out!=edge)
				throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
						Messages.mismatch("Unknown outgoing edge", getName(out), getName(edge)));
			out = null;
		}
	}

	public Edge getIn() {
		return in;
	}

	public Edge getOut() {
		return out;
	}

	/**
	 * Directly sets the incoming edge to {@code in}
	 *
	 * @param in
	 */
	public void setIn(Edge in) {
		this.in = in;
	}

	/**
	 * Directly sets the outgoing edge to {@code out}
	 *
	 * @param out
	 */
	public void setOut(Edge out) {
		this.out = out;
	}

}
