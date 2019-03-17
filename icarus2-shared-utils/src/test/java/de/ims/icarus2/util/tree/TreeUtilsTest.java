/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static org.junit.jupiter.api.Assertions.assertEquals;

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
				assertEquals("[]", TreeUtils.toString(Tree.root()));
			}

			@Test
			void withPayload() {
				assertEquals("[x]", TreeUtils.toString(
						Tree.<String>root().setData("x")));
			}

			@Test
			void whenNested() {
				Tree<String> root = Tree.root();
				root.newChild();
				assertEquals("[[]]", TreeUtils.toString(root));
			}

			@Test
			void whenDeeplyNested() {
				Tree<String> root = Tree.root();
				root.newChild().newChild();
				assertEquals("[[[]]]", TreeUtils.toString(root));
			}

			@Test
			void whenNestedWithPayload() {
				Tree<String> root = Tree.<String>root().setData("x");
				root.newChild().setData("y");
				assertEquals("[x[y]]", TreeUtils.toString(root));
			}

			@Test
			void asSiblings() {
				Tree<String> root = Tree.root();
				root.newChild();
				root.newChild();
				assertEquals("[[][]]", TreeUtils.toString(root));
			}

			@Test
			void asSiblingsWithPayload() {
				Tree<String> root = Tree.<String>root().setData("x");
				root.newChild().setData("y");
				root.newChild().setData("z");
				assertEquals("[x[y][z]]", TreeUtils.toString(root));
			}
		}
	}
}
