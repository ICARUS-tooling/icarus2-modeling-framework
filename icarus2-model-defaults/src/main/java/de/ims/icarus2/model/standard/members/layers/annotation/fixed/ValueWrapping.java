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
package de.ims.icarus2.model.standard.members.layers.annotation.fixed;

import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.MutablePrimitives;
import de.ims.icarus2.util.MutablePrimitives.MutablePrimitive;
import de.ims.icarus2.util.MutablePrimitives.Primitive;

/**
 * @author Markus Gärtner
 *
 */
public class ValueWrapping {

	// Getters

	public static int getInteger(Object value) {
		return ((Primitive<?>)value).intValue();
	}

	public static long getLong(Object value) {
		return ((Primitive<?>)value).longValue();
	}

	public static float getFloat(Object value) {
		return ((Primitive<?>)value).floatValue();
	}

	public static double getDouble(Object value) {
		return ((Primitive<?>)value).doubleValue();
	}

	public static boolean getBoolean(Object value) {
		return ((Primitive<?>)value).booleanValue();
	}

	// Setters

	public static void setInteger(Object target, int value) {
		((MutablePrimitive<?>)target).setInt(value);
	}

	public static void setLong(Object target, long value) {
		((MutablePrimitive<?>)target).setLong(value);
	}

	public static void setFloat(Object target, float value) {
		((MutablePrimitive<?>)target).setFloat(value);
	}

	public static void setDouble(Object target, double value) {
		((MutablePrimitive<?>)target).setDouble(value);
	}

	public static void setBoolean(Object target, boolean value) {
		((MutablePrimitive<?>)target).setBoolean(value);
	}

	// Wrappers

	public static Object wrapMutable(Object value, ValueType valueType) {
		if(valueType==ValueType.INTEGER) {
			value = new MutablePrimitives.MutableInteger(((Number) value).intValue());
		} else if(valueType==ValueType.LONG) {
			value = new MutablePrimitives.MutableLong(((Number) value).longValue());
		} else if(valueType==ValueType.FLOAT) {
			value = new MutablePrimitives.MutableFloat(((Number) value).floatValue());
		} else if(valueType==ValueType.DOUBLE) {
			value = new MutablePrimitives.MutableDouble(((Number) value).doubleValue());
		} else if(valueType==ValueType.BOOLEAN) {
			value = new MutablePrimitives.MutableBoolean(((Boolean) value).booleanValue());
		}

		return value;
	}
}
