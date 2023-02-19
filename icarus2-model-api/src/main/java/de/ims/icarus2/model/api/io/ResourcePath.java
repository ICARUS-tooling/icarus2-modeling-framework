/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.io;

import static java.util.Objects.requireNonNull;

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
		requireNonNull(path);
		requireNonNull(type);

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
