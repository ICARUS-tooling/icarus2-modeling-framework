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
package de.ims.icarus2.util.eval.var;

import static de.ims.icarus2.util.Conditions.checkNotNull;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.Collection;
import java.util.function.Consumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusException;
import de.ims.icarus2.util.Mutable;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.MutableByte;
import de.ims.icarus2.util.MutablePrimitives.MutableChar;
import de.ims.icarus2.util.MutablePrimitives.MutableDouble;
import de.ims.icarus2.util.MutablePrimitives.MutableFloat;
import de.ims.icarus2.util.MutablePrimitives.MutableInteger;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.MutablePrimitive;
import de.ims.icarus2.util.MutablePrimitives.MutableShort;

/**
 * @author Markus Gärtner
 *
 */
public class SimpleVariableSet implements VariableSet {

	private final VariableDescriptor[] descriptors;
	private final Mutable<?>[] variables;

	/**
	 * Maps descriptor names to index values for above arrays
	 */
	private final TObjectIntMap<String> variablesLookup;

	public SimpleVariableSet(Collection<? extends VariableDescriptor> variableDescriptors) {
		checkNotNull(variableDescriptors);

		int size = variableDescriptors.size();

		descriptors = new VariableDescriptor[size];
		variables = new Mutable[size];

		variablesLookup = new TObjectIntHashMap<String>(size, 0.75f, -1);

		int index = 0;
		for(VariableDescriptor descriptor : variableDescriptors) {
			String name = descriptor.getName();

			if(variablesLookup.containsKey(name))
				throw new IcarusException(GlobalErrorCode.INVALID_INPUT, "Duplicate variable name: "+name);

			descriptors[index] = descriptor;
			variablesLookup.put(name, index);
			variables[index] = createStorage(descriptor);

			index++;
		}
	}

