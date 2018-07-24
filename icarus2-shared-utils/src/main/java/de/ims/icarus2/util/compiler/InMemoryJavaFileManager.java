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
package de.ims.icarus2.util.compiler;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.SecureClassLoader;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
@SuppressWarnings("rawtypes")
public class InMemoryJavaFileManager extends ForwardingJavaFileManager {

	/**
	 * Maps class names to their respective "output files"
	 */
	private final Map<String, ByteArrayJavaFileObject> classBytesLookup = new Object2ObjectOpenHashMap<>();

	private final ClassLoader sharedClassLoader;

	/**
	 * Will initialize the manager with the specified standard java file manager
	 *
	 * @param fileManager
	 */
	@SuppressWarnings("unchecked")
	public InMemoryJavaFileManager(JavaFileManager fileManager) {
		super(fileManager);

		PrivilegedAction<ClassLoader> createLoader = () -> {
			return new SecureClassLoader() {
				@Override
				protected Class<?> findClass(String name) throws ClassNotFoundException {
					ByteArrayJavaFileObject fileObject = getJavaFileObject(name, false, true);
					if(fileObject==null)
						throw new ClassNotFoundException("No compiled class saved in this manager for name: "+name);

					byte[] b = fileObject.getBytes();

					return super.defineClass(name, b, 0, b.length);
				}
			};
		};

		sharedClassLoader = AccessController.doPrivileged(createLoader);
	}

	/**
	 * Looks up the "output file" for the given {@code classname}, creating a new
	 * instance of {@link ByteArrayJavaFileObject} if needed.
	 *
	 * @param className
	 * @param createIfMissing
	 * @return
	 */
	protected ByteArrayJavaFileObject getJavaFileObject(String className, boolean createIfMissing, boolean removeFromCache) {
		ByteArrayJavaFileObject fileObject = classBytesLookup.get(className);

		if(fileObject==null && createIfMissing) {
			fileObject = new ByteArrayJavaFileObject(className);
			classBytesLookup.put(className, fileObject);
		}

		if(fileObject!=null && removeFromCache) {
			classBytesLookup.remove(className);
		}

		return fileObject;
	}

	/**
	 * Will be used by us to get the class loader for our compiled class. It
	 * creates an anonymous class extending the SecureClassLoader which uses the
	 * byte code created by the compiler and stored in the JavaClassObject, and
	 * returns the Class for it
	 */
	@Override
	public ClassLoader getClassLoader(Location location) {
		return sharedClassLoader;
	}

	/**
	 * Gives the compiler an instance of the JavaClassObject so that the
	 * compiler can write the byte code into it.
	 */
	@Override
	public JavaFileObject getJavaFileForOutput(Location location,
			String className, Kind kind, FileObject sibling) throws IOException {
		return getJavaFileObject(className, true, false);
	}
}