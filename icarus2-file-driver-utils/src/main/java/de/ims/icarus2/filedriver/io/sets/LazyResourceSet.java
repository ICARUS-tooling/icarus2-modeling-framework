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
package de.ims.icarus2.filedriver.io.sets;

import static java.util.Objects.requireNonNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.io.PathResolver;
import de.ims.icarus2.model.api.io.ResourcePath;
import de.ims.icarus2.util.io.resource.FileResource;
import de.ims.icarus2.util.io.resource.IOResource;
import de.ims.icarus2.util.io.resource.ReadOnlyURLResource;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Implements a {@link ResourceSet} that is linked to a central storage file and
 * manages a collection of target files that are resolved with the help of a
 * {@link PathResolver}, provided at construction time. The implementation
 * resolves resources to target files lazily, as soon as they are requested. The
 * same policy holds true for the corresponding checksums.
 *
 * @author Markus Gärtner
 *
 */
public class LazyResourceSet implements ResourceSet {

	private final Int2ObjectMap<IOResource> resources = new Int2ObjectOpenHashMap<>();
	private final PathResolver pathResolver;

	/**
	 * @param pathResolver
	 * @param storage
	 */
	public LazyResourceSet(PathResolver pathResolver) {
		requireNonNull(pathResolver);

		this.pathResolver = pathResolver;
	}

	@Override
	public String toString() {
		return getClass().getName()+"["+getResourceCount()+" resources]";
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.sets.ResourceSet#getResourceCount()
	 */
	@Override
	public int getResourceCount() {
		return pathResolver.getPathCount();
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.sets.ResourceSet#getResourceAt(int)
	 */
	@Override
	public synchronized IOResource getResourceAt(int resourceIndex) {
		IOResource resource = resources.get(resourceIndex);
		if(resource==null) {
			ResourcePath resourcePath = pathResolver.getPath(resourceIndex);
			if(resourcePath==null)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT,
						"No resource available for index: "+resourceIndex); //$NON-NLS-1$

			switch (resourcePath.getType()) {
			case LOCAL:
				resource = new FileResource(Paths.get(resourcePath.getPath()));
				break;

			case REMOTE:
				try {
					resource = new ReadOnlyURLResource(new URL(resourcePath.getPath()));
				} catch (MalformedURLException e) {
					throw new ModelException(ModelErrorCode.DRIVER_METADATA_CORRUPTED,
							"Stored path is not a valid URL: "+resourcePath.getPath());
				}
				break;

			default:
				throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
						"Resolver returned unsupported path type: "+resourcePath);//TODO more info in exception //$NON-NLS-1$
			}

			resources.put(resourceIndex, resource);
		}

		return resource;
	}
}
