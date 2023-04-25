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
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.lang.Primitives._long;
import static java.util.Objects.requireNonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.engine.QueryInput;
import de.ims.icarus2.query.api.engine.ThreadVerifier;
import de.ims.icarus2.query.api.engine.Tripwire;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.query.api.iql.IqlStream;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.events.ChangeSource;
import de.ims.icarus2.util.io.IOUtil;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractFilterProcessor extends ChangeSource implements QueryInput {

	private static final Logger log = LoggerFactory.getLogger(AbstractFilterProcessor.class);

	private static final AtomicLong idGen = new AtomicLong();

	/** Used to delegate execution to another thread */
	private final ExecutorService executor;
	/** Convert index values from filters to actual candidate objects */
	private final LongFunction<Container> candidateLookup;
	/** Current lifecycle state */
	private final AtomicReference<State> state = new AtomicReference<>(State.WAITING);
	/** Buffers for the {@link QueryInput#load(Container[])} method */
	private final ThreadLocal<long[]> readBuffer;

	private final long id;

	static enum State {
		/** Initial state */
		WAITING(false),
		/** Backend process(es) started, but buffers still pending preparation- */
		STARTED(false),
		/** Buffers prepared by backend. */
		PREPARED(false),
		/** Filter process finished without issues. This includes orderly cancellation. */
		FINISHED(false),
		/** Filter got told to ignore backend data. */
		IGNORED(true),
		/** Filter process failed. */
		FAILED(true),
		;

		private final boolean fail;
		private State(boolean fail) { this.fail = fail; }

		boolean isFinished() { return ordinal()>FINISHED.ordinal(); }

		public boolean isFail() { return fail; }
	}

	protected AbstractFilterProcessor(BuilderBase<?,?> builder) {
		builder.validate();
		id = idGen.incrementAndGet();
		executor = builder.getExecutor();
		candidateLookup = builder.getCandidateLookup();

		readBuffer = ThreadLocal.withInitial(() -> new long[IOUtil.DEFAULT_BUFFER_SIZE]);
	}

	/** Returns the read buffer to be used by the current thread. */
	protected long[] getBuffer() { return readBuffer.get(); }

	protected long getId() { return id; }

	protected ExecutorService getExecutor() {
		return executor;
	}

	protected LongFunction<Container> getCandidateLookup() {
		return candidateLookup;
	}

	protected final boolean trySetState(State expected, State next) {
		State current = state.get();
		if(current.isFinished())
			throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE, "Current state is already marked finished: "+current);
		return state.compareAndSet(expected, next);
	}

	protected final State getState() {
		return state.get();
	}

	protected final void setState(State next) {
		state.set(next);
	}

	/** Uses the {@link #getCandidateLookup() candidate lookup} to translate
	 * from the {@code indices} array to values in the {@code items} buffer. */
	protected final void translate(long[] indices, Container[] items, int len) {
		assert len<=indices.length;
		assert len<=items.length;

		LongFunction<Container> lookup = getCandidateLookup();
		for (int i = 0; i < len; i++) {
			items[i] = lookup.apply(indices[i]);
		}
	}

	protected static abstract class OrderedSink implements CandidateSink {

		private long last = UNSET_LONG;

		private static void checkOrdered(long value, long last) {
			if(value<0L)
				throw new QueryException(GlobalErrorCode.INVALID_INPUT, "Candidate values must not be negatve: "+value);
			if(last!=UNSET_LONG && value<=last)
				throw new QueryException(QueryErrorCode.UNORDERED_CANDIDATES,
						String.format("Unordered candidate stream: previous value %d - got %d", _long(last), _long(value)));
		}

		protected abstract void addImpl(long value) throws InterruptedException;

		@Override
		public final void add(long candidate) throws InterruptedException {
			checkOrdered(candidate, last);
			addImpl(candidate);
			last = candidate;
		}

		protected abstract void addImpl(long[] candidates, int offset, int len) throws InterruptedException;

		@Override
		public final void add(long[] candidates, int offset, int len) throws InterruptedException {
			requireNonNull(candidates);
			long last0 = last;
			for (int i = 0; i < len; i++) {
				long v = candidates[offset + i];
				checkOrdered(v, last0);
				last0 = v;
			}
			addImpl(candidates, offset, len);
			last = last0;
		}
	}

	static class FilterJob implements Runnable {
		private final QueryFilter filter;
		private final FilterContext context;
		private final String label;

		private volatile ThreadVerifier threadVerifier;

		private Throwable exception;
		private volatile boolean interrupted, finished;

		public FilterJob(String label, QueryFilter filter, FilterContext context) {
			this.label = requireNonNull(label);
			this.filter = requireNonNull(filter);
			this.context = requireNonNull(context);
		}

		public void interrupt() {
			ThreadVerifier tv = threadVerifier;

			boolean needsInterrupt = !finished && !interrupted;

			finished = true;
			interrupted = true;

			if(tv!=null && needsInterrupt) {
				tv.getThread().interrupt();
			}
		}

		/** Signals that the filter process has been interrupted */
		public boolean isInterrupted() {
			return interrupted;
		}

		/** Returns the exception caught during filtering, if any. */
		public Throwable getException() {
			return exception;
		}

		/**
		 * Returns whether or not this job has already finished (either
		 * successfully or with an exception)
		 */
		public boolean isFinished() {
			return finished;
		}

		public boolean wasInterrupted() {
			return finished && interrupted;
		}

		/**
		 * Delegates to {@link ThreadVerifier#checkThread()} on the internal verifier.
		 * Before this call, client code should first check if {@link Tripwire} is actually
		 * {@link Tripwire#ACTIVE active} to ensure lower load on frequent calls.
		 */
		public final void checkThread() {
			checkState("Thread verifier not initialized yet", threadVerifier!=null);
			threadVerifier.checkThread();
		}

		/**
		 * Delegates to {@link ThreadVerifier#checkNotThread()} on the internal verifier.
		 * Before this call, client code should first check if {@link Tripwire} is actually
		 * {@link Tripwire#ACTIVE active} to ensure lower load on frequent calls.
		 */
		public final void checkNotThread() {
			checkState("Thread verifier not initialized yet", threadVerifier!=null);
			threadVerifier.checkNotThread();
		}

		/** Hook for subclasses to intercept end of filter process */
		protected void done() {
			// no-op
		}

		/**
		 * Execute filter and catch exceptions.
		 *
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			threadVerifier = ThreadVerifier.forCurrentThread(label);
			try {
				try {
					filter.filter(context);
				} catch (QueryException e) {
					exception = e;
				} catch (IcarusApiException e) {
					exception = e;
				} catch (InterruptedException e) {
					exception = e;
					interrupted = true;
				} catch (Throwable t) {
					//TODO should we add a mechanism to mark unexpected errors?
					exception = t;
				}

				if(exception!=null) {
					try {
						//FIXME we violate the contract here since discard() might have already been called!
						context.getSink().discard();
					} catch (InterruptedException e) {
						e.addSuppressed(exception);
						exception = e;
						interrupted = true;
					}
					if(log.isDebugEnabled()) {
						log.debug("Filter process {} failed", label, exception);
					}
				}
			} finally {
				finished = true;
				done();
			}
		}
	}

	@Deprecated
	public static class Batch {
		private final long[] candidates;
		private int cursor, size;

		public Batch(int capacity) {
			candidates = new long[capacity];
			cursor = 0;
			size = 0;
		}

		public boolean add(long candidate) {
			if(size>=candidates.length) {
				return false;
			}

			candidates[size++] = candidate;
			return true;
		}

		//TODO add batch methods

	}

	public static abstract class BuilderBase<B extends BuilderBase<B, P>, P extends AbstractFilterProcessor>
		extends AbstractBuilder<B, P> {

		private static final int DEFAULT_CAPACITY = IOUtil.DEFAULT_BUFFER_SIZE;
		private static final boolean DEFAULT_FAIR = false;

		private ExecutorService executor;
		private LongFunction<Container> candidateLookup;
		private Context context;
		private IqlStream stream;
		private IqlQuery query;
		private Integer capacity;
		private Boolean fair;

		public B executor(ExecutorService executor) {
			requireNonNull(executor);
			checkState("Executor service already set", this.executor==null);
			this.executor = executor;
			return thisAsCast();
		}

		public B candidateLookup(LongFunction<Container> candidateLookup) {
			requireNonNull(candidateLookup);
			checkState("Candidate lookup already set", this.candidateLookup==null);
			this.candidateLookup = candidateLookup;
			return thisAsCast();
		}

		public B context(Context context) {
			requireNonNull(context);
			checkState("Context already set", this.context==null);
			this.context = context;
			return thisAsCast();
		}

		public B stream(IqlStream stream) {
			requireNonNull(stream);
			checkState("Stream already set", this.stream==null);
			this.stream = stream;
			return thisAsCast();
		}

		public B query(IqlQuery query) {
			requireNonNull(query);
			checkState("Query already set", this.query==null);
			this.query = query;
			return thisAsCast();
		}

		public B capacity(int capacity) {
			checkState("Capacity already set", this.capacity==null);
			this.capacity = Integer.valueOf(capacity);
			return thisAsCast();
		}

		public B fair(boolean fair) {
			checkState("Fair already set", this.fair==null);
			this.fair = Boolean.valueOf(fair);
			return thisAsCast();
		}

		public int getCapacity() { return capacity==null ? DEFAULT_CAPACITY : capacity.intValue(); }
		public boolean isFair() { return fair==null ? DEFAULT_FAIR : fair.booleanValue(); }


		public ExecutorService getExecutor() {
			return executor;
		}

		public LongFunction<Container> getCandidateLookup() {
			return candidateLookup;
		}

		public Context getContext() {
			return context;
		}

		public IqlStream getStream() {
			return stream;
		}

		public IqlQuery getQuery() {
			return query;
		}

		@Override
		protected void validate() {
			checkState("Executor service not set", executor!=null);
			checkState("Candidate lookup not set", candidateLookup!=null);
			checkState("Context not set", context!=null);
			checkState("Stream not set", stream!=null);
			checkState("Query not set", query!=null);
		}
	}
}
