package cz.matysekxx.beatbounce.util;

import java.time.Duration;

public final class Time {
    private Time() {
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            System.err.println("Thread interrupted");
            Thread.currentThread().interrupt();
        }
    }

    public static void sleep(Duration duration) {
        sleep(duration.toMillis());
    }
}
