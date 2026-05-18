package cz.matysekxx.beatbounce.model.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.WindowData;

import java.awt.*;

/**
 * The {@code AbstractTile} class represents a generic tile in the 3D game space.
 * It extends {@link Entity} to provide 3D rendering capabilities.
 * Tiles are associated with a {@link BeatEvent} and have a depth position {@code z}.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = NormalTile.class, name = "normal"),
        @JsonSubTypes.Type(value = MovingTile.class, name = "moving"),
        @JsonSubTypes.Type(value = LongTile.class, name = "long"),
        @JsonSubTypes.Type(value = SmallTile.class, name = "small"),
        @JsonSubTypes.Type(value = BreakableTile.class, name = "breakable")
})
public abstract class AbstractTile extends Entity {
    /**
     * The depth position of the tile in the 3D space.
     */
    protected double z;
    /**
     * The length of the tile along the Z-axis.
     */
    protected double lengthInZ;
    /**
     * Current impact animation time.
     */
    protected double impactTime = 0;
    /**
     * Duration of the impact animation in seconds.
     */
    protected static final double IMPACT_DURATION = 0.25;
    /**
     * Whether the tile has been activated (landed on).
     */
    protected boolean isActivated = false;
    /**
     * The beat event associated with this tile.
     */
    private BeatEvent beatEvent;

    /**
     * Default constructor for {@code AbstractTile}.
     * Initializes the entity with coordinates (0, 0) and a default Z-length of 50.0.
     */
    protected AbstractTile() {
        super(0, 0);
        this.lengthInZ = 50.0;
    }

    /**
     * Constructs a new {@code AbstractTile} with the specified beat event, location, and dimensions.
     *
     * @param beatEvent the {@link BeatEvent} associated with this tile
     * @param point     the (x, y) coordinates of the tile
     * @param z         the depth position of the tile
     * @param lengthInZ the length of the tile along the Z-axis
     */
    public AbstractTile(BeatEvent beatEvent, Point point, double z, double lengthInZ) {
        super(point.x, point.y);
        this.beatEvent = beatEvent;
        this.z = z;
        this.lengthInZ = lengthInZ;
    }

    /**
     * Returns the beat event associated with this tile.
     *
     * @return the {@link BeatEvent}
     */
    public BeatEvent getBeatEvent() {
        return beatEvent;
    }

    /**
     * Returns the depth position of the tile.
     *
     * @return the {@code z} coordinate
     */
    public double getZ() {
        return z;
    }

    /**
     * Returns the length of the tile along the Z-axis.
     *
     * @return the {@code lengthInZ} value
     */
    public double getLengthInZ() {
        return lengthInZ;
    }

    /**
     * Renders the tile in a 3D perspective onto the 2D graphics context.
     *
     * @param g2d        the graphics context to paint on
     * @param cam        the {@link Camera3D} used for perspective calculations
     * @param windowData the {@link WindowData} containing screen dimensions
     */
    public void render(Graphics2D g2d, Camera3D cam, WindowData windowData) {
        double pulseScale = 1.0;
        if (impactTime > 0) {
            double progress = impactTime / IMPACT_DURATION;
            pulseScale = 1.0 + 0.15 * Math.sin(progress * Math.PI);
        }

        final double scaleFront = cam.getScale(this.getZ());
        final double scaleBack = cam.getScale(this.getZ() + getLengthInZ());
        final Polygon polygon = new Polygon(
                createXPoints(
                        cam, windowData.width(), scaleFront, scaleBack, this.getX(), pulseScale),
                createYPoints(
                        cam, scaleFront, scaleBack, windowData.height() / 3),
                4
        );
        if (!Settings.graphicsQuality.equals("LOW")) {
            g2d.setStroke(RenderCache.STROKE_4);
            g2d.setColor(new Color(255, 255, 255, isActivated ? 180 : 60));
            g2d.drawPolygon(polygon);
        }

        this.drawTile(g2d, polygon, scaleFront);
        if (impactTime > 0) {
            final double progress = impactTime / IMPACT_DURATION;
            g2d.setColor(new Color(255, 255, 255, (int) (180 * progress)));
            g2d.fillPolygon(polygon);
        }
    }

    /**
     * Internal rendering method for subclasses to define their appearance.
     *
     * @param g2d     the graphics context
     * @param polygon the projected 2D polygon
     * @param scale   the scale factor at the front of the tile
     */
    public abstract void drawTile(Graphics2D g2d, Polygon polygon, double scale);

    /**
     * Calculates the Y-coordinates for the vertices of the tile's 3D projection.
     *
     * @param cam        the {@link Camera3D} used for scaling
     * @param scaleFront the scale factor for the front edge of the tile
     * @param scaleBack  the scale factor for the back edge of the tile
     * @param horizonY   the vertical position of the horizon on the screen
     * @return an array of Y-coordinates for the polygon vertices
     */
    protected int[] createYPoints(Camera3D cam, double scaleFront, double scaleBack, int horizonY) {
        double baseHeight = 150 - cam.getY();
        final int screenYFront = (int) (horizonY + (baseHeight * scaleFront));
        final int screenYBack = (int) (horizonY + (baseHeight * scaleBack));
        return new int[]{
                screenYFront, screenYFront, screenYBack, screenYBack
        };
    }

    /**
     * Calculates the X-coordinates for the vertices of the tile's 3D projection.
     *
     * @param cam        the {@link Camera3D} used for scaling
     * @param width      the width of the rendering area
     * @param scaleFront the scale factor for the front edge of the tile
     * @param scaleBack  the scale factor for the back edge of the tile
     * @param targetX    the horizontal position of the tile in the world
     * @param pulseScale the current pulse scale multiplier
     * @return an array of X-coordinates for the polygon vertices
     */
    protected int[] createXPoints(Camera3D cam, int width, double scaleFront, double scaleBack, int targetX, double pulseScale) {
        final double centerScreenFront = calculateCenterScreen(
                targetX, cam.getX(), width, scaleFront);
        final double centerScreenBack = calculateCenterScreen(
                targetX, cam.getX(), width, scaleBack);

        final double frontWidth = 100 * scaleFront * pulseScale;
        final double backWidth = 100 * scaleBack * pulseScale;

        return new int[]{
                (int) (centerScreenFront - frontWidth / 2),
                (int) (centerScreenFront + frontWidth / 2),
                (int) (centerScreenBack + backWidth / 2),
                (int) (centerScreenBack - backWidth / 2)
        };
    }

    /**
     * Calculates the horizontal center of an object on the screen based on its world position and camera position.
     *
     * @param targetX the horizontal position of the target in the world
     * @param camX    the horizontal position of the camera in the world
     * @param width   the width of the rendering area
     * @param scale   the scale factor based on depth
     * @return the screen-space X-coordinate of the center of the object
     */
    private double calculateCenterScreen(int targetX, double camX, int width, double scale) {
        return ((double) width / 2) + ((targetX - camX) * scale);
    }

    /**
     * Sets the location of the tile in the 2D world space.
     *
     * @param x the new horizontal position
     * @param y the new vertical position
     */
    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Resets the internal state of the tile. By default does nothing.
     */
    public void reset() {
        this.impactTime = 0;
        this.isActivated = false;
    }

    /**
     * Triggers the impact animation for this tile.
     */
    public void onLanding() {
        this.impactTime = IMPACT_DURATION;
        this.isActivated = true;
    }

    /**
     * Updates the impact animation state.
     *
     * @param deltaTime the time elapsed since the last update
     */
    public void updateImpact(double deltaTime) {
        if (impactTime > 0) {
            impactTime -= deltaTime;
            if (impactTime < 0) impactTime = 0;
        }
    }
}
