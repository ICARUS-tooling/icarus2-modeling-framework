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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/driver/io/sets/SingletonFileSet.java $
 *
 * $LastChangedDate: 2015-12-18 14:36:38 +0100 (Fr, 18 Dez 2015) $
 * $LastChangedRevision: 440 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.filedriver.io.sets;

import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.nio.file.Path;

/**
 * @author Markus Gärtner
 * @version $Id: SingletonFileSet.java 440 2015-12-18 13:36:38Z mcgaerty $
 *
 */
public final class SingletonFileSet implements FileSet {

	private final Path file;

	/**
	 * Creates a new {@code SingletonFileSet} that points to the given {@code file}.
	 *
	 * @param file
	 * @param storage
	 */
	public SingletonFileSet(Path file) {
		checkNotNull(file);

		this.file = file;
	}

	@Override
	public String toString() {
		return "SingletonFileSet [path="+file+"]";
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.sets.FileSet#getFileCount()
	 */
	@Override
	public int getFileCount() {
		return 1;
	}

	private void checkIndex(int fileIndex) {
		if(fileIndex!=0)
			throw new IllegalArgumentException("Invalid file index: "+fileIndex+" - only legal value is 0"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.sets.FileSet#getFileAt(int)
	 */
	@Override
	public Path getFileAt(int fileIndex) {
		checkIndex(fileIndex);

		return file;
	}
}
