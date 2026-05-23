package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.gui.Camera3D;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * The {@code Orb} class represents a collectible item in the game world.
 * It is rendered as a glowing, levitating energetic sphere that the player can pick up to increase their score.
 * <p>
 * Key visual features:
 * <ul>
 *   <li><b>Levitation:</b> Smooth sinusoidal vertical oscillation to make the orb appear floating.</li>
 *   <li><b>Pulsing Glow:</b> A dynamic radial gradient that expands and contracts over time.</li>
 *   <li><b>Performance Optimization:</b> Caches {@link RadialGradientPaint} to avoid expensive object creation during frames where the orb hasn't moved relative to the screen.</li>
 * </ul>
 */
public class Orb {
    /**
     * Horizontal world coordinate (X).
     */
    private final double x;
    /**
     * Vertical world coordinate (Y) representing the base height before levitation.
     */
    private final double y;
    /**
     * Depth world coordinate (Z).
     */
    private final double z;
    /**
     * Physical radius of the orb in world units.
     */
    private final double radius;

    /**
     * Reusable ellipse object for rendering the outer glow effect.
     */
    private final Ellipse2D.Double glowEllipse;
    /**
     * Reusable ellipse object for rendering the main solid body of the orb.
     */
    private final Ellipse2D.Double mainEllipse;
    /**
     * Reusable ellipse object for rendering the specular highlight (shine).
     */
    private final Ellipse2D.Double highlightEllipse;

    /**
     * Flag indicating if the orb has been picked up by the player.
     * Collected orbs are no longer rendered or eligible for collision.
     */
    private boolean collected;

    /**
     * Cached gradient paint for the glow effect. 
     * Recalculated only when the orb's screen position or pulse size changes.
     */
    private RadialGradientPaint cachedPaint;

    /**
     * Last projected screen X-coordinate used for cache validation.
     */
    private int lastPx;
    /**
     * Last projected screen Y-coordinate used for cache validation.
     */
    private int lastPy;
    /**
     * Last calculated glow radius used for cache validation.
     */
    private int lastGlowR;

    /**
     * Constructs a new {@code Orb} at the specified 3D world coordinates.
     *
     * @param x      the horizontal world position
     * @param y      the vertical world position (base height)
     * @param z      the depth world position
     * @param radius the physical radius of the orb
     */
    public Orb(double x, double y, double z, double radius) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.collected = false;
        this.glowEllipse = new Ellipse2D.Double();
        this.mainEllipse = new Ellipse2D.Double();
        this.highlightEllipse = new Ellipse2D.Double();
    }

    /**
     * Returns the horizontal world position.
     *
     * @return the {@code x} coordinate
     */
    public double getX() { return x; }

    /**
     * Returns the base vertical world position.
     *
     * @return the {@code y} coordinate
     */
    public double getY() { return y; }

    /**
     * Returns the depth world position.
     *
     * @return the {@code z} coordinate
     */
    public double getZ() { return z; }

    /**
     * Returns the physical world radius.
     *
     * @return the {@code radius} value
     */
    public double getRadius() { return radius; }

    /**
     * Returns whether the orb has been collected.
     *
     * @return {@code true} if collected, {@code false} otherwise
     */
    public boolean isCollected() { return collected; }

    /**
     * Sets the collection status of the orb.
     *
     * @param collected the new collection status
     */
    public void setCollected(boolean collected) { this.collected = collected; }

    /**
     * Renders the orb in 3D perspective with levitation and pulsing effects.
     * <p>
     * The rendering process consists of three layers:
     * <ol>
     *   <li><b>Glow Layer:</b> A large, pulsing radial gradient (skipped in LOW graphics quality).</li>
     *   <li><b>Body Layer:</b> A solid white circle representing the core.</li>
     *   <li><b>Highlight Layer:</b> A small offset shine to give a sense of volume.</li>
     * </ol>
     *
     * @param g2d  the graphics context to paint on
     * @param cam  the {@link Camera3D} used for projection
     * @param win  the window metadata for screen dimension access
     */
    public void render(Graphics2D g2d, Camera3D cam, cz.matysekxx.beatbounce.gui.WindowData win) {
        if (collected) return;

        final double scale = cam.getScale(z);
        if (scale <= 0) return;

        final long t = System.currentTimeMillis();
        final double levitationOffset = Math.sin(t / 200.0) * 12.0;
        final int px = (int) (win.width() / 2.0 + (x - cam.getX()) * scale);
        final int py = (int) (win.height() / 3.0 + ((y + levitationOffset) - cam.getY()) * scale);
        int pr = (int) (radius * scale);

        if (pr < 1) pr = 1;

        if (!Settings.graphicsQuality.equals("LOW")) {
            final float pulse = (float) ((Math.sin(t / 120.0) + 1.0) / 2.0);
            final int glowR = (int) (pr * (1.8f + pulse * 0.6f));

            final Color glowStart = new Color(255, 170, 0, 160);
            final Color glowEnd = new Color(255, 170, 0, 0);
            if (cachedPaint == null || lastPx != px || lastPy != py || lastGlowR != glowR) {
                cachedPaint = new RadialGradientPaint(
                        px, py, glowR,
                        new float[]{0f, 1f},
                        new Color[]{glowStart, glowEnd}
                );
                lastPx = px;
                lastPy = py;
                lastGlowR = glowR;
            }
            g2d.setPaint(cachedPaint);
            glowEllipse.setFrame(px - glowR, py - glowR, glowR * 2, glowR * 2);
            g2d.fill(glowEllipse);
        }

        g2d.setColor(Color.WHITE);
        mainEllipse.setFrame(px - pr, py - pr, pr * 2, pr * 2);
        g2d.fill(mainEllipse);

        if (!Settings.graphicsQuality.equals("LOW")) {
            g2d.setColor(new Color(255, 255, 200, 200));
            final int highlightR = (int) (pr * 0.5);
            highlightEllipse.setFrame(px - pr * 0.2, py - pr * 0.2, highlightR, highlightR);
            g2d.fill(highlightEllipse);
        }
    }
}
