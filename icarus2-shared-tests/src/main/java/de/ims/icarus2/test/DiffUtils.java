/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/*
 * $Revision: 434 $
 *
 */
package de.ims.icarus2.test;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.WeakHashMap;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

/**
 * @author Markus Gärtner
 *
 */
public final class DiffUtils {

	private DiffUtils() {
		// no-op
	}

	@SuppressWarnings("boxing")
	public static boolean deepEquals(final Object obj1, final Object obj2) throws IllegalArgumentException, IllegalAccessException {
		if (obj1 == null)
			throw new NullPointerException("Invalid obj1"); //$NON-NLS-1$
		if (obj2 == null)
			throw new NullPointerException("Invalid obj2"); //$NON-NLS-1$

		final Class<?> clazz = obj1.getClass();
		final Trace trace = new Trace(clazz, true);
		trace.visit(obj1);
		try {
			return AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {

				@Override
				public Boolean run() throws Exception {
					return getChecker(clazz).equals(trace, obj1, obj2);
				}
			});
		} catch (PrivilegedActionException e) {
			Exception cause = e.getException();
			if(cause instanceof IllegalAccessException) {
				throw (IllegalAccessException) cause;
			} else if(cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else
				throw new InternalError("Unexpected exception from privileged invocation", cause); //$NON-NLS-1$
		} finally {
			trace.leave(obj1);
		}
	}

	public static Trace deepDiff(final Object obj1, final Object obj2) throws IllegalAccessException {
		requireNonNull(obj1);
		requireNonNull(obj2);

		final Class<?> clazz = obj1.getClass();
		final Trace trace = new Trace(clazz, true);
		trace.visit(obj1);
		try {
			AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {

				@SuppressWarnings("boxing")
				@Override
				public Boolean run() throws Exception {
					return getChecker(clazz).equals(trace, obj1, obj2);
				}
			});
		} catch (PrivilegedActionException e) {
			Exception cause = e.getException();
			if(cause instanceof IllegalAccessException) {
				throw (IllegalAccessException) cause;
			} else if(cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			}
		} finally {
			trace.leave(obj1);
		}

