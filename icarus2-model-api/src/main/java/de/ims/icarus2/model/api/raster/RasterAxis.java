/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.raster;

import javax.annotation.Nullable;

import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.id.Identity;

/**
 * @author Markus Gärtner
 *
 */
public interface RasterAxis extends Identity {

	/**
	 * Returns the minimum value a vector is allowed to
	 * be assigned for this axis.
	 * @return
	 */
	long getMinValue();

	/**
	 * Returns the maximum value a vector is allowed to
	 * be assigned for this axis.
	 * @return
	 */
	long getMaxValue();

	/**
	 * Returns the granularity of this axis, i.e. the minimum distance
	 * of two values in the rester space to be considered <i>truly different</i>.
	 *
	 * @return
	 */
	long getGranularity();

	/**
	 * Calculates and returns the size of the raster space for the given annotation
	 * value.
	 *
	 * @param item
	 * @param layer
	 * @param value
	 * @return
	 */
	long getRasterSize(Item item, FragmentLayer layer, @Nullable Object value);
}
