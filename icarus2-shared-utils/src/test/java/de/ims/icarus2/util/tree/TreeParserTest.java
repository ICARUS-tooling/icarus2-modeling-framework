/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import de.ims.icarus2.util.strings.BracketStyle;

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
			assertTreeEquals(Tree.newRoot(), parser.parseTree(input));
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
			assertTreeEquals(Tree.<String>newRoot().setData("x"), parser.parseTree(input));
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
			Tree<String> root = Tree.newRoot();
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
			Tree<String> root = Tree.<String>newRoot().setData("x");
			root.newChild().setData("y");
			assertTreeEquals(root, parser.parseTree(input));
		}

		@Test
		void withSiblings() {
			Tree<String> root = Tree.newRoot();
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
			Tree<String> root = Tree.<String>newRoot().setData("x");
			root.newChild().setData("y");
			root.newChild().setData("z");
			assertTreeEquals(root, parser.parseTree(input));
		}
	}

}
