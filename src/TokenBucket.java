/**
 * @author lzn
 * @date 2023/04/05 17:32
 * @Description The implement of token bucket algorithm
 * <p>
 * Suppose there are a few tokens in a bucket. When a request comes, a token has to be taken from the bucket for it to be processed.
 * If there is no token available in the bucket, the request will be rejected and the requester has to retry later.
 * The token bucket is also refilled per time unit.
 * <p>
 * Instead of using a dedicated thread to refill the bucket with fixed amount tokens, we can refill it lazily when a request comes.
 * The amount of tokens to refill equals to (current time - last refill time) * max allowed tokens per time unit. The improved implementation:
 */
public class TokenBucket extends RateLimiter {

    private double currentBucketSize;

    private long lastRefillTime;

    //Token generation speed /s
    private final long refillRate;

    public TokenBucket(int maxCapacity) {
        super(maxCapacity);
        this.refillRate = 1;
        this.currentBucketSize = maxCapacity;
        lastRefillTime = System.currentTimeMillis();
    }

    @Override
    public synchronized boolean allow() {
        //Refill tokens when request comes
        refillTokens();
        if (currentBucketSize < 1) {
            return false;
        }
        currentBucketSize--;
        return true;
    }

    private void refillTokens() {
        long now = System.currentTimeMillis();
        double tokensToAdd = (now - lastRefillTime) * refillRate / 1000.0;
        System.out.println("tokensToAdd: " + tokensToAdd);
        currentBucketSize = Math.min(currentBucketSize + tokensToAdd, maxCapacity);
        System.out.println("currentBucketSize: " + currentBucketSize);
        lastRefillTime = now;
    }
}
