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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/layers/item/DefaultFragmentLayer.java $
 *
 * $LastChangedDate: 2015-08-19 15:38:58 +0200 (Mi, 19 Aug 2015) $
 * $LastChangedRevision: 422 $
 * $LastChangedBy: mcgaerty $
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
 * @version $Id: DefaultFragmentLayer.java 422 2015-08-19 13:38:58Z mcgaerty $
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
