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

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.StructureFlag;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.mem.Assessable;
import de.ims.icarus2.util.mem.Reference;
import de.ims.icarus2.util.mem.ReferenceType;

/**
 * Implements an {@link Item} that is suitable for serving as the virtual
 * root node of a {@link Structure}. The implementation manages the edges
 * directly attached to the root to allow quick lookups without the host
 * structure having to do the storage work.
 * <p>
 * TODO tell about the 3 specialized subclasses
 *
 * @author Markus Gärtner
 *
 */
@Assessable
public abstract class RootItem<E extends Edge> implements Item, NodeInfo {

	public static <E extends Edge> RootItem<E> forStructure(Structure structure) {
		return forManifest(structure.getManifest());
	}

	public static <E extends Edge> RootItem<E> forManifest(StructureManifest manifest) {

		if(manifest.isStructureFlagSet(StructureFlag.MULTI_ROOT)) {
			return new RootItem.MultiEdgeRootItem<>();
		} else {
			return new RootItem.SingleEdgeRootItem<>();
		}
	}

	@Reference(ReferenceType.UPLINK)
	private Structure owner;

	public RootItem(Structure owner) {
		setStructure(owner);
	}

	public RootItem() {
		// no-op
	}

	public void setStructure(Structure structure) {
		requireNonNull(structure);
		if (owner !=null)
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_LINKING, "Owning structure already set");

		this.owner = structure;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation always returns {@code false}.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#isTopLevel()
	 */
	@Override
	public boolean isTopLevel() {
		return false;
	}

	public abstract int getEdgeCount();

	public abstract E getEdgeAt(int index);

	public abstract void addEdge(E edge);

	public abstract void removeEdge(E edge);

	public abstract int indexOfEdge(Edge edge);

	@Override
	public long edgeCount() {
		return getEdgeCount();
	}

