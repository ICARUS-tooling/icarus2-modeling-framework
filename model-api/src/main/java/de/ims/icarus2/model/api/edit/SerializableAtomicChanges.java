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
package de.ims.icarus2.model.api.edit;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.CorpusMember;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.classes.ClassUtils;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
public class SerializableAtomicChanges {

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
	 *
	 * @author Markus Gärtner
	 *
	 * @deprecated maintaining the index value of an item is left to the driver implementation that manages it
	 */
	public static class IndexChange implements AtomicChange {
		protected final Item item;
		protected long expectedIndex;
		protected long newIndex;

		public IndexChange(Item item, long expectedIndex, long newIndex) {
			this.item = item;
			this.expectedIndex = expectedIndex;
			this.newIndex = newIndex;
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			long index = item.getIndex();
			if(index!=expectedIndex)
				throw new ModelException(item.getCorpus(), ModelErrorCode.MODEL_CORRUPTED_EDIT, "Expected index "+expectedIndex+" - got "+item); //$NON-NLS-1$ //$NON-NLS-2$

			// Fail-fast for invalid index values
			item.setIndex(newIndex);

			long tmp = expectedIndex;
			expectedIndex = newIndex;
			newIndex = tmp;
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return item;
		}
	}

	public static class ItemChange implements AtomicChange {

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

	public static class ItemMoveChange implements AtomicChange {

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

	public static class ItemSequenceChange implements AtomicChange {

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

	public static class EdgeChange implements AtomicChange {

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

	public static class EdgeMoveChange implements AtomicChange {

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

	public static class EdgeSequenceChange implements AtomicChange {

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

	public static class TerminalChange implements AtomicChange {

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

	public static class PositionChange implements AtomicChange {

		protected final Fragment fragment;
		protected final boolean isBegin;

		protected Position position;

		// We do not store expected position, since its implementation details are undefined

		public PositionChange(Fragment fragment, boolean isBegin, Position position) {
			this.fragment = fragment;
			this.isBegin = isBegin;
			this.position = position;
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
	 *
	 *
	 * @author Markus Gärtner
	 *
	 */
	public abstract static class AbstractValueChange implements AtomicChange {
		protected final AnnotationLayer layer;
		protected final Item item;
		protected final String key;

		public AbstractValueChange(AnnotationLayer layer, Item item, String key) {
			this.layer = layer;
			this.item = item;
			this.key = key;
		}

	}

	public static class ValueChange extends AbstractValueChange {

		protected Object value, expectedValue;

		public ValueChange(AnnotationLayer layer, Item item, String key, Object value, Object expectedValue) {
			super(layer, item, key);

			this.value = value;
			this.expectedValue = expectedValue;
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

	public static class IntegerValueChange extends AbstractValueChange {

		protected int value, expectedValue;

		public IntegerValueChange(AnnotationLayer layer, Item item, String key, int value, int expectedValue) {
			super(layer, item, key);
			this.value = value;
			this.expectedValue = expectedValue;
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

	public static class LongValueChange extends AbstractValueChange {

		protected long value, expectedValue;

		public LongValueChange(AnnotationLayer layer, Item item, String key, long value, long expectedValue) {
			super(layer, item, key);

			this.value = value;
			this.expectedValue = expectedValue;
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

	public static class FloatValueChange extends AbstractValueChange {

		protected float value, expectedValue;

		public FloatValueChange(AnnotationLayer layer, Item item, String key, float value, float expectedValue) {
			super(layer, item, key);

			this.value = value;
			this.expectedValue = expectedValue;
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

	public static class DoubleValueChange extends AbstractValueChange {

		protected double value, expectedValue;

		public DoubleValueChange(AnnotationLayer layer, Item item, String key, double value, double expectedValue) {
			super(layer, item, key);

			this.value = value;
			this.expectedValue = expectedValue;
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

	public static class BooleanValueChange extends AbstractValueChange {

		protected boolean value, expectedValue;

		public BooleanValueChange(AnnotationLayer layer, Item item, String key, boolean value, boolean expectedValue) {
			super(layer, item, key);

			this.value = value;
			this.expectedValue = expectedValue;
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

	public interface AtomicChangeWriter {

	}

	public static final class ChangeProxy {
		public AtomicChangeType type;
		public AnnotationLayer annotationLayer;
		public String key;
		public Container container;
		public Structure structure;
		public Item item1, item2, expectedItem;
		public Edge edge1, edge2;
		public Fragment fragment;
		public Position position;
		public long index1, index2, expectedSize, expectedIndex;
		public boolean add, isSource, isBegin; //TODO unify to 1 boolean field!!!
		public DataSequence<? extends Item> items;
		public DataSequence<? extends Edge> edges;
	}
}
