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
/**
 *
 */
package de.ims.icarus2.model.api.view.paged;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.util.ChangeableTest;
import de.ims.icarus2.util.PartTest;

/**
 * @author Markus Gärtner
 *
 */
@Disabled //TODO enable and implement tests
public interface CorpusModelTest<M extends CorpusModel>
		extends PartTest<PagedCorpusView, M>, ChangeableTest<M> {

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#isModelEditable()}.
	 */
	@Test
	default void testIsModelEditable() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#isModelComplete()}.
	 */
	@Test
	default void testIsModelComplete() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#isModelActive()}.
	 */
	@Test
	default void testIsModelActive() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getCorpus()}.
	 */
	@Test
	default void testGetCorpus() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getView()}.
	 */
	@Test
	default void testGetView() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getMemberType(de.ims.icarus2.model.api.members.CorpusMember)}.
	 */
	@Test
	default void testGetMemberType() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#isItem(de.ims.icarus2.model.api.members.CorpusMember)}.
	 */
	@Test
	default void testIsItem() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#isContainer(de.ims.icarus2.model.api.members.CorpusMember)}.
	 */
	@Test
	default void testIsContainer() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#isStructure(de.ims.icarus2.model.api.members.CorpusMember)}.
	 */
	@Test
	default void testIsStructure() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#isEdge(de.ims.icarus2.model.api.members.CorpusMember)}.
	 */
	@Test
	default void testIsEdge() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#isFragment(de.ims.icarus2.model.api.members.CorpusMember)}.
	 */
	@Test
	default void testIsFragment() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#isLayer(de.ims.icarus2.model.api.members.CorpusMember)}.
	 */
	@Test
	default void testIsLayer() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getSize(de.ims.icarus2.model.api.layer.ItemLayer)}.
	 */
	@Test
	default void testGetSize() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getRootContainer(de.ims.icarus2.model.api.layer.ItemLayer)}.
	 */
	@Test
	default void testGetRootContainerItemLayer() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getRootContainer()}.
	 */
	@Test
	default void testGetRootContainer() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getItem(de.ims.icarus2.model.api.layer.ItemLayer, long)}.
	 */
	@Test
	default void testGetItemItemLayerLong() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getContainer(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testGetContainer() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getLayer(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testGetLayer() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getIndex(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testGetIndex() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getBeginOffset(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testGetBeginOffset() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getEndOffset(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testGetEndOffset() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#isVirtual(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testIsVirtual() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getContainerType(de.ims.icarus2.model.api.members.container.Container)}.
	 */
	@Test
	default void testGetContainerType() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getBaseContainers(de.ims.icarus2.model.api.members.container.Container)}.
	 */
	@Test
	default void testGetBaseContainers() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getBoundaryContainer(de.ims.icarus2.model.api.members.container.Container)}.
	 */
	@Test
	default void testGetBoundaryContainer() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getItemCount(de.ims.icarus2.model.api.members.container.Container)}.
	 */
	@Test
	default void testGetItemCount() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getItemAt(de.ims.icarus2.model.api.members.container.Container, long)}.
	 */
	@Test
	default void testGetItemAt() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testIndexOfItem() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#containsItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testContainsItem() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#addItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testAddItemContainerItem() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#addItem(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testAddItemContainerLongItem() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#addItems(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.util.collections.seq.DataSequence)}.
	 */
	@Test
	default void testAddItems() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#removeItem(de.ims.icarus2.model.api.members.container.Container, long)}.
	 */
	@Test
	default void testRemoveItemContainerLong() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#removeItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testRemoveItemContainerItem() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#removeItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
	 */
	@Test
	default void testRemoveItems() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#moveItem(de.ims.icarus2.model.api.members.container.Container, long, long)}.
	 */
	@Test
	default void testMoveItem() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getStructureType(de.ims.icarus2.model.api.members.structure.Structure)}.
	 */
	@Test
	default void testGetStructureType() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure)}.
	 */
	@Test
	default void testGetEdgeCountStructure() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getEdgeAt(de.ims.icarus2.model.api.members.structure.Structure, long)}.
	 */
	@Test
	default void testGetEdgeAtStructureLong() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#indexOfEdge(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@Test
	default void testIndexOfEdge() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#containsEdge(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@Test
	default void testContainsEdge() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testGetEdgeCountStructureItem() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, boolean)}.
	 */
	@Test
	default void testGetEdgeCountStructureItemBoolean() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getEdgeAt(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, long, boolean)}.
	 */
	@Test
	default void testGetEdgeAtStructureItemLongBoolean() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getParent(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testGetParent() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#indexOfChild(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testIndexOfChild() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getSiblingAt(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, long)}.
	 */
	@Test
	default void testGetSiblingAt() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getHeight(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testGetHeight() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getDepth(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testGetDepth() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getDescendantCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testGetDescendantCount() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getVirtualRoot(de.ims.icarus2.model.api.members.structure.Structure)}.
	 */
	@Test
	default void testGetVirtualRoot() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#isRoot(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testIsRoot() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#addEdge(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@Test
	default void testAddEdgeStructureEdge() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#addEdge(de.ims.icarus2.model.api.members.structure.Structure, long, de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@Test
	default void testAddEdgeStructureLongEdge() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#addEdges(de.ims.icarus2.model.api.members.structure.Structure, long, de.ims.icarus2.util.collections.seq.DataSequence)}.
	 */
	@Test
	default void testAddEdges() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#removeEdge(de.ims.icarus2.model.api.members.structure.Structure, long)}.
	 */
	@Test
	default void testRemoveEdgeStructureLong() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#removeEdge(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@Test
	default void testRemoveEdgeStructureEdge() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#removeEdges(de.ims.icarus2.model.api.members.structure.Structure, long, long)}.
	 */
	@Test
	default void testRemoveEdges() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#moveEdge(de.ims.icarus2.model.api.members.structure.Structure, long, long)}.
	 */
	@Test
	default void testMoveEdge() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#setTerminal(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Edge, de.ims.icarus2.model.api.members.item.Item, boolean)}.
	 */
	@Test
	default void testSetTerminal() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getStructure(de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@Test
	default void testGetStructure() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getSource(de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@Test
	default void testGetSource() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getTarget(de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@Test
	default void testGetTarget() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#setSource(de.ims.icarus2.model.api.members.item.Edge, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testSetSource() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#setTarget(de.ims.icarus2.model.api.members.item.Edge, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testSetTarget() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getItem(de.ims.icarus2.model.api.members.item.Fragment)}.
	 */
	@Test
	default void testGetItemFragment() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getFragmentBegin(de.ims.icarus2.model.api.members.item.Fragment)}.
	 */
	@Test
	default void testGetFragmentBegin() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getFragmentEnd(de.ims.icarus2.model.api.members.item.Fragment)}.
	 */
	@Test
	default void testGetFragmentEnd() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#setFragmentBegin(de.ims.icarus2.model.api.members.item.Fragment, de.ims.icarus2.model.api.raster.Position)}.
	 */
	@Test
	default void testSetFragmentBegin() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#setFragmentEnd(de.ims.icarus2.model.api.members.item.Fragment, de.ims.icarus2.model.api.raster.Position)}.
	 */
	@Test
	default void testSetFragmentEnd() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#collectKeys(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.util.function.Consumer)}.
	 */
	@Test
	default void testCollectKeys() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getIntegerValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetIntegerValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getLongValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetLongValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getFloatValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetFloatValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getDoubleValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetDoubleValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getBooleanValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetBooleanValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#setValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.Object)}.
	 */
	@Test
	default void testSetValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#setIntegerValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String, int)}.
	 */
	@Test
	default void testSetIntegerValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#setLongValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String, long)}.
	 */
	@Test
	default void testSetLongValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#setFloatValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String, float)}.
	 */
	@Test
	default void testSetFloatValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#setDoubleValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String, double)}.
	 */
	@Test
	default void testSetDoubleValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#setBooleanValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String, boolean)}.
	 */
	@Test
	default void testSetBooleanValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#hasAnnotations(de.ims.icarus2.model.api.layer.AnnotationLayer)}.
	 */
	@Test
	default void testHasAnnotationsAnnotationLayer() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#hasAnnotations(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testHasAnnotationsAnnotationLayerItem() {
		fail("Not yet implemented"); // TODO
	}

}
