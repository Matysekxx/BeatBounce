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
 * A tile that shatters after the player successfully lands on it once.
 * <p>
 * Gameplay: the first collision is valid and awards bonus points. The tile
 * is then marked as {@code broken}. Any subsequent collision with a broken tile
 * causes the player to fall — the player must not step on it a second time.
 * <p>
 * Visual: renders with a fractured / cracked appearance (cyan cracks over a dark body)
 * and triggers a shatter animation via {@link #breakTile()}.
 */
public class BreakableTile extends AbstractTile {

    /**
     * Whether the tile has already been broken by the player.
     */
    private boolean broken = false;

    /**
     * Animation progress 0.0 = intact, 1.0 = fully shattered (invisible).
     */
    private float breakProgress = 0.0f;

    private Color baseColor;

    /**
     * Required for Jackson deserialization.
     */
    protected BreakableTile() {
        super();
    }

    /**
     * Constructs a new {@code BreakableTile}.
     *
     * @param beatEvent the associated beat event
     * @param x         world X position
     * @param y         world Y position
     * @param z         world Z position (depth)
     */
    @JsonCreator
    public BreakableTile(
            @JsonProperty("beatEvent") BeatEvent beatEvent,
            @JsonProperty("x") int x,
            @JsonProperty("y") int y,
            @JsonProperty("z") double z) {
        super(beatEvent, new Point(x, y), z, 50.0);
        calculateColors();
    }

    private void calculateColors() {
        final float h = (float) ((z % 5000) / 5000.0);
        this.baseColor = Color.getHSBColor(h, 0.9f, 0.7f);
    }

    /**
     * Returns whether this tile has already been broken.
     *
     * @return {@code true} if the tile is broken
     */
    public boolean isBroken() {
        return broken;
    }

    /**
     * Marks the tile as broken and begins the shatter animation.
     * Call this from {@code GameModel} upon the first valid collision.
     */
    public void breakTile() {
        this.broken = true;
        this.breakProgress = 0.0f;
    }

    /**
     * Advances the break animation by one tick.
     *
     * @param deltaTime elapsed seconds since last update
     */
    public void updateBreakAnimation(double deltaTime) {
        if (broken && breakProgress < 1.0f) {
            breakProgress = Math.min(1.0f, breakProgress + (float) (deltaTime * 3.0));
        }
    }

    /**
     * Returns the current break animation progress.
     *
     * @return value in [0.0, 1.0]
     */
    public float getBreakProgress() {
        return breakProgress;
    }

    @Override
    public void reset() {
        this.broken = false;
        this.breakProgress = 0.0f;
    }

    @Override
    public void paint3D(Graphics2D g2d, Polygon polygon, double scale) {
        if (breakProgress >= 1.0f) return;
        final float alpha = 1.0f - breakProgress * 0.9f;
        final int alphaInt = (int) (200 * alpha);

        if (!Settings.graphicsQuality.equals("LOW")) {
            g2d.setStroke(RenderCache.STROKE_8);
            g2d.setColor(new Color(255, 80, 0, (int) (50 * alpha)));
            g2d.drawPolygon(polygon);

            if (Settings.graphicsQuality.equals("HIGH")) {
                g2d.setStroke(RenderCache.STROKE_4);
                g2d.setColor(new Color(255, 140, 0, (int) (100 * alpha)));
                g2d.drawPolygon(polygon);
            }

            g2d.setStroke(RenderCache.STROKE_2);
            g2d.setColor(new Color(255, 200, 0, (int) (160 * alpha)));
            g2d.drawPolygon(polygon);
        }

        final Color fillColor = new Color(
                baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alphaInt);
        g2d.setColor(fillColor);
        g2d.fillPolygon(polygon);

        if (broken && !Settings.graphicsQuality.equals("LOW")) {
            drawCracks(g2d, polygon, alpha);
        }

        g2d.setStroke(RenderCache.STROKE_1_5);
        g2d.setColor(new Color(255, 255, 255, alphaInt));
        g2d.drawPolygon(polygon);
        g2d.setStroke(RenderCache.STROKE_1);
    }

    /**
     * Draws stylised crack lines across the broken tile surface.
     */
    private void drawCracks(Graphics2D g2d, Polygon polygon, float alpha) {
        final Rectangle b = polygon.getBounds();
        final int cx = b.x + b.width / 2;
        final int cy = b.y + b.height / 2;
        g2d.setStroke(RenderCache.STROKE_1);
        g2d.setColor(new Color(255, 80, 0, (int) (200 * alpha)));
        g2d.drawLine(cx, cy, b.x + b.width, b.y);
        g2d.drawLine(cx, cy, b.x, b.y + b.height);
        g2d.drawLine(cx, cy, b.x + b.width, b.y + b.height);
        g2d.drawLine(cx, cy, b.x, b.y);
    }

    @Override
    public void paint3D(Graphics2D g2d, Camera3D cam, WindowData windowData) {
        if (breakProgress >= 1.0f) return;
        super.paint3D(g2d, cam, windowData);
    }
}
