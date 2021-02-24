/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.util.tree;

/**
 * Models trees as a list of nodes and the structure as lists of pointers from
 * parent node to the child nodes' positions in the original list of nodes;
 *
 * @author Markus Gärtner
 *
 */
public interface TreeIndex {

	/** The total number of nodes. */
	int nodeCount();

	/** Number of children for a given node. */
	int childCount(int node);

	/** Child of node at specified index. */
	int childAt(int node, int index);
}
