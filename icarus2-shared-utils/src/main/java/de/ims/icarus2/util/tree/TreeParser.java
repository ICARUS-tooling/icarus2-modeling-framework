/**
 *
 */
package de.ims.icarus2.util.tree;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.lang.Primitives._int;
import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;
import java.util.function.Function;

import de.ims.icarus2.util.MutablePrimitives.MutableInteger;

/**
 * @author Markus Gärtner
 *
 */
public class TreeParser<T> {

	public static TreeParser<String> forStringPayload(BracketStyle bracketStyle) {
		return new TreeParser<>(s -> s.isEmpty() ? null : s, bracketStyle);
	}

	public static TreeParser<Integer> withAutomaticNumbering(BracketStyle bracketStyle) {
		final MutableInteger counter = new MutableInteger(1);

		return new TreeParser<>(s -> _int(counter.getAndIncrement()), bracketStyle);
	}

	private final Function<String, T> payloadParser;
	private final BracketStyle bracketStyle;

	public TreeParser(Function<String, T> payloadParser,
			BracketStyle bracketStyle) {
		this.payloadParser = requireNonNull(payloadParser);
		this.bracketStyle = requireNonNull(bracketStyle);
	}

	/**
	 * Parses the tree encoded in the specified string by using the
	 * {@link BracketStyle} set for this parser. Any textual payload within
	 * a tree node will be delegated to the payload parser defined at
	 * construction time.
	 * <p>
	 * Note that textual payload must appear inside a node <b>before</b> any
	 * of its child nodes start!
	 *
	 * @param input
	 * @return
	 */
	public Tree<T> parseTree(String input) {
		requireNonNull(input);

		// Make sure we don't have to deal with some whitespace artifacts
		input = input.trim();

		checkArgument("Input string must not be empty", !input.isEmpty());

		final Tree<T> root = Tree.root();

		final StringBuilder buffer = new StringBuilder();
		Tree<T> current = root;
		int depth = 0;

		final Consumer<Tree<T>> handlePayload = (tree) -> {
			/*
			 *  Payload can also only occur before first child, so any
			 *  node with children cannot receive any payload data anymore
			 */
			if(tree.isEmpty()) {
				String payload = buffer.toString().trim();
				tree.setData(payloadParser.apply(payload));
			}
			// Always reset buffer
			buffer.setLength(0);
		};

		for(int i=0; i<input.length(); i++) {
			char c = input.charAt(i);

			// Start of a new node declaration
			if(c==bracketStyle.openBracket) {
				handlePayload.accept(current);
				if(depth>0) {
					current = current.newChild();
				}
				depth++;
			}
			// End of current node declaration
			else if(c==bracketStyle.closeBracket) {
				handlePayload.accept(current);
				current = current.parent();
				depth--;
			}
			// Textual payload
			else {
				buffer.append(c);
			}
		}

		return root;
	}
}
