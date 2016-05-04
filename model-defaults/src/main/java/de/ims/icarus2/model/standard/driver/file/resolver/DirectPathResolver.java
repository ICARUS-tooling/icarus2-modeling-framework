/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus2.model.standard.driver.file.resolver;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotNull;
import gnu.trove.set.hash.THashSet;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.io.PathResolver;
import de.ims.icarus2.model.io.ResourcePath;
import de.ims.icarus2.model.manifest.api.LocationManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest.PathEntry;
import de.ims.icarus2.model.manifest.api.LocationManifest.PathType;
import de.ims.icarus2.model.manifest.api.LocationType;
import de.ims.icarus2.model.manifest.api.PathResolverManifest;

/**
 * Implements a simple path resolver that is directly linked to a fixed set
 * of path declarations pointing to local files.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DirectPathResolver implements PathResolver {


	public static DirectPathResolver forManifest(PathResolverManifest manifest) {
		return forManifest(manifest.getLocationManifest());
	}

	/**
	 * Convenient helper method that scans the provided {@link LocationManifest}
	 * and extracts all the paths in it.
	 * Note that the {@code manifest} must be of type {@link PathType#FILE FILE}
	 * or {@link PathType#FOLDER FOLDER} and must contain a valid
	 * {@link LocationManifest#getRootPath() root-path}!
	 * <p>
	 * For manifests of type {@link PathType#FILE FILE} the method will take the
	 * declared root-path as the sole file the returned resolver is going to contain.
	 * <p>
	 * For {@link PathType#FOLDER FOLDER} type manifests there are 2 strategies depending
	 * on whether or not the manifest contains additional {@link PathEntry} elements.
	 * If there are no such entries, then all {@link Files#isRegularFile(Path, java.nio.file.LinkOption...) regular}
	 * files in the root folder will be included. Otherwise the given entries will be
	 * used to {@link Path#resolve(String) resolve} the final paths relative to the root folder
	 * in case they are of type {@link PathType#FILE file} or used for scanning the folder content.
	 *
	 *
	 * @param manifest
	 * @return
	 */
	public static DirectPathResolver forManifest(LocationManifest manifest) {
		checkNotNull(manifest);
		checkArgument("Can only handle file or folder locations",
				manifest.getRootPathType()==PathType.FILE
				|| manifest.getRootPathType()==PathType.FOLDER);
		checkArgument("Manifest must define a root path", manifest.getRootPath()!=null);

		String rootPath = manifest.getRootPath();
		PathType rootPathType = manifest.getRootPathType();

		if(rootPathType==PathType.FILE) {
			return new DirectPathResolver(rootPath);
		} else { // can only be PathType.FOLDER now

			// We only allow shallow nesting, therefore only look for regular files within the root folder
			List<PathEntry> pathEntries = manifest.getPathEntries();

			List<String> files = new ArrayList<>();

			Path root = Paths.get(rootPath);

			// No path entries mean that we should add all regular files in the folder
			if(pathEntries.isEmpty()) {
				try (DirectoryStream<Path> stream = Files.newDirectoryStream(root, Files::isRegularFile)) {

					for(Path path : stream) {
						files.add(path.toString());
					}
				} catch (IOException e) {
					throw new ModelException(ModelErrorCode.IO_ERROR,
							"Failed to scan root folder for regular files: "+root, e);
				}
			} else {
				// If we have path entries, they represent explicit files or patterns in the host folder

				List<String> patterns = new ArrayList<>();
				Set<String> directFiles = new THashSet<>();

				for(PathEntry entry : pathEntries) {
					if(entry.getType()==PathType.FILE) {
						String value = entry.getValue();
						if(value==null || value.isEmpty())
							throw new ModelException(ModelErrorCode.MANIFEST_CORRUPTED_STATE,
									"Empty path");

						directFiles.add(value);
						files.add(root.resolve(value).toString());
					} else if(entry.getType()==PathType.PATTERN) {
						patterns.add(entry.getValue());
					} else
						throw new ModelException(ModelErrorCode.MANIFEST_ERROR,
								"Can only handle FILE or PATTERN path types inside FOLDER: got "+entry.getType());
				}

				// Scan pass on the folder in case we have patterns being defined
				if(!patterns.isEmpty()) {
					Matcher[] matchers = new Matcher[patterns.size()];

					for(int i=0; i<patterns.size(); i++) {
						matchers[i] = Pattern.compile(patterns.get(i)).matcher("");
					}

					Filter<Path> filter = p -> {
						if(!Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS)) {
							return false;
						}
						//TODO needs checking if we can safely assume a non-empty path
						String path = p.getFileName().toString();
						if(directFiles.contains(path)) {
							return false;
						}

						for(int i=0; i<matchers.length; i++) {
							Matcher matcher = matchers[i];

							matcher.reset(path);

							if(matcher.find()) {
								return true;
							}
						}

						return false;
					};

					// Scan folder content
					try (DirectoryStream<Path> stream = Files.newDirectoryStream(root, filter)) {

						for(Path path : stream) {
							files.add(path.toString());
						}
					} catch (IOException e) {
						throw new ModelException(ModelErrorCode.IO_ERROR,
								"Failed to scan root folder for files based on patterns: "+root, e);
					}
				}
			}

			if(files.isEmpty())
				throw new ModelException(ModelErrorCode.MANIFEST_CORRUPTED_STATE,
						"No valid entries found for file resolver");

			return new DirectPathResolver(files);
		}
	}

	private final String[] paths;

	public DirectPathResolver(List<String> paths) {
		checkNotNull(paths);
		checkArgument(!paths.isEmpty());

		this.paths = paths.toArray(new String[paths.size()]);
	}

	public DirectPathResolver(String...paths) {
		checkNotNull(paths);
		checkArgument(paths.length>0);

		this.paths = paths;
	}

	/**
	 * @see de.ims.icarus2.model.io.PathResolver#getPath(int)
	 */
	@Override
	public ResourcePath getPath(int chunkIndex) {
		return new ResourcePath(paths[chunkIndex], LocationType.LOCAL);
	}

	/**
	 * @see de.ims.icarus2.model.io.PathResolver#getPathCount()
	 */
	@Override
	public int getPathCount() {
		return paths.length;
	}

	/**
	 * @see de.ims.icarus2.model.io.PathResolver#close()
	 */
	@Override
	public void close() {
		// no-op
	}
}
