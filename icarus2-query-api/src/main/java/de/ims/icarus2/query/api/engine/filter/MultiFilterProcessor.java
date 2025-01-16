/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.engine.filter;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static java.util.Objects.requireNonNull;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.driver.indices.func.DualIntersectionOfLong;
import de.ims.icarus2.model.api.driver.indices.func.HeapIntersectionOfLong;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.engine.Tripwire;
import de.ims.icarus2.util.collections.BlockingLongBatchQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
public class MultiFilterProcessor extends AbstractFilterProcessor {

	public static Builder builder() { return new Builder(); }

	private static final Logger log = LoggerFactory.getLogger(MultiFilterProcessor.class);

	private final ParallelJob[] jobs;
	private final FilterDelegate[] delegates;
	private final BlockingLongBatchQueue[] queues;
	private final Merger merger;

	private volatile boolean canceled;

	private MultiFilterProcessor(Builder builder) {
		super(builder);

		List<QueryFilter> filters = new ObjectArrayList<>(builder.getFilters());
		jobs = new ParallelJob[filters.size()];
		queues = new BlockingLongBatchQueue[filters.size()];
		delegates = new FilterDelegate[filters.size()];

		for (int i = 0; i < jobs.length; i++) {
			BlockingLongBatchQueue queue = new BlockingLongBatchQueue(builder.getCapacity(), builder.isFair());
			FilterDelegate delegate = new FilterDelegate(i, queue);
			FilterContext context = FilterContext.builder()
					.context(builder.getContext())
					.query(builder.getQuery())
					.stream(builder.getStream())
					.sink(delegate)
					.build();
			jobs[i] = new ParallelJob(i, filters.get(i), context);
			delegates[i] = delegate;
			queues[i] = queue;
		}

		merger = new Merger(delegates);
	}

	private void jobDone(ParallelJob job) {
		FilterDelegate delegate = job.delegate();

		//TODO check delegate and update state
	}

	private void maybeStartFilters() {
		if(trySetState(State.WAITING, State.STARTED)) {
			for (FilterJob filterJob : jobs) {
				getExecutor().execute(filterJob);
			}
		}
	}

	@Override
	public int load(Container[] buffer) throws InterruptedException {
		requireNonNull(buffer);
		checkNotEmpty(buffer);

		// Escalate errors from filter process
		if(getState().isFail()) {
			//TODO check if any of the delegates got canceled

			throw new QueryException(GlobalErrorCode.DELEGATION_FAILED, "Filter process ended prematurely.");
		}

		if(merger.broken) {
			return 0;
		}

		if(getState().isFinished()) {
			return 0;
		}

		maybeStartFilters();

		long[] indices = getBuffer();
		int limit = Math.min(buffer.length, indices.length);

		// Read a chunk, this may block the current thread
		int count = merger.read(indices, 0, limit);
		if(count>0) {
			// Now translate indices into containers for consumer
			translate(indices, buffer, count);
		}

		return count;
	}

	@Override
	public void close() {
		super.close();

		QueryException exception = null;

		for (int i = 0; i < jobs.length; i++) {
			try {
				closeQueueInterruptibly(queues[i]);
			} catch (Exception e) {
				if(exception==null) {
					exception = new QueryException(GlobalErrorCode.DELEGATION_FAILED, "Failed to close queue for job "+i, e);
				} else {
					exception.addSuppressed(e);
				}
			}
			jobs[i].interrupt();
		}

		if(exception!=null)
			throw exception;
	}

	private static void closeQueueInterruptibly(BlockingLongBatchQueue queue) {
		try {
			queue.close();
		} catch (InterruptedException e) {
			throw new QueryException(GlobalErrorCode.INTERRUPTED, "Failed to close internal buffer queue");
		}
	}

	@VisibleForTesting
	class Merger {
		private final PrimitiveIterator.OfLong intersection;

		private volatile boolean broken = false;

		Merger(PrimitiveIterator.OfLong[] sources) {
			requireNonNull(sources);
			checkArgument("Must have at least 2 index sources", sources.length>1);
			if(sources.length==2) {
				intersection = new DualIntersectionOfLong(sources[0], sources[1]);
			} else {
				intersection = new HeapIntersectionOfLong(sources);
			}
		}

		synchronized int read(long[] buffer, int offset, int len) {
			if(broken) {
				return 0;
			}

			int count = 0;

			for (int i = 0; i < len; i++) {
				if(!intersection.hasNext()) {
					break;
				}
				long value = intersection.nextLong();
				if(value==UNSET_LONG) {
					broken = true;
					return 0;
				}
				buffer[offset+count] = value;
				count++;
			}

			return count;
		}
	}

	@VisibleForTesting
	class ParallelJob extends FilterJob {

		private final int index;

