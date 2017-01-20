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
package de.ims.icarus2.model.manifest.api;

import static java.util.Objects.requireNonNull;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import de.ims.icarus2.util.EditOperation;
import de.ims.icarus2.util.strings.StringResource;

/**
 *
 * @author Markus Gärtner
 *
 */
public enum ContainerType implements StringResource {

	/**
	 * Special type of container that only exists as bridge between the top-level
	 * members of a layer and the layer itself. Containers of this type will always
	 * have a size of {@code 0} and will not support any {@link EditOperation} whatsoever.
	 */
	@Deprecated
	PROXY("proxy", 0, 0),

	/**
	 * The container holds a single {@code Item}.
	 */
	SINGLETON("singleton", 0, 1, EditOperation.CLEAR, EditOperation.ADD, EditOperation.REMOVE), //$NON-NLS-1$

//	/**
//	 * The container holds a non-continuous collection
//	 * of {@code Item}s. The elements may appear in
//	 * any order.
//	 *
//	 * @deprecated There is currently no reason to do the hassle of implementing
//	 * a somehow "random" order container that still allows indexed access to its
//	 * members
//	 */
//	@Deprecated
//	SET("set", 0, -1), //$NON-NLS-1$

	/**
	 * The container holds an ordered and continuous list
	 * of {@code Item}s.
	 */
	SPAN("span", 0, -1, EditOperation.CLEAR, EditOperation.ADD, EditOperation.REMOVE), //$NON-NLS-1$

	/**
	 * The container holds a non-continuous but ordered
	 * collection of {@code Item}s.
	 */
	LIST("list", 0, -1), //$NON-NLS-1$
	;

	private final EnumSet<EditOperation> operations;
	private final int minSize, maxSize;
	private final String xmlForm;

	private ContainerType(String xmlForm, int minSize, int maxSize, EditOperation...operations) {
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.xmlForm = xmlForm;

		if(operations==null || operations.length==0) {
			this.operations = EnumSet.allOf(EditOperation.class);
		} else {
			this.operations = EnumSet.noneOf(EditOperation.class);
			for(EditOperation operation : operations) {
				this.operations.add(operation);
			}
		}

		// Make sure no container ever allows the LINK action!
		this.operations.remove(EditOperation.LINK);
	}

	/**
	 * @return the operations supported by this container type in no particular order
	 */
	public EditOperation[] getOperations() {
		return operations.toArray(new EditOperation[operations.size()]);
	}

	/**
	 * Returns whether or not the given operation is supported on this
	 * type of container.
	 * @param operation The operation in question
	 * @return {@code true} iff the given operation is supported on this
	 * container type
	 * @throws NullPointerException if the {@code operation} argument
	 * is {@code null}
	 */
	public boolean supportsOperation(EditOperation operation) {
		requireNonNull(operation);

		return operations.contains(operation);
	}

	/**
	 * Returns the minimum allowed size of the container
	 * @return the minSize
	 */
	public int getMinSize() {
		return minSize;
	}

	/**
	 * Returns the maximum allowed size of the container.
	 * A return value of {@code -1} means that the container does
	 * not have an upper limit to its size.
	 *
	 * @return the maxSize
	 */
	public int getMaxSize() {
		return maxSize;
	}

	/**
	 * @see de.ims.icarus2.model.util.StringResource.XmlResource#getStringValue()
	 */
	@Override
	public String getStringValue() {
		return xmlForm;
	}

	/**
	 * Returns whether or not a container of the specified {@code target} type
	 * is a legal replacement for containers which are assigned this type.
	 *
	 *
	 * @param target
	 * @return
	 */
	public boolean isCompatibleWith(ContainerType target) {
		return ordinal()>=target.ordinal();
	}

	public byte id() {
		return (byte) ordinal();
	}

	private static Map<String, ContainerType> xmlLookup;

	public static ContainerType parseContainerType(String s) {
		if(xmlLookup==null) {
			Map<String, ContainerType> map = new HashMap<>();
			for(ContainerType type : values()) {
				map.put(type.xmlForm, type);
			}
			xmlLookup = map;
		}

		return xmlLookup.get(s);
	}

	private static final ContainerType[] values = values();

	public static ContainerType forId(byte id) {
		return values[id];
	}
}