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
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.query.api.engine.result.Match;
import de.ims.icarus2.query.api.engine.result.MatchCollector;
import de.ims.icarus2.query.api.engine.result.MatchSource;
import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
public class QueryUtils {

	public static final int BUFFER_STARTSIZE = 1<<10;

	private static class WorkerThreadFactory implements ThreadFactory {

	    private final AtomicInteger counter;
	    private final String prefix;
	    private final ThreadFactory factory;

	    public WorkerThreadFactory(String prefix) {
	        this.counter = new AtomicInteger();
	        this.prefix = prefix;
	        this.factory = Executors.defaultThreadFactory();
	    }

	    @Override
	    public Thread newThread(Runnable r) {
	        Thread thread = factory.newThread(r);
	        thread.setName(prefix + "-worker-" + counter.incrementAndGet());
	        return thread;
	    }
	}

	private static final AtomicInteger factoryCounter = new AtomicInteger();

	public static ThreadFactory createThreadFactory() {
		return new WorkerThreadFactory("icqp-batch-"+factoryCounter.incrementAndGet());
	}

	public static ExecutorService createExecutorService(int workerLimit) {
		checkArgument("worker limit must be positive", workerLimit>0);

		if(workerLimit==1) {
			return Executors.newSingleThreadExecutor(createThreadFactory());
		}

		return Executors.newFixedThreadPool(workerLimit, createThreadFactory());
	}

	public static QueryInput fixedInput(Container...containers) {
		return new FixedQueryInput(containers);
	}

	static class FixedQueryInput implements QueryInput {
		private final Container[] items;
		private int cursor = 0;

		FixedQueryInput(Container[] items) {
			this.items = requireNonNull(items);
		}

		@Override
		public int load(Container[] buffer) {
			if(cursor>=items.length) {
				return 0;
			}

			final int len = Math.min(buffer.length, items.length-cursor+1);
			System.arraycopy(items, cursor, buffer, 0, len);
			cursor += len;

			return len;
		}
	}

	public static QueryOutput bufferedOutput(LaneSetup lane, int limit) {
		return new BufferedSingleLaneQueryOutput(lane, limit);
	}

	public static QueryOutput bufferedOutput(LaneSetup lane) {
		return bufferedOutput(lane, UNSET_INT);
	}

	static class BufferedSingleLaneQueryOutput implements QueryOutput, MatchCollector {

		private final LaneSetup lane;
		private final int limit;
		private final List<Match> buffer = new ObjectArrayList<>();

		BufferedSingleLaneQueryOutput(LaneSetup lane, int limit) {
			this.lane = requireNonNull(lane);
			this.limit = limit;
		}

		@Override
		public MatchCollector createCollector(LaneSetup lane, ThreadVerifier threadVerifier) {
			checkArgument("Foreign lane", lane==this.lane);
			return this;
		}

		@Override
		public boolean collect(MatchSource source) {
			if(isFull()) {
				return false;
			}

			buffer.add(source.toMatch());

			return !isFull();
		}

		@Override
		public void closeCollector(LaneSetup lane) { /* no -op*/ }

		@Override
		public long countMatches() { return buffer.size(); }

		@Override
		public boolean isFull() { return limit!=UNSET_INT && buffer.size()>=limit; }

		public void reset() { buffer.clear(); }

		public int getLimit() { return limit; }

		public LaneSetup getLane() { return lane; }

		public List<Match> getMatches() { return CollectionUtils.unmodifiableListProxy(buffer); }
	}
}
