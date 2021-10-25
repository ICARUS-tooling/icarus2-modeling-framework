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

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * Skeleton class for implementing a worker that is associated with a
 * {@link DefaultJobController} instance for monitoring. This implementation
 * does all the bookkeeping work and uses an instance of {@link Task} to
 * delegate the actual matcher work. This task should occasionally call
 * {@link QueryWorker#isCanceled()} to see if they need to bail, preferably
 * on the beginning of a new iteration pass, the start of a processing batch
 * or similar "clean" point during operation.
 *
 * @author Markus Gärtner
 *
 */
public final class QueryWorker implements Runnable {

	/**
	 * Interface for delegating the actual matcher work.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface Task {
		/** Called once during a task lifecycle to perform the actual matcher work. */
		void execute(QueryWorker worker) throws InterruptedException;

		/** Method called when the worker is shutting down, either due to natural causes,
		 * a crash or by being canceled. Implementation can use this as a central place for
		 * performing final maintenance work and cleaning up buffers/references.*/
		void cleanup(QueryWorker worker);
	}

	private final DefaultJobController controller;
	private ThreadVerifier threadVerifier;

	private final String id;
	private final Task task;
	private final Map<String, Object> clientData = new Object2ObjectOpenHashMap<>();

	private volatile boolean canceled = false;

	/** Package-private constructor so that only tests or the {@link DefaultJobController}
	 * can instantiate. */
	QueryWorker(String id, DefaultJobController controller, Task task) {
		this.id = requireNonNull(id);
		this.controller = requireNonNull(controller);
		this.task = requireNonNull(task);
	}

	/** Set the 'canceled' flag to true and informa the controller on the first call of this method. */
	public final void cancel() {
		if(!canceled) {
			canceled = true;
			controller.workerCanceled(this);
		}
	}

	/** Returns the 'canceled' flag. */
	public boolean isCanceled() { return canceled; }

	public ThreadVerifier getThreadVerifier() {
		checkState("No thread verifier assigned", threadVerifier!=null);
		return threadVerifier;
	}

	public DefaultJobController getController() { return controller; }

	public Thread getThread() { return threadVerifier.getThread(); }

	public void putClientData(String key, Object value) {
		clientData.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T removeClientData(String key) {
		return (T) clientData.remove(key);
	}

	@SuppressWarnings("unchecked")
	public <T> T getClientData(String key) {
		return (T) clientData.get(key);
	}

	@Override
	public final void run() {
		// Mark this thread as the only one we are allowed to operate on
		threadVerifier = ThreadVerifier.forCurrentThread(id);

		controller.workerStarted(this);

		try {
			task.execute(this);

			if(!canceled) {
				controller.workerDone(this);
			}
		} catch(InterruptedException e) {
			// Possible through hard cancellation from the executor service
			cancel();
		} catch(Throwable e) {
			controller.workerFailed(this, e);
		} finally {
			try {
				// Let task perform internal cleanup
				task.cleanup(this);
			} finally {
				// Ensure we do NOT hold any references to external data
				clientData.clear();
			}
		}
	}
}