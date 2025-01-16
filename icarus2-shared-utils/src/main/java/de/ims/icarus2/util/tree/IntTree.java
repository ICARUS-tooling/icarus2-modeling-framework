/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
 * Models a tree interface that is limited to integer indices and
 * provides quick index-based access to structural data.
 * Note that this tree model is designed for one-directional
 * traversal only, as it does not provide a way to directly obtain
 * the parent node for a given child node.
 *
 * @author Markus Gärtner
 *
 */
public interface IntTree {

	/** Special reserved node id for the virtual root of a tree */
	public static final int ROOT = -2;

	/** Total number of nodes in tree */
	int size();
	/** Number of child nodes for designated node */
	int size(int nodeId);
	/** Path length to deepest nested child of designated node */
	int height(int nodeId);
	/** Path length to root of designated node */
	int depth(int nodeId);
	/** Node id of child at index for designated node */
	int childAt(int nodeId, int index);
}
