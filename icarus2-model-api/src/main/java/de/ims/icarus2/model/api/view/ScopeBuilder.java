/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import java.util.List;

import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public class ScopeBuilder {

	private List<Layer> layers = new ArrayList<>();
	private List<Context> contexts = new ArrayList<>();
	private ItemLayer primaryLayer;
	private final Corpus corpus;

	public ScopeBuilder(Corpus corpus) {
		if (corpus == null)
			throw new NullPointerException("Invalid corpus"); //$NON-NLS-1$

		this.corpus = corpus;
	}

	public ScopeBuilder(Scope source) {
		if (source == null)
			throw new NullPointerException("Invalid source"); //$NON-NLS-1$

		corpus = source.getCorpus();
		primaryLayer = source.getPrimaryLayer();
		layers.addAll(source.getLayers());
		contexts.addAll(source.getContexts());
	}

	/**
	 * @return the layers
	 */
	public List<Layer> getLayers() {
		return CollectionUtils.getListProxy(layers);
	}

	/**
	 * @return the contexts
	 */
	public List<Context> getContexts() {
		return CollectionUtils.getListProxy(contexts);
	}

	/**
	 * @return the primaryLayer
	 */
	public ItemLayer getPrimaryLayer() {
		return primaryLayer;
	}

	/**
	 * @return the corpus
	 */
	public Corpus getCorpus() {
		return corpus;
	}

	/**
	 * @param layers the layers to set
	 */
	public ScopeBuilder setLayers(List<Layer> layers) {
		if (layers == null)
			throw new NullPointerException("Invalid layers"); //$NON-NLS-1$

		this.layers.clear();
		this.layers.addAll(layers);

		return this;
	}

	/**
	 * @param contexts the contexts to set
	 */
	public ScopeBuilder setContexts(List<Context> contexts) {
		if (contexts == null)
			throw new NullPointerException("Invalid contexts"); //$NON-NLS-1$

		this.contexts.clear();
		this.contexts.addAll(contexts);

		return this;
	}

	/**
	 * @param primaryLayer the primaryLayer to set
	 */
	public ScopeBuilder setPrimaryLayer(ItemLayer primaryLayer) {
		if (primaryLayer == null)
			throw new NullPointerException("Invalid primaryLayer"); //$NON-NLS-1$

		this.primaryLayer = primaryLayer;

		return this;
	}

	public ScopeBuilder addContext(Context context) {
		if (context == null)
			throw new NullPointerException("Invalid context"); //$NON-NLS-1$

		if(!contexts.contains(context)) {
			contexts.add(context);
		}

		return this;
	}

	public ScopeBuilder addContexts(List<Context> contexts) {
		if (contexts == null)
			throw new NullPointerException("Invalid contexts"); //$NON-NLS-1$

		for(Context context : contexts) {
			addContext(context);
		}

		return this;
	}

	public ScopeBuilder removeContext(Context context) {
		if (context == null)
			throw new NullPointerException("Invalid context"); //$NON-NLS-1$

		if(!contexts.remove(context))
			throw new IllegalArgumentException("Unknown context: "+context); //$NON-NLS-1$

		return this;
	}

	public ScopeBuilder removeContexts(List<Context> contexts) {
		if (contexts == null)
			throw new NullPointerException("Invalid contexts"); //$NON-NLS-1$

		for(Context context : contexts) {
			removeContext(context);
		}

		return this;
	}

	public ScopeBuilder removeAllContexts() {
		contexts.clear();

		return this;
	}

	public ScopeBuilder addLayer(Layer layer) {
		if (layer == null)
			throw new NullPointerException("Invalid layer"); //$NON-NLS-1$

		if(!contexts.contains(layer.getContext()))
			throw new IllegalArgumentException("Unknown context for layer: "+layer); //$NON-NLS-1$

		if(!layers.contains(layer)) {
			layers.add(layer);
		}

		return this;
	}

	public ScopeBuilder addLayers(List<Layer> layers) {
		if (layers == null)
			throw new NullPointerException("Invalid layess"); //$NON-NLS-1$

		for(Layer layer : layers) {
			addLayer(layer);
		}

		return this;
	}

	public ScopeBuilder removeLayer(Layer layer) {
		if (layer == null)
			throw new NullPointerException("Invalid layer"); //$NON-NLS-1$

		if(!layers.remove(layer))
			throw new IllegalArgumentException("Unknown layer: "+layer); //$NON-NLS-1$

		return this;
	}

	public ScopeBuilder removeLayers(List<Layer> layers) {
		if (layers == null)
			throw new NullPointerException("Invalid layers"); //$NON-NLS-1$

		for(Layer layer : layers) {
			removeLayer(layer);
		}

		return this;
	}

	public ScopeBuilder removeAllLayers() {
		layers.clear();

		return this;
	}

	public Scope build() {
		if(primaryLayer==null)
			throw new IllegalStateException("No primary layer defined"); //$NON-NLS-1$
		if(contexts.isEmpty())
			throw new IllegalStateException("No contexts defined"); //$NON-NLS-1$

		return new Scope(corpus, contexts, layers, primaryLayer);
	}

}
