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
package de.ims.icarus2.query.api.engine;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.lang.Primitives._int;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.LongFunction;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.view.Scope;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.engine.matcher.Matcher;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.Role;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.StructureMatcher;
import de.ims.icarus2.query.api.engine.result.MatchAccumulator;
import de.ims.icarus2.query.api.engine.result.MatchAggregator;
import de.ims.icarus2.query.api.engine.result.MatchSource;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.query.api.iql.IqlStream;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.AccumulatingException;
import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Implements a {@link QueryJob} that operates on a single {@link IqlStream}.
 * Subclasses model different scenarios depending on the number of {@link IqlLane lanes}
 * involved or whether joins between lanes are explicit or done via global constraints.
 *
 * @author Markus Gärtner
 *
 */
public abstract class SingleStreamJob implements QueryJob, QueryWorker.Task {

	private static final Logger log = LoggerFactory.getLogger(SingleStreamJob.class);

	public static Builder builder() { return new Builder(); }

	private static final String KEY_BUFFER = "buffer";
	private static final String KEY_MATCHER = "matcher";
	private static final String KEY_MATCHERS = "matchers";

	protected final IqlQuery query;
	protected final QueryInput input;
	protected final QueryOutput output;
	protected final BiConsumer<Thread, Throwable> exceptionHandler;
	protected final int batchSize;
	//TODO

	protected SingleStreamJob(Builder builder) {
		query = builder.getQuery();
		input = builder.getInput();
		output = builder.getOutput();
		exceptionHandler = builder.getExceptionHandler();
		batchSize = builder.getBatchSize();
	}

	@Override
	public IqlQuery getSource() { return query; }

	public int getBatchSize() { return batchSize; }

	protected final void disconnect(Corpus corpus) {
		CorpusManager corpusManager = corpus.getManager();
		try {
			corpusManager.disconnect(corpus.getManifest());
		} catch (InterruptedException e) {
			log.error("Corpus disconnection process interrupted", e);
		} catch (AccumulatingException e) {
			log.error("Corpus disconnetion encoutnered %d errors", _int(e.getExceptionCount()), e);
		}
	}

	protected abstract void afterJobFinished();

	@Override
	public JobController execute(ExecutorService executorService, int workerLimit) {
		requireNonNull(executorService);
		checkArgument("worker limit must be positive", workerLimit>0);

		DefaultJobController controller = DefaultJobController.builder()
				.executorService(executorService)
				.query(query)
				.exceptionHandler((worker, t) -> exceptionHandler.accept(worker.getThread(), t))
				.shutdownHook(this::afterJobFinished)
				.build();

		for (int i = 0; i < workerLimit; i++) {
			controller.createWorker("worker-"+i, this);
		}

		return controller;
	}

	/**
	 * Uses the given matcher to process all the still available input data
	 * in batches. This method returns once there are no more remaining items
	 * in the underlying {@link QueryInput input} or the {@link QueryOutput output}
	 * is {@link QueryOutput#isFull() full}.
	 * <p>
	 * Additionally the buffer used for storing items from the input will be
	 * stored as client data in the given worker with {@link #KEY_BUFFER} as key.
	 */
	protected final void matchInput(Matcher<Container> matcher, QueryWorker worker) {

		final Container[] buffer = new Container[batchSize];
		worker.putClientData(KEY_BUFFER, buffer);

		int length;
		while((length = input.load(buffer)) > 0) {
			for (int i = 0; i < length; i++) {
				// Abort search when canceled
				if(worker.isCanceled()) {
					return;
				}

				Container target = buffer[i];
				// We rely on the original index values assigned to each container
				long index = target.getIndex();

				if(matcher.matches(index, target)) {
					// Exit entire search if we have enough results already
					if(output.isFull()) {
						return;
					}
				}
			}
		}
	}

	static class SingleLaneJob extends SingleStreamJob {

		private final StructurePattern pattern;

		SingleLaneJob(Builder builder) {
			super(builder);

			pattern = builder.getPattern();
			checkState("Single pattern must have role 'SINGLETON'", pattern.getRole()==Role.SINGLETON);
		}

		private StructureMatcher createMatcher(ThreadVerifier threadVerifier) {
			return  pattern.matcherBuilder()
				// Use same thread verifier for all components of the pipeline
				.threadVerifier(threadVerifier)
				// Instantiate result handler for this thread
				.matchCollector(output.createTerminalCollector(threadVerifier))
				.build();
		}

