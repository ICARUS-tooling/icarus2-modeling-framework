/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.structure;

import static de.ims.icarus2.SharedTestUtils.mockSequence;
import static de.ims.icarus2.model.api.ModelTestUtils.assertUnsupportedOperation;
import static de.ims.icarus2.model.api.ModelTestUtils.mockEdge;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.api.ModelTestUtils.mockStructure;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
public interface ImmutableEdgeStorageTest<S extends EdgeStorage>
		extends EdgeStorageTest<S> {

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.EdgeStorage#addEdge(de.ims.icarus2.model.api.members.structure.Structure, long, de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@Test
	default void testAddEdge() {
		assertUnsupportedOperation(() -> create().addEdge(mockStructure(), 0L, mockEdge()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.EdgeStorage#addEdges(de.ims.icarus2.model.api.members.structure.Structure, long, de.ims.icarus2.util.collections.seq.DataSequence)}.
	 */
	@Test
	default void testAddEdges() {
		assertUnsupportedOperation(() -> create().addEdges(
				mockStructure(), 0L, mockSequence(mockEdge())));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.EdgeStorage#removeEdge(de.ims.icarus2.model.api.members.structure.Structure, long)}.
	 */
	@Test
	default void testRemoveEdge() {
		assertUnsupportedOperation(() -> create().removeEdge(mockStructure(), 0L));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.EdgeStorage#removeEdges(de.ims.icarus2.model.api.members.structure.Structure, long, long)}.
	 */
	@Test
	default void testRemoveEdges() {
		assertUnsupportedOperation(() -> create().removeEdges(mockStructure(), 0L, 1L));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.EdgeStorage#swapEdges(de.ims.icarus2.model.api.members.structure.Structure, long, long)}.
	 */
	@Test
	default void testSwapEdges() {
		assertUnsupportedOperation(() -> create().swapEdges(mockStructure(), 0L, 1L));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.EdgeStorage#setTerminal(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Edge, de.ims.icarus2.model.api.members.item.Item, boolean)}.
	 */
	@Test
	default void testSetTerminal() {
		assertUnsupportedOperation(() -> create().setTerminal(
				mockStructure(), mockEdge(), mockItem(), true));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.EdgeStorage#newEdge(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testNewEdge() {
		assertUnsupportedOperation(() -> create().newEdge(
				mockStructure(), mockItem(), mockItem()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.Recyclable#recycle()}.
	 */
	@Override
	@Test
	default void testRecycle() {
		assertUnsupportedOperation(() -> create().recycle());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.Recyclable#revive()}.
	 */
	@Test
	default void testRevive() {
		assertFalse(create().revive());
	}

}
