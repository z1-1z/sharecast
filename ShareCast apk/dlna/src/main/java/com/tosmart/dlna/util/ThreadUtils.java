package com.tosmart.dlna.util;

import android.app.Application;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author pc-001
 */
public class ThreadUtils {
    private static final String TAG = "ThreadUtils";
    public static final int CORE_POOL_SIZE = 2;
    public static final int MAXIMUM_POOL_SIZE = 5;
    public static final int KEEP_ALIVE_TIME = 1000;
    /**
     * 创建线程池
     */
    private static ExecutorService executor = null;

    public synchronized static void prepare() {
        if (executor == null || executor.isShutdown()) {
            executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                    KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(), (ThreadFactory) Thread::new);

        }
    }

    /**
     * 在{@link Application#onTerminate}的时候销毁线程池
     */
    public synchronized static void shutdown() {
        if (executor != null) {
            if (!executor.isShutdown()) {
                executor.shutdown();
            }
            executor = null;
        }
    }

    /**
     * 执行Runnable形式的任务
     *
     * @param task 需要执行的任务
     */
    public static void execute(Runnable task) {
        executor.execute(task);
    }

    /**
     * 执行Callable形式的任务
     *
     * @param task 需要执行的任务
     * @return 任务执行结束返回结果信息
     */
    public static Future<?> submit(Callable<?> task) {
        return executor.submit(task);
    }

}
