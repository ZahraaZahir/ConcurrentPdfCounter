package org.example;

import java.util.LinkedList;
import java.util.Queue;

public class PdfCountLogger implements Runnable {
    private final Queue<String> messageQueue = new LinkedList<>();
    private final Object queueMonitor;

    public PdfCountLogger(Object monitor) {
        this.queueMonitor = monitor;
    }

    public void addLogMessage(String msg) {
        synchronized (queueMonitor) {
            messageQueue.offer(msg);
            queueMonitor.notify();
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String msg;
                synchronized (queueMonitor) {
                    while (messageQueue.isEmpty() && !Thread.currentThread().isInterrupted()) {
                        queueMonitor.wait();
                    }

                    if (Thread.currentThread().isInterrupted() && messageQueue.isEmpty()) {
                        break;
                    }
                    msg = messageQueue.poll();
                }

                if (msg != null) {
                    System.out.println(msg);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            String msg;
            while (true) {
                synchronized (queueMonitor) {
                    msg = messageQueue.poll();
                }
                if (msg != null) {
                    System.out.println(msg);
                } else {
                    break;
                }
            }
            System.out.println("Logger thread exiting.");
        }
    }
}
