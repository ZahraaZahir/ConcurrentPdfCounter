package org.example;

public interface DirectoryScanner {
    void countPdfs();
    int getPdfCount();
    void incrementAndNotify(String threadName);
}