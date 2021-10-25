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
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.Role;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.StructureMatcher;
import de.ims.icarus2.query.api.engine.result.Tripwire;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.query.api.iql.IqlStream;
import de.ims.icarus2.util.AbstractBuilder;
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

	public static Builder builder() { return new Builder(); }

	private static final String KEY_BUFFER = "buffer";
	private static final String KEY_MATCHER = "matcher";

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

	@Override
	public JobController execute(ExecutorService executorService, int workerLimit) {
		requireNonNull(executorService);
		checkArgument("worker limit must be positive", workerLimit>0);

		DefaultJobController controller = DefaultJobController.builder()
				.executorService(executorService)
				.query(query)
				.exceptionHandler((worker, t) -> exceptionHandler.accept(worker.getThread(), t))
				.build();

		for (int i = 0; i < workerLimit; i++) {
			controller.createWorker("worker-"+i, this);
		}

		return controller;
	}

	static class SingleLaneJob extends SingleStreamJob {

		private final StructurePattern pattern;

		SingleLaneJob(Builder builder) {
			super(builder);

			pattern = builder.getPattern();
			checkState("Single pattern must have role 'SINGLETON'", pattern.getRole()==Role.SINGLETON);
		}

		/**
		 * @see de.ims.icarus2.query.api.engine.QueryWorker.Task#execute(de.ims.icarus2.query.api.engine.QueryWorker)
		 */
		@Override
		public void execute(QueryWorker worker) throws InterruptedException {
			final ThreadVerifier threadVerifier = worker.getThreadVerifier();
			// make sure we're on the right thread to begin with!
			if(Tripwire.ACTIVE) {
				threadVerifier.checkThread();
			}

			final Container[] buffer = new Container[batchSize];
			worker.putClientData(KEY_BUFFER, buffer);

			final StructureMatcher matcher = pattern.matcherBuilder()
					// Use same thread verifier for all components of the pipeline
					.threadVerifier(threadVerifier)
					// Instantiate result handler for this thread
					.matchCollector(output.createCollector(pattern.getId(), threadVerifier))
					.build();
			worker.putClientData(KEY_MATCHER, matcher);

			// Now process the input data in batches
			int length;
			scan : while((length = input.load(buffer)) > 0) {
				for (int i = 0; i < length; i++) {
					// Abort search when canceled
					if(worker.isCanceled()) {
						break scan;
					}

					Container target = buffer[i];
					// We rely on the original index values assigned to each container
					long index = target.getIndex();

					if(matcher.matches(index, target)) {
						// Exit entire search if we have enough results already
						if(output.isFull()) {
							break scan;
						}
					}
				}
			}

			/* No cleanup needed here. We do that in cleanup(worker) method!
			 * That way we can be sure that cleanup is being done even if errors
			 * occurred or the process got canceled.
			 */
		}

		@Override
		public void cleanup(QueryWorker worker) {
			/*
			 *  Make sure the output buffer is shut down.
			 *
			 *  We do this cleanup step first, since the handler might have a
			 *  thread verifier assigned to it and so will throw an exception
			 *  if this method is called from another thread!
			 */
			output.closeCollector(pattern.getId());

			// The following cleanup steps are not thread-related

			// Cleanup all our previously stored container references
			Container[] buffer = worker.removeClientData(KEY_BUFFER);
			Arrays.fill(buffer, null);

			// Force a reset of the matcher just to be sure
			StructureMatcher matcher = worker.removeClientData(KEY_MATCHER);
			matcher.reset();

			// At this point the worker should not hold any more references to our data
		}
	}

	static class MultiLaneJob extends SingleStreamJob {

		private final StructurePattern[] patterns;

		MultiLaneJob(Builder builder) {
			super(builder);

			//TODO

		}

		@Override
		public void execute(QueryWorker worker) throws InterruptedException {
			// TODO Auto-generated method stub

		}

		@Override
		public void cleanup(QueryWorker worker) {
			// TODO Auto-generated method stub

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
