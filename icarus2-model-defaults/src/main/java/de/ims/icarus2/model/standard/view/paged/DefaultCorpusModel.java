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
package de.ims.icarus2.model.standard.view.paged;

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.ObjLongConsumer;

import javax.annotation.Nullable;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.apiguard.Api;
import de.ims.icarus2.apiguard.Api.ApiType;
import de.ims.icarus2.apiguard.Guarded;
import de.ims.icarus2.apiguard.Guarded.MethodType;
import de.ims.icarus2.apiguard.Mandatory;
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
import de.ims.icarus2.model.api.view.paged.CorpusModel;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ContainerManifestBase;
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
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.collections.LookupList;
import de.ims.icarus2.util.collections.seq.DataSequence;
import de.ims.icarus2.util.collections.set.DataSet;
import de.ims.icarus2.util.events.ChangeSource;
import de.ims.icarus2.util.lang.Lazy;
import de.ims.icarus2.util.mem.Assessable;
import de.ims.icarus2.util.mem.Primitive;
import de.ims.icarus2.util.mem.Reference;
import de.ims.icarus2.util.mem.ReferenceType;

/**
 *
 * @author Markus Gärtner
 *
 */
@TestableImplementation(CorpusModel.class)
@Assessable
public class DefaultCorpusModel extends AbstractPart<PagedCorpusView> implements CorpusModel {

	public static Builder builder() {
		return new Builder();
	}

	@Primitive
	protected final boolean writable;
	@Primitive
	protected final boolean readable;

	/**
	 * Listener added to the host corpus responsible for cleaning
	 * up the model once the surrounding view gets closed
	 */
	@Reference(ReferenceType.DOWNLINK)
	protected final CorpusListener viewObserver;

	/**
	 * Proxy container that represents the horizontal filtering performed
	 * by the surrounding view. Created lazily when actually needed.
	 */
	@Reference(ReferenceType.DOWNLINK)
	protected final Lazy<RootContainer> rootContainer;

	/**
	 * Raw access to back-end storage
	 */
	@Reference(ReferenceType.UPLINK)
	protected final ItemLayerManager itemLayerManager;

	/**
	 * Optional external handler for the changes nade through this model.
	 */
	@Reference(ReferenceType.DOWNLINK)
	protected final Consumer<AtomicChange> changeHandler;

	/**
	 * Proxy for storing the filtered items in an efficient lookup structure.
	 * Created lazily with a default implementation based on
	 * {@link LookupList}.
	 *
	 * @see BufferedItemLookup
	 */
	@Reference(ReferenceType.DOWNLINK)
	protected final Lazy<BufferedItemLookup> itemLookup;

	private static final Logger log = LoggerFactory.getLogger(DefaultCorpusModel.class);

	public DefaultCorpusModel(Builder builder) {
		requireNonNull(builder);
		this.itemLayerManager = builder.getItemLayerManager();

		writable = builder.getAccessMode().isWrite();
		readable = builder.getAccessMode().isRead();

		viewObserver = createViewObserver(); //TODO bad, should not call non-private builder in constructor!
		itemLookup = Lazy.create(this::createItemLookup, true);
		rootContainer = Lazy.create(this::createRootContainer, true);

		Consumer<AtomicChange> changeHandler = builder.getChangeHandler();
		if(changeHandler==null) {
			/**
			 * Default strategy: Grab the enclosing corpus' edit manager.
			 * If there is no edit manager present on the corpus then the change
			 * will be executed directly.
			 */
			changeHandler = change -> {
				CorpusEditManager editModel = getCorpus().getEditManager();

				if(editModel==null) {
					change.execute();
				} else {
					editModel.execute(change);
				}
			};
		} else {
			log.info("Using custom change handler - if modifications to the corpus are "
					+ "not reflected properly, this might be the cause!");
		}

		this.changeHandler = changeHandler;
	}

