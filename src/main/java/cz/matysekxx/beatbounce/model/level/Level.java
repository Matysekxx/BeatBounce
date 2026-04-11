package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.model.entity.AbstractTile;

import java.util.List;

public class Level {
    private final List<AbstractTile> tiles;
    private final String songName;

    public Level(List<AbstractTile> tiles, String songName) {
        this.tiles = tiles;
        this.songName = songName;
    }
}
