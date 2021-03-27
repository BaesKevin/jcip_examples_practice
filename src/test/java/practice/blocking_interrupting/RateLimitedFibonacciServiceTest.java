package practice.blocking_interrupting;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class RateLimitedFibonacciServiceTest {

    @Test
    public void shouldReturnNthFibonacciNumber() throws InterruptedException {
        FibonacciService wrappedService = new UncachedFibonacciService();
        FibonacciService rateLimitedService = new RateLimitedFibonacciService(wrappedService, 1);

        BigDecimal nthFibonacciNumber = rateLimitedService.getNthFibonacciNumber(10);

        assertThat(nthFibonacciNumber).isEqualTo(new BigDecimal("34"));
    }

    @Test
    public void shouldRespectAllowedRequests() throws InterruptedException {
        int sleepingPill = -1;
        FibonacciService wrappedService = new StubFibonacciService(sleepingPill);
        FibonacciService rateLimitedService = new RateLimitedFibonacciService(wrappedService, 5);

        // submit max allowed number of tasks, all of which are sleeping
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        for (int i = 0; i < 5; i++) {
            executorService.submit(() -> rateLimitedService.getNthFibonacciNumber(sleepingPill)); // this will block, so do it in another thread
        }
        // submit N+1st task with should block
        Future<?> taskToInterrupt = executorService.submit(() -> {
            try {
                rateLimitedService.getNthFibonacciNumber(1);
                fail("getNthFibonacciNumber returned normally while it should have blocked");
            } catch (InterruptedException e) {
                // success, interrupted
            }
        });

        assertFutureTimesOut(taskToInterrupt, 500, MILLISECONDS);

        executorService.shutdownNow();
        executorService.awaitTermination(500, MILLISECONDS);
        assertThat(executorService.isShutdown());
    }

    @Test
    public void blockedTasksProgress() throws InterruptedException {
        int sleepingPill = -1;
        FibonacciService wrappedService = new StubFibonacciService(sleepingPill);
        FibonacciService rateLimitedService = new RateLimitedFibonacciService(wrappedService, 5);

        // submit max allowed number of tasks, all of which are sleeping
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        List<Future<BigDecimal>> sleepers = IntStream.range(0, 5)
                .mapToObj(i -> executorService.submit(() -> rateLimitedService.getNthFibonacciNumber(sleepingPill)))
                .collect(toList());
        // submit N+1st task with should block
        Future<?> blockedTask = executorService.submit(() -> {
            try {
                rateLimitedService.getNthFibonacciNumber(1);
            } catch (InterruptedException e) {
                fail("getNthFibonacciNumber returned normally while it should have blocked");
            }
        });

        sleepers.forEach(future -> future.cancel(true));

        assertFutureCompletesSuccesfully(blockedTask, 500, MILLISECONDS);

        executorService.shutdownNow();
        executorService.awaitTermination(500, MILLISECONDS);
        assertThat(executorService.isShutdown());
    }

    private void assertFutureTimesOut(Future<?> taskToInterrupt, int timeout, TimeUnit timeUnit) {
        timeoutHelper(taskToInterrupt, timeout, timeUnit, true);
    }

    private void assertFutureCompletesSuccesfully(Future<?> taskToInterrupt, int timeout, TimeUnit timeUnit) {
        timeoutHelper(taskToInterrupt, timeout, timeUnit, false);
    }

    private void timeoutHelper(Future<?> taskToInterrupt, int timeout, TimeUnit timeUnit, boolean shouldTimeOut) {
        boolean timedOut = false;
        try {
            taskToInterrupt.get(timeout, timeUnit);
        } catch (ExecutionException e) {
            fail(e.getMessage());
        } catch (TimeoutException e) {
            // success
            timedOut = true;
        } catch (InterruptedException e) {
            fail("unexpected interrupt while waiting for task");
        }

        assertThat(timedOut).isEqualTo(shouldTimeOut);
    }

    static class StubFibonacciService implements FibonacciService {
        private final int sleepingPill;

        public StubFibonacciService(int sleepingPill) {
            this.sleepingPill = sleepingPill;
        }

        @Override
        public BigDecimal getNthFibonacciNumber(int n) throws InterruptedException {
            if (n == sleepingPill) {
                Thread.sleep(2000);
            }
            return BigDecimal.ZERO;
        }
    }
}