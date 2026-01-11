package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class PdfCountLoggerTest {

    @Test
    void should_StartWithZero_When_ScannerIsInitialized() {
        Object monitor = new Object();
        PdfCountLogger logger = new PdfCountLogger(monitor);
        DirectoryScannerUsingThreadPool scanner = new DirectoryScannerUsingThreadPool("path", logger);

        assertEquals(0, scanner.getPdfCount());
    }

    @Test
    void should_IncrementCountByOne_When_NotifyIsCalledOnce() {
        Object monitor = new Object();
        PdfCountLogger logger = new PdfCountLogger(monitor);
        DirectoryScannerUsingThreadPool scanner = new DirectoryScannerUsingThreadPool("path", logger);

        scanner.incrementAndNotify("TestThread");

        assertEquals(1, scanner.getPdfCount());
    }
}