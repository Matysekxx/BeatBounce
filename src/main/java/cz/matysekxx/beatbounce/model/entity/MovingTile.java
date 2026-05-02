package cz.matysekxx.beatbounce.model.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.matysekxx.beatbounce.event.BeatEvent;

import java.awt.*;

public class MovingTile extends AbstractTile {
    private int startX;
    private int amplitude;
    private double speed;
    private double time;

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
        g2d.setColor(Color.ORANGE);
        g2d.fillPolygon(polygon);
    }
}