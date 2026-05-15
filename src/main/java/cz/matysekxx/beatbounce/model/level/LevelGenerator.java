package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.model.audio.AudioAnalyzer;
import cz.matysekxx.beatbounce.model.audio.AudioData;

import java.util.Optional;

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
     *     <li>Checks the in-memory levelCache.</li>
     *     <li>Attempts to load from the local disk cache.</li>
     *     <li>Performs fresh audio analysis and procedural generation if no cache is found.</li>
     * </ol>
     *
     * @param audioData       the audio metadata and sample data to analyze
     * @param speedMultiplier the multiplier for playback speed (scales timestamps)
     * @param stars           the difficulty rating (1-10 stars)
     * @return a fully initialized {@link Level}
     */
    public static Level generateLevel(AudioData audioData, float speedMultiplier, int stars) {
        final LevelCacheKey key = LevelCacheKey.of(audioData.file().getAbsolutePath(), speedMultiplier);
        if (LevelCacheManager.contains(key))
            return new Level(LevelCacheManager.get(key), audioData, audioData.file().getName(), stars);

        final Optional<LevelCacheData> cachedLevelOpt = LevelFileCache.fromFile(audioData.file(), speedMultiplier);
        if (cachedLevelOpt.isPresent()) {
            final LevelCacheData diskCachedLevel = cachedLevelOpt.get();
            final Level loadedLevel = new Level(
                    diskCachedLevel.tiles(), audioData,
                    diskCachedLevel.songName(),
                    diskCachedLevel.stars() > 0 ? diskCachedLevel.stars() : stars
            );
            LevelCacheManager.put(key, loadedLevel.tiles());
            return loadedLevel;
        }

        final AudioAnalyzer audioAnalyzer = new AudioAnalyzer(audioData, speedMultiplier);
        final Level generatedLevel = new GenerationContext(
                audioAnalyzer.analyze(), audioData.file().getName(), audioData, stars
        ).generate();
        LevelCacheManager.put(key, generatedLevel.tiles());
        LevelFileCache.toFile(generatedLevel, speedMultiplier);

        return generatedLevel;
    }
}