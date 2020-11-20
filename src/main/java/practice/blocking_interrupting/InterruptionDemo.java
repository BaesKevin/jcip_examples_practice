package practice.blocking_interrupting;

import practice.util.NamedThreadFactory;

import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static practice.util.Uninterruptable.join;
import static practice.util.Uninterruptable.sleep;

public class InterruptionDemo {

    public static final int SLEEP_INTERVAL = 100;

    public static void main(String[] args) throws InterruptedException {
        new InterruptionDemo().run();
    }

    private void run() throws InterruptedException {
        interruptableThread();
        interruptableFuture();
        shutdownHandler();
    }

    private void interruptableThread() {
        // thread sleeps in intervals, temporarily checking if it should stop
        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                sleep(SLEEP_INTERVAL);
                System.out.println("Thread interrupted: " + Thread.currentThread().isInterrupted());
            }
        }, "interruptible-thread");

        // main sleeps 500 ms before interrupting the thread
        thread.start();
        sleep(500);
        thread.interrupt();
        join(thread);
    }

    private void interruptableFuture() throws InterruptedException {
        // non-zero core pool size or keep alive time 'busyloop' the pool thread until it's allowed to exit
        ExecutorService singleUseExecutor = getSingleUseExecutor();

        // tas
        Future<?> task = singleUseExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                sleep(100);
                System.out.println("Future interrupted: " + Thread.currentThread().isInterrupted());
            }
        });

        sleep(500);
        task.cancel(true);

        // technically not needed as the single use executor does not keep alive threads after completion
        singleUseExecutor.shutdown(); // initiate shutdown, do not accept new tasks, does not wait for tasks
        singleUseExecutor.awaitTermination(150, MILLISECONDS); // synchronously wait for the pool to shut down
    }

    private ThreadPoolExecutor getSingleUseExecutor() {
        return new ThreadPoolExecutor(
                0,
                1,
                0, MILLISECONDS,
                new SynchronousQueue<>(),
                new NamedThreadFactory("single-use-pool"));
    }

    private void shutdownHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("JVM shutdown detected");
        }));
    }

}
