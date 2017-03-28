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
 */
package de.ims.icarus2.model.api.edit.io;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.edit.AtomicChange;
import de.ims.icarus2.model.api.edit.AtomicChangeType;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.CorpusMember;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.api.view.CorpusModel;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.classes.ClassUtils;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * Provides a collection of ready-to-use implementations of {@link SerializableAtomicChange} functionality
 * that covers all the modifications possible on a corpus through the {@link CorpusModel}
 * interface.
 * <p>
 *
 * @author Markus Gärtner
 *
 */
public class SerializableAtomicModelChange {

	protected static void checkExpectedSize(String msg, long size, long expected) {
		if(size!=expected)
			throw new ModelException(ModelErrorCode.MODEL_CORRUPTED_EDIT,
					Messages.sizeMismatchMessage(msg, expected, size));
	}

	protected static <E extends Item> void checkExpectedMember(String msg, E item, E expected) {
		if(item!=expected)
			throw new ModelException(ModelErrorCode.MODEL_CORRUPTED_EDIT,
					Messages.mismatchMessage(msg, ModelUtils.getName(expected), ModelUtils.getName(item)));
	}

	/**
	 * Models the {@link CorpusModel#addItem(Container, long, Item)}
	 * and {@link CorpusModel#removeItem(Container, long)} changes.
	 *
	 *
	 * @author Markus Gärtner
	 *
	 * @see CorpusModel#addItem(Container, long, Item)
	 * @see CorpusModel#removeItem(Container, long)
	 */
	public static class ItemChange implements SerializableAtomicChange {

		protected final Container container;
		protected final Item item;
		protected final long index;

		protected boolean add;
		protected long expectedSize;

		public ItemChange(Container container, Item item, long expectedSize, long index, boolean add) {
			this.container = container;
			this.item = item;
			this.index = index;
			this.add = add;
			this.expectedSize = expectedSize;
		}

		protected ItemChange(AtomicChangeProxy proxy) {
			container = proxy.getContainer();
			index = proxy.getIndex1();
			expectedSize = proxy.getExpectedSize();
			add = proxy.getIsAdd();

			item = add ? proxy.getItem1() : null;
		}

		@Override
		public AtomicChangeProxy toProxy() {
			AtomicChangeProxy proxy = new AtomicChangeProxy(AtomicChangeType.ITEM_CHANGE)
				.setContainer(container)
				.setIndex1(index)
				.setExpectedSize(expectedSize)
				.setIsAdd(add);

			if(add) {
				proxy.setItem1(item);
			}

			return proxy;
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			checkExpectedSize("Remove/Add failed", container.getItemCount(), expectedSize);

			if(add) {
				container.addItem(index, item);
				expectedSize++;
			} else {
				checkExpectedMember("Remove failed", container.getItemAt(index), item);

				container.removeItem(index);

				expectedSize--;
			}

			add = !add;
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return container;
		}

	}

	/**
	 * Models the {@link CorpusModel#moveItem(Container, long, long)} change.
	 *
	 * @author Markus Gärtner
	 *
	 * @see CorpusModel#moveItem(Container, long, long)
	 */
	public static class ItemMoveChange implements SerializableAtomicChange {

		protected final Container container;

		protected long index0, index1;
		protected Item item0, item1;
		protected long expectedSize;

		public ItemMoveChange(Container container, long expectedSize, long index0, long index1, Item item0, Item item1) {
			this.container = container;
			this.index0 = index0;
			this.index1 = index1;
			this.item0 = item0;
			this.item1 = item1;
			this.expectedSize = expectedSize;
		}

		protected ItemMoveChange(AtomicChangeProxy proxy) {
			this(proxy.getContainer(), proxy.getExpectedSize(), proxy.getIndex1(), proxy.getIndex2(), proxy.getItem1(), proxy.getItem2());
		}

