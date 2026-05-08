package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.model.audio.AudioAnalyzer;
import cz.matysekxx.beatbounce.model.audio.AudioData;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.TileFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for generating game levels based on audio analysis.
 * It handles beat detection, gap filling, and tile placement.
 */
public class LevelGenerator {
    private static final Map<CacheKey, List<AbstractTile>> levelCache = new ConcurrentHashMap<>();

    public static double getZSpeed() { return 500.0; }

    @Deprecated
    public static Level generateLevel(Iterable<BeatEvent> events, String songName) {
        return new GenerationContext(events, songName, null, 1).generate();
    }

    public static Level generateLevel(AudioData audioData, float speedMultiplier, int stars) {
        final CacheKey key = new CacheKey(audioData.file().getAbsolutePath(), speedMultiplier);

        if (levelCache.containsKey(key)) {
            return new Level(levelCache.get(key), audioData, audioData.file().getName(), stars);
        }

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
    private record CacheKey(String filePath, float speedMultiplier) {}
}
