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

import javax.annotation.Nullable;

import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
public class TreeNodeInfo implements NodeInfo {

	private static final int INITIAL_CAPACITY = 3;

	private Edge in;

	private Edge[] out;
	private int count = 0;

	//TODO add fields for all the depth/height/descendantCount etc stuff

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#edgeCount()
	 */
	@Override
	public long edgeCount() {
		return in==null ? count : count+1;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#edgeCount(boolean)
	 */
	@Override
	public long edgeCount(boolean incoming) {
		if(incoming) {
			return in==null ? 0 : 1;
		}
		return count;
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

		int idx = ensureIntegerValueRange(index);
		if(out==null || idx<0 || idx>=count)
			throw Nodes.noEdgeForIndex(false, index);

		return out[idx];
	}

	private int indexOf(Edge edge) {
		if(out!=null) {
			for (int i = 0; i < count; i++) {
				if(out[i]==edge) {
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

		if(incoming) {
			if(in!=null)
				throw Nodes.edgeAlreadyPresent(true, edge);
			in = edge;
		} else {
			if(indexOf(edge)!=IcarusUtils.UNSET_INT)
				throw Nodes.edgeAlreadyPresent(false, edge);

			if(out==null) {
				out = new Edge[INITIAL_CAPACITY];
			} else if(count+1>=out.length) {
				out = Arrays.copyOf(out, out.length*2);
			}

			out[count++] = edge;
		}
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
			in = null;
		} else {
			int index = indexOf(edge);
			if(index==IcarusUtils.UNSET_INT)
				throw Nodes.unknownEdge(false, edge);

			for (int i = index; i < out.length-1; i++) {
				out[i] = out[i+1];
			}
			out[out.length-1] = null;
			count--;
		}

	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#getType()
	 */
	@Override
	public Type getType() {
		return Type.TREE;
	}

	/**
	 * Directly sets the incoming edge
	 */
	public void setIn(@Nullable Edge in) {
		this.in = in;
	}

	/**
	 * Directly sets all the outgoing edges
	 */
	public void setOut(@Nullable Edge[] out) {
		this.out = out;
	}

	/**
	 * Reduces the internal buffer array for outgoing arrays to a size
	 * that exactly fits the current number of outgoing edges. If that count
	 * is zero, the internal buffer will be deleted.
	 */
	public void trim() {
		if(out==null) {
			return;
		}

		if(count==0) {
			out = null;
		} else {
			out = Arrays.copyOf(out, count);
		}
	}

	public Edge getIn() {
		return in;
	}

	public Edge[] getOut() {
		return out;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
