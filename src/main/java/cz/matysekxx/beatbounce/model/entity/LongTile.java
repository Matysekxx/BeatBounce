package cz.matysekxx.beatbounce.model.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.gui.RenderCache;

import java.awt.*;

/**
 * The {@code LongTile} class represents a tile with extended length along the Z-axis.
 * It is typically used for sustained notes in the game.
 * It extends {@link AbstractTile}.
 *
 * @author Matysekxx
 */
public class LongTile extends AbstractTile {
    /**
     * Scratch polygon for rendering the thickness (3D effect) of the tile.
     */
    private final Polygon thicknessScratch = new Polygon(new int[4], new int[4], 4);

    /**
     * Constructs a new {@code LongTile} with specified parameters.
     *
     * @param beatEvent the {@link BeatEvent} associated with this tile
     * @param x         the horizontal position
     * @param y         the vertical position
     * @param z         the depth position
     * @param lengthInZ the length of the tile along the Z-axis
     */
    @JsonCreator
    public LongTile(
            @JsonProperty("beatEvent") BeatEvent beatEvent,
            @JsonProperty("x") int x,
            @JsonProperty("y") int y,
            @JsonProperty("z") double z,
            @JsonProperty("lengthInZ") double lengthInZ) {
        super(beatEvent, new Point(x, y), z, lengthInZ);
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
        final boolean isLow = Settings.graphicsQuality.equals("LOW");

        Color dynamicBase = getDynamicColor(0.8f, 0.9f, 0.7);
        Color dynamicLight = getDynamicColor(0.5f, 1.0f, 0.7);
        Color tileColor = isActivated ? dynamicLight : dynamicBase;

        if (!isLow) {
            g2d.setStroke(RenderCache.STROKE_8);
            g2d.setColor(RenderCache.customColorWithAlpha(tileColor, 35));
            g2d.drawPolygon(polygon);

            final int thickness = (int) (10 * scale);
            if (thickness > 0) {
                for (int i = 0; i < 4; i++) {
                    thicknessScratch.xpoints[i] = polygon.xpoints[i];
                    thicknessScratch.ypoints[i] = polygon.ypoints[i] + thickness;
                }
                thicknessScratch.invalidate();
                g2d.setColor(RenderCache.customColorWithAlpha(new Color(10, 10, 20), 180));
                g2d.fillPolygon(thicknessScratch);
            }
        }

        g2d.setColor(RenderCache.customColorWithAlpha(tileColor, 220));
        g2d.fillPolygon(polygon);

        g2d.setStroke(RenderCache.STROKE_2);
        g2d.setColor(Color.WHITE);
        g2d.drawPolygon(polygon);

        g2d.setStroke(RenderCache.STROKE_1);
    }
}
