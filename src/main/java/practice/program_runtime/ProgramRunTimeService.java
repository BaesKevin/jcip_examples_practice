package practice.program_runtime;

public interface ProgramRunTimeService {

    void start();
    void stop() throws InterruptedException;
    void stopNow();
    long getTimeRunning();

}
