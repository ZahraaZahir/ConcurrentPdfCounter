package org.example;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class DirectoryScannerUsingThreadPool implements DirectoryScanner {
    private final String directoryPath;
    private final AtomicInteger pdfCount = new AtomicInteger(0);
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
                System.err.println("Invalid directory for ThreadPool: " + directoryPath);
                return;
            }

            File[] initial = root.listFiles();
            if (initial != null) {
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
                        allWorkersReadyLatch, startLatch);
                ThreadPoolProvider.getInstance().submitTask(w);
            }

            allWorkersReadyLatch.await();
            startLatch.countDown();

            workerCompletionLatch.await();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            System.err.println("Thread-pool scan interrupted in countPdfs.");
        } catch (Exception e) {
            System.err.println("Error in ThreadPool countPdfs: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ThreadPoolProvider.getInstance().shutdown();
        }
    }

    @Override
    public void incrementAndNotify(String threadName) {
        int currentCount = pdfCount.incrementAndGet();
        String logMsg = String.format("[Printer][%s][%s] PDF count: %d",
                "ThreadPool", threadName, currentCount);
        logger.addLogMessage(logMsg);
    }

    @Override
    public int getPdfCount() {
        return pdfCount.get();
    }
}
