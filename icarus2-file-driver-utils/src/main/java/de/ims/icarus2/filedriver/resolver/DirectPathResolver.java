/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
package de.ims.icarus2.filedriver.resolver;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.io.PathResolver;
import de.ims.icarus2.model.api.io.ResourcePath;
import de.ims.icarus2.model.api.io.resources.ResourceProvider;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.LocationManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest.PathEntry;
import de.ims.icarus2.model.manifest.api.LocationManifest.PathType;
import de.ims.icarus2.model.manifest.api.LocationType;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * Implements a simple path resolver that is directly linked to a fixed set
 * of path declarations pointing to local files.
 *
 * @author Markus Gärtner
 *
 */
public class DirectPathResolver implements PathResolver {

	/**
	 * Convenient helper method that scans the provided {@link LocationManifest}
	 * and extracts all the paths in it.
	 * Note that the {@code manifest} must be of type {@link PathType#FILE FILE},
	 * {@link PathType#RESOURCE} or {@link PathType#FOLDER FOLDER} and for the last two
	 * must contain a valid {@link LocationManifest#getRootPath() root-path}!
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
	 * <p>
	 * For manifests of type {@link PathType#RESOURCE} will try to use the current class
	 * loader to {@link ClassLoader#getResource(String) resolve} the resource, transform
	 * the {@link URL} into a {@link Path} and then use those paths as the resolved elements.
	 *
	 * @param manifest
	 * @return
	 */
	public static DirectPathResolver forManifest(LocationManifest manifest, ResourceProvider resourceProvider) {
		requireNonNull(manifest);

		String rootPath = manifest.getRootPath();
		PathType rootPathType = manifest.getRootPathType();

		checkArgument("Can only handle file, resource or folder locations",
				rootPathType==PathType.FILE || rootPathType==PathType.FOLDER || rootPathType==PathType.RESOURCE);
		checkArgument("Manifest must define a root path", rootPath!=null);


		if(rootPathType==PathType.FILE) {
			return new DirectPathResolver(rootPath);
		}

		// We only allow shallow nesting, therefore only look for regular files within the root folder
		List<PathEntry> pathEntries = manifest.getPathEntries();
		List<String> files = new ArrayList<>();

		if(rootPathType==PathType.RESOURCE) {
			ClassLoader classLoader = DirectPathResolver.class.getClassLoader();
			URL url = classLoader.getResource(rootPath);
			Path path;

			try {
				path = Paths.get(url.toURI());
			} catch (URISyntaxException e) {
				throw new ModelException(ManifestErrorCode.MANIFEST_ERROR,
						"Declared resource path cannot be transformed to a proper URI: "+url, e);
			}

			files.add(path.toString());
		} else { // can only be PathType.FOLDER now

			Path root = Paths.get(rootPath);

			// No path entries means that we should add all regular files in the folder
			if(pathEntries.isEmpty()) {
				try (DirectoryStream<Path> stream = resourceProvider.children(root, null)) {

					for(Path path : stream) {
						if(!resourceProvider.isDirectory(path)) {
							files.add(path.toString());
						}
					}
				} catch (IOException e) {
					throw new ModelException(GlobalErrorCode.IO_ERROR,
							"Failed to scan root folder for regular files: "+root, e);
				}
			} else {
				// If we have path entries, they represent explicit files or patterns in the host folder

				List<String> patterns = new ArrayList<>();
				Set<String> directFiles = new ObjectOpenHashSet<>();

				for(PathEntry entry : pathEntries) {

					switch (entry.getType()) {

					case FILE: {
						// Explicit file entries get resolved directly
						String value = entry.getValue();
						if(value==null || value.isEmpty())
							throw new ModelException(ManifestErrorCode.MANIFEST_CORRUPTED_STATE,
									"Empty path");

						directFiles.add(value);
						files.add(root.resolve(value).toString());
					} break;

					case PATTERN: {
						// For pattern entries we collect them and wait for a second pass
						patterns.add(entry.getValue());
					} break;

					case RESOURCE: {

					} break;

					default:
						throw new ModelException(ManifestErrorCode.MANIFEST_ERROR,
								"Can only handle FILE or PATTERN path types inside FOLDER: got "+entry.getType());
					}
				}

				// Scan pass on the folder in case we have patterns being defined
				if(!patterns.isEmpty()) {
					Matcher[] matchers = new Matcher[patterns.size()];

					for(int i=0; i<patterns.size(); i++) {
						matchers[i] = Pattern.compile(patterns.get(i)).matcher("");
					}

					Filter<Path> filter = p -> {
						// Ignore links (or any non-regular file) for pattern matching
						if(resourceProvider.isDirectory(p)) {
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
								//FIXME need to evaluate if we should introduce some sort of exclusion flag for entries/pattern
								return true;
							}
						}

						return false;
					};

					// Scan folder content
					try (DirectoryStream<Path> stream = resourceProvider.children(root, null)) {

						for(Path path : stream) {
							if(filter.accept(path)) {
								files.add(path.toString());
							}
						}
					} catch (IOException e) {
						throw new ModelException(GlobalErrorCode.IO_ERROR,
								"Failed to scan root folder for files based on patterns: "+root, e);
					}
				}
			}
		}

		if(files.isEmpty())
			throw new ModelException(ManifestErrorCode.MANIFEST_CORRUPTED_STATE,
					"No valid entries found for file resolver");

		return new DirectPathResolver(files);
	}

	private final String[] paths;

	public DirectPathResolver(List<String> paths) {
		requireNonNull(paths);
		checkArgument(!paths.isEmpty());

		this.paths = paths.toArray(new String[paths.size()]);
	}

	public DirectPathResolver(String...paths) {
		requireNonNull(paths);
		checkArgument(paths.length>0);

		this.paths = paths;
	}

	/**
	 * @see de.ims.icarus2.model.api.io.PathResolver#getPath(int)
	 */
	@Override
	public ResourcePath getPath(int chunkIndex) {
		return new ResourcePath(paths[chunkIndex], LocationType.LOCAL);
	}

	/**
	 * @see de.ims.icarus2.model.api.io.PathResolver#getPathCount()
	 */
	@Override
	public int getPathCount() {
		return paths.length;
	}

	/**
	 * @see de.ims.icarus2.model.api.io.PathResolver#close()
	 */
	@Override
	public void close() {
		// no-op
	}
}
