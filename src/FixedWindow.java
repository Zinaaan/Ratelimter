import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author lzn
 * @date 2023/04/05 17:47
 * @Description The implement of fixed window algorithm
 * <p>
 * Fixed window counter algorithm divides the timeline into fixed-size windows and assign a counter to each window.
 * Each request, based on its arriving time, is mapped to a window.
 * If the counter in the window has reached the limit, requests falling in this window should be rejected.
 * For example, if we set the window size to 1 minute. Then the windows are [00:00, 00:01), [00:01, 00:02), ...[23:59, 00:00).
 */
public class FixedWindow extends RateLimiter {

    // TODO: Clean up stale entries
    //We can run a job to clean stale windows regularly. For instance, schedule a task running at 00:00:00 to remove all the entries created in previous day.
    private final Deque<Long> queue;

    /**
     * The size of each time window in milliseconds
     */
    private final long windowSizeInMillis;

    /**
     * Creates a new FixedWindow rate limiter with the specified capacity and time window size
     *
     * @param maxCapacity        the maximum number of requests allowed within each time window
     * @param windowSizeInMillis the size of each time window in milliseconds
     */
    public FixedWindow(int maxCapacity, int windowSizeInMillis) {
        super(maxCapacity);
        this.windowSizeInMillis = windowSizeInMillis;
        this.queue = new ArrayDeque<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean allow() {
        // calculate the current time window
        long now = System.currentTimeMillis();
        while (!queue.isEmpty() && now - queue.peek() > windowSizeInMillis) {
            queue.poll();
        }

        if (queue.size() < maxCapacity) {
            queue.offer(now);
            return true;
        }

        return false;
    }

}
