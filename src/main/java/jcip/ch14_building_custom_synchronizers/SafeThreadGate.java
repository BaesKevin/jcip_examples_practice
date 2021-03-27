package jcip.ch14_building_custom_synchronizers;

import net.jcip.annotations.*;

/**
 * ThreadGate
 * <p/>
 * Recloseable gate using wait and notifyAll
 *
 * @author Brian Goetz and Tim Peierls
 */
@ThreadSafe
public class SafeThreadGate implements ThreadGate {
    // CONDITION-PREDICATE: opened-since(n) (isOpen || generation>n)
    @GuardedBy("this") private boolean isOpen;
    @GuardedBy("this") private int generation;
    @GuardedBy("this") private int proceeded;

    @Override
    public synchronized void close() {
        isOpen = false;
    }

    @Override
    public synchronized void open() {
        ++generation;
        isOpen = true;
        notifyAll();
    }

    // BLOCKS-UNTIL: opened-since(generation on entry)
    @Override
    public synchronized void await() throws InterruptedException {
        int arrivalGeneration = generation; // await before open: arrival gen == 0
        while (!isOpen && arrivalGeneration == generation)
            wait();

        proceeded++;
    }

    @Override
    public synchronized int getProceeded() {
        return proceeded;
    }
}
