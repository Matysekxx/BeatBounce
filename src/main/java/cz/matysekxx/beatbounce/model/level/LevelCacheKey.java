package cz.matysekxx.beatbounce.model.level;

/**
 * Internal record used as a key for the memory cache.
 *
 * @param filePath        the absolute path to the audio file
 * @param speedMultiplier the speed multiplier used for generation
 */
public record LevelCacheKey(String filePath, float speedMultiplier) {
    /**
     * Factory method to create a new {@code LevelCacheKey}.
     *
     * @param filePath        absolute path to the audio file
     * @param speedMultiplier the speed multiplier
     * @return a new key instance
     */
    public static LevelCacheKey of(String filePath, float speedMultiplier) {
        return new LevelCacheKey(filePath, speedMultiplier);
    }
}