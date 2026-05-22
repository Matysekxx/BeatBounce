package cz.matysekxx.beatbounce.model.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.gui.RenderCache;

import java.awt.*;

/**
 * A long tile that allows the player to roll over its surface.
 * Used for sustained rhythmic sections or to fill large gaps.
 */
public class LongTile extends AbstractTile {
    /**
     * The primary base color of the tile.
     */
    private final Color baseColor;

    /**
     * The base color with an alpha transparency of 220.
     */
    private final Color baseColorAlpha220;

    /**
     * A lightened version of the base color used when the tile is activated.
     */
    private final Color lightenedColor;

    /**
     * Scratch polygon for rendering thickness.
     */
    private final Polygon thicknessScratch = new Polygon(new int[4], new int[4], 4);

    @JsonCreator
    public LongTile(
            @JsonProperty("beatEvent") BeatEvent beatEvent,
            @JsonProperty("x") int x,
            @JsonProperty("y") int y,
            @JsonProperty("z") double z,
            @JsonProperty("lengthInZ") double lengthInZ) {
        super(beatEvent, new Point(x, y), z, lengthInZ);
        float h = (float) ((z % 5000) / 5000.0);
        this.baseColor = Color.getHSBColor(h, 0.8f, 0.9f);
        this.baseColorAlpha220 = RenderCache.customColorWithAlpha(baseColor, 220);
        this.lightenedColor = Color.getHSBColor(h, 0.5f, 1.0f);
    }

    @Override
    public void drawTile(Graphics2D g2d, Polygon polygon, double scale) {
        final boolean isLow = Settings.graphicsQuality.equals("LOW");
        final Color displayColor = isActivated ? lightenedColor : baseColor;

        if (!isLow) {
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

        g2d.setColor(isActivated ? lightenedColor : baseColorAlpha220);
        g2d.fillPolygon(polygon);

        g2d.setStroke(RenderCache.STROKE_2);
        g2d.setColor(Color.WHITE);
        g2d.drawPolygon(polygon);

        g2d.setStroke(RenderCache.STROKE_1);
    }
}
