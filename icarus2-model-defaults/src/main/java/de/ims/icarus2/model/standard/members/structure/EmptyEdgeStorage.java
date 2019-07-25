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
package de.ims.icarus2.model.standard.members.structure;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.annotations.TestableImplementation;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(EdgeStorage.class)
public class EmptyEdgeStorage extends AbstractImmutableEdgeStorage {

	private RootItem<Edge> root;

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#addNotify(de.ims.icarus2.model.api.members.structure.Structure)
	 */
	@Override
	public void addNotify(Structure context) {
		root = new RootItem.EmptyRootItem<>(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#removeNotify(de.ims.icarus2.model.api.members.structure.Structure)
	 */
	@Override
	public void removeNotify(Structure context) {
		root = null;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getStructureType()
	 */
	@Override
	public StructureType getStructureType() {
		return StructureType.SET;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure)
	 */
	@Override
	public long getEdgeCount(Structure context) {
		return 0L;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeAt(de.ims.icarus2.model.api.members.structure.Structure, long)
	 */
	@Override
	public Edge getEdgeAt(Structure context, long index) {
		throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS, "Edge storage is empty");
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#indexOfEdge(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Edge)
	 */
	@Override
	public long indexOfEdge(Structure context, Edge edge) {
		return IcarusUtils.UNSET_LONG;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long getEdgeCount(Structure context, Item node) {
		return 0L;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, boolean)
	 */
	@Override
	public long getEdgeCount(Structure context, Item node, boolean isSource) {
		return 0L;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeAt(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, long, boolean)
	 */
	@Override
	public Edge getEdgeAt(Structure context, Item node, long index,
			boolean isSource) {
		throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS, "Edge storage is empty");
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getVirtualRoot(de.ims.icarus2.model.api.members.structure.Structure)
	 */
	@Override
	public Item getVirtualRoot(Structure context) {
		return root;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#isRoot(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public boolean isRoot(Structure context, Item node) {
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getParent(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public Item getParent(Structure context, Item node) {
		return null;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#indexOfChild(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long indexOfChild(Structure context, Item child) {
		return IcarusUtils.UNSET_LONG;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getSiblingAt(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, long)
	 */
	@Override
	public Item getSiblingAt(Structure context, Item child, long offset) {
		throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS, "Edge storage is empty");
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getHeight(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long getHeight(Structure context, Item node) {
		if(node!=root)
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER, "Edge storage is empty");

		return 0;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getDepth(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long getDepth(Structure context, Item node) {
		return IcarusUtils.UNSET_LONG;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getDescendantCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long getDescendantCount(Structure context, Item parent) {
		return 0;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		// just to make sure
		root = null;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return true;
	}

}
