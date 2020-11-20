package jcip.ch12_testing_concurrent_programs;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class SemaphoreBoundedBufferTest {
    private static final long LOCKUP_DETECT_TIMEOUT = 500;
    private static final int CAPACITY = 10000;
    private static final int THRESHOLD = 10000;

    @Test
    public void testIsEmptyWhenConstructed() {
        SemaphoreBoundedBuffer<Integer> bb = new SemaphoreBoundedBuffer<>(10);
        assertTrue(bb.isEmpty());
        assertFalse(bb.isFull());
    }

    @Test
    public void testIsFullAfterPuts() throws InterruptedException {
        SemaphoreBoundedBuffer<Integer> bb = new SemaphoreBoundedBuffer<>(10);
        for (int i = 0; i < 10; i++)
            bb.put(i);
        assertTrue(bb.isFull());
        assertFalse(bb.isEmpty());
    }

    @Test
    public void testTakeBlocksWhenEmpty() {
        final SemaphoreBoundedBuffer<Integer> bb = new SemaphoreBoundedBuffer<>(10);
        Thread taker = new Thread(() -> {
            try {
                int unused = bb.take();

                fail(); // if we get here, it's an error
            } catch (InterruptedException success) {
            }
        });
        try {
            taker.start();
            Thread.sleep(LOCKUP_DETECT_TIMEOUT); // wait for the taker to get to an interruptable point
            taker.interrupt(); // interrupt blocked method
            taker.join(LOCKUP_DETECT_TIMEOUT); // safety timeout
            assertFalse(taker.isAlive()); // assure join worked
        } catch (Exception unexpected) {
            fail();
        }
    }


    static class Big {
        double[] data = new double[100000];
    }

    @Test
    public void testLeak() throws InterruptedException {
        SemaphoreBoundedBuffer<SemaphoreBoundedBufferTest.Big> bb = new SemaphoreBoundedBuffer<>(CAPACITY);
        int heapSize1 = snapshotHeap();
        for (int i = 0; i < CAPACITY; i++)
            bb.put(new SemaphoreBoundedBufferTest.Big());
        for (int i = 0; i < CAPACITY; i++)
            bb.take();
        int heapSize2 = snapshotHeap();
        assertTrue(Math.abs(heapSize1 - heapSize2) < THRESHOLD);
    }

    private int snapshotHeap() {
        /* Snapshot heap and return heap size */
        return 0;
    }

}
