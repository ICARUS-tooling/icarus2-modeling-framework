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
 *
 */
package de.ims.icarus2.model.standard.view;

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;
import java.util.function.ObjLongConsumer;

import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.edit.CorpusEditManager;
import de.ims.icarus2.model.api.edit.change.AtomicChange;
import de.ims.icarus2.model.api.edit.io.SerializableAtomicModelChange;
import de.ims.icarus2.model.api.events.CorpusAdapter;
import de.ims.icarus2.model.api.events.CorpusEvent;
import de.ims.icarus2.model.api.events.CorpusListener;
import de.ims.icarus2.model.api.events.PageListener;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.CorpusMember;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.manager.ItemLayerManager;
import de.ims.icarus2.model.api.members.item.manager.ItemLookup;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.api.view.CorpusModel;
import de.ims.icarus2.model.api.view.CorpusView;
import de.ims.icarus2.model.api.view.CorpusView.PageControl;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.members.container.AbstractImmutableContainer;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.AbstractPart;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.LookupList;
import de.ims.icarus2.util.collections.seq.DataSequence;
import de.ims.icarus2.util.collections.set.DataSet;
import de.ims.icarus2.util.events.ChangeSource;
import de.ims.icarus2.util.lang.Lazy;

/**
 *
 * @author Markus Gärtner
 *
 */
public class DefaultCorpusModel extends AbstractPart<CorpusView> implements CorpusModel {

	protected final boolean writable;
	protected final boolean readable;

	protected final ChangeSource changeSource;

	/**
	 * Listener added to the host corpus responsible for cleaning
	 * up the model once the surrounding view gets closed
	 */
	protected final CorpusListener viewObserver;

	/**
	 * Proxy container that represents the horizontal filtering performed
	 * by the surrounding view. Created lazily when actually needed.
	 */
	protected final Lazy<RootContainer> rootContainer;

	/**
	 * Raw access to back-end storage
	 */
	protected final ItemLayerManager itemLayerManager;

	/**
	 * Proxy for storing the filtered items in an efficient lookup structure.
	 * Created lazily with a default implementation based on
	 * {@link LookupList}.
	 *
	 * @see BufferedItemLookup
	 */
	protected final Lazy<ItemLookup> itemLookup;

	private static final Logger log = LoggerFactory.getLogger(DefaultCorpusModel.class);

	public DefaultCorpusModel(Builder builder) {
		requireNonNull(builder);
		this.itemLayerManager = builder.getItemLayerManager();

		writable = builder.getAccessMode().isWrite();
		readable = builder.getAccessMode().isRead();

		viewObserver = createViewObserver();
		itemLookup = Lazy.create(this::createItemLookup, true);
		rootContainer = Lazy.create(this::createRootContainer, true);

		changeSource = new ChangeSource(this);
	}

	//---------------------------------------------
	//			INITIALIZATION METHODS
	//---------------------------------------------

	@Override
	public void addNotify(CorpusView owner) {
		super.addNotify(owner);

		owner.getCorpus().addCorpusListener(viewObserver);
	}

	/**
	 * Besides delegating to the matching super function this implementation
	 * removes listeners registered with other corpus resources and then tries
	 * to close down internal modules.
	 *
	 * @param owner
	 */
	@Override
	public void removeNotify(CorpusView owner) {
		super.removeNotify(owner);

		owner.getCorpus().removeCorpusListener(viewObserver);

		IcarusUtils.close(viewObserver, log, "view observer");
		IcarusUtils.close(rootContainer, log, "root container");
		IcarusUtils.close(itemLookup, log, "item lookup");
		IcarusUtils.close(changeSource, log, "change source");
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
		changeSource.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		changeSource.removeChangeListener(listener);
	}

	protected CorpusListener createViewObserver() {
		return new ViewObserver();
	}

	protected RootContainer createRootContainer() {
		ItemLayer layer = getView().getScope().getPrimaryLayer();

		return new RootContainer(layer);
	}

