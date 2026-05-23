package cz.matysekxx.beatbounce.model.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.WindowData;

import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * The {@code NormalTile} class represents a static tile in the 3D game space.
 * It can optionally have "fake lanes" which are purely visual elements that appear alongside the tile.
 * It extends {@link AbstractTile}.
 */
public class NormalTile extends AbstractTile {
    /**
     * Scratch polygon for fake lanes.
     */
    private final Polygon fakeScratchPolygon = new Polygon(new int[4], new int[4], 4);
    /**
     * Hue offset based on the tile's Z position.
     */
    private float hueOffset;
    /**
     * List of horizontal offsets for rendering non-collidable "fake" lanes.
     */
    private List<Integer> fakeLaneOffsets;
    /**
     * Base color for the tile.
     */
    private Color baseColor;
    /**
     * Base color with alpha 120.
     */
    private Color baseColorAlpha120;
    /**
     * Base color with alpha 180.
     */
    private Color baseColorAlpha180;
    /**
     * Base color with alpha 230.
     */
    private Color baseColorAlpha230;
    /**
     * Lightened color used when activated.
     */
    private Color lightenedColor;
    /**
     * Lightened color with alpha 230.
     */
    private Color lightenedColorAlpha230;

    /**
     * Default constructor for {@code NormalTile}.
     */
    protected NormalTile() {
        super();
    }

    /**
     * Constructs a new {@code NormalTile} with specified parameters.
     *
     * @param beatEvent       the {@link BeatEvent} associated with this tile
     * @param x               the horizontal position
     * @param y               the vertical position
     * @param z               the depth position
     * @param fakeLaneOffsets a list of integer offsets for rendering fake lanes
     */
    @JsonCreator
    public NormalTile(
            @JsonProperty("beatEvent") BeatEvent beatEvent,
            @JsonProperty("x") int x,
            @JsonProperty("y") int y,
            @JsonProperty("z") double z,
            @JsonProperty("fakeLaneOffsets") List<Integer> fakeLaneOffsets) {
        super(beatEvent, new Point(x, y), z, 50.0);
        this.hueOffset = (float) ((z % 5000) / 5000.0);
        this.fakeLaneOffsets = fakeLaneOffsets != null ? fakeLaneOffsets : Collections.emptyList();
        calculateColors();
    }

    /**
     * Constructs a new {@code NormalTile} at the specified point and depth.
     *
     * @param beatEvent the {@link BeatEvent} associated with this tile
     * @param point     the (x, y) coordinates of the tile
     * @param z         the depth position
     */
    public NormalTile(BeatEvent beatEvent, Point point, double z) {
        this(beatEvent, point.x, point.y, z, Collections.emptyList());
    }

    /**
     * Constructs a new {@code NormalTile} at the specified point and depth with fake lanes.
     *
     * @param beatEvent       the {@link BeatEvent} associated with this tile
     * @param point           the (x, y) coordinates of the tile
     * @param z               the depth position
     * @param fakeLaneOffsets a list of integer offsets for rendering fake lanes
     */
    public NormalTile(BeatEvent beatEvent, Point point, double z, List<Integer> fakeLaneOffsets) {
        this(beatEvent, point.x, point.y, z, fakeLaneOffsets);
    }

    /**
     * Calculates the colors used for rendering based on the hue offset.
     */
    private void calculateColors() {
        final float h = 0.33f + (hueOffset * 0.1f);
        this.baseColor = Color.getHSBColor(h, 1.0f, 1.0f);
        final Color baseDimColor = Color.getHSBColor(h, 0.8f, 0.4f);
        this.baseColorAlpha120 = RenderCache.customColorWithAlpha(baseDimColor, 120);
        this.baseColorAlpha180 = RenderCache.customColorWithAlpha(baseDimColor, 180);
        this.baseColorAlpha230 = RenderCache.customColorWithAlpha(baseColor, 230);
        this.lightenedColor = Color.getHSBColor(h, 0.6f, 1.0f);
        this.lightenedColorAlpha230 = RenderCache.customColorWithAlpha(lightenedColor, 230);
    }

    /**
     * Returns the list of fake lane offsets.
     *
     * @return a {@link List} of integers representing offsets
     */
    public List<Integer> getFakeLaneOffsets() {
        return fakeLaneOffsets;
    }

    /**
     * Renders the tile and its fake lanes in a 3D perspective.
     *
     * @param g2d        the graphics context to paint on
     * @param cam        the {@link Camera3D} used for perspective calculations
     * @param windowData the {@link WindowData} containing screen dimensions
     */
    @Override
    public void render(Graphics2D g2d, Camera3D cam, WindowData windowData) {
        if (fakeLaneOffsets != null && !fakeLaneOffsets.isEmpty()) {
            final double scaleFront = cam.getScale(this.getZ());
            final double scaleBack = cam.getScale(this.getZ() + getLengthInZ());
            final int LANE_WIDTH = 120;

            Color dynamicFakeColor = RenderCache.customColorWithAlpha(getDynamicColor(0.8f, 0.4f, 0.0), 120);
            Color dynamicFakeBorder = RenderCache.customColorWithAlpha(getDynamicColor(0.8f, 0.5f, 0.0), 180);

            for (int offset : fakeLaneOffsets) {
                setupPolygon(cam, windowData.width(), windowData.height() / 3, scaleFront, scaleBack, this.getX() + (offset * LANE_WIDTH), 1.0, fakeScratchPolygon);

                drawPolygon(g2d, dynamicFakeColor, dynamicFakeBorder, fakeScratchPolygon);
            }
        }
        super.render(g2d, cam, windowData);
    }

    /**
     * Internal helper method to fill and draw a polygon with specified colors.
     *
     * @param g2d                the graphics context
     * @param dynamicFakeColor   the fill color
     * @param dynamicFakeBorder  the border color
     * @param fakeScratchPolygon the polygon to draw
     */
    private void drawPolygon(Graphics2D g2d, Color dynamicFakeColor, Color dynamicFakeBorder, Polygon fakeScratchPolygon) {
        g2d.setColor(dynamicFakeColor);
        g2d.fillPolygon(fakeScratchPolygon);

        if (!Settings.graphicsQuality.equals("LOW")) {
            g2d.setStroke(RenderCache.STROKE_1_5);
            g2d.setColor(dynamicFakeBorder);
            g2d.drawPolygon(fakeScratchPolygon);
        }
        g2d.setStroke(RenderCache.STROKE_1);
    }

    @Override
    public void drawTile(Graphics2D g2d, Polygon polygon, double scale) {
        Color dynamicBase = getDynamicColor(0.9f, 0.9f, 0.0);
        Color dynamicLight = getDynamicColor(0.5f, 1.0f, 0.0);
        Color tileColor = isActivated ? dynamicLight : dynamicBase;

        if (!Settings.graphicsQuality.equals("LOW")) {
            g2d.setStroke(RenderCache.STROKE_8);
            g2d.setColor(RenderCache.customColorWithAlpha(tileColor, 40));
            g2d.drawPolygon(polygon);

            if (Settings.graphicsQuality.equals("HIGH")) {
                g2d.setStroke(RenderCache.STROKE_4);
                g2d.setColor(RenderCache.customColorWithAlpha(tileColor, 90));
                g2d.drawPolygon(polygon);
            }
        }

        g2d.setColor(RenderCache.customColorWithAlpha(tileColor, 220));
        g2d.fillPolygon(polygon);

        g2d.setStroke(RenderCache.STROKE_1_5);
        g2d.setColor(Color.WHITE);
        g2d.drawPolygon(polygon);

        g2d.setStroke(RenderCache.STROKE_1);
    }
}
