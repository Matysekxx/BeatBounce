package cz.matysekxx.beatbounce.model.audio;

/**
 * Immutable data model representing the detected tempo of an audio track.
 * <p>
 * Contains the primary BPM and the estimated timestamp of the first beat,
 * which together allow quantizing arbitrary timestamps to the beat grid.
 *
 * @param primaryBpm      detected beats-per-minute (60–240 range)
 * @param firstBeatOffset estimated time in seconds of the very first beat
 */
public record TempoMap(double primaryBpm, double firstBeatOffset) {

    /**
     * Fallback for tracks where BPM detection fails.
     */
    public static final TempoMap DEFAULT = new TempoMap(120.0, 0.0);

    /**
     * Returns the duration of one beat in seconds.
     *
     * @return beat interval in seconds
     */
    public double getBeatInterval() {
        return primaryBpm > 0 ? 60.0 / primaryBpm : 0.5;
    }

    /**
     * Snaps a raw timestamp to the nearest beat grid position.
     *
     * @param timestamp raw time in seconds
     * @return quantized time in seconds
     */
    public double quantizeToBeat(double timestamp) {
        final double interval = getBeatInterval();
        if (interval <= 0) return timestamp;
        final double adjusted = timestamp - firstBeatOffset;
        final double beat = Math.round(adjusted / interval);
        return firstBeatOffset + beat * interval;
    }

    /**
     * Returns whether the detected BPM falls within a musically plausible range.
     *
     * @return {@code true} if 60 ≤ bpm ≤ 240
     */
    public boolean isValidBpm() {
        return primaryBpm >= 60.0 && primaryBpm <= 240.0;
    }

    /**
     * Returns a human-readable BPM string rounded to one decimal place.
     *
     * @return formatted BPM string
     */
    public String formatBpm() {
        return String.format("%.1f BPM", primaryBpm);
    }
}
