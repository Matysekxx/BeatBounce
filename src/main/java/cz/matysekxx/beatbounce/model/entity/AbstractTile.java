package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.gui.Paintable;
import cz.matysekxx.beatbounce.event.BeatEvent;

import java.awt.*;

public abstract class AbstractTile extends Entity implements Paintable {
    private final BeatEvent beatEvent;
    private Rectangle rectangle;

    public AbstractTile(BeatEvent beatEvent, Point point) {
        super(point.x,  point.y);
        this.beatEvent = beatEvent;
    }

    public BeatEvent getBeatEvent() {
        return beatEvent;
    }
}
