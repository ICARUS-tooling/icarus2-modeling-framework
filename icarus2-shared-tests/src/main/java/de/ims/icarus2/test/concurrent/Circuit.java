/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.test.concurrent;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.function.Executable;

/**
 * @author Markus Gärtner
 *
 */
public class Circuit implements AutoCloseable {

	private final ExecutorService executor;
	private final List<Executable> tasks = new ArrayList<>();

	public Circuit(int nThreads) {
		this(Executors.newFixedThreadPool(nThreads));
	}

	public Circuit(ExecutorService executor) {
		this.executor = requireNonNull(executor);
	}

	public Circuit addTask(Executable task) {
		tasks.add(requireNonNull(task));
		return this;
	}

	public synchronized Duration executeAndWait() throws InterruptedException {
		return executeAndWait(0, TimeUnit.MILLISECONDS);
	}

	/**
	 * Schedules all the previously supplied tasks and awaits their
	 * completion or until the given {@code timeout} has elapsed.
	 */
	public synchronized Duration executeAndWait(long timeout, TimeUnit unit)
			throws InterruptedException {
		if(tasks.isEmpty())
			throw new IllegalStateException("No tasks to execute");

		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(tasks.size());

		Vector<Throwable> errors = new Vector<>();

		Collections.shuffle(tasks);

		for(Executable task : tasks) {
			executor.execute(() -> {
				try {
					start.await();
					task.execute();
				} catch (Throwable e) {
					errors.add(e);
				} finally {
					done.countDown();
				}
			});
		}

		Instant tStart = Instant.now();

		// Signal all the tasks to start
		start.countDown();

		// Await completion or failure of all tasks
		if(timeout==0) {
			done.await();
		} else if(!done.await(timeout, unit))
			throw new AssertionError(String.format("Task execution timed out after %d %s",
					Long.valueOf(timeout), unit));

		Instant tEnd = Instant.now();

		if(!errors.isEmpty()) {
			// Pick first error and attach all others as suppressed
			Throwable error = errors.firstElement();
			if(errors.size()>1) {
				errors.subList(1, errors.size()).forEach(error::addSuppressed);
			}

			StringBuilder sb = new StringBuilder();
			sb.append("Task execution failed with ").append(errors.size()).append(" errors:");
			for(Throwable t : errors) {
				sb.append('\n').append(t.getMessage());
			}
			throw new AssertionError(sb.toString(), error);
		}

		return Duration.between(tStart, tEnd);
	}

	@Override
	public void close() throws Exception {
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.SECONDS);
	}
}
