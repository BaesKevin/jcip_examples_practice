package jcip.ch0_introduction;

import net.jcip.annotations.NotThreadSafe;

/**
 * UnsafeSequence
 *
 * @author Brian Goetz and Tim Peierls
 */

@NotThreadSafe
public class UnsafeSequence implements Sequence<Long> {
    private long value;

    /**
     * Returns a unique value.
     */
    public Long getNext() {
        return value++;
    }
}
