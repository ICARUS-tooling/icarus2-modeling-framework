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
			} else {
				return null;
			}
		} else {
			maybePurge();
			return weakRef.get();
		}
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
		// Clear the hard refs; then, the only remaining refs to
		// all values we were storing are weak (unless somewhere
		// else is still using them) and so GC may reclaim them:
		hardRefs = null;
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