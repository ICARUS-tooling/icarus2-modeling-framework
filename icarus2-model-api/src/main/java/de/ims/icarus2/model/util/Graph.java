/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.util;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
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
import de.ims.icarus2.util.collections.LazyCollection;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

/**
 * @author Markus Gärtner
 *
 */
public class Graph<E extends Object> {

	private final static int TYPE_UNKNOWN = 0;
	private final static int TYPE_CONTEXT = 1;
	private final static int TYPE_GROUP = 2;
	private final static int TYPE_LAYER = 3;

	private static final Predicate<Object> ACCEPT_ALL = o -> true;
	@SuppressWarnings("unchecked")
	public static <E extends Object> Predicate<E> acceptAll() {
		return (Predicate<E>) ACCEPT_ALL;
	}

	private static final Predicate<Object> ACCEPT_NONE = o -> true;
	@SuppressWarnings("unchecked")
	public static <E extends Object> Predicate<E> acceptNone() {
		return (Predicate<E>) ACCEPT_NONE;
	}

	public static <E extends Object> Predicate<E> acceptRoots(Graph<E> graph) {
		return o -> graph.incomingCount(o)==0;
	}

	public static <E extends Object> Predicate<E> acceptLeafs(Graph<E> graph) {
		return o -> graph.outgoingCount(o)==0;
	}

	public static Predicate<LayerGroup> groupsForContext(Context context)  {
		requireNonNull(context);

		return group -> group.getContext()==context;
	}

	public static Predicate<Layer> layersForContext(Context context)  {
		requireNonNull(context);

		return layer -> layer.getContext()==context;
	}

	public static Predicate<Layer> layersForGroup(LayerGroup group)  {
		requireNonNull(group);

		return layer -> layer.getLayerGroup()==group;
	}

	// CONTEXTS factories

	public static Graph<Context> contextGraph(Context rootContext, Predicate<? super Context> filter, DependencyType...dependencies) {
		Graph<Context> graph = new Graph<>(Context.class, TYPE_CONTEXT);
		graph.fillWithMapper(Collections.singleton(rootContext), contextMapper(graph, toLookup(dependencies)), filter);
		return graph;
	}

	public static Graph<Context> contextGraph(Collection<? extends Context> rootContexts, Predicate<? super Context> filter, DependencyType...dependencies) {
		Graph<Context> graph = new Graph<>(Context.class, TYPE_CONTEXT);
		graph.fillWithMapper(rootContexts, contextMapper(graph, toLookup(dependencies)), filter);
		return graph;
	}

	public static Graph<Context> contextGraph(Corpus corpus, Predicate<? super Context> filter, DependencyType...dependencies) {
		Graph<Context> graph = new Graph<>(Context.class, TYPE_CONTEXT);
		graph.fillWithMapper(corpus.getRootContexts(), contextMapper(graph, toLookup(dependencies)), filter);
		return graph;
	}

	// GROUPS factories

	public static Graph<LayerGroup> groupGraph(LayerGroup rootGroup, Predicate<? super LayerGroup> filter, DependencyType...dependencies) {
		Graph<LayerGroup> graph = new Graph<>(LayerGroup.class, TYPE_GROUP);
		graph.fillWithMapper(Collections.singleton(rootGroup), groupMapper(graph, toLookup(dependencies)), filter);
		return graph;
	}

	public static Graph<LayerGroup> groupGraph(Collection<? extends LayerGroup> rootGroups, Predicate<? super LayerGroup> filter, DependencyType...dependencies) {
		Graph<LayerGroup> graph = new Graph<>(LayerGroup.class, TYPE_GROUP);
		graph.fillWithMapper(rootGroups, groupMapper(graph, toLookup(dependencies)), filter);
		return graph;
	}

	public static Graph<LayerGroup> groupGraph(Context rootContext, Predicate<? super LayerGroup> filter, DependencyType...dependencies) {
		Graph<LayerGroup> graph = new Graph<>(LayerGroup.class, TYPE_GROUP);
		graph.fillWithMapper(rootContext.getLayerGroups(), groupMapper(graph, toLookup(dependencies)), filter);
		return graph;
	}

	public static Graph<LayerGroup> groupGraph(Corpus corpus, Predicate<? super LayerGroup> filter, DependencyType...dependencies) {
		LazyCollection<LayerGroup> roots = LazyCollection.lazySet();
		Consumer<Context> collector = c -> roots.add(c.getPrimaryLayer().getLayerGroup()); //TODO do we rly want to limit groups to a subset?

		corpus.forEachRootContext(collector);
		corpus.forEachVirtualContext(collector);

		Graph<LayerGroup> graph = new Graph<>(LayerGroup.class, TYPE_GROUP);
		graph.fillWithMapper((Collection<LayerGroup>)roots.getAsSet(), groupMapper(graph, toLookup(dependencies)), filter);
		return graph;
	}