		@Override
		public AtomicChangeProxy toProxy() {
			return new AtomicChangeProxy(AtomicChangeType.ITEM_MOVE_CHANGE)
				.setContainer(container)
				.setExpectedSize(expectedSize)
				.setIndex1(index0)
				.setIndex2(index1)
				.setItem1(item0)
				.setItem2(item1);
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			checkExpectedSize("Move failed", container.getItemCount(), expectedSize);

			Item currentItem0 = container.getItemAt(index0);
			Item currentItem1 = container.getItemAt(index1);

			checkExpectedMember("Move failed - item0", currentItem0, item0);
			checkExpectedMember("Move failed - item1", currentItem1, item1);

			container.moveItem(index0, index1);

			// Swap indices and expected items

			item0 = currentItem1;
			item1 = currentItem0;

			long tmp = index0;
			index0 = index1;
			index1 = tmp;
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return container;
		}

	}

	/**
	 * Models the {@link CorpusModel#addItems(Container, long, DataSequence)}
	 * and {@link CorpusModel#removeItems(Container, long, long)} changes,
	 *
	 * @author Markus Gärtner
	 *
	 * @see CorpusModel#addItems(Container, long, DataSequence)
	 * @see CorpusModel#removeItems(Container, long, long)
	 */
	public static class ItemSequenceChange implements SerializableAtomicChange {

		protected final Container container;
		protected final long index0, index1;

		protected DataSequence<? extends Item> items;
		protected boolean add;
		protected long expectedSize;

		/**
		 * Create a new change that models adding a sequence of items to a container
		 *
		 * @param container
		 * @param index
		 * @param items
		 */
		public ItemSequenceChange(Container container, long expectedSize, long index, DataSequence<? extends Item> items) {
			this.container = container;
			this.index0 = index;
			this.index1 = index + items.entryCount();
			this.items = items;
			this.add = true;
			this.expectedSize = expectedSize;
		}

		/**
		 * Create a new change that models the removal of a sequence of items from a container.
		 *
		 * @param container
		 * @param index0
		 * @param index1
		 */
		public ItemSequenceChange(Container container, long expectedSize, long index0, long index1) {
			this.container = container;
			this.index0 = index0;
			this.index1 = index1;
			this.items = null;
			this.add = false;
			this.expectedSize = expectedSize;
		}

		protected ItemSequenceChange(AtomicChangeProxy proxy) {
			container = proxy.getContainer();
			index0 = proxy.getIndex1();
			index1 = proxy.getIndex2();
			expectedSize = proxy.getExpectedSize();

			add = proxy.getIsAdd();
			if(add) {
				items = proxy.getItems();
			}
		}

		@Override
		public AtomicChangeProxy toProxy() {
			AtomicChangeProxy proxy = new AtomicChangeProxy(AtomicChangeType.ITEMS_CHANGE)
				.setContainer(container)
				.setIndex1(index0)
				.setIndex2(index1)
				.setIsAdd(add)
				.setExpectedSize(expectedSize);

			if(add) {
				proxy.setItems(items);
			}

			return proxy;
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			checkExpectedSize("Batch Remove/Add failed", container.getItemCount(), expectedSize);

			if(add) {
				container.addItems(index0, items);
				expectedSize += items.entryCount();

				items = null;
			} else {
				items = container.removeItems(index0, index1);

				expectedSize -= items.entryCount();
			}

			add = !add;
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return container;
		}

	}

	/**
	 * Models the {@link CorpusModel#addEdge(Structure, long, Edge)} and
	 * {@link CorpusModel#removeEdge(Structure, long)} changes.
	 *
	 * @author Markus Gärtner
	 *
	 * @see CorpusModel#addEdge(Structure, long, Edge)
	 * @see CorpusModel#removeEdge(Structure, long)
	 */
	public static class EdgeChange implements SerializableAtomicChange {

		protected final Structure structure;
		protected final Edge edge;
		protected final long index;

		protected boolean add;
		protected long expectedSize;

		public EdgeChange(Structure structure, Edge edge, long expectedSize, long index, boolean add) {
			this.structure = structure;
			this.edge = edge;
			this.index = index;
			this.add = add;
			this.expectedSize = expectedSize;
		}

		protected EdgeChange(AtomicChangeProxy proxy) {
			structure = proxy.getStructure();
			index = proxy.getIndex1();
			expectedSize = proxy.getExpectedSize();
			add = proxy.getIsAdd();

			edge = add ? proxy.getEdge1() : null;
		}

