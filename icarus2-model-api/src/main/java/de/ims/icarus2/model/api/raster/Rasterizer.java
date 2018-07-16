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
package de.ims.icarus2.model.api.raster;


/**
 * Defines a transformation facility that defines a vector space and allows for the
 * transformation of a set of raster values into a "vector" {@link Position}.
 *
 * @author Markus Gärtner
 *
 */
public interface Rasterizer {

	/**
	 * Dimensionality of the underlying vector space model.
	 *
	 * @return
	 */
	int getAxisCount();

	/**
	 * Fetches the specified axis of the underlying vector space model.
	 *
	 * @param index
	 * @return
	 */
	RasterAxis getRasterAxisAt(int index);

	/**
	 * Wraps the given set of values into a vector usable as a {@link Position}.
	 * @param values
	 * @return
	 */
	Position createPosition(long...values);

	/**
	 * Returns the metric to be used for the underlying vector space
	 *
	 * @return
	 */
	Metric<Position> getMetric();
}