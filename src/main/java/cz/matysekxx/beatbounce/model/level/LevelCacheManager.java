package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.model.entity.AbstractTile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the in-memory cache of generated level tiles.
 * Prevents re-analyzing and re-generating levels for the same audio file and speed
 * within a single session.
 *
 * @author Matysekxx
 */
public class LevelCacheManager {
    /**
     * In-memory cache to store generated tile lists for specific audio files and speed multipliers.
     */
    private static final Map<LevelCacheKey, List<AbstractTile>> levelCache = new ConcurrentHashMap<>();

    /**
     * Stores a list of tiles in the cache.
     *
     * @param key   the cache key
     * @param tiles the list of tiles to store
     */
    public static void put(LevelCacheKey key, List<AbstractTile> tiles) {
        levelCache.put(key, tiles);
    }

    /**
     * Checks if the cache contains a level for the given key.
     *
     * @param key the cache key
     * @return true if present
     */
    public static boolean contains(LevelCacheKey key) {
        return levelCache.containsKey(key);
    }

    /**
     * Retrieves a list of tiles from the cache.
     *
     * @param key the cache key
     * @return the list of tiles or null if not found
     */
    public static List<AbstractTile> get(LevelCacheKey key) {
        return levelCache.get(key);
    }

    /**
     * Clears all entries from the in-memory cache.
     */
    public static void clear() {
        levelCache.clear();
    }
}
