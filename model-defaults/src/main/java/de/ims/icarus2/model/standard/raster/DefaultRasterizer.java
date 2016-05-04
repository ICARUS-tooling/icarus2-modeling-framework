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

 * $Revision: 457 $
 * $Date: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/raster/DefaultRasterizer.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.raster;

import de.ims.icarus2.model.api.raster.Metric;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.api.raster.RasterAxis;
import de.ims.icarus2.model.api.raster.Rasterizer;
import de.ims.icarus2.model.manifest.util.Messages;

/**
 * @author Markus Gärtner
 * @version $Id: DefaultRasterizer.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public class DefaultRasterizer implements Rasterizer {

	private final RasterAxis[] axes;
	private final Metric<Position> metric;

	public DefaultRasterizer(RasterAxis[] axes, Metric<Position> metric) {
		if (axes == null)
			throw new NullPointerException("Invalid axes");
		if (metric == null)
			throw new NullPointerException("Invalid metric");
		if (axes.length == 0)
			throw new IllegalArgumentException("Empty axes array");

		this.axes = axes;
		this.metric = metric;
	}

	/**
	 * @see de.ims.icarus2.model.api.raster.Rasterizer#getAxisCount()
	 */
	@Override
	public int getAxisCount() {
		return axes.length;
	}

	/**
	 * @see de.ims.icarus2.model.api.raster.Rasterizer#getRasterAxisAt(int)
	 */
	@Override
	public RasterAxis getRasterAxisAt(int index) {
		return axes[index];
	}

	/**
	 * @see de.ims.icarus2.model.api.raster.Rasterizer#createPosition(long[])
	 */
	@Override
	public Position createPosition(long... values) {
		if(values.length!=axes.length)
			throw new IllegalArgumentException(Messages.mismatchMessage(
					"Provided number of vector elements does not equal axes count",
					axes.length, values.length));

		switch (values.length) {
		case 0:
			throw new IllegalArgumentException("Empty values array - cannot create position");
		case 1:
			return new Positions.Position1D(values[0]);
		case 2:
			return new Positions.Position2D(values[0], values[1]);
		case 3:
			return new Positions.Position3D(values[0], values[1], values[2]);

		default:
			return new Positions.PositionND(values);
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.raster.Rasterizer#getMetric()
	 */
	@Override
	public Metric<Position> getMetric() {
		return metric;
	}
}
