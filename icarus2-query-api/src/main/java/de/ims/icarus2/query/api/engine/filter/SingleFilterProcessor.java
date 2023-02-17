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
import de.ims.icarus2.query.api.engine.result.Match;
import it.unimi.dsi.fastutil.longs.LongArrayList;

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

	private static final int START_CAPACITY = 1024;

	private final LongArrayList candidates = new LongArrayList(START_CAPACITY);

	private SingleFilterProcessor(Builder builder) {
		super(builder);

		sink = new SinkDelegate();
		final QueryFilter filter = builder.getFilter();
		final FilterContext context = FilterContext.builder()
				.context(builder.getContext())
				.query(builder.getQuery())
				.stream(builder.getStream())
				.sink(sink)
				.build();
		job = new FilterJob(1, filter, context);
	}

	private void maybeStartFilter() {
		if(trySetState(State.WAITING, State.PREPARED)) {
			getExecutor().execute(job);
		}
	}

	// QUERY INPUT METHODS

	@Override
	public int load(Container[] buffer) {
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
			trySetState(State.WAITING, State.PREPARED);
			// nothing extra to do here as we already initialized the buffer
		}

		@Override
		public void prepare(int size) {
			if(Tripwire.ACTIVE) {
				job.checkThread();
			}
			if(trySetState(State.WAITING, State.PREPARED)) {
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

		@VisibleForTesting
		void add(long value) {
			if(Tripwire.ACTIVE) {
				job.checkThread();
			}
			candidates.add(value);
		}

		@Override
		public void add(Match match) {
			if(Tripwire.ACTIVE) {
				job.checkThread();
			}
			add(match.getIndex());
		}
	}

	public static class Builder extends AbstractFilterProcessor.BuilderBase<Builder, SingleFilterProcessor> {

		private QueryFilter filter;

		private Builder() { /* no-op */ }

		public Builder filter(QueryFilter filter) {
			requireNonNull(filter);
			checkState("Filter already set", this.filter==null);
			this.filter = filter;
			return this;
		}

		public QueryFilter getFilter() {
			return filter;
		}

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