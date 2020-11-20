package jcip.ch7_cancallation_shutdown;

import java.math.BigInteger;
import java.util.concurrent.*;

/**
 * BrokenPrimeProducer
 * <p/>
 * Unreliable cancellation that can leave producers stuck in a blocking operation
 *
 * @author Brian Goetz and Tim Peierls
 */
class BrokenPrimeProducer extends Thread {
    private final BlockingQueue<BigInteger> queue;
    private volatile boolean cancelled = false;

    BrokenPrimeProducer(BlockingQueue<BigInteger> queue) {
        this.queue = queue;
    }

    public void run() {
        try {
            BigInteger p = BigInteger.ONE;
            while (!cancelled)
                queue.put(p = p.nextProbablePrime());
        } catch (InterruptedException consumed) {
        }
    }

    public void cancel() {
        cancelled = true;
    }
}

class BrokenPrimeConsumer {
    void consumePrimes() throws InterruptedException {
        BlockingQueue<BigInteger> primes = new ArrayBlockingQueue<BigInteger>(20);
        BrokenPrimeProducer brokenPrimeProducer = new BrokenPrimeProducer(primes);

        try {
            while(needMorePrimes()) {
                consume(primes.take());
            }
        } finally {
            brokenPrimeProducer.cancel();
        }
    }

    boolean needMorePrimes() {
        return true;
    }

    private void consume(BigInteger prime) {
    }
}

