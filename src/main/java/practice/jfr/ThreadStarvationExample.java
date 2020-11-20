package practice.jfr;

/**
 * Simulate a very simple resource deadlock: start 2 threads that try to acquire the same lock and busyloop once the lock is
 * acquired. One of the threads is infinitely stuck waiting for the lock while the other does its 'expensive' operation.
 * If we make the busy loop sleep, visualvm will show the thread as 'sleeping, if we make it just busyloop, it will show
 * the thread as running.
 * The second thread is stuck in the 'monitor' state.
 *
 * This will not show up in a thread dump as a 'deadlock'
 */
public class ThreadStarvationExample {

    public static void main(String[] args) {
        new ThreadStarvationExample().run();
    }

    private void run() {
        Object lock = new Object();

        createBusyLoopWithLockHoldThread(lock, "t1").start();
        createBusyLoopWithLockHoldThread(lock, "t2").start();
    }

    private Thread createBusyLoopWithLockHoldThread(Object lock, String name) {
        return new Thread(() -> {
            synchronized (lock) {
                System.out.println(name + " has the lock");
                // busy loop to hold the lock
                while (true) {
//                    try {
//                        Thread.sleep(200);
//                    } catch (InterruptedException e) {
//                        System.out.println(name + " interrupted");
//                    }
                }
            }
        });
    }

}
