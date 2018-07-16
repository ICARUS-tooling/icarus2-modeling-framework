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
import static de.ims.icarus2.util.Conditions.checkState;

import java.util.Objects;

import de.ims.icarus2.model.api.edit.change.AtomicChangeType;
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
		Objects.requireNonNull(type);

		this.type = type;
	}

	// Access methods with existence check (for serialization code that reconstructs change objects from proxies)

	public AtomicChangeType getType() {
		Objects.requireNonNull(type, "Type not set");
		return type;
	}

	public AnnotationLayer getAnnotationLayer() {
		Objects.requireNonNull(annotationLayer, "Annotation layer not set");
		return annotationLayer;
	}

	public String getKey() {
		Objects.requireNonNull(key, "Key not set");
		return key;
	}

	public Container getContainer() {
		Objects.requireNonNull(container, "Container not set");
		return container;
	}

	public Structure getStructure() {
		Objects.requireNonNull(structure, "Structure not set");
		return structure;
	}

	public Item getItem1() {
		Objects.requireNonNull(item1, "Item 1 not set");
		return item1;
	}

	public Item getItem2() {
		Objects.requireNonNull(item2, "Item 2 not set");
		return item2;
	}

	public Edge getEdge1() {
		Objects.requireNonNull(edge1, "Edge 1 not set");
		return edge1;
	}

	public Edge getEdge2() {
		Objects.requireNonNull(edge2, "Edge 2 not set");
		return edge2;
	}

	public Fragment getFragment() {
		Objects.requireNonNull(fragment, "Fragment not set");
		return fragment;
	}

	public Position getPosition() {
		Objects.requireNonNull(position, "Position not set");
		return position;
	}

	public long getIndex1() {
		Objects.requireNonNull(index1, "Index 1 not set");
		return index1.longValue();
	}

	public long getIndex2() {
		Objects.requireNonNull(index2, "Index 2 not set");
		return index2.longValue();
	}

	public long getExpectedSize() {
		Objects.requireNonNull(expectedSize, "Expected size not set");
		return expectedSize.longValue();
	}

	public boolean getIsAdd() {
		Objects.requireNonNull(isAdd, "Add flag not set");
		return isAdd.booleanValue();
	}

	public boolean getIsSource() {
		Objects.requireNonNull(isSource, "Source flag not set");
		return isSource.booleanValue();
	}

	public boolean getIsBegin() {
		Objects.requireNonNull(isBegin, "Begin flag not set");
		return isBegin.booleanValue();
	}

	public DataSequence<? extends Item> getItems() {
		Objects.requireNonNull(items, "Items sequence not set");
		return items;
	}

	public DataSequence<? extends Edge> getEdges() {
		Objects.requireNonNull(edges, "Edge sequence not set");
		return edges;
	}

	public Object getValue1() {
		Objects.requireNonNull(value1, "Value 1 not set");
		return value1;
	}

	public Object getValue2() {
		Objects.requireNonNull(value2, "Value 2 not set");
		return value2;
	}

	public ValueType getValueType() {
		Objects.requireNonNull(valueType, "Value type not set");
		return valueType;
	}

	/*
	 *  Builder-style setters that verify that the specified field hasn't been
	 *  set before and that the given value is valid.
	 */


	public AtomicChangeProxy setAnnotationLayer(AnnotationLayer annotationLayer) {
		Objects.requireNonNull(annotationLayer);
		checkState("Annotation layer already set", this.annotationLayer==null);
		this.annotationLayer = annotationLayer;
		return this;
	}

	public AtomicChangeProxy setKey(String key) {
		Objects.requireNonNull(key);
		checkState("Key already set", this.key==null);
		this.key = key;
		return this;
	}

	public AtomicChangeProxy setContainer(Container container) {
		Objects.requireNonNull(container);
		checkState("Container already set", this.container==null);
		this.container = container;
		return this;
	}

	public AtomicChangeProxy setStructure(Structure structure) {
		Objects.requireNonNull(structure);
		checkState("Structure already set", this.structure==null);
		this.structure = structure;
		return this;
	}

	public AtomicChangeProxy setItem1(Item item1) {
		Objects.requireNonNull(item1);
		checkState("Item 1 already set", this.item1==null);
		this.item1 = item1;
		return this;
	}

	public AtomicChangeProxy setItem2(Item item2) {
		Objects.requireNonNull(item2);
		checkState("Item 2 already set", this.item2==null);
		this.item2 = item2;
		return this;
	}

	public AtomicChangeProxy setEdge1(Edge edge1) {
		Objects.requireNonNull(edge1);
		checkState("Edge 1 already set", this.edge1==null);
		this.edge1 = edge1;
		return this;
	}

	public AtomicChangeProxy setEdge2(Edge edge2) {
		Objects.requireNonNull(edge2);
		checkState("Edge 2 already set", this.edge2==null);
		this.edge2 = edge2;
		return this;
	}

	public AtomicChangeProxy setFragment(Fragment fragment) {
		Objects.requireNonNull(fragment);
		checkState("Fragment already set", this.fragment==null);
		this.fragment = fragment;
		return this;
	}

	public AtomicChangeProxy setPosition(Position position) {
		Objects.requireNonNull(position);
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
		Objects.requireNonNull(items);
		checkState("Items sequence already set", this.items==null);
		this.items = items;
		return this;
	}

	public AtomicChangeProxy setEdges(DataSequence<? extends Edge> edges) {
		Objects.requireNonNull(edges);
		checkState("Edge sequence already set", this.edges==null);
		this.edges = edges;
		return this;
	}

	public AtomicChangeProxy setValue1(Object value1) {
		Objects.requireNonNull(value1);
		checkState("Value 1 already set", this.value1==null);
		this.value1 = value1;
		return this;
	}

	public AtomicChangeProxy setValue2(Object value2) {
		Objects.requireNonNull(value2);
		checkState("Value 2 already set", this.value2==null);
		this.value2 = value2;
		return this;
	}

	public AtomicChangeProxy setValueType(ValueType valueType) {
		Objects.requireNonNull(valueType);
		checkState("Value type already set", this.valueType==null);
		this.valueType = valueType;
		return this;
	}
}