	// LAYERS factories

	public static Graph<Layer> layerGraph(Layer rootLayer, Predicate<? super Layer> filter, DependencyType...dependencies) {
		Graph<Layer> graph = new Graph<>(Layer.class, TYPE_LAYER);
		graph.fillWithMapper(Collections.singleton(rootLayer), layerMapper(graph, toLookup(dependencies)), filter);
		return graph;
	}

	public static Graph<Layer> layerGraph(Collection<? extends Layer> rootLayers, Predicate<? super Layer> filter, DependencyType...dependencies) {
		Graph<Layer> graph = new Graph<>(Layer.class, TYPE_LAYER);
		graph.fillWithMapper(rootLayers, layerMapper(graph, toLookup(dependencies)), filter);
		return graph;
	}

	public static Graph<Layer> layerGraph(LayerGroup rootGroup, Predicate<? super Layer> filter, DependencyType...dependencies) {
		Graph<Layer> graph = new Graph<>(Layer.class, TYPE_LAYER);
		graph.fillWithMapper(rootGroup.getLayers(), layerMapper(graph, toLookup(dependencies)), filter);
		return graph;
	}

	public static Graph<Layer> layerGraph(Context rootContext, Predicate<? super Layer> filter, DependencyType...dependencies) {
		Graph<Layer> graph = new Graph<>(Layer.class, TYPE_LAYER);
		graph.fillWithMapper(rootContext.getLayers(), layerMapper(graph, toLookup(dependencies)), filter);
		return graph;

	}

	public static Graph<Layer> layerGraph(Corpus rootCorpus, Predicate<? super Layer> filter, DependencyType...dependencies) {
		Graph<Layer> graph = new Graph<>(Layer.class, TYPE_LAYER);
		graph.fillWithMapper(rootCorpus.getLayers(), layerMapper(graph, toLookup(dependencies)), filter);
		return graph;
	}

	/**
	 * Converts an array of {@link DependencyType} instances into a lookup
	 * {@link Set}. If the provided array is {@code null} or empty, the returned
	 * set will contain all possible type {@link DependencyType#values() values}.
	 *
	 * @param dependencies
	 * @return
	 */
	private static Set<DependencyType> toLookup(DependencyType[] dependencies) {
		if(dependencies==null || dependencies.length==0) {
			return EnumSet.allOf(DependencyType.class);
		}

		Set<DependencyType> result = EnumSet.noneOf(DependencyType.class);
		CollectionUtils.feedItems(result, dependencies);

		return result;
	}

	// UTILITY FILLERS

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
	public static <E extends Object> Graph<E> genericGraphFromSupplier(Class<E> contentClass, Supplier<? extends E> startingPoints,
			Function<E, Collection<? extends E>> childFunc, Predicate<? super E> filter) {
		Graph<E> graph = new Graph<E>(contentClass, TYPE_UNKNOWN);

		graph.fillWithFunction(startingPoints, childFunc, filter);

		return graph;
	}

	/**
	 * @see #genericGraphFromSupplier(Class, Supplier, Function, Predicate)
	 *
	 * @param contentClass
	 * @param startingPoints
	 * @param childFunc
	 * @param filter
	 * @return
	 */
	public static <E extends Object> Graph<E> genericGraphFromCollection(Class<E> contentClass, Collection<? extends E> startingPoints,
			Function<E, Collection<? extends E>> childFunc, Predicate<? super E> filter) {
		Graph<E> graph = new Graph<E>(contentClass, TYPE_UNKNOWN);

		graph.fillWithFunction(startingPoints, childFunc, filter);

		return graph;
	}

	/**
	 * @see #genericGraphFromSupplier(Class, Supplier, Function, Predicate)
	 *
	 * @param contentClass
	 * @param startingPoints
	 * @param childFunc
	 * @param filter
	 * @return
	 */
	public static <E extends Object> Graph<E> genericGraphFromArray(Class<E> contentClass, E[] startingPoints,
			Function<E, E[]> childFunc, Predicate<? super E> filter) {
		Graph<E> graph = new Graph<E>(contentClass, TYPE_UNKNOWN);

		graph.fillWithFunction(startingPoints, childFunc, filter);

		return graph;
	}

	//TODO add genericGraph factory that uses the fillWithMapper

	/**
	 * One of the TYPE_CONTEXT, TYPE_GROUP, TYPE_LAYER constants
	 */
	private final int type;

