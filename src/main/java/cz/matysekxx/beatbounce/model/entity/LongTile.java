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
    private final Color baseColor;
    private final Color baseColorAlpha220;
    private final Color lightenedColor;

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
        this.baseColorAlpha220 = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 220);
        this.lightenedColor = Color.getHSBColor(h, 0.5f, 1.0f);
    }
    @Override
    public void drawTile(Graphics2D g2d, Polygon polygon, double scale) {
        final boolean isLow = Settings.graphicsQuality.equals("LOW");
        final Color displayColor = isActivated ? lightenedColor : baseColor;

        if (!isLow) {
            final Polygon thicknessPoly = new Polygon(polygon.xpoints, polygon.ypoints, polygon.npoints);
            final int thickness = (int) (10 * scale);
            if (thickness > 0) {
                thicknessPoly.translate(0, thickness);
                g2d.setColor(new Color(10, 10, 20, 180));
                g2d.fillPolygon(thicknessPoly);
            }
        }

        final Rectangle bounds = polygon.getBounds();
        final GradientPaint gp = new GradientPaint(
                bounds.x, bounds.y, displayColor.brighter(),
                bounds.x, bounds.y + bounds.height, isActivated ? lightenedColor : baseColorAlpha220
        );
        g2d.setPaint(gp);
        g2d.fillPolygon(polygon);

        g2d.setStroke(RenderCache.STROKE_2);
        g2d.setColor(Color.WHITE);
        g2d.drawPolygon(polygon);
    }
}
