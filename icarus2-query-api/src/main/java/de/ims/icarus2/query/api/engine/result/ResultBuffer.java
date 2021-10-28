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
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import com.google.common.annotations.VisibleForTesting;

import de.ims.icarus2.query.api.engine.ThreadVerifier;
import de.ims.icarus2.query.api.engine.Tripwire;
import de.ims.icarus2.query.api.engine.result.ResultBuffer.SortableBase.SortableBuilderBase;
import de.ims.icarus2.util.AbstractBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrays;

/**
 * Models the storage part of result handling.
 * <p>
 * Implementation note:<br>
 * We use {@link ObjectArrays} here in certain situations over {@link Arrays}. This
 * is the case mostly for sorting (via {@link ObjectArrays#quickSort(Object[], int, int, Comparator)}
 * as this implementation does not create object overhead from creating additional temporary
 * arrays during recursion.
 *
 * @author Markus Gärtner
 *
 */
public abstract class ResultBuffer<T> {

	private final int initialGlobalSize;
	private final int collectorBufferSize;
	private final List<Collector<T>> collectors = new ObjectArrayList<>();

	private T[] items;
	private int size;
	private final IntFunction<T[]> bufferGen;

	private final Object collectorLock = new Object();

	protected ResultBuffer(BuilderBase<?, T, ?> builder) {
		initialGlobalSize = builder.initialGlobalSize();
		collectorBufferSize = builder.collectorBufferSize();
		bufferGen = builder.bufferGen();
	}

	@VisibleForTesting
	List<T> items() {
		checkState("result empty", size > 0);
		return Arrays.asList(items).subList(0, size);
	}

	/** Creates a new collector for the given thread. Uses {@code collectorLock}. */
	public final Predicate<T> createCollector(ThreadVerifier threadVerifier) {
		requireNonNull(threadVerifier);
		synchronized (collectorLock) {
			Collector<T> collector = requireNonNull(newCollector(threadVerifier));
			collectors.add(collector);
			return collector;
		}
	}

	protected abstract Collector<T> newCollector(ThreadVerifier threadVerifier);

	public final int size() { return size; }

	public final T get(int index) {
		rangeCheck(index);
		return items[index];
	}

	/** Performs final maintenance work. Uses {@code collectorLock}. */
	public final void finish() {
		synchronized (collectorLock) {
			for (int i = collectors.size()-1; i >= 0; i--) {
//				assert collectors.get(i)!=null;
				collectors.get(i).finish();
			}
			doFinish();
		}
	}

	/** Finalize state after all pending collectors have been merged. */
	protected void doFinish() { /* no-op */ }

	protected final T[] createBuffer(int size) { return bufferGen.apply(size); }
	protected final T[] createCollectorBuffer() { return bufferGen.apply(collectorBufferSize); }

	private void rangeCheck(int index) {
		if(index<0 || index>=size)
			throw new ArrayIndexOutOfBoundsException();
	}

	private void rangeCheckForAdd(int index) {
		if(index<0 || index>size)
			throw new ArrayIndexOutOfBoundsException();
	}

	private void ensureCapacity(int capacity) {
		if(items==null) {
			items = createBuffer(Math.max(initialGlobalSize, capacity));
		} else {
			items = ObjectArrays.grow(items, capacity, size);
		}
	}

	protected final void add(T[] elements, int offset, int length) {
		ensureCapacity(size + length);
		System.arraycopy(elements, offset, items, size, length);
		size += length;
	}

	protected final void add(T element) {
		ensureCapacity(size + 1);
		items[size++] = element;
	}

	protected final void insert(int index, T[] elements, int offset, int length) {
		rangeCheckForAdd(index);
		ensureCapacity(size + length);
		System.arraycopy(items, index, items, index + length, size - index);
		System.arraycopy(elements, offset, items, index, length);
		size += length;
	}