	protected ItemLookup createItemLookup() {
		CorpusView view = getView();
		PageControl pageControl = view.getPageControl();

		BufferedItemLookup itemLookup = new BufferedItemLookup(itemLayerManager, pageControl);

		// Make sure that the new item lookup is up2date wrt the current page state!
		if(pageControl.isPageLoaded()) {
			try {
				itemLookup.reload();
			} catch (InterruptedException e) {
				log.error("Loading of page was interrupted", e);
				throw new ModelException(getCorpus(), GlobalErrorCode.INTERRUPTED,
						"Could not finish creation of item lookup - loading of page was interrupted", e);
			}
		}

		return itemLookup;
	}

	//---------------------------------------------
	//			SANITY CHECK METHODS
	//---------------------------------------------

	protected final void checkReadAccess() {
		if(!readable)
			throw new ModelException(getCorpus(), ModelErrorCode.MODEL_WRITE_ONLY, "Underlying corpus view is write only");
	}

	protected final void checkWriteAccess() {
		if(!writable)
			throw new ModelException(getCorpus(), ModelErrorCode.MODEL_READ_ONLY, "Underlying corpus view is read only");
	}

	protected final void checkActivePage() {
		if(!getView().getPageControl().isPageLoaded())
			throw new ModelException(getCorpus(), ModelErrorCode.VIEW_EMPTY, "Current page of corpus view is empty");
	}

	protected final void checkLayerContained(Layer layer) {
		if(!getView().getScope().containsLayer(layer))
			throw new ModelException(getCorpus(), ModelErrorCode.MODEL_ILLEGAL_MEMBER, "Foreign layer: "+getName(layer));
	}

	protected final void checkPrimaryLayer(Layer layer) {
		if(rootContainer.value().getLayer()!=layer)
			throw new ModelException(getCorpus(), ModelErrorCode.MODEL_ILLEGAL_MEMBER, "Not a valid primary layer: "+getName(layer));
	}

	protected final void checkIndexForAdd(long index, long size) {
		// Sanity checks
		if(index<0)
			throw new ModelException(getCorpus(), ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					"Negative index for add: "+index);
		if(index>size)
			throw new ModelException(getCorpus(), ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					Messages.indexOutOfBoundsMessage("Invalid index for add", 0, size, index));

	}

	protected final void checkIndexForRemove(long index, long size) {
		// Sanity checks
		if(index<0)
			throw new ModelException(getCorpus(), ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					"Negative index for remove: "+index);
		if(index>=size)
			throw new ModelException(getCorpus(), ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					Messages.indexOutOfBoundsMessage("Invalid index for remove", 0, size-1, index));

	}

	//---------------------------------------------
	//			UTILITY METHODS
	//---------------------------------------------

	public ItemLayerManager getItemLayerManager() {
		return itemLayerManager;
	}

	protected static <E extends Object> E ensureNotNull(E obj) {
		if(obj==null)
			throw new IllegalStateException("Null data encountered");
		return obj;
	}

	//---------------------------------------------
	//			GENERAL STATE METHODS
	//---------------------------------------------

	@Override
	public boolean isModelEditable() {
		return writable;
	}

	@Override
	public boolean isModelActive() {
		return getView().isActive();
	}

	@Override
	public boolean isModelComplete() {
		CorpusView view = getView();

		return view.getPageSize()>=view.getSize();
	}

	@Override
	public CorpusView getView() {
		checkAdded();
		return getOwner();
	}

	//---------------------------------------------
	//			MEMBER METHODS
	//---------------------------------------------


	@Override
	public MemberType getMemberType(CorpusMember member) {
		//TODO always allow at least type queries?
//		checkReadAccess();

		return ensureNotNull(member.getMemberType());
	}


	//---------------------------------------------
	//			LAYER METHODS
	//---------------------------------------------

	@Override
	public long getSize(ItemLayer layer) {
		checkReadAccess();
		checkLayerContained(layer);

		return itemLayerManager.getItemCount(layer);
	}

