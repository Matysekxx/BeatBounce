package cz.matysekxx.beatbounce.model.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.gui.RenderCache;

import java.awt.*;

/**
 * The {@code MovingTile} class represents a tile that oscillates horizontally over time.
 * It extends {@link AbstractTile} and implements movement logic based on an amplitude and speed.
 */
public class MovingTile extends AbstractTile {
    private int startX;
    private int amplitude;
    private double speed;
    private double time;
    private float hueOffset;
    private Color baseColorAlpha220;

    /**
     * Default constructor for {@code MovingTile}.
     */
    protected MovingTile() {
        super();
    }

    /**
     * Constructs a new {@code MovingTile} with specified parameters.
     *
     * @param beatEvent the {@link BeatEvent} associated with this tile
     * @param x         the initial horizontal position
     * @param y         the vertical position
     * @param z         the depth position
     * @param amplitude the maximum horizontal displacement from the starting position
     * @param speed     the frequency of oscillation
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
        this.hueOffset = (float) ((z % 5000) / 5000.0);
        calculateColors();
    }

    /**
     * Calculates the colors used for rendering based on the hue offset.
     */
    private void calculateColors() {
        final float h = 0.1f + (hueOffset * 0.1f);
        final Color baseColor = Color.getHSBColor(h, 1.0f, 1.0f);
        this.baseColorAlpha220 = RenderCache.customColorWithAlpha(baseColor, 220);
    }

    /**
     * Updates the tile's position based on elapsed time.
     *
     * @param deltaTime the time elapsed since the last update
     */
    public void update(double deltaTime) {
        this.time += deltaTime;
        double phase = 0;
        if (amplitude > 0) {
            double ratio = startX / (double) amplitude;
            ratio = Math.max(-1.0, Math.min(1.0, ratio));
            phase = Math.asin(ratio);
        }

        final int newX = (int) (Math.sin(time * speed + phase) * amplitude);
        this.setLocation(newX, this.getY());
    }

    /**
     * Returns the starting horizontal position of the tile.
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
     * Returns the horizontal oscillation speed.
     *
     * @return the {@code speed} value
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Renders the 3D polygon of the tile onto the graphics context.
     * Includes neon effects if graphics quality is not set to LOW.
     *
     * @param g2d     the graphics context to paint on
     * @param polygon the polygon representing the tile's shape on screen
     */
    @Override
    public void paint3D(Graphics2D g2d, Polygon polygon) {
        if (!Settings.graphicsQuality.equals("LOW")) {
            final Color neonColor = new Color(255, 165, 0);

            g2d.setStroke(RenderCache.STROKE_6);
            g2d.setColor(RenderCache.customColorWithAlpha(neonColor, 60));
            g2d.drawPolygon(polygon);

            if (Settings.graphicsQuality.equals("HIGH")) {
                g2d.setStroke(RenderCache.STROKE_3);
                g2d.setColor(RenderCache.customColorWithAlpha(neonColor, 120));
                g2d.drawPolygon(polygon);
            }
        }

        g2d.setColor(baseColorAlpha220);
        g2d.fillPolygon(polygon);

        g2d.setStroke(RenderCache.STROKE_2);
        g2d.setColor(Color.WHITE);
        g2d.drawPolygon(polygon);

        g2d.setStroke(RenderCache.STROKE_1);
    }
}
