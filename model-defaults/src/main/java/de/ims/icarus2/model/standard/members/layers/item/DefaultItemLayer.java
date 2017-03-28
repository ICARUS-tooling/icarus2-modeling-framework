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
package de.ims.icarus2.model.standard.members.layers.item;

import static de.ims.icarus2.util.Conditions.checkNotSet;
import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.driver.id.IdManager;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.standard.members.container.ProxyContainer;
import de.ims.icarus2.model.standard.members.layers.AbstractLayer;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultItemLayer extends AbstractLayer<ItemLayerManifest> implements ItemLayer {

	private ItemLayer boundaryLayer;
	private ItemLayer foundationLayer;

	/**
	 * Lazily acquired manager to map between ids and index values for this layer
	 */
	private IdManager idManager;

	private Container proxyContainer;

	/**
	 * @param context
	 * @param manifest
	 */
	public DefaultItemLayer(ItemLayerManifest manifest) {
		super(manifest);

		proxyContainer = new ProxyContainer(this);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.ItemLayer#getProxyContainer()
	 */
	@Override
	public Container getProxyContainer() {
		return proxyContainer;
	}

	/**
	 * Lazily acquires the {@link IdManager} for this layer if needed and
	 * then returns it.
	 *
	 * @see de.ims.icarus2.model.api.layer.ItemLayer#getIdManager()
	 */
	@Override
	public IdManager getIdManager() {
		if(idManager==null) {
			idManager = getContext().getDriver().getIdManager(getManifest());
		}

		return idManager;
	}

	/**
	 * @param boundaryLayer the boundaryLayer to set
	 */
	@Override
	public void setBoundaryLayer(ItemLayer boundaryLayer) {
		requireNonNull(boundaryLayer);

		checkNotSet("Boundary layer", this.boundaryLayer, boundaryLayer);

		this.boundaryLayer = boundaryLayer;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.StructureLayer#getBoundaryLayer()
	 */
	@Override
	public ItemLayer getBoundaryLayer() {
		return boundaryLayer;
	}

	/**
	 * @param boundaryLayer the foundationLayer to set
	 */
	@Override
	public void setFoundationLayer(ItemLayer foundationLayer) {
		requireNonNull(foundationLayer);

		checkNotSet("Foundation layer", this.foundationLayer, foundationLayer);

		this.foundationLayer = foundationLayer;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.StructureLayer#getFoundationLayer()
	 */
	@Override
	public ItemLayer getFoundationLayer() {
		return foundationLayer;
	}
}
