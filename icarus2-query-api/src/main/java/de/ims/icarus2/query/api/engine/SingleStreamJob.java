/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.LongFunction;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.query.api.engine.CorpusData.LayerRef;
import de.ims.icarus2.query.api.engine.matcher.Matcher;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.Role;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.StructureMatcher;
import de.ims.icarus2.query.api.engine.result.MatchAccumulator;
import de.ims.icarus2.query.api.engine.result.MatchAggregator;
import de.ims.icarus2.query.api.exp.EvaluationContext.LaneContext;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.query.api.iql.IqlStream;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

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
	private static final String KEY_BRIDGES = "bridges";

	protected final CorpusData corpusData;
	protected final IqlQuery query;
	protected final QueryInput input;
	protected final QueryOutput output;
	protected final int batchSize;

	protected final List<Closeable> closeables;

	protected SingleStreamJob(Builder builder) {
		query = builder.getQuery();
		input = builder.getInput();
		output = builder.getOutput();
		batchSize = builder.getBatchSize();
		corpusData = builder.getCorpusData();
		closeables = new ObjectArrayList<>(builder.getCloseables());
	}

	@Override
	public IqlQuery getSource() { return query; }

	public int getBatchSize() { return batchSize; }

	private void shutdown() {
		corpusData.close();
	}

	@Override
	public JobController execute(ExecutorService executorService, int workerLimit) {
		requireNonNull(executorService);
		checkArgument("worker limit must be positive", workerLimit>0);

		DefaultJobController controller = DefaultJobController.builder()
				.executorService(executorService)
				.query(query)
				.shutdownHook(this::shutdown)
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

	/**
	 * @see de.ims.icarus2.query.api.engine.QueryWorker.Task#execute(de.ims.icarus2.query.api.engine.QueryWorker)
	 */
	@Override
	public final void execute(QueryWorker worker) throws InterruptedException {
		// Make sure we're on the right thread to begin with!
		if(Tripwire.ACTIVE) {
			worker.getThreadVerifier().checkThread();
		}

		final Matcher<Container> matcher = createMatcher(worker);

		// Now process the input data in batches
		matchInput(matcher, worker);

		/* No cleanup needed here. We do that in cleanup(worker) method!
		 * That way we can be sure that cleanup is being done even if errors
		 * occurred or the process got canceled.
		 */
	}

	@Override
	public final void cleanup(QueryWorker worker) {
		if(Tripwire.ACTIVE) {
			worker.getThreadVerifier().checkThread();
		}

		/* Make sure the output buffer is shut down.
		 * This can be an I/O heavy process depending on the configuration
		 * of the output implementation and its buffer(s).
		 */
		output.closeTerminalCollector(worker.getThreadVerifier());

		// The following cleanup steps are not thread-related

		// Cleanup all our previously stored container references
		Container[] buffer = worker.removeClientData(KEY_BUFFER);
		Arrays.fill(buffer, null);

		// Force a reset of the matcher just to be sure
		@SuppressWarnings("resource")
		Matcher<?> matcher = worker.removeClientData(KEY_MATCHER);
		matcher.close();

		// Let implementation handle further cleanup
		cleanupImpl(worker);

		// At this point the worker should not hold any more references to our data
	}

	protected abstract void cleanupImpl(QueryWorker worker);

	protected abstract Matcher<Container> createMatcher(QueryWorker worker);

	static class SingleLaneJob extends SingleStreamJob {

		private final StructurePattern pattern;

		SingleLaneJob(Builder builder) {
			super(builder);

			pattern = builder.getPattern();
			checkState("Single pattern must have role 'SINGLETON'", pattern.getRole()==Role.SINGLETON);
		}

		@Override
		protected Matcher<Container> createMatcher(QueryWorker worker) {
			StructureMatcher matcher = pattern.matcherBuilder()
				// Use same thread verifier for all components of the pipeline
				.threadVerifier(worker.getThreadVerifier())
				// Instantiate result handler for this thread
				.matchCollector(output.createTerminalCollector(worker.getThreadVerifier()))
				.build();
			worker.putClientData(KEY_MATCHER, matcher);
			return matcher;
		}

		@Override
		protected void cleanupImpl(QueryWorker worker) {
			// no-op
		}
	}

	static class MultiLaneJob extends SingleStreamJob {

		/** Raw patterns to create matchers from */
		private final StructurePattern[] patterns;
		/** Indicates that caching is possible for the bridge between pattern i and i+1 */
		private final boolean[] cachableBridge;

		MultiLaneJob(Builder builder) {
			super(builder);
			patterns = CollectionUtils.toArray(builder.getPatterns(), StructurePattern[]::new);
			assert patterns.length>1;

			cachableBridge = new boolean[patterns.length-1];

			/*
			 *  We can cache the mapping towards a pattern if the set of labels
			 *  used by the target pattern is disjoint to the total accumulated
			 *  set of labels so far.
			 */
			final Set<String> globalLabels = new ObjectOpenHashSet<>();
			for (int i = 0; i < patterns.length; i++) {
				StructurePattern pattern = patterns[i];
				Set<String> usedLabels = pattern.getReferencedMembers();

				if(i>0) {
					/*
					 *  Performance info:
					 *  Collections.disjoint() checks for collection
					 *  types to determine which collection to iterate and which to use
					 *  for containment checks. We use globalLabels as first argument here
					 *  so it gets picked as "contains", meaning less overall iterations.
					 */
					cachableBridge[i-1] = Collections.disjoint(globalLabels, usedLabels);
				}

				globalLabels.addAll(usedLabels);
			}
		}

		private static LayerRef getPrimaryLayer(StructurePattern pattern) {
			LaneContext laneContext = pattern.getContext().getLaneContext();
			return laneContext.getLaneInfo().getLayer();
		}

		@Override
		@SuppressWarnings("resource")
		protected Matcher<Container> createMatcher(QueryWorker worker) {
			final ThreadVerifier threadVerifier = worker.getThreadVerifier();
			// We need to construct the matcher combination back to front

			final int laneCount = patterns.length;

			final LaneBridge[] bridges = new LaneBridge[laneCount-1];
			worker.putClientData(KEY_BRIDGES, bridges);
			// Final collector that just aggregates individual matches into a single MultiMatch
			//FIXME we have to hand over the raw array here and disable defensive copying in
			// the aggregator to allow us lazy populating the array
			final MatchAggregator aggregator = new MatchAggregator(bridges,
					output.createTerminalCollector(threadVerifier));

			final int last = laneCount-1;
			final LayerRef[] layers = new LayerRef[laneCount];

			// The final matcher doesn't need any bridging
			final StructureMatcher lastMatcher = patterns[last].matcherBuilder()
					.threadVerifier(threadVerifier)
					.matchCollector(aggregator)
					.build();
			worker.putClientData(KEY_MATCHER, lastMatcher);
			layers[last] = getPrimaryLayer(patterns[last]);

			// Now build and link all the intermediate bridges (n-1 .. 0)
			Matcher<Container> previousMatcher = lastMatcher;
			for (int i = last-1; i >= 0; i--) {
				final StructurePattern activePattern = patterns[i];
				assert activePattern.getContext().getCorpusData()==corpusData : "mixed corpus data";

				final LayerRef layer = layers[i] = getPrimaryLayer(activePattern);
				final LayerRef nextLayer = layers[i+1];

				final LongFunction<Item> itemLookup = corpusData.access(nextLayer);
				final LongFunction<Container> containerLookup = idx -> Container.class.cast(itemLookup.apply(idx));
				final LaneMapper laneMapper = corpusData.map(layer, nextLayer);

				final LaneBridge bridge;
				if(cachableBridge[i]) {
					bridge = LaneBridge.Cached.builder()
							.accumulator(new MatchAccumulator()) // we just use the default settings
							.itemLookup(containerLookup)
							.laneMapper(laneMapper)
							.next(previousMatcher)
							.pattern(activePattern)
							.threadVerifier(threadVerifier)
							.build();
				} else {
					bridge = LaneBridge.Uncached.builder()
							.bufferSize(QueryUtils.BUFFER_STARTSIZE)
							.itemLookup(containerLookup)
							.laneMapper(laneMapper)
							.next(previousMatcher)
							.pattern(activePattern)
							.threadVerifier(threadVerifier)
							.build();
				}

				bridges[i] = bridge;
				previousMatcher = bridge;
			}

			return previousMatcher;
		}

		@Override
		protected void cleanupImpl(QueryWorker worker) {

			LaneBridge[] bridges = worker.removeClientData(KEY_BRIDGES);
			Stream.of(bridges).forEach(LaneBridge::close);
		}
	}

	public static class Builder extends AbstractBuilder<Builder, SingleStreamJob> {

		private final List<StructurePattern> patterns = new ObjectArrayList<>();
		private IqlQuery query;
		private QueryInput input;
		private QueryOutput output;
		private Integer batchSize;

		private final List<Closeable> closeables = new ObjectArrayList<>();

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

		public List<Closeable> getCloseables() { return CollectionUtils.unmodifiableListProxy(closeables); }
		public Builder addCloseable(Closeable...closeables) {
			requireNonNull(closeables);
			CollectionUtils.feedItems(this.closeables, closeables);
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

		public int getBatchSize() { return batchSize==null ? QueryUtils.DEFAULT_BATCH_SIZE : batchSize.intValue(); }

		public Builder batchSize(int batchSize) {
			checkArgument("Batch size must be positive", batchSize>0);
			checkArgument("Batch size already set", this.batchSize==null);
			this.batchSize = Integer.valueOf(batchSize);
			return this;
		}

		//TODO

		protected CorpusData getCorpusData() {
			return patterns.get(0).getContext().getCorpusData();
		}

		@Override
		protected void validate() {
			checkState("No patterns defined", !patterns.isEmpty());
			checkState("No input defined", input!=null);
			checkState("No output defined", output!=null);
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