		@Override
		public AtomicChangeProxy toProxy() {
			AtomicChangeProxy proxy = new AtomicChangeProxy(AtomicChangeType.ITEM_CHANGE)
				.setStructure(structure)
				.setIndex1(index)
				.setExpectedSize(expectedSize)
				.setIsAdd(add);

			if(add) {
				proxy.setEdge1(edge);
			}

			return proxy;
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			checkExpectedSize("Remove/Add failed", structure.getEdgeCount(), expectedSize);

			if(add) {
				structure.addEdge(index, edge);
				expectedSize++;
			} else {
				checkExpectedMember("Removing failed", structure.getEdgeAt(index), edge);

				structure.removeEdge(index);

				expectedSize--;
			}

			add = !add;
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return structure;
		}

	}

	/**
	 * Models the {@link CorpusModel#moveEdge(Structure, long, long)} change.
	 *
	 * @author Markus Gärtner
	 *
	 * @see CorpusModel#moveEdge(Structure, long, long)
	 */
	public static class EdgeMoveChange implements SerializableAtomicChange {

		protected final Structure structure;

		protected long index0, index1;
		protected Edge edge0, edge1;
		protected long expectedSize;

		public EdgeMoveChange(Structure structure, long expectedSize, long index0, long index1, Edge edge0, Edge edge1) {
			this.structure = structure;
			this.index0 = index0;
			this.index1 = index1;
			this.edge0 = edge0;
			this.edge1 = edge1;
			this.expectedSize = expectedSize;
		}

		protected EdgeMoveChange(AtomicChangeProxy proxy) {
			this(proxy.getStructure(), proxy.getExpectedSize(), proxy.getIndex1(), proxy.getIndex2(), proxy.getEdge1(), proxy.getEdge2());
		}

		@Override
		public AtomicChangeProxy toProxy() {
			return new AtomicChangeProxy(AtomicChangeType.EDGE_MOVE_CHANGE)
				.setStructure(structure)
				.setExpectedSize(expectedSize)
				.setIndex1(index0)
				.setIndex2(index1)
				.setEdge1(edge0)
				.setEdge2(edge1);
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			checkExpectedSize("Move failed", structure.getEdgeCount(), expectedSize);

			Edge currentEdge0 = structure.getEdgeAt(index0);
			Edge currentEdge1 = structure.getEdgeAt(index1);

			checkExpectedMember("Move failed - edge0", currentEdge0, edge0);
			checkExpectedMember("Move failed - edge1", currentEdge1, edge1);

			structure.moveEdge(index0, index1);

			// Swap indices and expected edges

			edge0 = currentEdge1;
			edge1 = currentEdge0;

			long tmp = index0;
			index0 = index1;
			index1 = tmp;
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return structure;
		}

	}

	/**
	 * Models the {@link CorpusModel#addEdges(Structure, long, DataSequence)} and
	 * {@link CorpusModel#removeEdges(Structure, long, long)} changes.
	 *
	 * @author Markus Gärtner
	 *
	 * @see CorpusModel#addEdges(Structure, long, DataSequence)
	 * @see CorpusModel#removeEdges(Structure, long, long)
	 */
	public static class EdgeSequenceChange implements SerializableAtomicChange {

		protected final Structure structure;
		protected final long index0, index1;

		protected DataSequence<? extends Edge> edges;
		protected boolean add;
		protected long expectedSize;

		public EdgeSequenceChange(Structure structure, long expectedSize, long index, DataSequence<? extends Edge> edges) {
			this.structure = structure;
			this.index0 = index;
			this.index1 = index + edges.entryCount();
			this.edges = edges;
			this.add = true;
			this.expectedSize = expectedSize;
		}

		public EdgeSequenceChange(Structure structure, long expectedSize, long index0, long index1) {
			this.structure = structure;
			this.index0 = index0;
			this.index1 = index1;
			this.edges = null;
			this.add = false;
			this.expectedSize = expectedSize;
		}

		protected EdgeSequenceChange(AtomicChangeProxy proxy) {
			structure = proxy.getStructure();
			index0 = proxy.getIndex1();
			index1 = proxy.getIndex2();
			expectedSize = proxy.getExpectedSize();

			add = proxy.getIsAdd();
			if(add) {
				edges = proxy.getEdges();
			}
		}

