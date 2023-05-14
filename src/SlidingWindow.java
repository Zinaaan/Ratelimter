import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lzn
 * @date 2023/03/21 17:00
 * @Description
 */
public class SlidingWindow extends RateLimiter {

    /**
     * key: timestamp, value: request times
     */
    private final ConcurrentHashMap<Long, AtomicInteger> windows = new ConcurrentHashMap<>();

    /**
     * The size of each time window in milliseconds
     */
    private final long windowSizeInMillis;

    public SlidingWindow(int maxRequestPerSec, int windowSizeInMillis) {
        super(maxRequestPerSec);
        this.windowSizeInMillis = windowSizeInMillis;
    }

    @Override
    public boolean allow() {
        long currentTime = System.currentTimeMillis();
        long curWindowKey = currentTime / windowSizeInMillis * 1000;
        windows.putIfAbsent(curWindowKey, new AtomicInteger(0));
        long preWindowKey = curWindowKey - windowSizeInMillis;
        AtomicInteger preCount = windows.get(preWindowKey);
        if(preCount == null){
            return windows.get(curWindowKey).incrementAndGet() <= maxCapacity;
        }
        double preWeight = 1 - (currentTime - curWindowKey) / 1000.0;
        long count = (long) (preCount.get() * preWeight + windows.get(curWindowKey).incrementAndGet());
        return count <= maxCapacity;
    }
}
