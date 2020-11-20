package practice.program_runtime;

import java.util.Random;

public class RunTimeServiceClient {

    public static void main(String[] args) throws InterruptedException {
        practice.program_runtime.FileCheckpointRunningTimeService fileCheckpointRunningTimeService = new practice.program_runtime.FileCheckpointRunningTimeService();
        System.out.println("starting count: " + fileCheckpointRunningTimeService.getTimeRunning());
        fileCheckpointRunningTimeService.start();

        Thread.sleep(new Random().nextInt(10) * 1000);

        System.out.println("Counter counted: " + fileCheckpointRunningTimeService.getTimeRunning() + " seconds.");

        fileCheckpointRunningTimeService.stop();
    }

}
