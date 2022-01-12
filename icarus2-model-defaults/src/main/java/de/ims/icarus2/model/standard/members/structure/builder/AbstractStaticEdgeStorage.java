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
package de.ims.icarus2.model.standard.members.structure.builder;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.members.structure.AbstractImmutableEdgeStorage;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.LookupList;

/**
 * @author Markus Gärtner
 *
 * @param <R> type of the root node used for the structure
 *
 */
public abstract class AbstractStaticEdgeStorage<R extends Item> extends AbstractImmutableEdgeStorage {

	protected final R virtualRoot;

	protected final LookupList<Edge> edges;

	public AbstractStaticEdgeStorage(R root, LookupList<Edge> edges) {
		requireNonNull(root);
		requireNonNull(edges);

		this.virtualRoot = root;
		this.edges = edges;
	}

	@Override
	public void recycle() {
		throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Cannot recycle static edge storage");
	}

	@Override
	public boolean revive() {
		return false;
	}

	@Override
	public void addNotify(@Nullable Structure context) {
		// no-op
	}

	@Override
	public void removeNotify(@Nullable Structure context) {
		// no-op
	}

	@Override
	public Item getVirtualRoot(@Nullable Structure context) {
		return virtualRoot;
	}

	@Override
	public boolean isRoot(Structure context, Item node) {
		requireNonNull(node);
		return node != virtualRoot && getParent(context, node)==virtualRoot;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure)
	 */
	@Override
	public long getEdgeCount(@Nullable Structure context) {
		return edges.size();
	}

	protected Edge getEdgeAt(int index) {
		if(index<0 || index>=edges.size())
			throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					Messages.outOfBounds("Invalid index", index, 0, edges.size()-1));
		return edges.get(index);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeAt(de.ims.icarus2.model.api.members.structure.Structure, long)
	 */
	@Override
	public Edge getEdgeAt(@Nullable Structure context, long index) {
		return getEdgeAt(IcarusUtils.ensureIntegerValueRange(index));
	}

	protected int indexOfEdge(Edge edge) {
		requireNonNull(edge);
		return edges.indexOf(edge);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#indexOfEdge(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Edge)
	 */
	@Override
	public long indexOfEdge(@Nullable Structure context, Edge edge) {
		return indexOfEdge(edge);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long getEdgeCount(Structure context, Item node) {
		requireNonNull(node);
		return getEdgeCount(context, node, true) + getEdgeCount(context, node, false);
	}

}
