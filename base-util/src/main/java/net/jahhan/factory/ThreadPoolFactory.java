package net.jahhan.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import net.jahhan.constant.enumeration.ThreadPoolEnum;

/**
 * 线程池
 * 
 * @author nince
 */
@Singleton
public final class ThreadPoolFactory {

	private static final Map<ThreadPoolEnum, ExecutorService> threadPoolMap = new HashMap<ThreadPoolEnum, ExecutorService>(
			2, 1);

	static {
		final ScheduledExecutorService newScheduledThreadPool = Executors.newScheduledThreadPool(4);
		threadPoolMap.put(ThreadPoolEnum.SCHEDULED, newScheduledThreadPool);

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(20);
		threadPoolMap.put(ThreadPoolEnum.FIXED, newFixedThreadPool);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				newScheduledThreadPool.shutdown();
				newFixedThreadPool.shutdown();
				try {
					if (!newScheduledThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
						newScheduledThreadPool.shutdownNow();
					}
					if (!newFixedThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
						newFixedThreadPool.shutdownNow();
					}
				} catch (Exception e) {
					Thread.currentThread().interrupt();
				}
			}
		});
	}

	public ExecutorService getExecuteService(ThreadPoolEnum threadPoolEnum) {
		return threadPoolMap.get(threadPoolEnum);
	}
}
