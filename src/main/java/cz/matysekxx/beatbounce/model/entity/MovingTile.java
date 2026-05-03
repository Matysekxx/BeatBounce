package cz.matysekxx.beatbounce.model.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.gui.RenderCache;

import java.awt.*;

public class MovingTile extends AbstractTile {
    private int startX;
    private int amplitude;
    private double speed;
    private double time;
    private float hueOffset;
    private Color baseColorAlpha220;

    protected MovingTile() {
        super();
    }

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

    private void calculateColors() {
        final float h = 0.1f + (hueOffset * 0.1f);
        final Color baseColor = Color.getHSBColor(h, 1.0f, 1.0f);
        this.baseColorAlpha220 = RenderCache.customColorWithAlpha(baseColor, 220);
    }

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

    public int getStartX() {
        return startX;
    }

    public int getAmplitude() {
        return amplitude;
    }

    public double getSpeed() {
        return speed;
    }

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
