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
package de.ims.icarus2.model.standard.members.structure;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.item.Edge;

/**
 * @author Markus Gärtner
 *
 */
public class LeafNodeInfo implements NodeInfo {

	private Edge in;

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#getType()
	 */
	@Override
	public Type getType() {
		return Type.LEAF;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#edgeCount()
	 */
	@Override
	public long edgeCount() {
		return in==null ? 0L : 1L;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#edgeCount(boolean)
	 */
	@Override
	public long edgeCount(boolean incoming) {
		return incoming ? edgeCount() : 0L;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#edgeAt(long, boolean)
	 */
	@Override
	public Edge edgeAt(long index, boolean incoming) {
		if(!incoming)
			throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					"No outgoing edges - invalid index "+index);
		if(in==null || index!=0L)
			throw Nodes.noEdgeForIndex(true, index);

		return in;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#addEdge(de.ims.icarus2.model.api.members.item.Edge, boolean)
	 */
	@Override
	public void addEdge(Edge edge, boolean incoming) {
		requireNonNull(edge);

		if(incoming) {
			if(in!=null)
				throw Nodes.edgeAlreadySet(true, in);
			this.in = edge;
		} else
			throw new ModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
					"Cannot set outgoing edge for dedicated leaf node");

	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#removeEdge(de.ims.icarus2.model.api.members.item.Edge, boolean)
	 */
	@Override
	public void removeEdge(Edge edge, boolean incoming) {
		requireNonNull(edge);

		if(incoming) {
			if(in!=edge)
				throw Nodes.unknownEdge(true, edge);
			this.in = null;
		} else
			throw new ModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
					"Cannot remove outgoing edge for dedicated leaf node");
	}

}
