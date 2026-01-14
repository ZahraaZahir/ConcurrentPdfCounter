package org.example;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class DirectoryScannerUsingThreadPool implements DirectoryScanner {
    private final String directoryPath;
    private final AtomicInteger pdfCount = new AtomicInteger(0);
    private final AtomicInteger pendingTasks = new AtomicInteger(0);
    private final PdfCountLogger logger;

    public DirectoryScannerUsingThreadPool(String path, PdfCountLogger logger) {
        this.directoryPath = path;
        this.logger = logger;
    }

    @Override
    public void countPdfs() {
        BlockingQueue<File> queue = new LinkedBlockingQueue<>();
        try {
            File root = new File(directoryPath);
            if (!root.exists() || !root.isDirectory()) {
                System.err.println("Invalid directory: " + directoryPath);
                return;
            }

            File[] initial = root.listFiles();
            if (initial != null) {
                pendingTasks.set(initial.length);
                for (File f : initial) {
                    queue.offer(f);
                }
            }

            int poolSize = ThreadPoolProvider.getInstance().getThreadCount();
            CountDownLatch workerCompletionLatch = new CountDownLatch(poolSize);
            CountDownLatch allWorkersReadyLatch = new CountDownLatch(poolSize);
            CountDownLatch startLatch = new CountDownLatch(1);

            for (int i = 0; i < poolSize; i++) {
                ThreadPoolWorker w = new ThreadPoolWorker(queue, this, workerCompletionLatch,
                        allWorkersReadyLatch, startLatch, pendingTasks);
                ThreadPoolProvider.getInstance().submitTask(w);
            }

            allWorkersReadyLatch.await();
            startLatch.countDown();
            workerCompletionLatch.await();

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            ThreadPoolProvider.getInstance().shutdown();
        }
    }

    @Override
    public void incrementAndNotify(String threadName) {
        int currentCount = pdfCount.incrementAndGet();
        logger.addLogMessage(String.format("[Printer][ThreadPool][%s] PDF count: %d", threadName, currentCount));
    }

    @Override
    public int getPdfCount() {
        return pdfCount.get();
    }
}