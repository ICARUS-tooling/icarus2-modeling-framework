/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
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
 *
 */
package de.ims.icarus2.model.standard.raster;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.raster.Metric;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.api.raster.RasterAxis;
import de.ims.icarus2.model.api.raster.Rasterizer;
import de.ims.icarus2.model.manifest.util.Messages;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultRasterizer implements Rasterizer {

	private final RasterAxis[] axes;
	private final Metric<Position> metric;

	public DefaultRasterizer(RasterAxis[] axes, Metric<Position> metric) {
		requireNonNull(axes);
		checkArgument("Axes array must not be empty", axes.length>0);
		requireNonNull(metric);

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

		return Positions.createPosition(values);
	}

	/**
	 * @see de.ims.icarus2.model.api.raster.Rasterizer#getMetric()
	 */
	@Override
	public Metric<Position> getMetric() {
		return metric;
	}
}
