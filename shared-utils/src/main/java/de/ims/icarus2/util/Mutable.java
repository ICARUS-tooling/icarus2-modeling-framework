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
package de.ims.icarus2.util;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusException;
import de.ims.icarus2.util.classes.Primitives;

/**
 * @author Markus Gärtner
 *
 */
public interface Mutable<O extends Object> extends Wrapper<O>, Cloneable {

	void set(Object value);

	void clear();

	boolean isPrimitive();

	boolean isEmpty();

	public static Mutable<?> forClass(Class<?> clazz) {
		return forClass(clazz, true);
	}

	public static Mutable<?> forClass(Class<?> clazz, boolean unwrap) {

		if(unwrap && Primitives.isPrimitiveWrapperClass(clazz)) {
			clazz = Primitives.unwrap(clazz);
		}

		Mutable<?> result = null;

		if(clazz.isPrimitive()) {
			switch (clazz.getSimpleName()) {
			case "int": result = new MutablePrimitives.MutableInteger(); break;
			case "long": result = new MutablePrimitives.MutableLong(); break;
			case "byte": result = new MutablePrimitives.MutableByte(); break;
			case "short": result = new MutablePrimitives.MutableShort(); break;
			case "float": result = new MutablePrimitives.MutableFloat(); break;
			case "double": result = new MutablePrimitives.MutableDouble(); break;
			case "char": result = new MutablePrimitives.MutableChar(); break;
			case "boolean": result = new MutablePrimitives.MutableBoolean(); break;
			case "void": result = NULL; break;

			default:
				break;
			}
		} else if(Void.class.equals(clazz)) {
			result = NULL;
		} else {
			result = new Mutable.MutableObject<Object>();
		}

		if(result==null)
			throw new IcarusException(GlobalErrorCode.NOT_IMPLEMENTED, "Unable to produce mutable storage for class: "+clazz);

		return result;
	}

	public static final Mutable<Void> NULL = new Mutable<Void>() {

		@Override
		public Void get() {
			return null;
		}

		@Override
		public void set(Object value) {
			throw new UnsupportedOperationException("Cannot set value for null container");
		}

		@Override
		public void clear() {
			// no-op
		}

		@Override
		public boolean isPrimitive() {
			return false;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}
	};

	public static class MutableObject<O extends Object> implements Mutable<O> {

		public static final Object DEFAULT_EMPTY_VALUE = null;

		private O value;

		public MutableObject() {
			// no-op
		}

		public MutableObject(O value) {
			set(value);
		}

		/**
		 * @see de.ims.icarus2.util.Wrapper#get()
		 */
		@Override
		public O get() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.Mutable#set(java.lang.Object)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public void set(Object value) {
			this.value = (O) value;
		}

		/**
		 * @see de.ims.icarus2.util.Mutable#clear()
		 */
		@SuppressWarnings("unchecked")
		@Override
		public void clear() {
			value = (O)DEFAULT_EMPTY_VALUE;
		}

		/**
		 * @see java.lang.Object#clone()
		 */
		@Override
		public Object clone() {
			return new MutableObject<>(value);
		}

		/**
		 * @see de.ims.icarus2.util.Mutable#isPrimitive()
		 */
		@Override
		public boolean isPrimitive() {
			return false;
		}

		/**
		 * @see de.ims.icarus2.util.Mutable#isEmpty()
		 */
		@Override
		public boolean isEmpty() {
			return value==DEFAULT_EMPTY_VALUE;
		}
	}
}
