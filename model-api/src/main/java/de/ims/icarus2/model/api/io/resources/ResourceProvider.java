/**
 *
 */
package de.ims.icarus2.model.api.io.resources;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.Lock;

/**
 * Models a highlevel view on
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
	boolean create(Path path) throws IOException;

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
