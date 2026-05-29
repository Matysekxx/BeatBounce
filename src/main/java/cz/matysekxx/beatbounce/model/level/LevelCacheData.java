package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.model.entity.AbstractTile;

import java.util.List;

/**
 * Data structure for serialising and deserialising level data to/from disk cache.
 *
 * @param tiles              the list of tiles in the level
 * @param songName           the name of the song
 * @param artist             the artist of the song
 * @param stars              the difficulty rating (1-10)
 * @param cacheVersion       format version, used to detect stale caches
 * @param bpm                detected BPM of the track (informational)
 * @param totalBeatsDetected total number of raw beat events detected during analysis
 */
public record LevelCacheData(
        List<AbstractTile> tiles,
        String songName,
        String artist,
        int stars,
        int cacheVersion,
        double bpm,
        int totalBeatsDetected
) {
}
