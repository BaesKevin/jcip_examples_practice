package practice.threadsafety;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * program written to show the difference between the default 'profile' JFC and my custom 'profile threads' JFC
 */
public class FibonacciProgram {

    public static void main(String[] args) throws InterruptedException {
        new FibonacciProgram().run();
    }

    private void run() throws InterruptedException {
        int n = 10000;
//        BigDecimal nthFibonacciNumber = getNthFibonacciNumberSync(n);
        BigDecimal nthFibonacciNumber = getNthFibonacciNumberAsync(n);

        System.out.printf("Fibonacci number %d is %s", n, nthFibonacciNumber.toString());
    }

    public BigDecimal getNthFibonacciNumberSync(int n) {
        FibonacciSequence sequence = new FibonacciSequence();

        for (int i = 0; i < n; i++) {
            sequence.getNext();
        }

        return sequence.getNext();
    }

    public BigDecimal getNthFibonacciNumberAsync(int n) throws InterruptedException {
        FibonacciSequence sequence = new FibonacciSequence();

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < n - 1; i++) {
            executorService.submit(sequence::getNext);
        }
        executorService.shutdown();
        executorService.awaitTermination(10000, TimeUnit.MILLISECONDS);

        return sequence.getNext();
    }
}
