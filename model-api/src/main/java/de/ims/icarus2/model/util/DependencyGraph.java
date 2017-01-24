/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 */
package de.ims.icarus2.model.util;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.DependencyType;
import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public class DependencyGraph<E extends Object> {

	private final static int TYPE_UNKNOWN = 0;
	private final static int TYPE_CONTEXT = 1;
	private final static int TYPE_GROUP = 2;
	private final static int TYPE_LAYER = 3;

	// CONTEXTS factories

	public static DependencyGraph<Context> contextGraph(Context rootContext, DependencyType...dependencies) {
		requireNonNull(rootContext);

		DependencyGraph<Context> tree = new DependencyGraph<Context>(Context.class, TYPE_CONTEXT, rootContext);

		final Set<Context> visited = new ReferenceOpenHashSet<>();
		final Stack<Context> buffer = new ObjectArrayList<>();

		buffer.push(rootContext);

		while(!buffer.isEmpty()) {
			final Context context = buffer.pop();

			if(visited.contains(context)) {
				continue;
			}



			context.forEachLayer(l -> {
				Context hostContext = l.getContext();
				if(hostContext!=context) {
					buffer.push(hostContext);
				}
			});
		}

		return tree;
	}

	public static DependencyGraph<Context> contextGraph(Corpus corpus, DependencyType...dependencies) {

	}

	// GROUPS factories

	public static DependencyGraph<LayerGroup> groupGraph(LayerGroup rootGroup, DependencyType...dependencies) {

	}

	public static DependencyGraph<LayerGroup> groupGraph(Context context, DependencyType...dependencies) {

	}

	/**
	 * Creates a graph
	 *
	 * @param corpus
	 * @param reverse
	 * @param options
	 * @return
	 */
	public static DependencyGraph<LayerGroup> groupGraph(Corpus corpus, DependencyType...dependencies) {

	}

	// LAYERS factories

	public static DependencyGraph<Layer> layerGraph(Layer rootLayer, DependencyType...dependencies) {

	}

	public static DependencyGraph<Layer> layerGraph(LayerGroup rootGroup, DependencyType...dependencies) {

	}

	public static DependencyGraph<Layer> layerGraph(Context rootContext, DependencyType...dependencies) {

	}

	public static DependencyGraph<Layer> layerGraph(Corpus rootCorpus, DependencyType...dependencies) {

	}

	// UTILITY FILLERS

	private static void collectLayers(Layer source, Consumer<? super Layer> action, Set<DependencyType> dependencies) {
		if(dependencies.contains(DependencyType.STRONG)) {
			source.getBaseLayers().forEachEntry(action);
		}

		if(ModelUtils.isItemLayer(source)) {
			ItemLayer itemLayer = (ItemLayer) source;
			ItemLayer foundationLayer = itemLayer.getFoundationLayer();

			if(foundationLayer!=null && dependencies.contains(DependencyType.FOUNDATION)) {
				action.accept(foundationLayer);
			}

			ItemLayer boundaryLayer = itemLayer.getBoundaryLayer();

			if(boundaryLayer!=null && dependencies.contains(DependencyType.BOUNDARY)) {
				action.accept(boundaryLayer);
			}

			if(ModelUtils.isFragmentLayer(source)) {
				FragmentLayer fragmentLayer = (FragmentLayer) source;
				AnnotationLayer valueLayer = fragmentLayer.getValueLayer();

				if(valueLayer!=null && dependencies.contains(DependencyType.VALUE)) {
					action.accept(valueLayer);
				}
			}
		}
	}

	/**
	 * Fills a graph using the given {@link Function} to generate
	 * children for a {@code node}. If the function returns either
	 * {@code null} or an {@link Collection#isEmpty() empty} collection,
	 * the respective node is assumed to be a leaf node in the graph.
	 * Note that this method checks for every node that it has not yet
	 * had any children stored for it, i.e. if verifies that the generated
	 * graph really is a graph without cycles.
	 *
	 * @param childFunc
	 */
	public static <E extends Object> DependencyGraph<E> genericGraph(Class<E> nodeClass, Supplier<? extends E> roots,
			Function<E, Collection<? extends E>> childFunc) {
		return createGraph(nodeClass, false, TYPE_UNKNOWN, roots, childFunc);
	}

	private static <E extends Object> DependencyGraph<E> createGraph(Class<E> nodeClass, int type,
			Supplier<? extends E> roots, Function<E, Collection<? extends E>> childFunc) {
		DependencyGraph<E> tree = new DependencyGraph<E>(nodeClass, TYPE_UNKNOWN, roots);

		tree.fill(childFunc);

		return tree;
	}

	/**
	 * One of the TYPE_CONTEXT, TYPE_GROUP, TYPE_LAYER constants
	 */
	private final int type;

	private final Class<E> nodeClass;

	private final Set<E> roots = new ReferenceOpenHashSet<>();
	private transient E root;

	private final Map<E, Node<E>> graph = new Reference2ObjectOpenHashMap<>();

	// CONSTRUCTORS

	DependencyGraph(Class<E> nodeClass, int type) {
		requireNonNull(nodeClass);

		this.nodeClass = nodeClass;
		this.type = type;
	}

	DependencyGraph(Class<E> nodeClass, int type, Collection<? extends E> roots) {
		this(nodeClass, type);

		requireNonNull(roots);
		checkArgument(!roots.isEmpty());

		this.roots.addAll(roots);

		checkState("No roots", !this.roots.isEmpty());
	}

	DependencyGraph(Class<E> nodeClass, int type, Supplier<? extends E> roots) {
		this(nodeClass, type);

		requireNonNull(roots);

		CollectionUtils.feedItems(this.roots, roots);

		checkState("No roots", !this.roots.isEmpty());
	}

	DependencyGraph(Class<E> nodeClass, int type, E root) {
		this(nodeClass, type);

		requireNonNull(root);

		roots.add(root);
	}

	// FILL METHODS

	Node<E> node(E content, boolean createIfMissing) {
		Node<E> node = graph.get(content);
		if(node==null && createIfMissing) {
			node = new Node<>(content);
			graph.put(content, node);
		}
		return node;
	}

	void addLink(E from, E to) {
		requireNonNull(from);
		requireNonNull(to);

		node(from, true).addOutgoing(to);
		node(to, true).addIncoming(from);
	}

	/**
	 * Fills this graph using the given {@link Function} to generate
	 * children for a {@code node}. If the function returns either
	 * {@code null} or an {@link Collection#isEmpty() empty} collection,
	 * the respective node is assumed to be a leaf node in the graph.
	 * Note that this method checks for every node that it has not yet
	 * had any children stored for it, i.e. if verifies that the generated
	 * graph really is a graph without cycles.
	 *
	 * @param childFunc
	 */
	void fill(Function<E, Collection<? extends E>> childFunc) {
		ObjectArrayList<E> buffer = new ObjectArrayList<>(roots);

		while(!buffer.isEmpty()) {
			E source = buffer.pop();
			Node<E> node = node(source, true);

			if(node.flagSet(FLAG_VISITED)) {
				continue;
			}

			Collection<? extends E> children = childFunc.apply(source);

			if(children!=null && !children.isEmpty()) {
				for(E target : children) {
					node.addOutgoing(target);
					node(target, true).addIncoming(source);
				}
			}
		}
	}

	// TREE methods

	public boolean hasMultiRoots() {
		return roots.size()>1;
	}

	public E root() {
		checkState("Graph has multiple roots", !hasMultiRoots());

		if(root==null) {
			root = roots.iterator().next();
		}

		return root;
	}

	public Collection<E> roots() {
		return Collections.unmodifiableSet(roots);
	}

	public void forEachRoot(Consumer<? super E> action) {
		requireNonNull(action);

		roots.forEach(action);
	}

	public Collection<E> nodes() {
		return Collections.unmodifiableSet(graph.keySet());
	}

	public void forEachNode(Consumer<? super E> action) {
		requireNonNull(action);

		graph.keySet().forEach(action);
	}

	public int outgoingCount(E content) {
		requireNonNull(content);

		Node<E> n = new Node<DependencyGraph.E>(content)

		Node<E> node = node(content, false);
		return node==null ? 0 : node.;
	}

	public Collection<E> childNodes(E parent) {
		requireNonNull(parent);

		Set<E> children = graph.get(parent);
		if(children==null) {
			children = Collections.emptySet();
		} else {
			children = Collections.unmodifiableSet(children);
		}

		return children;
	}

	public void forEachChild(E parent, Consumer<? super E> action) {
		requireNonNull(parent);
		requireNonNull(action);

		Set<E> children = graph.get(parent);
		if(children!=null) {
			children.forEach(action);
		}
	}

	// TYPE + METADATA methods

	public Class<E> getNodeClass() {
		return nodeClass;
	}

	public boolean isContextGraph() {
		return type==TYPE_CONTEXT;
	}

	public boolean isGroupGraph() {
		return type==TYPE_GROUP;
	}

	public boolean isLayerGraph() {
		return type==TYPE_LAYER;
	}

	private static final int FLAG_VISITED = (1<<0);

	private static class Node<E extends Object> {
		private final E content;

		private Set<E> incoming, outgoing;
		private int flags = 0;

		Node(E content) {
			requireNonNull(content);

			this.content = content;
		}

		private Set<E> incoming(boolean createIfMissing) {
			if(incoming==null && createIfMissing) {
				incoming = new ReferenceOpenHashSet<>();
			}
			return incoming;
		}

		private Set<E> outgoing(boolean createIfMissing) {
			if(outgoing==null && createIfMissing) {
				outgoing = new ReferenceOpenHashSet<>();
			}
			return outgoing;
		}

		boolean flagSet(int flag) {
			return (flags & flag) == flag;
		}

		void setFlag(int flag, boolean active) {
			if(active) {
				flags |= flag;
			} else {
				flags &= ~flag;
			}
		}

		void addIncoming(E incoming) {
			incoming(true).add(incoming);
		}

		void addOutgoing(E outgoing) {
			outgoing(true).add(outgoing);
		}

		void forEachIncoming(Consumer<? super E> action) {
			Set<E> incoming = incoming(false);
			if(incoming!=null) {
				incoming.forEach(action);
			}
		}

		void forEachOutgoing(Consumer<? super E> action) {
			Set<E> outgoing = outgoing(false);
			if(outgoing!=null) {
				outgoing.forEach(action);
			}
		}

		int incomingCount() {
			return incoming==null ? 0 : incoming.size();
		}

		int outgoingCount() {
			return outgoing==null ? 0 : outgoing.size();
		}

		Set<E> outgoing() {
			return outgoing(false);
		}

		Set<E> incoming() {
			return incoming(false);
		}
	}
}
