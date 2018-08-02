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
package de.ims.icarus2.model.standard.raster;

import javax.swing.Icon;

import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.raster.RasterAxis;

public class CharacterAxis implements RasterAxis {

	/**
	 * @see de.ims.icarus2.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return "characterAxis"; //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return "Character Offset Axis";
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Orders characters according to their appearance in the hosting character sequence.";
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return null;
	}

	/**
	 * @see de.ims.icarus2.model.api.raster.RasterAxis#getMaxValue()
	 */
	@Override
	public long getMaxValue() {
		return Long.MAX_VALUE;
	}

	/**
	 * @see de.ims.icarus2.model.api.raster.RasterAxis#getMinValue()
	 */
	@Override
	public long getMinValue() {
		return 0;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}

	/**
	 *
	 * @see de.ims.icarus2.model.api.raster.RasterAxis#getRasterSize(de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.layer.FragmentLayer, java.lang.Object)
	 */
	@Override
	public long getRasterSize(Item item, FragmentLayer layer, Object value) {
		CharSequence s = (CharSequence) value;

		return s==null ? 0 : s.length();
	}

	/**
	 *
	 * @see de.ims.icarus2.model.api.raster.RasterAxis#getGranularity()
	 */
	@Override
	public long getGranularity() {
		return 1L;
	}

}