	protected final int find(T key, int from, int to, Comparator<? super T> c) {
		return ObjectArrays.binarySearch(items, from, to, key, c);
	}

	protected final void copyTo(int srcPos, T[] dest, int destPos, int length) {
		rangeCheck(srcPos+length-1);
		System.arraycopy(items, srcPos, dest, destPos, length);
	}

	protected final void copyFrom(int destPos, T[] source, int srcPos, int length) {
		rangeCheck(destPos+length-1);
		System.arraycopy(source, srcPos, items, destPos, length);
	}

	protected final void sortResult(Comparator<? super T> c) {
		ObjectArrays.quickSort(items, 0, size, c);
	}

	protected final void trim(int limit) {
		if(size > limit) {
			Arrays.fill(items, limit, size, null);
			size = limit;
		}
	}

	protected final Object collectorLock() { return collectorLock; }

	protected interface Collector<T> extends Predicate<T> {
		/** Callback for subclasses to perform final maintenance work.
		 * In contrast to {@link #test(Object)} this method can be called
		 * from different threads and must! */
		default void finish() { /* no-op */ }
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
		private final ThreadVerifier threadVerifier;
		private final T[] buffer;
		private int cursor = 0;

		protected BufferedCollector(T[] buffer, ThreadVerifier threadVerifier) {
			this.buffer = requireNonNull(buffer);
			this.threadVerifier = requireNonNull(threadVerifier);
		}

		/** Add item to buffer, call {@link #merge()} if buffer is full and unconditionally return {@code true}. */
		@Override
		public final boolean test(T item) {
			if(Tripwire.ACTIVE) {
				threadVerifier.checkThread();
			}
			buffer[cursor++] = item;
			boolean result = true;
			if(cursor>=buffer.length) {
				result = merge(buffer, buffer.length);
				cursor = 0;
			}
			return result;
		}

		/** Merge any leftover items in buffer */
		@Override
		public void finish() {
			if(cursor>0) {
				merge(buffer, cursor);
				Arrays.fill(buffer, 0, cursor, null);
				cursor = 0;
			}
		}

		/** Integrate the current buffer into the host result */
		protected abstract boolean merge(T[] buffer, int length);
	}

