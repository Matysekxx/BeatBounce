package cz.matysekxx.beatbounce.model.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.Paintable;
import cz.matysekxx.beatbounce.gui.WindowData;

import java.awt.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = NormalTile.class, name = "normal"),
        @JsonSubTypes.Type(value = MovingTile.class, name = "moving"),
        @JsonSubTypes.Type(value = LongTile.class, name = "long")
})
public abstract class AbstractTile extends Entity implements Paintable {
    protected double z;
    protected double lengthInZ;
    private BeatEvent beatEvent;

    protected AbstractTile() {
        super(0, 0);
        this.lengthInZ = 50.0;
    }

    public AbstractTile(BeatEvent beatEvent, Point point, double z, double lengthInZ) {
        super(point.x, point.y);
        this.beatEvent = beatEvent;
        this.z = z;
        this.lengthInZ = lengthInZ;
    }

    public BeatEvent getBeatEvent() {
        return beatEvent;
    }

    public double getZ() {
        return z;
    }

    public double getLengthInZ() {
        return lengthInZ;
    }

    @Override
    public void paint3D(Graphics2D g2d, Camera3D cam, WindowData windowData) {
        final double scaleFront = cam.getScale(this.getZ());
        final double scaleBack = cam.getScale(this.getZ() + getLengthInZ());
        this.paint3D(g2d, new Polygon(
                createXPoints(
                        cam, windowData.width(), scaleFront, scaleBack),
                createYPoints(
                        cam, scaleFront, scaleBack, windowData.height() / 3),
                4
        ));
    }

    private int[] createYPoints(Camera3D cam, double scaleFront, double scaleBack, int horizonY) {
        final int screenYFront = (int) (horizonY + ((150 - cam.getY()) * scaleFront));
        final int screenYBack = (int) (horizonY + ((150 - cam.getY()) * scaleBack));
        return new int[]{
                screenYFront, screenYFront, screenYBack, screenYBack
        };
    }

    private int[] createXPoints(Camera3D cam, int width, double scaleFront, double scaleBack) {
        final double centerScreenFront = calculateCenterScreen(
                this, (int) cam.getX(), width, scaleFront);
        final double centerScreenBack = calculateCenterScreen(
                this, (int) cam.getX(), width, scaleBack);

        final double frontWidth = 100 * scaleFront;
        final double backWidth = 100 * scaleBack;

        return new int[]{
                (int) (centerScreenFront - frontWidth / 2),
                (int) (centerScreenFront + frontWidth / 2),
                (int) (centerScreenBack + backWidth / 2),
                (int) (centerScreenBack - backWidth / 2)
        };
    }

    private double calculateCenterScreen(AbstractTile tile, int camX, int width, double scale) {
        return ((double) width / 2) + ((tile.getX() - camX) * scale);
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }
}