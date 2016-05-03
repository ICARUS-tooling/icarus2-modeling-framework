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

 * $Revision: 382 $
 * $Date: 2015-04-09 16:23:50 +0200 (Do, 09 Apr 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/layer/FragmentLayer.java $
 *
 * $LastChangedDate: 2015-04-09 16:23:50 +0200 (Do, 09 Apr 2015) $
 * $LastChangedRevision: 382 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.layer;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.manifest.FragmentLayerManifest;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.raster.RasterAxis;
import de.ims.icarus2.model.api.raster.Rasterizer;

/**
 * Models a layer that holds fragments of another layer's items. Fragmentation is possible
 * for every item layer that is linked with an annotation layer featuring annotation values
 * for which a {@link Rasterizer} implementation is available.
 *
 * @author Markus Gärtner
 * @version $Id: FragmentLayer.java 382 2015-04-09 14:23:50Z mcgaerty $
 *
 * @see Fragment
 * @see Rasterizer
 */
public interface FragmentLayer extends ItemLayer {

	@Override
	FragmentLayerManifest getManifest();

	AnnotationLayer getValueLayer();

	Rasterizer getRasterizer();

	/**
	 * Short hand method for fetching the correct annotation value for the given
	 * item and then forwarding all necessary data to the appropriate {@link RasterAxis}
	 * of this layer's rasterizer.
	 *
	 * @param item
	 * @param axis
	 * @return
	 *
	 * @see RasterAxis#getRasterSize(Item, FragmentLayer, Object)
	 * @see Rasterizer#getRasterAxisAt(int)
	 */
	long getRasterSize(Item item, int axis);

	// Modification methods

	/**
	 * Defines the layer to obtain annotation values from that are used for rasterization in
	 * this fragment layer.
	 *
	 * @param layer
	 *
	 * @throws NullPointerException iff the {@code layer} argument is {@code null}
	 * @throws ModelException in case the value layer has already been set
	 */
	void setValueLayer(AnnotationLayer layer);

	/**
	 * Defines the rasterizer to use in this layer
	 * @param rasterizer
	 *
	 * @throws NullPointerException iff the {@code rasterizer} argument is {@code null}
	 * @throws ModelException in case the rasterizer has already been set
	 */
	void setRasterizer(Rasterizer rasterizer);
}
