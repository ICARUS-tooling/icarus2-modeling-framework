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
package de.ims.icarus2.util.lang;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import de.ims.icarus2.util.io.IOUtil;

/**
 * @author Markus Gärtner
 *
 */
public class ExternalClassLoader extends URLClassLoader {

//	private volatile static ExternalClassLoader sharedLoader;
//
//	public static ExternalClassLoader getSharedLoader() {
//		ExternalClassLoader result = sharedLoader;
//
//		if (result == null) {
//			synchronized (ExternalClassLoader.class) {
//				result = sharedLoader;
//
//				if (result == null) {
//					try {
//						sharedLoader = newExternalClassLoader(Core.getCore().getExternalFolder());
//					} catch (IOException e) {
//						throw new IllegalStateException("Cannot access global 'external' folder"); //$NON-NLS-1$
//					}
//					result = sharedLoader;
//
//					// NOTE: the shutdown hook suppresses possible IOExceptions!
//					Core.getCore().addShutdownHook("Close External Classloader", new Runnable() { //$NON-NLS-1$
//
//						@Override
//						public void run() {
//							try {
//								sharedLoader.close();
//							} catch (IOException e) {
//								// ignore
//							}
//						}
//					});
//				}
//			}
//		}
//
//		return result;
//	}

	private final Path rootFolder;

	private final WeakHashMap<Closeable, Void> closeables = new WeakHashMap<>();

	private final Map<String, URLClassLoader> jarLoaders = new HashMap<>();

	public static ExternalClassLoader newExternalClassLoader(Path rootFolder) throws IOException {
		return newExternalClassLoader(rootFolder, ExternalClassLoader.class.getClassLoader());
	}

	public static ExternalClassLoader newExternalClassLoader(Path rootFolder, ClassLoader parent) throws IOException {

		List<URL> urlList = new ArrayList<>();

		try(DirectoryStream<Path> stream = Files.newDirectoryStream(rootFolder, IOUtil.jarFilter)) {
			for(Path urlPath : stream) {
				urlList.add(urlPath.toUri().toURL());
			}
		}

		URL[] urls = new URL[urlList.size()];
		urlList.toArray(urls);

		return AccessController.doPrivileged(new PrivilegedAction<ExternalClassLoader>() {

			@Override
			public ExternalClassLoader run() {
				return new ExternalClassLoader(urls, rootFolder, parent);
			}
		});
	}

	private ExternalClassLoader(URL[] urls, Path rootFolder, ClassLoader parent) {
		super(urls, parent);

		if (rootFolder == null)
			throw new NullPointerException("Invalid rootFolder"); //$NON-NLS-1$

		this.rootFolder = rootFolder;
	}

	public Path getRootFolder() {
		return rootFolder;
	}

	protected String getClassFileName(String name) {
		return name.concat(".class"); //$NON-NLS-1$
	}

	/**
	 * Reads the content of a file given via the {@code path} argument and
	 * loads the content as class {@code name}.
	 */
	protected Class<?> defineClass(String name, Path path) throws IOException {
		byte[] b = Files.readAllBytes(path);

		return defineClass(name, b, 0, b.length);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {

		// Try to load from file first
		Path path = rootFolder.resolve(getClassFileName(name));

		if(Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {

			// Make sure package is defined before class is loaded
	        int i = name.lastIndexOf('.');
	        if(i!=-1) {
	            String pkgname = name.substring(0, i);
	            Package pkg = getPackage(pkgname);
	            if(pkg==null) {
                    definePackage(pkgname, null, null, null, null, null, null, null);
	            }
	        }

	        try {
				return defineClass(name, path);
			} catch (IOException e) {
				throw new ClassNotFoundException(name, e);
			}
		}

		return super.findClass(name);
	}

	protected ClassLoader getJarLoader(String jarName) throws FileNotFoundException, MalformedURLException {

		if(!jarName.endsWith(".jar")) { //$NON-NLS-1$
			jarName += ".jar"; //$NON-NLS-1$
		}

		synchronized (jarLoaders) {
			URLClassLoader loader = jarLoaders.get(jarName);
			if(loader==null) {

				Path jarFile = rootFolder.resolve(jarName);

				if(!Files.exists(jarFile, LinkOption.NOFOLLOW_LINKS))
					throw new FileNotFoundException("Missing jar file '"+jarFile+"' for external class loader in folder "+rootFolder); //$NON-NLS-1$ //$NON-NLS-2$

				URL url = jarFile.toUri().toURL();

				PrivilegedAction<URLClassLoader> createLoader = () -> new URLClassLoader(new URL[]{url}, this);

				loader = AccessController.doPrivileged(createLoader);

				jarLoaders.put(jarName, loader);
				closeables.put(loader, null);
			}

			return loader;
		}
	}

	public Class<?> loadClass(String jarName, String name) throws ClassNotFoundException {
		if (jarName == null)
			throw new NullPointerException("Invalid jarName"); //$NON-NLS-1$

		ClassLoader loader;

		try {
			loader = getJarLoader(jarName);
		} catch (FileNotFoundException e) {
			throw new ClassNotFoundException(name, e);
		} catch (MalformedURLException e) {
			throw new ClassNotFoundException(name, e);
		}

		return loader.loadClass(name);
	}

	/**
	 * @see java.io.Closeable#close()
	 * @see URLClassLoader#close()
	 */
	@Override
	public void close() throws IOException {

		super.close();

		// Handling copied from java.net.URLClassLoader#close()

        List<IOException> errors = new ArrayList<>();

        // now close any remaining streams.

        synchronized (closeables) {
            Set<Closeable> keys = closeables.keySet();
            for (Closeable c : keys) {
                try {
                    c.close();
                } catch (IOException ioex) {
                    errors.add(ioex);
                }
            }
            closeables.clear();
        }

        if (errors.isEmpty()) {
            return;
        }

        IOException firstex = errors.remove(0);

        // Suppress any remaining exceptions

        for (IOException error: errors) {
            firstex.addSuppressed(error);
        }
        throw firstex;
	}
}
