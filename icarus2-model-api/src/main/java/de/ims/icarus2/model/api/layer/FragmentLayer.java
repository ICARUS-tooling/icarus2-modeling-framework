/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.layer;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.raster.RasterAxis;
import de.ims.icarus2.model.api.raster.Rasterizer;
import de.ims.icarus2.model.manifest.api.FragmentLayerManifest;

/**
 * Models a layer that holds fragments of another layer's items. Fragmentation is possible
 * for every item layer that is linked with an annotation layer featuring annotation values
 * for which a {@link Rasterizer} implementation is available.
 *
 * @author Markus Gärtner
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
