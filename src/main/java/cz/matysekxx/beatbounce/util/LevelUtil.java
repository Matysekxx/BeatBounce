package cz.matysekxx.beatbounce.util;

import cz.matysekxx.beatbounce.model.level.Level;

public class LevelUtil {
    public static String getCleanSongName(Level level) {
        final String name = level.songName();
        final int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}
