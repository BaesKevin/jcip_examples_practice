package jcip.ch2_thread_safety;

import jcip.util.DataRaceTest;
import jcip.util.DataRaceTestHelper;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

public class LazyInitRaceTest {

    // test: when multiple thread call getInstance at approx the same time, only one object is returned
    // because two threads observe instance to be null at the same time

    // running the whole test class makes it so the test with DataRaceTestHelper succeeds,
    // theory: when running the test long enough the compiler has inlined some values so the test always succeeds
    // actual problem: the test class called getThreadXTask after waiting on the synchronizer. It seems that
    // this introduced enough extra work that that data race never happened.
    @Test
    public void lazyInitOnlyCreatesOnceSingleThread() {
        LazyInitRace lazyInitRace = new LazyInitRace();
        ExpensiveObject instance1 = lazyInitRace.getInstance();
        ExpensiveObject instance2 = lazyInitRace.getInstance();

        assertThat(instance1).isSameAs(instance2);
    }

    @Test
    public void lazyInitOnlyCreatesOnceMultipleThreads() throws InterruptedException {
        AtomicInteger failures = new AtomicInteger();
        for (int i = 0; i < 10000; i++) {
            LazyInitRace lazyInitRace = new LazyInitRace();
            AtomicReference<ExpensiveObject> instance1 = new AtomicReference<>();
            AtomicReference<ExpensiveObject> instance2 = new AtomicReference<>();
            CountDownLatch startSynchronizer = new CountDownLatch(1);
            Thread thread1 = new Thread(() -> {
                try {
                    startSynchronizer.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                instance1.set(lazyInitRace.getInstance());
            });
            Thread thread2 = new Thread(() -> {
                try {
                    startSynchronizer.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                instance2.set(lazyInitRace.getInstance());
            });
            thread1.start();
            thread2.start();
            startSynchronizer.countDown();
            thread1.join(10000);
            thread2.join(10000);

            if (instance1.get() != instance2.get()) {
                failures.incrementAndGet();
            }
        }

        assertThat(failures.get()).isGreaterThan(0);
    }

    @Test
    public void lazyInitOnlyCreatesOnceMultipleThreadsWithDataRaceWithHelper() {
        DataRaceTestHelper dataRaceTestHelper = new DataRaceTestHelper(new LazyInitDataRaceTest());

        int dataRaces = dataRaceTestHelper.tryDetectingDataRaces();

        assertThat(dataRaces).isZero();
    }

    static class LazyInitDataRaceTest extends DataRaceTest {
        private LazyInitRace lazyInitRace;
        private ExpensiveObject instance1;
        private ExpensiveObject instance2;

        public LazyInitDataRaceTest() {
            super(10000);
        }

        @Override
        protected void setup() {
            lazyInitRace = new LazyInitRace();
        }

        @Override
        protected void doThread1Task() {
            instance1 = lazyInitRace.getInstance();
        }

        @Override
        protected void doThread2Task() {
            instance2 = lazyInitRace.getInstance();
        }

        @Override
        protected boolean dataRaceHappened() {
            return instance1 != instance2;
        }
    }


}