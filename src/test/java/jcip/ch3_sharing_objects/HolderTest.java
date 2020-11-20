package jcip.ch3_sharing_objects;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HolderTest {

    // can't get assertInsanity to throw an exception on my machine
    @Test
    public void assertSanity() throws InterruptedException {
        int count = 1_000_000;

        AtomicInteger npes = new AtomicInteger(0);
        AtomicInteger insanities = new AtomicInteger(0);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        for (int i = 0; i < count; i++) {
            StuffIntoPublic stuffIntoPublic = new StuffIntoPublic();
            CountDownLatch startLatch = new CountDownLatch(1);

            executorService.submit(() -> {
                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                stuffIntoPublic.initialize();
            });
            executorService.submit(() -> {
                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try{
                    stuffIntoPublic.holder.assertSanity();
                } catch (NullPointerException npe) {
                    npes.incrementAndGet();
                } catch (AssertionError e) {
                    insanities.incrementAndGet();
                }
            });

            startLatch.countDown();
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        System.out.printf("npes: %d, insanities: %d", npes.get(), insanities.get());
    }
}
