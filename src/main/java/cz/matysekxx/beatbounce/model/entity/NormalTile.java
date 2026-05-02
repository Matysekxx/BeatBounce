package cz.matysekxx.beatbounce.model.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.WindowData;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class NormalTile extends AbstractTile {
    private float hueOffset;
    private List<Integer> fakeLaneOffsets;

    protected NormalTile() {
        super();
    }

    @JsonCreator
    public NormalTile(
            @JsonProperty("beatEvent") BeatEvent beatEvent,
            @JsonProperty("x") int x,
            @JsonProperty("y") int y,
            @JsonProperty("z") double z,
            @JsonProperty("fakeLaneOffsets") List<Integer> fakeLaneOffsets) {
        super(beatEvent, new Point(x, y), z, 50.0);
        this.hueOffset = (float) ((z % 5000) / 5000.0);
        this.fakeLaneOffsets = fakeLaneOffsets != null ? fakeLaneOffsets : Collections.emptyList();
    }

    public NormalTile(BeatEvent beatEvent, Point point, double z) {
        this(beatEvent, point.x, point.y, z, Collections.emptyList());
    }

    public NormalTile(BeatEvent beatEvent, Point point, double z, List<Integer> fakeLaneOffsets) {
        this(beatEvent, point.x, point.y, z, fakeLaneOffsets);
    }

    public List<Integer> getFakeLaneOffsets() {
        return fakeLaneOffsets;
    }

    @Override
    public void paint3D(Graphics2D g2d, Camera3D cam, WindowData windowData) {
        if (fakeLaneOffsets != null && !fakeLaneOffsets.isEmpty()) {
            final double scaleFront = cam.getScale(this.getZ());
            final double scaleBack = cam.getScale(this.getZ() + getLengthInZ());
            final int LANE_WIDTH = 120;

            for (int offset : fakeLaneOffsets) {
                final Polygon fakePoly = new Polygon(
                        createXPoints(cam, windowData.width(), scaleFront, scaleBack, this.getX() + (offset * LANE_WIDTH)),
                        createYPoints(cam, scaleFront, scaleBack, windowData.height() / 3),
                        4
                );
                drawFakePolygon(g2d, fakePoly);
            }
        }
        super.paint3D(g2d, cam, windowData);
    }

    private void drawFakePolygon(Graphics2D g2d, Polygon polygon) {
        final float h = 0.33f + (hueOffset * 0.1f);
        final float s = 0.8f;
        final float b = 0.4f;

        final Color baseColor = Color.getHSBColor(h, s, b);

        g2d.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 120));
        g2d.fillPolygon(polygon);

        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 180));
        g2d.drawPolygon(polygon);

        g2d.setStroke(new BasicStroke(1.0f));
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