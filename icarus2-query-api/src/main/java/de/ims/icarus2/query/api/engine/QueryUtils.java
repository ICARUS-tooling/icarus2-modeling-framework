/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;

import de.ims.icarus2.model.api.corpus.CorpusOwner;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.view.streamed.StreamedCorpusView;
import de.ims.icarus2.query.api.engine.result.Match;
import de.ims.icarus2.query.api.engine.result.MatchCollector;
import de.ims.icarus2.util.annotations.PreliminaryValue;
import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
public class QueryUtils {

	@PreliminaryValue
	public static final int BUFFER_STARTSIZE = 1<<10;

	@PreliminaryValue
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

	@ThreadSafe
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

		@Override
		public void close() { /* no-op */ }
	}

	public static QueryInput streamedInput(StreamedCorpusView view) {
		 return new StreamedQueryInput(view);
	}

	@ThreadSafe
	static class StreamedQueryInput implements QueryInput, CorpusOwner {
		private final StreamedCorpusView view;

		StreamedQueryInput(StreamedCorpusView view) {
			this.view = requireNonNull(view);
			view.acquire(this);
		}

		@Override
		public synchronized int load(Container[] buffer) {
			int count = 0;
			while(count < buffer.length && view.advance()) {
				buffer[count++] = (Container) view.currentItem();
			}
			return count;
		}

		@Override
		public void close() {
			view.close();
		}

		@Override
		public Optional<String> getId() { return Optional.of(getClass().getSimpleName()); }

		@Override
		public Optional<String> getDescription() { return Optional.empty(); }

		@Override
		public Optional<String> getName() { return Optional.of(getClass().getSimpleName()); }

		@Override
		public boolean release() throws InterruptedException {
			return true;
		}
	}

	public static BufferedQueryOutput bufferedOutput(int id, int limit) {
		return new BufferedQueryOutput(id, limit);
	}

	public static BufferedQueryOutput bufferedOutput(int id) {
		return bufferedOutput(id, UNSET_INT);
	}

	@ThreadSafe
	public static class BufferedQueryOutput implements QueryOutput {

		private final int id;
		private final int limit;
		private final List<Match> buffer = new ObjectArrayList<>();
		private final Object lock = new Object();

		public BufferedQueryOutput(int id, int limit) {
			this.id = id;
			this.limit = limit;
		}

		@Override
		public MatchCollector createTerminalCollector(ThreadVerifier threadVerifier) {
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
		public void closeTerminalCollector(ThreadVerifier threadVerifier) {
			if(Tripwire.ACTIVE) {
				threadVerifier.checkThread();
			}
			// nothing to do here really
		}

		@Override
		public long countMatches() { return buffer.size(); }

		@Override
		public boolean isFull() { return limit!=UNSET_INT && buffer.size()>=limit; }

		@Override
		public void close() { /* no-op */ }

		@Override
		public void discard() {
			synchronized (lock) {
				buffer.clear();
			}
		}

		public int getLimit() { return limit; }

		public int getId() { return id; }

		public List<Match> getMatches() { return CollectionUtils.unmodifiableListProxy(buffer); }
	}
}
