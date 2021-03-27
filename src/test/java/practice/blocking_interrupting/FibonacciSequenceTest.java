package practice.blocking_interrupting;

import org.junit.Test;
import practice.threadsafety.FibonacciSequence;
import practice.util.NamedThreadFactory;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static practice.util.Uninterruptable.awaitLatch;

public class FibonacciSequenceTest {

    @Test
    public void getFirst10FibonacciNumbers() {
        FibonacciSequence fibonacciSequence = new FibonacciSequence();

        long[] expectedFibs = {0, 1, 1, 2, 3, 5, 8, 13, 21, 34};
        long[] actualFibs = IntStream.range(0, 10).mapToLong(i -> fibonacciSequence.getNext().longValue()).toArray();

        assertThat(actualFibs).hasSize(10).containsExactly(expectedFibs);
    }

    @Test
    public void get101thFibonacciNumber() {
        FibonacciSequence sequence = new FibonacciSequence();

        BigDecimal actual101thFib = IntStream.range(0, 101)
                .mapToObj(fib -> sequence.getNext())
                .collect(toList())
                .get(100);

        BigDecimal fib100 = new BigDecimal("354224848179261915075");
        assertThat(actual101thFib).isEqualTo(fib100);
    }

    // sequence is thread safe if concurrent calls to getNext correctly update the sequence
    // this test calls getNext 10 times from 10 threads, and then validates that the next number
    // is the 101st fibonacci number
    @Test
    public void get100thFibonaciNumberConcurrent() {
        int testCount = 1000;
        int workers = 10;
        int fibonacciPerWorker = 10;

        for (int i = 0; i < testCount; i++) {
            CountDownLatch startLatch = new CountDownLatch(1);
            FibonacciSequence sequence = new FibonacciSequence();

            ExecutorService sequenceService = Executors.newFixedThreadPool(workers, new NamedThreadFactory("fibonacci-pool"));

            for (int worker = 0; worker < workers; worker++) {
                sequenceService.submit(() -> {
                    awaitLatch(startLatch);
                    for (int j = 0; j < fibonacciPerWorker; j++) {
                        sequence.getNext();
                    }
                });
            }

            try {
                startLatch.countDown();

                sequenceService.shutdown();
                boolean terminated = sequenceService.awaitTermination(1, TimeUnit.SECONDS);
                assertThat(terminated).isTrue();

                BigDecimal fib101 = new BigDecimal("354224848179261915075");
                assertThat(sequence.getNext()).isEqualTo(fib101);
            } catch (InterruptedException e) {
                fail("unexpected interrupt waiting for service to terminate");
            }

        }
    }
}