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

 * $Revision: 398 $
 * $Date: 2015-05-29 11:29:49 +0200 (Fr, 29 Mai 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.core/source/de/ims/icarus2/util/classes/ExternalClassLoader.java $
 *
 * $LastChangedDate: 2015-05-29 11:29:49 +0200 (Fr, 29 Mai 2015) $
 * $LastChangedRevision: 398 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util.classes;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import de.ims.icarus2.util.io.IOUtil;

/**
 * @author Markus Gärtner
 * @version $Id: ExternalClassLoader.java 398 2015-05-29 09:29:49Z mcgaerty $
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

		return new ExternalClassLoader(urls, rootFolder, parent);
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

				loader = new URLClassLoader(new URL[]{url}, this);

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