	private final Class<E> contentClass;

	private final Map<E, Node<E>> graph = new Reference2ObjectOpenHashMap<>();

	// CONSTRUCTORS

	Graph(Class<E> contentClass, int type) {
		requireNonNull(contentClass);

		this.contentClass = contentClass;
		this.type = type;
	}

	// FILL METHODS

	Node<E> node(E content, boolean createIfMissing, boolean requirePresent) {
		Node<E> node = graph.get(content);
		if(node==null && createIfMissing) {
			node = new Node<>(content);
			graph.put(content, node);
		}
		if(node==null && requirePresent)
			throw new IllegalStateException("Missing internal buffer for node: "+content);

		return node;
	}

	Node<E> node(E content, boolean createIfMissing) {
		return node(content, createIfMissing, true);
	}

	void addLink(E from, E to) {
		requireNonNull(from);
		requireNonNull(to);

		node(from, true).addOutgoing(to);
		node(to, true).addIncoming(from);
	}

	public static BiConsumer<Layer, Consumer<? super Layer>> layerMapper(Graph<Layer> graph, Set<DependencyType> dependencies) {
		return (source, action) -> {
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
		};
	}

	public static BiConsumer<LayerGroup, Consumer<? super LayerGroup>> groupMapper(Graph<LayerGroup> graph, Set<DependencyType> dependencies) {
		return (source, action) -> {
			source.forEachDependency(d -> {
				if(dependencies.contains(d.getType())) {
					action.accept(d.getTarget());
				}
			});
		};
	}