	@Override
	public Container getRootContainer(ItemLayer layer) {
		checkReadAccess();
		checkLayerContained(layer);
		checkActivePage();
		checkPrimaryLayer(layer);

		return rootContainer.value();
	}

	@Override
	public Item getItem(ItemLayer layer, long index) {
		checkReadAccess();
		checkLayerContained(layer);

		return itemLayerManager.getItem(layer, index);
	}


	//---------------------------------------------
	//			ITEM METHODS
	//---------------------------------------------

	@Override
	public Container getContainer(Item item) {
		checkReadAccess();

		return item.getContainer();
	}

	@Override
	public ItemLayer getLayer(Item item) {
		checkReadAccess();

		return ensureNotNull(item.getLayer());
	}

	@Override
	public long getIndex(Item item) {
		checkReadAccess();

		return item.getIndex();
	}

	@Override
	public long getBeginOffset(Item item) {
		checkReadAccess();

		return item.getBeginOffset();
	}

	@Override
	public long getEndOffset(Item item) {
		checkReadAccess();

		return item.getEndOffset();
	}

	@Override
	public boolean isVirtual(Item item) {
		checkReadAccess();

		return ModelUtils.isVirtual(item);
	}


	//---------------------------------------------
	//			CONTAINER METHODS
	//---------------------------------------------

	@Override
	public ContainerType getContainerType(Container container) {
		checkReadAccess();

		return ensureNotNull(container.getContainerType());
	}

	@Override
	public DataSet<Container> getBaseContainers(Container container) {
		checkReadAccess();

		return ensureNotNull(container.getBaseContainers());
	}

	@Override
	public Container getBoundaryContainer(Container container) {
		checkReadAccess();

		return container.getBoundaryContainer();
	}

	@Override
	public long getItemCount(Container container) {
		checkReadAccess();

		return container.getItemCount();
	}

	@Override
	public Item getItemAt(Container container, long index) {
		checkReadAccess();

		return ensureNotNull(container.getItemAt(index));
	}

	@Override
	public long indexOfItem(Container container, Item item) {
		checkReadAccess();

		return container.indexOfItem(item);
	}

	@Override
	public boolean containsItem(Container container, Item item) {
		return indexOfItem(container, item)!=IcarusUtils.UNSET_LONG;
	}

	@Override
	public void addItem(Container container, Item item) {
		checkWriteAccess();
		long index = container.getItemCount();

		addItem(container, index, item);
	}

	@Override
	public void addItem(Container container, long index, Item item) {
		checkWriteAccess();
		checkIndexForAdd(index, container.getItemCount());

		executeChange(new SerializableAtomicModelChange.ItemChange(container, item, container.getItemCount(), index, true));
	}

	@Override
	public void addItems(Container container, long index,
			DataSequence<? extends Item> items) {
		checkWriteAccess();
		checkIndexForAdd(index, container.getItemCount());

		executeChange(new SerializableAtomicModelChange.ItemSequenceChange(container, container.getItemCount(), index, items));
	}

	@Override
	public Item removeItem(Container container, long index) {
		checkWriteAccess();
		checkIndexForRemove(index, container.getItemCount());

		Item item = container.getItemAt(index);

		executeChange(new SerializableAtomicModelChange.ItemChange(container, item, container.getItemCount(), index, false));

		return item;
	}

	@Override
	public boolean removeItem(Container c, Item item) {
		long index = c.indexOfItem(item);

		if(index==IcarusUtils.UNSET_LONG) {
			return false;
		}

		removeItem(c, item);

		return true;
	}

	@Override
	public void removeItems(Container container, long index0, long index1) {
		checkWriteAccess();
		checkIndexForRemove(index0, container.getItemCount());
		checkIndexForRemove(index1, container.getItemCount());
		checkArgument(index0<=index1);

		executeChange(new SerializableAtomicModelChange.ItemSequenceChange(container, container.getItemCount(), index0, index1));
	}

