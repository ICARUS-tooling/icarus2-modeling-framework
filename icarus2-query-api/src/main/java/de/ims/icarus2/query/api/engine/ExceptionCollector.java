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
