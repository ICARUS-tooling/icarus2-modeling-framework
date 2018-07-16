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
package de.ims.icarus2.model.api.layer;

import de.ims.icarus2.model.api.raster.Rasterizer;

/**
 * @author Markus Gärtner
 *
 */
public enum DependencyType {

	/**
	 * Used to assign boundary layers or containers.
	 */
	BOUNDARY,

	/**
	 * Used to assign foundation layers.
	 */
	FOUNDATION,

	/**
	 * Used to indicate the {@link AnnotationLayer} used for a certain {@link Rasterizer}.
	 */
	VALUE,

	/**
	 * Default type indicating a direct dependency (typically a base layer relationship).
	 */
	STRONG,

	;
}
