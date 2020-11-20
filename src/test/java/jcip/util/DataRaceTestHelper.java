package jcip.util;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class helps validate that data races happen in a certain class.
 * Clients subclass this class and provide a {@link DataRaceTest instance}.
 * <p>
 * This class calls the provided methods in separate threads, but provides sufficient internal synchronization
 * that clients don't need to worry about sharing objects between the setup/dataRaceHappened methods and
 * the methods called by doThreadNTask().
 */
public final class DataRaceTestHelper {
    private final DataRaceTest dataRaceTest;

    // Technically, it is not required to use synchronization in this class as thread.start and thread.join happen-before
    // other actions. The book suggests not relying too much on the memory model, so we do use synchronization.
    private final Object thread1lock = new Object();
    private final Object thread2lock = new Object();

    public DataRaceTestHelper(DataRaceTest dataRaceTest) {
        this.dataRaceTest = dataRaceTest;
    }

    public final int tryDetectingDataRaces() {
        int detectedDataRaces = 0;  // thread local, no sync needed
        ExecutorService executor = Executors.newFixedThreadPool(2);

        for (int i = 0; i < dataRaceTest.getIterationCount(); i++) {
            doSetup();
            CountDownLatch startSynchronizer = new CountDownLatch(1);

            Future<?> task1 = executor.submit(getThread1Runnable(startSynchronizer));
            Future<?> task2 = executor.submit(getThread2Runnable(startSynchronizer));

            startSynchronizer.countDown();
            try {
                task1.get(1, TimeUnit.SECONDS);
                task2.get(1, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
                detectedDataRaces++;
            }

            // dataRaceHappened may share state with the thread tasks
            synchronized (thread1lock) {
                synchronized (thread2lock) {
                    if (dataRaceTest.dataRaceHappened()) {
                        detectedDataRaces++;
                    }
                }
            }
        }

        return detectedDataRaces;
    }

    private Runnable getThread1Runnable(CountDownLatch startSynchronizer) {
        return () -> {
            try {
                startSynchronizer.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            doThread1();
        };
    }

    private Runnable getThread2Runnable(CountDownLatch startSynchronizer) {
        return () -> {
            try {
                startSynchronizer.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            doThread2();
        };
    }

    // setup most likely shares state with the thread tasks
    private void doSetup() {
        synchronized (thread1lock) {
            synchronized (thread2lock) {
                dataRaceTest.setup();
            }
        }
    }

    private void doThread1() {
        synchronized (thread1lock) {
            dataRaceTest.doThread1Task();
        }
    }

    private void doThread2() {
        synchronized (thread2lock) {
            dataRaceTest.doThread2Task();
        }
    }
}
