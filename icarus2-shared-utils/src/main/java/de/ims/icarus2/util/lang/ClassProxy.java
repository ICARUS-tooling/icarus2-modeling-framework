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
package de.ims.icarus2.util.lang;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Markus Gärtner
 *
 */
public final class ClassProxy {
	
	private static final Logger log = LoggerFactory.getLogger(ClassProxy.class);

	private final String className;
	private final ClassLoader classLoader;

	private Map<String, Object> properties;

	public ClassProxy(String className, ClassLoader classLoader) {
		requireNonNull(className);
		requireNonNull(classLoader);

		this.className = className;
		this.classLoader = classLoader;
	}

	public Object loadObject() {
		try {
			Class<?> clazz = classLoader.loadClass(className);

			return clazz.newInstance();
		} catch (ClassNotFoundException e) {
			log.error("ClassProxy: Could not find class: {}", className, e); //$NON-NLS-1$
		} catch (InstantiationException e) {
			log.error("ClassProxy: Unable to instantiate class: {}", className, e); //$NON-NLS-1$
		} catch (IllegalAccessException e) {
			log.error("ClassProxy: Unable to access default constructor: {}", className, e); //$NON-NLS-1$
		}

		return null;
	}

	public Class<?> loadClass() throws ClassNotFoundException {
		return classLoader.loadClass(className);
	}

	public Object loadObjectUnsafe() throws ClassNotFoundException, InstantiationException, IllegalAccessException  {
		Class<?> clazz = classLoader.loadClass(className);

		return clazz.newInstance();
	}

	@Override
	public String toString() {
		return "ClassProxy: "+className; //$NON-NLS-1$
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return classLoader.hashCode() * className.hashCode();
	}

	/**
	 * Two {@code ClassProxy} instances are considered equal if
	 * they both refer to the same {@code Class} as by their
	 * {@code className} field and both use the same {@code ClassLoader}
	 * to load the final object.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} if(obj instanceof ClassProxy) {
			ClassProxy other = (ClassProxy) obj;
			return className.equals(other.className)
					&& classLoader.equals(other.classLoader);
		}
		return false;
	}

	public Object getProperty(String key) {
		return properties==null ? null : properties.get(key);
	}

	public void setProperty(String key, Object value) {
		if(properties==null) {
			properties = new HashMap<>();
		}

		properties.put(key, value);
	}

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return the classLoader
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}
}
