package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.gui.Paintable;
import cz.matysekxx.beatbounce.event.BeatEvent;

import java.awt.*;

public abstract class AbstractTile extends Entity implements Paintable {
    private final BeatEvent beatEvent;
    protected double z;

    public AbstractTile(BeatEvent beatEvent, Point point, double z) {
        super(point.x,  point.y);
        this.beatEvent = beatEvent;
        this.z = z;
    }

    public BeatEvent getBeatEvent() {
        return beatEvent;
    }

    public double getZ() {
        return z;
    }

    public double getLengthInZ() {
        return 0;
    }

    public abstract void paint3D(Graphics2D g2d, Polygon polygon);
}
