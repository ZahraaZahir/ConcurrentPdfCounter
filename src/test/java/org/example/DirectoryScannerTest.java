package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DirectoryScannerTest {

    @AfterEach
    void tearDown() {
        ThreadPoolProvider.getInstance().shutdown();
    }

    @Test
    void should_FindTwoPdfs_When_DirectoryHasMixedFiles(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("file1.pdf"));
        Files.createFile(tempDir.resolve("file2.pdf"));
        Files.createFile(tempDir.resolve("ignore.txt"));

        Object monitor = new Object();
        PdfCountLogger logger = new PdfCountLogger(monitor);
        DirectoryScannerUsingThreadPool scanner = new DirectoryScannerUsingThreadPool(
                tempDir.toString(), logger, monitor);

        scanner.countPdfs();

        assertEquals(2, scanner.getPdfCount(), "Should count exactly 2 PDF files");
    }

    @Test
    void should_ReturnZero_When_DirectoryIsEmpty(@TempDir Path tempDir) {
        Object monitor = new Object();
        PdfCountLogger logger = new PdfCountLogger(monitor);
        DirectoryScannerUsingThreadPool scanner = new DirectoryScannerUsingThreadPool(
                tempDir.toString(), logger, monitor);

        scanner.countPdfs();

        assertEquals(0, scanner.getPdfCount(), "Empty directory should result in 0 count");
    }
}