package cz.matysekxx.beatbounce.model.audio;

import cz.matysekxx.beatbounce.model.entity.TileType;

public class BeatData {
    private final double time;
    private final double salience;
    private final TileType type;

    public BeatData(double time, double salience, TileType type) {
        this.time = time;
        this.salience = salience;
        this.type = type;
    }

    public final double time() {
        return time;
    }

    public final double salience() {
        return salience;
    }

    public final TileType type() {
        return type;
    }
}