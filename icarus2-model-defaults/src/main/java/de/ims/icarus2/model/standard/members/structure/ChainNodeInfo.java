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

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.item.Edge;

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
		} else {
			return 0;
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#edgeAt(long, boolean)
	 */
	@Override
	public Edge edgeAt(long index, boolean incoming) {
		if(incoming) {
			if(in==null)
				throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
						"No incoming edge for index: "+index);

			return in;
		} else {
			if(out==null)
				throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
						"No outgoing edge for index: "+index);

			return out;
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#addEdge(de.ims.icarus2.model.api.members.item.Edge, boolean)
	 */
	@Override
	public void addEdge(Edge edge, boolean incoming) {
		if(incoming) {

		} else {

		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfo#removeEdge(de.ims.icarus2.model.api.members.item.Edge, boolean)
	 */
	@Override
	public void removeEdge(Edge edge, boolean incoming) {
		// TODO Auto-generated method stub
	}

	public Edge getIn() {
		return in;
	}

	public Edge getOut() {
		return out;
	}

	public void setIn(Edge in) {
		this.in = in;
	}

	public void setOut(Edge out) {
		this.out = out;
	}

}
