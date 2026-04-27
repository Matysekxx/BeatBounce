package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.event.BeatEvent;

import java.awt.*;

public class NormalTile extends AbstractTile {
    public NormalTile(BeatEvent beatEvent, Point point, double z) {
        super(beatEvent, point, z);
    }

    @Override
    public void paint3D(Graphics2D g2d, Polygon polygon) {
        g2d.setColor(Color.GREEN);
        g2d.fillPolygon(polygon);
    }
}
