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
import de.ims.icarus2.query.api.engine.QueryOutput.BufferedQueryOutput;
import de.ims.icarus2.query.api.engine.result.Match;
import de.ims.icarus2.query.api.engine.result.MatchCollector;
import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
public class QueryUtils {

	public static final int BUFFER_STARTSIZE = 1<<10;

	public static final int DEFAULT_BATCH_SIZE = 1<<10;

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
		public synchronized int load(Container[] buffer) {
			if(cursor>=items.length) {
				return 0;
			}

			final int len = Math.min(buffer.length, items.length-cursor);
			System.arraycopy(items, cursor, buffer, 0, len);
			cursor += len;

			return len;
		}
	}

	public static BufferedSingleLaneQueryOutput bufferedOutput(int id, int limit) {
		return new BufferedSingleLaneQueryOutput(id, limit);
	}

	public static BufferedSingleLaneQueryOutput bufferedOutput(int id) {
		return bufferedOutput(id, UNSET_INT);
	}

	public static class BufferedSingleLaneQueryOutput implements BufferedQueryOutput {

		private final int id;
		private final int limit;
		private final List<Match> buffer = new ObjectArrayList<>();
		private final Object lock = new Object();

		public BufferedSingleLaneQueryOutput(int id, int limit) {
			this.id = id;
			this.limit = limit;
		}

		@Override
		public MatchCollector createCollector(int id, ThreadVerifier threadVerifier) {
			checkArgument("Foreign lane", id==this.id);
			return source -> {
				if(Tripwire.ACTIVE) {
					threadVerifier.checkThread();
				}
				synchronized (lock) {
					if(!isFull()) {
						buffer.add(source.toMatch());
					}
					return !isFull();
				}
			};
		}

		@Override
		public void closeCollector(int id) {
			checkArgument("Foreign lane", id==this.id);
		}

		@Override
		public long countMatches() { return buffer.size(); }

		@Override
		public boolean isFull() { return limit!=UNSET_INT && buffer.size()>=limit; }

		public void reset() {
			synchronized (lock) {
				buffer.clear();
			}
		}

		public int getLimit() { return limit; }

		public int getId() { return id; }

		@Override
		public List<Match> getMatches() { return CollectionUtils.unmodifiableListProxy(buffer); }
	}
}
