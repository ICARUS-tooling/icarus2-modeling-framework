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