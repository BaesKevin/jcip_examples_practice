package practice;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;

public class SemaphoreRateLimitedExecutor {
    private final Executor executor;
    private final Semaphore noRequestsAllowed;

    public SemaphoreRateLimitedExecutor(Executor executor, int noRequestsAllowed) {
        this.executor = executor;
        this.noRequestsAllowed = new Semaphore(noRequestsAllowed);
    }

    public void execute(Runnable command) throws InterruptedException {
        noRequestsAllowed.acquire();
        try {
            doExecute(command);
        } catch (RejectedExecutionException e) {
            noRequestsAllowed.release();
        }
    }

    public boolean tryExecute(Runnable command) {
        if(!noRequestsAllowed.tryAcquire()) {
            return false;
        }

        try {
            doExecute(command);
            return true;
        } catch (RejectedExecutionException e) {
            noRequestsAllowed.release();
            return false;
        }
    }

    private void doExecute(Runnable command) {
        executor.execute(() -> {
            try {
                command.run();
            } finally {
                noRequestsAllowed.release();
            }
        });
    }
}
