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
/**
 *
 */
package de.ims.icarus2.query.api.engine.result;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import de.ims.icarus2.query.api.engine.ThreadVerifier;
import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Models the storage part of result handling.
 *
 * @author Markus Gärtner
 *
 */
public abstract class ResultBuffer<T> {

	protected final int collectorBufferSize;
	private final List<Collector<T>> collectors = new ObjectArrayList<>();

	protected final List<T> items;
	private final ListProxy<T> proxy = new ListProxy<>();

	protected ResultBuffer(int initialGlobalSize, int collectorBufferSize) {
		checkArgument("Initial global buffer size must be positive", initialGlobalSize>0);
		checkArgument("Collector buffer size must be positive", collectorBufferSize>0);
		items = new ObjectArrayList<>(initialGlobalSize);
		this.collectorBufferSize = collectorBufferSize;
	}

	protected ResultBuffer(int initialGlobalSize) {
		checkArgument("Initial global buffer size must be positive", initialGlobalSize>0);
		items = new ObjectArrayList<>(initialGlobalSize);
		this.collectorBufferSize = UNSET_INT;
	}

	public final Predicate<T> createCollector(ThreadVerifier threadVerifier) {
		requireNonNull(threadVerifier);
		Collector<T> collector = newCollector(threadVerifier);
		collectors.add(collector);
		return collector;
	}

	protected abstract Collector<T> newCollector(ThreadVerifier threadVerifier);

	public final void finish() {
		for (int i = collectors.size()-1; i >= 0; i--) {
			collectors.get(i).finish();
		}
		doFinish();
	}

	/** Finalize state after all pending collectors have been merged. */
	protected void doFinish() { /* no-op */ }

	protected void add(int index, T[] elements, int length) {
		if(index==UNSET_INT) {
			index = items.size();
		}
		proxy.reset(elements, length);
		items.addAll(index, proxy);
		proxy.reset();
	}

	protected interface Collector<T> extends Predicate<T> {
		/** Callback for subclasses to perform final maintenance work.
		 * In contrast to {@link #test(Object)} this method can be called
		 * from different threads and must! */
		default void finish() { /* no-op */ }
	}

	private static final class ListProxy<E> extends AbstractList<E> {

		private E[] elements;
		private int length;

		void reset(E[] elements, int length) {
			this.elements = elements;
			this.length = length;
		}

		void reset() {
			elements = null;
			length = 0;
		}

		// TODO should we do a range check here for index?
		@Override
		public E get(int index) { return elements[index]; }

		@Override
		public int size() { return length; }

	}

	/**
	 * Collector implementation that uses a fixed-sized array as buffer internally.
	 * Once the buffer is full, {@link BufferedCollector#merge(int)} is called to
	 * integrate it into the host {@link ResultBuffer} instance. Additionally
	 * {@link Collector#finish()} is implemented in a way that any leftover items
	 * in the buffer are also properly merged.
	 *
	 * @author Markus Gärtner
	 *
	 * @param <T> type of items to be buffered
	 */
	private static abstract class BufferedCollector<T> implements Collector<T> {
		protected final ThreadVerifier threadVerifier;
		protected final T[] buffer;
		private int cursor = 0;

		@SuppressWarnings("unchecked")
		protected BufferedCollector(Class<T> clazz, int bufferSize, ThreadVerifier threadVerifier) {
			buffer = (T[]) Array.newInstance(clazz, bufferSize);
			this.threadVerifier = requireNonNull(threadVerifier);
		}

		/** Add item to buffer, call {@link #merge()} if buffer is full and unconditionally return {@code true}. */
		@Override
		public final boolean test(T item) {
			if(Tripwire.ACTIVE) {
				threadVerifier.checkThread();
			}
			buffer[cursor++] = item;
			if(cursor>=buffer.length) {
				merge(buffer.length);
				cursor = 0;
			}
			return true;
		}

		/** Merge any leftover items in buffer */
		@Override
		public void finish() {
			if(cursor>0) {
				merge(cursor);
				cursor = 0;
			}
		}

		/** Integrate the current buffer into the host result */
		protected abstract void merge(int length);
	}

	public static final class Unlimited extends ResultBuffer<Match> {

		public Unlimited(int initialGlobalSize, int collectorBufferSize) {
			super(initialGlobalSize, collectorBufferSize);
		}

		@Override
		protected Collector<Match> newCollector(ThreadVerifier threadVerifier) {
			return new CollectorImp(collectorBufferSize, threadVerifier);
		}

		private class CollectorImp extends BufferedCollector<Match> {
			private CollectorImp(int bufferSize, ThreadVerifier threadVerifier) {
				super(Match.class, bufferSize, threadVerifier);
			}

			@Override
			protected void merge(int length) {
				synchronized (items) {
					add(UNSET_INT, buffer, length);
				}
			}
		}
	}

	static abstract class SortedBase extends ResultBuffer<ResultEntry> {

		private final Comparator<ResultEntry> sorter;
		private ResultEntry[] tmp;

		public SortedBase(int initialGlobalSize, int collectorBufferSize,
				Comparator<ResultEntry> sorter) {
			super(initialGlobalSize, collectorBufferSize);
			this.sorter = requireNonNull(sorter);
		}

		protected final void doMerge(ResultEntry[] buffer, int length) {
			// Sort the original data
			Arrays.sort(buffer, 0, length, sorter);
			synchronized (items) {
				// Easy mode for first insertion
				if(items.isEmpty()) {
					CollectionUtils.feedItems(items, buffer);
					return;
				}

				// Find left and right insertion points for first and last item in buffer
				int left = Collections.binarySearch(items, buffer[0], sorter);
				if(left < 0) {
					left = -left - 1;
				}
				int right = Collections.binarySearch(items, buffer[length-1], sorter);
				if(right < 0) {
					right = -right - 1;
				}
				// Copy section [n..m] from items list, combine with buffer and sort
				// Copy chunk of sorted tmp array back to [n..m] in items list
				// Insert remaining items at right insertion point into items list
			}
		}
	}

	public static final class Sorted extends SortedBase {

	}

	public static final class FirstN extends SortedBase {

	}

	public static final class BestN extends SortedBase {

	}
}