		@Override
		public AtomicChangeProxy toProxy() {
			AtomicChangeProxy proxy = new AtomicChangeProxy(AtomicChangeType.EDGES_CHANGE)
				.setStructure(structure)
				.setIndex1(index0)
				.setIndex2(index1)
				.setIsAdd(add)
				.setExpectedSize(expectedSize);

			if(add) {
				proxy.setEdges(edges);
			}

			return proxy;
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			checkExpectedSize("Batch Remove/Add failed", structure.getEdgeCount(), expectedSize);

			if(add) {
				structure.addEdges(index0, edges);
				expectedSize += edges.entryCount();

				edges = null;
			} else {
				edges = structure.removeEdges(index0, index1);

				expectedSize -= edges.entryCount();
			}

			add = !add;
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return structure;
		}

	}

	/**
	 * Models the {@link CorpusModel#setTerminal(Structure, Edge, Item, boolean)},
	 * {@link CorpusModel#setSource(Edge, Item)} and {@link CorpusModel#setTarget(Edge, Item)} changes.
	 *
	 * @author Markus Gärtner
	 *
	 * @see CorpusModel#setTerminal(Structure, Edge, Item, boolean)
	 * @see CorpusModel#setSource(Edge, Item)
	 * @see CorpusModel#setTarget(Edge, Item)
	 */
	public static class TerminalChange implements SerializableAtomicChange {

		protected final Structure structure;
		protected final Edge edge;
		protected final boolean isSource;

		protected Item terminal;
		protected Item expected;

		public TerminalChange(Structure structure, Edge edge, boolean isSource, Item terminal, Item expected) {
			this.structure = structure;
			this.edge = edge;
			this.isSource = isSource;

			this.terminal = terminal;
			this.expected = expected;
		}

		protected TerminalChange(AtomicChangeProxy proxy) {
			this(proxy.getStructure(), proxy.getEdge1(), proxy.getIsSource(), proxy.getItem1(), proxy.getItem2());
		}

		@Override
		public AtomicChangeProxy toProxy() {
			return new AtomicChangeProxy(AtomicChangeType.TERMINAL_CHANGE)
				.setStructure(structure)
				.setEdge1(edge)
				.setIsSource(isSource)
				.setItem1(terminal)
				.setItem2(expected);
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			checkExpectedMember("Wrong host structure", edge.getStructure(), structure);

			Item oldTerminal = isSource ? edge.getSource() : edge.getTarget();
			checkExpectedMember("Terminal change failed", oldTerminal, expected);

			structure.setTerminal(edge, terminal, isSource);

			terminal = oldTerminal;
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return edge;
		}

	}

	/**
	 * Models the {@link CorpusModel#setFragmentBegin(Fragment, Position)} and
	 * {@link CorpusModel#setFragmentEnd(Fragment, Position)} changes.
	 *
	 * @author Markus Gärtner
	 *
	 * @see CorpusModel#setFragmentBegin(Fragment, Position)
	 * @see CorpusModel#setFragmentEnd(Fragment, Position)
	 *
	 */
	public static class PositionChange implements SerializableAtomicChange {

		protected final Fragment fragment;
		protected final boolean isBegin;

		protected Position position;

		// We do not store expected position, since its implementation details are undefined

		public PositionChange(Fragment fragment, boolean isBegin, Position position) {
			this.fragment = fragment;
			this.isBegin = isBegin;
			this.position = position;
		}

		protected PositionChange(AtomicChangeProxy proxy) {
			this(proxy.getFragment(), proxy.getIsBegin(), proxy.getPosition());
		}

		@Override
		public AtomicChangeProxy toProxy() {
			return new AtomicChangeProxy(AtomicChangeType.POSITION_CHANGE)
				.setFragment(fragment)
				.setIsBegin(isBegin)
				.setPosition(position);
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			Position currentPosition = isBegin ? fragment.getFragmentBegin() : fragment.getFragmentEnd();

			if(isBegin) {
				ModelUtils.checkFragmentPositions(fragment, position, null);

				fragment.setFragmentBegin(position);
			} else {
				ModelUtils.checkFragmentPositions(fragment, null, position);

				fragment.setFragmentEnd(position);
			}

			position = currentPosition;
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return fragment;
		}

	}

