package jcip.util;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class DataRaceTestHelperExample {

    @Test
    public void unsafeCounterHasDataRaceTest() {
        DataRaceTestHelper dataRaceTestHelper = new DataRaceTestHelper(new UnsafeCounterTest());

        int dataRaces = dataRaceTestHelper.tryDetectingDataRaces();

        assertThat(dataRaces).isNotZero();
    }

    @Test
    public void safeCounterDoesntHaveDataRaceTest() {
        DataRaceTestHelper dataRaceTestHelper = new DataRaceTestHelper(new SafeCounterTest());

        int dataRaces = dataRaceTestHelper.tryDetectingDataRaces();

        assertThat(dataRaces).isZero();
    }

    static class UnsafeCounterTest extends DataRaceTest {
        private int i;

        public UnsafeCounterTest() {
            super(1000);
        }

        @Override
        protected void setup() {
            i = 0;
        }

        @Override
        protected void doThread1Task() {
            i++;
        }

        @Override
        protected void doThread2Task() {
            i++;
        }

        @Override
        protected boolean dataRaceHappened() {
            return i != 2;
        }
    }

    static class SafeCounterTest extends DataRaceTest {
        private AtomicInteger i;

        public SafeCounterTest() {
            super(100_000);
        }

        @Override
        protected void setup() {
            i = new AtomicInteger();
        }

        @Override
        protected void doThread1Task() {
            i.incrementAndGet();
        }

        @Override
        protected void doThread2Task() {
            i.incrementAndGet();
        }

        @Override
        protected boolean dataRaceHappened() {
            return i.get() != 2;
        }
    }
}