	private static void checkNotIncoming(boolean incoming) {
		if(incoming)
			throw new ModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
					"Virtual root item cannot have incoming edges");
	}

	@Override
	public long edgeCount(boolean incoming) {
		checkNotIncoming(incoming);

		return getEdgeCount();
	}

	@Override
	public Edge edgeAt(long index, boolean incoming) {
		checkNotIncoming(incoming);

		return getEdgeAt(IcarusUtils.ensureIntegerValueRange(index));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addEdge(Edge edge, boolean incoming) {
		checkNotIncoming(incoming);

		addEdge((E)edge);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void removeEdge(Edge edge, boolean incoming) {
		checkNotIncoming(incoming);

		removeEdge((E)edge);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.ITEM;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getContainer()
	 */
	@Override
	public Structure getContainer() {
		return owner;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getIndex()
	 */
	@Override
	public long getIndex() {
		return IcarusUtils.UNSET_LONG;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getId()
	 */
	@Override
	public long getId() {
		return IcarusUtils.UNSET_LONG;
	}

	/**
	 * Root item mirrors state of owner.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#isAlive()
	 */
	@Override
	public boolean isAlive() {
		return owner.isAlive();
	}

	/**
	 * Root item mirrors state of owner.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#isLocked()
	 */
	@Override
	public boolean isLocked() {
		return owner.isLocked();
	}

	/**
	 * Root item mirrors state of owner.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return owner.isDirty();
	}

	/**
	 * Implements a root item that cannot have edges, i.e. which is
	 * always empty.
	 *
	 * @author Markus Gärtner
	 *
	 * @param <E>
	 */
	public static class EmptyRootItem<E extends Edge> extends RootItem<E> {

		public EmptyRootItem(Structure owner) {
			super(owner);
		}

		public EmptyRootItem() {
			super();
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.RootItem#getEdgeCount()
		 */
		@Override
		public int getEdgeCount() {
			return 0;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.RootItem#getEdgeAt(int)
		 */
		@Override
		public E getEdgeAt(int index) {
			throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					"No root edge available for index: "+index);
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.RootItem#addEdge(de.ims.icarus2.model.api.members.item.Edge)
		 */
		@Override
		public void addEdge(E edge) {
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					"Cannot add edge to empty root item: "+getName(edge));
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.RootItem#removeEdge(de.ims.icarus2.model.api.members.item.Edge)
		 */
		@Override
		public void removeEdge(E edge) {
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					"Cannot remove edge from empty root item: "+getName(edge));
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.RootItem#indexOfEdge(de.ims.icarus2.model.api.members.item.Edge)
		 */
		@Override
		public int indexOfEdge(Edge edge) {
			return UNSET_INT;
		}

	}

	/**
	 * Root item with at most one edge.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class SingleEdgeRootItem<E extends Edge> extends RootItem<E> {

		private E edge;

		public SingleEdgeRootItem(Structure owner) {
			super(owner);
		}

		public SingleEdgeRootItem() {
			super();
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.RootItem#getEdgeCount()
		 */
		@Override
		public int getEdgeCount() {
			return edge==null ? 0 : 1;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.RootItem#getEdgeAt(int)
		 */
		@Override
		public E getEdgeAt(int index) {
			E edge = this.edge;
			if(edge==null || index!=0)
				throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
						"No root edge available for index: "+index);

			return edge;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.RootItem#addEdge(de.ims.icarus2.model.api.members.item.Edge)
		 */
		@Override
		public void addEdge(E edge) {
			requireNonNull(edge);

			if(this.edge!=null)
				throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER, "Singleton edge already set - cannot add "+ModelUtils.getName(edge));

			this.edge = edge;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.RootItem#removeEdge(de.ims.icarus2.model.api.members.item.Edge)
		 */
		@Override
		public void removeEdge(E edge) {
			requireNonNull(edge);

			if(edge!=this.edge)
				throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER, "Unknown edge - cannot remove "+ModelUtils.getName(edge));

			this.edge = null;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.RootItem#indexOfEdge(de.ims.icarus2.model.api.members.item.Edge)
		 */
		@Override
		public int indexOfEdge(Edge edge) {
			requireNonNull(edge);
			return this.edge==edge ? 0 : UNSET_INT;
		}

	}

	/**
	 * Root item that can have multiple edges stored in a {@link List}.
	 * <p>
	 * Note that this implementation does not provide any additional
	 * help for the {@link #indexOfEdge(Edge)} lookup method. Thereby
	 * it is possible to end up with bad performance for very large
	 * numbers of edged connected to the root node when the aforementioned
	 * method is called frequently.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class MultiEdgeRootItem<E extends Edge> extends RootItem<E> {

		public static final int DEFAULT_CAPACITY = 5;

		private final List<E> edges;

		public MultiEdgeRootItem(Structure owner) {
			this(owner, UNSET_INT);
		}

		public MultiEdgeRootItem() {
			this(UNSET_INT);
		}

		public MultiEdgeRootItem(int capacity) {
			super();

			edges = createEdgesBuffer(null, capacity);
		}

		public MultiEdgeRootItem(Structure owner, int capacity) {
			super(owner);

			edges = createEdgesBuffer(owner, capacity);
		}

		private List<E> createEdgesBuffer(Structure owner, int capacity) {
			checkArgument(capacity==UNSET_INT || capacity>0);

			if(capacity==UNSET_INT) {
				capacity = DEFAULT_CAPACITY;
			}

			return new ArrayList<>(3);
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.RootItem#getEdgeCount()
		 */
		@Override
		public int getEdgeCount() {
			return edges.size();
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.RootItem#getEdgeAt(int)
		 */
		@Override
		public E getEdgeAt(int index) {
			return edges.get(index);
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.RootItem#addEdge(de.ims.icarus2.model.api.members.item.Edge)
		 */
		@Override
		public void addEdge(E edge) {
			requireNonNull(edge);
			if(edges.contains(edge))
				throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER, "Edge already present: "+ModelUtils.getName(edge));

			edges.add(edge);
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.RootItem#removeEdge(de.ims.icarus2.model.api.members.item.Edge)
		 */
		@Override
		public void removeEdge(E edge) {
			requireNonNull(edge);
			if(!edges.remove(edge))
				throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER, "Cannot remove unknown edge: "+ModelUtils.getName(edge));
		}

		public void removeAllEdges() {
			edges.clear();
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.RootItem#indexOfEdge(de.ims.icarus2.model.api.members.item.Edge)
		 */
		@Override
		public int indexOfEdge(Edge edge) {
			requireNonNull(edge);
			return edges.indexOf(edge);
		}

	}
}
