/**
 *
 */
package de.ims.icarus2.test.concurrent;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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

	public Circuit task(Executable task) {
		tasks.add(requireNonNull(task));
		return this;
	}

	public synchronized void execute() throws InterruptedException {
		execute(0, TimeUnit.MILLISECONDS);
	}

	/**
	 * Schedules all the previously supplied tasks and awaits their
	 * completion or until the given {@code timeout} has elapsed.
	 */
	public synchronized Duration execute(long timeout, TimeUnit unit) throws InterruptedException {
		if(tasks.isEmpty())
			throw new IllegalStateException("No tasks to execute");

		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(tasks.size());

		Vector<Throwable> errors = new Vector<>();

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
		done.await(timeout, unit);

		Instant tEnd = Instant.now();

		if(!errors.isEmpty()) {
			// Pick first error and attach all others as suppressed
			Throwable error = errors.firstElement();
			if(errors.size()>1) {
				errors.subList(1, errors.size()).forEach(error::addSuppressed);
			}
			throw new AssertionError("Task execution failed", error);
		}

		return Duration.between(tStart, tEnd);
	}

	@Override
	public void close() throws Exception {
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.SECONDS);
	}
}
