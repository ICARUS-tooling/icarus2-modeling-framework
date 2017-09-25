/**
 *
 */
package de.ims.icarus2.model.api.io.resources;

import static de.ims.icarus2.util.Conditions.checkState;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Markus Gärtner
 *
 */
public class VirtualResourceProvider implements ResourceProvider {

	private static final Logger log = LoggerFactory.getLogger(VirtualResourceProvider.class);

	private final Map<Path, VirtualIOResource> resources = new HashMap<>();

	private final Set<Path> directories = new HashSet<>();

	private final Map<Path, Lock> locks = new WeakHashMap<>();

	protected VirtualIOResource createResource() {
		return new VirtualIOResource();
	}

	/**
	 * @see bwfdm.replaydh.io.resources.ResourceProvider#exists(java.nio.file.Path)
	 */
	@Override
	public boolean exists(Path path) {
		return resources.containsKey(path) || isDirectory(path);
	}

	/**
	 * @see bwfdm.replaydh.io.resources.ResourceProvider#create(java.nio.file.Path)
	 */
	@Override
	public boolean create(Path path) throws IOException {
		boolean hasResource = resources.containsKey(path);
		if(!hasResource) {
			resources.put(path, createResource());
		}

		return !hasResource;
	}

	/**
	 * @see bwfdm.replaydh.io.resources.ResourceProvider#getResource(java.nio.file.Path)
	 */
	@Override
	public IOResource getResource(Path path) throws IOException {
		checkState("No resoruce registered for path: "+path, resources.containsKey(path));
		return resources.get(path);
	}

	public void clear() {
		directories.clear();
		resources.values().forEach(t -> {
			try {
				t.delete();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	public void addDirectory(Path path) {
		directories.add(path);
	}

	public Collection<VirtualIOResource> getResources() {
		return Collections.unmodifiableCollection(resources.values());
	}

	public Set<Path> getPaths() {
		return Collections.unmodifiableSet(resources.keySet());
	}

	/**
	 * @see bwfdm.replaydh.io.resources.ResourceProvider#isDirectory(java.nio.file.Path)
	 */
	@Override
	public boolean isDirectory(Path path) {
		return directories.contains(path);
	}

	/**
	 * @see bwfdm.replaydh.io.resources.ResourceProvider#getLock(java.nio.file.Path)
	 */
	@Override
	public Lock getLock(Path path) {
		synchronized (locks) {
			Lock lock = locks.get(path);
			if(lock==null) {
				lock = new ReentrantLock();
				locks.put(path, lock);
			}
			return lock;
		}
	}

	/**
	 * @see bwfdm.replaydh.io.resources.ResourceProvider#children(java.nio.file.Path)
	 */
	@Override
	public DirectoryStream<Path> children(Path folder, String glob) throws IOException {
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

			if(path.getParent().equals(folder)) {
				if(matcher==null) {
					stream.add(path);
					continue;
				}

				matcher.reset(path.getFileName().toString());
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
