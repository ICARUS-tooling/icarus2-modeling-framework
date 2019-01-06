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
