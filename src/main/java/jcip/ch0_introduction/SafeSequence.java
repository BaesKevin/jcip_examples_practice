package jcip.ch0_introduction;

import net.jcip.annotations.*;

/**
 * Sequence
 *
 * @author Brian Goetz and Tim Peierls
 */

@ThreadSafe
public class SafeSequence implements Sequence<Long> {
    @GuardedBy("this") private long nextValue;

    public synchronized Long getNext() {
        return nextValue++;
    }

}
