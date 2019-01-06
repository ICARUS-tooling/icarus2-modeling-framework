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
package de.ims.icarus2.model.standard.raster;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.lang.Primitives._int;
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

	public DefaultRasterizer(Metric<Position> metric, RasterAxis...axes) {
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
			throw new IllegalArgumentException(Messages.mismatch(
					"Provided number of vector elements does not equal axes count",
					_int(axes.length), _int(values.length)));

		return Positions.create(values);
	}

	/**
	 * @see de.ims.icarus2.model.api.raster.Rasterizer#getMetric()
	 */
	@Override
	public Metric<Position> getMetric() {
		return metric;
	}
}
