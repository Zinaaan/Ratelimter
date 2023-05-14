import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author lzn
 * @date 2023/04/05 17:56
 * @Description The implement of sliding window log algorithm
 * <p>
 * Sliding window log algorithm keeps a log of request timestamps for each user.
 * When a request comes, we first pop all outdated timestamps before appending the new request time to the log.
 * Then we decide whether this request should be processed depending on whether the log size has exceeded the limit.
 */
public class SlidingWindowLog extends RateLimiter{

    private final Deque<Long> logQueue = new ArrayDeque<>();

    public SlidingWindowLog(int maxRequestPerSec) {
        super(maxRequestPerSec);
    }

    @Override
    public boolean allow() {
        synchronized (logQueue) {
            long currentTime = System.currentTimeMillis();
            while (!logQueue.isEmpty() && logQueue.getFirst() <= currentTime) {
                logQueue.poll();
            }
            logQueue.offer(currentTime);
            System.out.println(currentTime + ", log size = " + logQueue.size());
        }
        return logQueue.size() <= maxCapacity;
    }
}
