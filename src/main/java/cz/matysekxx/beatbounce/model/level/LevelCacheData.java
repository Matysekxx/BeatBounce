package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.model.entity.AbstractTile;

import java.util.List;

/**
 * Data structure for serialising and deserialising level data to/from disk cache.
 *
 * @param tiles              the list of tiles in the level
 * @param songName           the name of the song
 * @param stars              the difficulty rating (1-10)
 * @param cacheVersion       format version, used to detect stale caches
 * @param bpm                detected BPM of the track (informational)
 * @param totalBeatsDetected total number of raw beat events detected during analysis
 */
public record LevelCacheData(
        List<AbstractTile> tiles,
        String songName,
        int stars,
        int cacheVersion,
        double bpm,
        int totalBeatsDetected
) {
    /**
     * Current cache format version. Increment whenever the tile model changes.
     */
    public static final int CURRENT_VERSION = 2;

    /**
     * Convenience constructor for levels that don't track BPM or beat counts.
     *
     * @param tiles    tile list
     * @param songName song name
     * @param stars    difficulty
     */
    public LevelCacheData(List<AbstractTile> tiles, String songName, int stars) {
        this(tiles, songName, stars, CURRENT_VERSION, 0.0, 0);
    }
}
