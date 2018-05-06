package com.lbsphoto.app.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lbsphoto
 */
public class ThreadCenter {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 1;

    private ThreadCenter(){}
    private static ThreadCenter instance;
    private ThreadPoolExecutor threadPoolExecutor;
    public static ThreadCenter getInstance() {
        if (instance == null) {
            instance = new ThreadCenter();
        }
        return instance;
    }

    private void init() {
        if (threadPoolExecutor == null) {
            threadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE,
                    MAXIMUM_POOL_SIZE,
                    KEEP_ALIVE,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    THREAD_FACTORY);
        }
    }

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger m_Count = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + m_Count.getAndIncrement());
        }
    };

    public void excuteThread(Runnable runnable) {
        init();
        threadPoolExecutor.execute(runnable);
    }
}
