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
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;

import de.ims.icarus2.model.api.io.resources.IOResource;

/**
 * @author Markus Gärtner
 *
 */
public final class CompoundResourceSet implements ResourceSet {

	private final ResourceSet[] resourceSets;
	private final int[] counts;
	private final int count;

	public CompoundResourceSet(List<ResourceSet> resourceSets) {
		this(resourceSets.toArray(new ResourceSet[0]));
	}

	public CompoundResourceSet(ResourceSet[] fileSets) {
		requireNonNull(fileSets);
		checkArgument("File set empty", fileSets.length>0);

		this.resourceSets = fileSets;
		counts = new int[fileSets.length];

		int count = 0;
		for(int i=0; i<fileSets.length; i++) {
			counts[i] = count;
			count += fileSets[i].getResourceCount();
		}
		this.count = count;
	}


	@Override
	public String toString() {
		return "CompoundResourceSet["+getResourceCount()+" in "+resourceSets.length+" sub-sets]";
	}

	private int fileSetIndex(int fileIndex) {
		int idx = Arrays.binarySearch(counts, fileIndex);
		if(idx < 0) {
			idx = -idx - 2; // normal semantics would be "-idx - 1", but we need the previous bucket
		}

		return idx;
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.sets.ResourceSet#getResourceCount()
	 */
	@Override
	public int getResourceCount() {
		return count;
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.sets.ResourceSet#getResourceAt(int)
	 */
	@Override
	public IOResource getResourceAt(int resourceIndex) {
		int localIndex = fileSetIndex(resourceIndex);
		resourceIndex -= counts[localIndex];

		return resourceSets[localIndex].getResourceAt(resourceIndex);
	}
}
