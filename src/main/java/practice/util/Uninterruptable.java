package practice.util;

import java.util.concurrent.CountDownLatch;

import static java.lang.Thread.currentThread;

/**
 * These operations mirror blocking methods in the JDK. The methods here ignore interruption until the operation is done,
 * and restore the interrupt if the original operation was interrupted.
 */
public class Uninterruptable {

    public static void sleep(long millis) {
        boolean doneWaiting = false;
        boolean interrupted = false;

        try {
            long startTime = System.currentTimeMillis();
            long timeWaited = 0;

            while (!doneWaiting) {
                try {
                    Thread.sleep(millis - timeWaited);
                    doneWaiting = true;
                } catch (InterruptedException e) {
                    long interruptionTime = System.currentTimeMillis();
                    timeWaited += interruptionTime - startTime;
                    startTime = interruptionTime;
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                currentThread().interrupt();
            }
        }
    }

    public static void awaitLatch(CountDownLatch latch) {
        boolean interrupted = false;
        boolean awaitDone = false;

        try {
            while (!awaitDone) {
                try {
                    latch.await();
                    awaitDone = true;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted)
                currentThread().interrupt();
        }
    }

    public static void join(Thread thread) {
        boolean interrupted = false;
        boolean joinDone = false;

        try{
            while(!joinDone) {
                try {
                    thread.join();
                    joinDone = true;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if(interrupted)
                Thread.currentThread().interrupt();
        }
    }

}
