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
package de.ims.icarus2.model.api.io;

import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.net.URL;
import java.nio.file.Path;

import de.ims.icarus2.model.manifest.api.LocationType;
import de.ims.icarus2.util.Options;

/**
 * Helper class to describe an abstract physical data location in the context
 * of some {@link LocationType}. Depending on the type of the source data the
 * meaning of the returned {@code path} string may vary. While for {@link LocationType#LOCAL LOCAL}
 * or {@link LocationType#REMOTE REMOTE} the {@code path} itself is sufficient for accessing
 * the data by translating it into a {@link Path} or {@link URL} object and then opening
 * the respective input stream, the matter becomes more complicated when data is stored within
 * a database system. In this case the {@code path} denotes the address of the database and
 * additional information (like the row index of a table to start from, etc...) can be
 * obtained via property values set on the {@code ResourcePath} object.
 * <p>
 * Note that the {@code ResourcePath} model does not provide
 * translation of the abstract path to the data into a readable stream. All {@code LocationType}
 * specific behavior is to be implemented by the objects that use a {@code ResourcePath} instance to
 * load data from!
 *
 * @author Markus Gärtner
 *
 */
public class ResourcePath extends Options {

	private static final long serialVersionUID = 4608518181833150521L;

	private final String path;
	private final LocationType type;

	/**
	 * Creates a new {@code ResourcePath} using the given (relative or abstract) path and {@code LocationType}.
	 * @param path
	 * @param type
	 */
	public ResourcePath(String path, LocationType type) {
		checkNotNull(path);
		checkNotNull(type);

		this.path = path;
		this.type = type;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @return the type
	 */
	public LocationType getType() {
		return type;
	}

}
