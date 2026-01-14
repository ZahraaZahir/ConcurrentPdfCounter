package org.example;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolWorker implements Runnable {
    private final BlockingQueue<File> queue;
    private final DirectoryScannerUsingThreadPool scanner;
    private final CountDownLatch workerCompletionLatch;
    private final CountDownLatch allWorkersReadyLatch;
    private final CountDownLatch startLatch;
    private final AtomicInteger pendingTasks;

    public ThreadPoolWorker(BlockingQueue<File> queue,
            DirectoryScannerUsingThreadPool scanner,
            CountDownLatch workerCompletionLatch,
            CountDownLatch allWorkersReadyLatch,
            CountDownLatch startLatch,
            AtomicInteger pendingTasks) {
        this.queue = queue;
        this.scanner = scanner;
        this.workerCompletionLatch = workerCompletionLatch;
        this.allWorkersReadyLatch = allWorkersReadyLatch;
        this.startLatch = startLatch;
        this.pendingTasks = pendingTasks;
    }

    @Override
    public void run() {
        try {
            allWorkersReadyLatch.countDown();
            startLatch.await();

            while (pendingTasks.get() > 0 && !Thread.currentThread().isInterrupted()) {
                File file = queue.poll(10, TimeUnit.MILLISECONDS);

                if (file == null)
                    continue;

                try {
                    if (file.isDirectory()) {
                        File[] children = file.listFiles();
                        if (children != null) {
                            pendingTasks.addAndGet(children.length);
                            for (File c : children) {
                                queue.offer(c);
                            }
                        }
                    } else if (file.getName().toLowerCase().endsWith(".pdf")) {
                        scanner.incrementAndNotify(Thread.currentThread().getName());
                    }
                } finally {
                    pendingTasks.decrementAndGet();
                }
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            workerCompletionLatch.countDown();
        }
    }
}