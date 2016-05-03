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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/raster/RasterAxis.java $
 *
 * $LastChangedDate: 2015-04-09 16:23:50 +0200 (Do, 09 Apr 2015) $
 * $LastChangedRevision: 382 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.raster;

import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id: RasterAxis.java 382 2015-04-09 14:23:50Z mcgaerty $
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
	long getRasterSize(Item item, FragmentLayer layer, Object value);
}
