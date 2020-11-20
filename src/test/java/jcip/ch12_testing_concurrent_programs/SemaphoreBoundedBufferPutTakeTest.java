package jcip.ch12_testing_concurrent_programs;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class SemaphoreBoundedBufferPutTakeTest {
    static final long PUTTAKE_TIMEOUT = 1000 * 10;

    protected static final ExecutorService pool = Executors.newCachedThreadPool();
    protected SemaphoreBoundedBuffer<Integer> bb;
    protected volatile int nTrials;
    protected int nPairs;
    protected CyclicBarrier barrier;
    protected final AtomicInteger putSum = new AtomicInteger(0);
    protected final AtomicInteger takeSum = new AtomicInteger(0);

    @Before
    public void setUp() {
        this.bb = new SemaphoreBoundedBuffer<>(10);
        nTrials = 100_000;
        nPairs = 10;
        barrier = new CyclicBarrier(nPairs * 2 + 1);
    }

    @Test(timeout = PUTTAKE_TIMEOUT)
    public void putTakeTest() {
        doTest();
        pool.shutdown();
    }

    void doTest() {
        try {
            for (int i = 0; i < nPairs; i++) {
                pool.execute(new SemaphoreBoundedBufferPutTakeTest.Producer());
                pool.execute(new SemaphoreBoundedBufferPutTakeTest.Consumer());
            }
            barrier.await(); // wait for all threads to be ready
            barrier.await(); // wait for all threads to finish
            assertEquals(putSum.get(), takeSum.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // cheap medium-quality random number function (Marsaglia, 2003)
    // ensures numbers change from run to run
    static int xorShift(int y) {
        y ^= (y << 6);
        y ^= (y >>> 21);
        y ^= (y << 7);
        return y;
    }

    class Producer implements Runnable {
        public void run() {
            try {
                // good enough seed for a sequence of random numbers
                int seed = (this.hashCode() ^ (int) System.nanoTime());
                int sum = 0;
                barrier.await();
                for (int i = nTrials; i > 0; --i) {
                    bb.put(seed);
                    sum += seed;
                    seed = xorShift(seed);
                }
                putSum.getAndAdd(sum);
                barrier.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    class Consumer implements Runnable {
        public void run() {
            try {
                barrier.await();
                int sum = 0;
                for (int i = nTrials; i > 0; --i) {
                    sum += bb.take();
                }
                takeSum.getAndAdd(sum);
                barrier.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
