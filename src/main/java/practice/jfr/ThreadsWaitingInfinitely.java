package practice.jfr;

import java.util.concurrent.*;

/**
 * Starts n threads that are all waiting in BlockingQueue.take to showcase monitoring tools.
 *
 * Ways to detect that the threads are not doing anything:
 * <ul>
 *     <li>In IDEA, request 'Dump threads' (camera icon)</li>
 *     <li>In visualvm: threads tab shows all threads are parked, meaning 'doing nothing'</li>
 * </ul>
 */
public class ThreadsWaitingInfinitely {

    public static void main(String[] args) {
        new ThreadsWaitingInfinitely().infiniteWaitingThreads();
    }

    void infiniteWaitingThreads() {
        int i = 1000;
        ExecutorService service = Executors.newFixedThreadPool(i);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down executor with shutdownNow");
            service.shutdownNow();
        }));

        simulateNWaitingThreads(i, service);
    }

    private void simulateNWaitingThreads(int nThreads, ExecutorService service) {
        BlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<>(1);

        for (int i = 0; i < nThreads; i++) {
            service.submit(() -> {
                try {
                    blockingQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

}
