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
package de.ims.icarus2.util.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import de.ims.icarus2.util.strings.BracketStyle;

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
	public static <T> Tree<T> newRoot() {
		return new Tree<>(null);
	}

	/**
	 * Creates a new (potential root) node with the given payload
	 */
	public static <T> Tree<T> newNode(T data) {
		return new Tree<T>(null).setData(data);
	}

	private Tree<T> parent;
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

	public Tree<T> root() {
		Tree<T> root = parent;
		while(root!=null && root.parent!=null) {
			root = root.parent;
		}
		return root;
	}

	public Tree<T> childAt(int index) {
		return children.get(index);
	}

	public int childCount() {
		return children==null ? 0 : children.size();
	}

	public Tree<T> firstChild() {
		return children.get(0);
	}

	public Tree<T> lastChild() {
		return children.get(children.size()-1);
	}

	public boolean isChildless() {
		return children==null || children.isEmpty();
	}

	public Tree<T> addChild(Tree<T> child) {
		if(children==null) {
			children = new ArrayList<>(4);
		}
		children.add(child);
		child.parent = this;
		return self();
	}

	/**
	 * Creates a new child for the given {@code parent} and directly
	 * {@link Tree#addChild(Tree) adds} it.
	 *
	 * @return
	 */
	public Tree<T> newChild() {
		Tree<T> child = new Tree<>(this);
		addChild(child);
		return child;
	}

	public Tree<T> newChild(T data) {
		return newChild().setData(data);
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

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return TreeUtils.toString(this, BracketStyle.SQUARE, String::valueOf);
	}
}
