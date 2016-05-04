/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 422 $
 * $Date: 2015-08-19 15:38:58 +0200 (Mi, 19 Aug 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/path/CorpusGraph.java $
 *
 * $LastChangedDate: 2015-08-19 15:38:58 +0200 (Mi, 19 Aug 2015) $
 * $LastChangedRevision: 422 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.path;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotNull;
import gnu.trove.map.hash.THashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.DependencyType;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.FragmentLayerManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest;
import de.ims.icarus2.model.manifest.api.Manifest;

/**
 *
 * @author Markus Gärtner
 * @version $Id: CorpusGraph.java 422 2015-08-19 13:38:58Z mcgaerty $
 *
 */
public class CorpusGraph {

	private final CorpusManifest corpus;

	private final List<ContextManifest> contexts = new ArrayList<>();

	private final Map<LayerManifest, Node> nodeMap = new THashMap<>();

	final Lock lock = new ReentrantLock();

	public CorpusGraph(CorpusManifest corpus) {
		checkNotNull(corpus);
		checkNotTemplate(corpus);

		this.corpus = corpus;
	}

	public CorpusManifest getCorpus() {
		return corpus;
	}

	private static void checkNotTemplate(Manifest manifest) {
		if(manifest.isTemplate())
			throw new ModelException(ModelErrorCode.INVALID_INPUT,
					"Manifest must not be a template: "+getName(manifest));
	}

	public void addContext(ContextManifest context) {
		checkNotNull(context);
		checkNotTemplate(context);
		checkArgument(context.getCorpusManifest()==corpus);

		lock.lock();
		try {
			contexts.add(context);

			for(LayerManifest layer : context.getLayerManifests()) {
				getNode(layer);
			}
		} finally {
			lock.unlock();
		}
	}

	public Node getNode(LayerManifest layer) {
		checkNotNull(layer);
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
				properties = new THashMap<>();
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
		private final LayerManifest layer;

		private AtomicBoolean initialized = new AtomicBoolean(false);

		private List<Edge> outgoingEdges = new ArrayList<>(5);
		private List<Edge> incomingEdges = new ArrayList<>(5);


		Node(LayerManifest layer) {
			this.layer = layer;
		}

		public LayerManifest getLayer() {
			return layer;
		}

		private void ensureInit() {
			if(initialized.compareAndSet(false, true)) {

				lock.lock();
				try {
					for(TargetLayerManifest baseLayer : layer.getBaseLayerManifests()) {
						tryCreateEdge(baseLayer, DependencyType.STRONG);
					}

					if(CorpusUtils.isItemLayer(layer)) {
						ItemLayerManifest itemLayer = (ItemLayerManifest) layer;
						tryCreateEdge(itemLayer.getBoundaryLayerManifest(), DependencyType.BOUNDARY);
						tryCreateEdge(itemLayer.getFoundationLayerManifest(), DependencyType.FOUNDATION);

						if(CorpusUtils.isFragmentLayer(itemLayer)) {
							FragmentLayerManifest fragmentLayer = (FragmentLayerManifest)itemLayer;
							tryCreateEdge(fragmentLayer.getValueLayerManifest(), DependencyType.VALUE);
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

			LayerManifest targetLayer = manifest.getResolvedLayerManifest();

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
