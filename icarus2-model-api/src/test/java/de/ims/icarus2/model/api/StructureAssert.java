/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.model.api;

import de.ims.icarus2.model.api.members.structure.Structure;

/**
 * @author Markus Gärtner
 *
 */
public class StructureAssert extends AbstractContainerAssert<StructureAssert, Structure> {

	public StructureAssert(Structure actual) {
		super(actual, StructureAssert.class);
	}

	Structure actual() {
		return actual;
	}

	@SuppressWarnings("boxing")
	public ItemInStructureAssert node(long index) {
		isNotNull();
		if(index>=actual.getItemCount())
			throw failure("Node index %d out of bounds. Structure only holds %d nodes.", index, actual.getItemCount());
		return new ItemInStructureAssert(this, actual.getItemAt(index));
	}

	@SuppressWarnings("boxing")
	public EdgeInStructureAssert edge(long index) {
		isNotNull();
		if(index>=actual.getEdgeCount())
			throw failure("Edge index %d out of bounds. Structure only holds %d edges.", index, actual.getEdgeCount());
		return new EdgeInStructureAssert(this, actual.getEdgeAt(index));
	}

	public ItemInStructureAssert root() {
		isNotNull();
		return new ItemInStructureAssert(this, actual.getVirtualRoot());
	}

	public ItemInStructureAssert firstNode() {
		isNotNull();
		return new ItemInStructureAssert(this, actual.getFirstItem());
	}

	public ItemInStructureAssert lastNode() {
		isNotNull();
		return new ItemInStructureAssert(this, actual.getLastItem());
	}

	@SuppressWarnings("boxing")
	public StructureAssert hasEdgeCountOf(long expected) {
		isNotNull();
		long size = actual.getEdgeCount();
		if(size!=expected)
			throw failureWithActualExpected(size, expected, "Expected structure %s to have %d edges, but got %d", actual, expected, size);
		return myself;
	}

	//TODO
}
