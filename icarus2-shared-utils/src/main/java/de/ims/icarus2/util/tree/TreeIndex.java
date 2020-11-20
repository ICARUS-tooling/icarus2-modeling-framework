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
