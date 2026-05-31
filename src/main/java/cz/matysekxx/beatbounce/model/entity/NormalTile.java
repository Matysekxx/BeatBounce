package cz.matysekxx.beatbounce.model.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.WindowData;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@code NormalTile} class represents the standard tile in BeatBounce.
 * It supports multiple segments (real and fake) across the road width.
 *
 * @author Matysekxx
 */
public class NormalTile extends AbstractTile {
    private final List<Integer> realLaneOffsets;
    private final List<Integer> fakeLaneOffsets;
    private final Polygon segmentScratchPolygon = new Polygon(new int[4], new int[4], 4);

    protected NormalTile() {
        super();
        this.realLaneOffsets = new ArrayList<>(List.of(0));
        this.fakeLaneOffsets = Collections.emptyList();
    }

    @JsonCreator
    public NormalTile(
            @JsonProperty("beatEvent") BeatEvent beatEvent,
            @JsonProperty("x") int x,
            @JsonProperty("y") int y,
            @JsonProperty("z") double z,
            @JsonProperty("realLaneOffsets") List<Integer> realLaneOffsets,
            @JsonProperty("fakeLaneOffsets") List<Integer> fakeLaneOffsets) {
        super(beatEvent, new Point(x, y), z, 50.0);
        this.realLaneOffsets = realLaneOffsets != null ? realLaneOffsets : new ArrayList<>(List.of(0));
        this.fakeLaneOffsets = fakeLaneOffsets != null ? fakeLaneOffsets : Collections.emptyList();
    }

    public NormalTile(BeatEvent beatEvent, Point point, double z, List<Integer> fakeLaneOffsets) {
        this(beatEvent, point.x, point.y, z, List.of(0), fakeLaneOffsets);
    }

    @Override
    public void render(Graphics2D g2d, Camera3D cam, WindowData windowData) {
        final double sF = cam.getScale(getZ()), sB = cam.getScale(getZ() + getLengthInZ());
        if (sF <= 0) return;

        g2d.setColor(RenderCache.customColorWithAlpha(getDynamicColor(0.8f, 0.4f, 0.0), 120));
        for (int offset : fakeLaneOffsets) {
            setupPolygon(cam, windowData, sF, sB, getX() + offset, 1.0, segmentScratchPolygon);
            g2d.fillPolygon(segmentScratchPolygon);
            if (!Settings.graphicsQuality.equals("LOW")) {
                g2d.setStroke(RenderCache.STROKE_1_5);
                g2d.setColor(RenderCache.customColorWithAlpha(getDynamicColor(0.8f, 0.5f, 0.0), 180));
                g2d.drawPolygon(segmentScratchPolygon);
            }
        }

        for (int offset : realLaneOffsets) {
            setupPolygon(cam, windowData, sF, sB, getX() + offset, 1.0, segmentScratchPolygon);
            drawTile(g2d, segmentScratchPolygon, sF);
        }
    }

    @Override
    public void drawTile(Graphics2D g2d, Polygon polygon, double scale) {
        final Color base = getDynamicColor(0.9f, 0.9f, 0.0);
        final Color tileColor = isActivated ? getDynamicColor(0.5f, 1.0f, 0.0) : base;

        if (!Settings.graphicsQuality.equals("LOW")) {
            g2d.setStroke(RenderCache.STROKE_8);
            g2d.setColor(RenderCache.customColorWithAlpha(tileColor, 40));
            g2d.drawPolygon(polygon);
        }

        g2d.setColor(RenderCache.customColorWithAlpha(tileColor, 220));
        g2d.fillPolygon(polygon);
        g2d.setStroke(RenderCache.STROKE_1_5);
        g2d.setColor(Color.WHITE);
        g2d.drawPolygon(polygon);
        g2d.setStroke(RenderCache.STROKE_1);
    }

    @Override
    public boolean isHit(double playerX, double playerRadius) {
        final double halfWidth = 60.0 + playerRadius;
        for (int offset : realLaneOffsets) {
            final double tx = getX() + offset;
            if (playerX >= tx - halfWidth && playerX <= tx + halfWidth) return true;
        }
        return false;
    }

    public List<Integer> getRealLaneOffsets() { return realLaneOffsets; }
    public List<Integer> getFakeLaneOffsets() { return fakeLaneOffsets; }
}
