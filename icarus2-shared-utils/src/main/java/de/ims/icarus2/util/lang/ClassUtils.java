/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.lang;

import static de.ims.icarus2.util.lang.Primitives.isPrimitiveWrapperClass;
import static java.util.Objects.requireNonNull;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * @author Markus Gärtner
 *
 */
public final class ClassUtils {

	private ClassUtils() {
		// no-op
	}

	public static boolean equals(Object o1, Object o2) {
		return o1==null ? o2==null : o1.equals(o2);
	}

	public static <E extends Enum<E>> E max(E enum1, E enum2) {
		return enum2.ordinal()>enum1.ordinal() ? enum2 : enum1;
	}

	public static <E extends Enum<E>> E min(E enum1, E enum2) {
		return enum2.ordinal()<enum1.ordinal() ? enum2 : enum1;
	}

	public static Object instantiate(Object source) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (source == null)
			throw new NullPointerException("Invalid source"); //$NON-NLS-1$

		if(source instanceof String) {
			source = Class.forName((String) source);
		}

		if(source instanceof ClassProxy) {
			source = ((ClassProxy)source).loadClass();
		}

		if(source instanceof Class) {
			return ((Class<?>) source).newInstance();
		}

		return source;
	}

	/**
	 * Get the underlying class for a type, or null if the type is a variable
	 * type.
	 *
	 * @param type
	 *            the type
	 * @return the underlying class
	 */
	public static Class<?> getClass(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			return getClass(((ParameterizedType) type).getRawType());
		} else if (type instanceof GenericArrayType) {
			Type componentType = ((GenericArrayType) type)
					.getGenericComponentType();
			Class<?> componentClass = getClass(componentType);
			if (componentClass != null) {
				return Array.newInstance(componentClass, 0).getClass();
			}
			return null;
		} else {
			return null;
		}
	}

	/**
	 * Get the actual type arguments a child class has used to extend a generic
	 * base class.
	 *
	 * @param baseClass
	 *            the base class
	 * @param childClass
	 *            the child class
	 * @return a list of the raw classes for the actual type arguments.
	 */
	public static <T> List<Class<?>> getTypeArguments(Class<T> baseClass,
			Class<? extends T> childClass) {
		Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
		Type type = childClass;
		// start walking up the inheritance hierarchy until we hit baseClass
		while (!getClass(type).equals(baseClass)) {
			if (type instanceof Class) {
				// there is no useful information for us in raw types, so just
				// keep going.
				type = ((Class<?>) type).getGenericSuperclass();
			} else {
				ParameterizedType parameterizedType = (ParameterizedType) type;
				Class<?> rawType = (Class<?>) parameterizedType.getRawType();

				Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
				TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
				for (int i = 0; i < actualTypeArguments.length; i++) {
					resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
				}

				if (!rawType.equals(baseClass)) {
					type = rawType.getGenericSuperclass();
				}
			}
		}

		// finally, for each actual type argument provided to baseClass,
		// determine (if possible)
		// the raw class for that type argument.
		Type[] actualTypeArguments;
		if (type instanceof Class) {
			actualTypeArguments = ((Class<?>) type).getTypeParameters();
		} else {
			actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
		}
		List<Class<?>> typeArgumentsAsClasses = new ArrayList<Class<?>>();
		// resolve types by chasing down type variables.
		for (Type baseType : actualTypeArguments) {
			while (resolvedTypes.containsKey(baseType)) {
				baseType = resolvedTypes.get(baseType);
			}
			typeArgumentsAsClasses.add(getClass(baseType));
		}
		return typeArgumentsAsClasses;
	}

	private static class SerializationBuffer extends ByteArrayInputStream {
		final ByteArrayOutputStream out;
		final ObjectOutputStream objOut;
		ObjectInputStream objIn;

		SerializationBuffer() {
			super(new byte[1<<10]);

			out = new ByteArrayOutputStream(1<<12);
			try {
				objOut = new ObjectOutputStream(new BufferedOutputStream(out, 1<<10));
			} catch (IOException e) {
				throw new InternalError();
			}
		}

		public void reset(byte[] b) {
			buf = b;
			count = b.length;
			pos = 0;
			mark = 0;
		}

		public Object clone(Serializable source) throws IOException, ClassNotFoundException {
			out.reset();
			objOut.reset();

			objOut.writeObject(source);
			objOut.flush();

			reset(out.toByteArray());
			if(objIn==null) {
				objIn = new ObjectInputStream(this);
			}

			return objIn.readObject();
		}
	}

	private static ThreadLocal<SerializationBuffer> _serBuf = ThreadLocal.withInitial(SerializationBuffer::new);

	/**
	 * Tries to create a clone of the provided {@code source} object.
	 * This method uses the following strategy:
	 * <ol>
	 * <li>If {@code source} implements {@link Singleton} return it</li>
	 * <li>If {@code source} extends {@link Number} return it</li>
	 * <li>If {@code source} is of type {@link String} return it</li>
	 * <li>If {@code source} implements {@link Cloneable}, use reflection
	 * to access its public {@code clone()} method and call it</li>
	 * <li>If {@code source} implements {@link Serializable}, use some helper
	 * code to serialize it and then deserialize the binary form into a new
	 * object of the same class.</li>
	 * </ol>
	 * Note that especially the last option is potentially very costly and as
	 * such this method should be used rather carefully when performance is of
	 * crucial role.
	 *
	 * @param source
	 * @return
	 * @throws CloneNotSupportedException if all listed attempts of creating a clone fail
	 */
	@SuppressWarnings("resource")
	public static Object clone(Object source) throws CloneNotSupportedException {
		requireNonNull(source);

		Class<?> clazz = source.getClass();

		if(Singleton.class.isAssignableFrom(clazz) || clazz==String.class
				|| Number.class.isAssignableFrom(clazz)
				|| isPrimitiveWrapperClass(clazz)) {
			return source;
		}

		if(Cloneable.class.isAssignableFrom(clazz)) {
			try {
				Method m = source.getClass().getMethod("clone");
				return m.invoke(source);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				throw new UnsupportedOperationException("No working clone() method on target obejct: "+source, e);
			}
		}

		if(Serializable.class.isAssignableFrom(clazz)) {
			SerializationBuffer buf = _serBuf.get();
			try {
				return buf.clone((Serializable) source);
			} catch (ClassNotFoundException | IOException e) {
				throw new UnsupportedOperationException("Cloning via serialization failed for target object: "+source, e);
			}
		}

		throw new CloneNotSupportedException("Cannot clone object: "+source);
	}

	/**
	 * Weaker version of {@link #clone(Object)} that returns the original {@code source}
	 * in case the cloning attempt fails.
	 *
	 * @param source
	 * @return
	 */
	public static Object tryClone(Object source) {
		try {
			return clone(source);
		} catch (CloneNotSupportedException e) {
			// Best effort, just return original object if we can't clone...
			return source;
		}
	}

	/*
	 * Following implementations based on
	 * https://stackoverflow.com/a/18606772
	 */

	private static Set<Class<?>> getSuperclasses(Class<?> clazz) {
	    final Set<Class<?>> result = new LinkedHashSet<>();
	    final Queue<Class<?>> queue = new ArrayDeque<>();
	    queue.add(clazz);
	    while (!queue.isEmpty()) {
	        Class<?> c = queue.remove();
	        if (result.add(c)) {
	            Class<?> sup = c.getSuperclass();
	            if (sup != null) queue.add(sup);
	            queue.addAll(Arrays.asList(c.getInterfaces()));
	        }
	    }
	    return result;
	}

	public static Set<Class<?>> commonSuperclasses(Class<?>...classes) {
	    Iterator<Class<?>> it = Arrays.asList(classes).iterator();
	    if (!it.hasNext()) {
	        return Collections.emptySet();
	    }
	    // begin with set from first hierarchy
	    Set<Class<?>> result = getSuperclasses(it.next());
	    // remove non-superclasses of remaining
	    while (it.hasNext()) {
	        Class<?> c = it.next();
	        Iterator<Class<?>> resultIt = result.iterator();
	        while (resultIt.hasNext()) {
	            Class<?> sup = resultIt.next();
	            if (!sup.isAssignableFrom(c)) {
	                resultIt.remove();
	            }
	        }
	    }
	    return result;
	}
}
