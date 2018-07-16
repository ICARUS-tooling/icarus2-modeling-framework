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

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.ObjIntConsumer;

import de.ims.icarus2.model.api.io.resources.IOResource;

/**
 * Stores a collection of (file) resources together with their respective checksums.
 *
 * @author Markus Gärtner
 *
 */
public interface ResourceSet {

	/**
	 * Returns the number of resources this storage covers (at least {@code 1}).
	 */
	int getResourceCount();

	/**
	 * Returns the abstract location of the resource specified by the
	 * {@code resourceIndex} argument.
	 *
	 * @param resourceIndex
	 * @return
	 */
	IOResource getResourceAt(int resourceIndex);

	default int indexOfFile(IOResource file) {
		int resourceCount = getResourceCount();

		for(int i=0; i<resourceCount; i++) {
			if(getResourceAt(i).equals(file)) {
				return i;
			}
		}

		return -1;
	}

	default void forEachFile(IntConsumer action) {
		int resourceCount = getResourceCount();

		for(int i=0; i<resourceCount; i++) {
			action.accept(i);
		}
	}

	default void forEachFile(ObjIntConsumer<IOResource> action) {
		int resourceCount = getResourceCount();

		for(int i=0; i<resourceCount; i++) {
			action.accept(getResourceAt(i), i);
		}
	}

	default void forEachFile(Consumer<IOResource> action) {
		int resourceCount = getResourceCount();

		for(int i=0; i<resourceCount; i++) {
			action.accept(getResourceAt(i));
		}
	}
}
