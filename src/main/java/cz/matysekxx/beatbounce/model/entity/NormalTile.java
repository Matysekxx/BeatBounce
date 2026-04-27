package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.event.BeatEvent;

import java.awt.*;

public class NormalTile extends AbstractTile {
    private float hueOffset;
    protected NormalTile() {
        super();
    }
    
    public NormalTile(BeatEvent beatEvent, Point point, double z) {
        super(beatEvent, point, z, 50.0);
        this.hueOffset = (float) ((z % 5000) / 5000.0);
    }

    @Override
    public void paint3D(Graphics2D g2d, Polygon polygon) {
        final float h = 0.33f + (hueOffset * 0.1f);
        final float s = 1.0f;
        final float b = 1.0f;
        
        final Color baseColor = Color.getHSBColor(h, s, b);

        g2d.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 200));
        g2d.fillPolygon(polygon);

        g2d.setStroke(new BasicStroke(2.0f));
        g2d.setColor(Color.WHITE);
        g2d.drawPolygon(polygon);

        g2d.setStroke(new BasicStroke(1.0f));
    }
}