	/**
	 * Provides a common base class for all kinds of value changes.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public abstract static class AbstractValueChange implements SerializableAtomicChange {
		protected final AnnotationLayer layer;
		protected final Item item;
		protected final String key;

		public AbstractValueChange(AnnotationLayer layer, Item item, String key) {
			this.layer = layer;
			this.item = item;
			this.key = key;
		}

		protected AbstractValueChange(AtomicChangeProxy proxy) {
			this(proxy.getAnnotationLayer(), proxy.getItem1(), proxy.getKey());
		}

		protected AtomicChangeProxy toProxy0(ValueType valueType, Object value1, Object value2) {
			return new AtomicChangeProxy(AtomicChangeType.VALUE_CHANGE)
			.setAnnotationLayer(layer)
			.setItem1(item)
			.setKey(key)
			.setValueType(valueType)
			.setValue1(value1)
			.setValue2(value2);
		}
	}

	/**
	 * Implements a generic {@code object-level} value change.
	 * This covers a wide range of possible {@link ValueType value types}, essentially
	 * all but the specialized types for primitive values.
	 * <p>
	 * This models the {@link CorpusModel#setValue(AnnotationLayer, Item, String, Object)}
	 * change.
	 *
	 * @author Markus Gärtner
	 *
	 * @see CorpusModel#setValue(AnnotationLayer, Item, String, Object)
	 */
	public static class ValueChange extends AbstractValueChange {

		protected Object value, expectedValue;
		protected final ValueType valueType;

		public ValueChange(AnnotationLayer layer, ValueType valueType, Item item, String key, Object value, Object expectedValue) {
			super(layer, item, key);

			this.value = value;
			this.expectedValue = expectedValue;
			this.valueType = valueType;
		}

		protected ValueChange(AtomicChangeProxy proxy) {
			this(proxy.getAnnotationLayer(), proxy.getValueType(), proxy.getItem1(),
					proxy.getKey(), proxy.getValue1(), proxy.getValue2());
		}

		@Override
		public AtomicChangeProxy toProxy() {
			return toProxy0(valueType, value, expectedValue);
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			Object oldValue = layer.getAnnotationStorage().getValue(item, key);

			if(!ClassUtils.equals(oldValue, expectedValue))
				throw new ModelException(layer.getCorpus(), ModelErrorCode.MODEL_CORRUPTED_EDIT,
						"Expected value '"+IcarusUtils.toLoggableString(expectedValue)+"' - got '"+IcarusUtils.toLoggableString(oldValue)+"'");

			layer.getAnnotationStorage().setValue(item, key, value);

			expectedValue = value;
			value = oldValue;
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return item;
		}
	}

	/**
	 * Models the {@link CorpusModel#setIntegerValue(AnnotationLayer, Item, String, int)} change.
	 *
	 * @author Markus Gärtner
	 *
	 * @see CorpusModel#setIntegerValue(AnnotationLayer, Item, String, int)
	 */
	public static class IntegerValueChange extends AbstractValueChange {

		protected int value, expectedValue;

		public IntegerValueChange(AnnotationLayer layer, Item item, String key, int value, int expectedValue) {
			super(layer, item, key);
			this.value = value;
			this.expectedValue = expectedValue;
		}

		protected IntegerValueChange(AtomicChangeProxy proxy) {
			super(proxy.getAnnotationLayer(), proxy.getItem1(), proxy.getKey());

			if(proxy.value1!=null) {
				value = ((Integer)proxy.value1).intValue();
			}

			if(proxy.value2!=null) {
				expectedValue = ((Integer)proxy.value2).intValue();
			}
		}

