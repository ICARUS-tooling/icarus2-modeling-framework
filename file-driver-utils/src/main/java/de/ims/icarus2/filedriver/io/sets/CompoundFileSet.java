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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * @author Markus Gärtner
 *
 */
public final class CompoundFileSet implements FileSet {

	private final FileSet[] fileSets;
	private final int[] counts;
	private final int count;

	public CompoundFileSet(List<FileSet> fileSets) {
		this(fileSets.toArray(new FileSet[0]));
	}

	public CompoundFileSet(FileSet[] fileSets) {
		checkNotNull(fileSets);
		checkArgument(fileSets.length>0);

		this.fileSets = fileSets;
		counts = new int[fileSets.length];

		int count = 0;
		for(int i=0; i<fileSets.length; i++) {
			counts[i] = count;
			count += fileSets[i].getFileCount();
		}
		this.count = count;
	}


	@Override
	public String toString() {
		return "CompoundFileSet["+getFileCount()+" in "+fileSets.length+" sub-sets]";
	}

	private int fileSetIndex(int fileIndex) {
		int idx = Arrays.binarySearch(counts, fileIndex);
		if(idx < 0) {
			idx = -idx - 2; // normal semantics would be "-idx - 1", but we need the previous bucket
		}

		return idx;
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.sets.FileSet#getFileCount()
	 */
	@Override
	public int getFileCount() {
		return count;
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.sets.FileSet#getFileAt(int)
	 */
	@Override
	public Path getFileAt(int fileIndex) {
		int localIndex = fileSetIndex(fileIndex);
		fileIndex -= counts[localIndex];

		return fileSets[localIndex].getFileAt(fileIndex);
	}
}