	@Override
	public void moveItem(Container container, long index0, long index1) {
		checkWriteAccess();
		checkIndexForRemove(index0, container.getItemCount());
		checkIndexForRemove(index1, container.getItemCount());

		Item item0 = container.getItemAt(index0);
		Item item1 = container.getItemAt(index1);

		executeChange(new SerializableAtomicModelChange.ItemMoveChange(container, container.getItemCount(), index0, index1, item0, item1));
	}


	//---------------------------------------------
	//			STRUCTURE METHODS
	//---------------------------------------------

	@Override
	public StructureType getStructureType(Structure structure) {
		checkReadAccess();

		return ensureNotNull(structure.getStructureType());
	}

	@Override
	public long getEdgeCount(Structure structure) {
		checkReadAccess();

		return structure.getEdgeCount();
	}

	@Override
	public Edge getEdgeAt(Structure structure, long index) {
		checkReadAccess();

		return ensureNotNull(structure.getEdgeAt(index));
	}

	@Override
	public long indexOfEdge(Structure structure, Edge edge) {
		checkReadAccess();

		return structure.indexOfEdge(edge);
	}

	@Override
	public boolean containsEdge(Structure structure, Edge edge) {
		checkReadAccess();

		return structure.indexOfEdge(edge)!=-1L;
	}

	@Override
	public long getEdgeCount(Structure structure, Item node) {
		checkReadAccess();

		return structure.getEdgeCount(node);
	}

	@Override
	public long getEdgeCount(Structure structure, Item node, boolean isSource) {
		checkReadAccess();

		return structure.getEdgeCount(node, isSource);
	}

	@Override
	public Edge getEdgeAt(Structure structure, Item node, long index, boolean isSource) {
		checkReadAccess();

		return ensureNotNull(structure.getEdgeAt(node, index, isSource));
	}

	@Override
	public Item getVirtualRoot(Structure structure) {
		checkReadAccess();

		return ensureNotNull(structure.getVirtualRoot());
	}

	@Override
	public boolean isRoot(Structure structure, Item node) {
		checkReadAccess();

		return structure.isRoot(node);
	}

	@Override
	public Item getParent(Structure structure, Item node) {
		checkReadAccess();

		return structure.getParent(node);
	}

	@Override
	public long indexOfChild(Structure structure, Item child) {
		checkReadAccess();

		return structure.indexOfChild(child);
	}

	@Override
	public Item getSiblingAt(Structure structure, Item child, long offset) {
		checkReadAccess();

		return structure.getSiblingAt(child, offset);
	}

	@Override
	public long getHeight(Structure structure, Item node) {
		checkReadAccess();

		return structure.getHeight(node);
	}

	@Override
	public long getDepth(Structure structure, Item node) {
		checkReadAccess();

		return structure.getDepth(node);
	}

	@Override
	public long getDescendantCount(Structure structure, Item parent) {
		checkReadAccess();

		return structure.getDescendantCount(parent);
	}

	@Override
	public void addEdge(Structure structure, Edge edge) {
		checkWriteAccess();

		long index = structure.getEdgeCount();

		addEdge(structure, index, edge);
	}

	@Override
	public void addEdge(Structure structure, long index, Edge edge) {
		checkWriteAccess();
		checkIndexForAdd(index, structure.getEdgeCount());

		executeChange(new SerializableAtomicModelChange.EdgeChange(structure, edge, structure.getEdgeCount(), index, true));
	}

	@Override
	public void addEdges(Structure structure, long index,
			DataSequence<? extends Edge> edges) {
		checkWriteAccess();
		checkIndexForAdd(index, structure.getEdgeCount());

		executeChange(new SerializableAtomicModelChange.EdgeSequenceChange(structure, structure.getEdgeCount(), index, edges));
	}

