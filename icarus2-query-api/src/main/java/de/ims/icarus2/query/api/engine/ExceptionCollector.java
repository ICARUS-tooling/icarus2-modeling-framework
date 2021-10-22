/**
 *
 */
package de.ims.icarus2.query.api.engine;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.BiConsumer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
public class ExceptionCollector implements BiConsumer<Thread, Throwable> {

	private final List<Entry> entries = new ObjectArrayList<>();
	private final Object lock = new Object();

	@Override
	public void accept(Thread thread, Throwable exception) {
		synchronized (lock) {
			entries.add(new Entry(thread, exception));
		}
	}

	public boolean hasEntries( ) { return !entries.isEmpty(); } // no synchronization for single value read

	public List<Entry> getEntries() {
		synchronized (lock) {
			return new ObjectArrayList<>(entries);
		}
	}

	public static class Entry {
		private final Thread thread;
		private final Throwable exception;

		public Entry(Thread thread, Throwable exception) {
			this.thread = requireNonNull(thread);
			this.exception = requireNonNull(exception);
		}

		public Thread getThread() { return thread; }

		public Throwable getException() { return exception; }
	}
}
