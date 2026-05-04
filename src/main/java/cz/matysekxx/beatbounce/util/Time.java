package cz.matysekxx.beatbounce.util;

import java.time.Duration;

/// Utility class providing static methods for time-related operations.
///
/// This class is designed to simplify common tasks like thread sleeping
/// by handling checked exceptions internally.
///
/// ### Example Usage:
/// ```java
/// // Sleep for a specific amount of milliseconds
/// Time.sleep(500);
///
/// // Sleep using Java Time API
/// Time.sleep(Duration.ofSeconds(2));
/// ```
public final class Time {

    /// Private constructor to prevent instantiation of this utility class.
    private Time() {
    }

    /// Causes the currently executing thread to sleep for the specified number of milliseconds.
    ///
    /// This method wraps [Thread.sleep] and handles [InterruptedException] by
    /// restoring the interrupted status of the current thread.
    ///
    /// @param millis The length of time to sleep in milliseconds.
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /// Causes the currently executing thread to sleep for the specified [Duration].
    ///
    /// This is a convenience method that converts the duration to milliseconds
    /// and calls [sleep(long)].
    ///
    /// @param duration The duration to sleep. Must not be null.
    /// @throws NullPointerException if the duration is null.
    public static void sleep(Duration duration) {
        sleep(duration.toMillis());
    }
}