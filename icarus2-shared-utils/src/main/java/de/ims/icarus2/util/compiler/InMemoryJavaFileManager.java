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
	public ClassLoader getSharedClassLoader() {
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