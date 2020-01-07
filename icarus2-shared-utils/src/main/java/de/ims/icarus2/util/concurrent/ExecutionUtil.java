/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutionUtil {

	private static final Logger log = LoggerFactory.getLogger(ExecutionUtil.class);

	private static ExecutorService executorService;

	private static RejectedExecutionHandler rejectedExecutionHandler;
	static {
		rejectedExecutionHandler = null;
		//TODO if we ever need to implement a default policy for rejected executions, add it here
	}

	private static synchronized ExecutorService getExecutorService() {

		if (executorService == null) {
			// this creates daemon threads.
			ThreadFactory threadFactory = new ThreadFactory() {
				final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

				@Override
				public Thread newThread(final Runnable r) {
					Thread thread = defaultFactory.newThread(r);
					thread.setName("ICARUS2-worker-" + thread.getName()); //$NON-NLS-1$
					thread.setDaemon(true);
					return thread;
				}
			};

			int maxThreadCount = Math.max(1, Runtime.getRuntime().availableProcessors()-1);

			RejectedExecutionHandler handler = rejectedExecutionHandler;
			if(handler==null) {
				handler = new ThreadPoolExecutor.AbortPolicy();
			}

			executorService = new ThreadPoolExecutor(1,
					maxThreadCount, 10L, TimeUnit.MINUTES,
					new LinkedBlockingQueue<Runnable>(), threadFactory,
					handler);

			// Ensure that in case of a proper VM shutdown we also close the executor service
			Runnable shutdownTask = () -> {
				try {
					close();
				} catch (InterruptedException e) {
					log.error("Failed to shutdown executor service", e);
				}
			};
			Runtime.getRuntime().addShutdownHook(
					new Thread(shutdownTask, "ICARUS2-executor-shutdown"));
		}
		return executorService;
	}

	public static void execute(Runnable r) {
		getExecutorService().execute(r);
	}

	public static Future<?> submit(Runnable task) {
		return getExecutorService().submit(task);
	}

	public static <T extends Object> Future<T> submit(Callable<T> task) {
		return getExecutorService().submit(task);
	}

	public static void close() throws InterruptedException {
		if(executorService==null) {
			return;
		}

		executorService.shutdownNow();

		executorService.awaitTermination(5, TimeUnit.SECONDS);
	}
}
