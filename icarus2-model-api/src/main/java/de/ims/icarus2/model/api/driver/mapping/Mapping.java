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
package de.ims.icarus2.model.api.driver.mapping;

import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.MappingManifest;

/**
 * @author Markus Gärtner
 *
 */
public interface Mapping {

	/**
	 * Returns the driver that created and manages this mapping.
	 *
	 * @return
	 */
	Driver getDriver();

	/**
	 * Returns the {@code source} layer for the mapping this mapping represents.
	 * Note that the mapping must accept each element in this source layer as a
	 * legal input to methods of its {@link MappingReader} instances!
	 *
	 * @return
	 */
	ItemLayerManifest getSourceLayer();

	/**
	 * Returns the {@code target} layer for the mapping this index represents.
	 *
	 * @return
	 */
	ItemLayerManifest getTargetLayer();

	/**
	 * Returns the manifest this mapping is based upon.
	 *
	 * @return
	 */
	MappingManifest getManifest();

	/**
	 * Creates a new reader instance to access the data in this mapping.
	 *
	 * @return
	 */
	MappingReader newReader();

	/**
	 * Releases all currently held resources.
	 */
	void close();
}