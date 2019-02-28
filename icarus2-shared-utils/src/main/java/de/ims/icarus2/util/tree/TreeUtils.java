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

/**
 * @author Markus Gärtner
 *
 */
public class TreeUtils {

	public static <T> void traversePreOrder(Tree<T> tree, Consumer<? super Tree<T>> action) {
		action.accept(tree);
		tree.forEachChild(c -> traversePreOrder(c, action));
	}

	public static <T> void traversePostOrder(Tree<T> tree, Consumer<? super Tree<T>> action) {
		tree.forEachChild(c -> traversePostOrder(c, action));
		action.accept(tree);
	}

	public static <T> void traverse(Tree<T> tree, TreeVisitor<T> visitor) {
		visitor.enter(tree);
		tree.forEachChild(c -> traverse(c, visitor));
		visitor.exit(tree);
	}

	public static <T> void traverse(Tree<T> tree, Consumer<? super Tree<T>> onEnter,
			Consumer<? super Tree<T>> onExit) {
		onEnter.accept(tree);
		tree.forEachChild(c -> traverse(c, onEnter, onExit));
		onExit.accept(tree);
	}

	public static <T> void processPayloadPreOrder(Tree<T> tree, Consumer<? super T> action) {
		action.accept(tree.getData());
		tree.forEachChild(c -> processPayloadPreOrder(c, action));
	}

	public static <T> void processPayloadPostOrder(Tree<T> tree, Consumer<? super T> action) {
		tree.forEachChild(c -> processPayloadPostOrder(c, action));
		action.accept(tree.getData());
	}

	public static <T> List<T> collectPayloadPreOrder(Tree<T> tree) {
		List<T> result = new ArrayList<>();
		processPayloadPreOrder(tree, result::add);
		return result;
	}

	public static <T> List<T> collectPayloadPostOrder(Tree<T> tree) {
		List<T> result = new ArrayList<>();
		processPayloadPostOrder(tree, result::add);
		return result;
	}

	public static <T> String toString(Tree<T> tree, BracketStyle bracketStyle,
			Function<T, String> labelGen) {
		StringBuilder sb = new StringBuilder();
		traverse(tree,
				t -> sb.append(bracketStyle.openBracket)
					.append(Optional.ofNullable(
						labelGen.apply(t.getData()))
						.orElse("")),
				t -> sb.append(bracketStyle.closeBracket));
		return sb.toString();
	}

	public static String toString(Tree<String> tree) {
		return toString(tree, BracketStyle.SQUARE, s -> s);
	}

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
