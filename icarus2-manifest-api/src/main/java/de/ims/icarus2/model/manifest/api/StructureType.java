/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.api;

import static java.util.Objects.requireNonNull;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.ims.icarus2.util.EditOperation;
import de.ims.icarus2.util.strings.StringResource;

/**
 * @author Markus Gärtner
 *
 */
public enum StructureType implements StringResource {

	/**
	 * An unordered collection of nodes, not connected
	 * by any edges. This is by far the most basic type of
	 * structure.
	 */
	SET("set", false, 0, 0, 0), //$NON-NLS-1$

	/**
	 * An ordered sequence of nodes, each with at most one
	 * predecessor and successor. Edges in this structure are
	 * expected to be {@code directed} only!
	 */
	CHAIN("chain", true, 1, 1, 1), //$NON-NLS-1$

	/**
	 * A hierarchically ordered collection of nodes where each node
	 * is assigned at most one parent and is allowed to have an arbitrary
	 * number of children. All edges are {@code directed} from a parent
	 * down to the child node itself.
	 */
	TREE("tree", true, -1, 1, 1), //$NON-NLS-1$

	//TODO Add FOREST as a means to model N-best tree lists? or move that to a kind of "parallel" structure meta-type?

	/**
	 * A general graph with the only restriction that edges have to be
	 * directed.
	 */
	DIRECTED_GRAPH("directed-graph", true, -1, -1, 0), //$NON-NLS-1$

	/**
	 * Being the most unbounded and therefore most complex type a {@code GRAPH}
	 * does not pose any restrictions on nodes or edges.
	 */
	GRAPH("graph", false, -1, -1, 0); //$NON-NLS-1$

	private final int outgoingEdgeLimit;
	private final int incomingEdgeLimit;
	private final int minEdgeCount;
	private final boolean directed;
	private final Set<EditOperation> operations;
	private final String xmlForm;

	private StructureType(String xmlForm, boolean directed, int outgoingEdgeLimit,
			int incomingEdgeLimit, int minEdgeCount,
			EditOperation...operations) {

		this.xmlForm = xmlForm;
		this.outgoingEdgeLimit = outgoingEdgeLimit;
		this.incomingEdgeLimit = incomingEdgeLimit;
		this.minEdgeCount = minEdgeCount;
		this.directed = directed;

		if(operations==null || operations.length==0) {
			this.operations = EnumSet.allOf(EditOperation.class);
		} else {
			this.operations = EnumSet.noneOf(EditOperation.class);
			for(EditOperation operation : operations) {
				this.operations.add(operation);
			}
		}
	}

	/**
	 * @return the operations
	 */
	public EditOperation[] getOperations() {
		return operations.toArray(new EditOperation[operations.size()]);
	}

	/**
	 * Returns whether or not the given operation is supported on this
	 * type of structure.
	 * @param operation The operation in question
	 * @return {@code true} iff the given operation is supported on this
	 * structure type
	 * @throws NullPointerException if the {@code operation} argument
	 * is {@code null}
	 */
	public boolean supportsOperation(EditOperation operation) {
		requireNonNull(operation);

		return operations.contains(operation);
	}

	/**
	 * Returns the maximum number of child nodes a single node is
	 * allows to have in this structure type.
	 * <p>
	 * Note that this does <b>not</b> affect the root node!
	 * <p>
	 * A value of {@code -1} indicated no limit.
	 *
	 * @return the outgoingEdgeLimit
	 */
	public int getOutgoingEdgeLimit() {
		return outgoingEdgeLimit;
	}

	public boolean isLegalOutgoingEdgeCount(int count) {
		return outgoingEdgeLimit!=-1 && count<=outgoingEdgeLimit;
	}

	/**
	 * Returns the maximum number of parent nodes a single node is
	 * allows to have in this structure type.
	 * <p>
	 * Note that this does <b>not</b> affect the root node!
	 * <p>
	 * A value of {@code -1} indicated no limit.
	 *
	 * @return the incomingEdgeLimit
	 */
	public int getIncomingEdgeLimit() {
		return incomingEdgeLimit;
	}

	public boolean isLegalIncomingEdgeCount(int count) {
		return incomingEdgeLimit!=-1 && count<=incomingEdgeLimit;
	}

	/**
	 * Returns the minimum number of edges a single node must
	 * have assigned.
	 * <p>
	 * Note that this does <b>not</b> affect the root node!
	 * <p>
	 * A value of {@code -1} indicated no limit.
	 *
	 * @return the minEdgeCount
	 */
	public int getMinEdgeCount() {
		return minEdgeCount;
	}

	public boolean isLegalEdgeCount(int count) {
		return minEdgeCount!=-1 && count>=minEdgeCount;
	}

	/**
	 * @return the directed
	 */
	public boolean isDirected() {
		return directed;
	}

	/**
	 * @see de.ims.icarus2.model.util.StringResource.XmlResource#getStringValue()
	 */
	@Override
	public String getStringValue() {
		return xmlForm;
	}

	/**
	 * Returns whether or not a structure of the specified {@code target} type
	 * is a legal replacement for structures which are assigned this type.
	 *
	 *
	 * @param target
	 * @return
	 */
	public boolean isCompatibleWith(StructureType target) {
		return ordinal()>=target.ordinal();
	}

	public byte id() {
		return (byte) ordinal();
	}

	private static Map<String, StructureType> xmlLookup;

	public static StructureType parseStructureType(String s) {
		if(xmlLookup==null) {
			Map<String, StructureType> map = new HashMap<>();
			for(StructureType type : values()) {
				map.put(type.xmlForm, type);
			}
			xmlLookup = map;
		}

		return xmlLookup.get(s);
	}

	private static final StructureType[] values = values();

	public static StructureType forId(byte id) {
		return values[id];
	}
}