	@Override
	public Edge removeEdge(Structure structure, long index) {
		checkWriteAccess();
		checkIndexForRemove(index, structure.getEdgeCount());

		Edge edge = structure.getEdgeAt(index);

		executeChange(new SerializableAtomicModelChange.EdgeChange(structure, edge, structure.getEdgeCount(), index, false));

		return edge;
	}

	@Override
	public boolean removeEdge(Structure structure, Edge edge) {
		long index = structure.indexOfEdge(edge);

		if(index==IcarusUtils.UNSET_LONG) {
			return false;
		}

		removeEdge(structure, index);

		return true;
	}

	@Override
	public void removeEdges(Structure structure, long index0, long index1) {
		checkWriteAccess();
		checkIndexForRemove(index0, structure.getEdgeCount());
		checkIndexForRemove(index1, structure.getEdgeCount());
		checkArgument(index0<=index1);

		executeChange(new SerializableAtomicModelChange.EdgeSequenceChange(structure, structure.getEdgeCount(), index0, index1));
	}

	@Override
	public void moveEdge(Structure structure, long index0, long index1) {
		checkWriteAccess();
		checkIndexForRemove(index0, structure.getEdgeCount());
		checkIndexForRemove(index1, structure.getEdgeCount());

		Edge edge0 = structure.getEdgeAt(index0);
		Edge edge1 = structure.getEdgeAt(index1);

		executeChange(new SerializableAtomicModelChange.EdgeMoveChange(structure, structure.getEdgeCount(), index0, index1, edge0, edge1));
	}

	@Override
	public Item setTerminal(Structure structure, Edge edge, Item item,
			boolean isSource) {
		Item oldTerminal = edge.getTerminal(isSource);

		executeChange(new SerializableAtomicModelChange.TerminalChange(structure, edge, isSource, item, oldTerminal));

		return oldTerminal;
	}


	//---------------------------------------------
	//			EDGE METHODS
	//---------------------------------------------

	@Override
	public Structure getStructure(Edge edge) {
		checkReadAccess();

		return ensureNotNull(edge.getStructure());
	}

	@Override
	public Item getSource(Edge edge) {
		checkReadAccess();

		return ensureNotNull(edge.getSource());
	}

	@Override
	public Item getTarget(Edge edge) {
		checkReadAccess();

		return ensureNotNull(edge.getTarget());
	}


	//---------------------------------------------
	//			FRAGMENT METHODS
	//---------------------------------------------

	@Override
	public Item getItem(Fragment fragment) {
		checkReadAccess();

		return ensureNotNull(fragment.getItem());
	}

	@Override
	public Position getFragmentBegin(Fragment fragment) {
		checkReadAccess();

		return ensureNotNull(fragment.getFragmentBegin());
	}

	@Override
	public Position getFragmentEnd(Fragment fragment) {
		checkReadAccess();

		return ensureNotNull(fragment.getFragmentEnd());
	}

	@Override
	public Position setFragmentBegin(Fragment fragment, Position position) {
		Position oldPosition = ensureNotNull(fragment.getFragmentBegin());

		executeChange(new SerializableAtomicModelChange.PositionChange(fragment, true, position));

		return oldPosition;
	}

	@Override
	public Position setFragmentEnd(Fragment fragment, Position position) {
		Position oldPosition = ensureNotNull(fragment.getFragmentEnd());

		executeChange(new SerializableAtomicModelChange.PositionChange(fragment, false, position));

		return oldPosition;
	}


	//---------------------------------------------
	//			VALUE_MANIFEST METHODS
	//---------------------------------------------

	@Override
	public boolean collectKeys(AnnotationLayer layer, Item item,
			Consumer<String> action) {
		checkReadAccess();

		return layer.getAnnotationStorage().collectKeys(item, action);
	}

	@Override
	public Object getValue(AnnotationLayer layer, Item item, String key) {
		checkReadAccess();

		return layer.getAnnotationStorage().getValue(item, key);
	}

	@Override
	public int getIntegerValue(AnnotationLayer layer, Item item, String key) {
		checkReadAccess();

		return layer.getAnnotationStorage().getIntegerValue(item, key);
	}

