/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * Models a compact description of a subset of contexts and layers
 * used for a specific purpose. Each {@code scope} is defined by its
 * <i>primary layer</i> as returned by {@link #getPrimaryLayer()}. This
 * layer is used as the measure of granularity (Note that this layer
 * is bound to be a {@code ItemLayer} or a compatible class).
 * For every element in this layer that was declared loaded by the host
 * corpus, it is guaranteed that all required data of underlying layers that are
 * within this scope (as can be checked by {@link #containsLayer(Layer)}
 * will also be available.
 * <p>
 * A scope models the <i>vertical</i> part of filtering for scalable
 * access to corpus resources provided by the framework.
 *
 * @author Markus Gärtner
 *
 */
public class Scope {

	public static Scope withLayers(Corpus corpus, String...layerIds) {
		ScopeBuilder builder = new ScopeBuilder(corpus);

		for(String layerId : layerIds) {
			Layer layer = corpus.getLayer(layerId, true);
			builder.addContext(layer.getContext());
			builder.addLayer(layer);
		}

		builder.addContext(corpus.getRootContext());
		builder.setPrimaryLayer(corpus.getPrimaryLayer());

		return builder.build();
	}

	private final Corpus corpus;
	private final ItemLayer primaryLayer;
	private final List<Context> contexts;
	private final List<Layer> layers;

	private Set<Context> contextsLut;
	private Set<Layer> layersLut;

	public Scope(Corpus corpus, List<Context> contexts, List<Layer> layers, ItemLayer primaryLayer) {
		if (corpus == null)
			throw new NullPointerException("Invalid corpus");
		if (contexts == null)
			throw new NullPointerException("Invalid contexts");
		if (layers == null)
			throw new NullPointerException("Invalid layers");
		if (primaryLayer == null)
			throw new NullPointerException("Invalid primaryLayer");

		if(layers.isEmpty())
			throw new IllegalArgumentException("List of layers is empty"); //$NON-NLS-1$

		if(primaryLayer.getCorpus()!=corpus)
			throw new IllegalArgumentException("Primary layer is not party of specified corpus: "+ModelUtils.getName(primaryLayer)); //$NON-NLS-1$

		for(Context context : contexts) {
			if(context.getCorpus()!=corpus)
				throw new IllegalArgumentException("Context is not party of specified corpus: "+ModelUtils.getName(context)); //$NON-NLS-1$
		}

		for(Layer layer : layers) {
			if(layer.getCorpus()!=corpus)
				throw new IllegalArgumentException("Layer is not party of specified corpus: "+ModelUtils.getName(layer)); //$NON-NLS-1$
		}

		this.corpus = corpus;
		this.primaryLayer = primaryLayer;
		this.contexts = new ArrayList<>(contexts);
		this.layers = new ArrayList<>(layers);
	}

	public Corpus getCorpus() {
		return corpus;
	}

	public List<Context> getContexts() {
		return CollectionUtils.getListProxy(contexts);
	}

	public int getContextCount() {
		return contexts.size();
	}

	public boolean containsContext(Context context) {
		if (context == null)
			throw new NullPointerException("Invalid context"); //$NON-NLS-1$

		if(contextsLut==null) {
			contextsLut = new HashSet<>(contexts);
		}

		return contextsLut.contains(context);
	}

	/**
	 * Returns {@code true} if the given layer is either the
	 * primary layer of this scope or contained in the list
	 * of secondary layers.
	 *
	 * @throws NullPointerException if the {@code layer} argument
	 * is {@code null}.
	 */
	public boolean containsLayer(Layer layer) {
		if (layer == null)
			throw new NullPointerException("Invalid layer"); //$NON-NLS-1$

		if(layersLut==null) {
			layersLut = new HashSet<>(layers);
		}

		return layersLut.contains(layer);
	}

	/**
	 * Returns the layer that defines the granularity of this scope.
	 * The members of that layer are intended to represent atomic units
	 * when loading and/or caching is performed for this scope. Atomicity
	 * in the context of a scope means that once a member of that layer is
	 * loaded, the corpus guarantees that all underlying data referenced by
	 * that member will be fully available, too. It is therefore vital to
	 * follow the simple rule when defining a layer as primary:
	 * <p>
	 * <i>As fine-grained as possible, as coarse-grained as necessary!</i>
	 */
	public ItemLayer getPrimaryLayer() {
		return primaryLayer;
	}

	/**
	 * Returns the list of additional layers available through this scope.
	 * This can be an arbitrary subset of the combined collection of layers
	 * hosted by all the {@code Context} instances as returned by {@link #getContexts()}.
	 * <p>
	 * Note however, that there are some things to keep in mind:
	 * <ul>
	 * <li><b>{@link AnnotationLayer}</b>s are only allowed in a scope if their respective
	 * base-layer is also a part of that scope.</li>
	 * <li><b>{@link ItemLayer}</b>s below the primary layer will be guaranteed to get
	 * their content loaded in blocks that are covered by the members in the primary layer</li>
	 * <li><b>{@link ItemLayer}</b>s above the primary layer will only be loaded partially unlike
	 * specifically requested</li>
	 * <li>The behavior of <b>{@link ItemLayer}</b>s that are not linked to the primary
	 * layer in any way is not specified.</li>
	 * </ul>
	 */
	public List<Layer> getLayers() {
		return CollectionUtils.getListProxy(layers);
	}

	public void forEachLayer(Consumer<? super Layer> action) {
		layers.forEach(action);
	}

	public void forEachContext(Consumer<? super Context> action) {
		contexts.forEach(action);
	}

	public int getLayerCount() {
		return layers.size();
	}
}
