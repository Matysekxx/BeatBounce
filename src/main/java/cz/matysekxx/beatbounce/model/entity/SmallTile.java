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
    public void paint3D(Graphics2D g2d, Polygon polygon) {
        g2d.setColor(isActivated ? new Color(100, 100, 100, 220) : new Color(255, 255, 255, 180));
        g2d.fillPolygon(polygon);

        g2d.setStroke(RenderCache.STROKE_1);
        g2d.setColor(Color.WHITE);
        g2d.drawPolygon(polygon);
    }
}
