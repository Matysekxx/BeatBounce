package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.model.entity.AbstractTile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LevelCacheManager {
    /**
     * In-memory cache to store generated tile lists for specific audio files and speed multipliers.
     */
    private static final Map<LevelCacheKey, List<AbstractTile>> levelCache = new ConcurrentHashMap<>();

    public static void put(LevelCacheKey key, List<AbstractTile> tiles) {
        levelCache.put(key, tiles);
    }

    public static boolean contains(LevelCacheKey key) {
        return levelCache.containsKey(key);
    }

    public static List<AbstractTile> get(LevelCacheKey key) {
        return levelCache.get(key);
    }

    public static void clear() {
        levelCache.clear();
    }
}
