/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;

/**
 * @author Markus Gärtner
 *
 */
public class ItemInStructureAssert extends AbstractItemAssert<ItemInStructureAssert, Item> {

	private final StructureAssert structure;

	ItemInStructureAssert(StructureAssert structure, Item actual) {
		super(actual, ItemInStructureAssert.class);
		this.structure = requireNonNull(structure);
	}

	public StructureAssert structure() {
		return structure;
	}

	@SuppressWarnings("boxing")
	public ItemInStructureAssert hasIncomingEdgeCount(long expected) {
		isNotNull();
		long value = structure.actual().getIncomingEdgeCount(actual);
		if(value!=expected)
			throw failureWithActualExpected(value, expected, "Expected item %s in structure %s to have %d incoming edges, but got %d", actual, structure.actual(), expected, value);
		return myself;
	}

	@SuppressWarnings("boxing")
	public ItemInStructureAssert hasOutgoingEdgeCount(long expected) {
		isNotNull();
		long value = structure.actual().getOutgoingEdgeCount(actual);
		if(value!=expected)
			throw failureWithActualExpected(value, expected, "Expected item %s in structure %s to have %d outgoing edges, but got %d", actual, structure.actual(), expected, value);
		return myself;
	}

	@SuppressWarnings("boxing")
	public ItemInStructureAssert hasHeight(long expected) {
		isNotNull();
		long value = structure.actual().getHeight(actual);
		if(value!=expected)
			throw failureWithActualExpected(value, expected, "Expected item %s in structure %s to have height %d, but got %d", actual, structure.actual(), expected, value);
		return myself;
	}

	@SuppressWarnings("boxing")
	public ItemInStructureAssert hasDepth(long expected) {
		isNotNull();
		long value = structure.actual().getDepth(actual);
		if(value!=expected)
			throw failureWithActualExpected(value, expected, "Expected item %s in structure %s to have depth %d, but got %d", actual, structure.actual(), expected, value);
		return myself;
	}

	@SuppressWarnings("boxing")
	public ItemInStructureAssert hasDescendantCount(long expected) {
		isNotNull();
		long value = structure.actual().getDescendantCount(actual);
		if(value!=expected)
			throw failureWithActualExpected(value, expected, "Expected item %s in structure %s to have %d descendants, but got %d", actual, structure.actual(), expected, value);
		return myself;
	}

	public ItemInStructureAssert isRoot() {
		isNotNull();
		hasIncomingEdgeCount(1);
		Edge edge = structure.actual().getEdgeAt(actual, 0, false);
		if(edge.getSource()!=structure.actual().getVirtualRoot())
			throw failure("Expected item %s to be a root node in structure %s", actual, structure.actual());
		return myself;
	}

	public ItemInStructureAssert isVirtualRoot() {
		isNotNull();
		if(actual!=structure.actual().getVirtualRoot())
			throw failure("Expected %s to be the virtual root of %s", actual, structure.actual());
		return myself;
	}

	public ItemInStructureAssert isLeaf() {
		isNotNull();
		hasOutgoingEdgeCount(0);
		return myself;
	}

	//TODO
}
