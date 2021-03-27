package jcip.ch14_building_custom_synchronizers;

import net.jcip.annotations.GuardedBy;

/**
 * Variant on {@link SafeThreadGate} where threads are potentially blocked forever if the gate is opened and closed
 * in rapid succession.
 */
public class UnsafeThreadGate implements ThreadGate {
    // CONDITION-PREDICATE: is-open (isOpen)
    @GuardedBy("this") private boolean isOpen;
    @GuardedBy("this") private int proceeded;

    public synchronized void close() {
        isOpen = false;
    }

    public synchronized void open() {
        isOpen = true;
        notifyAll();
    }

    // BLOCKS-UNTIL:is-open (isOpen)
    public synchronized void await() throws InterruptedException {
        while (!isOpen)
            wait();

        proceeded++;
    }

    public synchronized int getProceeded() {
        return proceeded;
    }
}