		/**
		 * @see de.ims.icarus2.query.api.engine.QueryWorker.Task#execute(de.ims.icarus2.query.api.engine.QueryWorker)
		 */
		@Override
		public void execute(QueryWorker worker) throws InterruptedException {
			final ThreadVerifier threadVerifier = worker.getThreadVerifier();
			// Make sure we're on the right thread to begin with!
			if(Tripwire.ACTIVE) {
				threadVerifier.checkThread();
			}

			final StructureMatcher matcher = createMatcher(threadVerifier);
			worker.putClientData(KEY_MATCHER, matcher);

			// Now process the input data in batches
			matchInput(matcher, worker);

			/* No cleanup needed here. We do that in cleanup(worker) method!
			 * That way we can be sure that cleanup is being done even if errors
			 * occurred or the process got canceled.
			 */
		}

		@Override
		public void cleanup(QueryWorker worker) {
			if(Tripwire.ACTIVE) {
				worker.getThreadVerifier().checkThread();
			}

			// Make sure the output buffer is shut down.
			output.closeTerminalCollector(worker.getThreadVerifier());

			// The following cleanup steps are not thread-related

			// Cleanup all our previously stored container references
			Container[] buffer = worker.removeClientData(KEY_BUFFER);
			Arrays.fill(buffer, null);

			// Force a reset of the matcher just to be sure
			StructureMatcher matcher = worker.removeClientData(KEY_MATCHER);
			matcher.reset();

			// At this point the worker should not hold any more references to our data
		}

		@Override
		protected void afterJobFinished() {
			disconnect(pattern.getContext().getCorpus());
		}
	}

	static class MultiLaneJob extends SingleStreamJob {

		/** Raw patterns to create matchers from */
		private final StructurePattern[] patterns;
		/** Indicates that caching is possible for the bridge between pattern i and i+1 */
		private final boolean[] cachableBridge;

		MultiLaneJob(Builder builder) {
			super(builder);
			patterns = builder.getPatterns().toArray(new StructurePattern[0]);
			assert patterns.length>1;

			cachableBridge = new boolean[patterns.length-1];
			//TODO fill cachableBridge array depending on used member labels in the patterns
		}

		private ItemLayer findLayer(Scope scope, IqlLane lane) {
			if(lane.isProxy()) {
				return scope.getPrimaryLayer();
			}

			ItemLayer layer = scope.getCorpus().getLayer(lane.getName(), false);
			if(!scope.containsLayer(layer))
				throw new QueryException(QueryErrorCode.INCORRECT_USE,
						"Layer not registered with scope for query: "+lane.getName());

			return layer;
		}

		private Matcher<Container> createMatcher(ThreadVerifier threadVerifier) {
			// We need to construct the matcher combination back to front

			final int laneCount = patterns.length;
			final Scope scope = patterns[0].getContext().getScope();

			// Final collector that just aggregates individual matches into a single MultiMatch
			final MatchSource[] nonTerminalMatches = new MatchSource[laneCount-1];
			final MatchAggregator aggregator = new MatchAggregator(nonTerminalMatches,
					output.createTerminalCollector(threadVerifier));

			final ItemLayer[] layers = new ItemLayer[laneCount];

			// The final matcher doesn't need any bridging
			final int last = laneCount-1;
			Matcher<Container> matcher = patterns[last].matcherBuilder()
					.threadVerifier(threadVerifier)
					.matchCollector(aggregator)
					.build();
			layers[last] = findLayer(scope, patterns[last].getSource());

			// Now build and link all the intermediate bridges (0 .. n-1)
			for (int i = last-1; i >= 0; i--) {
				final StructurePattern pattern = patterns[i];
				final ItemLayer layer = layers[i] = findLayer(scope, pattern.getSource());
				final ItemLayer nextLayer = layers[i+1];

				LongFunction<Container> itemLookup;
				LaneMapper laneMapper;

				final LaneBridge bridge;
				if(cachableBridge[i]) {
					bridge = LaneBridge.Cached.builder()
							.accumulator(new MatchAccumulator()) // we just use the default settings
							.itemLookup(itemLookup)
							.laneMapper(laneMapper)
							.next(matcher)
							.pattern(pattern)
							.build();
				} else {
					bridge = LaneBridge.Uncached.builder()
							.bufferSize(QueryUtils.BUFFER_STARTSIZE)
							.itemLookup(itemLookup)
							.laneMapper(laneMapper)
							.next(matcher)
							.pattern(pattern)
							.build();
				}

				nonTerminalMatches[i] = bridge;
				matcher = bridge;
			}

			return matcher;
		}

