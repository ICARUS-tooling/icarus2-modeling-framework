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

import static java.util.Objects.requireNonNull;
import static org.mockito.Mockito.mock;

import java.util.function.Function;
import java.util.function.Supplier;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.standard.members.FixedLayerMemberFactory;
import de.ims.icarus2.model.standard.members.structure.RootItem;
import de.ims.icarus2.model.standard.members.structure.builder.StructureBuilder;
import de.ims.icarus2.model.standard.members.structure.builder.StructureBuilder.SortType;
import de.ims.icarus2.util.strings.BracketStyle;
import de.ims.icarus2.util.tree.Tree;
import de.ims.icarus2.util.tree.TreeParser;
import de.ims.icarus2.util.tree.TreeUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

/**
 * Provides a collection of predefined tree mocks that can be used for
 * testing.
 *
 * @author Markus Gärtner
 *
 */
public enum TreeStructure implements Function<Container,Structure> {

	EMPTY,
	SINGLETON("[]", true),
	CHAIN_2("[[]]", true),
	CHAIN_3("[[[]]]", true),
	CHAIN_5("[[[[[]]]]]", true),
	CHAIN_10("[[[[[[[[[[]]]]]]]]]]", true),
	/**{@code *00 } */
	TREE_3_BALANCED("[[][]]", true),
	/**{@code *000 } */
	TREE_4_BALANCED("[[][][]]", true),
	/**{@code *0000 } */
	TREE_5_BALANCED("[[][][][]]", true),

	//TODO
	;

	private static final StructureManifest DEFAULT_MANIFEST = mock(StructureManifest.class);

	private final Tree<Integer> tree;
	private final StructureManifest manifest = null;

	private TreeStructure() {
		tree = Tree.newRoot();
	}

	private TreeStructure(Supplier<Tree<Integer>> source) {
		tree = source.get();
	}

	private TreeStructure(String s, boolean createIndices) {
		TreeParser<Integer> parser = createIndices ? TreeParser.withAutomaticNumbering(BracketStyle.SQUARE)
				: TreeParser.forIntegerPayload(BracketStyle.SQUARE);
		tree = parser.parseTree(s);
	}

	private StructureManifest manifest() {
		return manifest==null ? DEFAULT_MANIFEST : manifest;
	}

	/**
	 * @see java.util.function.Function#apply(java.lang.Object)
	 */
	@Override
	public Structure apply(Container host) {
		return convertTree(manifest(), tree, host);
	}

	public static Structure parseTree(String s, StructureManifest m, Container host) {
		Tree<Integer> t = TreeParser.withAutomaticNumbering(BracketStyle.SQUARE).parseTree(s);
		return convertTree(m, t, host);
	}

	/**
	 * Converts a tree with numerical payload data into a tree structure. Each node in the
	 * source tree must have a unique id that defines the actual positional index of the
	 * node itself, within the list of "tokens" in the generated structure.
	 */
	public static Structure convertTree(StructureManifest m, Tree<Integer> t, Container host) {
		StructureBuilder sb = StructureBuilder.builder(m);
		sb.createRoot();
		Reference2ObjectMap<Tree<Integer>, Item> lookup = new Reference2ObjectOpenHashMap<>();
		LongSet usedIds = new LongOpenHashSet();

		sb.augmented(false);
		sb.sortingNodes(SortType.NATURAL);
		sb.memberFactory(FixedLayerMemberFactory.INSTANCE);
		sb.host(host);

		TreeUtils.traversePreOrder(t, node -> {
			long index = node.getData().longValue();
			if(!usedIds.add(index))
				throw new IllegalArgumentException("Duplicate node id: "+index);

			Item child = sb.newNode(index);
			sb.addNode(child);

			lookup.put(node, child);
			Edge edge;
			if(node.parent()==null) {
				RootItem<Edge> parent = sb.getRoot();
				edge = sb.newEdge(index, parent, child);
				parent.addEdge(edge);
			} else {
				edge = sb.newEdge(index, requireNonNull(lookup.get(node.parent())), child);
			}
			sb.addEdge(edge);
		});

		return sb.build();
	}
}
