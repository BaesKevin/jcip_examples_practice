package practice.util;

import jcip.ch8_applying_thread_pools.MyAppThread;

import java.util.concurrent.ThreadFactory;

/**
 * copy from {@link jcip.ch8_applying_thread_pools.MyThreadFactory}
 */
public class NamedThreadFactory implements ThreadFactory {
    private final String poolName;

    public NamedThreadFactory(String poolName) {
        this.poolName = poolName;
    }

    public Thread newThread(Runnable runnable) {
        return new MyAppThread(runnable, poolName);
    }
}
