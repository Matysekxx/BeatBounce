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
 * A tile that temporarily modifies the game's scroll speed when the player lands on it.
 * <p>
 * Gameplay: when the player touches a {@code SpeedTile}, {@code GameModel} reads the
 * {@link #getSpeedMultiplier()} and applies it as a temporary speed factor for a fixed
 * duration (e.g. 3 seconds). Values > 1.0 accelerate the track; values < 1.0 slow it.
 * <p>
 * Visual: renders in electric-green / lime hues to signal a power-up, with a pulsing
 * glow animation.
 */
public class SpeedTile extends AbstractTile {

    /**
     * The speed multiplier applied to the game when this tile is activated.
     * Typical range: 0.5 (slow-mo) – 2.0 (double speed).
     */
    private float speedMultiplier;

    /**
     * {@code true} after the player has triggered the speed effect.
     */
    private boolean activated = false;

    private Color baseColor;
    private Color glowColor;

    /**
     * Required for Jackson deserialization.
     */
    protected SpeedTile() {
        super();
    }

    /**
     * Constructs a new {@code SpeedTile}.
     *
     * @param beatEvent       the associated beat event
     * @param x               world X position
     * @param y               world Y position
     * @param z               world Z position (depth)
     * @param speedMultiplier multiplier applied to game speed on activation
     */
    @JsonCreator
    public SpeedTile(
            @JsonProperty("beatEvent") BeatEvent beatEvent,
            @JsonProperty("x") int x,
            @JsonProperty("y") int y,
            @JsonProperty("z") double z,
            @JsonProperty("speedMultiplier") float speedMultiplier) {
        super(beatEvent, new Point(x, y), z, 50.0);
        this.speedMultiplier = speedMultiplier;
        calculateColors();
    }

    private void calculateColors() {
        if (speedMultiplier >= 1.0f) {
            this.baseColor = new Color(255, 200, 0, 230);
            this.glowColor = new Color(255, 150, 0, 100);
        } else {
            this.baseColor = new Color(200, 0, 255, 230);
            this.glowColor = new Color(150, 0, 255, 100);
        }
    }

    /**
     * Returns the speed multiplier this tile applies to the game.
     *
     * @return speed multiplier (e.g. 1.5 for +50 % speed)
     */
    public float getSpeedMultiplier() {
        return speedMultiplier;
    }

    /**
     * Returns whether the speed effect has already been triggered.
     *
     * @return {@code true} if activated
     */
    public boolean isActivated() {
        return activated;
    }

    /**
     * Marks this tile as having already triggered its speed effect.
     * Call once from {@code GameModel} upon the first collision.
     */
    public void activate() {
        this.activated = true;
    }

    @Override
    public void reset() {
        this.activated = false;
    }

    @Override
    public void paint3D(Graphics2D g2d, Polygon polygon) {
        final long t = System.currentTimeMillis();
        final float pulse = (float) ((Math.sin(t / 120.0) + 1.0) / 2.0);

        if (!Settings.graphicsQuality.equals("LOW")) {
            g2d.setStroke(RenderCache.STROKE_8);
            g2d.setColor(new Color(
                    glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(),
                    (int) (40 + pulse * 80)));
            g2d.drawPolygon(polygon);

            if (Settings.graphicsQuality.equals("HIGH")) {
                g2d.setStroke(RenderCache.STROKE_4);
                g2d.setColor(new Color(
                        glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(),
                        (int) (100 + pulse * 100)));
                g2d.drawPolygon(polygon);
            }

            g2d.setStroke(RenderCache.STROKE_2);
            g2d.setColor(new Color(
                    glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(),
                    (int) (180 + pulse * 75)));
            g2d.drawPolygon(polygon);
        }

        g2d.setColor(baseColor);
        g2d.fillPolygon(polygon);
        if (!Settings.graphicsQuality.equals("LOW")) {
            drawSpeedSymbol(g2d, polygon);
        }
        g2d.setStroke(RenderCache.STROKE_1_5);
        g2d.setColor(Color.WHITE);
        g2d.drawPolygon(polygon);
        g2d.setStroke(RenderCache.STROKE_1);
    }

    /**
     * Draws a simple arrow or chevron symbol indicating speed direction.
     */
    private void drawSpeedSymbol(Graphics2D g2d, Polygon polygon) {
        final Rectangle b = polygon.getBounds();
        if (b.width < 10 || b.height < 6) return;
        final int cx = b.x + b.width / 2;
        final int cy = b.y + b.height / 2;
        final int aw = b.width / 4;
        final int ah = b.height / 3;
        g2d.setStroke(RenderCache.STROKE_2);
        g2d.setColor(new Color(255, 255, 255, 180));
        if (speedMultiplier >= 1.0f) {
            g2d.drawLine(cx - aw, cy, cx + aw, cy);
            g2d.drawLine(cx + aw, cy, cx, cy - ah);
            g2d.drawLine(cx + aw, cy, cx, cy + ah);
        } else {
            g2d.drawLine(cx + aw, cy, cx - aw, cy);
            g2d.drawLine(cx - aw, cy, cx, cy - ah);
            g2d.drawLine(cx - aw, cy, cx, cy + ah);
        }
        g2d.setStroke(RenderCache.STROKE_1);
    }

    @Override
    public void paint3D(Graphics2D g2d, Camera3D cam, WindowData windowData) {
        super.paint3D(g2d, cam, windowData);
    }
}
