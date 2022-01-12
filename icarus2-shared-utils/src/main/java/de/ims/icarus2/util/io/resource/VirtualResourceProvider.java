/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.io.resource;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.util.annotations.TestableImplementation;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(ResourceProvider.class)
public class VirtualResourceProvider implements ResourceProvider {

	private final Map<Path, VirtualIOResource> resources = new HashMap<>();

	private final Set<Path> directories = new HashSet<>();

	private final Map<Path, Lock> locks = new WeakHashMap<>();

	protected VirtualIOResource createResource() {
		return new VirtualIOResource();
	}

	@Override
	public boolean exists(Path path) {
		return resources.containsKey(path) || isDirectory(path);
	}

	@Override
	public boolean create(Path path, boolean directory) throws IOException {
		requireNonNull(path);
		if(directory) {
			return directories.add(path);
		} else if(!resources.containsKey(path)) {
			resources.put(path, createResource());
			return true;
		}

		return false;
	}

	@Override
	public IOResource getResource(Path path) {
		requireNonNull(path);
		if(directories.contains(path))
			throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT,
					"Path is registered as a directory: "+path);
		IOResource resource = resources.get(path);
		if(resource==null)
			throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT,
					"No resource registered for path: "+path);
		return resources.get(path);
	}

	public void clear() {
		directories.clear();
		resources.forEach((path, resource) -> resource.delete());
	}

	public void addDirectory(Path path) {
		requireNonNull(path);
		directories.add(path);
	}

	public Collection<VirtualIOResource> getResources() {
		return Collections.unmodifiableCollection(resources.values());
	}

	public Set<Path> getPaths() {
		return Collections.unmodifiableSet(resources.keySet());
	}

	@Override
	public boolean isDirectory(Path path) {
		requireNonNull(path);
		return directories.contains(path);
	}

	@Override
	public Lock getLock(Path path) {
		requireNonNull(path);
		getResource(path); // To make sure we only return locks for known non-folder paths
		synchronized (locks) {
			Lock lock = locks.get(path);
			if(lock==null) {
				lock = new ReentrantLock();
				locks.put(path, lock);
			}
			return lock;
		}
	}

	@Override
	public DirectoryStream<Path> children(Path folder, String glob) throws IOException {
		requireNonNull(folder);
		requireNonNull(glob);

		VirtualDirectoryStream stream = new VirtualDirectoryStream();

		Matcher matcher = null;
		if(!"*".equals(glob)) {
			String regex = Globs.toWindowsRegexPattern(glob);

			matcher = Pattern.compile(regex).matcher("");
		}

		for(Path path : resources.keySet()) {
			if(path.equals(folder)) {
				continue;
			}

			Path parent = path.getParent();
			if(parent==null) {
				continue;
			}

			if(parent.equals(folder)) {
				if(matcher==null) {
					stream.add(path);
					continue;
				}

				Path fileName = path.getFileName();
				if(fileName==null) {
					continue;
				}

				matcher.reset(fileName.toString());
				if(matcher.matches()) {
					stream.add(path);
				}
			}
		}

		return stream;
	}

	private static class VirtualDirectoryStream extends ArrayList<Path> implements DirectoryStream<Path> {

		private static final long serialVersionUID = -1L;

		private volatile boolean closed = false;

		/**
		 * @see java.util.ArrayList#iterator()
		 */
		@Override
		public Iterator<Path> iterator() {
			checkState("Stream closed", !closed);
			return super.iterator();
		}

		/**
		 * @see java.io.Closeable#close()
		 */
		@Override
		public void close() throws IOException {
			closed = true;
			clear();
		}

	}
}
