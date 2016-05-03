/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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
 * $Revision: 400 $
 * $Date: 2015-05-29 15:06:46 +0200 (Fr, 29 Mai 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.core/source/de/ims/icarus2/util/classes/ClassProxy.java $
 *
 * $LastChangedDate: 2015-05-29 15:06:46 +0200 (Fr, 29 Mai 2015) $
 * $LastChangedRevision: 400 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util.classes;

import static de.ims.icarus2.model.util.Conditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Markus Gärtner
 * @version $Id: ClassProxy.java 400 2015-05-29 13:06:46Z mcgaerty $
 *
 */
public final class ClassProxy {
	
	private static final Logger log = LoggerFactory.getLogger(ClassProxy.class);

	private final String className;
	private final ClassLoader classLoader;

	private Map<String, Object> properties;

	public ClassProxy(String className, ClassLoader classLoader) {
		checkNotNull(className);
		checkNotNull(classLoader);

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
