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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotNull;
import static de.ims.icarus2.util.Conditions.checkState;
import de.ims.icarus2.model.api.edit.AtomicChangeType;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * Models a proxy usable for serialization that offers a unified carrier
 * object for all the information encoded in an arbitrary change to a
 * corpus resource.
 * <p>
 * Note that the content of a proxy objects is only valid for the moment
 * it gets passed to whatever serialization facility is being used! Such
 * facilities should <b>not</b> keep the proxy objects and attempt to
 * serialize it at a later time, since the corpus members (like
 * {@link Item items}, {@link Container containers}, and others referenced
 * by the proxy fields, might change during that time, corrupting the
 * serialization process.
 * <p>
 *
 *
 * @author Markus Gärtner
 *
 */
public final class AtomicChangeProxy {

	public final  AtomicChangeType type;


	public AnnotationLayer annotationLayer;
	public String key;
	public Container container;
	public Structure structure;
	public Item item1, item2;
	public Edge edge1, edge2;
	public Fragment fragment;
	public Position position;
	public Long index1, index2, expectedSize;
	public Boolean isAdd, isSource, isBegin;
	public DataSequence<? extends Item> items;
	public DataSequence<? extends Edge> edges;
	public Object value1, value2;

	/**
	 * Usable for value changes in order to not rely on {@code instanceof}
	 * checks for determining serialization type of the {@link #value1}
	 * and {@link #value2} fields.
	 */
	public ValueType valueType;

	public AtomicChangeProxy(AtomicChangeType type) {
		checkNotNull(type);

		this.type = type;
	}

	// Access methods with existence check (for serialization code that reconstructs change objects from proxies)

	public AtomicChangeType getType() {
		checkNotNull("Type not set", type);
		return type;
	}

	public AnnotationLayer getAnnotationLayer() {
		checkNotNull("Annotation layer not set", annotationLayer);
		return annotationLayer;
	}

	public String getKey() {
		checkNotNull("Key not set", key);
		return key;
	}

	public Container getContainer() {
		checkNotNull("Container not set", container);
		return container;
	}

	public Structure getStructure() {
		checkNotNull("Structure not set", structure);
		return structure;
	}

	public Item getItem1() {
		checkNotNull("Item 1 not set", item1);
		return item1;
	}

	public Item getItem2() {
		checkNotNull("Item 2 not set", item2);
		return item2;
	}

	public Edge getEdge1() {
		checkNotNull("Edge 1 not set", edge1);
		return edge1;
	}

	public Edge getEdge2() {
		checkNotNull("Edge 2 not set", edge2);
		return edge2;
	}

	public Fragment getFragment() {
		checkNotNull("Fragment not set", fragment);
		return fragment;
	}

	public Position getPosition() {
		checkNotNull("Position not set", position);
		return position;
	}

	public long getIndex1() {
		checkNotNull("Index 1 not set", index1);
		return index1.longValue();
	}

	public long getIndex2() {
		checkNotNull("Index 2 not set", index2);
		return index2.longValue();
	}

	public long getExpectedSize() {
		checkNotNull("Expected size not set", expectedSize);
		return expectedSize.longValue();
	}

	public boolean getIsAdd() {
		checkNotNull("Add flag not set", isAdd);
		return isAdd.booleanValue();
	}

	public boolean getIsSource() {
		checkNotNull("Source flag not set", isSource);
		return isSource.booleanValue();
	}

	public boolean getIsBegin() {
		checkNotNull("Begin flag not set", isBegin);
		return isBegin.booleanValue();
	}

	public DataSequence<? extends Item> getItems() {
		checkNotNull("Items sequence not set", items);
		return items;
	}

	public DataSequence<? extends Edge> getEdges() {
		checkNotNull("Edge sequence not set", edges);
		return edges;
	}

	public Object getValue1() {
		checkNotNull("Value 1 not set", value1);
		return value1;
	}

	public Object getValue2() {
		checkNotNull("Value 2 not set", value2);
		return value2;
	}

	public ValueType getValueType() {
		checkNotNull("Value type not set", valueType);
		return valueType;
	}

	/*
	 *  Builder-style setters that verify that the specified field hasn't been
	 *  set before and that the given value is valid.
	 */


	public AtomicChangeProxy setAnnotationLayer(AnnotationLayer annotationLayer) {
		checkNotNull(annotationLayer);
		checkState("Annotation layer already set", this.annotationLayer==null);
		this.annotationLayer = annotationLayer;
		return this;
	}

	public AtomicChangeProxy setKey(String key) {
		checkNotNull(key);
		checkState("Key already set", this.key==null);
		this.key = key;
		return this;
	}

	public AtomicChangeProxy setContainer(Container container) {
		checkNotNull(container);
		checkState("Container already set", this.container==null);
		this.container = container;
		return this;
	}

	public AtomicChangeProxy setStructure(Structure structure) {
		checkNotNull(structure);
		checkState("Structure already set", this.structure==null);
		this.structure = structure;
		return this;
	}

	public AtomicChangeProxy setItem1(Item item1) {
		checkNotNull(item1);
		checkState("Item 1 already set", this.item1==null);
		this.item1 = item1;
		return this;
	}

	public AtomicChangeProxy setItem2(Item item2) {
		checkNotNull(item2);
		checkState("Item 2 already set", this.item2==null);
		this.item2 = item2;
		return this;
	}

	public AtomicChangeProxy setEdge1(Edge edge1) {
		checkNotNull(edge1);
		checkState("Edge 1 already set", this.edge1==null);
		this.edge1 = edge1;
		return this;
	}

	public AtomicChangeProxy setEdge2(Edge edge2) {
		checkNotNull(edge2);
		checkState("Edge 2 already set", this.edge2==null);
		this.edge2 = edge2;
		return this;
	}

	public AtomicChangeProxy setFragment(Fragment fragment) {
		checkNotNull(fragment);
		checkState("Fragment already set", this.fragment==null);
		this.fragment = fragment;
		return this;
	}

	public AtomicChangeProxy setPosition(Position position) {
		checkNotNull(position);
		checkState("Position already set", this.position==null);
		this.position = position;
		return this;
	}

	public AtomicChangeProxy setIndex1(long index1) {
		checkArgument(index1>=0);
		checkState("Index 1 already set", this.index1==null);
		this.index1 = Long.valueOf(index1);
		return this;
	}

	public AtomicChangeProxy setIndex2(long index2) {
		checkArgument(index2>=0);
		checkState("index 2 already set", this.index2==null);
		this.index2 = Long.valueOf(index2);
		return this;
	}

	public AtomicChangeProxy setExpectedSize(long expectedSize) {
		checkArgument(expectedSize>=0);
		checkState("Expected size already set", this.expectedSize==null);
		this.expectedSize = Long.valueOf(expectedSize);
		return this;
	}

	public AtomicChangeProxy setIsAdd(boolean isAdd) {
		checkState("Add flag already set", this.isAdd==null);
		this.isAdd = Boolean.valueOf(isAdd);
		return this;
	}

	public AtomicChangeProxy setIsSource(boolean isSource) {
		checkState("Source flag already set", this.isSource==null);
		this.isSource = Boolean.valueOf(isSource);
		return this;
	}

	public AtomicChangeProxy setIsBegin(boolean isBegin) {
		checkState("Begin flag already set", this.isBegin==null);
		this.isBegin = Boolean.valueOf(isBegin);
		return this;
	}

	public AtomicChangeProxy setItems(DataSequence<? extends Item> items) {
		checkNotNull(items);
		checkState("Items sequence already set", this.items==null);
		this.items = items;
		return this;
	}

	public AtomicChangeProxy setEdges(DataSequence<? extends Edge> edges) {
		checkNotNull(edges);
		checkState("Edge sequence already set", this.edges==null);
		this.edges = edges;
		return this;
	}

	public AtomicChangeProxy setValue1(Object value1) {
		checkNotNull(value1);
		checkState("Value 1 already set", this.value1==null);
		this.value1 = value1;
		return this;
	}

	public AtomicChangeProxy setValue2(Object value2) {
		checkNotNull(value2);
		checkState("Value 2 already set", this.value2==null);
		this.value2 = value2;
		return this;
	}

	public AtomicChangeProxy setValueType(ValueType valueType) {
		checkNotNull(valueType);
		checkState("Value type already set", this.valueType==null);
		this.valueType = valueType;
		return this;
	}
}