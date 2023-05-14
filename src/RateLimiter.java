/**
 * @author lzn
 * @date 2023/04/05 17:06
 * @Description
 */
public abstract class RateLimiter {

    /**
     * The maximum number of requests allowed within each time window
     */
    protected final int maxCapacity;

    /**
     * Constructor of the RateLimiter
     *
     * @param maxCapacity: the max capacity of the current rate limiter
     */
    public RateLimiter(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public abstract boolean allow();
}
