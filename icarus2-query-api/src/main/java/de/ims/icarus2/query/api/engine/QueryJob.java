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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import de.ims.icarus2.query.api.iql.IqlQuery;

/**
 * Intermediate class that wraps all the information for executing a corpus search.
 * It's purpose is to start the actual execution using a given {@link ExecutorService}
 *
 *
 * @author Markus Gärtner
 *
 */
public interface QueryJob {

	IqlQuery getSource();

	/**
	 * Executes this job by scheduling workers on the provided {@link ExecutorService}
	 * and returns a controller instance that can be used to monitor progress and interact
	 * with the underlying workers.
	 * <p>
	 * A number of workers up the specified limit will be created and scheduled.
	 */
	JobController execute(ExecutorService executorService, int workerLimit);

	/**
	 * Similar to {@link #execute(ExecutorService, int)} but internally picks a
	 * suitable executor service based on the specified worker limit.
	 */
	default JobController execute(int workerLimit) {
		checkArgument("worker limit must be positive", workerLimit>0);

		final ExecutorService executorService = QueryUtils.createExecutorService(workerLimit);
		return execute(executorService, workerLimit);
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface JobController {

		/** Returns the query the underlying {@link QueryJob} is evaluating. */
		IqlQuery getSource();

		/** Returns the number of still active workers for the underlying jobs. */
		int getActive();

		/** Returns the number of total workers that were started for the underlying job. */
		int getTotal();

		/** Attempts to stop the still active workers, potentially blocking
		 * for an amount of time up the specified timeout. */
		boolean cancel(long timeout, TimeUnit unit) throws InterruptedException;

		/** Fetches a snapshot of the current search statistics. */
		JobStats getStats();

		/** Returns the current status of the job. */
		JobStatus getStatus();

		/** Starts the background process(es). This method immediately returns upon
		 * scheduling involved threads. */
		void start();

		/** Returns {@code true} iff the job has been finished, either by successfully
		 * performing the associated search, getting canceled or by aborting due to errors. */
		boolean isFinished();
	}

	public static enum JobStatus {
		WAITING(false),
		ACTIVE(false),
		CANCELED(true),
		FAILED(true),
		DONE(true),
		;

		private final boolean finished;

		private JobStatus(boolean finished) {
			this.finished = finished;
		}

		public boolean isFinished() { return finished; }
	}

	public static class JobStats {
		//TODO
	}
}
