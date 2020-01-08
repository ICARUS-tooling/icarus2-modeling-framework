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

import static de.ims.icarus2.util.IcarusUtils.ensureIntegerValueRange;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;

import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
public class GraphNodeInfo implements NodeInfo {

	private static final int INITIAL_CAPACITY = 5;

	/**
	 * All incoming and outgoing edges in a single array:
	 *
	 * <code>
	 * [--incoming--|--outgoing--]
	 * </code>
	 *
	 */
	private Edge[] edges;
	private int count_in = 0;
	private int count_out = 0;

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#edgeCount()
	 */
	@Override
	public long edgeCount() {
		return count_in + count_out;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#edgeCount(boolean)
	 */
	@Override
	public long edgeCount(boolean incoming) {
		return incoming ? count_in : count_out;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#edgeAt(long, boolean)
	 */
	@Override
	public Edge edgeAt(long index, boolean incoming) {
		int idx = ensureIntegerValueRange(index);

		if(incoming) {
			if(idx<0 || idx>=count_in)
				throw Nodes.noEdgeForIndex(true, idx);
			return edges[idx];
		}

		if(idx<0 || idx>=count_out)
			throw Nodes.noEdgeForIndex(false, idx);
		return edges[count_in+idx];
	}

	private int indexOf(Edge edge, boolean incoming) {
		if(edges!=null) {
			int start = incoming ? 0 : count_in;
			int limit = incoming ? count_in : edges.length;
			for (int i = start; i < limit; i++) {
				if(edges[i]==edge) {
					return i;
				}
			}
		}
		return IcarusUtils.UNSET_INT;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#addEdge(de.ims.icarus2.model.api.members.item.Edge, boolean)
	 */
	@Override
	public void addEdge(Edge edge, boolean incoming) {
		requireNonNull(edge);

		if(indexOf(edge, incoming)!=IcarusUtils.UNSET_INT)
			throw Nodes.edgeAlreadyPresent(incoming, edge);

		if(edges==null) {
			edges = new Edge[INITIAL_CAPACITY];
		} else if(count_in+count_out+1>=edges.length) {
			edges = Arrays.copyOf(edges, edges.length*2);
		}

		if(incoming) {
			System.arraycopy(edges, count_in, edges, count_in+1, count_out);
			edges[count_in++] = edge;
		} else {
			edges[count_in+count_out] = edge;
			count_out++;
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#removeEdge(de.ims.icarus2.model.api.members.item.Edge, boolean)
	 */
	@Override
	public void removeEdge(Edge edge, boolean incoming) {
		requireNonNull(edge);

		int index = indexOf(edge, incoming);
		if(index==IcarusUtils.UNSET_INT)
			throw Nodes.unknownEdge(incoming, edge);

		for (int i = index; i < edges.length-1; i++) {
			edges[i] = edges[i+1];
		}
		edges[edges.length-1] = null;

		if(incoming) {
			count_in--;
		} else {
			count_out--;
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#getType()
	 */
	@Override
	public Type getType() {
		return Type.GRAPH;
	}

}
