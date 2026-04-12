package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.Paintable;
import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.gui.WindowData;

import java.awt.*;

public abstract class AbstractTile extends Entity implements Paintable {
    private final BeatEvent beatEvent;
    protected double z;

    public AbstractTile(BeatEvent beatEvent, Point point, double z) {
        super(point.x,  point.y);
        this.beatEvent = beatEvent;
        this.z = z;
    }

    public BeatEvent getBeatEvent() {
        return beatEvent;
    }

    public double getZ() {
        return z;
    }

    public double getLengthInZ() {
        return 0;
    }

    @Override
    public void paint3D(Graphics2D g2d, Camera3D cam, WindowData windowData) {
        final double distance = cam.getDistanceTo(this.getZ());
        final double length = this.getLengthInZ() > 0 ? this.getLengthInZ() : 50;
        final double tileDepth = distance + length;
        final int horizonY = windowData.height() / 3;

        if (tileDepth <= 0 || distance > 3000) return;

        final double scaleFront = cam.getScale(this.getZ());
        final double scaleBack = cam.getScale(this.getZ() + length);

        final int screenYFront = (int) (horizonY + ((150 - cam.getY()) * scaleFront));
        final int screenYBack = (int) (horizonY + ((150 - cam.getY()) * scaleBack));

        final double centerScreenFront = calculateCenterScreen(
                this, (int) cam.getX(), windowData.width(), scaleFront);
        final double centerScreenBack = calculateCenterScreen(
                this, (int) cam.getX(), windowData.width(), scaleBack);

        final double frontWidth = 100*scaleFront;
        final double backWidth = 100*scaleBack;

        final int[] pointsX = {
                (int) (centerScreenFront - frontWidth / 2),
                (int) (centerScreenFront + frontWidth / 2),
                (int) (centerScreenBack + backWidth / 2),
                (int) (centerScreenBack - backWidth / 2)
        };
        final int[] pointsY = {
                screenYFront, screenYFront, screenYBack, screenYBack
        };
        this.paint3D(g2d, new Polygon(
                pointsX,
                pointsY,
                4
        ));
    }
    private double calculateCenterScreen(AbstractTile tile,int camX ,int width ,double scale) {
        return ((double) width / 2) + ((tile.getX() - camX) * scale);
    }
}
