import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author lzn
 * @date 2023/04/05 17:56
 * @Description The implement of leaky bucket algorithm
 * <p>
 * the requests are processed at an approximately constant rate, which smooths out bursts of requests. Even though the incoming requests can be burst,
 * the outgoing responses are always at a same rate
 */
public class LeakyBucket extends RateLimiter {

    /**
     * Store packets in the bucket
     */
    private final Deque<Long> queue;

    /**
     * Timestamp of the last time the bucket was leaked
     */
    private long lastLeakTime;

    /**
     * Rate at which packets leak from the bucket per second
     */
    private final int leakRate;

    private int currentBucketSize;

    private ScheduledExecutorService executor;

    public LeakyBucket(int maxCapacity) {
        super(maxCapacity);
        //request/sec
        this.leakRate = 5;
        this.lastLeakTime = System.currentTimeMillis();
        this.currentBucketSize = 0;
        this.queue = new ArrayDeque<>();
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::leak, 0, 1000 / leakRate, TimeUnit.MILLISECONDS);
    }

    private synchronized void leak() {
        long now = System.currentTimeMillis();
        int packetsToRemove = (int) ((now - lastLeakTime) * leakRate / 1000);
        packetsToRemove = Math.min(packetsToRemove, currentBucketSize);
        currentBucketSize -= packetsToRemove;
        System.out.println("leak request: " + packetsToRemove);
        System.out.println("currentBucketSize: " + currentBucketSize);
        int index = 0;
        while (index < packetsToRemove) {
            queue.poll();
            index++;
        }
        lastLeakTime = now;
    }

    @Override
    public synchronized boolean allow() {
        if (currentBucketSize >= maxCapacity) {
            System.out.println("exceed the max capacity: " + queue.size());
            return false;
        }
        long now = System.currentTimeMillis();
        queue.add(now);
        currentBucketSize++;
        return true;
    }

    public void close() {
        executor.shutdown();
    }
}