	@Override
	public long getLongValue(AnnotationLayer layer, Item item, String key) {
		checkReadAccess();

		return layer.getAnnotationStorage().getLongValue(item, key);
	}

	@Override
	public float getFloatValue(AnnotationLayer layer, Item item, String key) {
		checkReadAccess();

		return layer.getAnnotationStorage().getFloatValue(item, key);
	}

	@Override
	public double getDoubleValue(AnnotationLayer layer, Item item, String key) {
		checkReadAccess();

		return layer.getAnnotationStorage().getDoubleValue(item, key);
	}

	@Override
	public boolean getBooleanValue(AnnotationLayer layer, Item item, String key) {
		checkReadAccess();

		return layer.getAnnotationStorage().getBooleanValue(item, key);
	}

	@Override
	public void removeAllValues(AnnotationLayer layer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAllValues(AnnotationLayer layer, String key) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object setValue(AnnotationLayer layer, Item item, String key,
			Object value) {
		Object oldValue = layer.getAnnotationStorage().getValue(item, key);
		ValueType valueType = layer.getManifest().getAnnotationManifest(key).getValueType();

		executeChange(new SerializableAtomicModelChange.ValueChange(layer, valueType, item, key, value, oldValue));

		return oldValue;
	}

	@Override
	public int setIntegerValue(AnnotationLayer layer, Item item, String key,
			int value) {
		int oldValue = layer.getAnnotationStorage().getIntegerValue(item, key);

		executeChange(new SerializableAtomicModelChange.IntegerValueChange(layer, item, key, value, oldValue));

		return oldValue;
	}

	@Override
	public long setLongValue(AnnotationLayer layer, Item item, String key,
			long value) {
		long oldValue = layer.getAnnotationStorage().getLongValue(item, key);

		executeChange(new SerializableAtomicModelChange.LongValueChange(layer, item, key, value, oldValue));

		return oldValue;
	}

	@Override
	public float setFloatValue(AnnotationLayer layer, Item item, String key,
			float value) {
		float oldValue = layer.getAnnotationStorage().getFloatValue(item, key);

		executeChange(new SerializableAtomicModelChange.FloatValueChange(layer, item, key, value, oldValue));

		return oldValue;
	}

	@Override
	public double setDoubleValue(AnnotationLayer layer, Item item, String key,
			double value) {
		double oldValue = layer.getAnnotationStorage().getDoubleValue(item, key);

		executeChange(new SerializableAtomicModelChange.DoubleValueChange(layer, item, key, value, oldValue));

		return oldValue;
	}

	@Override
	public boolean setBooleanValue(AnnotationLayer layer, Item item,
			String key, boolean value) {
		boolean oldValue = layer.getAnnotationStorage().getBooleanValue(item, key);

		executeChange(new SerializableAtomicModelChange.BooleanValueChange(layer, item, key, value, oldValue));

		return oldValue;
	}

	@Override
	public boolean hasAnnotations(AnnotationLayer layer) {
		checkReadAccess();

		return layer.getAnnotationStorage().hasAnnotations();
	}

	@Override
	public boolean hasAnnotations(AnnotationLayer layer, Item item) {
		checkReadAccess();

		return layer.getAnnotationStorage().hasAnnotations(item);
	}

	/**
	 * Helper method to check whether or not the enclosing corpus is editable
	 * and to forward an atomic change to the edit model.
	 * <p>
	 * If there is no {@link Corpus#getEditManager() edit model} present on the
	 * {@link CorpusModel#getCorpus() corpus} then the {@code change} will be
	 * {@link AtomicChange#execute() executed} directly.
	 *
	 * @param change the atomic change to be executed
	 * @throws UnsupportedOperationException if the corpus is not editable
	 */
	protected void executeChange(AtomicChange change) {
		checkWriteAccess();

		CorpusEditManager editModel = getCorpus().getEditManager();

		if(editModel==null) {
			change.execute();
		} else {
			editModel.execute(change);
		}
	}

	//TODO forward destruction/reload of internal resources according to page changes
	protected class ViewObserver extends CorpusAdapter {

		@Override
		public void corpusViewDestroyed(CorpusEvent e) {
			if(e.getCorpusView()==getView()) {
				//TODO disconnect all resources!!
			}
		}

	}

	protected static class BufferedItemLookup implements ItemLookup, PageListener, ObjLongConsumer<Item>, AutoCloseable {

		private boolean dataLocked = false;

		private final PageControl pageControl;
		private final LookupList<Item> items;
		private final ItemLayerManager itemLayerManager;

		public BufferedItemLookup(ItemLayerManager itemLayerManager, PageControl pageControl) {
			requireNonNull(itemLayerManager);
			requireNonNull(pageControl);

			this.pageControl = pageControl;
			int capacity = pageControl.getPageSize();

			this.itemLayerManager = itemLayerManager;
			items = new LookupList<>(capacity);

			pageControl.addPageListener(this);
		}

		protected synchronized void lock() {
			dataLocked = true;
		}

		protected synchronized void clear() {
			items.clear();
			dataLocked = false;
		}

		protected synchronized void reload() throws InterruptedException {
			try {
				IndexSet indices = pageControl.getIndices();
				ItemLayer layer = pageControl.getView().getScope().getPrimaryLayer();
				itemLayerManager.forItems(layer, IndexUtils.wrap(indices), this);
			} catch (ModelException e) {
				items.clear();
				throw e;
			} finally {
				dataLocked = false;
			}
		}

		@Override
		public void accept(Item item, long index) {
			items.add(item);
		}

		protected final void checkNotLocked() {
			if(dataLocked)
				throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
						"Cannot access data as it is locked due to being either reloaded or cleared");
		}

		protected final void checkPageControl(PageControl pageControl) {
			if(this.pageControl!=pageControl)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT,
						"Notification chain corrupted - provided page control does not match the one saved for this model");
		}

