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
 */
package de.ims.icarus2.filedriver.io.sets;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.ObjIntConsumer;

/**
 * Stores a collection of files together with their respective checksums.
 *
 * @author Markus Gärtner
 *
 */
public interface FileSet {

	/**
	 * Returns the number of files this storage covers (at least {@code 1}).
	 */
	int getFileCount();

	/**
	 * Returns the physical location of the file specified by the {@code fileIndex}
	 * argument.
	 *
	 * @param fileIndex
	 * @return
	 */
	Path getFileAt(int fileIndex);

	default void forEachFile(IntConsumer action) {
		int fileCount = getFileCount();

		for(int i=0; i<fileCount; i++) {
			action.accept(i);
		}
	}

	default void forEachFile(ObjIntConsumer<Path> action) {
		int fileCount = getFileCount();

		for(int i=0; i<fileCount; i++) {
			action.accept(getFileAt(i), i);
		}
	}

	default void forEachFile(Consumer<Path> action) {
		int fileCount = getFileCount();

		for(int i=0; i<fileCount; i++) {
			action.accept(getFileAt(i));
		}
	}
}
