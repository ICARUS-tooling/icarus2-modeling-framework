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
package de.ims.icarus2.model.manifest.api;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
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
	 *
	 * @deprecated
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

	private static final ContainerType[] _values = values();

	public ContainerType[] getCompatibleTypes() {
		if(ordinal()<=1) {
			return new ContainerType[0];
		} else {
			return Arrays.copyOfRange(_values, 1, ordinal());
		}
	}

	public ContainerType[] getIncompatibleTypes() {
		if(ordinal()>=_values.length-1) {
			return new ContainerType[0];
		} else {
			return Arrays.copyOfRange(_values, ordinal()+1, _values.length);
		}
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