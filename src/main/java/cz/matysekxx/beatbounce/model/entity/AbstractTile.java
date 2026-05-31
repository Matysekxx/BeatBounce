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
 *
 * @author Matysekxx
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
    /** Duration of the impact animation in seconds. */
    protected static final double IMPACT_DURATION = 0.25;
    /** Scratch polygon for projection rendering. */
    protected final Polygon scratchPolygon = new Polygon(new int[4], new int[4], 4);
    /** The depth position of the tile in the 3D space. */
    protected double z;
    /** The length of the tile along the Z-axis. */
    protected double lengthInZ;
    /** Current impact animation time. */
    protected double impactTime = 0;
    /** Whether the tile has been activated (landed on). */
    protected boolean isActivated = false;
    /** The beat event associated with this tile. */
    private BeatEvent beatEvent;

    /**
     * Default constructor for {@code AbstractTile}.
     */
    protected AbstractTile() {
        super(0, 0);
        this.lengthInZ = 50.0;
    }

    /**
     * Constructs a new {@code AbstractTile}.
     */
    public AbstractTile(BeatEvent beatEvent, Point point, double z, double lengthInZ) {
        super(point.x, point.y);
        this.beatEvent = beatEvent;
        this.z = z;
        this.lengthInZ = lengthInZ;
    }

    public BeatEvent getBeatEvent() { return beatEvent; }
    public double getZ() { return z; }
    public double getLengthInZ() { return lengthInZ; }

    /**
     * Determines if the player's horizontal position is within the bounds of this tile.
     */
    public boolean isHit(double playerX, double playerRadius) {
        final double halfWidth = (this instanceof SmallTile ? 25.0 : 60.0) + playerRadius;
        final double tx = getX();
        return playerX >= tx - halfWidth && playerX <= tx + halfWidth;
    }

    /**
     * Returns the base width of the tile in world units.
     */
    protected int getTileWidth() {
        return 100;
    }

    /**
     * Projects and set up a polygon for 3D rendering.
     */
    protected void setupPolygon(Camera3D cam, WindowData windowData, double sF, double sB, int targetX, double pulseScale, Polygon poly) {
        final int cxF = cam.projectX(targetX, z, windowData.width()), cyF = cam.projectY(150, z, windowData.horizonY());
        final int cxB = cam.projectX(targetX, z + lengthInZ, windowData.width()), cyB = cam.projectY(150, z + lengthInZ, windowData.horizonY());
        final double wF = getTileWidth() * sF * pulseScale, wB = getTileWidth() * sB * pulseScale;

        poly.xpoints[0] = (int) (cxF - wF / 2); poly.ypoints[0] = cyF;
        poly.xpoints[1] = (int) (cxF + wF / 2); poly.ypoints[1] = cyF;
        poly.xpoints[2] = (int) (cxB + wB / 2); poly.ypoints[2] = cyB;
        poly.xpoints[3] = (int) (cxB - wB / 2); poly.ypoints[3] = cyB;
        poly.invalidate();
    }

    /**
     * Renders the tile in a 3D perspective.
     */
    public void render(Graphics2D g2d, Camera3D cam, WindowData windowData) {
        double pulse = 1.0;
        if (impactTime > 0) pulse = 1.0 + 0.3 * Math.sin((impactTime / IMPACT_DURATION) * Math.PI);

        final double sF = cam.getScale(z), sB = cam.getScale(z + lengthInZ);
        setupPolygon(cam, windowData, sF, sB, getX(), pulse, scratchPolygon);

        if (!Settings.graphicsQuality.equals("LOW")) {
            g2d.setStroke(RenderCache.STROKE_4);
            g2d.setColor(RenderCache.whiteWithAlpha(isActivated ? 180 : 60));
            g2d.drawPolygon(scratchPolygon);
        }

        this.drawTile(g2d, scratchPolygon, sF);
        if (impactTime > 0) {
            g2d.setColor(RenderCache.whiteWithAlpha((int) (220 * (impactTime / IMPACT_DURATION))));
            g2d.fillPolygon(scratchPolygon);
        }
    }

    /**
     * Calculates a dynamic color that smoothly changes over time.
     */
    protected Color getDynamicColor(float saturation, float brightness, double phaseOffset) {
        final double timeFactor = (System.currentTimeMillis() % 25000) / 25000.0;
        final double zFactor = (this.z % 4000) / 4000.0;
        final float hue = (float) ((timeFactor + zFactor + phaseOffset) % 1.0);
        return Color.getHSBColor(hue, saturation, brightness);
    }

    /**
     * Internal rendering method for subclasses.
     */
    public abstract void drawTile(Graphics2D g2d, Polygon polygon, double scale);

    /**
     * Returns the horizontal position of the tile at a specific world time.
     */
    public double getXAt(double timestamp) {
        return this.getX();
    }

    /**
     * Sets the location of the tile.
     */
    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Resets the internal state.
     */
    public void reset() {
        this.impactTime = 0;
        this.isActivated = false;
    }

    /**
     * Triggers the impact animation.
     */
    public void onLanding() {
        this.impactTime = IMPACT_DURATION;
        this.isActivated = true;
    }

    /**
     * Updates the impact animation state.
     */
    public void updateImpact(double deltaTime) {
        if (impactTime > 0) {
            impactTime -= deltaTime;
            if (impactTime < 0) impactTime = 0;
        }
    }
}
