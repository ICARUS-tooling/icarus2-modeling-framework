/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 448 $
 * $Date: 2016-01-19 17:30:06 +0100 (Di, 19 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/structure/RootItem.java $
 *
 * $LastChangedDate: 2016-01-19 17:30:06 +0100 (Di, 19 Jan 2016) $
 * $LastChangedRevision: 448 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.structure;

import static de.ims.icarus2.model.standard.util.CorpusUtils.ensureIntegerValueRange;
import static de.ims.icarus2.model.standard.util.CorpusUtils.getName;
import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.StructureFlag;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.standard.util.CorpusUtils;
import de.ims.icarus2.util.mem.HeapMember;
import de.ims.icarus2.util.mem.Reference;
import de.ims.icarus2.util.mem.ReferenceType;

/**
 * Implements an {@link Item} that is suitable for serving as the virtual
 * root node of a {@link Structure}. The implementation manages the edges
 * directly attached to the root to allow quick lookups without the host
 * structure having to do the storage work.
 * <p>
 * TODO rell about the 2 specialized subclasses
 *
 * @author Markus Gärtner
 * @version $Id: RootItem.java 448 2016-01-19 16:30:06Z mcgaerty $
 *
 */
@HeapMember
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
		if (owner !=null)
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_LINKING, "Owning structure already set");
		checkNotNull(structure);

		this.owner = structure;
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

		return getEdgeAt(ensureIntegerValueRange(index));
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
	 * @see de.ims.icarus2.model.api.members.CorpusMember#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return getContainer().getCorpus();
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
	 * @see de.ims.icarus2.model.api.members.item.Item#getLayer()
	 */
	@Override
	public ItemLayer getLayer() {
		return getContainer().getLayer();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getBeginOffset()
	 */
	@Override
	public long getBeginOffset() {
		return -1;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getEndOffset()
	 */
	@Override
	public long getEndOffset() {
		return -1;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getIndex()
	 */
	@Override
	public long getIndex() {
		return -1;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#setIndex(long)
	 */
	@Override
	public void setIndex(long newIndex) {
		throw new UnsupportedOperationException("ROOT nodes cannot have index values assigned"); //$NON-NLS-1$
	}

	/**
	 * Root item is always alive.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#isAlive()
	 */
	@Override
	public boolean isAlive() {
		return owner.isAlive();
	}

	/**
	 * Root item can never be locked.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#isLocked()
	 */
	@Override
	public boolean isLocked() {
		return owner.isLocked();
	}

	/**
	 * Root item can never be dirty.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return owner.isDirty();
	}

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
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER, "Cannot add edge to empty root item: "+getName(edge));
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.RootItem#removeEdge(de.ims.icarus2.model.api.members.item.Edge)
		 */
		@Override
		public void removeEdge(E edge) {
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER, "Cannot remove edge from empty root item: "+getName(edge));
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.RootItem#indexOfEdge(de.ims.icarus2.model.api.members.item.Edge)
		 */
		@Override
		public int indexOfEdge(Edge edge) {
			return -1;
		}

	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id: RootItem.java 448 2016-01-19 16:30:06Z mcgaerty $
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
			if(this.edge!=null)
				throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER, "Singleton edge already set - cannot add "+CorpusUtils.getName(edge));

			this.edge = edge;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.RootItem#removeEdge(de.ims.icarus2.model.api.members.item.Edge)
		 */
		@Override
		public void removeEdge(E edge) {
			if(edge!=this.edge)
				throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER, "Unknown edge - cannot remove "+CorpusUtils.getName(edge));

			this.edge = null;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.RootItem#indexOfEdge(de.ims.icarus2.model.api.members.item.Edge)
		 */
		@Override
		public int indexOfEdge(Edge edge) {
			return this.edge==edge ? 0 : -1;
		}

	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id: RootItem.java 448 2016-01-19 16:30:06Z mcgaerty $
	 *
	 */
	public static class MultiEdgeRootItem<E extends Edge> extends RootItem<E> {

		public static final int DEFAULT_CAPACITY = 5;

		private final List<E> edges;

		public MultiEdgeRootItem(Structure owner) {
			this(owner, -1);
		}

		public MultiEdgeRootItem() {
			this(-1);
		}

		public MultiEdgeRootItem(int capacity) {
			super();

			edges = createEdgesBuffer(null, capacity);
		}

		public MultiEdgeRootItem(Structure owner, int capacity) {
			super(owner);

			edges = createEdgesBuffer(owner, capacity);
		}

		protected List<E> createEdgesBuffer(Structure owner, int capacity) {
			if(capacity<0) {
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
			if(edges.contains(edge))
				throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER, "Edge already present: "+CorpusUtils.getName(edge));

			edges.add(edge);
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.RootItem#removeEdge(de.ims.icarus2.model.api.members.item.Edge)
		 */
		@Override
		public void removeEdge(E edge) {
			if(!edges.remove(edge))
				throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER, "Cannot remove unknown edge: "+CorpusUtils.getName(edge));
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.RootItem#indexOfEdge(de.ims.icarus2.model.api.members.item.Edge)
		 */
		@Override
		public int indexOfEdge(Edge edge) {
			return edges.indexOf(edge);
		}

	}
}
