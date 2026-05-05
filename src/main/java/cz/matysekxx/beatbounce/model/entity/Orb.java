package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.Paintable;
import cz.matysekxx.beatbounce.gui.WindowData;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * The {@code Orb} class represents a collectible item in the game world.
 * Orbs have a 3D position (x, y, z) and a radius.
 * They implement {@link Paintable} for 3D rendering with pulsing glow effects.
 */
public class Orb implements Paintable {
    private final double x;
    private final double y;
    private final double z;
    private final double radius;
    private final Ellipse2D.Double glowEllipse;
    private final Ellipse2D.Double mainEllipse;
    private final Ellipse2D.Double highlightEllipse;
    private boolean collected;

    /**
     * Constructs a new {@code Orb} with specified coordinates and radius.
     *
     * @param x      the horizontal position
     * @param y      the vertical position
     * @param z      the depth position
     * @param radius the radius of the orb
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
     * Returns the horizontal position of the orb.
     *
     * @return the {@code x} coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the vertical position of the orb.
     *
     * @return the {@code y} coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Returns the depth position of the orb.
     *
     * @return the {@code z} coordinate
     */
    public double getZ() {
        return z;
    }

    /**
     * Returns the radius of the orb.
     *
     * @return the {@code radius} value
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Returns whether the orb has been collected by the player.
     *
     * @return {@code true} if collected, {@code false} otherwise
     */
    public boolean isCollected() {
        return collected;
    }

    /**
     * Sets the collection status of the orb.
     *
     * @param collected the new collection status
     */
    public void setCollected(boolean collected) {
        this.collected = collected;
    }

    /**
     * Renders the orb in 3D space.
     * Includes a pulsing glow effect if graphics quality is not set to LOW.
     *
     * @param g2d the graphics context to paint on
     * @param cam the {@link Camera3D} used for perspective calculations
     * @param win the {@link WindowData} containing screen dimensions
     */
    @Override
    public void paint3D(Graphics2D g2d, Camera3D cam, WindowData win) {
        if (collected) return;

        double scale = cam.getScale(z);
        if (scale <= 0) return;

        final int px = (int) (win.width() / 2.0 + (x - cam.getX()) * scale);
        final int py = (int) (win.height() / 3.0 + (y - cam.getY()) * scale);
        int pr = (int) (radius * scale);

        if (pr < 1) pr = 1;

        final long t = System.currentTimeMillis();
        final float pulse = (float) ((Math.sin(t / 200.0) + 1.0) / 2.0);

        if (!Settings.graphicsQuality.equals("LOW")) {
            final int glowR = (int) (pr * (1.5f + pulse * 0.5f));
            g2d.setPaint(new RadialGradientPaint(
                    px, py, glowR,
                    new float[]{0f, 1f},
                    new Color[]{new Color(255, 200, 0, 150), new Color(255, 200, 0, 0)}
            ));
            glowEllipse.setFrame(px - glowR, py - glowR, glowR * 2, glowR * 2);
            g2d.fill(glowEllipse);
        }

        g2d.setColor(new Color(255, 255, 100));
        mainEllipse.setFrame(px - pr, py - pr, pr * 2, pr * 2);
        g2d.fill(mainEllipse);

        g2d.setColor(Color.WHITE);
        final int highlightR = (int) (pr * 0.4);
        highlightEllipse.setFrame(px - pr * 0.3, py - pr * 0.3, highlightR, highlightR);
        g2d.fill(highlightEllipse);
    }

    /**
     * Implementation of {@link Paintable#paint3D(Graphics2D, Polygon)}.
     * Currently does nothing for {@code Orb}.
     *
     * @param g2d     the graphics context
     * @param polygon the polygon to paint
     */
    @Override
    public void paint3D(Graphics2D g2d, Polygon polygon) {

    }
}
