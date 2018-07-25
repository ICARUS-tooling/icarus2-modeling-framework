/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.layers.item;

import static de.ims.icarus2.util.Conditions.checkNotSet;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.raster.Rasterizer;
import de.ims.icarus2.model.manifest.api.FragmentLayerManifest;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultFragmentLayer extends DefaultItemLayer implements FragmentLayer {

	private Rasterizer rasterizer;
	private AnnotationLayer valueLayer;

	/**
	 * @param manifest
	 */
	public DefaultFragmentLayer(FragmentLayerManifest manifest) {
		super(manifest);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.FragmentLayer#getValueLayer()
	 */
	@Override
	public AnnotationLayer getValueLayer() {
		return valueLayer;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.FragmentLayer#getRasterizer()
	 */
	@Override
	public Rasterizer getRasterizer() {
		return rasterizer;
	}

	/**
	 * @param rasterizer the rasterizer to set
	 */
	@Override
	public void setRasterizer(Rasterizer rasterizer) {
		if (rasterizer == null)
			throw new NullPointerException("Invalid rasterizer"); //$NON-NLS-1$

		checkNotSet("Rasterizer", this.rasterizer, rasterizer);

		this.rasterizer = rasterizer;
	}

	/**
	 * @param valueLayer the valueLayer to set
	 */
	@Override
	public void setValueLayer(AnnotationLayer valueLayer) {
		if (valueLayer == null)
			throw new NullPointerException("Invalid valueLayer"); //$NON-NLS-1$

		checkNotSet("Value layer", this.valueLayer, valueLayer);

		this.valueLayer = valueLayer;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.FragmentLayer#getRasterSize(de.ims.icarus2.model.api.members.item.Item, int)
	 */
	@Override
	public long getRasterSize(Item item, int axis) {
		// Fetch annotation key + value
		String key = getManifest().getAnnotationKey();
		Object value = valueLayer.getAnnotationStorage().getValue(item, key);

		// Forward computation
		return rasterizer.getRasterAxisAt(axis).getRasterSize(item, this, value);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layers.AbstractLayer#getManifest()
	 */
	@Override
	public FragmentLayerManifest getManifest() {
		return (FragmentLayerManifest) super.getManifest();
	}
}