		return trace;
	}

	private static Map<Class<?>, EqualityChecker> checkerLookup = new WeakHashMap<>();

	public static class Trace {
		private final Class<?> rootClass;
		private final Stack<Field> fields = new Stack<>();
		private final List<String> messages = new ArrayList<>();
		private final Set<Object> visited = new ReferenceOpenHashSet<>();
		private final boolean doDiff;

		Trace(Class<?> rootClass, boolean doDiff) {
			this.rootClass = rootClass;
			this.doDiff = doDiff;
		}

		Trace(Class<?> rootClass) {
			this(rootClass, false);
		}

		/**
		 * Returns {@code true} iff the given object has not yet been visited
		 */
		boolean visit(Object obj) {
			return visited.add(obj);
		}

		void leave(Object obj) {
			visited.remove(obj);
		}

		void enter(FieldHandler handler) {
			fields.push(handler.getField());
		}

		void exit(FieldHandler checker) {
			fields.pop();
		}

		String trace() {
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			sb.append(rootClass.getName());
			if(!fields.isEmpty()) {
				sb.append(": "); //$NON-NLS-1$
			}
			for(Iterator<Field> it = fields.iterator(); it.hasNext(); ) {
				sb.append(it.next().getName());
				if(it.hasNext()) {
					sb.append('.');
				}
			}
			sb.append('}');

			return sb.toString();
		}

		void addMsg(String msg) {
			messages.add(trace()+": "+msg); //$NON-NLS-1$
		}

		public Class<?> getRootClass() {
			return rootClass;
		}

		public boolean hasMessages() {
			return !messages.isEmpty();
		}

		public String getMessages() {
			return DiffUtils.toString(messages, '\n', true);
		}
	}

	private static String toString(Collection<?> collection, char delimiter, boolean brackets) {
		StringBuilder sb = new StringBuilder();

		if(brackets) {
			sb.append('{');
		}

		for(Iterator<?> i = collection.iterator(); i.hasNext(); ) {
			sb.append(i.next());
			if(i.hasNext()) {
				sb.append(delimiter);
			}
		}

		if(brackets) {
			sb.append('}');
		}

		return sb.toString();
	}

	private static EqualityChecker getChecker(final Class<?> targetClass) {
		EqualityChecker checker = checkerLookup.get(targetClass);

		if(checker==null) {
			synchronized (checkerLookup) {
				checker = checkerLookup.get(targetClass);
				if(checker==null) {
					checker = new EqualityChecker(targetClass);
					checkerLookup.put(targetClass, checker);
				}
			}
		}

		return checker;
	}

	private static class EqualityChecker {

		private EqualityChecker parent;
		private final Class<?> targetClass;
		private final List<FieldHandler> fieldHandlers = new ArrayList<>();

		private static final String DEEP_HANDLING_PREFIX = "de.ims."; //$NON-NLS-1$

		EqualityChecker(Class<?> targetClass) {
			if (targetClass == null)
				throw new NullPointerException("Invalid targetClass"); //$NON-NLS-1$

			this.targetClass = targetClass;

			Class<?> parentClass = targetClass.getSuperclass();
			if(parentClass!=null && parentClass!=Object.class) {
				parent = getChecker(parentClass);
			}

			for(Field field : targetClass.getDeclaredFields()) {
				if(Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				if(Modifier.isTransient(field.getModifiers())) {
					continue;
				}

				Class<?> type = field.getType();

				if(!field.isAccessible()) {
					field.setAccessible(true);
				}

				if(type.isPrimitive()) {
					fieldHandlers.add(new PrimitiveFieldHandler(field));
				} else if(type.isArray()) {
					if(type.getComponentType().isPrimitive()) {
						fieldHandlers.add(new PrimitiveArrayFieldHandler(field));
					} else {
						fieldHandlers.add(new ArrayFieldHandler(field));
					}
				} else if(type==Object.class || type==String.class) {
					fieldHandlers.add(new ObjectFieldHandler(field));
				} else if(Map.class.isAssignableFrom(type)) {
					fieldHandlers.add(new MapFieldHandler(field));
				} else if(List.class.isAssignableFrom(type)) {
					fieldHandlers.add(new ListFieldHandler(field));
				} else if(Collection.class.isAssignableFrom(type)) {
					fieldHandlers.add(new CollectionFieldHandler(field));
				} else if(Optional.class.equals(type)
						|| type.getName().startsWith(DEEP_HANDLING_PREFIX)) {
					// NOTE
					// Only use deep field comparison for our "own" classes!
					fieldHandlers.add(new DeepObjectFieldHandler(field));
				} else {
					fieldHandlers.add(new ObjectFieldHandler(field));
				}
			}
		}

		boolean equals(Trace trace, Object obj1, Object obj2) throws IllegalArgumentException, IllegalAccessException {

			if(parent!=null && !parent.equals(trace, obj1, obj2) && !trace.doDiff) {
				return false;
			}

			for(int i=0; i<fieldHandlers.size(); i++) {
				if(!equalsField(i, trace, obj1, obj2) && !trace.doDiff) {
					return false;
				}
			}

			return true;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return targetClass.hashCode();
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} if(obj instanceof EqualityChecker) {
				return targetClass.equals(((EqualityChecker)obj).targetClass);
			}
			return false;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "EqualityChecker@"+targetClass.getName(); //$NON-NLS-1$
		}

		boolean equalsField(int fieldIndex, Trace trace, Object obj1, Object obj2) throws IllegalArgumentException, IllegalAccessException {
			FieldHandler handler = fieldHandlers.get(fieldIndex);

			try {
				trace.enter(handler);

				return handler.equalsField(trace, obj1, obj2);
			} finally {
				trace.exit(handler);
			}
		}
	}

	private static abstract class FieldHandler {

		protected final Field field;

		FieldHandler(Field field) {
			this.field = field;
		}

		Field getField() {
			return field;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return field.hashCode();
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} if(obj instanceof FieldHandler) {
				return field.equals(((FieldHandler)obj).field);
			}
			return false;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "FieldHandler@"+field.toString(); //$NON-NLS-1$
		}

		/**
		 * Returns whether or not the values on the field this handler
		 * is responsible for are equal on the two given objects. This
		 * method assumes both arguments to be non null!
		 * @throws IllegalAccessException
		 * @throws IllegalArgumentException
		 */
		abstract boolean equalsField(Trace trace, Object obj1, Object obj2) throws IllegalArgumentException, IllegalAccessException;

		void localNull(Trace trace, Object value2) {
			trace.addMsg("Local value is null, target value is "+String.valueOf(value2)); //$NON-NLS-1$
		}

		void otherNull(Trace trace, Object value1) {
			trace.addMsg("Target value is null, local value is "+String.valueOf(value1)); //$NON-NLS-1$
		}

		void dif(Trace trace, Object value1, Object value2) {
			trace.addMsg("Local value is "+String.valueOf(value1)+", target value is "+String.valueOf(value2)); //$NON-NLS-1$ //$NON-NLS-2$
		}

		void differentClass(Trace trace, Object value1, Object value2) {
			trace.addMsg("Local class is "+value1.getClass()+", target class is "+value2.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private static class ObjectFieldHandler extends FieldHandler {

		ObjectFieldHandler(Field field) {
			super(field);
		}

		@Override
		boolean equalsField(Trace trace, Object obj1, Object obj2) throws IllegalArgumentException, IllegalAccessException {

			final Object value1 = field.get(obj1);
			final Object value2 = field.get(obj2);

			if(value1==value2) {
				return true;
			} else if(value1==null && value2==null) {
				return true;
			} else if(value1==null) {
				localNull(trace, value2);
				return false;
			} else if(value2==null) {
				otherNull(trace, value1);
				return false;
			} else if(!value1.getClass().isAssignableFrom(value2.getClass())) {
				differentClass(trace, value1, value2);
				return false;
			} else if(!equals(trace, value1, value2)) {
				dif(trace, value1, value2);
				return false;
			} else {
				return true;
			}
		}

		/**
		 * @throws IllegalAccessException if an access issue arises while using reflection to
		 * check for equality in subclass implementations
		 */
		protected boolean equals(Trace trace, Object value1, Object value2) throws IllegalArgumentException, IllegalAccessException {
			return value1.equals(value2);
		}
	}

	private static class DeepObjectFieldHandler extends ObjectFieldHandler {

		DeepObjectFieldHandler(Field field) {
			super(field);
		}

		/**
		 * @see de.ims.icarus2.util.lang.ClassUtils.ObjectFieldHandler#equals(de.ims.icarus2.util.lang.ClassUtils.Trace, java.lang.Object, java.lang.Object)
		 */
		@Override
		protected boolean equals(Trace trace, Object value1, Object value2)
				throws IllegalArgumentException, IllegalAccessException {

			// Step into value1 object graph
			if(!trace.visit(value1)) {
				return true;
			}

			try {
				return getChecker(value1.getClass()).equals(trace, value1, value2);

			} finally {
				trace.leave(value1);
			}
		}

	}

	private enum FieldType {
		INTEGER,
		LONG,
		SHORT,
		FLOAT,
		DOUBLE,
		CHARACTER,
		BYTE,
		BOOLEAN;
	}

	private static final Map<Class<?>, FieldType> types = new HashMap<>();
	static {
		types.put(int.class, FieldType.INTEGER);
		types.put(long.class, FieldType.LONG);
		types.put(short.class, FieldType.SHORT);
		types.put(float.class, FieldType.FLOAT);
		types.put(double.class, FieldType.DOUBLE);
		types.put(char.class, FieldType.CHARACTER);
		types.put(byte.class, FieldType.BYTE);
		types.put(boolean.class, FieldType.BOOLEAN);
	}

	private static class PrimitiveFieldHandler extends FieldHandler {

		private final FieldType type;

		PrimitiveFieldHandler(Field field) {
			super(field);

			type = types.get(field.getType());
		}

		@Override
		boolean equalsField(Trace trace, Object obj1, Object obj2) throws IllegalArgumentException, IllegalAccessException {
			final boolean isEqual;

			switch (type) {
			case INTEGER:
				isEqual = field.getInt(obj1)==field.getInt(obj2);
				break;

			case LONG:
				isEqual = field.getLong(obj1)==field.getLong(obj2);
				break;

			case SHORT:
				isEqual = field.getShort(obj1)==field.getShort(obj2);
				break;

			case FLOAT:
				isEqual = Float.compare(field.getFloat(obj1), field.getFloat(obj2))==0;
				break;

			case DOUBLE:
				isEqual = Double.compare(field.getDouble(obj1), field.getDouble(obj2))==0;
				break;

			case CHARACTER:
				isEqual = field.getChar(obj1)==field.getChar(obj2);
				break;

			case BYTE:
				isEqual = field.getByte(obj1)==field.getByte(obj2);
				break;

			case BOOLEAN:
				isEqual = field.getBoolean(obj1)==field.getBoolean(obj2);
				break;

			default:
				throw new IllegalStateException();
			}

			if(!isEqual) {
				dif(trace, field.get(obj1), field.get(obj2));
			}

			return isEqual;
		}

	}

	private static class ArrayFieldHandler extends ObjectFieldHandler {

		ArrayFieldHandler(Field field) {
			super(field);
		}

		void arrayLength(Trace trace, Object array1, Object array2) {
			trace.addMsg("Local array length is "+Array.getLength(array1)+", target array length is "+Array.getLength(array2)); //$NON-NLS-1$ //$NON-NLS-2$
		}

		void localNull(Trace trace, Object value2, int index) {
			trace.addMsg("Local array value at index+"+index+" is null, target value is "+String.valueOf(value2)); //$NON-NLS-1$ //$NON-NLS-2$
		}

		void otherNull(Trace trace, Object value1, int index) {
			trace.addMsg("Target array value at index+"+index+" is null, local value is "+String.valueOf(value1)); //$NON-NLS-1$ //$NON-NLS-2$
		}

		void dif(Trace trace, Object value1, Object value2, int index) {
			trace.addMsg("Local array value at index+"+index+" is "+String.valueOf(value1)+", target value is "+String.valueOf(value2)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		void differentClass(Trace trace, Object value1, Object value2, int index) {
			trace.addMsg("Local element class in array at index+"+index+" is "+value1.getClass()+", target class is "+value2.getClass()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		/**
		 * @see de.ims.icarus2.util.lang.ClassUtils.ObjectFieldHandler#equals(de.ims.icarus2.util.lang.ClassUtils.Trace, java.lang.Object, java.lang.Object)
		 */
		@Override
		protected boolean equals(Trace trace, Object value1, Object value2)
				throws IllegalArgumentException, IllegalAccessException {
			Object[] a1 = (Object[]) value1;
			Object[] a2 = (Object[]) value2;

			if(a1.length!=a2.length) {
				arrayLength(trace, a1, a2);
				return false;
			}

			for(int i=0; i<a1.length; i++) {
				Object item1 = a1[i];
				Object item2 = a2[i];

				boolean mismatch = false;

				if(item1==item2) {
					continue;
				} else if(item1==null && item2==null) {
					continue;
				} else if(item1==null) {
					localNull(trace, item2, i);
					mismatch = true;
				} else if(item2==null) {
					otherNull(trace, item1, i);
					mismatch = true;
				} else if(!item1.getClass().isAssignableFrom(item2.getClass())) {
					differentClass(trace, item1, item2, i);
					mismatch = true;
				} else if(trace.visit(item1)) {
					try {
						if(!getChecker(item1.getClass()).equals(trace, item1, item2)) {
							dif(trace, item1, item2, i);
							mismatch = true;
						}
					} finally {
						trace.leave(item1);
					}
				}

				// Break loop if we are only interested in checking the fact of difference
				if(mismatch && !trace.doDiff) {
					return false;
				}
			}

			return true;
		}

	}

	private static class PrimitiveArrayFieldHandler extends ObjectFieldHandler {

		private final FieldType type;

		PrimitiveArrayFieldHandler(Field field) {
			super(field);

			type = types.get(field.getType().getComponentType());
		}

		/**
		 * @see de.ims.icarus2.util.lang.ClassUtils.ObjectFieldHandler#equals(de.ims.icarus2.util.lang.ClassUtils.Trace, java.lang.Object, java.lang.Object)
		 */
		@Override
		protected boolean equals(Trace trace, Object value1, Object value2)
				throws IllegalArgumentException, IllegalAccessException {
			final boolean isEqual;

			switch (type) {
			case INTEGER:
				isEqual = Arrays.equals((int[]) value1, (int[]) value2);
				break;

			case LONG:
				isEqual = Arrays.equals((long[]) value1, (long[]) value2);
				break;

			case SHORT:
				isEqual = Arrays.equals((short[]) value1, (short[]) value2);
				break;

			case FLOAT:
				isEqual = Arrays.equals((float[]) value1, (float[]) value2);
				break;

			case DOUBLE:
				isEqual = Arrays.equals((double[]) value1, (double[]) value2);
				break;

			case CHARACTER:
				isEqual = Arrays.equals((char[]) value1, (char[]) value2);
				break;

			case BYTE:
				isEqual = Arrays.equals((byte[]) value1, (byte[]) value2);
				break;

			case BOOLEAN:
				isEqual = Arrays.equals((boolean[]) value1, (boolean[]) value2);
				break;

			default:
				throw new IllegalStateException();
			}

			return isEqual;
		}
	}

	private static boolean equalsCollection(Collection<?> c1, Collection<?> c2) {
		if(c1.size()!=c2.size()) {
			return false;
		}

		for(Object item : c1) {
			if(!c2.contains(item)) {
				return false;
			}
		}

		return true;
	}

	private static boolean equalsList(List<?> l1, List<?> l2) {
		if(l1.size()!=l2.size()) {
			return false;
		}

		for(int i=0; i<l1.size(); i++) {
			if(!l1.get(i).equals(l2.get(i))) {
				return false;
			}
		}

		return true;
	}

	private static boolean equalsMap(Map<?, ?> m1, Map<?, ?> m2) {
		if(m1.size()!=m2.size()) {
			return false;
		}

		for(Map.Entry<?, ?> entry : m1.entrySet()) {
			Object value1 = entry.getValue();
			Object value2 = m2.get(entry.getKey());

			if(value1==value2) {
				continue;
			} else if(value1==null || value2==null) {
				return false;
			}

			boolean equals;

			if((value1 instanceof Map) && (value2 instanceof Map)) {
				equals = equalsMap((Map<?, ?>) value1 , (Map<?, ?>) value2);
			} else  if((value1 instanceof List) && (value2 instanceof List)) {
				equals = equalsList((List<?>) value1 , (List<?>) value2);
			} else if((value1 instanceof Collection) && (value2 instanceof Collection)) {
				equals = equalsCollection((Collection<?>) value1, (Collection<?>) value2);
			} else  {
				equals = value1.equals(value2);
			}

			if(!equals) {
				return false;
			}
		}

		return true;
	}

	private static class MapFieldHandler extends ObjectFieldHandler {

		MapFieldHandler(Field field) {
			super(field);
		}

		/**
		 * @see de.ims.icarus2.util.lang.ClassUtils.ObjectFieldHandler#equals(de.ims.icarus2.util.lang.ClassUtils.Trace, java.lang.Object, java.lang.Object)
		 */
		@Override
		protected boolean equals(Trace trace, Object value1, Object value2)
				throws IllegalArgumentException, IllegalAccessException {
			Map<?, ?> m1 = (Map<?, ?>) value1;
			Map<?, ?> m2 = (Map<?, ?>) value2;

			return equalsMap(m1, m2);
		}
	}

	private static class CollectionFieldHandler extends ObjectFieldHandler {

		CollectionFieldHandler(Field field) {
			super(field);
		}

		/**
		 * @see de.ims.icarus2.util.lang.ClassUtils.ObjectFieldHandler#equals(de.ims.icarus2.util.lang.ClassUtils.Trace, java.lang.Object, java.lang.Object)
		 */
		@Override
		protected boolean equals(Trace trace, Object value1, Object value2)
				throws IllegalArgumentException, IllegalAccessException {
			Collection<?> c1 = (Collection<?>) value1;
			Collection<?> c2 = (Collection<?>) value2;

			return equalsCollection(c1, c2);
		}
	}

	private static class ListFieldHandler extends ObjectFieldHandler {

		ListFieldHandler(Field field) {
			super(field);
		}

		/**
		 * @see de.ims.icarus2.util.lang.ClassUtils.ObjectFieldHandler#equals(de.ims.icarus2.util.lang.ClassUtils.Trace, java.lang.Object, java.lang.Object)
		 */
		@Override
		protected boolean equals(Trace trace, Object value1, Object value2)
				throws IllegalArgumentException, IllegalAccessException {
			List<?> l1 = (List<?>) value1;
			List<?> l2 = (List<?>) value2;

			return equalsList(l1, l2);
		}
	}
}