	//---------------------------------------------
	//			INITIALIZATION METHODS
	//---------------------------------------------

	@Override
	public void addNotify(PagedCorpusView owner) {
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
	public void removeNotify(PagedCorpusView owner) {
		super.removeNotify(owner);

		owner.getCorpus().removeCorpusListener(viewObserver);

		IcarusUtils.close(viewObserver, log, "view observer");
		IcarusUtils.close(rootContainer, log, "root container");
		IcarusUtils.close(itemLookup, log, "item lookup");
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
		itemLookup.value().addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		itemLookup.value().removeChangeListener(listener);
	}

	protected CorpusListener createViewObserver() {
		return new ViewObserver();
	}

	protected RootContainer createRootContainer() {
		ItemLayer layer = getView().getScope().getPrimaryLayer();

		return new RootContainer(layer);
	}

	protected BufferedItemLookup createItemLookup() {
		PagedCorpusView view = getView();
		PageControl pageControl = view.getPageControl();

		BufferedItemLookup itemLookup = new BufferedItemLookup(this, itemLayerManager, pageControl);

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

	protected final void checkAlive(Item item) {
		if(!item.isAlive())
			throw new ModelException(ModelErrorCode.MODEL_CORRUPTED_STATE, "Item is dead: "+getName(item));
	}

	protected final void checkClean(Item item) {
		if(item.isDirty())
			throw new ModelException(ModelErrorCode.MODEL_CORRUPTED_STATE, "Item is dirty: "+getName(item));
	}

	protected final void checkUnlocked(Item item) {
		if(item.isLocked())
			throw new ModelException(ModelErrorCode.MODEL_CORRUPTED_STATE, "Item is locked: "+getName(item));
	}

	private void checkReadItem(Item target) {
		checkReadAccess();
		checkAlive(target);
		checkClean(target);
	}

	private void checkWriteItem(Item target) {
		checkWriteAccess();
		checkAlive(target);
		checkUnlocked(target);
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
					Messages.indexOutOfBounds("Invalid index for add", 0, size, index));

	}

	protected final void checkIndexForRemove(long index, long size) {
		// Sanity checks
		if(index<0)
			throw new ModelException(getCorpus(), ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					"Negative index for remove: "+index);
		if(index>=size)
			throw new ModelException(getCorpus(), ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					Messages.indexOutOfBounds("Invalid index for remove", 0, size-1, index));

	}

	//---------------------------------------------
	//			UTILITY METHODS
	//---------------------------------------------

	public ItemLayerManager getItemLayerManager() {
		return itemLayerManager;
	}

	protected static <E extends Object> E ensureNotNull(E obj) {
		if(obj==null)
			throw new ModelException(ModelErrorCode.MODEL_CORRUPTED_STATE, "Null data encountered");
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
		PagedCorpusView view = getView();

		return view.getPageSize()>=view.getSize();
	}

	@Override
	public PagedCorpusView getView() {
		checkAdded();
		return getOwner();
	}

	//---------------------------------------------
	//			MEMBER METHODS
	//---------------------------------------------


	@Override
	public MemberType getMemberType(CorpusMember member) {
		// Always allow at least type queries?
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
		checkActivePage();
		checkLayerContained(layer);
		checkPrimaryLayer(layer);

		return rootContainer.value();
	}


	//---------------------------------------------
	//			ITEM METHODS
	//---------------------------------------------

	@Override
	public Container getContainer(Item item) {
		checkReadItem(item);

		return item.getContainer();
	}

	@Override
	public ItemLayer getLayer(Item item) {
		checkReadItem(item);

		return ensureNotNull(item.getLayer());
	}

	@Override
	public long getIndex(Item item) {
		checkReadItem(item);

		return item.getIndex();
	}

	@Override
	public long getBeginOffset(Item item) {
		checkReadItem(item);

		return item.getBeginOffset();
	}

	@Override
	public long getEndOffset(Item item) {
		checkReadItem(item);

		return item.getEndOffset();
	}

	@Override
	public boolean isVirtual(Item item) {
		checkReadItem(item);

		return ModelUtils.isVirtual(item);
	}


	//---------------------------------------------
	//			CONTAINER METHODS
	//---------------------------------------------

	@Override
	public ContainerType getContainerType(Container container) {
		checkReadItem(container);

		return ensureNotNull(container.getContainerType());
	}

	@Override
	public DataSet<Container> getBaseContainers(Container container) {
		checkReadItem(container);

		return ensureNotNull(container.getBaseContainers());
	}

	@Override
	public Container getBoundaryContainer(Container container) {
		checkReadItem(container);

		return container.getBoundaryContainer();
	}

	@Override
	public long getItemCount(Container container) {
		checkReadItem(container);

		return container.getItemCount();
	}

	@Override
	public Item getItemAt(Container container, long index) {
		checkReadItem(container);

		return ensureNotNull(container.getItemAt(index));
	}

	@Override
	public long indexOfItem(Container container, Item item) {
		checkReadItem(container);

		return container.indexOfItem(item);
	}

	@Override
	public boolean containsItem(Container container, Item item) {
		checkReadItem(container);

		return container.containsItem(item);
	}

	@Override
	public void addItem(Container container, Item item) {
		checkWriteItem(container);
		long index = container.getItemCount();

		addItem(container, index, item);
	}

	@Override
	public void addItem(Container container, long index, Item item) {
		checkWriteItem(container);
		checkIndexForAdd(index, container.getItemCount());

		executeChange(new SerializableAtomicModelChange.ItemChange(
				container, item, container.getItemCount(), index, true));
	}

	@Override
	public void addItems(Container container, long index,
			DataSequence<? extends Item> items) {
		checkWriteItem(container);
		checkIndexForAdd(index, container.getItemCount());

		executeChange(new SerializableAtomicModelChange.ItemSequenceChange(
				container, container.getItemCount(), index, items));
	}

	@Override
	public Item removeItem(Container container, long index) {
		checkWriteItem(container);
		checkIndexForRemove(index, container.getItemCount());

		Item item = container.getItemAt(index);

		executeChange(new SerializableAtomicModelChange.ItemChange(
				container, item, container.getItemCount(), index, false));

		return item;
	}

	@Override
	public boolean removeItem(Container c, Item item) {
		checkWriteItem(c);
		long index = c.indexOfItem(item);

		if(index==IcarusUtils.UNSET_LONG) {
			return false;
		}

		removeItem(c, index);

		return true;
	}

	@Override
	public void removeItems(Container container, long index0, long index1) {
		checkWriteItem(container);
		checkIndexForRemove(index0, container.getItemCount());
		checkIndexForRemove(index1, container.getItemCount());
		checkArgument(index0<=index1);

		executeChange(new SerializableAtomicModelChange.ItemSequenceChange(
				container, container.getItemCount(), index0, index1));
	}

	@Override
	public void swapItems(Container container, long index0, long index1) {
		checkWriteItem(container);
		checkIndexForRemove(index0, container.getItemCount());
		checkIndexForRemove(index1, container.getItemCount());

		Item item0 = container.getItemAt(index0);
		Item item1 = container.getItemAt(index1);

		executeChange(new SerializableAtomicModelChange.ItemMoveChange(
				container, container.getItemCount(), index0, index1, item0, item1));
	}


	//---------------------------------------------
	//			STRUCTURE METHODS
	//---------------------------------------------

	@Override
	public StructureType getStructureType(Structure structure) {
		checkReadItem(structure);

		return ensureNotNull(structure.getStructureType());
	}

	@Override
	public long getEdgeCount(Structure structure) {
		checkReadItem(structure);

		return structure.getEdgeCount();
	}

	@Override
	public Edge getEdgeAt(Structure structure, long index) {
		checkReadItem(structure);

		return ensureNotNull(structure.getEdgeAt(index));
	}

	@Override
	public long indexOfEdge(Structure structure, Edge edge) {
		checkReadItem(structure);

		return structure.indexOfEdge(edge);
	}

	@Override
	public boolean containsEdge(Structure structure, Edge edge) {
		checkReadItem(structure);

		return structure.indexOfEdge(edge)!=-1L;
	}

	@Override
	public long getEdgeCount(Structure structure, Item node) {
		checkReadItem(structure);

		return structure.getEdgeCount(node);
	}

	@Override
	public long getEdgeCount(Structure structure, Item node, boolean isSource) {
		checkReadItem(structure);

		return structure.getEdgeCount(node, isSource);
	}

	@Override
	public Edge getEdgeAt(Structure structure, Item node, long index, boolean isSource) {
		checkReadItem(structure);

		return ensureNotNull(structure.getEdgeAt(node, index, isSource));
	}

	@Override
	public Item getVirtualRoot(Structure structure) {
		checkReadItem(structure);

		return ensureNotNull(structure.getVirtualRoot());
	}

	@Override
	public boolean isRoot(Structure structure, Item node) {
		checkReadItem(structure);

		return structure.isRoot(node);
	}

	@Override
	public Item getParent(Structure structure, Item node) {
		checkReadItem(structure);

		return structure.getParent(node);
	}

	@Override
	public long indexOfChild(Structure structure, Item child) {
		checkReadItem(structure);

		return structure.indexOfChild(child);
	}

	@Override
	public Item getSiblingAt(Structure structure, Item child, long offset) {
		checkReadItem(structure);

		return structure.getSiblingAt(child, offset);
	}

	@Override
	public long getHeight(Structure structure, Item node) {
		checkReadItem(structure);

		return structure.getHeight(node);
	}

	@Override
	public long getDepth(Structure structure, Item node) {
		checkReadItem(structure);

		return structure.getDepth(node);
	}

	@Override
	public long getDescendantCount(Structure structure, Item parent) {
		checkReadItem(structure);

		return structure.getDescendantCount(parent);
	}

	@Override
	public void addEdge(Structure structure, Edge edge) {
		checkWriteItem(structure);

		long index = structure.getEdgeCount();

		addEdge(structure, index, edge);
	}

	@Override
	public void addEdge(Structure structure, long index, Edge edge) {
		checkWriteItem(structure);
		checkIndexForAdd(index, structure.getEdgeCount());

		executeChange(new SerializableAtomicModelChange.EdgeChange(
				structure, edge, structure.getEdgeCount(), index, true));
	}

	@Override
	public void addEdges(Structure structure, long index,
			DataSequence<? extends Edge> edges) {
		checkWriteItem(structure);
		checkIndexForAdd(index, structure.getEdgeCount());

		executeChange(new SerializableAtomicModelChange.EdgeSequenceChange(
				structure, structure.getEdgeCount(), index, edges));
	}

	@Override
	public Edge removeEdge(Structure structure, long index) {
		checkWriteItem(structure);
		checkIndexForRemove(index, structure.getEdgeCount());

		Edge edge = structure.getEdgeAt(index);

		executeChange(new SerializableAtomicModelChange.EdgeChange(
				structure, edge, structure.getEdgeCount(), index, false));

		return edge;
	}

	@Override
	public boolean removeEdge(Structure structure, Edge edge) {
		checkWriteItem(structure);
		long index = structure.indexOfEdge(edge);

		if(index==IcarusUtils.UNSET_LONG) {
			return false;
		}

		removeEdge(structure, index);

		return true;
	}

	@Override
	public void removeEdges(Structure structure, long index0, long index1) {
		checkWriteItem(structure);
		checkIndexForRemove(index0, structure.getEdgeCount());
		checkIndexForRemove(index1, structure.getEdgeCount());
		checkArgument(index0<=index1);

		executeChange(new SerializableAtomicModelChange.EdgeSequenceChange(
				structure, structure.getEdgeCount(), index0, index1));
	}

	@Override
	public void swapEdges(Structure structure, long index0, long index1) {
		checkWriteItem(structure);
		checkIndexForRemove(index0, structure.getEdgeCount());
		checkIndexForRemove(index1, structure.getEdgeCount());

		Edge edge0 = structure.getEdgeAt(index0);
		Edge edge1 = structure.getEdgeAt(index1);

		executeChange(new SerializableAtomicModelChange.EdgeMoveChange(
				structure, structure.getEdgeCount(), index0, index1, edge0, edge1));
	}

	@Override
	public Item setTerminal(Structure structure, Edge edge, Item item,
			boolean isSource) {
		checkWriteItem(structure);
		Item oldTerminal = edge.getTerminal(isSource);

		executeChange(new SerializableAtomicModelChange.TerminalChange(
				structure, edge, isSource, item, oldTerminal));

		return oldTerminal;
	}


	//---------------------------------------------
	//			EDGE METHODS
	//---------------------------------------------

	@Override
	public Structure getStructure(Edge edge) {
		checkReadItem(edge);

		return ensureNotNull(edge.getStructure());
	}

	@Override
	public Item getSource(Edge edge) {
		checkReadItem(edge);

		return ensureNotNull(edge.getSource());
	}

	@Override
	public Item getTarget(Edge edge) {
		checkReadItem(edge);

		return ensureNotNull(edge.getTarget());
	}


	//---------------------------------------------
	//			FRAGMENT METHODS
	//---------------------------------------------

	@Override
	public Item getItem(Fragment fragment) {
		checkReadItem(fragment);

		return ensureNotNull(fragment.getItem());
	}

	@Override
	public Position getFragmentBegin(Fragment fragment) {
		checkReadItem(fragment);

		return ensureNotNull(fragment.getFragmentBegin());
	}

	@Override
	public Position getFragmentEnd(Fragment fragment) {
		checkReadItem(fragment);

		return ensureNotNull(fragment.getFragmentEnd());
	}

	@Override
	public Position setFragmentBegin(Fragment fragment, Position position) {
		checkWriteItem(fragment);
		Position oldPosition = ensureNotNull(fragment.getFragmentBegin());

		executeChange(new SerializableAtomicModelChange.PositionChange(fragment, true, position));

		return oldPosition;
	}

	@Override
	public Position setFragmentEnd(Fragment fragment, Position position) {
		checkWriteItem(fragment);
		Position oldPosition = ensureNotNull(fragment.getFragmentEnd());

		executeChange(new SerializableAtomicModelChange.PositionChange(fragment, false, position));

		return oldPosition;
	}


	//---------------------------------------------
	//			ANNOTATION METHODS
	//---------------------------------------------

	@Override
	public boolean collectKeys(AnnotationLayer layer, Item item,
			Consumer<String> action) {
		checkReadItem(item);

		return layer.getAnnotationStorage().collectKeys(item, action);
	}

	@Override
	public Object getValue(AnnotationLayer layer, Item item, String key) {
		checkReadItem(item);

		return layer.getAnnotationStorage().getValue(item, key);
	}

	@Override
	public int getIntegerValue(AnnotationLayer layer, Item item, String key) {
		checkReadItem(item);

		return layer.getAnnotationStorage().getInteger(item, key);
	}

	@Override
	public long getLongValue(AnnotationLayer layer, Item item, String key) {
		checkReadItem(item);

		return layer.getAnnotationStorage().getLong(item, key);
	}

	@Override
	public float getFloatValue(AnnotationLayer layer, Item item, String key) {
		checkReadItem(item);

		return layer.getAnnotationStorage().getFloat(item, key);
	}

	@Override
	public double getDoubleValue(AnnotationLayer layer, Item item, String key) {
		checkReadItem(item);

		return layer.getAnnotationStorage().getDouble(item, key);
	}

	@Override
	public boolean getBooleanValue(AnnotationLayer layer, Item item, String key) {
		checkReadItem(item);

		return layer.getAnnotationStorage().getBoolean(item, key);
	}

	@Override
	public Object setValue(AnnotationLayer layer, Item item, String key,
			Object value) {
		checkWriteItem(item);
		Object oldValue = layer.getAnnotationStorage().getValue(item, key);
		ValueType valueType = Optional.ofNullable(layer.getManifest())
				.flatMap(m -> m.getAnnotationManifest(key))
				.map(AnnotationManifest::getValueType)
				.orElse(ValueType.DEFAULT_VALUE_TYPE); //TODO not pretty

		executeChange(new SerializableAtomicModelChange.ValueChange(
				layer, valueType, item, key, value, oldValue));

		return oldValue;
	}

	@Override
	public int setIntegerValue(AnnotationLayer layer, Item item, String key,
			int value) {
		checkWriteItem(item);
		int oldValue = layer.getAnnotationStorage().getInteger(item, key);

		executeChange(new SerializableAtomicModelChange.IntegerValueChange(
				layer, item, key, value, oldValue));

		return oldValue;
	}

	@Override
	public long setLongValue(AnnotationLayer layer, Item item, String key,
			long value) {
		checkWriteItem(item);
		long oldValue = layer.getAnnotationStorage().getLong(item, key);

		executeChange(new SerializableAtomicModelChange.LongValueChange(
				layer, item, key, value, oldValue));

		return oldValue;
	}

	@Override
	public float setFloatValue(AnnotationLayer layer, Item item, String key,
			float value) {
		checkWriteItem(item);
		float oldValue = layer.getAnnotationStorage().getFloat(item, key);

		executeChange(new SerializableAtomicModelChange.FloatValueChange(
				layer, item, key, value, oldValue));

		return oldValue;
	}

	@Override
	public double setDoubleValue(AnnotationLayer layer, Item item, String key,
			double value) {
		checkWriteItem(item);
		double oldValue = layer.getAnnotationStorage().getDouble(item, key);

		executeChange(new SerializableAtomicModelChange.DoubleValueChange(
				layer, item, key, value, oldValue));

		return oldValue;
	}

	@Override
	public boolean setBooleanValue(AnnotationLayer layer, Item item,
			String key, boolean value) {
		checkWriteItem(item);
		boolean oldValue = layer.getAnnotationStorage().getBoolean(item, key);

		executeChange(new SerializableAtomicModelChange.BooleanValueChange(
				layer, item, key, value, oldValue));

		return oldValue;
	}

	@Override
	public boolean hasAnnotations(AnnotationLayer layer) {
		checkReadAccess();

		return layer.getAnnotationStorage().hasAnnotations();
	}

	@Override
	public boolean hasAnnotations(AnnotationLayer layer, Item item) {
		checkReadItem(item);

		return layer.getAnnotationStorage().hasAnnotations(item);
	}

	/**
	 * Helper method to check whether or not the enclosing corpus is editable
	 * and to forward an atomic change to the change handler.
	 *
	 * @param change the atomic change to be executed
	 * @throws ModelException if the corpus is not editable
	 */
	protected void executeChange(AtomicChange change) {
		checkWriteAccess();

		changeHandler.accept(change);
	}

	//TODO forward destruction/reload of internal resources according to page changes
	protected class ViewObserver extends CorpusAdapter {

		@Override
		public void corpusPartDestroyed(CorpusEvent e) {
			if(e.getCorpusView()==getView()) {
				//TODO disconnect all resources!!
			}
		}

	}

	protected static class BufferedItemLookup extends ChangeSource
			implements ItemLookup, PageListener, ObjLongConsumer<Item>, Closeable {

		private boolean dataLocked = false;

		private final PageControl pageControl;
		private final LookupList<Item> items;
		private final ItemLayerManager itemLayerManager;

		public BufferedItemLookup(Object source, ItemLayerManager itemLayerManager, PageControl pageControl) {
			super(source);
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
			try {
				items.clear();
			} finally {
				dataLocked = false;
			}
		}

		protected synchronized void reload() throws InterruptedException {
			try {
				IndexSet indices = pageControl.getIndices();
				ItemLayer layer = pageControl.getView().getScope().getPrimaryLayer();
				itemLayerManager.forItems(layer, IndexUtils.wrap(indices), this);
			} catch (ModelException e) {
				// In case of problems clear storage and re-throw
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
		 * @see de.ims.icarus2.model.api.events.PageListener#pageClosing(de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl, int)
		 */
		@Override
		public void pageClosing(PageControl source, int page) {
			checkPageControl(source);

			lock();
		}

		/**
		 * @see de.ims.icarus2.model.api.events.PageListener#pageClosed(de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl, int)
		 */
		@Override
		public void pageClosed(PageControl source, int page) {
			checkPageControl(source);

			clear();

			fireStateChanged();
		}

		/**
		 * @see de.ims.icarus2.model.api.events.PageListener#pageLoading(de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl, int, int)
		 */
		@Override
		public void pageLoading(PageControl source, int page, int size) {
			checkPageControl(source);

			lock();
		}

		/**
		 * @see de.ims.icarus2.model.api.events.PageListener#pageLoaded(de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl, int, int)
		 */
		@Override
		public void pageLoaded(PageControl source, int page, int size) {
			checkPageControl(source);

			try {
				reload();

				fireStateChanged();
			} catch (InterruptedException e) {
				log.error("Loading of page was interrupted", e);
			}
		}

		/**
		 * @see de.ims.icarus2.model.api.events.PageListener#pageFailed(de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl, int, de.ims.icarus2.model.api.ModelException)
		 */
		@Override
		public void pageFailed(PageControl source, int page, IcarusApiException ex) {
			checkPageControl(source);

			clear();

			fireStateChanged();
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
		public void close() {
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
		public ContainerManifestBase<?> getManifest() {
			return layer.getManifest().getRootContainerManifest().get();
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

	@Api(type=ApiType.BUILDER)
	public static class Builder extends AbstractBuilder<Builder, DefaultCorpusModel> {

		private AccessMode accessMode;
		private ItemLayerManager itemLayerManager;
		private Consumer<AtomicChange> changeHandler;

		protected Builder() {
			// no-op
		}

		/**
		 * Allows to override the way new {@link AtomicChange changes} to the
		 * underlying corpus data are handled:
		 * <p>
		 * Each write operation on the corpus data is internally wrapped into
		 * an appropriate instance of {@link AtomicChange} by this model. The
		 * resulting {@code change} objects are then (by default) delivered
		 * to the corpus' {@link Corpus#getEditManager() edit manager} for
		 * further handling, execution and, if configured, storage.
		 * <p>
		 * If a custom {@code changeHandler} is set by this method, it will
		 * be used instead of above mentioned strategy.
		 *
		 * @param changeHandler
		 * @return
		 */
		@Guarded(methodType=MethodType.BUILDER)
		public Builder changeHandler(Consumer<AtomicChange> changeHandler) {
			requireNonNull(changeHandler);
			checkState(this.changeHandler==null);

			this.changeHandler = changeHandler;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public Consumer<AtomicChange> getChangeHandler() {
			return changeHandler;
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder accessMode(AccessMode accessMode) {
			requireNonNull(accessMode);
			checkState(this.accessMode==null);

			this.accessMode = accessMode;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public AccessMode getAccessMode() {
			return accessMode;
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder itemLayerManager(ItemLayerManager itemLayerManager) {
			requireNonNull(itemLayerManager);
			checkState(this.itemLayerManager==null);

			this.itemLayerManager = itemLayerManager;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
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
