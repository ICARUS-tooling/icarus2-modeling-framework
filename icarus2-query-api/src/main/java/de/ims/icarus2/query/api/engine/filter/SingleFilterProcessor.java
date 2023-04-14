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
package de.ims.icarus2.query.api.engine.filter;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.function.LongFunction;

import com.google.common.annotations.VisibleForTesting;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.engine.Tripwire;
import de.ims.icarus2.util.collections.BlockingLongBatchQueue;
import de.ims.icarus2.util.io.IOUtil;

/**
 * @author Markus Gärtner
 *
 */
public class SingleFilterProcessor extends AbstractFilterProcessor {

	public static Builder builder() { return new Builder(); }

	/** The executable job wrapper, holds both raw filter and our context */
	private final FilterJob job;
	/** Candidate sink to link between buffer and filter */
	private final SinkDelegate sink;

	private final BlockingLongBatchQueue queue;

	private final ThreadLocal<long[]> readBuffer;

	private volatile boolean broken = false;

	private SingleFilterProcessor(Builder builder) {
		super(builder);

		queue = new BlockingLongBatchQueue(builder.getCapacity(), builder.isFair());
		readBuffer = ThreadLocal.withInitial(() -> new long[IOUtil.DEFAULT_BUFFER_SIZE]);

		sink = new SinkDelegate();
		final QueryFilter filter = builder.getFilter();
		final FilterContext context = FilterContext.builder()
				.context(builder.getContext())
				.query(builder.getQuery())
				.stream(builder.getStream())
				.sink(sink)
				.build();
		job = new FilterJob("filter-job-"+getId()+"-1", filter, context);
	}

	private void maybeStartFilter() {
		if(trySetState(State.WAITING, State.STARTED)) {
			getExecutor().execute(job);
		}
	}

	/** Uses the {@link #getCandidateLookup() candidate lookup} to translate
	 * from the {@code indices} array to values in the {@code items} buffer. */
	private void translate(long[] indices, Container[] items, int len) {
		assert len<=indices.length;
		assert len<=items.length;

		LongFunction<Container> lookup = getCandidateLookup();
		for (int i = 0; i < len; i++) {
			items[i] = lookup.apply(indices[i]);
		}
	}

	// QUERY INPUT METHODS

	@Override
	public int load(Container[] buffer) throws InterruptedException {
		requireNonNull(buffer);
		checkNotEmpty(buffer);

		// Escalate errors from filter process
		if(job.isFinished()) {
			// If job was canceled we just re-throw the exception
			if(job.isInterrupted())
				throw (InterruptedException) job.getException();
			// Otherwise wrap the exception
			if(job.getException()!=null)
				throw new QueryException(GlobalErrorCode.DELEGATION_FAILED, "Filter process failed", job.getException());
		}

		if(getState().isFinished()) {
			return 0;
		}

		maybeStartFilter();

		long[] indices = readBuffer.get();
		int limit = Math.min(buffer.length, indices.length);

		// Read 1 chunk, this may block the current thread
		int count = queue.read(indices, 0, limit);
		if(count==0) {
			// Exit early in case the queue is effectively closed
			return 0;
		}

		// Try to read further chunks if we can do so without blocking
		int remaining;
		while((remaining = limit - count) > 0) {
			int read = queue.tryRead(indices, count, remaining, true);
			if(read>0) {
				count += read;
			} else {
				break;
			}
		}

		// Now translate indices into containers for consumer
		translate(indices, buffer, count);

		return count;
	}

	private void closeQueueSilently() {
		try {
			queue.close();
		} catch (InterruptedException e) {
			throw new QueryException(GlobalErrorCode.INTERRUPTED, "Failed to close internal buffer queue");
		}
	}

	@Override
	public void close() {
		super.close();

		closeQueueSilently();
		job.interrupt();
	}

	@VisibleForTesting
	class SinkDelegate implements CandidateSink {

		@Override
		public void prepare() {
			if(Tripwire.ACTIVE) {
				job.checkThread();
			}
			trySetState(State.STARTED, State.PREPARED);
		}

		@Override
		public void prepare(int size) throws InterruptedException {
			if(Tripwire.ACTIVE) {
				job.checkThread();
			}
			checkArgument("Buffer size must be positive", size > 0);
			if(trySetState(State.STARTED, State.PREPARED)) {
				queue.resize(size);
			}
		}

		@Override
		public void discard() throws InterruptedException {
			if(Tripwire.ACTIVE) {
				job.checkThread();
			}
			broken = true;
			queue.close();
			trySetState(State.PREPARED, State.FINISHED);
			fireStateChanged();
		}

		@Override
		public void finish() throws InterruptedException {
			if(Tripwire.ACTIVE) {
				job.checkThread();
			}
			queue.close();
			if(!trySetState(State.PREPARED, State.FINISHED))
				throw new QueryException(QueryErrorCode.INCORRECT_USE, "Lifecycle violation - sink must be prepared and run without errors before it can be finished.");
			fireStateChanged();
		}

		@Override
		public void ignore() {
			if(Tripwire.ACTIVE) {
				job.checkThread();
			}

			closeQueueSilently();
			if(!trySetState(State.STARTED, State.IGNORED))
				throw new QueryException(QueryErrorCode.INCORRECT_USE, "Lifecycle violation: Expected STARTED state for closing sink - got "+getState());
			fireStateChanged();
		}

		@Override
		public void add(long candidate) throws InterruptedException {
			if(Tripwire.ACTIVE) {
				job.checkThread();
			}
			queue.write(candidate);
		}

		@Override
		public void add(long[] candidates, int offset, int len) throws InterruptedException {
			queue.write(candidates, offset, len);
		}
	}

	public static class Builder extends AbstractFilterProcessor.BuilderBase<Builder, SingleFilterProcessor> {

		private static final int DEFAULT_CAPACITY = IOUtil.DEFAULT_BUFFER_SIZE;
		private static final boolean DEFAULT_FAIR = false;

		private QueryFilter filter;
		private Integer capacity;
		private Boolean fair;

		private Builder() { /* no-op */ }

		public Builder filter(QueryFilter filter) {
			requireNonNull(filter);
			checkState("Filter already set", this.filter==null);
			this.filter = filter;
			return this;
		}

		public Builder capacity(int capacity) {
			checkState("Capacity already set", this.capacity==null);
			this.capacity = Integer.valueOf(capacity);
			return this;
		}

		public Builder fair(boolean fair) {
			checkState("Fair already set", this.fair==null);
			this.fair = Boolean.valueOf(fair);
			return this;
		}

		public QueryFilter getFilter() { return filter; }
		public int getCapacity() { return capacity==null ? DEFAULT_CAPACITY : capacity.intValue(); }
		public boolean isFair() { return fair==null ? DEFAULT_FAIR : fair.booleanValue(); }

		@Override
		protected void validate() {
			super.validate();

			checkState("Filter not set", filter!=null);
		}

		@Override
		protected SingleFilterProcessor create() {
			return new SingleFilterProcessor(this);
		}
	}
}
