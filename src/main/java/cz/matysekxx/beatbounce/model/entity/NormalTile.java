package cz.matysekxx.beatbounce.model.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.WindowData;

import java.awt.*;

public class NormalTile extends AbstractTile {
    private float hueOffset;
    private int fakeDirection;

    protected NormalTile() {
        super();
    }

    @JsonCreator
    public NormalTile(
            @JsonProperty("beatEvent") BeatEvent beatEvent,
            @JsonProperty("x") int x,
            @JsonProperty("y") int y,
            @JsonProperty("z") double z,
            @JsonProperty("fakeDirection") int fakeDirection) {
        super(beatEvent, new Point(x, y), z, 50.0);
        this.hueOffset = (float) ((z % 5000) / 5000.0);
        this.fakeDirection = fakeDirection;
    }

    public NormalTile(BeatEvent beatEvent, Point point, double z) {
        this(beatEvent, point.x, point.y, z, 0);
    }

    public NormalTile(BeatEvent beatEvent, Point point, double z, int fakeDirection) {
        this(beatEvent, point.x, point.y, z, fakeDirection);
    }

    public int getFakeDirection() {
        return fakeDirection;
    }

    @Override
    public void paint3D(Graphics2D g2d, Camera3D cam, WindowData windowData) {
        super.paint3D(g2d, cam, windowData);
        if (fakeDirection != 0) {
            final double scaleFront = cam.getScale(this.getZ());
            final double scaleBack = cam.getScale(this.getZ() + getLengthInZ());
            final int LANE_WIDTH = 120;

            if (fakeDirection == -1 || fakeDirection == 2) {
                final Polygon fakePolyLeft = new Polygon(
                        createXPoints(cam, windowData.width(), scaleFront, scaleBack, this.getX() - LANE_WIDTH),
                        createYPoints(cam, scaleFront, scaleBack, windowData.height() / 3),
                        4
                );
                drawFakePolygon(g2d, fakePolyLeft, -0.15f);
            }

            if (fakeDirection == 1 || fakeDirection == 2) {
                final Polygon fakePolyRight = new Polygon(
                        createXPoints(cam, windowData.width(), scaleFront, scaleBack, this.getX() + LANE_WIDTH),
                        createYPoints(cam, scaleFront, scaleBack, windowData.height() / 3),
                        4
                );
                drawFakePolygon(g2d, fakePolyRight, 0.15f);
            }
        }
    }

    private void drawFakePolygon(Graphics2D g2d, Polygon polygon, float extraHueOffset) {
        float h = 0.33f + (hueOffset * 0.1f) + extraHueOffset;
        h = h - (float) Math.floor(h);

        final float s = 0.6f;
        final float b = 0.8f;

        final Color baseColor = Color.getHSBColor(h, s, b);
        final Color neonColor = Color.getHSBColor(h, 1.0f, 1.0f);

        g2d.setStroke(new BasicStroke(4.0f));
        g2d.setColor(new Color(neonColor.getRed(), neonColor.getGreen(), neonColor.getBlue(), 30));
        g2d.drawPolygon(polygon);

        g2d.setStroke(new BasicStroke(2.0f));
        g2d.setColor(new Color(neonColor.getRed(), neonColor.getGreen(), neonColor.getBlue(), 60));
        g2d.drawPolygon(polygon);

        g2d.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 70));
        g2d.fillPolygon(polygon);

        g2d.setStroke(new BasicStroke(1.0f));
        g2d.setColor(new Color(255, 255, 255, 120));
        g2d.drawPolygon(polygon);
    }

    @Override
    public void paint3D(Graphics2D g2d, Polygon polygon) {
        final float h = 0.33f + (hueOffset * 0.1f);
        final float s = 1.0f;
        final float b = 1.0f;

        final Color baseColor = Color.getHSBColor(h, s, b);
        final Color neonColor = new Color(0, 255, 255);

        g2d.setStroke(new BasicStroke(8.0f));
        g2d.setColor(new Color(neonColor.getRed(), neonColor.getGreen(), neonColor.getBlue(), 40));
        g2d.drawPolygon(polygon);

        g2d.setStroke(new BasicStroke(4.0f));
        g2d.setColor(new Color(neonColor.getRed(), neonColor.getGreen(), neonColor.getBlue(), 100));
        g2d.drawPolygon(polygon);
        
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.setColor(new Color(neonColor.getRed(), neonColor.getGreen(), neonColor.getBlue(), 180));
        g2d.drawPolygon(polygon);

        g2d.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 230));
        g2d.fillPolygon(polygon);

        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(Color.WHITE);
        g2d.drawPolygon(polygon);

        g2d.setStroke(new BasicStroke(1.0f));
    }
}