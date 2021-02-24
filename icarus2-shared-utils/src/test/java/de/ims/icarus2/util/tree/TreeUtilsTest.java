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

import static de.ims.icarus2.test.TestUtils.assertListEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
class TreeUtilsTest {

	@Nested
	class TestToString {

		@Nested
		class ForStringTree {

			@Test
			void whenEmpty() {
				assertEquals("[]", TreeUtils.toString(Tree.newRoot()));
			}

			@Test
			void withPayload() {
				assertEquals("[x]", TreeUtils.toString(
						Tree.<String>newRoot().setData("x")));
			}

			@Test
			void whenNested() {
				Tree<String> root = Tree.newRoot();
				root.newChild();
				assertEquals("[[]]", TreeUtils.toString(root));
			}

			@Test
			void whenDeeplyNested() {
				Tree<String> root = Tree.newRoot();
				root.newChild().newChild();
				assertEquals("[[[]]]", TreeUtils.toString(root));
			}

			@Test
			void whenNestedWithPayload() {
				Tree<String> root = Tree.<String>newRoot().setData("x");
				root.newChild().setData("y");
				assertEquals("[x[y]]", TreeUtils.toString(root));
			}

			@Test
			void asSiblings() {
				Tree<String> root = Tree.newRoot();
				root.newChild();
				root.newChild();
				assertEquals("[[][]]", TreeUtils.toString(root));
			}

			@Test
			void asSiblingsWithPayload() {
				Tree<String> root = Tree.<String>newRoot().setData("x");
				root.newChild().setData("y");
				root.newChild().setData("z");
				assertEquals("[x[y][z]]", TreeUtils.toString(root));
			}
		}
	}

	@Nested
	class Traversal {

		@Nested
		class Preorder {

			@Test
			void whenEmpty() {
				Consumer<Tree<String>> action = mock(Consumer.class);
				Tree<String> tree = Tree.newRoot();

				TreeUtils.traversePreOrder(tree, action);

				verify(action).accept(tree);
			}

			@Test
			void withPayloadFlat() {
				List<String> buffer = new ArrayList<>();

				Tree<String> tree = Tree.<String>newRoot().setData("root")
						.newChild("1").parent()
						.newChild("2").parent();

				TreeUtils.traversePreOrder(tree, node -> buffer.add(node.getData()));

				assertListEquals(buffer, "root", "1", "2");
			}

			@Test
			void withPayloadDeep() {
				List<String> buffer = new ArrayList<>();

				Tree<String> tree = Tree.<String>newRoot().setData("root")
						.newChild("1")
							.newChild("1.1").parent()
							.newChild("1.2").parent()
							.parent()
						.newChild("2")
							.newChild("2.1")
								.newChild("2.1.1").parent()
								.newChild("2.1.2").parent()
								.newChild("2.1.3").parent()
								.parent()
							.newChild("2.2").parent()
							.parent();

				TreeUtils.traversePreOrder(tree, node -> buffer.add(node.getData()));

				assertListEquals(buffer, "root", "1", "1.1", "1.2", "2", "2.1", "2.1.1", "2.1.2",
						"2.1.3", "2.2");
			}
		}

		@Nested
		class Postorder {

			@Test
			void whenEmpty() {
				Consumer<Tree<String>> action = mock(Consumer.class);
				Tree<String> tree = Tree.newRoot();

				TreeUtils.traversePostOrder(tree, action);

				verify(action).accept(tree);
			}

			@Test
			void withPayloadFlat() {
				List<String> buffer = new ArrayList<>();

				Tree<String> tree = Tree.<String>newRoot().setData("root")
						.newChild("1").parent()
						.newChild("2").parent();

				TreeUtils.traversePostOrder(tree, node -> buffer.add(node.getData()));

				assertListEquals(buffer, "1", "2", "root");
			}

			@Test
			void withPayloadDeep() {
				List<String> buffer = new ArrayList<>();

				Tree<String> tree = Tree.<String>newRoot().setData("root")
						.newChild("1")
							.newChild("1.1").parent()
							.newChild("1.2").parent()
							.parent()
						.newChild("2")
							.newChild("2.1")
								.newChild("2.1.1").parent()
								.newChild("2.1.2").parent()
								.newChild("2.1.3").parent()
								.parent()
							.newChild("2.2").parent()
							.parent();

				TreeUtils.traversePostOrder(tree, node -> buffer.add(node.getData()));

				assertListEquals(buffer, "1.1", "1.2", "1", "2.1.1", "2.1.2",
						"2.1.3", "2.1", "2.2", "2", "root");
			}
		}
	}
}
