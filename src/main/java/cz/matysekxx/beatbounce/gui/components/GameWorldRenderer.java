package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.WindowData;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.Orb;
import cz.matysekxx.beatbounce.model.entity.Sphere;
import cz.matysekxx.beatbounce.model.game.GameEngine;
import cz.matysekxx.beatbounce.model.game.state.GameState;
import cz.matysekxx.beatbounce.model.level.Level;

import cz.matysekxx.beatbounce.util.UIScale;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Handles the rendering of the 3D world environment, including the planet,
 * neon grid (floor and ceiling), and game entities.
 */
public class GameWorldRenderer {
    /**
     * Primary light color for the planet body.
     */
    private static final Color PLANET_BODY_LIGHT = new Color(45, 15, 80);

    /**
     * Secondary dark color for the planet body.
     */
    private static final Color PLANET_BODY_DARK = new Color(10, 0, 25);

    /**
     * Inner glow color for the planet rings.
     */
    private static final Color PLANET_RING_INNER = new Color(255, 200, 255, 200);

    /**
     * Fully transparent color.
     */
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    /**
     * Cached colors for the entire HSB hue range to improve performance.
     */
    private static final Color[] HUE_CACHE = new Color[360];

    static {
        for (int i = 0; i < 360; i++) {
            HUE_CACHE[i] = Color.getHSBColor(i / 360f, 0.85f, 1.0f);
        }
    }

    /**
     * The camera used for 3D projections.
     */
    private final Camera3D cam;

    /**
     * The game engine providing state and entity data.
     */
    private final GameEngine gameEngine;

    /**
     * The level data containing tiles and metadata.
     */
    private final Level level;

    /**
     * The player sphere entity.
     */
    private final Sphere sphere;

    /**
     * Scratch array for storing projected screen coordinates [x, y].
     */
    private final int[] projScratch = new int[2];
    /**
     * Reusable arrays for RadialGradientPaint.
     */
    private final float[] fractions = {0f, 1f};
    private final Color[] colors = new Color[2];
    /**
     * Off-screen buffer for the static background elements.
     */
    private BufferedImage bgCache;
    /**
     * Cached RadialGradientPaint for the planet glow.
     */
    private RadialGradientPaint cachedGlowPaint;
    /**
     * Cached RadialGradientPaint for the planet body.
     */
    private RadialGradientPaint cachedBodyPaint;
    /**
     * Last used glow radius for caching.
     */
    private int lastGlowR = -1;
    /**
     * Last used base color RGB for caching.
     */
    private int lastBaseColorRGB = -1;
    /**
     * Last used glow alpha for caching.
     */
    private int lastGlowAlpha = -1;

    /**
     * Constructs a new GameWorldRenderer.
     *
     * @param cam        the camera used for 3D projection
     * @param gameEngine the game engine
     * @param level      the current level
     * @param sphere     the player's sphere
     */
    public GameWorldRenderer(Camera3D cam, GameEngine gameEngine, Level level, Sphere sphere) {
        this.cam = cam;
        this.gameEngine = gameEngine;
        this.level = level;
        this.sphere = sphere;
    }

    /**
     * Draws the background of the game world.
     *
     * @param g2d      the graphics context
     * @param width    the width of the screen
     * @param height   the height of the screen
     * @param horizonY the y-coordinate of the horizon
     */
    public void drawBackground(Graphics2D g2d, int width, int height, int horizonY) {
        if (bgCache == null || bgCache.getWidth() != width || bgCache.getHeight() != height) {
            this.bgCache = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            final Graphics2D cg = bgCache.createGraphics();
            RenderUtils.initGraphics2D(cg);
            RenderUtils.drawBackground(cg, width, height);
            RenderUtils.drawFloor(cg, width, height, horizonY);
            cg.dispose();
        }
        g2d.drawImage(bgCache, 0, 0, null);
    }

    /**
     * Draws the planet and the neon grid.
     *
     * @param g2d       the graphics context
     * @param width     the width of the screen
     * @param height    the height of the screen
     * @param horizonY  the y-coordinate of the horizon
     * @param time      the current time
     * @param globalHue the global hue for coloring
     */
    public void drawPlanetAndGrid(Graphics2D g2d, int width, int height, int horizonY, long time, float globalHue) {
        drawPlanet(g2d, width, horizonY, time, globalHue);
        RenderUtils.drawHorizonLine(g2d, width, horizonY);
        drawNeonGrid(g2d, width, height, horizonY, globalHue);
    }