	/**
	 * Collects unordered elements without limit.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static final class Unlimited<T> extends ResultBuffer<T> {

		public static <T> Builder<T> builder(Class<T> elementClass) {
			return new Builder<>(elementClass);
		}

		private Unlimited(Builder<T> builder) { super(builder); }

		@Override
		protected Collector<T> newCollector(ThreadVerifier threadVerifier) {
			return new CollectorImp(createCollectorBuffer(), threadVerifier);
		}

		private final class CollectorImp extends BufferedCollector<T> {
			private CollectorImp(T[] buffer, ThreadVerifier threadVerifier) {
				super(buffer, threadVerifier);
			}

			@Override
			protected boolean merge(T[] buffer, int length) {
				synchronized (collectorLock()) {
					add(buffer, 0, length);
				}
				return true;
			}
		}

		public static class Builder<T> extends BuilderBase<Builder<T>, T, Unlimited<T>> {
			@SuppressWarnings("unchecked")
			private Builder(Class<T> elementClass) {
				super(true);
				requireNonNull(elementClass);
				bufferGen(size -> (T[])Array.newInstance(elementClass, size));
			}
			@Override
			protected Unlimited<T> create() { return new Unlimited<>(this); }
		}
	}

	/**
	 * Collects unordered elements with a hard limit. This implementation makes weaker
	 * guarantees than {@link FirstN} when it comes to concurrent attempts of adding
	 * items to the result:
	 * <p>
	 * We use {@link BufferedCollector}s here and only check at merge time whether or
	 * not there's still capacity left for adding items to the result. In the context
	 * of concurrent access this means that we can make no estimates on the time an
	 * element gets added in relation to when it gets first processed.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static final class Limited<T> extends ResultBuffer<T> {

		public static <T> Builder<T> builder(Class<T> elementClass) {
			return new Builder<>(elementClass);
		}

		private final int limit;

		private Limited(Builder<T> builder) {
			super(builder);
			limit = builder.limit();
		}

		@Override
		protected Collector<T> newCollector(ThreadVerifier threadVerifier) {
			return new CollectorImp(createCollectorBuffer(), threadVerifier);
		}

		private final class CollectorImp extends BufferedCollector<T> {
			private CollectorImp(T[] buffer, ThreadVerifier threadVerifier) {
				super(buffer, threadVerifier);
			}

			@Override
			protected boolean merge(T[] buffer, int length) {
				synchronized (collectorLock()) {
					int remaining = limit - size();
					if(remaining>0) {
						add(buffer, 0, Math.min(length, remaining));
						return size()<limit;
					}
					return false;
				}
			}
		}

		public static class Builder<T> extends BuilderBase<Builder<T>, T, Limited<T>> {

			private Integer limit;
			@SuppressWarnings("unchecked")
			private Builder(Class<T> elementClass) {
				super(true);
				requireNonNull(elementClass);
				bufferGen(size -> (T[])Array.newInstance(elementClass, size));
			}

			public Builder<T> limit(int limit) {
				checkArgument("limit must be positive", limit>0);
				checkState("limit already set", this.limit==null);
				this.limit = Integer.valueOf(limit);
				return thisAsCast();
			}

			int limit() { return limit.intValue(); }

			@Override
			protected void validate() {
				super.validate();
				checkState("limit not set", limit!=null);
			}

			@Override
			protected Limited<T> create() { return new Limited<>(this); }
		}
	}

	static abstract class SortableBase extends ResultBuffer<ResultEntry> {

		private final Comparator<ResultEntry> sorter;
		private final int initialTmpSize;
		private ResultEntry[] tmp;

		public SortableBase(SortableBuilderBase<?,?> builder) {
			super(builder);
			sorter = builder.sorter();
			initialTmpSize = builder.initialTmpSize();
		}

		protected final Comparator<ResultEntry> sorter() { return sorter; }

		protected final void sortResult() {
			if(sorter!=null) {
				sortResult(sorter);
			}
		}

		private void ensureTmpCapacity(int capacity) {
			if(tmp==null) {
				tmp = createBuffer(Math.max(initialTmpSize, capacity));
			} else {
				tmp = ObjectArrays.grow(tmp, capacity);
			}
		}

		/**
		 * Merges the given {@code buffer} into the underlying array of entries.
		 * The {@code buffer} array is expected to be sorted already.
		 *
		 * @param buffer
		 * @param length
		 */
		protected final void mergeSorted(ResultEntry[] buffer, int length) {
			// Easy mode for first insertion
			if(size()==0) {
				add(buffer, 0, length);
				return;
			}

			// Find left and right insertion points for first and last item in buffer
			int right = find(buffer[length-1], 0, size(), sorter);
			if(right < 0) {
				right = -right - 1;
			}
			// If new set of results is completely before the old section, just prepend
			if(right==0) {
				insert(0, buffer, 0, length);
				return;
			}
			int left = find(buffer[0], 0, right, sorter);
			if(left < 0) {
				left = -left - 1;
			}
			// If new set of results is completely after the old section, just append
			if(left>=size()) {
				add(buffer, 0, length);
				return;
			}
			// Just insert new block if we have no overlap
			if(left==right) {
				insert(left, buffer, 0, length);
				return;
			}
			// Copy section [n..m] from items list, combine with buffer and sort
			int secLen = right - left;
			ensureTmpCapacity(length + secLen);
			copyTo(left, tmp, 0, secLen);
			System.arraycopy(buffer, 0, tmp, secLen, length);
			ObjectArrays.quickSort(tmp, 0, length + secLen, sorter);
			// Copy chunk of sorted tmp array back to [n..m] in items list
			copyFrom(left, tmp, 0, secLen);
			// Insert remaining items at right insertion point into items list
			insert(right, tmp, secLen, length);
		}

