/**
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
package de.ims.icarus2.util.concurrent;

import java.io.Closeable;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Kind of a merge between the {@code org.apache.lucene.util.CloseableThreadLocal}
 * implementation and the
 *
 * @author Markus Gärtner
 *
 */
public class CloseableThreadLocal<T> implements Closeable {

    public static <S> CloseableThreadLocal<S> withInitial(Supplier<? extends S> supplier) {
        return new SuppliedCloseableThreadLocal<>(supplier);
    }

	private ThreadLocal<WeakReference<T>> t = new ThreadLocal<>();

	// Use a WeakHashMap so that if a Thread exits and is
	// GC'able, its entry may be removed:
	private Map<Thread, T> hardRefs = new WeakHashMap<>();

	// Increase this to decrease frequency of purging in get:
	private static int PURGE_MULTIPLIER = 20;

	// On each get or set we decrement this; when it hits 0 we
	// purge. After purge, we set this to
	// PURGE_MULTIPLIER * stillAliveCount. This keeps
	// amortized cost of purging linear.
	private final AtomicInteger countUntilPurge = new AtomicInteger(
			PURGE_MULTIPLIER);

	protected T initialValue() {
		return null;
	}

	public T get() {
		WeakReference<T> weakRef = t.get();
		if (weakRef == null) {
			T iv = initialValue();
			if (iv != null) {
				set(iv);
				return iv;
			}
			return null;
		}

		maybePurge();
		return weakRef.get();
	}

	public void set(T object) {

		t.set(new WeakReference<>(object));

		synchronized (hardRefs) {
			hardRefs.put(Thread.currentThread(), object);
			maybePurge();
		}
	}

	private void maybePurge() {
		if (countUntilPurge.getAndDecrement() == 0) {
			purge();
		}
	}

	// Purge dead threads
	private void purge() {
		synchronized (hardRefs) {
			int stillAliveCount = 0;
			for (Iterator<Thread> it = hardRefs.keySet().iterator(); it
					.hasNext();) {
				final Thread t = it.next();
				if (!t.isAlive()) {
					it.remove();
				} else {
					stillAliveCount++;
				}
			}
			int nextCount = (1 + stillAliveCount) * PURGE_MULTIPLIER;
			if (nextCount <= 0) {
				// defensive: int overflow!
				nextCount = 1000000;
			}

			countUntilPurge.set(nextCount);
		}
	}

	@Override
	public void close() {
		// Try to close individual instances first
		Map<Thread, T> hardRefs = this.hardRefs;
		if(hardRefs!=null) {
			try {
				for(T instance : hardRefs.values()) {
					if(instance instanceof AutoCloseable) {
						((AutoCloseable)instance).close();
					}
				}
			} catch(Throwable t) {
				// ignore, we do this as best effort only
			}

		}

		// Clear the hard refs; then, the only remaining refs to
		// all values we wer'e storing are weak (unless somewhere
		// else is still using them) and so GC may reclaim them:
		this.hardRefs = null;
		// Take care of the current thread right now; others will be
		// taken care of via the WeakReferences.
		if (t != null) {
			t.remove();
		}
		t = null;
	}

    /**
     * An extension of CloseableThreadLocal that obtains its initial value from
     * the specified {@code Supplier}.
     */
    static final class SuppliedCloseableThreadLocal<T> extends CloseableThreadLocal<T> {

        private final Supplier<? extends T> supplier;

        SuppliedCloseableThreadLocal(Supplier<? extends T> supplier) {
            this.supplier = Objects.requireNonNull(supplier);
        }

        @Override
        protected T initialValue() {
            return supplier.get();
        }
    }
}