/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.util.lang.Primitives;

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
			throw new IcarusRuntimeException(GlobalErrorCode.NOT_IMPLEMENTED, "Unable to produce mutable storage for class: "+clazz);

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

	public static class MutableObject<O extends Object>
			implements Mutable<O>, Supplier<O>, Consumer<O> {

		public static final Object DEFAULT_EMPTY_VALUE = null;

		private O value;

		public MutableObject() {
			// no-op
		}

		public MutableObject(O value) {
			set(value);
		}

		@Override
		public void accept(O value) {
			this.value = value;
		}

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
		@SuppressWarnings("unchecked")
		@Override
		public Mutable<O> clone() {
			try {
				return (Mutable<O>) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new IllegalStateException("Not cloneable");
			}
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

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return isEmpty() ? "<empty>" : (value==null ? "null" : value.toString());
		}
	}
}
