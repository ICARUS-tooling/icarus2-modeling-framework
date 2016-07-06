/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
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
