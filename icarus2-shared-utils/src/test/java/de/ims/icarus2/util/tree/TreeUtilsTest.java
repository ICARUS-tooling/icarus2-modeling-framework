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
