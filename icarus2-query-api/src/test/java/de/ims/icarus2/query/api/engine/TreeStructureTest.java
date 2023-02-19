/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.model.api.ModelAssertions.assertThat;
import static de.ims.icarus2.util.lang.Primitives._int;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.model.api.members.container.Container;
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

			assertThat(structure)
				.hasItemCountOf(1)
				.hasEdgeCountOf(1)
				.firstNode()
				.isRoot()
				.isLeaf()
				.hasDepth(1)
				.hasHeight(0);
		}

		@Test
		public void testDualNode() throws Exception {
			Tree<Integer> tree = Tree.newNode(_int(0));
			tree.newChild(_int(1));
			Structure structure = TreeStructure.convertTree(manifest, tree, host);

			assertThat(structure)
				.hasItemCountOf(2)
				.hasEdgeCountOf(2)
				// Root asserts
				.root()
				.isVirtualRoot()
				.hasIncomingEdgeCount(0)
				.hasOutgoingEdgeCount(1)
				.hasHeight(2)
				.hasDepth(0)
				.hasDescendantCount(2)
				// First node
				.structure()
				.firstNode()
				.isRoot()
				.hasIncomingEdgeCount(1)
				.hasOutgoingEdgeCount(1)
				.hasHeight(1)
				.hasDepth(1)
				.hasDescendantCount(1)
				// Second node
				.structure()
				.lastNode()
				.isLeaf()
				.hasIncomingEdgeCount(1)
				.hasOutgoingEdgeCount(0)
				.hasHeight(0)
				.hasDepth(2)
				.hasDescendantCount(0);
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

			assertThat(structure)
				.hasItemCountOf(2)
				.hasEdgeCountOf(2)
				// Root asserts
				.root()
				.isVirtualRoot()
				.hasIncomingEdgeCount(0)
				.hasOutgoingEdgeCount(1)
				.hasHeight(2)
				.hasDepth(0)
				.hasDescendantCount(2)
				// First node
				.structure()
				.firstNode()
				.isLeaf()
				.hasIncomingEdgeCount(1)
				.hasOutgoingEdgeCount(0)
				.hasHeight(0)
				.hasDepth(2)
				.hasDescendantCount(0)
				// Second node
				.structure()
				.lastNode()
				.isRoot()
				.hasIncomingEdgeCount(1)
				.hasOutgoingEdgeCount(1)
				.hasHeight(1)
				.hasDepth(1)
				.hasDescendantCount(1);
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

			assertThat(structure)
				.hasItemCountOf(length)
				.hasEdgeCountOf(length)
				.root()
				.isVirtualRoot()
				.hasIncomingEdgeCount(0)
				.hasOutgoingEdgeCount(1)
				.hasHeight(length)
				.hasDepth(0)
				.hasDescendantCount(length);

			final int last = length-1;
			for (int i = 0; i < length; i++) {
				assertThat(structure)
					.node(i)
					.as("node_"+i)
					.hasIncomingEdgeCount(1)
					.hasOutgoingEdgeCount(i==last ? 0 : 1)
					.hasHeight(length-i-1)
					.hasDepth(i+1)
					.hasDescendantCount(length-i-1);
			}
		}

		@Test
		public void testBalanced3NodeTree() throws Exception {
			Tree<Integer> tree = Tree.newNode(_int(0));
			tree.newChild(_int(1));
			tree.newChild(_int(2));
			Structure structure = TreeStructure.convertTree(manifest, tree, host);

			assertThat(structure)
				.hasItemCountOf(3)
				.hasEdgeCountOf(3)
				.root()
				.isVirtualRoot()
				.hasIncomingEdgeCount(0)
				.hasOutgoingEdgeCount(1)
				.hasHeight(2)
				.hasDepth(0)
				.hasDescendantCount(3);

			// Root
			assertThat(structure)
				.firstNode()
				.isRoot()
				.hasIncomingEdgeCount(1)
				.hasOutgoingEdgeCount(2)
				.hasHeight(1)
				.hasDepth(1)
				.hasDescendantCount(2);

			// Left
			assertThat(structure)
				.node(1)
				.isLeaf()
				.hasIncomingEdgeCount(1)
				.hasOutgoingEdgeCount(0)
				.hasHeight(0)
				.hasDepth(2)
				.hasDescendantCount(0);

			// Right
			assertThat(structure)
				.node(2)
				.isLeaf()
				.hasIncomingEdgeCount(1)
				.hasOutgoingEdgeCount(0)
				.hasHeight(0)
				.hasDepth(2)
				.hasDescendantCount(0);
		}
	}
}
