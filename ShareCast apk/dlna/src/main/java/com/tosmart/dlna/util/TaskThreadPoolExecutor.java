package com.tosmart.dlna.util;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @date 2019/5/6
 */
public class TaskThreadPoolExecutor {
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 10;
    private static final long KEEP_ALIVE_TIME = 1L;
    private static final int MAX_WORK_QUEUE_SIZE = 16;
    private static final BlockingQueue<Runnable> WORK_QUEUE = new ArrayBlockingQueue<>(MAX_WORK_QUEUE_SIZE);

    private ExecutorService mExecutorService;
    private static TaskThreadPoolExecutor sInstance;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private TaskThreadPoolExecutor() {
        mExecutorService = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.MINUTES, WORK_QUEUE, new CustomThreadFactory());
    }

    public static TaskThreadPoolExecutor getInstance() {
        if (sInstance == null) {
            synchronized (TaskThreadPoolExecutor.class) {
                if (sInstance == null) {
                    sInstance = new TaskThreadPoolExecutor();
                }
            }
        }
        return sInstance;
    }

    public void execute(Runnable runnable) {
        mExecutorService.execute(runnable);
    }

    public void executeInMainThread(Runnable runnable) {
        if (!Looper.getMainLooper().equals(Looper.myLooper())) {
            mHandler.post(runnable);
        } else {
            runnable.run();
        }
    }

    private static class CustomThreadFactory implements ThreadFactory {
        private AtomicInteger mAtomicInteger = new AtomicInteger(0);

        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            Thread thread = new Thread(runnable);
            String threadName = TaskThreadPoolExecutor.class.getSimpleName() + mAtomicInteger.incrementAndGet();
            thread.setName(threadName);
            return thread;
        }
    }
}