		static abstract class SortableBuilderBase<B extends SortableBuilderBase<B, R>, R extends ResultBuffer<ResultEntry>>
				extends BuilderBase<B, ResultEntry, R> {

			private Comparator<ResultEntry> sorter;
			private Integer initialTmpSize;

			private final boolean requiresSorter;

			protected SortableBuilderBase(boolean requiresCollectorBuffer, boolean requiresSorter) {
				super(requiresCollectorBuffer);
				this.requiresSorter = requiresSorter;
				bufferGen(ResultEntry[]::new);
			}

			public B initialTmpSize(int initialTmpSize) {
				checkArgument("initial temp size must be positive", initialTmpSize>0);
				checkState("initial temp size already set", this.initialTmpSize==null);
				this.initialTmpSize = Integer.valueOf(initialTmpSize);
				return thisAsCast();
			}

			int initialTmpSize() { return initialTmpSize.intValue(); }

			public B sorter(Comparator<ResultEntry> sorter) {
				checkState("sorter already set", this.sorter==null);
				this.sorter = requireNonNull(sorter);
				return thisAsCast();
			}

			Comparator<ResultEntry> sorter() { return sorter; }

			@Override
			protected void validate() {
				super.validate();
				if(requiresSorter) {
					checkState("initial temp size not set", initialTmpSize!=null);
					checkState("sorter not set", sorter!=null);
				}
			}
		}
	}

	/**
	 * Inserts pre-sorted chunks to keep the total amount of sorting overhead small.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static final class Sorted extends SortableBase {

		public static Builder builder() { return new Builder(); }

		private Sorted(Builder builder) {
			super(builder);
		}

		@Override
		protected Collector<ResultEntry> newCollector(ThreadVerifier threadVerifier) {
			return new CollectorImp(createCollectorBuffer(), threadVerifier);
		}

		private final class CollectorImp extends BufferedCollector<ResultEntry> {
			private CollectorImp(ResultEntry[] buffer, ThreadVerifier threadVerifier) {
				super(buffer, threadVerifier);
			}

			@Override
			protected boolean merge(ResultEntry[] buffer, int length) {
				// Sort the original data (outside of lock)
				ObjectArrays.quickSort(buffer, 0, length, sorter());
				// Now merge the sorted data
				synchronized (collectorLock()) {
					mergeSorted(buffer, length);
				}
				return true;
			}
		}

		public static class Builder extends SortableBuilderBase<Builder, Sorted> {
			private Builder() { super(true, true); }
			@Override
			protected Sorted create() { return new Sorted(this); }
		}

	}

	public abstract static class LimitedBuilderBase<B extends LimitedBuilderBase<B,R>, R extends SortableBase>
		extends SortableBuilderBase<B, R> {

		private Integer limit;

		protected LimitedBuilderBase(boolean requiresCollectorBuffer, boolean requiresSorter) {
			super(requiresCollectorBuffer, requiresSorter);
		}

		public B limit(int limit) {
			checkArgument("limit must be positive", limit>0);
			checkState("limit already set", this.limit==null);
			this.limit = Integer.valueOf(limit);
			return thisAsCast();
		}

		int limit() { return limit.intValue(); }

		@Override
		protected void validate() {
			super.validate();
			checkState("limit not set", limit!=null);
		}
	}

	/**
	 * Stops after a set amount of results has been reached and then sorts them.
	 * This implementation makes more concrete promises on the composition of
	 * the result list compared to {@link Limited}:
	 * <p>
	 * Individual matches are added the instant they have been processed. This way
	 * the granularity of mixups is kept a lot smaller, but we still cannot
	 * guarantee that only the <i>actual</i> first N matches are added.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static final class FirstN extends SortableBase {

		public static Builder builder() { return new Builder(); }

		private final int limit;

		private FirstN(Builder builder) {
			super(builder);
			limit = builder.limit();
		}

		@Override
		protected Collector<ResultEntry> newCollector(ThreadVerifier threadVerifier) {
			return new CollectorImpl();
		}

		@Override
		protected void doFinish() {
			// Make sure actually only have the first N elements
			trim(limit);
			// Sort results if a sorter is present
			sortResult();
		}

		private final class CollectorImpl implements Collector<ResultEntry> {
			@Override
			public boolean test(ResultEntry entry) {
				synchronized (collectorLock()) {
					if(size()>=limit) {
						return false;
					}
					add(entry);
					return size()<limit;
				}
			}
		}

		public static class Builder extends LimitedBuilderBase<Builder, FirstN> {
			private Builder() { super(false, false); }
			@Override
			protected FirstN create() { return new FirstN(this); }
		}
	}

	/**
	 * Collects ordered results and keeps only a limited ordered subset.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static final class BestN extends SortableBase {

		public static Builder builder() { return new Builder(); }

		private final int limit;

		private BestN(Builder builder) {
			super(builder);
			limit = builder.limit();
		}

		@Override
		protected Collector<ResultEntry> newCollector(ThreadVerifier threadVerifier) {
			return new CollectorImpl(createCollectorBuffer(), threadVerifier);
		}

		private final class CollectorImpl extends BufferedCollector<ResultEntry> {
			protected CollectorImpl(ResultEntry[] buffer, ThreadVerifier threadVerifier) {
				super(buffer, threadVerifier);
			}

			@Override
			protected boolean merge(ResultEntry[] buffer, int length) {
				// Sort the original data (outside of lock)
				ObjectArrays.quickSort(buffer, 0, length, sorter());
				// Now merge the sorted data and then trim the result again
				synchronized (collectorLock()) {
					mergeSorted(buffer, length);
					trim(limit);
				}
				return true;
			}
		}

		public static class Builder extends LimitedBuilderBase<Builder, BestN> {
			private Builder() { super(true, true); }
			@Override
			protected BestN create() { return new BestN(this); }
		}
	}

	static abstract class BuilderBase<B extends BuilderBase<B, T,R>, T, R extends ResultBuffer<T>>
		extends AbstractBuilder<B, R> {

		private final boolean requiresCollectorBuffer;

		private Integer initialGlobalSize;
		private Integer collectorBufferSize;
		private IntFunction<T[]> bufferGen;

		protected BuilderBase(boolean requiresCollectorBuffer) {
			this.requiresCollectorBuffer = requiresCollectorBuffer;
		}

		public B initialGlobalSize(int initialGlobalSize) {
			checkArgument("initial global size must be positive", initialGlobalSize>0);
			checkState("initial global size already set", this.initialGlobalSize==null);
			this.initialGlobalSize = Integer.valueOf(initialGlobalSize);
			return thisAsCast();
		}

		int initialGlobalSize() { return initialGlobalSize.intValue(); }

		public B collectorBufferSize(int collectorBufferSize) {
			checkArgument("collector buffer size must be positive", collectorBufferSize>0);
			checkState("collector buffer size already set", this.collectorBufferSize==null);
			this.collectorBufferSize = Integer.valueOf(collectorBufferSize);
			return thisAsCast();
		}

		int collectorBufferSize() { return collectorBufferSize.intValue(); }

		protected B bufferGen(IntFunction<T[]> bufferGen) {
			checkState("initial global size already set", this.bufferGen==null);
			this.bufferGen = requireNonNull(bufferGen);
			return thisAsCast();
		}

		IntFunction<T[]> bufferGen() { return bufferGen; }

		@Override
		protected void validate() {
			checkState("initial global size not set", initialGlobalSize!=null);
			if(requiresCollectorBuffer) {
				checkState("collector buffer size not set", collectorBufferSize!=null);
			}
			checkState("buffer generator not set", bufferGen!=null);
		}
	}
}
