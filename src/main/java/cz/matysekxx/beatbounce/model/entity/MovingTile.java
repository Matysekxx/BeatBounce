package cz.matysekxx.beatbounce.model.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.gui.RenderCache;

import java.awt.*;

/**
 * The {@code MovingTile} class represents a tile that oscillates horizontally over time.
 * It extends {@link AbstractTile}.
 *
 * @author Matysekxx
 */
public class MovingTile extends AbstractTile {
    /**
     * The initial horizontal world coordinate.
     */
    private int startX;
    /**
     * The maximum horizontal displacement from the start position.
     */
    private int amplitude;
    /**
     * The speed of horizontal oscillation.
     */
    private double speed;
    /**
     * Internal accumulator for elapsed world time.
     */
    private double time;

    /**
     * Default constructor for {@code MovingTile}.
     */
    protected MovingTile() {
        super();
    }

    /**
     * Constructs a new {@code MovingTile} with specified oscillation parameters.
     *
     * @param beatEvent the {@link BeatEvent} associated with this tile
     * @param x         the initial horizontal position
     * @param y         the vertical position
     * @param z         the depth position
     * @param amplitude the maximum horizontal displacement
     * @param speed     the speed of oscillation
     */
    @JsonCreator
    public MovingTile(
            @JsonProperty("beatEvent") BeatEvent beatEvent,
            @JsonProperty("x") int x,
            @JsonProperty("y") int y,
            @JsonProperty("z") double z,
            @JsonProperty("amplitude") int amplitude,
            @JsonProperty("speed") double speed) {
        super(beatEvent, new Point(x, y), z, 50.0);
        this.startX = x;
        this.amplitude = amplitude;
        this.speed = speed;
        this.time = 0;
    }

    /**
     * Updates the tile's horizontal position based on the elapsed time.
     *
     * @param deltaTime the time elapsed since the last update
     */
    public void update(double deltaTime) {
        this.time += deltaTime;
        final int newX = (int) getXAt(this.time);
        this.setLocation(newX, this.getY());
    }

    /**
     * Sets the internal accumulator time and immediately updates the horizontal position.
     *
     * @param time the new time in seconds
     */
    public void setTime(double time) {
        this.time = time;
        final int newX = (int) getXAt(this.time);
        this.setLocation(newX, this.getY());
    }

    /**
     * Calculates the horizontal world coordinate of the tile at a specific time.
     * Uses a sine wave for smooth oscillation.
     *
     * @param timestamp the world time in seconds
     * @return the horizontal world coordinate at that time
     */
    @Override
    public double getXAt(double timestamp) {
        double phase = 0;
        if (amplitude > 0) {
            double ratio = startX / (double) amplitude;
            ratio = Math.clamp(ratio, -1.0, 1.0);
            phase = Math.asin(ratio);
        }
        return Math.sin(timestamp * speed + phase) * amplitude;
    }

    /**
     * Resets the tile's internal state, including resetting the oscillation time.
     */
    @Override
    public void reset() {
        super.reset();
        this.time = 0;
    }

    /**
     * Returns the initial horizontal position.
     *
     * @return the {@code startX} value
     */
    public int getStartX() {
        return startX;
    }

    /**
     * Returns the horizontal oscillation amplitude.
     *
     * @return the {@code amplitude} value
     */
    public int getAmplitude() {
        return amplitude;
    }

    /**
     * Returns the oscillation speed.
     *
     * @return the {@code speed} value
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Renders the tile with dynamic colors.
     *
     * @param g2d     the graphics context
     * @param polygon the projected 2D polygon of the tile's top face
     * @param scale   the scale factor at the front of the tile
     */
    @Override
    public void drawTile(Graphics2D g2d, Polygon polygon, double scale) {
        Color dynamicBase = getDynamicColor(1.0f, 1.0f, 0.5);
        Color dynamicLight = getDynamicColor(0.6f, 1.0f, 0.5);
        Color tileColor = isActivated ? dynamicLight : dynamicBase;
        if (!Settings.graphicsQuality.equals("LOW")) {
            g2d.setStroke(RenderCache.STROKE_8);
            g2d.setColor(RenderCache.customColorWithAlpha(tileColor, 50));
            g2d.drawPolygon(polygon);
        }

        g2d.setColor(RenderCache.customColorWithAlpha(tileColor, 220));
        g2d.fillPolygon(polygon);

        g2d.setStroke(RenderCache.STROKE_2);
        g2d.setColor(Color.WHITE);
        g2d.drawPolygon(polygon);

        g2d.setStroke(RenderCache.STROKE_1);
    }
}
