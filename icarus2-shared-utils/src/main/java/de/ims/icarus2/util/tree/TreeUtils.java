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
/**
 *
 */
package de.ims.icarus2.util.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import de.ims.icarus2.util.strings.BracketStyle;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
public class TreeUtils {

	/**
	 * Performs a pre-order traversial of the given {@code tree}. This implementation
	 * does not use recursion and therefore is suitable for very large trees.
	 *
	 * @param tree
	 * @param action
	 */
	public static <T> void traversePreOrder(Tree<T> tree, Consumer<? super Tree<T>> action) {
		Stack<Tree<T>> stack = new ObjectArrayList<>();

		stack.push(tree);

		while(!stack.isEmpty()) {
			Tree<T> node = stack.pop();
			action.accept(node);

			if(!node.isChildless()) {
				for (int i = node.childCount()-1; i >= 0 ; i--) {
					stack.push(node.childAt(i));
				}
			}
		}
	}

	/**
	 * Performs a post-order traversal of the given {@code tree}. This implementation
	 * does not use recursion and therefore is suitable for very large trees.
	 *
	 * @param tree
	 * @param action
	 */
	public static <T> void traversePostOrder(Tree<T> tree, Consumer<? super Tree<T>> action) {
		Stack<Tree<T>> stack = new ObjectArrayList<>();

		for (int i = tree.childCount()-1; i >= 0 ; i--) {
			stack.push(tree.childAt(i));
		}

		Tree<T> last = null;

		while(!stack.isEmpty()) {
			Tree<T> node = stack.pop();

			if(node.isChildless()) {
				// Leaf detected
				action.accept(node);
				last = node;
			} else if(last!=null && last==node.lastChild()) {
				// All children processed
				action.accept(node);
				last = node;
			} else {
				stack.push(node);
				for (int i = node.childCount()-1; i >= 0 ; i--) {
					stack.push(node.childAt(i));
				}
				last = null;
			}
		}

		// Finally apply action on the root
		action.accept(tree);
	}

	/**
	 * Traverses the given tree, applying the specified {@code visitor} to each node.
	 * This implementation uses no recursion and therefore is suitable for very large trees.
	 * Note however that there is no guarantee on the roder in which nodes will be visited!
	 *
	 * @param tree
	 * @param visitor
	 */
	public static <T> void traverse(Tree<T> tree, Consumer<Tree<T>> action) {
		Stack<Tree<T>> stack = new ObjectArrayList<>();
		stack.push(tree);

		while(!stack.isEmpty()) {
			Tree<T> node = stack.pop();
			action.accept(node);

			if(!node.isChildless()) {
				node.forEachChild(stack::push);
			}
		}
	}

	/**
	 * Traverses the given tree, applying the specified {@code visitor} to each node.
	 * This implementation uses recursion and therefore should not be used with large trees.
	 *
	 * @param tree
	 * @param visitor
	 */
	public static <T> void traverse(Tree<T> tree, TreeVisitor<T> visitor) {
		visitor.enter(tree);
		tree.forEachChild(c -> traverse(c, visitor));
		visitor.exit(tree);
	}

	/**
	 * Traverses the given tree, applying the specified {@code actions} to each node.
	 * This implementation uses recursion and therefore should not be used with large trees.
	 *
	 * @param tree
	 * @param onEnter
	 * @param onExit
	 */
	public static <T> void traverse(Tree<T> tree, Consumer<? super Tree<T>> onEnter,
			Consumer<? super Tree<T>> onExit) {
		onEnter.accept(tree);
		tree.forEachChild(c -> traverse(c, onEnter, onExit));
		onExit.accept(tree);
	}

	/**
	 *
	 * @param tree
	 * @param action
	 *
	 * @see #traversePreOrder(Tree, Consumer)
	 */
	public static <T> void processPayloadPreOrder(Tree<T> tree, Consumer<? super T> action) {
		traversePreOrder(tree, node -> action.accept(node.getData()));
	}

	/**
	 *
	 * @param tree
	 * @param action
	 *
	 * @see #traversePostOrder(Tree, Consumer)
	 */
	public static <T> void processPayloadPostOrder(Tree<T> tree, Consumer<? super T> action) {
		traversePostOrder(tree, node -> action.accept(node.getData()));
	}

	/**
	 *
	 * @param tree
	 * @return
	 *
	 * @see #processPayloadPreOrder(Tree, Consumer)
	 */
	public static <T> List<T> collectPayloadPreOrder(Tree<T> tree) {
		List<T> result = new ArrayList<>();
		processPayloadPreOrder(tree, result::add);
		return result;
	}

	/**
	 *
	 * @param tree
	 * @return
	 *
	 * @see #processPayloadPostOrder(Tree, Consumer)
	 */
	public static <T> List<T> collectPayloadPostOrder(Tree<T> tree) {
		List<T> result = new ArrayList<>();
		processPayloadPostOrder(tree, result::add);
		return result;
	}

	/**
	 * TUrns the given tree into a {@code String}, using recursive traversal.
	 *
	 * @param tree
	 * @param bracketStyle
	 * @param labelGen
	 * @return
	 */
	public static <T> String toString(Tree<T> tree, BracketStyle bracketStyle,
			Function<T, String> labelGen) {
		StringBuilder sb = new StringBuilder();
		traverse(tree,
				t -> sb.append(bracketStyle.openingBracket)
					.append(Optional.ofNullable(
						labelGen.apply(t.getData()))
						.orElse("")),
				t -> sb.append(bracketStyle.closingBracket));
		return sb.toString();
	}

	/**
	 *
	 * @param tree
	 * @return
	 *
	 * @see #toString(Tree, BracketStyle, Function)
	 */
	public static String toString(Tree<String> tree) {
		return toString(tree, BracketStyle.SQUARE, s -> s);
	}

	/**
	 * Compares two trees using recursive traversal.
	 *
	 * @param tree1
	 * @param tree2
	 * @return
	 */
	public static <T> boolean equals(Tree<? extends T> tree1, Tree<? extends T> tree2) {
		if(tree1.childCount()!=tree2.childCount()) {
			return false;
		}

		if(!Objects.equals(tree1.getData(), tree2.getData())) {
			return false;
		}

		for(int i=0; i<tree1.childCount(); i++) {
			if(!equals(tree1.childAt(i), tree2.childAt(i))) {
				return false;
			}
		}

		return true;
	}
}
