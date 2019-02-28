/**
 *
 */
package de.ims.icarus2.util.tree;

/**
 * @author Markus Gärtner
 *
 */
public interface TreeVisitor<T> {

	void enter(Tree<T> tree);
	void exit(Tree<T> tree);
}
