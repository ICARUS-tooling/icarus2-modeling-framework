/**
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

import de.ims.icarus2.model.api.members.item.Edge;

/**
 * @author Markus Gärtner
 *
 */
public interface NodeInfo {

	long edgeCount();

	long edgeCount(boolean incoming);

	Edge edgeAt(long index, boolean incoming);

	void addEdge(Edge edge, boolean incoming);

	void removeEdge(Edge edge, boolean incoming);

	Type getType();

	public enum Type {
		/** Only outgoing edges */
		ROOT,
		/** Up to 1 incoming and outgoing edge each */
		CHAIN,
		/** Up to 1 incoming edge and arbitrary number of outgoing edges */
		TREE,
		/** No limits whatsoever */
		GRAPH,
		/** Exactly one incoming edge */
		LEAF,
		;
	}
}