		/**
		 * @see de.ims.icarus2.model.api.events.PageListener#pageClosing(de.ims.icarus2.model.api.view.CorpusView.PageControl, int)
		 */
		@Override
		public void pageClosing(PageControl source, int page) {
			checkPageControl(source);

			lock();
		}

		/**
		 * @see de.ims.icarus2.model.api.events.PageListener#pageClosed(de.ims.icarus2.model.api.view.CorpusView.PageControl, int)
		 */
		@Override
		public void pageClosed(PageControl source, int page) {
			checkPageControl(source);

			clear();
		}

		/**
		 * @see de.ims.icarus2.model.api.events.PageListener#pageLoading(de.ims.icarus2.model.api.view.CorpusView.PageControl, int, int)
		 */
		@Override
		public void pageLoading(PageControl source, int page, int size) {
			checkPageControl(source);

			lock();
		}

		/**
		 * @see de.ims.icarus2.model.api.events.PageListener#pageLoaded(de.ims.icarus2.model.api.view.CorpusView.PageControl, int, int)
		 */
		@Override
		public void pageLoaded(PageControl source, int page, int size) {
			checkPageControl(source);

			try {
				reload();
			} catch (InterruptedException e) {
				log.error("Loading of page was interrupted", e);
			}
		}

		/**
		 * @see de.ims.icarus2.model.api.events.PageListener#pageFailed(de.ims.icarus2.model.api.view.CorpusView.PageControl, int, de.ims.icarus2.model.api.ModelException)
		 */
		@Override
		public void pageFailed(PageControl source, int page, ModelException ex) {
			checkPageControl(source);

			clear();
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.manager.ItemLookup#getItemCount()
		 */
		@Override
		public long getItemCount() {
			checkNotLocked();

			return items.size();
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.manager.ItemLookup#getItemAt(long)
		 */
		@Override
		public Item getItemAt(long index) {
			checkNotLocked();

			return items.get(IcarusUtils.ensureIntegerValueRange(index));
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.manager.ItemLookup#indexOfItem(de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long indexOfItem(Item item) {
			checkNotLocked();

			return items.indexOf(item);
		}

		/**
		 * @see java.lang.AutoCloseable#close()
		 */
		@Override
		public void close() throws Exception {
			pageControl.removePageListener(this);
		}

	}

	protected class RootContainer extends AbstractImmutableContainer {

		private final ItemLayer layer;

		public RootContainer(ItemLayer layer) {
			this.layer = layer;
		}

		protected CorpusModel getModel() {
			return DefaultCorpusModel.this;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#getId()
		 */
		@Override
		public long getId() {
			return IcarusUtils.UNSET_LONG;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#getContainer()
		 */
		@Override
		public Container getContainer() {
			return null;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#getLayer()
		 */
		@Override
		public ItemLayer getLayer() {
			return layer;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#getIndex()
		 */
		@Override
		public long getIndex() {
			return IcarusUtils.UNSET_LONG;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#isAlive()
		 */
		@Override
		public boolean isAlive() {
			return getModel().isModelActive();
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#isLocked()
		 */
		@Override
		public boolean isLocked() {
			return false;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#isDirty()
		 */
		@Override
		public boolean isDirty() {
			return false;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.CorpusMember#getCorpus()
		 */
		@Override
		public Corpus getCorpus() {
			return getModel().getCorpus();
		}

		/**
		 * @see de.ims.icarus2.model.api.members.container.Container#getContainerType()
		 */
		@Override
		public ContainerType getContainerType() {
			return ContainerType.LIST;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.container.Container#getManifest()
		 */
		@Override
		public ContainerManifest getManifest() {
			return layer.getManifest().getRootContainerManifest();
		}

		/**
		 * @see de.ims.icarus2.model.api.members.container.Container#getBaseContainers()
		 */
		@Override
		public DataSet<Container> getBaseContainers() {
			return DataSet.emptySet();
		}

		/**
		 * @see de.ims.icarus2.model.api.members.container.Container#getBoundaryContainer()
		 */
		@Override
		public Container getBoundaryContainer() {
			return null;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.container.Container#isItemsComplete()
		 */
		@Override
		public boolean isItemsComplete() {
			return getModel().isModelComplete();
		}

		/**
		 * @see de.ims.icarus2.model.api.members.container.Container#getItemCount()
		 */
		@Override
		public long getItemCount() {
			return itemLookup.value().getItemCount();
		}

		/**
		 * @see de.ims.icarus2.model.api.members.container.Container#getItemAt(long)
		 */
		@Override
		public Item getItemAt(long index) {
			return itemLookup.value().getItemAt(index);
		}

		/**
		 * @see de.ims.icarus2.model.api.members.container.Container#indexOfItem(de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long indexOfItem(Item item) {
			return itemLookup.value().indexOfItem(item);
		}
	}

	public static class Builder extends AbstractBuilder<Builder, CorpusModel> {

		private AccessMode accessMode;
		private ItemLayerManager itemLayerManager;


		public Builder accessMode(AccessMode accessMode) {
			requireNonNull(accessMode);
			checkState(this.accessMode==null);

			this.accessMode = accessMode;

			return thisAsCast();
		}

		public AccessMode getAccessMode() {
			return accessMode;
		}

		public Builder itemLayerManager(ItemLayerManager itemLayerManager) {
			requireNonNull(itemLayerManager);
			checkState(this.itemLayerManager==null);

			this.itemLayerManager = itemLayerManager;

			return thisAsCast();
		}

		public ItemLayerManager getItemLayerManager() {
			return itemLayerManager;
		}

		@Override
		protected void validate() {
			checkState("Missing access mode", accessMode!=null);
			checkState("Missing item layer manager", itemLayerManager!=null);
		}

		@Override
		public DefaultCorpusModel create() {
			return new DefaultCorpusModel(this);
		}
	}
}
