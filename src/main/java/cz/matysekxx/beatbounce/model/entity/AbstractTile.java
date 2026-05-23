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
     * Duration of the impact animation in seconds.
     */
    protected static final double IMPACT_DURATION = 0.25;
    /**
     * Scratch polygon for projection rendering.
     */
    protected final Polygon scratchPolygon = new Polygon(new int[4], new int[4], 4);
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
        setupPolygon(cam, windowData.width(), windowData.height() / 3, scaleFront, scaleBack, this.getX(), pulseScale, scratchPolygon);

        if (!Settings.graphicsQuality.equals("LOW")) {
            g2d.setStroke(RenderCache.STROKE_4);
            g2d.setColor(RenderCache.whiteWithAlpha(isActivated ? 180 : 60));
            g2d.drawPolygon(scratchPolygon);
        }

        this.drawTile(g2d, scratchPolygon, scaleFront);
        if (impactTime > 0) {
            final double progress = impactTime / IMPACT_DURATION;
            g2d.setColor(RenderCache.whiteWithAlpha((int) (180 * progress)));
            g2d.fillPolygon(scratchPolygon);
        }
    }

    /**
     * Calculates a dynamic color that smoothly changes over time and creates a wave effect along the Z-axis.
     *
     * @param saturation color saturation (0.0 - 1.0)
     * @param brightness color brightness (0.0 - 1.0)
     * @param phaseOffset phase shift, useful if different tile types should have a different base hue
     * @return a dynamic {@link Color} object
     */
    protected Color getDynamicColor(float saturation, float brightness, double phaseOffset) {
        final double timeFactor = (System.currentTimeMillis() % 25000) / 25000.0;
        final double zFactor = (this.z % 4000) / 4000.0;
        final float hue = (float) ((timeFactor + zFactor + phaseOffset) % 1.0);
        return Color.getHSBColor(hue, saturation, brightness);
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
     * Sets up a polygon with projected coordinates.
     *
     * @param cam         the camera used for projection
     * @param width       screen width
     * @param horizonY     the vertical position of the horizon on screen
     * @param scaleFront  scale at the front of the tile
     * @param scaleBack   scale at the back of the tile
     * @param targetX     world X coordinate
     * @param pulseScale  current animation pulse scale
     * @param poly        the polygon to populate
     */
    protected void setupPolygon(Camera3D cam, int width, int horizonY, double scaleFront, double scaleBack, int targetX, double pulseScale, Polygon poly) {
        fillXPoints(cam, width, scaleFront, scaleBack, targetX, pulseScale, poly.xpoints);
        fillYPoints(cam, scaleFront, scaleBack, horizonY, poly.ypoints);
        poly.invalidate();
    }

    /**
     * Fills the Y-coordinates for the vertices of the tile's 3D projection.
     *
     * @param cam        the camera used for projection
     * @param scaleFront scale at the front of the tile
     * @param scaleBack  scale at the back of the tile
     * @param horizonY   the vertical position of the horizon on screen
     * @param ypoints    array to fill with projected Y coordinates
     */
    protected void fillYPoints(Camera3D cam, double scaleFront, double scaleBack, int horizonY, int[] ypoints) {
        double baseHeight = 150 - cam.getY();
        final int screenYFront = (int) (horizonY + (baseHeight * scaleFront));
        final int screenYBack = (int) (horizonY + (baseHeight * scaleBack));
        ypoints[0] = screenYFront;
        ypoints[1] = screenYFront;
        ypoints[2] = screenYBack;
        ypoints[3] = screenYBack;
    }

    /**
     * Fills the X-coordinates for the vertices of the tile's 3D projection.
     *
     * @param cam        the camera used for projection
     * @param width      screen width
     * @param scaleFront scale at the front of the tile
     * @param scaleBack  scale at the back of the tile
     * @param targetX    world X coordinate
     * @param pulseScale current animation pulse scale
     * @param xpoints    array to fill with projected X coordinates
     */
    protected void fillXPoints(Camera3D cam, int width, double scaleFront, double scaleBack, int targetX, double pulseScale, int[] xpoints) {
        final double centerScreenFront = calculateCenterScreen(
                targetX, cam.getX(), width, scaleFront);
        final double centerScreenBack = calculateCenterScreen(
                targetX, cam.getX(), width, scaleBack);

        final double frontWidth = 100 * scaleFront * pulseScale;
        final double backWidth = 100 * scaleBack * pulseScale;

        xpoints[0] = (int) (centerScreenFront - frontWidth / 2);
        xpoints[1] = (int) (centerScreenFront + frontWidth / 2);
        xpoints[2] = (int) (centerScreenBack + backWidth / 2);
        xpoints[3] = (int) (centerScreenBack - backWidth / 2);
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
     * Returns the horizontal position of the tile at a specific world time.
     * For static tiles, this is simply the constant X-coordinate.
     * Subclasses with dynamic movement (like {@link MovingTile}) should override this.
     *
     * @param timestamp the world time in seconds
     * @return the horizontal world coordinate at that time
     */
    public double getXAt(double timestamp) {
        return this.getX();
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
