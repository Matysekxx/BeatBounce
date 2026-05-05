package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.model.entity.AbstractTile;

import java.util.List;

/**
 * Data structure used for serializing and deserializing level data to/from disk cache.
 *
 * @param tiles    the list of tiles in the level
 * @param songName the name of the song
 * @param stars    the difficulty rating in stars
 */
public record LevelCacheData(List<AbstractTile> tiles, String songName, int stars) {
}