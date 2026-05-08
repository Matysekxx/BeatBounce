package cz.matysekxx.beatbounce.model.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.RenderCache;

import java.awt.*;

/**
 * A smaller, thinner tile used to fill gaps in the level and provide visual guidance.
 */
public class SmallTile extends AbstractTile {

    @JsonCreator
    public SmallTile(
            @JsonProperty("beatEvent") BeatEvent beatEvent,
            @JsonProperty("x") int x,
            @JsonProperty("y") int y,
            @JsonProperty("z") double z) {
        super(beatEvent, new Point(x, y), z, 25.0);
    }

    @Override
    protected int[] createXPoints(Camera3D cam, int width, double scaleFront, double scaleBack, int targetX) {
        final double centerScreenFront = ((double) width / 2) + ((targetX - cam.getX()) * scaleFront);
        final double centerScreenBack = ((double) width / 2) + ((targetX - cam.getX()) * scaleBack);

        final double frontWidth = 60 * scaleFront;
        final double backWidth = 60 * scaleBack;

        return new int[]{
                (int) (centerScreenFront - frontWidth / 2),
                (int) (centerScreenFront + frontWidth / 2),
                (int) (centerScreenBack + backWidth / 2),
                (int) (centerScreenBack - backWidth / 2)
        };
    }

    @Override
    public void paint3D(Graphics2D g2d, Polygon polygon) {
        if (!Settings.graphicsQuality.equals("LOW")) {
            g2d.setStroke(RenderCache.STROKE_2);
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.drawPolygon(polygon);
        }

        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.fillPolygon(polygon);

        g2d.setStroke(RenderCache.STROKE_1);
        g2d.setColor(Color.WHITE);
        g2d.drawPolygon(polygon);
    }
}
