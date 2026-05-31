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
 *
 * @author Matysekxx
 */
public class GameWorldRenderer {
    /** Primary light color for the planet body. */
    private static final Color PLANET_BODY_LIGHT = new Color(45, 15, 80);
    /** Secondary dark color for the planet body. */
    private static final Color PLANET_BODY_DARK = new Color(10, 0, 25);
    /** Inner glow color for the planet rings. */
    private static final Color PLANET_RING_INNER = new Color(255, 200, 255, 200);
    /** Fully transparent color. */
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    /** Cached colors for the entire HSB hue range to improve performance. */
    private static final Color[] HUE_CACHE = new Color[360];

    static {
        for (int i = 0; i < 360; i++)
            HUE_CACHE[i] = Color.getHSBColor(i / 360f, 0.85f, 1.0f);
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

    /** Off-screen buffer for the static background elements. */
    private BufferedImage bgCache;
    /** Cached RadialGradientPaint for the planet glow. */
    private RadialGradientPaint cachedGlowPaint;
    /** Cached RadialGradientPaint for the planet body. */
    private RadialGradientPaint cachedBodyPaint;
    /** Last used glow radius for caching. */
    private int lastGlowR = -1;
    /** Last used base color RGB for caching. */
    private int lastBaseColorRGB = -1;
    /** Last used glow alpha for caching. */
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
     * @param g2d the graphics context
     * @param wd  the window data containing dimensions
     */
    public void drawBackground(Graphics2D g2d, WindowData wd) {
        if (bgCache == null || bgCache.getWidth() != wd.width() || bgCache.getHeight() != wd.height()) {
            bgCache = new BufferedImage(wd.width(), wd.height(), BufferedImage.TYPE_INT_RGB);
            Graphics2D cg = bgCache.createGraphics();
            RenderUtils.initGraphics2D(cg);
            RenderUtils.drawBackground(cg, wd.width(), wd.height());
            RenderUtils.drawFloor(cg, wd.width(), wd.height(), wd.horizonY());
            cg.dispose();
        }
        g2d.drawImage(bgCache, 0, 0, null);
    }

    /**
     * Draws the planet and the neon grid.
     *
     * @param g2d       the graphics context
     * @param wd        the window data
     * @param time      the current time
     * @param globalHue the global hue for coloring
     */
    public void drawPlanetAndGrid(Graphics2D g2d, WindowData wd, long time, float globalHue) {
        drawPlanet(g2d, wd, time, globalHue);
        RenderUtils.drawHorizonLine(g2d, wd.width(), wd.horizonY());
        drawNeonGrid(g2d, wd, globalHue);
    }

    /**
     * Renders the stylized planet with pulsatile glow and rings.
     */
    private void drawPlanet(Graphics2D g2d, WindowData wd, long time, float globalHue) {
        int cx = wd.width() / 2, cy = wd.horizonY() - UIScale.scale(150), r = UIScale.scale(100);
        float pulse = (float) ((Math.sin(time / 1000.0 * 1.5) + 1.0) / 2.0);
        int glowR = (int) (r * (2.f + pulse * 0.15f)), glowAlpha = (int) (15 + pulse * 30);
        Color baseColor = getCachedColor(globalHue);

        if (cachedGlowPaint == null || lastGlowR != glowR || lastBaseColorRGB != baseColor.getRGB() || lastGlowAlpha != glowAlpha) {
            cachedGlowPaint = new RadialGradientPaint(cx, cy, glowR, new float[]{0, 1}, 
                new Color[]{RenderCache.customColorWithAlpha(baseColor, glowAlpha), TRANSPARENT});
            lastGlowR = glowR; lastBaseColorRGB = baseColor.getRGB(); lastGlowAlpha = glowAlpha;
        }
        g2d.setPaint(cachedGlowPaint);
        g2d.fillOval(cx - glowR, cy - glowR, glowR * 2, glowR * 2);

        int ry = cy + (int) (Math.sin(time / 1000.0 * 0.4) * UIScale.scale(8));
        drawRing(g2d, cx, ry, r * 1.8f, UIScale.scale(28), 0, RenderCache.customColorWithAlpha(getCachedColor((globalHue + 0.3f) % 1f), 60), RenderCache.STROKE_1);
        drawRing(g2d, cx, ry, r * 1.4f, UIScale.scale(18), 0, RenderCache.customColorWithAlpha(baseColor, 40), RenderCache.STROKE_1);

        if (cachedBodyPaint == null) cachedBodyPaint = new RadialGradientPaint(cx - r / 2.5f, cy - r / 2.5f, r * 1.5f, new float[]{0, 1}, new Color[]{PLANET_BODY_LIGHT, PLANET_BODY_DARK});
        g2d.setPaint(cachedBodyPaint);
        g2d.fillOval(cx - r, cy - r, r * 2, r * 2);

        if (!Settings.graphicsQuality.equals("LOW")) {
            g2d.setColor(RenderCache.customColorWithAlpha(baseColor, 120));
            g2d.setStroke(RenderCache.STROKE_2_5);
            g2d.drawOval(cx - r, cy - r, r * 2, r * 2);
            drawRing(g2d, cx, ry, r * 1.4f, UIScale.scale(18), 180, RenderCache.customColorWithAlpha(baseColor, (int) (180 + 75 * pulse)), RenderCache.STROKE_2);
            drawRing(g2d, cx, ry, r * 1.8f, UIScale.scale(28), 180, RenderCache.customColorWithAlpha(getCachedColor((globalHue + 0.3f) % 1f), (int) (140 + 60 * pulse)), RenderCache.STROKE_2_5);
            drawRing(g2d, cx, ry, r * 1.8f, UIScale.scale(28), 180, PLANET_RING_INNER, RenderCache.STROKE_1);
        }
    }

    /**
     * Renders a segment of a 3D ring.
     */
    private void drawRing(Graphics2D g2d, int cx, int cy, float rx, int ry, int start, Color c, Stroke s) {
        g2d.setColor(c); g2d.setStroke(s);
        g2d.drawArc(cx - (int) rx, cy - ry, (int) (rx * 2), ry * 2, start, 180);
    }

    /**
     * Renders horizontal and vertical neon grid lines.
     */
    private void drawNeonGrid(Graphics2D g2d, WindowData wd, float globalHue) {
        double camZ = cam.getZ(), camZmod = camZ % 150;
        for (int z = 0; z < 3000; z += 150) {
            double dist = z - camZmod; if (dist <= 0) continue;
            int py = cam.projectY(150, camZ + dist, wd.horizonY());
            if (py < 0 || py > wd.height()) continue;
            g2d.setColor(RenderCache.customColorWithAlpha(getCachedColor((globalHue + (z / 3000f) * 0.15f) % 1f), (int) Math.clamp(255 - (dist / 3000.0 * 255), 0, 60)));
            g2d.drawLine(0, py, wd.width(), py);
        }
        g2d.setStroke(RenderCache.STROKE_2);
        for (int lx = -1200; lx <= 1200; lx += 120) {
            int sx = cam.projectX(lx, camZ + 20, wd.width()), sy = cam.projectY(150, camZ + 20, wd.horizonY());
            int ex = cam.projectX(lx, camZ + 3000, wd.width()), ey = cam.projectY(150, camZ + 3000, wd.horizonY());
            boolean main = Math.abs(lx) <= 300;
            g2d.setColor(RenderCache.customColorWithAlpha(getCachedColor(main ? globalHue : (globalHue + 0.5f) % 1f), main ? 110 : 55));
            g2d.drawLine(sx, sy, ex, ey);
        }
        g2d.setStroke(RenderCache.STROKE_1);
    }

    /**
     * Draws all game objects, including tiles, orbs, and the player sphere.
     *
     * @param g2d the graphics context
     * @param wd  the window data
     */
    public void drawGameObjects(Graphics2D g2d, WindowData wd) {
        if (gameEngine == null || gameEngine.getGameState() != GameState.FINISHED) {
            List<AbstractTile> tiles = level.tiles();
            for (int i = tiles.size() - 1; i >= 0; i--) {
                AbstractTile t = tiles.get(i);
                if (cam.getDistanceTo(t.getZ()) > 3000) continue;
                if (cam.getDistanceTo(t.getZ() + t.getLengthInZ()) <= 0) break;
                t.render(g2d, cam, wd);
            }
            if (gameEngine != null) {
                for (Orb o : gameEngine.getOrbs()) {
                    if (cam.getDistanceTo(o.getZ()) > 3000) continue;
                    if (cam.getDistanceTo(o.getZ()) <= 0) break;
                    o.render(g2d, cam, wd);
                }
            }
        }
        sphere.render(g2d, cam, wd);
    }

    /**
     * Retrieves a color from the hue cache based on a float value (0.0 to 1.0).
     */
    private Color getCachedColor(float hue) {
        int idx = (int) ((hue % 1.0f) * 359);
        return HUE_CACHE[idx < 0 ? idx + 360 : idx];
    }
}
