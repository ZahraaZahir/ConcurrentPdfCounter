package org.example;

import java.util.concurrent.*;
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

/*
 * References:
 * 1- Dr. Yad mentioned in the lectures that there should be one instance of the thread-pool provider for the whole program
 *   That's why we decided to utilize the Singleton Design Pattern
 * 2- We used this tutorial to implement the Singleton DP: https://www.digitalocean.com/community/tutorials/thread-safety-in-java-singleton-classes
 * 3- ChatGPT suggested this implementation of the shutdown() method to ensure graceful shutdown
 * */