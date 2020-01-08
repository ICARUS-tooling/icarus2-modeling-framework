/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