		@Override
		public AtomicChangeProxy toProxy() {
			return toProxy0(ValueType.INTEGER, Integer.valueOf(value), Integer.valueOf(expectedValue));
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			int oldValue = layer.getAnnotationStorage().getIntegerValue(item, key);

			if(oldValue!=expectedValue)
				throw new ModelException(layer.getCorpus(), ModelErrorCode.MODEL_CORRUPTED_EDIT,
						"Expected value "+expectedValue+" - got "+oldValue);

			layer.getAnnotationStorage().setIntegerValue(item, key, value);

			expectedValue = value;
			value = oldValue;
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return item;
		}
	}

	/**
	 * Models the {@link CorpusModel#setLongValue(AnnotationLayer, Item, String, long)} change.
	 *
	 * @author Markus Gärtner
	 *
	 * @see CorpusModel#setLongValue(AnnotationLayer, Item, String, long)
	 */
	public static class LongValueChange extends AbstractValueChange {

		protected long value, expectedValue;

		public LongValueChange(AnnotationLayer layer, Item item, String key, long value, long expectedValue) {
			super(layer, item, key);

			this.value = value;
			this.expectedValue = expectedValue;
		}

		protected LongValueChange(AtomicChangeProxy proxy) {
			super(proxy.getAnnotationLayer(), proxy.getItem1(), proxy.getKey());

			if(proxy.value1!=null) {
				value = ((Long)proxy.value1).longValue();
			}

			if(proxy.value2!=null) {
				expectedValue = ((Long)proxy.value2).longValue();
			}
		}

		@Override
		public AtomicChangeProxy toProxy() {
			return toProxy0(ValueType.LONG, Long.valueOf(value), Long.valueOf(expectedValue));
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			long oldValue = layer.getAnnotationStorage().getLongValue(item, key);

			if(oldValue!=expectedValue)
				throw new ModelException(layer.getCorpus(), ModelErrorCode.MODEL_CORRUPTED_EDIT,
						"Expected value "+expectedValue+" - got "+oldValue);

			layer.getAnnotationStorage().setLongValue(item, key, value);

			expectedValue = value;
			value = oldValue;
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return item;
		}
	}

	/**
	 * Models the {@link CorpusModel#setFloatValue(AnnotationLayer, Item, String, float)} change.
	 *
	 * @author Markus Gärtner
	 *
	 * @see CorpusModel#setFloatValue(AnnotationLayer, Item, String, float)
	 */
	public static class FloatValueChange extends AbstractValueChange {

		protected float value, expectedValue;

		public FloatValueChange(AnnotationLayer layer, Item item, String key, float value, float expectedValue) {
			super(layer, item, key);

			this.value = value;
			this.expectedValue = expectedValue;
		}

		protected FloatValueChange(AtomicChangeProxy proxy) {
			super(proxy.getAnnotationLayer(), proxy.getItem1(), proxy.getKey());

			if(proxy.value1!=null) {
				value = ((Float)proxy.value1).floatValue();
			}

			if(proxy.value2!=null) {
				expectedValue = ((Float)proxy.value2).floatValue();
			}
		}

		@Override
		public AtomicChangeProxy toProxy() {
			return toProxy0(ValueType.FLOAT, Float.valueOf(value), Float.valueOf(expectedValue));
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			float oldValue = layer.getAnnotationStorage().getFloatValue(item, key);

			if(Float.compare(oldValue, expectedValue)!=0)
				throw new ModelException(layer.getCorpus(), ModelErrorCode.MODEL_CORRUPTED_EDIT,
						"Expected value "+expectedValue+" - got "+oldValue);

			layer.getAnnotationStorage().setFloatValue(item, key, value);

			expectedValue = value;
			value = oldValue;
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return item;
		}
	}

	/**
	 * Models the {@link CorpusModel#setDoubleValue(AnnotationLayer, Item, String, double)} change.
	 *
	 * @author Markus Gärtner
	 *
	 * @see CorpusModel#setDoubleValue(AnnotationLayer, Item, String, double)
	 */
	public static class DoubleValueChange extends AbstractValueChange {

		protected double value, expectedValue;

		public DoubleValueChange(AnnotationLayer layer, Item item, String key, double value, double expectedValue) {
			super(layer, item, key);

			this.value = value;
			this.expectedValue = expectedValue;
		}

