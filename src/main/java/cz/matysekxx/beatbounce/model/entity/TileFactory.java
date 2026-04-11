package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.event.BeatEvent;

import java.awt.Point;

public class TileFactory {

    private TileFactory() {}

    public static NormalTile createNormalTile(BeatEvent event, int x, int y) {
        return new NormalTile(event, new Point(x, y));
    }

    public static LongTile createLongTile(BeatEvent event, int x, int y, int widthInPixels) {
        return new LongTile(event, new Point(x, y), widthInPixels);
    }
}
