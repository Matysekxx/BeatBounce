package cz.matysekxx.beatbounce.model.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.RenderCache;

import java.awt.*;

/**
 * The {@code SmallTile} class represents a narrower tile variant in the game.
 * It requires more precise movement from the player to land on.
 * It extends {@link AbstractTile}.
 *
 * @author Matysekxx
 */
public class SmallTile extends AbstractTile {
    /**
     * Scratch polygon for rendering the thickness (3D effect) of the tile.
     */
    private final Polygon thicknessScratch = new Polygon(new int[4], new int[4], 4);

    /**
     * Constructs a new {@code SmallTile} with specified parameters.
     *
     * @param beatEvent the {@link BeatEvent} associated with this tile
     * @param x         the horizontal position
     * @param y         the vertical position
     * @param z         the depth position
     */
    @JsonCreator
    public SmallTile(
            @JsonProperty("beatEvent") BeatEvent beatEvent,
            @JsonProperty("x") int x,
            @JsonProperty("y") int y,
            @JsonProperty("z") double z) {
        super(beatEvent, new Point(x, y), z, 50.0);
    }

    /**
     * Overrides the X-point calculation to make the tile narrower (50 world units instead of 100).
     *
     * @param cam        the camera used for projection
     * @param width      screen width
     * @param scaleFront scale at the front of the tile
     * @param scaleBack  scale at the back of the tile
     * @param targetX    world X coordinate
     * @param pulseScale current animation pulse scale
     * @param xpoints    array to fill with projected X coordinates
     */
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

    /**
     * Renders the tile with a thickness effect and dynamic colors.
     *
     * @param g2d     the graphics context
     * @param polygon the projected 2D polygon of the tile's top face
     * @param scale   the scale factor at the front of the tile
     */
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

        if (isActivated) {
            Color waveColor = getDynamicColor(0.6f, 1.0f, 0.0);
            g2d.setColor(RenderCache.customColorWithAlpha(waveColor, 230));
        } else {
            g2d.setColor(RenderCache.whiteWithAlpha(140));
        }
        g2d.fillPolygon(polygon);

        g2d.setStroke(RenderCache.STROKE_1);
        g2d.setColor(Color.WHITE);
        g2d.drawPolygon(polygon);
    }
}