	public static BiConsumer<Context, Consumer<? super Context>> contextMapper(Graph<Context> graph, Set<DependencyType> dependencies) {
		return (source, action) -> {
			source.forEachLayerGroup(g -> {
				g.forEachDependency(d -> {
					Context target = d.getTarget().getContext();
					if(source!=target && dependencies.contains(d.getType())) {
						action.accept(target);
					}
				});
			});
		};
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
	void fillWithFunction(Collection<? extends E> startingPoints, Function<E, Collection<? extends E>> childFunc, Predicate<? super E> filter) {
		ObjectArrayList<E> buffer = new ObjectArrayList<>();
		CollectionUtils.feedItems(buffer, startingPoints, filter);
		fillWithCollectionFunction0(buffer, childFunc, filter);
	}

	void fillWithFunction(Supplier<? extends E> startingPoints, Function<E, Collection<? extends E>> childFunc, Predicate<? super E> filter) {
		ObjectArrayList<E> buffer = new ObjectArrayList<>();
		CollectionUtils.feedItems(buffer, startingPoints, filter);
		fillWithCollectionFunction0(buffer, childFunc, filter);
	}

	private void fillWithCollectionFunction0(ObjectArrayList<E> buffer, Function<E, Collection<? extends E>> childFunc, Predicate<? super E> filter) {
		while(!buffer.isEmpty()) {
			E source = buffer.pop();
			Node<E> node = node(source, true);

			// Make sure we can't process already handled nodes
			if(node.flagSet(FLAG_FILLED)) {
				continue;
			}
			node.setFlag(FLAG_FILLED, true);

			Collection<? extends E> children = childFunc.apply(source);

			if(children!=null && !children.isEmpty()) {
				for(E target : children) {
					if(filter.test(target)) {
						node.addOutgoing(target);
						node(target, true).addIncoming(source);
						buffer.add(target);
					}
				}
			}
		}
	}

	void fillWithFunction(E[] startingPoints, Function<E, E[]> childFunc, Predicate<? super E> filter) {
		ObjectArrayList<E> buffer = new ObjectArrayList<>();
		CollectionUtils.feedItems(buffer, startingPoints, filter);
		fillWithArrayFunction0(buffer, childFunc, filter);
	}

	private void fillWithArrayFunction0(ObjectArrayList<E> buffer, Function<E, E[]> childFunc, Predicate<? super E> filter) {
		while(!buffer.isEmpty()) {
			E source = buffer.pop();
			Node<E> node = node(source, true);

			// Make sure we can't process already handled nodes
			if(node.flagSet(FLAG_FILLED)) {
				continue;
			}
			node.setFlag(FLAG_FILLED, true);

			E[] children = childFunc.apply(source);

			if(children!=null && children.length>0) {
				for(E target : children) {
					if(filter.test(target)) {
						node.addOutgoing(target);
						node(target, true).addIncoming(source);
						buffer.add(target);
					}
				}
			}
		}
	}

	void fillWithMapper(Collection<? extends E> startingPoints, BiConsumer<E, Consumer<? super E>> mapper, Predicate<? super E> filter) {
		ObjectArrayList<E> buffer = new ObjectArrayList<>();
		CollectionUtils.feedItems(buffer, startingPoints, filter);
		fillWithMapper0(buffer, mapper, filter);
	}

	void fillWithMapper(Supplier<? extends E> startingPoints, BiConsumer<E, Consumer<? super E>> mapper, Predicate<? super E> filter) {
		ObjectArrayList<E> buffer = new ObjectArrayList<>();
		CollectionUtils.feedItems(buffer, startingPoints, filter);
		fillWithMapper0(buffer, mapper, filter);
	}

	private void fillWithMapper0(ObjectArrayList<E> buffer, BiConsumer<E, Consumer<? super E>> mapper, Predicate<? super E> filter) {
		while(!buffer.isEmpty()) {
			E source = buffer.pop();
			Node<E> node = node(source, true);

			// Make sure we can't process already handled nodes
			if(node.flagSet(FLAG_FILLED)) {
				continue;
			}
			node.setFlag(FLAG_FILLED, true);

			mapper.accept(source, target -> {
				if(filter.test(target)) {
					node.addOutgoing(target);
					node(target, true).addIncoming(source);
					buffer.add(target);
				}
			});
		}
	}

	// GENERAL METHODS

	public void forEachRoot(Consumer<? super E> action) {
		requireNonNull(action);

		for(Node<E> node : graph.values()) {
			if(node.incomingCount()==0) {
				action.accept(node.content());
			}
		}
	}

	public Set<E> getRoots() {
		LazyCollection<E> result = LazyCollection.lazySet();
		forEachRoot(result);
		return result.getAsSet();
	}

	public void forEachLeaf(Consumer<? super E> action) {
		requireNonNull(action);

		for(Node<E> node : graph.values()) {
			if(node.outgoingCount()==0) {
				action.accept(node.content());
			}
		}
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

		Node<E> node = node(content, false, false);
		return node==null ? 0 : node.outgoingCount();
	}

	public int incomingCount(E content) {
		requireNonNull(content);

		Node<E> node = node(content, false, false);
		return node==null ? 0 : node.incomingCount();
	}

	private static <E extends Object> Set<E> unmodifiable(Set<E> set) {
		if(set==null) {
			set = Collections.emptySet();
		} else {
			set = Collections.unmodifiableSet(set);
		}
		return set;
	}

	public Set<E> incomingNodes(E content) {
		requireNonNull(content);

		Node<E> node = node(content, false, false);
		Set<E> incoming = node==null ? null : node.incoming();

		return unmodifiable(incoming);
	}

	public Set<E> outgoingNodes(E content) {
		requireNonNull(content);

		Node<E> node = node(content, false, false);
		Set<E> outgoing = node==null ? null : node.outgoing();

		return unmodifiable(outgoing);
	}

	public void forEachIncoming(E content, Consumer<? super E> action) {
		requireNonNull(content);
		requireNonNull(action);

		Node<E> node = node(content, false, false);
		if(node!=null) {
			node.forEachIncoming(action);
		}
	}

	public void forEachOutgoing(E content, Consumer<? super E> action) {
		requireNonNull(content);
		requireNonNull(action);

		Node<E> node = node(content, false, false);
		if(node!=null) {
			node.forEachOutgoing(action);
		}
	}

	// TYPE + METADATA methods

	public Class<E> getContentClass() {
		return contentClass;
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

	private void setFlagForNodes(int flag, boolean active) {
		for(Node<E> node : graph.values()) {
			node.setFlag(flag, active);
		}
	}

	// TRAVERSAL METHODS

	public static enum TraversalPolicy {
		/**
		 * Process node first, then all children
		 */
		PRE_ORDER,

		/**
		 * Process children first, then node itself
		 */
		POST_ORDER,
		;
	}

	public void walkTree(TraversalPolicy policy, Set<E> startingNodes, BiConsumer<Graph<E>, ? super E> action) {
		requireNonNull(policy);
		requireNonNull(action);
		requireNonNull(startingNodes);
		checkArgument("No starting nodes", !startingNodes.isEmpty());

		// Reset VISITED flag for entire graph
		setFlagForNodes(FLAG_VISITED, false);

		for(E node : startingNodes) {
			visit(policy, node, action);
		}
	}

	private void visit(TraversalPolicy policy, E content, BiConsumer<Graph<E>, ? super E> action) {
		Node<E> node = node(content, false, true);
		if(node.flagSet(FLAG_VISITED)) {
			return;
		}

		if(policy==TraversalPolicy.PRE_ORDER) {
			action.accept(this, content);
		}

		if(node.outgoingCount()>0) {
			for(E link : node.outgoing()) {
				visit(policy, link, action);
			}
		}

		if(policy==TraversalPolicy.POST_ORDER) {
			action.accept(this, content);
		}
	}

	public void walkTree(TraversalPolicy policy, Set<E> startingNodes, Consumer<? super E> action) {
		requireNonNull(policy);
		requireNonNull(action);
		requireNonNull(startingNodes);
		checkArgument("No starting nodes", !startingNodes.isEmpty());

		// Reset VISITED flag for entire graph
		setFlagForNodes(FLAG_VISITED, false);

		for(E node : startingNodes) {
			visit(policy, node, action);
		}
	}

	private void visit(TraversalPolicy policy, E content, Consumer<? super E> action) {
		Node<E> node = node(content, false, true);
		if(node.flagSet(FLAG_VISITED)) {
			return;
		}

		if(policy==TraversalPolicy.PRE_ORDER) {
			action.accept(content);
		}

		if(node.outgoingCount()>0) {
			for(E link : node.outgoing()) {
				visit(policy, link, action);
			}
		}

		if(policy==TraversalPolicy.POST_ORDER) {
			action.accept(content);
		}
	}

	/**
	 * Traverses this graph, visiting nodes in an unspecified order.
	 * At each node the given {@code visitor} is {@link BiPredicate#test(Object, Object) called}
	 * with this graph instance and the node in question as arguments.
	 * Outgoing links for a node are only being followed if the visitor
	 * returned {@code true} for above call. No node will be visited more
	 * than once.
	 *
	 * @param startingNodes
	 * @param visitor
	 */
	public void walkGraph(Set<E> startingNodes, BiPredicate<Graph<E>, ? super E> visitor) {
		requireNonNull(visitor);
		requireNonNull(startingNodes);
		checkArgument("No starting nodes", !startingNodes.isEmpty());

		// Reset VISITED flag for entire graph
		setFlagForNodes(FLAG_VISITED, false);

		// Grab nodes for starting points
		ObjectArrayList<Node<E>> buffer = new ObjectArrayList<>();
		for(E node : startingNodes) {
			buffer.add(node(node, false, true));
		}

		while(!buffer.isEmpty()) {
			Node<E> node = buffer.pop();

			boolean doContinue = visitor.test(this, node.content());

			// Mark node as VISITED
			node.setFlag(FLAG_VISITED, true);

			// Only if visitor allows it will we follow outgoing links
			if(doContinue) {
				Set<E> outgoing = node.outgoing();
				if(outgoing!=null) {
					for(E out : outgoing) {
						Node<E> nodeOut = node(out, false, true);
						if(!nodeOut.flagSet(FLAG_VISITED)) {
							buffer.add(nodeOut);
						}
					}
				}
			}
		}
	}

	public void walkGraph(Set<E> startingNodes, Predicate<? super E> visitor) {
		requireNonNull(visitor);
		requireNonNull(startingNodes);
		checkArgument("No starting nodes", !startingNodes.isEmpty());

		// Reset VISITED flag for entire graph
		setFlagForNodes(FLAG_VISITED, false);

		// Grab nodes for starting points
		ObjectArrayList<Node<E>> buffer = new ObjectArrayList<>();
		for(E node : startingNodes) {
			buffer.add(node(node, false, true));
		}

		while(!buffer.isEmpty()) {
			Node<E> node = buffer.pop();

			boolean doContinue = visitor.test(node.content());

			// Mark node as VISITED
			node.setFlag(FLAG_VISITED, true);

			// Only if visitor allows it will we follow outgoing links
			if(doContinue) {
				Set<E> outgoing = node.outgoing();
				if(outgoing!=null) {
					for(E out : outgoing) {
						Node<E> nodeOut = node(out, false, true);
						if(!nodeOut.flagSet(FLAG_VISITED)) {
							buffer.add(nodeOut);
						}
					}
				}
			}
		}
	}

	// COLLECTOR METHODS

	/**
	 * Used by our filler or builder code to check if we already processed
	 * a given node,
	 */
	private static final int FLAG_FILLED = (1<<0);

	/**
	 * Used by walker or visitor code to check if a given node has been
	 * visited previously.
	 */
	private static final int FLAG_VISITED = (1<<1);

	private static class Node<E extends Object> {
		private final E content;

		private Set<E> incoming, outgoing;
		private int flags = 0;

		Node(E content) {
			requireNonNull(content);

			this.content = content;
		}

		E content() {
			return content;
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