	protected Mutable<?> createStorage(VariableDescriptor descriptor) {
		return Mutable.forClass(descriptor.getNamespaceClass());
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#forEachVariable(java.util.function.Consumer)
	 */
	@Override
	public void forEachVariable(Consumer<? super VariableDescriptor> action) {
		for(int i=0; i<descriptors.length; i++) {
			action.accept(descriptors[i]);
		}
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#getVariable(java.lang.String)
	 */
	@Override
	public VariableDescriptor getVariable(String name) {
		return descriptors[getIndexFor(name)];
	}

	protected int getIndexFor(String variableName) {
		int index = variablesLookup.get(variableName);
		if(index==-1)
			throw new IcarusException(GlobalErrorCode.INVALID_INPUT, "Unknown variable name: "+variableName);
		return index;
	}

	protected final Mutable<? extends Object> getStorage(String name) {
		return variables[getIndexFor(name)];
	}

	protected final Mutable<? extends Object> storageAt(int index) {
		return variables[index];
	}

	protected final VariableDescriptor variableAt(int index) {
		return descriptors[index];
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#setValue(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(String variableName, Object value) {
		int index = getIndexFor(variableName);
		if(value==null && !variableAt(index).isNullable())
			throw new IcarusException(GlobalErrorCode.INVALID_INPUT, "Variable is not nullable: "+variableName);
		storageAt(index).set(value);
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#setByte(java.lang.String, byte)
	 */
	@Override
	public void setByte(String variableName, byte value) {
		Mutable<?> storage = getStorage(variableName);
		if(storage.isPrimitive()) {
			((MutablePrimitive<?>)storage).setByte(value);
		} else {
			storage.set(Byte.valueOf(value));
		}
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#setShort(java.lang.String, short)
	 */
	@Override
	public void setShort(String variableName, short value) {
		Mutable<?> storage = getStorage(variableName);
		if(storage.isPrimitive()) {
			((MutablePrimitive<?>)storage).setShort(value);
		} else {
			storage.set(Short.valueOf(value));
		}
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#setInteger(java.lang.String, int)
	 */
	@Override
	public void setInteger(String variableName, int value) {
		Mutable<?> storage = getStorage(variableName);
		if(storage.isPrimitive()) {
			((MutablePrimitive<?>)storage).setInt(value);
		} else {
			storage.set(Integer.valueOf(value));
		}
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#setLong(java.lang.String, long)
	 */
	@Override
	public void setLong(String variableName, long value) {
		Mutable<?> storage = getStorage(variableName);
		if(storage.isPrimitive()) {
			((MutablePrimitive<?>)storage).setLong(value);
		} else {
			storage.set(Long.valueOf(value));
		}
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#setBoolean(java.lang.String, boolean)
	 */
	@Override
	public void setBoolean(String variableName, boolean value) {
		Mutable<?> storage = getStorage(variableName);
		if(storage.isPrimitive()) {
			((MutablePrimitive<?>)storage).setBoolean(value);
		} else {
			storage.set(Boolean.valueOf(value));
		}
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#setChar(java.lang.String, char)
	 */
	@Override
	public void setChar(String variableName, char value) {
		Mutable<?> storage = getStorage(variableName);
		if(storage.isPrimitive()) {
			((MutablePrimitive<?>)storage).setChar(value);
		} else {
			storage.set(Character.valueOf(value));
		}
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#setFloat(java.lang.String, float)
	 */
	@Override
	public void setFloat(String variableName, float value) {
		Mutable<?> storage = getStorage(variableName);
		if(storage.isPrimitive()) {
			((MutablePrimitive<?>)storage).setFloat(value);
		} else {
			storage.set(Float.valueOf(value));
		}
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#setDouble(java.lang.String, double)
	 */
	@Override
	public void setDouble(String variableName, double value) {
		Mutable<?> storage = getStorage(variableName);
		if(storage.isPrimitive()) {
			((MutablePrimitive<?>)storage).setDouble(value);
		} else {
			storage.set(Double.valueOf(value));
		}
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#getValue(java.lang.String)
	 */
	@Override
	public Object getValue(String name) {
		return getStorage(name).get();
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#getInteger(java.lang.String)
	 */
	@Override
	public int getInteger(String name) {
		Mutable<?> storage = getStorage(name);
		if(storage.isPrimitive()) {
			return ((MutablePrimitive<?>)storage).intValue();
		} else {
			Object value = storage.get();
			return (value instanceof Number) ? ((Number)value).intValue() : MutableInteger.DEFAULT_EMPTY_VALUE;
		}
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#getShort(java.lang.String)
	 */
	@Override
	public short getShort(String name) {
		Mutable<?> storage = getStorage(name);
		if(storage.isPrimitive()) {
			return ((MutablePrimitive<?>)storage).shortValue();
		} else {
			Object value = storage.get();
			return (value instanceof Number) ? ((Number)value).shortValue() : MutableShort.DEFAULT_EMPTY_VALUE;
		}
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#getLong(java.lang.String)
	 */
	@Override
	public long getLong(String name) {
		Mutable<?> storage = getStorage(name);
		if(storage.isPrimitive()) {
			return ((MutablePrimitive<?>)storage).longValue();
		} else {
			Object value = storage.get();
			return (value instanceof Number) ? ((Number)value).longValue() : MutableLong.DEFAULT_EMPTY_VALUE;
		}
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#getByte(java.lang.String)
	 */
	@Override
	public byte getByte(String name) {
		Mutable<?> storage = getStorage(name);
		if(storage.isPrimitive()) {
			return ((MutablePrimitive<?>)storage).byteValue();
		} else {
			Object value = storage.get();
			return (value instanceof Number) ? ((Number)value).byteValue() : MutableByte.DEFAULT_EMPTY_VALUE;
		}
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#getChar(java.lang.String)
	 */
	@Override
	public char getChar(String name) {
		Mutable<?> storage = getStorage(name);
		if(storage.isPrimitive()) {
			return ((MutablePrimitive<?>)storage).charValue();
		} else {
			Object value = storage.get();
			return (value instanceof Character) ? ((Character)value).charValue() : MutableChar.DEFAULT_EMPTY_VALUE;
		}
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#getFloat(java.lang.String)
	 */
	@Override
	public float getFloat(String name) {
		Mutable<?> storage = getStorage(name);
		if(storage.isPrimitive()) {
			return ((MutablePrimitive<?>)storage).floatValue();
		} else {
			Object value = storage.get();
			return (value instanceof Number) ? ((Number)value).floatValue() : MutableFloat.DEFAULT_EMPTY_VALUE;
		}
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#getDouble(java.lang.String)
	 */
	@Override
	public double getDouble(String name) {
		Mutable<?> storage = getStorage(name);
		if(storage.isPrimitive()) {
			return ((MutablePrimitive<?>)storage).doubleValue();
		} else {
			Object value = storage.get();
			return (value instanceof Number) ? ((Number)value).doubleValue() : MutableDouble.DEFAULT_EMPTY_VALUE;
		}
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#getBoolean(java.lang.String)
	 */
	@Override
	public boolean getBoolean(String name) {
		Mutable<?> storage = getStorage(name);
		if(storage.isPrimitive()) {
			return ((MutablePrimitive<?>)storage).booleanValue();
		} else {
			Object value = storage.get();
			return (value instanceof Boolean) ? ((Boolean)value).booleanValue() : MutableBoolean.DEFAULT_EMPTY_VALUE;
		}
	}
}