		ParallelJob(int index, QueryFilter filter, FilterContext context) {
			super("filter-job-"+getId()+"-"+index, filter, context);
			this.index = index;
		}

		FilterDelegate delegate() {
			return delegates[index];
		}

		@Override
		protected void done() {
			jobDone(this);
		}
	}

	/**
	 * Wraps around a filter and provides {@link CandidateSink sink} mechanics for one end
	 * and an {@link PrimitiveIterator.OfLong iterator} interface for the other.
	 *
	 * @author Markus Gärtner
	 */
	@VisibleForTesting
	class FilterDelegate extends OrderedSink implements PrimitiveIterator.OfLong {

		/** Global identifier of individual delegate pipeline */
		private final int index;

		// Input fields

		/** Buffer storage */
		private final BlockingLongBatchQueue queue;

		// Output fields

		private volatile boolean active = true;
		private volatile boolean broken = false;

		FilterDelegate(int index, BlockingLongBatchQueue queue) {
			this.index = index;
			this.queue = requireNonNull(queue);
		}

		private FilterJob job() {
			return jobs[index];
		}

		@Override
		public boolean hasNext() { return active; }

		/**
		 * Addition to original contract:
		 * <p>
		 * In case the sink got {@link CandidateSink#discard() discarded}, a value of
		 * {@link -1} will be returned. This can be used by the {@link Merger} to detect
		 * a broken stream.
		 *
		 * @see java.util.PrimitiveIterator.OfLong#nextLong()
		 */
		@Override
		public long nextLong() {
			if(Tripwire.ACTIVE) {
				job().checkNotThread();
			}
			if(broken || !active) {
				return UNSET_LONG;
			}

			try {
				return queue.read();
			} catch (InterruptedException e) {
				broken = true;
				active = false;
				throw new QueryException(GlobalErrorCode.INTERRUPTED, "Reading of candidate queue interrupted.", e);
			}
		}

		@Override
		public void prepare() {
			if(Tripwire.ACTIVE) {
				job().checkThread();
			}
			trySetState(State.STARTED, State.PREPARED);
		}

		@Override
		public void prepare(int size) throws InterruptedException {
			if(Tripwire.ACTIVE) {
				job().checkThread();
			}
			checkArgument("Buffer size must be positive", size > 0);
			if(trySetState(State.STARTED, State.PREPARED)) {
				queue.resize(size);
			}
		}

		@Override
		public void discard() throws InterruptedException {
			if(Tripwire.ACTIVE) {
				job().checkThread();
			}
			broken = true;
			active = false;
			try {
				queue.close();
			} finally {
				trySetState(State.PREPARED, State.FINISHED);
			}
			fireStateChanged();
		}

		@Override
		public void finish() throws InterruptedException {
			if(Tripwire.ACTIVE) {
				job().checkThread();
			}
			active = false;
			try {
				queue.close();
			} finally {
				if(!trySetState(State.PREPARED, State.FINISHED))
					throw new QueryException(QueryErrorCode.INCORRECT_USE, "Lifecycle violation - sink must be prepared and run without errors before it can be finished.");
			}
			fireStateChanged();
		}

		@Override
		public void ignore() {
			if(Tripwire.ACTIVE) {
				job().checkThread();
			}

			active = false;
			try {
				closeQueueInterruptibly(queue);
			} finally {
				if(!trySetState(State.STARTED, State.IGNORED))
					throw new QueryException(QueryErrorCode.INCORRECT_USE, "Lifecycle violation: Expected STARTED state for closing sink - got "+getState());
			}
			fireStateChanged();
		}

		@Override
		protected void addImpl(long candidate) throws InterruptedException {
			if(Tripwire.ACTIVE) {
				job().checkThread();
			}
			queue.write(candidate);
		}

		@Override
		protected void addImpl(long[] candidates, int offset, int len) throws InterruptedException {
			if(Tripwire.ACTIVE) {
				job().checkThread();
			}
			queue.write(candidates, offset, len);
		}
	}

	public static class Builder extends BuilderBase<Builder, MultiFilterProcessor> {

		private final Set<QueryFilter> filters = new LinkedHashSet<>();

		private Builder() { /* no-op */ }

		public Builder filter(QueryFilter filter) {
			requireNonNull(filter);
			checkState("Filter already set", filters.add(filter));
			return this;
		}

		public Builder filters(QueryFilter...filters) {
			requireNonNull(filters);
			for(QueryFilter filter : filters) {
				checkState("Filter already set", this.filters.add(filter));
			}
			return this;
		}

		public Set<QueryFilter> getFilters() { return filters; }

		@Override
		protected void validate() {
			super.validate();

			checkState("No filters added", !filters.isEmpty());
		}


		@Override
		protected MultiFilterProcessor create() {
			return new MultiFilterProcessor(this);
		}

	}
}
