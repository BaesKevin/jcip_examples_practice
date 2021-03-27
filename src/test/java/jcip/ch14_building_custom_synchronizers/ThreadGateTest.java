package jcip.ch14_building_custom_synchronizers;

import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ThreadGateTest {

    @Test
    public void multipleOpenClose() throws InterruptedException {
        ThreadGate threadGate = new SafeThreadGate();
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.submit(waitAndPrintInALoop(threadGate));
        executorService.submit(waitAndPrintInALoop(threadGate));

        openClose(threadGate);
        openClose(threadGate);

        executorService.shutdownNow();
    }

    @Test
    public void openCloseInstantlySafe() throws InterruptedException {
        openThenCloseInstantlyTest(new SafeThreadGate());
    }

    @Test
    public void openCloseInstantlyUnsafe() throws InterruptedException {
        openThenCloseInstantlyTest(new UnsafeThreadGate());
    }

    private void openThenCloseInstantlyTest(ThreadGate threadGate) throws InterruptedException {
        int count = 1000;
        ExecutorService executorService = testCachedExecutor();

        for (int i = 0; i < count; i++) {
            executorService.submit(wait(threadGate));
        }

        Thread.sleep(1000);// wait until all are blocked
        threadGate.open();
        // after the open call, count+1 threads contend for the ThreadGate lock:
        // this test to call close + 1000 trying to reacquire the lock to emerge from wait
        threadGate.close();

        executorService.shutdown();
        executorService.awaitTermination(2, TimeUnit.SECONDS);

        assertThat(threadGate.getProceeded()).isEqualTo(count);
    }

    private void openClose(ThreadGate threadGate) throws InterruptedException {
        System.out.println("opening the gate");
        threadGate.open();
        Thread.sleep(500);
        System.out.println("closing the gate");
        threadGate.close();
    }

    private Runnable waitAndPrintInALoop(ThreadGate threadGate) {
        return () -> {
            while (true) {
                try {
                    threadGate.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                System.out.println("thread " + Thread.currentThread().getName() + " proceeds");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        };
    }

    static AtomicInteger interrupted = new AtomicInteger();

    private ThreadPoolExecutor testCachedExecutor() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                0, TimeUnit.SECONDS,
                new SynchronousQueue<>());
    }

    private Runnable wait(ThreadGate threadGate) {
        return () -> {
            try {
                threadGate.await();
            } catch (InterruptedException e) {
                interrupted.incrementAndGet();
                Thread.currentThread().interrupt();
            }
        };
    }
}