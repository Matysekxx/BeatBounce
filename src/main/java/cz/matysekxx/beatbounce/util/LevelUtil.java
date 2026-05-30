package cz.matysekxx.beatbounce.util;

import cz.matysekxx.beatbounce.model.level.Level;

/**
 * Utility class for level-related operations.
 *
 * @author Matysekxx
 */
public class LevelUtil {
    /**
     * Returns the clean song name from a level, without the file extension.
     *
     * @param level the level to get the song name from
     * @return the song name without the extension
     */
    public static String getCleanSongName(Level level) {
        final String name = level.songName();
        final int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}
