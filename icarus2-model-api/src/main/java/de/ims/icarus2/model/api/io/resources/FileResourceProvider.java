/**
 *
 */
package de.ims.icarus2.model.api.io.resources;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusException;

/**
 * @author Markus Gärtner
 *
 */
public class FileResourceProvider implements ResourceProvider {

	private volatile static FileResourceProvider instance;

	public static FileResourceProvider getSharedInstance() {
		FileResourceProvider result = instance;

		if (result == null) {
			synchronized (FileResourceProvider.class) {
				result = instance;

				if (result == null) {
					instance = new FileResourceProvider();
					result = instance;
				}
			}
		}

		return result;
	}

	private static final Map<Path, FileLockWrapper> sharedLocks = new WeakHashMap<>();

	/**
	 * @see bwfdm.replaydh.io.resources.ResourceProvider#exists(java.nio.file.Path)
	 */
	@Override
	public boolean exists(Path path) {
		return Files.exists(path, LinkOption.NOFOLLOW_LINKS);
	}

	/**
	 * @see bwfdm.replaydh.io.resources.ResourceProvider#create(java.nio.file.Path)
	 */
	@Override
	public boolean create(Path path) throws IOException {
		boolean exists = Files.exists(path, LinkOption.NOFOLLOW_LINKS);

		if(!exists) {
			if(Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
				Files.createDirectory(path);
			} else {
				Files.createFile(path);
			}
		}

		return !exists;
	}

	/**
	 * @see bwfdm.replaydh.io.resources.ResourceProvider#getResource(java.nio.file.Path)
	 */
	@Override
	public IOResource getResource(Path path) throws IOException {
		return new FileResource(path);
	}

	/**
	 * @see bwfdm.replaydh.io.resources.ResourceProvider#children(java.nio.file.Path)
	 */
	@Override
	public DirectoryStream<Path> children(Path folder, String glob) throws IOException {
		return Files.newDirectoryStream(folder, glob);
	}

	/**
	 * @see bwfdm.replaydh.io.resources.ResourceProvider#isDirectory(java.nio.file.Path)
	 */
	@Override
	public boolean isDirectory(Path path) {
		return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
	}

	/**
	 * @see bwfdm.replaydh.io.resources.ResourceProvider#getLock(java.nio.file.Path)
	 */
	@Override
	public Lock getLock(Path path) {
		synchronized (sharedLocks) {
			FileLockWrapper lockWrapper = sharedLocks.get(path);
			if(lockWrapper==null) {
				lockWrapper = new FileLockWrapper(path);
				sharedLocks.put(path, lockWrapper);
			}

			return lockWrapper;
		}
	}

	private void  removeLockWrapper(FileLockWrapper lockWrapper) {
		synchronized (sharedLocks) {
			sharedLocks.remove(Paths.get(lockWrapper.uri));
		}
	}

	private class FileLockWrapper implements Lock {

		private final URI uri;

		private volatile FileLock lock;
		private volatile FileChannel channel;

		/**
		 * Important: Must not maintain a strong link to the {@code path}
		 * argument, since we use it in the provider class for linking via
		 * a weak map. To work around this we store the path as an {@link URI}!
		 */
		FileLockWrapper(Path path) {
			requireNonNull(path);

			this.uri = path.toUri();
		}

		private synchronized FileChannel ensureChannel() throws IOException {
			if(channel==null) {
				Path path = Paths.get(this.uri);
				channel = FileChannel.open(path, StandardOpenOption.WRITE);
			}
			return channel;
		}

		/**
		 * @see java.util.concurrent.locks.Lock#lock()
		 */
		@Override
		public synchronized void lock() {
			checkState("Lock already acquired", lock==null || !lock.isValid());

			if(lock!=null) {
				try {
					lock.release();
				} catch (IOException e) {
					throw new IcarusException(GlobalErrorCode.IO_ERROR, "Failed to release internal lock on file: "+uri, e);
				}
			}

			try {
				ensureChannel();
			} catch (IOException e) {
				throw new IcarusException(GlobalErrorCode.IO_ERROR, "Failed to establish channel for file: "+uri, e);
			}

			try {
				lock = channel.lock();
			} catch (IOException e) {
				throw new IcarusException(GlobalErrorCode.IO_ERROR, "Failed to acquire lock on file: "+uri, e);
			}
		}

		/**
		 * @see java.util.concurrent.locks.Lock#lockInterruptibly()
		 */
		@Override
		public synchronized void lockInterruptibly() throws InterruptedException {
			// not sure this makes sense, but we have no interruptible implementation available
			lock();
		}

		/**
		 * @see java.util.concurrent.locks.Lock#tryLock()
		 */
		@Override
		public synchronized boolean tryLock() {
			try {
				return tryLock0(0, null);
			} catch (InterruptedException e) {
				throw new InternalError("Unexpected thread interruption", e);
			}
		}

		/**
		 * @see java.util.concurrent.locks.Lock#tryLock(long, java.util.concurrent.TimeUnit)
		 */
		@Override
		public synchronized boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
			requireNonNull(unit);

			return tryLock0(time, unit);
		}

		private synchronized boolean tryLock0(long time, TimeUnit unit) throws InterruptedException {
			checkState("Lock already acquired", lock==null || !lock.isValid());

			if(lock!=null) {
				try {
					lock.release();
				} catch (IOException e) {
					throw new IcarusException(GlobalErrorCode.IO_ERROR, "Failed to release internal lock on file: "+uri, e);
				}
			}

			try {
				ensureChannel();
			} catch (IOException e) {
				throw new IcarusException(GlobalErrorCode.IO_ERROR, "Failed to establish channel for file: "+uri, e);
			}

			long originalMillis = System.currentTimeMillis();
			long waitMillis = unit==null ? -1 : unit.toMillis(time);

			do {
				if(Thread.interrupted())
					throw new InterruptedException();

				try {
					lock = channel.tryLock();
				} catch (IOException e) {
					throw new IcarusException(GlobalErrorCode.IO_ERROR, "Failed to acquire lock on file: "+uri, e);
				}
			} while(waitMillis>-1 && (System.currentTimeMillis()-originalMillis)<waitMillis);

			return lock!=null;
		}

		/**
		 * @see java.util.concurrent.locks.Lock#unlock()
		 */
		@Override
		public synchronized void unlock() {
			checkState("No lock acquired", lock!=null);

			try {
				// Release the lock itself
				if(lock.isValid()) {
					lock.release();
				}

				// And also make sure the associated channel gets closed
				if(channel!=null && channel.isOpen()) {
					channel.close();
				}
			} catch (IOException e) {
				throw new IcarusException(GlobalErrorCode.IO_ERROR, "Failed to release lock", e);
			} finally {
				// make sure the lock mapping gets removed from the host resolver object
				lock = null;
				channel = null;

				removeLockWrapper(this);
			}
		}

		/**
		 * @see java.util.concurrent.locks.Lock#newCondition()
		 */
		@Override
		public Condition newCondition() {
			throw new UnsupportedOperationException("Conditions not supported");
		}

	}
}
