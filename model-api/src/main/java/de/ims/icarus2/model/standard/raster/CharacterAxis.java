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

 * $Revision: 400 $
 * $Date: 2015-05-29 15:06:46 +0200 (Fr, 29 Mai 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/raster/CharacterAxis.java $
 *
 * $LastChangedDate: 2015-05-29 15:06:46 +0200 (Fr, 29 Mai 2015) $
 * $LastChangedRevision: 400 $
 * $LastChangedBy: mcgaerty $
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
		return "CharacterAxis"; //$NON-NLS-1$
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
	 * @see de.ims.icarus2.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
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