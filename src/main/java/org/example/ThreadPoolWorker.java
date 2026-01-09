package org.example;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ThreadPoolWorker implements Runnable {
    private static final long POLL_TIMEOUT_MS = 100;
    private final BlockingQueue<File> queue;
    private final DirectoryScannerUsingThreadPool scanner;
    private final CountDownLatch workerCompletionLatch;
    private final CountDownLatch allWorkersReadyLatch;
    private final CountDownLatch startLatch;

    public ThreadPoolWorker(BlockingQueue<File> queue,
                            DirectoryScannerUsingThreadPool scanner,
                            CountDownLatch workerCompletionLatch,
                            CountDownLatch allWorkersReadyLatch,
                            CountDownLatch startLatch) {
        this.queue = queue;
        this.scanner = scanner;
        this.workerCompletionLatch = workerCompletionLatch;
        this.allWorkersReadyLatch = allWorkersReadyLatch;
        this.startLatch = startLatch;
    }

    @Override
    public void run() {
        try {
            allWorkersReadyLatch.countDown();
            startLatch.await();

            while (!Thread.currentThread().isInterrupted()) {
                File file = queue.poll(POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);

                if (file == null) {
                    break;
                }

                if (Thread.currentThread().isInterrupted()) {
                    break;
                }

                if (file.isDirectory()) {
                    File[] children = file.listFiles();
                    if (children != null) {
                        for (File c : children) {
                            if (Thread.currentThread().isInterrupted()) break;
                            queue.offer(c);
                        }
                    }
                } else if (file.getName().toLowerCase().endsWith(".pdf")) {
                    scanner.incrementAndNotify(Thread.currentThread().getName());
                }
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            System.err.println("ThreadPoolWorker " + Thread.currentThread().getName() + " was interrupted.");
        } catch (Exception e) {
            System.err.println("Error in ThreadPoolWorker " +
                    Thread.currentThread().getName() + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            workerCompletionLatch.countDown();
        }
    }
}
