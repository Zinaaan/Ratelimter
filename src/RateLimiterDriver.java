import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lzn
 * @date 2023/04/05 17:08
 * @Description
 */
public class RateLimiterDriver {
    private static ExecutorService pool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws InterruptedException {
//        LeakyBucket rateLimiter = new LeakyBucket(10);
//        testLimit(rateLimiter);
//        rateLimiter.close();
//        RateLimiter rateLimiter = new TokenBucket(5);
//        RateLimiter rateLimiter = new SlidingWindowLog(5);
//        RateLimiter rateLimiter = new SlidingWindow(5);
//        Thread thread = new Thread(() -> {
//            sendRequest(rateLimiter, 10, 1);
//            sendRequest(rateLimiter, 20, 2);
//            sendRequest(rateLimiter,50, 5);
//            sendRequest(rateLimiter,100, 10);
//            sendRequest(rateLimiter,200, 20);
//            sendRequest(rateLimiter,250, 25);
//            sendRequest(rateLimiter,500, 50);
//            sendRequest(rateLimiter,1000, 100);
//            testLimit(rateLimiter);
//        });

//        thread.start();
//        thread.join();

//        LeakyBucket bucket = new LeakyBucket(10);
//        RateLimiter bucket = new TokenBucket(10);
//        RateLimiter bucket = new FixedWindow(10, 1000);
        RateLimiter bucket = new SlidingWindow(10, 1000);

        // execute 20 requests, with a maximum of 5 requests per second
        for (int i = 1; i <= 20; i++) {
            if (bucket.allow()) {
                System.out.println("Request " + i + " processed");
            } else {
                System.out.println("Request " + i + " rejected");
            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // wait for the scheduled task to complete before exiting the program
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        bucket.close();
    }

    public static void testLimit(RateLimiter rateLimiter) {

        // 被限制的次数
        AtomicInteger limited = new AtomicInteger(0);
        // 线程数
        final int threads = 2;
        // 每条线程的执行轮数
        final int turns = 40;

        // 同步器
        CountDownLatch countDownLatch = new CountDownLatch(threads);
        long start = System.currentTimeMillis();
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    for (int j = 0; j < turns; j++) {
//                        long taskId = Thread.currentThread().getId();
                        boolean intercepted = rateLimiter.allow();
                        if (!intercepted) {
                            // 被限制的次数累积
                            limited.getAndIncrement();
                        }
                        Thread.sleep(200);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //waiting for all threads to finish executing
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pool.shutdown();
        float time = (System.currentTimeMillis() - start) / 1000F;
        //输出统计结果
        System.out.println("限制的次数为：" + limited.get() +
                ",通过的次数为：" + (threads * turns - limited.get()));
        System.out.println("限制的比例为：" + (float) limited.get() / (float) (threads * turns));
        System.out.println("运行的时长为：" + time);
    }

    private static void sendRequest(RateLimiter rateLimiter, int totalReq, int requestPerSec) {
        System.out.println("total request: " + totalReq + ", request per second: " + requestPerSec);
        long startTime = System.currentTimeMillis();
        CountDownLatch count = new CountDownLatch(totalReq);
        for (int i = 0; i < totalReq; i++) {
            try {
                new Thread(() -> {
                    while (!rateLimiter.allow()) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    count.countDown();
                }).start();
                TimeUnit.MILLISECONDS.sleep(1000 / requestPerSec);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            count.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        double duration = (System.currentTimeMillis() - startTime) / 1000.0;
        System.out.println(totalReq + " requests processed in " + duration + " seconds. " + "Rate: " + (double) totalReq / duration + " per second");
    }
}
