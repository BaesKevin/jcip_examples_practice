package practice.blocking_interrupting;

import java.math.BigDecimal;

public interface FibonacciService {

    BigDecimal getNthFibonacciNumber(int n) throws InterruptedException;

}
