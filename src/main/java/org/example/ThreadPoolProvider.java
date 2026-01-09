package org.example;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadPoolProvider {
    private static final int THREAD_COUNT = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
    private static ThreadPoolProvider instance;
    private final ExecutorService executor;
    private static final ReentrantLock lock = new ReentrantLock();

    private ThreadPoolProvider() {
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        this.executor = new ThreadPoolExecutor(
                THREAD_COUNT,
                THREAD_COUNT,
                0L,
                TimeUnit.MILLISECONDS,
                workQueue
        );
    }

    public static ThreadPoolProvider getInstance() {
        if (instance == null) {
            lock.lock();
            try {
                if (instance == null) {
                    instance = new ThreadPoolProvider();
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }

    public void submitTask(Runnable task) {
        executor.submit(task);
    }

    public int getThreadCount() {
        return THREAD_COUNT;
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("ThreadPoolProvider: Pool did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}