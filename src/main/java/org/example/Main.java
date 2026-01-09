package org.example;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Main {
    public static void main(String... args) {
        Scanner input = new Scanner(System.in);
        System.out.print("Please provide a PATH to a directory: ");
        String path = input.nextLine();

        Object sharedLogMonitor = new Object();
        PdfCountLogger logger = new PdfCountLogger(sharedLogMonitor);
        Thread loggerThread = new Thread(logger, "Logger");
        loggerThread.start();

        CountDownLatch mainLatch = new CountDownLatch(1);

        DirectoryScannerUsingThreadPool pool = new DirectoryScannerUsingThreadPool(path, logger, sharedLogMonitor);

        new Thread(() -> {
            try {
                pool.countPdfs();
            } catch (Exception e) {
                System.err.println("Error in thread-pool scanner: " + e.getMessage());
                e.printStackTrace();
            } finally {
                mainLatch.countDown();
            }
        }, "Pool-Scanner").start();

        try {
            mainLatch.await();
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Main thread interrupted while waiting for scanner or logger.");
        }

        System.out.println("Final PDF count (Thread Pool): " + pool.getPdfCount());

        loggerThread.interrupt();
        try {
            loggerThread.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Main thread interrupted while waiting for logger thread to join.");
        }
        System.out.println("Main thread exiting.");
    }
}