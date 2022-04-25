/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.engine;

import static de.ims.icarus2.model.api.ModelTestUtils.assertTreeProperties;
import static de.ims.icarus2.util.lang.Primitives._int;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.util.tree.Tree;

/**
 * @author Markus Gärtner
 *
 */
class TreeStructureTest {

	/**
	 * Tests for  {@link TreeStructure#parseTree(String, de.ims.icarus2.model.manifest.api.StructureManifest)}
	 */
	@Nested
	class ForParseTree {
		//TODO
	}

	/**
	 * Tests for  {@link TreeStructure#convertTree(de.ims.icarus2.model.manifest.api.StructureManifest, de.ims.icarus2.util.tree.Tree)}
	 */
	@Nested
	class ForConvertTree {

		private StructureManifest manifest;
		private Container host;

		@BeforeEach
		void setUp() {
			manifest = mock(StructureManifest.class);
			host = mock(Container.class);
		}

		@AfterEach
		void tearDown() {
			manifest = null;
			host = null;
		}

		@Test
		public void testNonUniqueIndexPayload() throws Exception {
			Tree<Integer> tree = Tree.newNode(_int(0));
			tree.newChild(_int(0));

			assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> TreeStructure.convertTree(manifest, tree, host));
		}

		@Test
		public void testSingleNode() throws Exception {
			Tree<Integer> tree = Tree.newNode(_int(0));
			Structure structure = TreeStructure.convertTree(manifest, tree, host);

			assertThat(structure.getItemCount()).isEqualTo(1);
			assertThat(structure.getEdgeCount()).isEqualTo(1); // root -> node

			Item node = structure.getFirstItem();
			assertTreeProperties(structure, node, 1, 0, 0, 1, 0);
		}

		@Test
		public void testDualNode() throws Exception {
			Tree<Integer> tree = Tree.newNode(_int(0));
			tree.newChild(_int(1));
			Structure structure = TreeStructure.convertTree(manifest, tree, host);

			assertThat(structure.getItemCount()).isEqualTo(2);
			assertThat(structure.getEdgeCount()).isEqualTo(2);

			assertTreeProperties(structure, structure.getVirtualRoot(), 0, 1, 2, 0, 2);

			Item node1 = structure.getFirstItem();
			assertTreeProperties(structure, node1, 1, 1, 1, 1, 1);

			Item node2 = structure.getLastItem();
			assertThat(node1).isNotSameAs(node2);
			assertTreeProperties(structure, node2, 1, 0, 0, 2, 0);
		}

		/**
		 * Same as {@link #testDualNode()} but we provide the nodes in reverse order
		 * of their appearance in the final node list.
		 */
		@Test
		public void testDualNodeReverse() throws Exception {
			Tree<Integer> tree = Tree.newNode(_int(1));
			tree.newChild(_int(0));
			Structure structure = TreeStructure.convertTree(manifest, tree, host);

			assertThat(structure.getItemCount()).isEqualTo(2);
			assertThat(structure.getEdgeCount()).isEqualTo(2);

			assertTreeProperties(structure, structure.getVirtualRoot(), 0, 1, 2, 0, 2);

			Item node1 = structure.getLastItem();
			assertTreeProperties(structure, node1, 1, 1, 1, 1, 1);

			Item node2 = structure.getFirstItem();
			assertThat(node1).isNotSameAs(node2);
			assertTreeProperties(structure, node2, 1, 0, 0, 2, 0);
		}

		@ValueSource(ints = {2, 3, 5, 10, 100})
		@ParameterizedTest
		public void testChain(int length) throws Exception {
			Tree<Integer> tree = Tree.newNode(_int(0));
			for(int i=1; i<length; i++) {
				tree = tree.newChild(_int(i));
			}
			tree = tree.root();
			Structure structure = TreeStructure.convertTree(manifest, tree, host);

			assertThat(structure.getItemCount()).isEqualTo(length);
			assertThat(structure.getEdgeCount()).isEqualTo(length);

			assertTreeProperties(structure, structure.getVirtualRoot(), 0, 1, length, 0, length);

			final int last = length-1;
			for (int i = 0; i < length; i++) {
				Item node = structure.getItemAt(i);
				assertTreeProperties("node_"+i, structure, node, 1, i==last ? 0 : 1, length-i-1, i+1, length-i-1);
			}
		}

		@Test
		public void testBalanced3NodeTree() throws Exception {
			Tree<Integer> tree = Tree.newNode(_int(0));
			tree.newChild(_int(1));
			tree.newChild(_int(2));
			Structure structure = TreeStructure.convertTree(manifest, tree, host);

			assertThat(structure.getItemCount()).isEqualTo(3);
			assertThat(structure.getEdgeCount()).isEqualTo(3);

			assertTreeProperties(structure, structure.getVirtualRoot(), 0, 1, 2, 0, 3);

			Item root = structure.getFirstItem();
			assertTreeProperties(structure, root, 1, 2, 1, 1, 2);

			Item node1 = structure.getItemAt(1);
			assertThat(root).isNotSameAs(node1);
			assertTreeProperties(structure, node1, 1, 0, 0, 2, 0);

			Item node2 = structure.getItemAt(2);
			assertThat(root).isNotSameAs(node2);
			assertThat(node1).isNotSameAs(node2);
			assertTreeProperties(structure, node2, 1, 0, 0, 2, 0);
		}
	}
}
