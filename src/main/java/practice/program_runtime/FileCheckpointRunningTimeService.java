package practice.program_runtime;

import practice.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import static practice.util.FileUtils.readLastLine;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Tracks program total run time. The granularity of the counter is 1 second.
 * The count is preserved between runs. The service attempts to update the counter on JVM shutdown.
 * The service writes temporary state to disk so that, in the event of a SIGKILL, it can keep counting.
 * <p>
 * This class is thread-safe: multiple threads may initialize and use the class.
 */
public class FileCheckpointRunningTimeService implements ProgramRunTimeService{
    public static final String PROGRAM_RUNTIME_TXT = "program_runtime.txt";
    private static final Object fileCreationLock = new Object();
    private static final long timeoutMs = 1000;

    private final ScheduledExecutorService scheduledExecutorService;
    private final AtomicLong timeRunning = new AtomicLong();
    private final Path uptimeFilePath;
    private boolean shutdownHookAdded = false;

    // todo make singleton version of this to avoid initialization synchronization issues
    public FileCheckpointRunningTimeService() {
        uptimeFilePath = Paths.get(FileUtils.TMP_DIR, PROGRAM_RUNTIME_TXT);
        scheduledExecutorService = newScheduledThreadPool(1);
        createTmpFileIfNotExists();
        registerShutdownHook();
        timeRunning.set(readLastCount());
    }

    private synchronized void registerShutdownHook() {
        if (!shutdownHookAdded) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("shutdown detected, stopping counter");
                stopNow();
            }));
            shutdownHookAdded = true;
        }
    }

    public void start() {
        scheduledExecutorService.scheduleAtFixedRate(this::countAndWrite, 1, 1, SECONDS);
    }

    public void stop() throws InterruptedException {
        scheduledExecutorService.shutdown();
        scheduledExecutorService.awaitTermination(timeoutMs, MILLISECONDS);
    }

    public void stopNow() {
        scheduledExecutorService.shutdownNow();
    }

    private void countAndWrite() {
        long newCount = timeRunning.incrementAndGet();
        FileUtils.tryWrite(uptimeFilePath, String.format("%d%n", newCount));
    }

    public long getTimeRunning() {
        return timeRunning.get();
    }

    // synchronized would not be thread-safe, synchronized is on the instance
    private void createTmpFileIfNotExists() {
        try {
            // avoid doing I/O with a lock
            synchronized (fileCreationLock) {
                if (Files.exists(uptimeFilePath)) {
                    return;
                }
            }

            Files.createFile(uptimeFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized int readLastCount() {
        return readLastLine(uptimeFilePath).map(Integer::parseInt).orElse(0);
    }

}
