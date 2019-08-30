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
package de.ims.icarus2.model.api.edit.io;

import java.io.IOException;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.edit.change.AtomicAddChange;
import de.ims.icarus2.model.api.edit.change.AtomicChangeType;
import de.ims.icarus2.model.api.edit.change.AtomicMoveChange;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.CorpusMember;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.api.view.paged.CorpusModel;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.seq.DataSequence;
import de.ims.icarus2.util.lang.ClassUtils;

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
					Messages.sizeMismatch(msg, expected, size));
	}

	protected static <E extends Item> void checkExpectedMember(String msg, E item, E expected) {
		if(item!=expected)
			throw new ModelException(ModelErrorCode.MODEL_CORRUPTED_EDIT,
					Messages.mismatch(msg, ModelUtils.getName(expected), ModelUtils.getName(item)));
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
	public static class ItemChange implements SerializableAtomicChange, AtomicAddChange<Item, Container> {

		private Container container;
		private Item item;
		private long index;

		private boolean add;
		private long expectedSize;

		public ItemChange(Container container, Item item, long expectedSize, long index, boolean add) {
			this.container = container;
			this.item = item;
			this.index = index;
			this.add = add;
			this.expectedSize = expectedSize;
		}

		protected ItemChange() {
			// no-op
		}

		@Override
		public AtomicChangeType getType() {
			return AtomicChangeType.ITEM_CHANGE;
		}

		@Override
		public void writeChange(ChangeWriter writer) throws IOException {
			writer.writeDefaultHeader(this);
			writer.writeBoolean(add);
			writer.writeMember(container);
			writer.writeLong(expectedSize);
			writer.writeLong(index);
			writer.writeMember(item);
		}

		@Override
		public void readChange(ChangeReader reader) throws IOException {
			reader.readDefaultHeader(this);
			add = reader.readBoolean();
			container = reader.readMember(MemberType.CONTAINER);
			expectedSize = reader.readLong();
			index = reader.readLong();
			item = reader.readMember(MemberType.ITEM);
		}

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

		@Override
		public CorpusMember getAffectedMember() {
			return container;
		}

		@Override
		public boolean isAdd() {
			return add;
		}

		@Override
		public Item getElement() {
			return item;
		}

		@Override
		public Container getContainer() {
			return container;
		}

		@Override
		public long getIndex() {
			return index;
		}
	}

	/**
	 * Models the {@link CorpusModel#swapItems(Container, long, long)} change.
	 *
	 * @author Markus Gärtner
	 *
	 * @see CorpusModel#swapItems(Container, long, long)
	 */
	public static class ItemMoveChange implements SerializableAtomicChange, AtomicMoveChange<Item, Container> {

		private Container container;
		private long index0, index1;
		private Item item0, item1;
		private long expectedSize;

		public ItemMoveChange(Container container, long expectedSize, long index0, long index1, Item item0, Item item1) {
			this.container = container;
			this.index0 = index0;
			this.index1 = index1;
			this.item0 = item0;
			this.item1 = item1;
			this.expectedSize = expectedSize;
		}

		protected ItemMoveChange() {
			// no-op
		}

		@Override
		public AtomicChangeType getType() {
			return AtomicChangeType.ITEM_MOVE_CHANGE;
		}

		@Override
		public void writeChange(ChangeWriter writer) throws IOException {
			writer.writeMember(container);
			writer.writeLong(expectedSize);
			writer.writeLong(index0);
			writer.writeMember(item0);
			writer.writeLong(index1);
			writer.writeMember(item1);
		}

		@Override
		public void readChange(ChangeReader reader) throws IOException {
			container = reader.readMember(MemberType.CONTAINER);
			expectedSize = reader.readLong();
			index0 = reader.readLong();
			item0 = reader.readMember(MemberType.ITEM);
			index1 = reader.readLong();
			item1 = reader.readMember(MemberType.ITEM);
		}

		@Override
		public void execute() {
			checkExpectedSize("Move failed", container.getItemCount(), expectedSize);

			Item currentItem0 = container.getItemAt(index0);
			Item currentItem1 = container.getItemAt(index1);

			checkExpectedMember("Move failed - item0", currentItem0, item0);
			checkExpectedMember("Move failed - item1", currentItem1, item1);

			container.swapItems(index0, index1);

			// Swap indices and expected items

			item0 = currentItem1;
			item1 = currentItem0;

			long tmp = index0;
			index0 = index1;
			index1 = tmp;
		}

		@Override
		public CorpusMember getAffectedMember() {
			return container;
		}

		@Override
		public Item getSourceElement() {
			return item0;
		}

		@Override
		public Item getTargetElement() {
			return item1;
		}

		@Override
		public Container getContainer() {
			return container;
		}

		@Override
		public long getSourceIndex() {
			return index0;
		}

		@Override
		public long getTargetIndex() {
			return index1;
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

		private Container container;
		private long index0, index1;

		private DataSequence<? extends Item> items;
		private boolean add;
		private long expectedSize;

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

		protected ItemSequenceChange() {
			// no-op
		}

		@Override
		public AtomicChangeType getType() {
			return AtomicChangeType.ITEMS_CHANGE;
		}

		@Override
		public void writeChange(ChangeWriter writer) throws IOException {
			writer.writeBoolean(add);
			writer.writeMember(container);
			writer.writeLong(expectedSize);
			writer.writeLong(index0);
			writer.writeLong(index1);
			writer.writeSequence(items);
		}

		@Override
		public void readChange(ChangeReader reader) throws IOException {
			add = reader.readBoolean();
			container = reader.readMember(MemberType.CONTAINER);
			expectedSize = reader.readLong();
			index0 = reader.readLong();
			index1 = reader.readLong();
			items = reader.readSequence(MemberType.ITEM);
		}

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
		 * @see de.ims.icarus2.model.api.edit.change.AtomicChange#getAffectedMember()
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
	public static class EdgeChange implements SerializableAtomicChange, AtomicAddChange<Edge, Structure> {

		protected Structure structure;
		protected Edge edge;
		protected long index;

		protected boolean add;
		protected long expectedSize;

		public EdgeChange(Structure structure, Edge edge, long expectedSize, long index, boolean add) {
			this.structure = structure;
			this.edge = edge;
			this.index = index;
			this.add = add;
			this.expectedSize = expectedSize;
		}

		protected EdgeChange() {
			// no-op
		}

		@Override
		public AtomicChangeType getType() {
			return AtomicChangeType.EDGE_CHANGE;
		}

		@Override
		public void writeChange(ChangeWriter writer) throws IOException {
			writer.writeBoolean(add);
			writer.writeMember(structure);
			writer.writeLong(expectedSize);
			writer.writeLong(index);
			writer.writeMember(edge);
		}

		@Override
		public void readChange(ChangeReader reader) throws IOException {
			add = reader.readBoolean();
			structure = reader.readMember(MemberType.STRUCTURE);
			expectedSize = reader.readLong();
			index = reader.readLong();
			edge = reader.readMember(MemberType.EDGE);
		}

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

		@Override
		public CorpusMember getAffectedMember() {
			return structure;
		}

		@Override
		public boolean isAdd() {
			return add;
		}

		@Override
		public Edge getElement() {
			return edge;
		}

		@Override
		public Structure getContainer() {
			return structure;
		}

		@Override
		public long getIndex() {
			return index;
		}
	}

	/**
	 * Models the {@link CorpusModel#swapEdges(Structure, long, long)} change.
	 *
	 * @author Markus Gärtner
	 *
	 * @see CorpusModel#swapEdges(Structure, long, long)
	 */
	public static class EdgeMoveChange implements SerializableAtomicChange, AtomicMoveChange<Edge, Structure> {

		private Structure structure;

		private long index0, index1;
		private Edge edge0, edge1;
		private long expectedSize;

		public EdgeMoveChange(Structure structure, long expectedSize, long index0, long index1, Edge edge0, Edge edge1) {
			this.structure = structure;
			this.index0 = index0;
			this.index1 = index1;
			this.edge0 = edge0;
			this.edge1 = edge1;
			this.expectedSize = expectedSize;
		}

		protected EdgeMoveChange() {
			// no-op
		}

		@Override
		public AtomicChangeType getType() {
			return AtomicChangeType.EDGE_MOVE_CHANGE;
		}

		@Override
		public void writeChange(ChangeWriter writer) throws IOException {
			writer.writeMember(structure);
			writer.writeLong(expectedSize);
			writer.writeLong(index0);
			writer.writeMember(edge0);
			writer.writeLong(index1);
			writer.writeMember(edge1);
		}

		@Override
		public void readChange(ChangeReader reader) throws IOException {
			structure = reader.readMember(MemberType.STRUCTURE);
			expectedSize = reader.readLong();
			index0 = reader.readLong();
			edge0 = reader.readMember(MemberType.EDGE);
			index1 = reader.readLong();
			edge1 = reader.readMember(MemberType.EDGE);
		}


		@Override
		public void execute() {
			checkExpectedSize("Move failed", structure.getEdgeCount(), expectedSize);

			Edge currentEdge0 = structure.getEdgeAt(index0);
			Edge currentEdge1 = structure.getEdgeAt(index1);

			checkExpectedMember("Move failed - edge0", currentEdge0, edge0);
			checkExpectedMember("Move failed - edge1", currentEdge1, edge1);

			structure.swapEdges(index0, index1);

			// Swap indices and expected edges

			edge0 = currentEdge1;
			edge1 = currentEdge0;

			long tmp = index0;
			index0 = index1;
			index1 = tmp;
		}

		@Override
		public CorpusMember getAffectedMember() {
			return structure;
		}

		@Override
		public Edge getSourceElement() {
			return edge0;
		}

		@Override
		public Edge getTargetElement() {
			return edge1;
		}

		@Override
		public Structure getContainer() {
			return structure;
		}

		@Override
		public long getSourceIndex() {
			return index0;
		}

		@Override
		public long getTargetIndex() {
			return index1;
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

		private Structure structure;
		private long index0, index1;

		private DataSequence<? extends Edge> edges;
		private boolean add;
		private long expectedSize;

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

		protected EdgeSequenceChange() {
			// no-op
		}

		@Override
		public AtomicChangeType getType() {
			return AtomicChangeType.EDGES_CHANGE;
		}

		@Override
		public void writeChange(ChangeWriter writer) throws IOException {
			writer.writeBoolean(add);
			writer.writeMember(structure);
			writer.writeLong(expectedSize);
			writer.writeLong(index0);
			writer.writeLong(index1);
			writer.writeSequence(edges);
		}

		@Override
		public void readChange(ChangeReader reader) throws IOException {
			add = reader.readBoolean();
			structure = reader.readMember(MemberType.STRUCTURE);
			expectedSize = reader.readLong();
			index0 = reader.readLong();
			index1 = reader.readLong();
			edges = reader.readSequence(MemberType.EDGE);

		}

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

		private Structure structure;
		private Edge edge;
		private boolean isSource;

		private Item terminal;
		private Item expected;

		public TerminalChange(Structure structure, Edge edge, boolean isSource, Item terminal, Item expected) {
			this.structure = structure;
			this.edge = edge;
			this.isSource = isSource;

			this.terminal = terminal;
			this.expected = expected;
		}

		protected TerminalChange() {
			// no-op
		}

		@Override
		public AtomicChangeType getType() {
			return AtomicChangeType.TERMINAL_CHANGE;
		}

		@Override
		public void writeChange(ChangeWriter writer) throws IOException {
			writer.writeMember(structure);
			writer.writeMember(edge);
			writer.writeBoolean(isSource);
			writer.writeMember(terminal);
			writer.writeMember(expected);
		}

		@Override
		public void readChange(ChangeReader reader) throws IOException {
			structure = reader.readMember(MemberType.STRUCTURE);
			edge = reader.readMember(MemberType.EDGE);
			isSource = reader.readBoolean();
			terminal = reader.readMember(MemberType.ITEM);
			expected = reader.readMember(MemberType.ITEM);
		}

		@Override
		public void execute() {
			checkExpectedMember("Wrong host structure", edge.getStructure(), structure);

			Item oldTerminal = isSource ? edge.getSource() : edge.getTarget();
			checkExpectedMember("Terminal change failed", oldTerminal, expected);

			structure.setTerminal(edge, terminal, isSource);

			terminal = oldTerminal;
		}

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

		private Fragment fragment;
		private boolean isBegin;

		private Position position;

		// We do not store expected position, since its implementation details are undefined

		public PositionChange(Fragment fragment, boolean isBegin, Position position) {
			this.fragment = fragment;
			this.isBegin = isBegin;
			this.position = position;
		}

		protected PositionChange() {
			// no-op
		}

		@Override
		public AtomicChangeType getType() {
			return AtomicChangeType.POSITION_CHANGE;
		}

		@Override
		public void writeChange(ChangeWriter writer) throws IOException {
			writer.writeMember(fragment);
			writer.writeBoolean(isBegin);
			writer.writePosition(position);
		}

		@Override
		public void readChange(ChangeReader reader) throws IOException {
			fragment = reader.readMember(MemberType.FRAGMENT);
			isBegin = reader.readBoolean();
			position = reader.readPosition();
		}

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
		protected AnnotationLayer layer;
		protected Item item;
		protected String key;

		public AbstractValueChange(AnnotationLayer layer, Item item, String key) {
			this.layer = layer;
			this.item = item;
			this.key = key;
		}

		protected AbstractValueChange() {
			// no-op
		}

		@Override
		public AtomicChangeType getType() {
			return AtomicChangeType.VALUE_CHANGE;
		}

		protected void defaultWriteValueChange(ChangeWriter writer) throws IOException {
			writer.writeMember(layer);
			writer.writeString(key);
			writer.writeMember(item);
		}

		protected void defaultReadValueChange(ChangeReader reader) throws IOException {
			layer = reader.readMember(MemberType.LAYER);
			key = reader.readString();
			item = reader.readMember(MemberType.ITEM);
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

		private Object value, expectedValue;
		private ValueType valueType;

		public ValueChange(AnnotationLayer layer, ValueType valueType, Item item, String key, Object value, Object expectedValue) {
			super(layer, item, key);

			this.value = value;
			this.expectedValue = expectedValue;
			this.valueType = valueType;
		}

		protected ValueChange() {
			// no-op
		}

		@Override
		public void writeChange(ChangeWriter writer) throws IOException {
			defaultWriteValueChange(writer);
			writer.writeValue(valueType, expectedValue);
			writer.writeValue(valueType, value);
		}

		@Override
		public void readChange(ChangeReader reader) throws IOException {
			defaultReadValueChange(reader);
			expectedValue = reader.readValue();
			value = reader.readValue();
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.change.AtomicChange#execute()
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

		private int value, expectedValue;

		public IntegerValueChange(AnnotationLayer layer, Item item, String key, int value, int expectedValue) {
			super(layer, item, key);
			this.value = value;
			this.expectedValue = expectedValue;
		}

		protected IntegerValueChange() {
			// no-op
		}

		@Override
		public void writeChange(ChangeWriter writer) throws IOException {
			defaultWriteValueChange(writer);
			writer.writeInt(expectedValue);
			writer.writeInt( value);
		}

		@Override
		public void readChange(ChangeReader reader) throws IOException {
			defaultReadValueChange(reader);
			expectedValue = reader.readInt();
			value = reader.readInt();
		}

		@Override
		public void execute() {
			int oldValue = layer.getAnnotationStorage().getInteger(item, key);

			if(oldValue!=expectedValue)
				throw new ModelException(layer.getCorpus(), ModelErrorCode.MODEL_CORRUPTED_EDIT,
						"Expected value "+expectedValue+" - got "+oldValue);

			layer.getAnnotationStorage().setInteger(item, key, value);

			expectedValue = value;
			value = oldValue;
		}

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

		private long value, expectedValue;

		public LongValueChange(AnnotationLayer layer, Item item, String key, long value, long expectedValue) {
			super(layer, item, key);

			this.value = value;
			this.expectedValue = expectedValue;
		}

		protected LongValueChange() {
			// no-op
		}

		@Override
		public void writeChange(ChangeWriter writer) throws IOException {
			defaultWriteValueChange(writer);
			writer.writeLong(expectedValue);
			writer.writeLong( value);
		}

		@Override
		public void readChange(ChangeReader reader) throws IOException {
			defaultReadValueChange(reader);
			expectedValue = reader.readLong();
			value = reader.readLong();
		}

		@Override
		public void execute() {
			long oldValue = layer.getAnnotationStorage().getLong(item, key);

			if(oldValue!=expectedValue)
				throw new ModelException(layer.getCorpus(), ModelErrorCode.MODEL_CORRUPTED_EDIT,
						"Expected value "+expectedValue+" - got "+oldValue);

			layer.getAnnotationStorage().setLong(item, key, value);

			expectedValue = value;
			value = oldValue;
		}

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

		private float value, expectedValue;

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
		public void writeChange(ChangeWriter writer) throws IOException {
			defaultWriteValueChange(writer);
			writer.writeFloat(expectedValue);
			writer.writeFloat( value);
		}

		@Override
		public void readChange(ChangeReader reader) throws IOException {
			defaultReadValueChange(reader);
			expectedValue = reader.readFloat();
			value = reader.readFloat();
		}

		@Override
		public void execute() {
			float oldValue = layer.getAnnotationStorage().getFloat(item, key);

			if(Float.compare(oldValue, expectedValue)!=0)
				throw new ModelException(layer.getCorpus(), ModelErrorCode.MODEL_CORRUPTED_EDIT,
						"Expected value "+expectedValue+" - got "+oldValue);

			layer.getAnnotationStorage().setFloat(item, key, value);

			expectedValue = value;
			value = oldValue;
		}

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

		private double value, expectedValue;

		public DoubleValueChange(AnnotationLayer layer, Item item, String key, double value, double expectedValue) {
			super(layer, item, key);

			this.value = value;
			this.expectedValue = expectedValue;
		}

		protected DoubleValueChange() {
			// no-op
		}

		@Override
		public void writeChange(ChangeWriter writer) throws IOException {
			defaultWriteValueChange(writer);
			writer.writeDouble(expectedValue);
			writer.writeDouble( value);
		}

		@Override
		public void readChange(ChangeReader reader) throws IOException {
			defaultReadValueChange(reader);
			expectedValue = reader.readDouble();
			value = reader.readDouble();
		}

		@Override
		public void execute() {
			double oldValue = layer.getAnnotationStorage().getDouble(item, key);

			if(Double.compare(oldValue, expectedValue)!=0)
				throw new ModelException(layer.getCorpus(), ModelErrorCode.MODEL_CORRUPTED_EDIT,
						"Expected value "+expectedValue+" - got "+oldValue);

			layer.getAnnotationStorage().setDouble(item, key, value);

			expectedValue = value;
			value = oldValue;
		}

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

		private boolean value, expectedValue;

		public BooleanValueChange(AnnotationLayer layer, Item item, String key, boolean value, boolean expectedValue) {
			super(layer, item, key);

			this.value = value;
			this.expectedValue = expectedValue;
		}

		protected BooleanValueChange() {
			// no-op
		}

		@Override
		public void writeChange(ChangeWriter writer) throws IOException {
			defaultWriteValueChange(writer);
			writer.writeBoolean(expectedValue);
			writer.writeBoolean( value);
		}

		@Override
		public void readChange(ChangeReader reader) throws IOException {
			defaultReadValueChange(reader);
			expectedValue = reader.readBoolean();
			value = reader.readBoolean();
		}

		/**
		 * @see de.ims.icarus2.model.api.edit.change.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			boolean oldValue = layer.getAnnotationStorage().getBoolean(item, key);

			if(oldValue!=expectedValue)
				throw new ModelException(layer.getCorpus(), ModelErrorCode.MODEL_CORRUPTED_EDIT,
						"Expected value "+expectedValue+" - got "+oldValue);

			layer.getAnnotationStorage().setBoolean(item, key, value);

			expectedValue = value;
			value = oldValue;
		}

		@Override
		public CorpusMember getAffectedMember() {
			return item;
		}
	}

	public static SerializableAtomicChange forType(AtomicChangeType type) {
		switch (type) {

		case ITEM_CHANGE: return new ItemChange();
		case ITEM_MOVE_CHANGE: return new ItemMoveChange();
		case ITEMS_CHANGE: return new ItemSequenceChange();
		case EDGE_CHANGE: return new EdgeChange();
		case EDGE_MOVE_CHANGE: return new EdgeMoveChange();
		case EDGES_CHANGE: return new EdgeSequenceChange();
		case POSITION_CHANGE: return new PositionChange();
		case TERMINAL_CHANGE: return new TerminalChange();
		case VALUE_CHANGE: return new ValueChange();

		default:
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Unrecognized change type: "+type);
		}
	}
}