		protected DoubleValueChange(AtomicChangeProxy proxy) {
			super(proxy.getAnnotationLayer(), proxy.getItem1(), proxy.getKey());

			if(proxy.value1!=null) {
				value = ((Double)proxy.value1).doubleValue();
			}

			if(proxy.value2!=null) {
				expectedValue = ((Double)proxy.value2).doubleValue();
			}
		}

		@Override
		public AtomicChangeProxy toProxy() {
			return toProxy0(ValueType.DOUBLE, Double.valueOf(value), Double.valueOf(expectedValue));
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			double oldValue = layer.getAnnotationStorage().getDoubleValue(item, key);

			if(Double.compare(oldValue, expectedValue)!=0)
				throw new ModelException(layer.getCorpus(), ModelErrorCode.MODEL_CORRUPTED_EDIT,
						"Expected value "+expectedValue+" - got "+oldValue);

			layer.getAnnotationStorage().setDoubleValue(item, key, value);

			expectedValue = value;
			value = oldValue;
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return item;
		}
	}

	/**
	 * Models the {@link CorpusModel#setBooleanValue(AnnotationLayer, Item, String, boolean)} change.
	 *
	 * @author Markus Gärtner
	 *
	 * @see CorpusModel#setBooleanValue(AnnotationLayer, Item, String, boolean)
	 */
	public static class BooleanValueChange extends AbstractValueChange {

		protected boolean value, expectedValue;

		public BooleanValueChange(AnnotationLayer layer, Item item, String key, boolean value, boolean expectedValue) {
			super(layer, item, key);

			this.value = value;
			this.expectedValue = expectedValue;
		}

		protected BooleanValueChange(AtomicChangeProxy proxy) {
			super(proxy.getAnnotationLayer(), proxy.getItem1(), proxy.getKey());

			if(proxy.value1!=null) {
				value = ((Boolean)proxy.value1).booleanValue();
			}

			if(proxy.value2!=null) {
				expectedValue = ((Boolean)proxy.value2).booleanValue();
			}
		}

		@Override
		public AtomicChangeProxy toProxy() {
			return toProxy0(ValueType.BOOLEAN, Boolean.valueOf(value), Boolean.valueOf(expectedValue));
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			boolean oldValue = layer.getAnnotationStorage().getBooleanValue(item, key);

			if(oldValue!=expectedValue)
				throw new ModelException(layer.getCorpus(), ModelErrorCode.MODEL_CORRUPTED_EDIT,
						"Expected value "+expectedValue+" - got "+oldValue);

			layer.getAnnotationStorage().setBooleanValue(item, key, value);

			expectedValue = value;
			value = oldValue;
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return item;
		}
	}

	public static AtomicChange fromProxy(AtomicChangeProxy proxy) {
		requireNonNull(proxy);

		switch (proxy.getType()) {

		case ITEM_CHANGE: return new ItemChange(proxy);
		case ITEM_MOVE_CHANGE: return new ItemMoveChange(proxy);
		case ITEMS_CHANGE: return new ItemSequenceChange(proxy);
		case EDGE_CHANGE: return new EdgeChange(proxy);
		case EDGE_MOVE_CHANGE: return new EdgeMoveChange(proxy);
		case EDGES_CHANGE: return new EdgeSequenceChange(proxy);
		case POSITION_CHANGE: return new PositionChange(proxy);
		case TERMINAL_CHANGE: return new TerminalChange(proxy);
		case VALUE_CHANGE: {
			ValueType valueType = proxy.getValueType();
			switch (valueType.getStringValue().toLowerCase()) {

			// For "primitive" annotations use the specialized change implementations
			case ValueType.INTEGER_TYPE_LABEL: return new IntegerValueChange(proxy);
			case ValueType.LONG_TYPE_LABEL: return new LongValueChange(proxy);
			case ValueType.FLOAT_TYPE_LABEL: return new FloatValueChange(proxy);
			case ValueType.DOUBLE_TYPE_LABEL: return new DoubleValueChange(proxy);
			case ValueType.BOOLEAN_TYPE_LABEL: return new BooleanValueChange(proxy);

			// For anything else use the object based fallback
			default:
				return new ValueChange(proxy);
			}
		}

		default:
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Unrecognized change type: "+proxy.getType());
		}
	}
}
