package practice.blocking_interrupting;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import practice.util.Uninterruptable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class WriteStringThreadSafetyTest {

    private Path tempFile;

    @Before
    public void setUp() throws Exception {
        tempFile = Files.createTempFile("", "");
    }

    @After
    public void tearDown() throws Exception {
        if (Files.exists(tempFile)) {
            Files.delete(tempFile);
        }
    }

    @Test
    public void synchronousWriteTest() throws IOException {
        int writers = 1000_000;
        StringBuilder expectedText = new StringBuilder();

        for (AtomicInteger i = new AtomicInteger(); i.get() < writers; i.incrementAndGet()) {
            String text = String.format("%d%n", i.get());
            expectedText.append(text);

            try {
                Files.writeString(tempFile, text, StandardOpenOption.APPEND);
            } catch (IOException e) {
                fail("io exception writing [" + text + "]");
            }
        }

        String actualText = String.join(System.lineSeparator(), Files.readAllLines(tempFile));
        assertThat(actualText).isEqualTo(expectedText.toString().trim());
    }

    @Test
    public void writeLinesConcurrentTest() throws IOException, InterruptedException {
        int concurrentWriters = 1000_000;

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(concurrentWriters);
        ExecutorService executor = Executors.newFixedThreadPool(100);

        Path tempFile = Files.createTempFile("", "");
        List<String> writtenStrings = new ArrayList<>();

        for (AtomicInteger i = new AtomicInteger(); i.get() < concurrentWriters; i.incrementAndGet()) {
            String text = String.format("%d%n", i.get());
            writtenStrings.add(text.trim());

            executor.submit(() -> {
                Uninterruptable.awaitLatch(startLatch);
                try {
                    Files.writeString(tempFile, text, StandardOpenOption.APPEND);
                } catch (IOException e) {
                    fail("io exception writing [" + text + "]");
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();
        executor.awaitTermination(100, TimeUnit.MILLISECONDS);

        List<String> lines = Files.readAllLines(tempFile).stream().map(String::trim).collect(toList());
        assertThat(lines).hasSameElementsAs(writtenStrings);
    }

}
