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
package de.ims.icarus2.util.tree;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.lang.Primitives._int;
import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;
import java.util.function.Function;

import de.ims.icarus2.util.MutablePrimitives.MutableInteger;
import de.ims.icarus2.util.strings.BracketStyle;

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

		final Tree<T> root = Tree.newRoot();

		final StringBuilder buffer = new StringBuilder();
		Tree<T> current = root;
		int depth = 0;

		final Consumer<Tree<T>> handlePayload = (tree) -> {
			/*
			 *  Payload can only occur before first child, so any
			 *  node with children cannot receive any payload data anymore
			 */
			if(tree.isChildless()) {
				String payload = buffer.toString().trim();
				tree.setData(payloadParser.apply(payload));
			}
			// Always reset buffer
			buffer.setLength(0);
		};

		boolean escaped = false;

		for(int i=0; i<input.length(); i++) {
			char c = input.charAt(i);

			// Everything escaped goes through unparsed
			if(escaped) {
				escaped = false;
				buffer.append(c);
				continue;
			}
			// Escaping will prevent the following symbol from being parsed
			if(c=='\\') {
				escaped = true;
				continue;
			}

			// Start of a new node declaration
			if(c==bracketStyle.openingBracket) {
				assert current!=null;
				handlePayload.accept(current);
				if(depth>0) {
					current = current.newChild();
				}
				depth++;
			}
			// End of current node declaration
			else if(c==bracketStyle.closingBracket) {
				assert current!=null;
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
