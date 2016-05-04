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

 * $Revision: 453 $
 * $Date: 2016-02-10 12:35:05 +0100 (Mi, 10 Feb 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/corpus/Scope.java $
 *
 * $LastChangedDate: 2016-02-10 12:35:05 +0100 (Mi, 10 Feb 2016) $
 * $LastChangedRevision: 453 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.corpus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
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
 * @version $Id: Scope.java 453 2016-02-10 11:35:05Z mcgaerty $
 *
 */
public class Scope {

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
			throw new IllegalArgumentException("Primary layer is not party of specified corpus: "+CorpusUtils.getName(primaryLayer)); //$NON-NLS-1$

		for(Context context : contexts) {
			if(context.getCorpus()!=corpus)
				throw new IllegalArgumentException("Context is not party of specified corpus: "+CorpusUtils.getName(context)); //$NON-NLS-1$
		}

		for(Layer layer : layers) {
			if(layer.getCorpus()!=corpus)
				throw new IllegalArgumentException("Layer is not party of specified corpus: "+CorpusUtils.getName(layer)); //$NON-NLS-1$
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
