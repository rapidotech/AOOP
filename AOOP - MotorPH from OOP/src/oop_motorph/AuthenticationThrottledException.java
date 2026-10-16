package oop_motorph;

public final class AuthenticationThrottledException extends RuntimeException {
    private final long retryAfterSeconds;
    public AuthenticationThrottledException(long seconds) {
        super("Sign-in was not completed. Try again in " + Math.max(1, seconds) + " seconds.");
        retryAfterSeconds = Math.max(1, seconds);
    }
    public long getRetryAfterSeconds() { return retryAfterSeconds; }
}
