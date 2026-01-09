# ConcurrentPdfCounter

A high-performance Java command-line tool designed to scan directories and count PDF files using a custom thread-pool architecture.

## Features

- **Multithreaded Scanning:** Utilizes a `ThreadPoolExecutor` to traverse directories concurrently.
- **Decoupled Logging:** Uses a dedicated `PdfCountLogger` thread to ensure console I/O does not block worker performance.
- **Thread-Safe Design:** Implements the Monitor Pattern and `CountDownLatch` for robust synchronization across worker threads.
- **Dynamic Scaling:** Automatically scales the thread pool size based on the host system's available CPU cores.

## Architecture

- **`DirectoryScanner`**: Interface defining the contract for counting logic.
- **`ThreadPoolProvider`**: A thread-safe Singleton managing the lifecycle of the `ExecutorService`.
- **`ThreadPoolWorker`**: Implements the scanning logic, using a `BlockingQueue` to handle directory traversal.
- **`PdfCountLogger`**: A consumer thread that processes log messages asynchronously from a message queue.
