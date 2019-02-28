/**
 *
 */
package de.ims.icarus2.util.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 *  Simple tree class that can store arbitrary user payload.
 *
 * @author Markus Gärtner
 *
 */
public class Tree<T> {

	/**
	 * Creates a new root node.
	 */
	public static <T> Tree<T> root() {
		return new Tree<>(null);
	}

	private final Tree<T> parent;
	private List<Tree<T>> children;

	private T data;

	private Tree(Tree<T> parent) {
		this.parent = parent;
	}

	private Tree<T> self() {
		return this;
	}

	public Tree<T> parent() {
		return parent;
	}

	public Tree<T> childAt(int index) {
		return children.get(index);
	}

	public int childCount() {
		return children==null ? 0 : children.size();
	}

	public boolean isEmpty() {
		return children==null || children.isEmpty();
	}

	public Tree<T> addChild(Tree<T> child) {
		if(children==null) {
			children = new ArrayList<>(4);
		}
		children.add(child);
		return self();
	}

	/**
	 * Creates a new child for the given {@code parent} and directly
	 * {@link Tree#addChild(Tree) adds} it.
	 *
	 * @param parent
	 * @return
	 */
	public Tree<T> newChild() {
		Tree<T> child = new Tree<>(this);
		addChild(child);
		return child;
	}

	public Tree<T> forEachChild(Consumer<? super Tree<T>> action) {
		if(children!=null) {
			children.forEach(action);
		}
		return self();
	}

	public T getData() {
		return data;
	}

	public Tree<T> setData(T data) {
		this.data = data;
		return self();
	}
}
