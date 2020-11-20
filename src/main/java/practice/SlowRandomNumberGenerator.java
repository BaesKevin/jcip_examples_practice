package practice;

import java.util.concurrent.*;

public class SlowRandomNumberGenerator {
    private final BlockingQueue<Integer> numbers = new ArrayBlockingQueue<>(10);
    private final ScheduledExecutorService generatorExecutor = Executors.newScheduledThreadPool(1);
    private final long periodInMs;

    public SlowRandomNumberGenerator(long periodInMs) {
        this.periodInMs = periodInMs;
    }

    public void start() {
        generatorExecutor.scheduleAtFixedRate(generatorTask(), 0, periodInMs, TimeUnit.MILLISECONDS);
    }

    public void stop() throws InterruptedException {
        generatorExecutor.shutdown();
        generatorExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS);
    }

    private Runnable generatorTask() {
        return () -> {
            try {
                numbers.put(ThreadLocalRandom.current().nextInt());
            } catch (InterruptedException e) {
                System.out.println("interrupted while putting new random number");
                Thread.currentThread().interrupt();
            }
        };
    }

    public int nextInt() throws InterruptedException {
        Integer number = numbers.take();
        System.out.println("client took number");
        return number;
    }

}
