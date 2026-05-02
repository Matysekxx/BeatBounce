package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.event.BeatEvent;

import java.awt.*;

public class TileFactory {

    private TileFactory() {
    }

    public static NormalTile createNormalTile(BeatEvent event, int x, int y, double z) {
        return new NormalTile(event, new Point(x, y), z);
    }

    public static NormalTile createFakeNormalTile(BeatEvent event, int x, int y, double z, int fakeDirection) {
        return new NormalTile(event, new Point(x, y), z, fakeDirection);
    }

    public static MovingTile createMovingTile(BeatEvent event, int x, int y, double z, int amplitude, double speed) {
        return new MovingTile(event, x, y, z, amplitude, speed);
    }
}