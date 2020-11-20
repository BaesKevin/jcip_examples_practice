package practice.blocking_interrupting;

import java.math.BigDecimal;
import java.util.concurrent.Semaphore;

public class RateLimitedFibonacciService implements FibonacciService {

    private final FibonacciService fibonacciService;
    private final Semaphore requestsAllowed;

    public RateLimitedFibonacciService(FibonacciService fibonacciService, int allowedRequests) {
        this.fibonacciService = fibonacciService;
        requestsAllowed = new Semaphore(allowedRequests);
    }

    @Override
    public BigDecimal getNthFibonacciNumber(int n) throws InterruptedException {
        requestsAllowed.acquire();
        try {
            return fibonacciService.getNthFibonacciNumber(n);
        } finally {
            requestsAllowed.release();
        }
    }
}