		@Override
		public void execute(QueryWorker worker) throws InterruptedException {
			final ThreadVerifier threadVerifier = worker.getThreadVerifier();
			if(Tripwire.ACTIVE) {
				threadVerifier.checkThread();
			}

			final StructureMatcher matcher = createMatcher(threadVerifier);

			// Now process the input data in batches
			matchInput(matcher, worker);

			/* No cleanup needed here. We do that in cleanup(worker) method!
			 * That way we can be sure that cleanup is being done even if errors
			 * occurred or the process got canceled.
			 */
		}

		@Override
		public void cleanup(QueryWorker worker) {
			if(Tripwire.ACTIVE) {
				worker.getThreadVerifier().checkThread();
			}

			// Make sure the output buffer is shut down.
			output.closeTerminalCollector(worker.getThreadVerifier());

			// The following cleanup steps are not thread-related

			// Cleanup all our previously stored container references
			Container[] buffer = worker.removeClientData(KEY_BUFFER);
			Arrays.fill(buffer, null);

			// Force a reset of all the matchers just to be sure
			StructureMatcher[] matchers = worker.removeClientData(KEY_MATCHERS);
			Stream.of(matchers).forEach(StructureMatcher::reset);

			// At this point the worker should not hold any more references to our data
		}

		@Override
		protected void afterJobFinished() {
			disconnect(patterns[0].getContext().getCorpus());
		}
	}

	public static class Builder extends AbstractBuilder<Builder, SingleStreamJob> {

		private final List<StructurePattern> patterns = new ObjectArrayList<>();
		private IqlQuery query;
		private QueryInput input;
		private QueryOutput output;
		private BiConsumer<Thread, Throwable> exceptionHandler;
		private Integer batchSize;

		private Builder() { /* no-op */ }

		public IqlQuery getQuery() { return query; }

		public Builder query(IqlQuery query) {
			requireNonNull(query);
			checkArgument("Query already set", this.query==null);
			this.query = query;
			return this;
		}

		public List<StructurePattern> getPatterns() { return CollectionUtils.unmodifiableListProxy(patterns); }
		public StructurePattern getPattern() {
			checkState("Must have exactly 1 pattern registered", patterns.size()==1);
			return patterns.get(0);
		}

		public Builder addPattern(StructurePattern pattern) {
			requireNonNull(pattern);
			patterns.add(pattern);
			return this;
		}

		public Builder addPatterns(List<StructurePattern> patterns) {
			requireNonNull(patterns);
			checkArgument("patterns list empty", !patterns.isEmpty());
			this.patterns.addAll(patterns);
			return this;
		}

		public QueryInput getInput() { return input; }

		public Builder input(QueryInput input) {
			requireNonNull(input);
			checkArgument("Input already set", this.input==null);
			this.input = input;
			return this;
		}

		public QueryOutput getOutput() { return output; }

		public Builder output(QueryOutput output) {
			requireNonNull(output);
			checkArgument("Output already set", this.output==null);
			this.output = output;
			return this;
		}

		public BiConsumer<Thread, Throwable> getExceptionHandler() { return exceptionHandler; }

		public Builder exceptionHandler(BiConsumer<Thread, Throwable> exceptionHandler) {
			requireNonNull(exceptionHandler);
			checkArgument("Exception handler already set", this.exceptionHandler==null);
			this.exceptionHandler = exceptionHandler;
			return this;
		}

		public int getBatchSize() { return batchSize==null ? QueryUtils.DEFAULT_BATCH_SIZE : batchSize.intValue(); }

		public Builder batchSize(int batchSize) {
			checkArgument("Batch size must be positive", batchSize>0);
			checkArgument("Batch size already set", this.batchSize==null);
			this.batchSize = Integer.valueOf(batchSize);
			return this;
		}

		//TODO

		@Override
		protected void validate() {
			checkState("No patterns defined", !patterns.isEmpty());
			checkState("No input defined", input!=null);
			checkState("No output defined", output!=null);
			checkState("No exception handler defined", exceptionHandler!=null);
		}

		@Override
		protected SingleStreamJob create() {
			if(patterns.size()==1) {
				return new SingleLaneJob(this);
			}

			return new MultiLaneJob(this);
		}

	}
}
