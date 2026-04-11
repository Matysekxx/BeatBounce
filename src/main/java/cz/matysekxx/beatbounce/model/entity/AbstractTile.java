package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.gui.Paintable;
import cz.matysekxx.beatbounce.model.audio.BeatData;

import java.awt.*;

public abstract class AbstractTile extends Entity implements Paintable {
    private final BeatData beatData;
    private Rectangle rectangle;

    public AbstractTile(BeatData beatData, Point point) {
        super(point.x,  point.y);
        this.beatData = beatData;
    }

    public BeatData getBeatData() {
        return beatData;
    }
}
