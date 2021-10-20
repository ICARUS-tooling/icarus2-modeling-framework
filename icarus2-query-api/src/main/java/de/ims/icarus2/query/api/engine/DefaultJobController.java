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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.engine.QueryJob.JobController;
import de.ims.icarus2.query.api.engine.QueryJob.JobStats;
import de.ims.icarus2.query.api.engine.QueryJob.JobStatus;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.AccumulatingException;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;

/**
 * Implements a {@link JobController} that is linked to a {@link QueryJob}
 * and manages a set of {@link QueryWorker} instances.
 * <p>
 * Workers should be created using the {@link #createWorker(String, de.ims.icarus2.query.api.engine.QueryWorker.Task)}
 * method, supplying a {@link QueryWorker.Task} implementation for each worker
 * to delegate the actual matcher work to.
 *
 *
 * @author Markus Gärtner
 *
 */
public class DefaultJobController implements JobController {

	public static Builder builder() { return new Builder(); }

	private final QueryJob job;
	private final ExecutorService executorService;
	private final QueryOutput resultProcessor;

	private final AtomicInteger total = new AtomicInteger();
	private final AtomicInteger active = new AtomicInteger();
	private final AtomicReference<JobStatus> status = new AtomicReference<>(JobStatus.WAITING);
	private final AccumulatingException.Buffer exceptionBuffer = new AccumulatingException.Buffer();

	private final ReferenceSet<QueryWorker> workers = new ReferenceOpenHashSet<>();
	private final Object lock = new Object();

	private DefaultJobController(Builder builder) {
		//TODO
	}

	public QueryJob getJob() { return job; }

	@Override
	public IqlQuery getSource() { return job.getSource(); }

	@Override
	public QueryOutput getResultProcessor() { return resultProcessor; }

	@Override
	public int getActive() { return active.get(); }

	@Override
	public int getTotal() { return total.get(); }

	/**
	 * Cancel all underlying
	 * @see de.ims.icarus2.query.api.engine.QueryJob.JobController#cancel(long, java.util.concurrent.TimeUnit)
	 *
	 * @throws IllegalStateException if the job is already finished or the executor service
	 * has already initiated shutdown.
	 */
	@Override
	public boolean cancel(long timeout, TimeUnit unit) throws InterruptedException {
		checkState("Job is already finished", !getStatus().isFinished());
		checkState("Executor service already shutdown", !executorService.isShutdown());

		if(executorService.isTerminated()) {
			return true;
		}

		workers.forEach(QueryWorker::cancel);

		// No timeout, so we need to try and shutdown immediately
		if(timeout==0) {
			List<Runnable> pendingWorkers = executorService.shutdownNow();
			// Make sure we at least (double) cancel all the still pending workers
			for(Runnable r : pendingWorkers) {
				assert r instanceof QueryWorker : "Unsupported worker class: "+r.getClass();
				((QueryWorker)r).cancel();
			}
			return true;
		}

		// Give workers time for orderly shutdown
		executorService.shutdown();
		return executorService.awaitTermination(timeout, unit);
	}

	@Override
	public JobStats getStats() {
		// TODO when JobStats class is finished, actually create a result here
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public JobStatus getStatus() { return status.get(); }

	/** Returns {@code true} iff the current status is a {@link JobStatus#isFinished() finisher}
	 * and no jobs are active anymore. */
	@Override
	public boolean isFinished() {
		return getStatus().isFinished() && getActive()==0;
	}

	/**
	 * Creates a new {@link QueryWorker} that is linked to this controller.
	 * Note that this method does <b>not</b> actually start the worker, but only
	 * stores it! To start all workers at once, use the {@link #start()} method
	 * after they all have been created using this method.
	 */
	public QueryWorker createWorker(String id, QueryWorker.Task task) {
		if(status.get()!=JobStatus.WAITING)
			throw new QueryException(GlobalErrorCode.ILLEGAL_STATE,
					"Cannot add workers if job has already been started");

		QueryWorker worker = new QueryWorker(id, this, task);
		workerCreated(worker);
		return worker;
	}

	/**
	 * @see de.ims.icarus2.query.api.engine.QueryJob.JobController#start()
	 */
	@Override
	public void start() {
		// Make sure we only ever get executed once
		if(!status.compareAndSet(JobStatus.WAITING, JobStatus.ACTIVE))
			throw new QueryException(QueryErrorCode.RECYCLED_JOB, "Query job already started");

		// Submit all workers for execution and then return
		for(QueryWorker worker : workers) {
			executorService.execute(worker);
		}
	}

	// methods called by the worker threads

	void workerCreated(QueryWorker worker) {
		synchronized (lock) {
			if(!workers.add(requireNonNull(worker)))
				throw new QueryException(GlobalErrorCode.INTERNAL_ERROR,
						"Worker already present: "+worker);
			total.incrementAndGet();
		}
	}
	void workerStarted() { active.incrementAndGet(); }
	void workerCanceled() {
		if(active.decrementAndGet() == 0) {
			status.compareAndSet(JobStatus.ACTIVE, JobStatus.CANCELED);
		}
	}
	void workerFailed(Exception e) {
		// Unconditionally set status to FAILED (this way subsequent cancellations won't overwrite it)
		status.set(JobStatus.FAILED);
		synchronized (lock) {
			active.decrementAndGet();
			exceptionBuffer.addException(e);
			workers.forEach(QueryWorker::cancel);
		}
	}
	void workerDone() {
		if(active.decrementAndGet() == 0) {
			status.compareAndSet(JobStatus.ACTIVE, JobStatus.DONE);
		}
	}

	public static class Builder extends AbstractBuilder<Builder, DefaultJobController> {

		private Builder() { /* no-op */ }

		@Override
		protected DefaultJobController create() {
			// TODO Auto-generated method stub
			return null;
		}

	}
}