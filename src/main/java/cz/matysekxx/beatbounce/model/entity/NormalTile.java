package cz.matysekxx.beatbounce.model.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.WindowData;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class NormalTile extends AbstractTile {
    private float hueOffset;
    private List<Integer> fakeLaneOffsets;
    private Color baseColor;
    private Color baseColorAlpha120;
    private Color baseColorAlpha180;
    private Color baseColorAlpha230;

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
        calculateColors();
    }

    public NormalTile(BeatEvent beatEvent, Point point, double z) {
        this(beatEvent, point.x, point.y, z, Collections.emptyList());
    }

    public NormalTile(BeatEvent beatEvent, Point point, double z, List<Integer> fakeLaneOffsets) {
        this(beatEvent, point.x, point.y, z, fakeLaneOffsets);
    }

    private void calculateColors() {
        final float h = 0.33f + (hueOffset * 0.1f);
        this.baseColor = Color.getHSBColor(h, 1.0f, 1.0f);
        final Color baseDimColor = Color.getHSBColor(h, 0.8f, 0.4f);
        this.baseColorAlpha120 = RenderCache.customColorWithAlpha(baseDimColor, 120);
        this.baseColorAlpha180 = RenderCache.customColorWithAlpha(baseDimColor, 180);
        this.baseColorAlpha230 = RenderCache.customColorWithAlpha(baseColor, 230);
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
        g2d.setColor(baseColorAlpha120);
        g2d.fillPolygon(polygon);

        if (!Settings.graphicsQuality.equals("LOW")) {
            g2d.setStroke(RenderCache.STROKE_1_5);
            g2d.setColor(baseColorAlpha180);
            g2d.drawPolygon(polygon);
        }

        g2d.setStroke(RenderCache.STROKE_1);
    }

    @Override
    public void paint3D(Graphics2D g2d, Polygon polygon) {
        if (!Settings.graphicsQuality.equals("LOW")) {
            g2d.setStroke(RenderCache.STROKE_8);
            g2d.setColor(RenderCache.cyanWithAlpha(40));
            g2d.drawPolygon(polygon);

            if (Settings.graphicsQuality.equals("HIGH")) {
                g2d.setStroke(RenderCache.STROKE_4);
                g2d.setColor(RenderCache.cyanWithAlpha(100));
                g2d.drawPolygon(polygon);
            }

            g2d.setStroke(RenderCache.STROKE_2);
            g2d.setColor(RenderCache.cyanWithAlpha(180));
            g2d.drawPolygon(polygon);
        }

        g2d.setColor(baseColorAlpha230);
        g2d.fillPolygon(polygon);

        g2d.setStroke(RenderCache.STROKE_1_5);
        g2d.setColor(Color.WHITE);
        g2d.drawPolygon(polygon);

        g2d.setStroke(RenderCache.STROKE_1);
    }
}