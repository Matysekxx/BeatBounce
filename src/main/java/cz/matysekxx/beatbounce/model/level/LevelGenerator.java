package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.model.audio.AudioAnalyzer;
import cz.matysekxx.beatbounce.model.audio.AudioData;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for generating game levels based on audio analysis.
 * It manages the high-level orchestration of level creation, caching,
 * and persistent storage of generated levels.
 * <p>
 * The actual procedural generation logic is encapsulated within the {@link GenerationContext} class.
 * </p>
 */
public class LevelGenerator {

    /**
     * In-memory cache to store generated tile lists for specific audio files and speed multipliers.
     */
    private static final Map<CacheKey, List<AbstractTile>> levelCache = new ConcurrentHashMap<>();

    /**
     * Returns the base movement speed along the Z-axis.
     * This value is fundamental for level generation and synchronization.
     *
     * @return the constant movement speed in world units per second (default is 500.0)
     */
    public static double getZSpeed() {
        return 500.0;
    }

    /**
     * Generates a level based on a list of events.
     *
     * @param events   the beat events to use for generation
     * @param songName the name of the song
     * @return a procedurally generated {@link Level}
     * @deprecated Use {@link #generateLevel(AudioData, float, int)} for better integration with caching.
     */
    @Deprecated
    public static Level generateLevel(Iterable<BeatEvent> events, String songName) {
        return new GenerationContext(events, songName, null, 1).generate();
    }

    /**
     * Generates a level for the specified audio data, speed, and difficulty.
     * <p>
     * This method follows a multi-tier loading strategy:
     * <ol>
     *     <li>Checks the in-memory {@link #levelCache}.</li>
     *     <li>Attempts to load from the local disk cache.</li>
     *     <li>Performs fresh audio analysis and procedural generation if no cache is found.</li>
     * </ol>
     *
     * @param audioData       the audio metadata and sample data to analyze
     * @param speedMultiplier the multiplier for playback speed (scales timestamps)
     * @param stars           the difficulty rating (1-5 stars)
     * @return a fully initialized {@link Level}
     */
    public static Level generateLevel(AudioData audioData, float speedMultiplier, int stars) {
        final CacheKey key = new CacheKey(audioData.file().getAbsolutePath(), speedMultiplier);
        if (levelCache.containsKey(key))
            return new Level(levelCache.get(key), audioData, audioData.file().getName(), stars);

        final Optional<LevelCacheData> cachedLevelOpt = Level.fromFile(audioData.file(), speedMultiplier);
        if (cachedLevelOpt.isPresent()) {
            final LevelCacheData diskCachedLevel = cachedLevelOpt.get();
            final Level loadedLevel = new Level(
                    diskCachedLevel.tiles(), audioData,
                    diskCachedLevel.songName(),
                    diskCachedLevel.stars() > 0 ? diskCachedLevel.stars() : stars
            );
            levelCache.put(key, loadedLevel.tiles());
            return loadedLevel;
        }

        final AudioAnalyzer audioAnalyzer = new AudioAnalyzer(audioData, speedMultiplier);
        final Level generatedLevel = new GenerationContext(
                audioAnalyzer.analyze(), audioData.file().getName(), audioData, stars
        ).generate();
        levelCache.put(key, generatedLevel.tiles());
        Level.toFile(generatedLevel, speedMultiplier);

        return generatedLevel;
    }

    /**
     * Internal record used as a key for the memory cache.
     *
     * @param filePath        the absolute path to the audio file
     * @param speedMultiplier the speed multiplier used for generation
     */
    private record CacheKey(String filePath, float speedMultiplier) {
    }
}
