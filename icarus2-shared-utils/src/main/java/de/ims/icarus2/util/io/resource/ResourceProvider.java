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
package de.ims.icarus2.util.io.resource;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.Lock;

/**
 * Models a highlevel view on a file system.
 *
 * @author Markus Gärtner
 *
 */
public interface ResourceProvider {

	/**
	 * Check whether the specified resource exists
	 */
	boolean exists(Path path);

	/**
	 * If necessary creates the specified resource.
	 *
	 * @see #exists(Path)
	 */
	boolean create(Path path, boolean directory) throws IOException;

	boolean isDirectory(Path path);

	Lock getLock(Path path);

	/**
	 *
	 * @see Files#newDirectoryStream(Path, String)
	 *
	 * @param folder
	 * @param glob
	 * @return
	 * @throws IOException
	 */
	DirectoryStream<Path> children(Path folder, String glob) throws IOException;

	/**
	 * Fetches the specified resource
	 */
	IOResource getResource(Path path) throws IOException;
}
