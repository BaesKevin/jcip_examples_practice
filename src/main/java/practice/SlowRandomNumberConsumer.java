package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class SlowRandomNumberConsumer {

    private final SlowRandomNumberGenerator slowRandomNumberGenerator;

    public SlowRandomNumberConsumer(SlowRandomNumberGenerator slowRandomNumberGenerator) {
        this.slowRandomNumberGenerator = slowRandomNumberGenerator;
    }

    public List<Integer> fetchNumbersSync() throws InterruptedException {
        int count = 10;
        List<Integer> numbers = new ArrayList<>(count);

        slowRandomNumberGenerator.start();

        for (int i = 0; i < count; i++) {
            try {
                numbers.add(slowRandomNumberGenerator.nextInt());
            } catch (InterruptedException e) {
                // ignore and try again
            }
        }

        slowRandomNumberGenerator.stop();

        return numbers;
    }

    public List<Integer> fetchNumbersConcurrent() throws InterruptedException {
        int count = 10;
        List<Integer> numbers = new ArrayList<>(count);

        slowRandomNumberGenerator.start();
        ExecutorService executorService = Executors.newCachedThreadPool();

        List<Callable<Integer>> randomNumberFetchingTasks = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            randomNumberFetchingTasks.add(slowRandomNumberGenerator::nextInt);
        }

        List<Future<Integer>> futures = executorService.invokeAll(randomNumberFetchingTasks);

        for (Future<Integer> future : futures) {
            try {
                numbers.add(future.get());
            } catch (ExecutionException e) {
                System.out.println("execution exception for number");
                e.printStackTrace();
            }
        }

        executorService.shutdownNow();
        slowRandomNumberGenerator.stop();

        return numbers;
    }

}
