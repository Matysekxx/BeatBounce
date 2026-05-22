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

    /**
     * Scratch polygon for rendering thickness.
     */
    private final Polygon thicknessScratch = new Polygon(new int[4], new int[4], 4);

    @JsonCreator
    public SmallTile(
            @JsonProperty("beatEvent") BeatEvent beatEvent,
            @JsonProperty("x") int x,
            @JsonProperty("y") int y,
            @JsonProperty("z") double z) {
        super(beatEvent, new Point(x, y), z, 50.0);
    }

    @Override
    protected void fillXPoints(Camera3D cam, int width, double scaleFront, double scaleBack, int targetX, double pulseScale, int[] xpoints) {
        final double centerScreenFront = ((double) width / 2) + ((targetX - cam.getX()) * scaleFront);
        final double centerScreenBack = ((double) width / 2) + ((targetX - cam.getX()) * scaleBack);

        final double frontWidth = 50 * scaleFront * pulseScale;
        final double backWidth = 50 * scaleBack * pulseScale;

        xpoints[0] = (int) (centerScreenFront - frontWidth / 2);
        xpoints[1] = (int) (centerScreenFront + frontWidth / 2);
        xpoints[2] = (int) (centerScreenBack + backWidth / 2);
        xpoints[3] = (int) (centerScreenBack - backWidth / 2);
    }

    @Override
    public void drawTile(Graphics2D g2d, Polygon polygon, double scale) {
        if (!Settings.graphicsQuality.equals("LOW")) {
            final int thickness = (int) (6 * scale);
            if (thickness > 0) {
                for (int i = 0; i < 4; i++) {
                    thicknessScratch.xpoints[i] = polygon.xpoints[i];
                    thicknessScratch.ypoints[i] = polygon.ypoints[i] + thickness;
                }
                thicknessScratch.invalidate();
                g2d.setColor(RenderCache.customColorWithAlpha(new Color(20, 20, 30), 150));
                g2d.fillPolygon(thicknessScratch);
            }
        }

        g2d.setColor(isActivated ? RenderCache.customColorWithAlpha(new Color(180, 255, 255), 230) : RenderCache.whiteWithAlpha(180));
        g2d.fillPolygon(polygon);

        g2d.setStroke(RenderCache.STROKE_1);
        g2d.setColor(Color.WHITE);
        g2d.drawPolygon(polygon);
    }
}
