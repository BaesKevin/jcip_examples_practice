package practice.blocking_interrupting;

import org.assertj.core.data.Percentage;
import org.junit.Test;
import practice.util.Uninterruptable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.System.currentTimeMillis;
import static org.assertj.core.api.Assertions.assertThat;


public class UninterruptableTest {
    @Test
    public void uninterruptableWaitCompletesNormallyWithoutInterrupt() throws InterruptedException {
        AtomicBoolean threadInterrupted = new AtomicBoolean(false);
        Thread thread = new Thread(() -> {
            Uninterruptable.sleep(100);
            threadInterrupted.set(Thread.currentThread().isInterrupted());
        });

        long startTime = currentTimeMillis();
        thread.start();
        thread.join(500);
        long stopTime = currentTimeMillis();
        long elapsed = stopTime - startTime;

        assertThat(elapsed).isCloseTo(100, Percentage.withPercentage(10));
        assertThat(threadInterrupted.get()).isFalse();
    }

    @Test
    public void uninterruptableWaitReturnsWhenWaitCompleted() throws InterruptedException {
        Thread thread = new Thread(() -> {
            Uninterruptable.sleep(1000);
        });

        long startTime = currentTimeMillis();
        thread.start();
        thread.interrupt();
        thread.join(2000);
        long stopTime = currentTimeMillis();
        long elapsed = stopTime - startTime;

        assertThat(elapsed).isCloseTo(1000, Percentage.withPercentage(10));
    }

    @Test
    public void uninterruptableWaitTracksWaitedTime() throws InterruptedException {
        Thread thread = new Thread(() -> {
            Uninterruptable.sleep(1000);
        });

        long startTime = currentTimeMillis();
        thread.start();
        // try to interrupt 5 times every 100 ms
        for (int i = 0; i < 5; i++) {
            Thread.sleep(100);
            thread.interrupt();
        }

        thread.join(2000);
        long stopTime = currentTimeMillis();
        long elapsed = stopTime - startTime;

        assertThat(elapsed).isCloseTo(1000, Percentage.withPercentage(10));
    }

    @Test
    public void uninterruptableWaitRetainsInterruptStatus() throws InterruptedException {
        AtomicBoolean threadInterrupted = new AtomicBoolean(false);

        Thread thread = new Thread(() -> {
            Uninterruptable.sleep(1000);
            threadInterrupted.set(Thread.currentThread().isInterrupted());
        });

        thread.start();
        thread.interrupt();
        thread.join(2000);

        assertThat(threadInterrupted.get()).isTrue();
    }

    @Test
    public void awaitLatchCompletesNormally() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Thread thread = new Thread(() -> {
            Uninterruptable.awaitLatch(latch);
            assertThat(Thread.currentThread().isInterrupted()).isFalse();
        });

        thread.start();
        latch.countDown();
        thread.join(100);
    }
}