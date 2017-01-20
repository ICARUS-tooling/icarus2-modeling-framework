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
 */
package de.ims.icarus2.filedriver.io.sets;

import static java.util.Objects.requireNonNull;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.io.PathResolver;
import de.ims.icarus2.model.api.io.ResourcePath;
import de.ims.icarus2.model.api.io.resources.FileResource;
import de.ims.icarus2.model.api.io.resources.IOResource;
import de.ims.icarus2.model.api.io.resources.ReadOnlyURLResource;

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
