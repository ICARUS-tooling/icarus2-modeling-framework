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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongFunction;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.engine.QueryInput;
import de.ims.icarus2.query.api.engine.ThreadVerifier;
import de.ims.icarus2.query.api.engine.Tripwire;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.query.api.iql.IqlStream;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.events.ChangeSource;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractFilterProcessor extends ChangeSource implements QueryInput {

	private static final AtomicLong idGen = new AtomicLong();

	/** Used to delegate execution to another thread */
	private final ExecutorService executor;
	/** Convert index values from filters to actual candidate objects */
	private final LongFunction<Container> candidateLookup;
	/** Current lifecycle state */
	private final AtomicReference<State> state = new AtomicReference<>(State.WAITING);

	private final long id;

	static enum State {
		/** Initial state */
		WAITING,
		/** Backend process(es) started, but buffers still pending preparation- */
		STARTED,
		/** Buffers prepared by backend. */
		PREPARED,
		/** Filter process finished without issues. */
		FINISHED,
		/** Filter got told to ignore backend data. */
		IGNORED,
		/** Filter process failed. */
		FAILED,
		;

		boolean isFinished() { return ordinal()>FINISHED.ordinal(); }
	}

	protected AbstractFilterProcessor(BuilderBase<?,?> builder) {
		builder.validate();
		id = idGen.incrementAndGet();
		executor = builder.getExecutor();
		candidateLookup = builder.getCandidateLookup();
	}

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

	static class FilterJob implements Runnable {
		private final QueryFilter filter;
		private final FilterContext context;
		private final String label;

		private volatile ThreadVerifier threadVerifier;

		private Throwable exception;
		private boolean interrupted, finished;

		public FilterJob(String label, QueryFilter filter, FilterContext context) {
			this.label = requireNonNull(label);
			this.filter = requireNonNull(filter);
			this.context = requireNonNull(context);
		}

		public void interrupt() {
			ThreadVerifier tv = threadVerifier;

			finished = true;
			interrupted = true;

			if(tv!=null) {
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
			checkState("hread verifier not initialized yet", threadVerifier!=null);
			threadVerifier.checkThread();
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
			} finally {
				finished = true;
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

		private ExecutorService executor;
		private LongFunction<Container> candidateLookup;
		private Context context;
		private IqlStream stream;
		private IqlQuery query;

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