    /**
     * Renders the stylized planet with pulsatile glow and rings.
     */
    private void drawPlanet(Graphics2D g2d, int width, int horizonY, long time, float globalHue) {
        final int cx = width / 2;
        final int cy = horizonY - UIScale.scale(150);
        final int r = UIScale.scale(100);
        final float t = time / 1000.0f;
        final float pulse = (float) ((Math.sin(t * 1.5) + 1.0) / 2.0);

        final int glowR = (int) (r * (2.f + pulse * 0.15f));
        final int glowAlpha = (int) (15 + pulse * 30);

        final Color baseColor = getCachedColor(globalHue);
        final Color secondaryColor = getCachedColor((globalHue + 0.3f) % 1.0f);

        if (cachedGlowPaint == null || lastGlowR != glowR || lastBaseColorRGB != baseColor.getRGB() || lastGlowAlpha != glowAlpha) {
            colors[0] = RenderCache.customColorWithAlpha(baseColor, glowAlpha);
            colors[1] = TRANSPARENT;
            cachedGlowPaint = new RadialGradientPaint(cx, cy, glowR, fractions, colors);
            lastGlowR = glowR;
            lastBaseColorRGB = baseColor.getRGB();
            lastGlowAlpha = glowAlpha;
        }
        g2d.setPaint(cachedGlowPaint);
        g2d.fillOval(cx - glowR, cy - glowR, glowR * 2, glowR * 2);

        final int ry = cy + (int) (Math.sin(t * 0.4) * UIScale.scale(8));

        drawRing(g2d, cx, ry, r * 1.8f, UIScale.scale(28), 0, RenderCache.customColorWithAlpha(secondaryColor, 60), RenderCache.STROKE_1);
        drawRing(g2d, cx, ry, r * 1.4f, UIScale.scale(18), 0, RenderCache.customColorWithAlpha(baseColor, 40), RenderCache.STROKE_1);

        if (cachedBodyPaint == null) {
            colors[0] = PLANET_BODY_LIGHT;
            colors[1] = PLANET_BODY_DARK;
            cachedBodyPaint = new RadialGradientPaint(cx - r / 2.5f, cy - r / 2.5f, r * 1.5f,
                    fractions, colors);
        }
        g2d.setPaint(cachedBodyPaint);
        g2d.fillOval(cx - r, cy - r, r * 2, r * 2);

        if (!Settings.graphicsQuality.equals("LOW")) {
            g2d.setColor(RenderCache.customColorWithAlpha(baseColor, 120));
            g2d.setStroke(RenderCache.STROKE_2_5);
            g2d.drawOval(cx - r, cy - r, r * 2, r * 2);

            drawRing(g2d, cx, ry, r * 1.4f, UIScale.scale(18), 180, RenderCache.customColorWithAlpha(baseColor, (int) (180 + 75 * pulse)), RenderCache.STROKE_2);
            drawRing(g2d, cx, ry, r * 1.8f, UIScale.scale(28), 180, RenderCache.customColorWithAlpha(secondaryColor, (int) (140 + 60 * pulse)), RenderCache.STROKE_2_5);
            drawRing(g2d, cx, ry, r * 1.8f, UIScale.scale(28), 180, PLANET_RING_INNER, RenderCache.STROKE_1);
        }
        g2d.setStroke(RenderCache.STROKE_1);
    }

    /**
     * Renders a segment of a 3D ring.
     */
    private void drawRing(Graphics2D g2d, int cx, int cy, float rx, int ry, int startAngle, Color color, BasicStroke stroke) {
        g2d.setColor(color);
        g2d.setStroke(stroke);
        g2d.drawArc(cx - (int) rx, cy - ry, (int) (rx * 2), ry * 2, startAngle, 180);
    }

