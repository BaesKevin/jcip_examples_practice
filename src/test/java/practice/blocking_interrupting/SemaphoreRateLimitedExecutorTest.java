package practice.blocking_interrupting;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import practice.SemaphoreRateLimitedExecutor;
import practice.util.Uninterruptable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;


public class SemaphoreRateLimitedExecutorTest {

    public static final int LOCKUP_TIMEOUT = 10000;

    private ExecutorService serviceToInstrument;

    @Before
    public void setUp() throws Exception {
        serviceToInstrument = Executors.newCachedThreadPool();
    }

    @After
    public void tearDown() throws Exception {
        serviceToInstrument.shutdownNow();
        serviceToInstrument.awaitTermination(10000, TimeUnit.SECONDS);
    }

    @Test
    public void executorRunsTasks() throws InterruptedException {
        SemaphoreRateLimitedExecutor semaphoreRateLimitedExecutor = new SemaphoreRateLimitedExecutor(serviceToInstrument, 100);
        AtomicInteger count = new AtomicInteger();

        Thread thread = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                try {
                    semaphoreRateLimitedExecutor.execute(count::incrementAndGet);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    fail();
                }
            }
        });

        thread.start();
        thread.join(LOCKUP_TIMEOUT);
        assertThat(count.get()).isEqualTo(100);
        assertThat(thread.isAlive()).isFalse();
    }

    @Test
    public void executeBlocksWhenNoMoreRequestsAllowed() throws InterruptedException {
        SemaphoreRateLimitedExecutor semaphoreRateLimitedExecutor = new SemaphoreRateLimitedExecutor(serviceToInstrument, 1);
        AtomicInteger tasksSubmitted = new AtomicInteger(0);
        semaphoreRateLimitedExecutor.execute(() -> {
            tasksSubmitted.incrementAndGet();
            sleep(5);
        });

        Thread thread = new Thread(() -> {
            try {
                semaphoreRateLimitedExecutor.execute(tasksSubmitted::incrementAndGet);
                fail(); // fail: execute should have blocked
            } catch (InterruptedException e) {
                fail();
            }
        });

        thread.interrupt();
        thread.join(500);
        assertThat(tasksSubmitted.get()).isEqualTo(1);
    }

    @Test
    public void tryExecuteReturnsTrueWhenSubmitSucceeds() {
        SemaphoreRateLimitedExecutor executor = new SemaphoreRateLimitedExecutor(serviceToInstrument, 1);

        boolean submitSuccess = executor.tryExecute(() -> {
            int a = 1 + 2;
        });

        assertThat(submitSuccess).isTrue();
    }

    @Test
    public void tryExecuteReturnsFalseWhenSubmitFails() {
        SemaphoreRateLimitedExecutor executor = new SemaphoreRateLimitedExecutor(serviceToInstrument, 1);
        boolean submitSuccess1 = executor.tryExecute(() -> sleep(5));
        boolean submitSuccess2 = executor.tryExecute(() -> sleep(1));

        assertThat(submitSuccess1).isTrue();
        assertThat(submitSuccess2).isFalse();
    }

    void sleep(int seconds) {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(seconds));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    void sleepUninterruptibly(int seconds) {
        Uninterruptable.sleep(TimeUnit.SECONDS.toMillis(seconds));
    }

}