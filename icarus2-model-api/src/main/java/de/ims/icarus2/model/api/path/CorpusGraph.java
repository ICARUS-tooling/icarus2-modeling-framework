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
package de.ims.icarus2.model.api.path;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.DependencyType;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.FragmentLayerManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.IcarusUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 *
 * @author Markus Gärtner
 *
 */
public class CorpusGraph {

	private final CorpusManifest corpus;

	private final List<ContextManifest> contexts = new ArrayList<>();

	private final Map<LayerManifest<?>, Node> nodeMap = new Object2ObjectOpenHashMap<>();

	final Lock lock = new ReentrantLock();

	public CorpusGraph(CorpusManifest corpus) {
		requireNonNull(corpus);
		checkNotTemplate(corpus);

		this.corpus = corpus;
	}

	public CorpusManifest getCorpus() {
		return corpus;
	}

	private static void checkNotTemplate(Manifest manifest) {
		if(manifest.isTemplate())
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Manifest must not be a template: "+ModelUtils.getName(manifest));
	}

	public void addContext(ContextManifest context) {
		requireNonNull(context);
		checkNotTemplate(context);
		checkArgument(IcarusUtils.equals(context.getCorpusManifest(), corpus));

		lock.lock();
		try {
			contexts.add(context);

			for(LayerManifest<?> layer : context.getLayerManifests()) {
				getNode(layer);
			}
		} finally {
			lock.unlock();
		}
	}

	public Node getNode(LayerManifest<?> layer) {
		requireNonNull(layer);
		//TODO maybe ensure that we only accept layers that are part of the saved corpus!!!

		lock.lock();
		try {
			Node node = nodeMap.get(layer);

			if(node==null) {
				node = new Node(layer);
				nodeMap.put(layer, node);
			}

			return node;
		} finally {
			lock.unlock();
		}
	}

	public abstract class Cell {
		private Map<Object, Object> properties;

		public Object putClientProperty(Object key, Object value) {
			if(properties==null) {
				properties = new Object2ObjectOpenHashMap<>();
			}

			return value==null ? properties.remove(key) : properties.put(key, value);
		}

		@SuppressWarnings("unchecked")
		public <O extends Object> O getClientProperty(Object key) {
			return properties==null ? null : (O)properties.get(key);
		}

		public CorpusGraph getGraph() {
			return CorpusGraph.this;
		}

		public void resetClientProperties() {
			if(properties!=null) {
				properties.clear();
			}
		}
	}

	public class Node extends Cell {
		private final LayerManifest<?> layer;

		private AtomicBoolean initialized = new AtomicBoolean(false);

		private List<Edge> outgoingEdges = new ArrayList<>(5);
		private List<Edge> incomingEdges = new ArrayList<>(5);


		Node(LayerManifest<?> layer) {
			this.layer = layer;
		}

		public LayerManifest<?> getLayer() {
			return layer;
		}

		private void ensureInit() {
			if(initialized.compareAndSet(false, true)) {

				lock.lock();
				try {
					for(TargetLayerManifest baseLayer : layer.getBaseLayerManifests()) {
						tryCreateEdge(baseLayer, DependencyType.STRONG);
					}

					if(ModelUtils.isItemLayer(layer)) {
						ItemLayerManifestBase<?> itemLayer = (ItemLayerManifestBase<?>) layer;
						tryCreateEdge(itemLayer.getBoundaryLayerManifest().orElse(null), DependencyType.BOUNDARY);
						tryCreateEdge(itemLayer.getFoundationLayerManifest().orElse(null), DependencyType.FOUNDATION);

						if(ModelUtils.isFragmentLayer(itemLayer)) {
							FragmentLayerManifest fragmentLayer = (FragmentLayerManifest)itemLayer;
							tryCreateEdge(fragmentLayer.getValueLayerManifest().orElse(null), DependencyType.VALUE);
						}
					}
				} finally {
					lock.unlock();
				}
			}
		}

		private void tryCreateEdge(TargetLayerManifest manifest, DependencyType type) {
			if(manifest==null) {
				return;
			}

			LayerManifest<?> targetLayer = manifest.getResolvedLayerManifest()
					.orElseThrow(ManifestException.error(
							"Unresolved target layer manifest: "+ManifestUtils.getName(manifest)));

			Node targetNode = getNode(targetLayer);

			Edge edge = new Edge(this, targetNode, type);

			addOutgoingEdge(edge);
			targetNode.addIncomingEdge(edge);
		}

		void addOutgoingEdge(Edge edge) {
			outgoingEdges.add(edge);
		}

		void addIncomingEdge(Edge edge) {
			incomingEdges.add(edge);
		}

		public int getOutgoingEdgeCount() {
			ensureInit();
			return outgoingEdges.size();
		}

		public int getIncomingEdgeCount() {
			ensureInit();
			return incomingEdges.size();
		}

		public int getEdgeCount(boolean isSource) {
			ensureInit();
			return isSource ? outgoingEdges.size() : incomingEdges.size();
		}

		public Edge getOutgoingEdgeAt(int index) {
			ensureInit();
			return outgoingEdges.get(index);
		}

		public Edge getIncomingEdgeAt(int index) {
			ensureInit();
			return incomingEdges.get(index);
		}

		public Edge getEdgeAt(boolean isSource, int index) {
			ensureInit();
			return isSource ? outgoingEdges.get(index) : incomingEdges.get(index);
		}

		public void forAllEdges(Consumer<? super Edge> action) {
			ensureInit();
			for(int i=0; i<incomingEdges.size(); i++) {
				action.accept(incomingEdges.get(i));
			}
			for(int i=0; i<outgoingEdges.size(); i++) {
				action.accept(outgoingEdges.get(i));
			}
		}

		public void forAllIncomingEdges(Consumer<? super Edge> action) {
			ensureInit();
			for(int i=0; i<incomingEdges.size(); i++) {
				action.accept(incomingEdges.get(i));
			}
		}

		public void forAllOutgoingEdges(Consumer<? super Edge> action) {
			ensureInit();
			for(int i=0; i<outgoingEdges.size(); i++) {
				action.accept(outgoingEdges.get(i));
			}
		}

		public void forAllEdges(boolean isSource, Consumer<? super Edge> action) {
			if(isSource) {
				forAllOutgoingEdges(action);
			} else {
				forAllIncomingEdges(action);
			}
		}
	}

	public class Edge extends Cell {
		private final Node source, target;
		private final DependencyType type;

		public Edge(Node source, Node target, DependencyType type) {
			this.source = source;
			this.target = target;
			this.type = type;
		}

		public Node getSource() {
			return source;
		}

		public Node getTarget() {
			return target;
		}

		public DependencyType getType() {
			return type;
		}
	}
}
