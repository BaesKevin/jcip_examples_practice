package practice.threadsafety;

import jcip.ch0_introduction.Sequence;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

public class FibonacciSequence implements Sequence<BigDecimal> {
    BigDecimal first = ZERO;
    BigDecimal second = ONE;
    int nthNumber = 1;

    // method with check-then-act and read-modify-write race conditions
    @Override
    public synchronized BigDecimal getNext() {
        if(nthNumber == 1) {
            nthNumber++;
            return first;
        } else if(nthNumber == 2) {
            nthNumber++;
            return second;
        }

        BigDecimal nextFib = first.add(second);
        first = second;
        second = nextFib;
        return nextFib;
    }
}
