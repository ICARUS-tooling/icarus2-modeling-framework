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

public class ExecutionUtil {


	private static ExecutorService executorService;

	private static RejectedExecutionHandler rejectedExecutionHandler;

	private static synchronized ExecutorService getExecutorService() {

		if (executorService == null) {
			// this creates daemon threads.
			ThreadFactory threadFactory = new ThreadFactory() {
				final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

				@Override
				public Thread newThread(final Runnable r) {
					Thread thread = defaultFactory.newThread(r);
					thread.setName("TaskManager-" + thread.getName()); //$NON-NLS-1$
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

	public static void close() throws Exception {
		if(executorService==null) {
			return;
		}

		executorService.shutdownNow();

		executorService.awaitTermination(5, TimeUnit.SECONDS);
	}
}
