/**
 *
 */
package de.ims.icarus2.util.tree;

import static de.ims.icarus2.test.TestUtils.assertNPE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
@SuppressWarnings("rawtypes")
class TreeParserTest implements ApiGuardedTest<TreeParser> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends TreeParser> getTestTargetClass() {
		return TreeParser.class;
	}
	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public TreeParser<?> createTestInstance(TestSettings settings) {
		return settings.process(TreeParser.forStringPayload(BracketStyle.SQUARE));
	}

	@Test
	void whenNullInput() {
		assertNPE(() -> TreeParser.forStringPayload(BracketStyle.SQUARE).parseTree(null));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"",
			" ",
			"\t",
			"\n",
			"\r",
	})
	void whenEmptyInput(String input) {
		assertThrows(IllegalArgumentException.class,
				() -> TreeParser.forStringPayload(BracketStyle.SQUARE).parseTree(input));
	}

	private static void assertTreeEquals(Tree<String> expected,
			Tree<String> actual) {
		assertTrue(TreeUtils.equals(expected, actual), () ->
				String.format("Expected %s - got %s",
						TreeUtils.toString(expected),
						TreeUtils.toString(actual)));
	}

	/**
	 * Test methods for {@link de.ims.icarus2.util.tree.TreeParser#parseTree(java.lang.String)}.
	 *
	 * All tests here use the square bracket style!
	 */
	@Nested
	class TestParse {

		private TreeParser<String> parser;

		@BeforeEach
		void setUp() {
			parser = TreeParser.forStringPayload(BracketStyle.SQUARE);
		}

		@AfterEach
		void tearDown() {
			parser = null;
		}

		@ParameterizedTest
		@ValueSource(strings = {
				"[]",
				" []",
				"[] ",
				"[ ]",
				"[\n]",
		})
		void whenEmpty(String input) {
			assertTreeEquals(Tree.root(), parser.parseTree(input));
		}

		@ParameterizedTest
		@ValueSource(strings = {
				"[x]",
				" [x]",
				"[x] ",
				"[ x]",
				"[x ]",
				"[\nx ]",
		})
		void withPayload(String input) {
			assertTreeEquals(Tree.<String>root().setData("x"), parser.parseTree(input));
		}

		@ParameterizedTest
		@ValueSource(strings = {
				"[[]]",
				"[ []]",
				"[[] ]",
				" [ []]",
				"[[\n]]",
				"[\t[]]",
				"[[  ]]",
				"[[]\n]",
				" [\n \t[\n\t] \n]",
		})
		void whenNested(String input) {
			Tree<String> root = Tree.root();
			root.newChild();
			assertTreeEquals(root, parser.parseTree(input));
		}

		@ParameterizedTest
		@ValueSource(strings = {
				"[x[y]]",
				"[ x[y]]",
				"[x [y]]",
				"[x[ y]]",
				"[x[y ]]",
				"[ x\n\t[\n\ty\n\t]]",
		})
		void whenNestedWithPayload(String input) {
			Tree<String> root = Tree.<String>root().setData("x");
			root.newChild().setData("y");
			assertTreeEquals(root, parser.parseTree(input));
		}

		@Test
		void withSiblings() {
			Tree<String> root = Tree.root();
			root.newChild();
			root.newChild();
			assertTreeEquals(root, parser.parseTree("[[][]]"));
		}

		@ParameterizedTest
		@ValueSource(strings = {
				"[x[y][z]]",
				"[ x[y][z]]",
				"[x [y][z]]",
				"[x[ y][z]]",
				"[x[y ][z]]",
				"[x[y][ z]]",
				"[x[y][z ]]",
				"[\nx\n\t[\n\ty\n\t]\n\t[z]]",
		})
		void withSiblingsAndPayload(String input) {
			Tree<String> root = Tree.<String>root().setData("x");
			root.newChild().setData("y");
			root.newChild().setData("z");
			assertTreeEquals(root, parser.parseTree(input));
		}
	}

}
