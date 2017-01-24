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
package de.ims.icarus2.model.standard.members.layers;

import static java.util.Objects.requireNonNull;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.Set;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.layer.Dependency;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.AbstractPart;
import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultLayerGroup extends AbstractPart<Context> implements LayerGroup {

	private final Set<Layer> layers = new ReferenceOpenHashSet<>();
	private final Set<Dependency<LayerGroup>> dependencies = new ReferenceOpenHashSet<>();
	private final LayerGroupManifest manifest;
	private ItemLayer primaryLayer;

	public DefaultLayerGroup(LayerGroupManifest manifest) {
		requireNonNull(manifest);

		this.manifest = manifest;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.LayerGroup#getContext()
	 */
	@Override
	public Context getContext() {
		checkAdded();
		return getOwner();
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.LayerGroup#getLayers()
	 */
	@Override
	public Set<Layer> getLayers() {
		return CollectionUtils.getSetProxy(layers);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.LayerGroup#getManifest()
	 */
	@Override
	public LayerGroupManifest getManifest() {
		return manifest;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.LayerGroup#getPrimaryLayer()
	 */
	@Override
	public ItemLayer getPrimaryLayer() {
		return primaryLayer;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.LayerGroup#getDependencies()
	 */
	@Override
	public Set<Dependency<LayerGroup>> getDependencies() {
		return CollectionUtils.getSetProxy(dependencies);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.LayerGroup#forEachLayer(java.util.function.Consumer)
	 */
	@Override
	public void forEachLayer(Consumer<? super Layer> action) {
		layers.forEach(action);
	}

	/**
	 * Changes the primary layer of this group. Note that the new primary layer must
	 * be added as regular layer to this group prior to making this method call!
	 *
	 * @param primaryLayer
	 */
	public void setPrimaryLayer(ItemLayer primaryLayer) {
		requireNonNull(primaryLayer);
		if(!layers.contains(primaryLayer))
			throw new ModelException(ManifestErrorCode.MANIFEST_UNKNOWN_ID, "Layer is unknown to this group: "+ModelUtils.getName(primaryLayer)); //$NON-NLS-1$

		this.primaryLayer = primaryLayer;
	}

	/**
	 * Adds the layer to the internal set of layers, discarding potential duplicates.
	 * @param layer
	 */
	public void addLayer(Layer layer) {
		requireNonNull(layer);
//		if(layers.contains(layer))
//			throw new IllegalArgumentException("Layer already added: "+ModelUtils.getName(layer)); //$NON-NLS-1$

		layer.addNotify(this);

		layers.add(layer);
	}

	/**
	 * Adds the specified dependency to the internal set of dependencies, discarding potential
	 * duplicates.
	 * @param dependency
	 */
	public void addDependency(Dependency<LayerGroup> dependency) {
		requireNonNull(dependency);
//		if(dependencies.contains(dependency))
//			throw new IllegalArgumentException("Dependency already added: "+dependency); //$NON-NLS-1$

		dependencies.add(dependency);
	}
}
