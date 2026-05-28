package cz.matysekxx.beatbounce.model.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.WindowData;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@code NormalTile} class represents the standard tile in BeatBounce.
 * It has been enhanced to support multiple segments (real and fake) and horizontal movement,
 * unifying the previous 'SlidingTile' functionality into the base tile type.
 */
public class NormalTile extends AbstractTile {
    /**
     * Relative horizontal offsets (in world units) for real (collidable) segments.
     */
    private final List<Integer> realLaneOffsets;
    /**
     * Relative horizontal offsets for fake (visual-only) segments.
     */
    private final List<Integer> fakeLaneOffsets;
    /**
     * Horizontal sliding speed in units per second.
     */
    private double speed = 0;
    /**
     * Scratch polygon for rendering multiple segments.
     */
    private final Polygon segmentScratchPolygon = new Polygon(new int[4], new int[4], 4);

    /**
     * Default constructor for {@code NormalTile}.
     */
    protected NormalTile() {
        super();
        this.realLaneOffsets = new ArrayList<>(List.of(0));
        this.fakeLaneOffsets = Collections.emptyList();
    }

    /**
     * Comprehensive constructor used by Jackson for deserialization.
     */
    @JsonCreator
    public NormalTile(
            @JsonProperty("beatEvent") BeatEvent beatEvent,
            @JsonProperty("x") int x,
            @JsonProperty("y") int y,
            @JsonProperty("z") double z,
            @JsonProperty("realLaneOffsets") List<Integer> realLaneOffsets,
            @JsonProperty("fakeLaneOffsets") List<Integer> fakeLaneOffsets,
            @JsonProperty("speed") double speed) {
        super(beatEvent, new Point(x, y), z, 50.0);
        this.realLaneOffsets = realLaneOffsets != null ? realLaneOffsets : new ArrayList<>(List.of(0));
        this.fakeLaneOffsets = fakeLaneOffsets != null ? fakeLaneOffsets : Collections.emptyList();
        this.speed = speed;
    }

    /**
     * Legacy constructor for simple tiles.
     */
    public NormalTile(BeatEvent beatEvent, Point point, double z, List<Integer> fakeLaneOffsets) {
        this(beatEvent, point.x, point.y, z, List.of(0), fakeLaneOffsets, 0);
    }

    /**
     * Updates the horizontal position if the tile has a speed set.
     * Implements wrapping logic to keep tiles within road bounds.
     */
    public void update(double deltaTime) {
        if (speed == 0) return;
        double newX = getX() + speed * deltaTime;
        final double roadWidth = RenderUtils.ROAD_WIDTH * 2.0;
        while (newX > RenderUtils.ROAD_WIDTH) newX -= roadWidth;
        while (newX < -RenderUtils.ROAD_WIDTH) newX += roadWidth;
        setLocation((int) newX, getY());
    }

    /**
     * Renders all segments of the tile in 3D perspective.
     */
    @Override
    public void render(Graphics2D g2d, Camera3D cam, WindowData windowData) {
        final double scaleFront = cam.getScale(this.getZ());
        final double scaleBack = cam.getScale(this.getZ() + getLengthInZ());
        if (scaleFront <= 0) return;

        final double roadWidth = RenderUtils.ROAD_WIDTH * 2.0;

        g2d.setColor(RenderCache.customColorWithAlpha(getDynamicColor(0.8f, 0.4f, 0.0), 120));
        for (int offset : fakeLaneOffsets) {
            prepareSegmentPolygon(cam, windowData, scaleFront, scaleBack, roadWidth, offset);
            g2d.fillPolygon(segmentScratchPolygon);
            if (!Settings.graphicsQuality.equals("LOW")) {
                g2d.setStroke(RenderCache.STROKE_1_5);
                g2d.setColor(RenderCache.customColorWithAlpha(getDynamicColor(0.8f, 0.5f, 0.0), 180));
                g2d.drawPolygon(segmentScratchPolygon);
            }
        }

        for (int offset : realLaneOffsets) {
            prepareSegmentPolygon(cam, windowData, scaleFront, scaleBack, roadWidth, offset);
            drawTile(g2d, segmentScratchPolygon, scaleFront);
        }
    }

    /**
     * Calculates the projected X position for a specific segment and populates the scratch polygon.
     * Implements wrapping logic to ensure segments stay within the road boundaries.
     */
    private void prepareSegmentPolygon(Camera3D cam, WindowData windowData, double scaleFront, double scaleBack, double roadWidth, int offset) {
        double segmentX = getX() + offset;
        while (segmentX > RenderUtils.ROAD_WIDTH) segmentX -= roadWidth;
        while (segmentX < -RenderUtils.ROAD_WIDTH) segmentX += roadWidth;

        setupPolygon(cam, windowData.width(), windowData.height() / 3, scaleFront, scaleBack, (int) segmentX, 1.0, segmentScratchPolygon);
    }

    @Override
    public void drawTile(Graphics2D g2d, Polygon polygon, double scale) {
        Color dynamicBase = getDynamicColor(0.9f, 0.9f, 0.0);
        Color dynamicLight = getDynamicColor(0.5f, 1.0f, 0.0);
        Color tileColor = isActivated ? dynamicLight : dynamicBase;

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

    /**
     * Checks if player radius overlaps any of the real segments.
     */
    @Override
    public boolean isHit(double playerX, double playerRadius) {
        final double halfWidth = 60.0 + playerRadius;
        final double roadWidth = RenderUtils.ROAD_WIDTH * 2.0;
        for (int offset : realLaneOffsets) {
            double tx = getX() + offset;
            while (tx > RenderUtils.ROAD_WIDTH) tx -= roadWidth;
            while (tx < -RenderUtils.ROAD_WIDTH) tx += roadWidth;

            if (playerX >= tx - halfWidth && playerX <= tx + halfWidth) return true;
        }
        return false;
    }

    public List<Integer> getRealLaneOffsets() {
        return realLaneOffsets;
    }

    public List<Integer> getFakeLaneOffsets() {
        return fakeLaneOffsets;
    }

    public double getSpeed() {
        return speed;
    }
}
