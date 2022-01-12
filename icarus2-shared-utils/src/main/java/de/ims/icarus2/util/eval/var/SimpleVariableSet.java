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
package de.ims.icarus2.util.eval.var;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
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
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class SimpleVariableSet implements VariableSet {

	/**
	 * Maps descriptor names to storages and descriptors
	 */
	private final Map<String, Variable> variablesLookup;

	public SimpleVariableSet(Collection<? extends VariableDescriptor> variableDescriptors) {
		requireNonNull(variableDescriptors);

		variablesLookup = new Object2ObjectOpenHashMap<>();

		for(VariableDescriptor descriptor : variableDescriptors) {
			String name = descriptor.getName();

			if(variablesLookup.containsKey(name))
				throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT, "Duplicate variable name: "+name);

			Variable variable = new Variable(createStorage(descriptor), descriptor);

			variablesLookup.put(name, variable);
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
		for(Variable variable : variablesLookup.values()) {
			action.accept(variable.descriptor);
		}
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#getVariable(java.lang.String)
	 */
	@Override
	public VariableDescriptor getVariable(String name) {
		return getVariableFor(name).descriptor;
	}

	protected Variable getVariableFor(String variableName) {
		Variable variable = variablesLookup.get(variableName);
		if(variable==null)
			throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT, "Unknown variable name: "+variableName);
		return variable;
	}

	protected Mutable<?> getStorage(String variableName) {
		return getVariableFor(variableName).storage;
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#setValue(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(String variableName, Object value) {
		Variable variable = getVariableFor(variableName);
		if(value==null && !variable.descriptor.isNullable())
			throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT, "Variable is not nullable: "+variableName);
		variable.storage.set(value);
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
		}

		Object value = storage.get();
		return (value instanceof Number) ? ((Number)value).intValue() : MutableInteger.DEFAULT_EMPTY_VALUE;
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#getShort(java.lang.String)
	 */
	@Override
	public short getShort(String name) {
		Mutable<?> storage = getStorage(name);
		if(storage.isPrimitive()) {
			return ((MutablePrimitive<?>)storage).shortValue();
		}

		Object value = storage.get();
		return (value instanceof Number) ? ((Number)value).shortValue() : MutableShort.DEFAULT_EMPTY_VALUE;
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#getLong(java.lang.String)
	 */
	@Override
	public long getLong(String name) {
		Mutable<?> storage = getStorage(name);
		if(storage.isPrimitive()) {
			return ((MutablePrimitive<?>)storage).longValue();
		}

		Object value = storage.get();
		return (value instanceof Number) ? ((Number)value).longValue() : MutableLong.DEFAULT_EMPTY_VALUE;
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#getByte(java.lang.String)
	 */
	@Override
	public byte getByte(String name) {
		Mutable<?> storage = getStorage(name);
		if(storage.isPrimitive()) {
			return ((MutablePrimitive<?>)storage).byteValue();
		}

		Object value = storage.get();
		return (value instanceof Number) ? ((Number)value).byteValue() : MutableByte.DEFAULT_EMPTY_VALUE;
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#getChar(java.lang.String)
	 */
	@Override
	public char getChar(String name) {
		Mutable<?> storage = getStorage(name);
		if(storage.isPrimitive()) {
			return ((MutablePrimitive<?>)storage).charValue();
		}

		Object value = storage.get();
		return (value instanceof Character) ? ((Character)value).charValue() : MutableChar.DEFAULT_EMPTY_VALUE;
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#getFloat(java.lang.String)
	 */
	@Override
	public float getFloat(String name) {
		Mutable<?> storage = getStorage(name);
		if(storage.isPrimitive()) {
			return ((MutablePrimitive<?>)storage).floatValue();
		}

		Object value = storage.get();
		return (value instanceof Number) ? ((Number)value).floatValue() : MutableFloat.DEFAULT_EMPTY_VALUE;
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#getDouble(java.lang.String)
	 */
	@Override
	public double getDouble(String name) {
		Mutable<?> storage = getStorage(name);
		if(storage.isPrimitive()) {
			return ((MutablePrimitive<?>)storage).doubleValue();
		}

		Object value = storage.get();
		return (value instanceof Number) ? ((Number)value).doubleValue() : MutableDouble.DEFAULT_EMPTY_VALUE;
	}

	/**
	 * @see de.ims.icarus2.util.eval.var.VariableSet#getBoolean(java.lang.String)
	 */
	@Override
	public boolean getBoolean(String name) {
		Mutable<?> storage = getStorage(name);
		if(storage.isPrimitive()) {
			return ((MutablePrimitive<?>)storage).booleanValue();
		}

		Object value = storage.get();
		return (value instanceof Boolean) ? ((Boolean)value).booleanValue() : MutableBoolean.DEFAULT_EMPTY_VALUE;
	}

	private static class Variable {
		final Mutable<?> storage;
		final VariableDescriptor descriptor;

		public Variable(Mutable<?> storage, VariableDescriptor descriptor) {
			this.storage = requireNonNull(storage);
			this.descriptor = requireNonNull(descriptor);
		}
	}
}
