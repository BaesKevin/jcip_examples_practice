package practice.blocking_interrupting;

import practice.threadsafety.FibonacciSequence;

import java.math.BigDecimal;

public class UncachedFibonacciService implements FibonacciService {

    public BigDecimal getNthFibonacciNumber(int n) {
        FibonacciSequence sequence = new FibonacciSequence();

        for (int i = 0; i < n - 1; i++) {
            sequence.getNext();
        }

        return sequence.getNext();
    }

}
