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

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.engine.Tripwire;
import de.ims.icarus2.util.collections.BlockingLongBatchQueue;

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

	private final BlockingLongBatchQueue buffer;


	private SingleFilterProcessor(Builder builder) {
		super(builder);

		buffer = new BlockingLongBatchQueue(builder.getCapacity(), builder.isFair());

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

	// QUERY INPUT METHODS

	@Override
	public int load(Container[] buffer) throws InterruptedException {
		requireNonNull(buffer);

		maybeStartFilter();
		// TODO check whether we already started the filter process

		return 0;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@VisibleForTesting
	class SinkDelegate implements CandidateSink {

		@Override
		public void prepare() {
			if(Tripwire.ACTIVE) {
				job.checkThread();
			}
			trySetState(State.STARTED, State.PREPARED);
			// nothing extra to do here as we already initialized the buffer
		}

		@Override
		public void prepare(int size) {
			if(Tripwire.ACTIVE) {
				job.checkThread();
			}
			if(trySetState(State.STARTED, State.PREPARED)) {
				if(size>START_CAPACITY) {
					synchronized (candidates) {
						candidates.size(size);
					}
				}
			}
		}

		@Override
		public void discard() throws InterruptedException {
			if(Tripwire.ACTIVE) {
				job.checkThread();
			}
			// TODO Auto-generated method stub

		}

		@Override
		public void finish() throws InterruptedException {
			if(Tripwire.ACTIVE) {
				job.checkThread();
			}
			if(!trySetState(State.PREPARED, State.FINISHED))
				throw new QueryException(QueryErrorCode.INCORRECT_USE, "");
		}

		@Override
		public void ignore() {
			if(Tripwire.ACTIVE) {
				job.checkThread();
			}
			// TODO Auto-generated method stub

		}

		@Override
		public void add(long candidate) {
			if(Tripwire.ACTIVE) {
				job.checkThread();
			}
			// TODO Auto-generated method stub
		}

		/**
		 * @see de.ims.icarus2.query.api.engine.filter.CandidateSink#add(long[])
		 */
		@Override
		public void add(long[] candidates) {
			// TODO Auto-generated method stub

		}
	}

	public static class Builder extends AbstractFilterProcessor.BuilderBase<Builder, SingleFilterProcessor> {

		private static final int DEFAULT_CAPACITY = 1024;
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