    /**
     * Coordinates the drawing of horizontal and vertical grid lines.
     */
    private void drawNeonGrid(Graphics2D g2d, int width, int height, int horizonY, float globalHue) {
        final double camZ = cam.getZ();
        final double camZmod = camZ % 150;
        drawHorizontalGrid(g2d, width, height, horizonY, globalHue, camZ, camZmod);
        drawVerticalGrid(g2d, width, horizonY, globalHue, camZ);
    }

    /**
     * Renders horizontal grid lines with perspective fading.
     */
    private void drawHorizontalGrid(Graphics2D g2d, int width, int height, int horizonY, float globalHue, double camZ, double camZmod) {
        for (int z = 0; z < 3000; z += 150) {
            final double distance = z - camZmod;
            if (distance <= 0) continue;
            if (!project(0, camZ + distance, width, horizonY)) continue;
            final int py = projScratch[1];
            if (py < 0 || py > height) continue;

            final int alpha = (int) Math.clamp(255 - (distance / 3000.0 * 255), 0, 60);
            final float rowHue = (globalHue + (z / 3000f) * 0.15f) % 1.0f;
            g2d.setColor(RenderCache.customColorWithAlpha(getCachedColor(rowHue), alpha));
            g2d.drawLine(0, py, width, py);
        }
    }

    /**
     * Renders vertical grid lines extending into the distance.
     */
    private void drawVerticalGrid(Graphics2D g2d, int width, int horizonY, float globalHue, double camZ) {
        g2d.setStroke(RenderCache.STROKE_2);
        for (int lx = -1200; lx <= 1200; lx += 120) {
            if (!project(lx, camZ + 20, width, horizonY)) continue;
            final int sx = projScratch[0], sy = projScratch[1];
            if (!project(lx, camZ + 3000, width, horizonY)) continue;
            final int ex = projScratch[0], ey = projScratch[1];

            final boolean isMainLane = Math.abs(lx) <= 300;
            final float laneHue = isMainLane ? globalHue : (globalHue + 0.5f) % 1.0f;
            final int alpha = isMainLane ? 110 : (int) (110 * 0.5);

            g2d.setColor(RenderCache.customColorWithAlpha(getCachedColor(laneHue), alpha));
            g2d.drawLine(sx, sy, ex, ey);
        }
        g2d.setStroke(RenderCache.STROKE_1);
    }

    /**
     * Draws all game objects, including tiles, orbs, and the player sphere.
     *
     * @param g2d        the graphics context
     * @param windowData the window data
     */
    public void drawGameObjects(Graphics2D g2d, WindowData windowData) {
        if (gameEngine == null || gameEngine.getGameState() != GameState.FINISHED) {
            final List<AbstractTile> tiles = level.tiles();
            for (int i = tiles.size() - 1; i >= 0; i--) {
                final AbstractTile tile = tiles.get(i);
                final double distance = cam.getDistanceTo(tile.getZ());
                final double tileDepth = distance + tile.getLengthInZ();

                if (distance > 3000) continue;
                if (tileDepth <= 0) break;

                tile.render(g2d, cam, windowData);
            }

            if (gameEngine != null) {
                final List<Orb> orbs = gameEngine.getOrbs();
                for (int i = orbs.size() - 1; i >= 0; i--) {
                    final Orb orb = orbs.get(i);
                    final double distance = cam.getDistanceTo(orb.getZ());

                    if (distance > 3000) continue;
                    if (distance <= 0) break;

                    orb.render(g2d, cam, windowData);
                }
            }
        }
        sphere.render(g2d, cam, windowData);
    }

    /**
     * Projects a 3D coordinate [x, z] to 2D screen coordinates using the camera.
     * Results are stored in {@code projScratch}.
     */
    private boolean project(double x, double z, int width, int horizonY) {
        final double scale = cam.getScale(z);
        if (scale <= 0) return false;
        projScratch[0] = (int) (width / 2.0 + (x - cam.getX()) * scale);
        projScratch[1] = (int) (horizonY + ((150. - cam.getY()) * scale));
        return true;
    }

    /**
     * Retrieves a color from the hue cache based on a float value (0.0 to 1.0).
     */
    private Color getCachedColor(float hue) {
        int index = (int) ((hue % 1.0f) * 359);
        if (index < 0) index += 360;
        return HUE_CACHE[index];
    }
}
