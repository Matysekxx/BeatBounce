package cz.matysekxx.beatbounce.model.entity;

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

    public MovingTile(BeatEvent beatEvent, Point point, double z, int amplitude, double speed) {
        super(beatEvent, point, z, 50.0);
        this.startX = point.x;
        this.amplitude = amplitude;
        this.speed = speed;
        this.time = 0;
    }

    public void update(double deltaTime) {
        this.time += deltaTime;
        int newX = startX + (int) (Math.sin(time * speed) * amplitude);
        this.setLocation(newX, this.getY());
    }

    @Override
    public void paint3D(Graphics2D g2d, Polygon polygon) {
        g2d.setColor(Color.ORANGE);
        g2d.fillPolygon(polygon);
    }
}
