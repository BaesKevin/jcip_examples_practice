package jcip.util;

public abstract class DataRaceTest {

    private final int iterationCount;

    public DataRaceTest(int iterationCount) {
        this.iterationCount = iterationCount;
    }

    public int getIterationCount() {
        return iterationCount;
    }

    protected abstract void setup();

    protected abstract void doThread1Task();

    protected abstract void doThread2Task();

    protected abstract boolean dataRaceHappened();


}
