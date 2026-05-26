package cz.matysekxx.beatbounce.model.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.WindowData;

import java.awt.*;

/**
 * The {@code BreakableTile} class represents a tile that shatters after being landed on.
 * It extends {@link AbstractTile}.
 * Once broken, it performs a shattering animation and eventually disappears.
 */
public class BreakableTile extends AbstractTile {
    /**
     * Whether the tile has been broken.
     */
    private boolean broken = false;
    /**
     * Progress of the shattering animation (0.0 to 1.0).
     */
    private float breakProgress = 0.0f;

    /**
     * Default constructor for {@code BreakableTile}.
     */
    protected BreakableTile() {
        super();
    }

    /**
     * Constructs a new {@code BreakableTile} with specified parameters.
     *
     * @param beatEvent the {@link BeatEvent} associated with this tile
     * @param x         the horizontal position
     * @param y         the vertical position
     * @param z         the depth position
     */
    @JsonCreator
    public BreakableTile(
            @JsonProperty("beatEvent") BeatEvent beatEvent,
            @JsonProperty("x") int x,
            @JsonProperty("y") int y,
            @JsonProperty("z") double z) {
        super(beatEvent, new Point(x, y), z, 50.0);
    }

    /**
     * Returns whether the tile is broken.
     *
     * @return {@code true} if broken, {@code false} otherwise
     */
    public boolean isBroken() {
        return broken;
    }

    /**
     * Marks the tile as broken and starts the shattering animation.
     */
    public void breakTile() {
        this.broken = true;
        this.breakProgress = 0.0f;
    }

    /**
     * Updates the shattering animation progress.
     *
     * @param deltaTime the time elapsed since the last update
     */
    public void updateBreakAnimation(double deltaTime) {
        if (broken && breakProgress < 1.0f) {
            breakProgress = Math.min(1.0f, breakProgress + (float) (deltaTime * 3.0));
        }
    }

    /**
     * Returns the current progress of the shattering animation.
     *
     * @return the {@code breakProgress} value
     */
    public float getBreakProgress() {
        return breakProgress;
    }

    /**
     * Resets the tile's internal state, including breaking status.
     */
    @Override
    public void reset() {
        super.reset();
        this.broken = false;
        this.breakProgress = 0.0f;
    }

    /**
     * Renders the tile with a warning color and optional crack visual effects.
     *
     * @param g2d     the graphics context
     * @param polygon the projected 2D polygon of the tile's top face
     * @param scale   the scale factor at the front of the tile
     */
    @Override
    public void drawTile(Graphics2D g2d, Polygon polygon, double scale) {
        if (breakProgress >= 1.0f) return;
        final float alpha = 1.0f - breakProgress * 0.9f;
        final int alphaInt = (int) (200 * alpha);

        final Color dynamicWarning = getDynamicColor(0.9f, 0.8f, 0.95);

        if (!Settings.graphicsQuality.equals("LOW")) {
            g2d.setStroke(RenderCache.STROKE_8);
            g2d.setColor(RenderCache.customColorWithAlpha(dynamicWarning, (int) (50 * alpha)));
            g2d.drawPolygon(polygon);

            if (Settings.graphicsQuality.equals("HIGH")) {
                g2d.setStroke(RenderCache.STROKE_4);
                g2d.setColor(RenderCache.customColorWithAlpha(Color.ORANGE, (int) (100 * alpha)));
                g2d.drawPolygon(polygon);
            }

            g2d.setStroke(RenderCache.STROKE_2);
            g2d.setColor(RenderCache.customColorWithAlpha(Color.YELLOW, (int) (160 * alpha)));
            g2d.drawPolygon(polygon);
        }

        g2d.setColor(RenderCache.customColorWithAlpha(dynamicWarning, alphaInt));
        g2d.fillPolygon(polygon);

        if (broken && !Settings.graphicsQuality.equals("LOW")) {
            drawCracks(g2d, polygon, alpha);
        }

        g2d.setStroke(RenderCache.STROKE_1_5);
        g2d.setColor(RenderCache.whiteWithAlpha(alphaInt));
        g2d.drawPolygon(polygon);
        g2d.setStroke(RenderCache.STROKE_1);
    }

    /**
     * Renders visual cracks on the tile's surface when it is broken.
     *
     * @param g2d     the graphics context
     * @param polygon the tile's polygon
     * @param alpha   the current transparency multiplier
     */
    private void drawCracks(Graphics2D g2d, Polygon polygon, float alpha) {
        int minX = polygon.xpoints[0], maxX = polygon.xpoints[0];
        int minY = polygon.ypoints[0], maxY = polygon.ypoints[0];
        for (int i = 1; i < 4; i++) {
            minX = Math.min(minX, polygon.xpoints[i]);
            maxX = Math.max(maxX, polygon.xpoints[i]);
            minY = Math.min(minY, polygon.ypoints[i]);
            maxY = Math.max(maxY, polygon.ypoints[i]);
        }

        final int cx = (minX + maxX) / 2;
        final int cy = (minY + maxY) / 2;

        g2d.setStroke(RenderCache.STROKE_1);
        g2d.setColor(RenderCache.customColorWithAlpha(Color.RED, (int) (200 * alpha)));
        g2d.drawLine(cx, cy, maxX, minY);
        g2d.drawLine(cx, cy, minX, maxY);
        g2d.drawLine(cx, cy, maxX, maxY);
        g2d.drawLine(cx, cy, minX, minY);
    }

    /**
     * Renders the tile in a 3D perspective.
     *
     * @param g2d        the graphics context to paint on
     * @param cam        the {@link Camera3D} used for perspective calculations
     * @param windowData the {@link WindowData} containing screen dimensions
     */
    @Override
    public void render(Graphics2D g2d, Camera3D cam, WindowData windowData) {
        if (breakProgress >= 1.0f) return;
        super.render(g2d, cam, windowData);
    }
}
