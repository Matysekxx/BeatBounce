package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.event.BeatEvent;

import java.awt.*;
import java.util.List;

public class TileFactory {

    private TileFactory() {
    }

    public static NormalTile createNormalTile(BeatEvent event, int x, int y, double z) {
        return new NormalTile(event, new Point(x, y), z);
    }

    public static NormalTile createNormalTileWithFakes(BeatEvent event, int x, int y, double z, List<Integer> fakeLaneOffsets) {
        return new NormalTile(event, new Point(x, y), z, fakeLaneOffsets);
    }

    public static MovingTile createMovingTile(BeatEvent event, int x, int y, double z, int amplitude, double speed) {
        return new MovingTile(event, x, y, z, amplitude, speed);
    }
}