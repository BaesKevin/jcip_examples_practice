package jcip.ch0_introduction;

import net.jcip.annotations.ThreadSafe;
import org.junit.Test;

import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

public class SequenceTest {

    private static final long TIMEOUT = 10000L;
    private int noTests = 100;
    private int numberToCountTo = 10;

    @Test
    public void unsafeSequenceWithMultipleThreadsResultsInLostUpdates() throws InterruptedException {
        for (int i = 0; i < noTests; i++) {
            runTest(numberToCountTo, new UnsafeSequence());
        }
    }

    @Test
    public void unsafeSequenceWithExecutor() throws InterruptedException {
        for (int i = 0; i < noTests; i++) {
            runTestWithExecutor(numberToCountTo, new UnsafeSequence());
        }
    }

    @Test
    public void safeSequence() throws InterruptedException {
        for (int i = 0; i < noTests; i++) {
            runTestWithExecutor(numberToCountTo, new SafeSequence());
        }
    }

    private void runTest(int count, Sequence sequence) throws InterruptedException {
        Thread countingThread1 = new Thread(new Counter(sequence, count));
        Thread countingThread2 = new Thread(new Counter(sequence, count));

        countingThread1.start();
        countingThread2.start();

        countingThread1.join(TIMEOUT);
        countingThread2.join(TIMEOUT);

        assertEquals(count * 2, sequence.getNext());
    }

    private void runTestWithExecutor(int count, Sequence sequence) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.execute(new Counter(sequence, count));
        executor.execute(new Counter(sequence, count));

        executor.shutdown();
        executor.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);

        assertEquals(count * 2, sequence.getNext());
    }

    @ThreadSafe
    static class Counter implements Runnable {
        private final Sequence sequence;
        private final int count;

        public Counter(Sequence sequence, int count) {
            this.sequence = sequence;
            this.count = count;
        }

        @Override
        public void run() {
            for (int i = 0; i < count; i++) {
                sequence.getNext();
            }
        }
    }
}