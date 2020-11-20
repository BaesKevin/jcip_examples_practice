package jcip.ch12_testing_concurrent_programs;

import jcip.BarrierTimer;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CyclicBarrier;

import static org.junit.Assert.assertEquals;

public class SemaphoreBoundedBufferTimedPutTakeTest extends SemaphoreBoundedBufferPutTakeTest {
    // this timer is the barrier action to perform
    private BarrierTimer timer = new BarrierTimer();

    @Override
    @Before
    public void setUp() {
        super.setUp();
        barrier = new CyclicBarrier(nPairs * 2 + 1, timer);
    }

    @Test
    public void timedTest() throws InterruptedException {
        int tpt = 100000; // trials per thread
        for (int cap = 1; cap <= 1000; cap *= 10) {
            System.out.println("Capacity: " + cap);
            for (int pairs = 1; pairs <= 128; pairs *= 2) {
                TimedPutTakeTest t = new TimedPutTakeTest(cap, pairs, tpt);
                System.out.print("Pairs: " + pairs + "\t");
                t.test();
                System.out.print("\t");
                Thread.sleep(1000);
                t.test();
                System.out.println();
                Thread.sleep(1000);
            }
        }
        pool.shutdown();
    }

    public void doTest() {
        try {
            timer.clear();
            for (int i = 0; i < nPairs; i++) {
                pool.execute(new SemaphoreBoundedBufferPutTakeTest.Producer());
                pool.execute(new SemaphoreBoundedBufferPutTakeTest.Consumer());
            }
            barrier.await();
            barrier.await();
            long nsPerItem = timer.getTime() / (nPairs * (long) nTrials);
            System.out.print("Throughput: " + nsPerItem + " ns/item");
            assertEquals(putSum.get(), takeSum.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
