/**
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
package de.ims.icarus2.model.standard.members.layer.item;

import static de.ims.icarus2.util.Conditions.checkNotSet;
import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.driver.id.IdManager;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.standard.members.container.ProxyContainer;
import de.ims.icarus2.model.standard.members.layer.AbstractLayer;
import de.ims.icarus2.util.annotations.TestableImplementation;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(ItemLayer.class)
public class DefaultItemLayer extends AbstractLayer<ItemLayerManifestBase<?>> implements ItemLayer {

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

		initProxy();
	}

	protected DefaultItemLayer(ItemLayerManifestBase<?> manifest) {
		super(manifest);

		initProxy();
	}

	private void initProxy() {
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
	 * Forces this layer to use the supplied {@link IdManager} instance.
	 *
	 * @param idManager the idManager to set
	 */
	public void setIdManager(IdManager idManager) {
		this.idManager = idManager;
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
