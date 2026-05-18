package cz.matysekxx.beatbounce.util;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

/**
 * Utility class providing static methods for time-related operations.
 * <p>
 * This class is designed to simplify common tasks like thread sleeping
 * by handling checked exceptions internally.
 * </p>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * // Sleep for a specific amount of milliseconds
 * Time.sleep(500);
 *
 * // Sleep using Java Time API
 * Time.sleep(Duration.ofSeconds(2));
 * }</pre>
 */
public final class Time {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Time() {
    }

    /**
     * Causes the currently executing thread to sleep for the specified number of milliseconds.
     * <p>
     * This method wraps {@link Thread#sleep(long)} and handles {@link InterruptedException} by
     * restoring the interrupted status of the current thread.
     * </p>
     *
     * @param millis The length of time to sleep in milliseconds.
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Causes the currently executing thread to sleep for the specified {@link Duration}.
     * <p>
     * This is a convenience method that converts the duration to milliseconds
     * and calls {@link #sleep(long)}.
     * </p>
     *
     * @param duration The duration to sleep. Must not be null.
     * @throws NullPointerException if the duration is null.
     */
    public static void sleep(Duration duration) {
        sleep(duration.toMillis());
    }

    /**
     * Delays the current thread to maintain a target frame rate.
     *
     * @param optimalTimeNanos the target duration for a single frame in nanoseconds
     * @param loopStartTime    the time when the loop iteration started in nanoseconds
     */
    public static void delay(long optimalTimeNanos, long loopStartTime) {
        final long timeTakenNanos = System.nanoTime() - loopStartTime;
        final long sleepNanos = optimalTimeNanos - timeTakenNanos;

        if (sleepNanos > 0) {
            final long targetTime = System.nanoTime() + sleepNanos;
            if (sleepNanos > 2_000_000L) {
                LockSupport.parkNanos(sleepNanos - 2_000_000L);
            }
            while (System.nanoTime() < targetTime) ;
        }
    }
}
