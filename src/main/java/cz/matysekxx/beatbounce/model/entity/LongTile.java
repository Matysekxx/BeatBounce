package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.event.BeatEvent;

import java.awt.*;

public class LongTile extends AbstractTile {
    protected LongTile() {
        super();
    }

    public LongTile(BeatEvent beatEvent, Point point, double z, double lengthInZ) {
        super(beatEvent, point, z, lengthInZ);
    }

    @Override
    public void paint3D(Graphics2D g2d, Polygon polygon) {
        g2d.setColor(Color.CYAN);
        g2d.fillPolygon(polygon);
    }
}
