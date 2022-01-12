/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import javax.annotation.Nullable;

import de.ims.icarus2.model.api.members.item.Edge;

/**
 * @author Markus Gärtner
 *
 */
public class ChainNodeInfo implements NodeInfo {

	private Edge in, out;

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#getType()
	 */
	@Override
	public Type getType() {
		return Type.CHAIN;
	}

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
				throw Nodes.noEdgeForIndex(true, index);

			return in;
		}

		if(out==null || index!=0L)
			throw Nodes.noEdgeForIndex(false, index);

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
				throw Nodes.edgeAlreadySet(true, in);
			in = edge;
		} else {
			if(out!=null)
				throw Nodes.edgeAlreadySet(false, out);
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
				throw Nodes.noEdgeDefined(true, edge);
			if(in!=edge)
				throw Nodes.unknownEdge(true, edge);
			in = null;
		} else {
			if(out==null)
				throw Nodes.noEdgeDefined(false, edge);

			if(out!=edge)
				throw Nodes.unknownEdge(false, edge);
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
	public void setIn(@Nullable Edge in) {
		this.in = in;
	}

	/**
	 * Directly sets the outgoing edge to {@code out}
	 *
	 * @param out
	 */
	public void setOut(@Nullable Edge out) {
		this.out = out;
	}

}
