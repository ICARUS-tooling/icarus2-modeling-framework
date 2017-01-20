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
 *
 */
package de.ims.icarus2.model.standard.corpus;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultContext implements Context {

	private final Corpus corpus;
	private final ContextManifest manifest;
	private Driver driver;
	private boolean added;

	private ItemLayer primaryLayer;
	private ItemLayer foundationLayer;

	private final List<Layer> layers = new ArrayList<>(5);
	private final List<LayerGroup> layerGroups = new ArrayList<>(5);

	private final Map<String, Layer> layerLookup = new HashMap<>();
	private final LoadingCache<String, Layer> foreignLayerCache;

	public DefaultContext(Corpus corpus, ContextManifest manifest) {
		requireNonNull(corpus);
		requireNonNull(manifest);

		this.corpus = corpus;
		this.manifest = manifest;

		foreignLayerCache = CacheBuilder.newBuilder()
				.weakValues()
				.build(new CacheLoader<String, Layer>(){

					@Override
					public Layer load(String key) throws Exception {
						return lookupForeignLayer(key);
					}});
	}

	/**
	 * Looks up a foreign layer that is mapped to the given <b>local</b> id.
	 * @param id
	 * @return the foreign layer mapped to the given {@code id}
	 *
	 * @throws ModelException in case the {@code id} is invalid or there is no layer mapped to it
	 */
	protected Layer lookupForeignLayer(String id) {
		ContextManifest contextManifest = getManifest();
		PrerequisiteManifest prerequisiteManifest = contextManifest.getPrerequisite(id);

		if(prerequisiteManifest==null)
			throw new ModelException(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
					"No prerequisite manifest for id: "+id);

		String contextId = prerequisiteManifest.getContextId();
		String layerId = prerequisiteManifest.getLayerId();

		if(contextManifest.getId().equals(contextId))
			throw new ModelException(ManifestErrorCode.MANIFEST_ERROR,
					"Foreign layer id points to this context: "+contextId);

		Context foreignContext = getCorpus().getContext(contextId);

		//TODO is it allowed for the foreign context to return a layer that is foreign to it?
		return foreignContext.getNativeLayer(layerId);
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Context#getPrimaryLayer()
	 */
	@Override
	public ItemLayer getPrimaryLayer() {
		return primaryLayer;
	}

	/**
	 * Sets the primary layer for this context. Note that the primary layer must be
	 * added as regular layer prior to calling this method!
	 *
	 * @param primaryLayer the primaryLayer to set
	 */
	public void setPrimaryLayer(ItemLayer primaryLayer) {

		if(primaryLayer!=null) {
			if(!layers.contains(primaryLayer))
				throw new ModelException(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
						"Primary layer is unknown to this context: "+primaryLayer); //$NON-NLS-1$
			if(primaryLayer.getLayerGroup().getPrimaryLayer()!=primaryLayer)
				throw new ModelException(ManifestErrorCode.MANIFEST_INVALID_ENVIRONMENT,
						"Context's primary layer must be the primary layer of the hosting group: "+primaryLayer); //$NON-NLS-1$
		}

		this.primaryLayer = primaryLayer;
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Context#getFoundationLayer()
	 */
	@Override
	public ItemLayer getFoundationLayer() {
		return foundationLayer;
	}

	/**
	 * Sets the foundation layer for this context. Note that the foundation layer must be
	 * added as regular layer prior to calling this method!
	 *
	 * @param foundationLayer the foundationLayer to set
	 */
	public void setFoundationLayer(ItemLayer foundationLayer) {

		if(foundationLayer!=null) {
			if(!layers.contains(foundationLayer))
				throw new ModelException(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
						"Foundation layer is unknown to this context: "+foundationLayer); //$NON-NLS-1$
		}

		this.foundationLayer = foundationLayer;
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Context#getLayerGroups()
	 */
	@Override
	public List<LayerGroup> getLayerGroups() {
		return CollectionUtils.getListProxy(layerGroups);
	}

	public void addLayerGroup(LayerGroup group) {
		requireNonNull(group);

		if(layerGroups.contains(group))
			throw new IllegalArgumentException("Layer group already added: "+ModelUtils.getName(group)); //$NON-NLS-1$

		group.addNotify(this);

		layerGroups.add(group);
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Context#getDriver()
	 */
	@Override
	public Driver getDriver() {
		Driver driver = this.driver;
		if(driver==null)
			throw new ModelException(getCorpus(), GlobalErrorCode.ILLEGAL_STATE,
					"Not yet connected to any driver: "+ModelUtils.getName(this));

		return driver;
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Context#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return corpus;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.NamedCorpusMember#getName()
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Context#getManifest()
	 */
	@Override
	public ContextManifest getManifest() {
		return manifest;
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Context#addNotify(de.ims.icarus2.model.api.corpus.Corpus)
	 */
	@Override
	public void addNotify(Corpus corpus) {
		if(corpus!=this.corpus)
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					"Cannot add context to foreign corpus: "+ModelUtils.getName(corpus));

		added = true;
	}

	/**
	 * This implementation simply switches an internal flag so that it can keep track of
	 * whether or not it has been removed from the surrounding corpus.
	 *
	 * @see de.ims.icarus2.model.api.corpus.Context#removeNotify(de.ims.icarus2.model.api.corpus.Corpus)
	 */
	@Override
	public void removeNotify(Corpus corpus) {
		if(corpus!=this.corpus)
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					"Cannot remove context from foreign corpus: "+ModelUtils.getName(corpus));

		added = false;
	}

	@Override
	public boolean isAdded() {
		return added;
	}

	@Override
	public void connectNotify(Driver driver) {
		requireNonNull(driver);

		if(this.driver!=null)
			throw new ModelException(getCorpus(), GlobalErrorCode.ILLEGAL_STATE,
					"Already connected to a driver: "+ModelUtils.getName(this.driver));

		this.driver = driver;
	}

	@Override
	public void disconnectNotify(Driver driver) {
		requireNonNull(driver);

		if(this.driver!=driver)
			throw new ModelException(getCorpus(), GlobalErrorCode.ILLEGAL_STATE,
					"Cannot disconnect from unknown driver: "+ModelUtils.getName(driver));

		this.driver = null;
	}

	@Override
	public boolean isConnected() {
		return driver!=null;
	}

	@Override
	public Layer getNativeLayer(String id) {
		Layer layer = layerLookup.get(id);

		if(layer==null)
			throw new ModelException(ManifestErrorCode.MANIFEST_UNKNOWN_ID, "No such native layer: "+id);

		return layer;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <L extends Layer> L getLayer(String id) {
		Layer layer = layerLookup.get(id);

		if(layer==null) {
			// Not a local layer, try external resolution process
			try {
				layer = foreignLayerCache.get(id);
			} catch (ExecutionException|UncheckedExecutionException e) {
				throw ModelException.unwrap(e);
			}
		}

		if(layer==null)
			throw new ModelException(ManifestErrorCode.MANIFEST_UNKNOWN_ID, "No such layer: "+id);

		return (L) layer;
	}

	@Override
	public void forEachLayer(Consumer<? super Layer> action) {
		layers.forEach(action);
	}

	public void addLayer(Layer layer) {
		requireNonNull(layer);
		if(layer.getContext()!=this)
			throw new ModelException(ManifestErrorCode.MANIFEST_INVALID_ENVIRONMENT,
					"Foreign layer: "+ModelUtils.getName(layer)); //$NON-NLS-1$

		String id = layer.getManifest().getId();

		if(layerLookup.containsKey(id))
			throw new ModelException(ManifestErrorCode.MANIFEST_DUPLICATE_ID,
					"Layer id already mapped to different layer: "+id); //$NON-NLS-1$

		layers.add(layer);
		layerLookup.put(id, layer);

		// Notify corpus
		getCorpus().addLayer(layer);
	}

	public void removeLayer(Layer layer) {
		requireNonNull(layer);
		if(layer.getContext()!=this)
			throw new ModelException(ManifestErrorCode.MANIFEST_INVALID_ENVIRONMENT,
					"Foreign layer: "+ModelUtils.getName(layer)); //$NON-NLS-1$

		String id = layer.getManifest().getId();

		if(!layers.remove(layer))
			throw new ModelException(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
					"Unknown layer: "+ModelUtils.getName(layer)); //$NON-NLS-1$
		layerLookup.remove(id);

		getCorpus().removeLayer(layer);
	}
}
