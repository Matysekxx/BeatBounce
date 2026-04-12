package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.event.BeatEvent;

import java.awt.*;

public class LongTile extends AbstractTile {
    
    private final double lengthInZ;
    
    public LongTile(BeatEvent beatEvent, Point point, double z, double lengthInZ) {
        super(beatEvent, point, z);
        this.lengthInZ = lengthInZ;
    }

    @Override
    public double getLengthInZ() {
        return lengthInZ;
    }

    @Override
    public void paint(Graphics2D g2d) {
    }

    @Override
    public void paint3D(Graphics2D g2d, Polygon polygon) {
        g2d.setColor(Color.RED);
        g2d.fillPolygon(polygon);
    }
}
