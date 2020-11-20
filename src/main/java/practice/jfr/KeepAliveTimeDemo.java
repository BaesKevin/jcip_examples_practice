package practice.jfr;

import java.util.concurrent.*;

public class KeepAliveTimeDemo {

    public static void main(String[] args) throws InterruptedException {

        // executors keep threads alive
        ExecutorService oneShotExecutor = new ThreadPoolExecutor(0, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        oneShotExecutor.submit(() -> {
            // do nothing
        });

//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                // do nothing
//            }
//        });
//        thread.start();
//        thread.join();

    }

}
