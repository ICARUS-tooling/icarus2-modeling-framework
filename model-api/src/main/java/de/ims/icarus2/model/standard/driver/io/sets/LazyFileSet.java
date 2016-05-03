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

 * $Revision: 440 $
 * $Date: 2015-12-18 14:36:38 +0100 (Fr, 18 Dez 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/driver/io/sets/LazyFileSet.java $
 *
 * $LastChangedDate: 2015-12-18 14:36:38 +0100 (Fr, 18 Dez 2015) $
 * $LastChangedRevision: 440 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.driver.io.sets;

import static de.ims.icarus2.model.util.Conditions.checkNotNull;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.nio.file.Path;
import java.nio.file.Paths;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.manifest.LocationType;
import de.ims.icarus2.model.io.PathResolver;
import de.ims.icarus2.model.io.ResourcePath;

/**
 * Implements a {@link FileSet} that is linked to a central storage file and
 * manages a collection of target files that are resolved with the help of a
 * {@link PathResolver}, provided at construction time. The implementation
 * resolves paths to target files lazily, as soon as they are requested. The
 * same policy holds true for the corresponding checksums.
 *
 * @author Markus Gärtner
 * @version $Id: LazyFileSet.java 440 2015-12-18 13:36:38Z mcgaerty $
 *
 */
public class LazyFileSet implements FileSet {

	private final TIntObjectMap<Path> paths = new TIntObjectHashMap<>();
	private final PathResolver pathResolver;

	/**
	 * Creates a new {@code SingletonFileSet} that points to the given {@code file}
	 * and uses the provided {@link ChecksumStorage} to store the checksum.
	 *
	 * @param pathResolver
	 * @param storage
	 */
	public LazyFileSet(PathResolver pathResolver) {
		checkNotNull(pathResolver);

		this.pathResolver = pathResolver;
	}

	@Override
	public String toString() {
		return getClass().getName()+"["+getFileCount()+" files]";
	}

	/**
	 * @see de.ims.icarus2.model.standard.driver.io.sets.FileSet#getFileCount()
	 */
	@Override
	public int getFileCount() {
		return pathResolver.getPathCount();
	}

	/**
	 * @see de.ims.icarus2.model.standard.driver.io.sets.FileSet#getFileAt(int)
	 */
	@Override
	public synchronized Path getFileAt(int fileIndex) {
		Path path = paths.get(fileIndex);
		if(path==null) {
			ResourcePath resourcePath = pathResolver.getPath(fileIndex);
			if(resourcePath==null)
				throw new ModelException(ModelErrorCode.INVALID_INPUT,
						"No file available for index: "+fileIndex); //$NON-NLS-1$
			if(resourcePath.getType()!=LocationType.LOCAL)
				throw new ModelException(ModelErrorCode.ILLEGAL_STATE,
						"Resolver returned unsupported path type: "+resourcePath);//TODO more info in exception //$NON-NLS-1$

			path = Paths.get(resourcePath.getPath());

			// Decision whether or not file paths should be absolute or relative is to be made on client code level!!!
//			if(!path.isAbsolute()) {
//				path = path.toAbsolutePath();
//			}

			paths.put(fileIndex, path);
		}

		return path;
	}
}
