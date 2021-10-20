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

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.StructureMatcher;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.query.api.iql.IqlStream;
import de.ims.icarus2.util.AbstractBuilder;
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
	protected final int batchSize;
	protected final QueryInput input;
	protected final QueryOutput output;
	//TODO

	protected SingleStreamJob(Builder builder) {

	}

	@Override
	public IqlQuery getSource() { return query; }

	public int getBatchSize() { return batchSize; }

	@Override
	public JobController execute(ExecutorService executorService, int workerLimit) {
		requireNonNull(executorService);
		checkArgument("worker limit must be positive", workerLimit>0);

		DefaultJobController controller = DefaultJobController.builder()
				//TODO configure builder
				.build();

		for (int i = 0; i < workerLimit; i++) {
			controller.createWorker("worker-"+i, this);
		}

		return controller;
	}

	static class SingleLaneJob extends SingleStreamJob {

		private final LaneSetup lane;

		SingleLaneJob(Builder builder) {
			super(builder);

			//TODO
		}

		/**
		 * @see de.ims.icarus2.query.api.engine.QueryWorker.Task#execute(de.ims.icarus2.query.api.engine.QueryWorker)
		 */
		@Override
		public void execute(QueryWorker worker) throws InterruptedException {
			final ThreadVerifier threadVerifier = worker.getThreadVerifier();
			// make sure we're on the right thread to begin with!
			threadVerifier.checkThread();

			final Container[] buffer = new Container[batchSize];
			worker.putClientData(KEY_BUFFER, buffer);

			final StructureMatcher matcher = lane.getPattern().matcherBuilder()
					// Use same thread verifier as all components of the pipeline
					.threadVerifier(threadVerifier)
					// Instantiate result handler for this thread
					.matchCollector(output.createCollector(lane, threadVerifier))
					.build();
			worker.putClientData(KEY_MATCHER, matcher);

			// Now process the input data in batches
			int length;
			scan : while((length = input.load(buffer)) > 0) {
				for (int i = 0; i < length; i++) {
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
			output.closeCollector(lane);

			// Cleanup all our previously stored container references
			Container[] buffer = worker.getClientData(KEY_BUFFER);
			Arrays.fill(buffer, null);

			// Force a reset of the matcher just to be sure
			StructureMatcher matcher = worker.getClientData(KEY_MATCHER);
			matcher.reset();
		}
	}

	static class MultiLaneJob extends SingleStreamJob {

		private final LaneSetup[] lanes;

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

		private final List<LaneSetup> lanes = new ObjectArrayList<>();
		private IqlStream stream;

		private Builder() { /* no-op */ }

		//TODO

		@Override
		protected void validate() {
			checkState("No lane setups defined", !lanes.isEmpty());
		}

		@Override
		protected SingleStreamJob create() {
			if(lanes.size()==1) {
				return new SingleLaneJob(this);
			}

			return new MultiLaneJob(this);
		}

	}
}
