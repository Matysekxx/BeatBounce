package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.WindowData;
import cz.matysekxx.beatbounce.model.game.GameModel;
import cz.matysekxx.beatbounce.model.game.GameState;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.Orb;
import cz.matysekxx.beatbounce.model.entity.Sphere;
import cz.matysekxx.beatbounce.model.level.Level;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Handles the rendering of the 3D world environment, including the planet, 
 * neon grid (floor and ceiling), and game entities.
 */
public class GameWorldRenderer {
    private static final Color PLANET_BODY_LIGHT = new Color(45, 15, 80);
    private static final Color PLANET_BODY_DARK = new Color(10, 0, 25);
    private static final Color PLANET_RING_INNER = new Color(255, 200, 255, 200);
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    private static final Color[] HUE_CACHE = new Color[360];
    static {
        for (int i = 0; i < 360; i++) {
            HUE_CACHE[i] = Color.getHSBColor(i / 360f, 0.85f, 1.0f);
        }
    }

    private final Camera3D cam;
    private final GameModel gameModel;
    private final Level level;
    private final Sphere sphere;
    
    private BufferedImage bgCache;
    private final int[] projScratch = new int[2];

    public GameWorldRenderer(Camera3D cam, GameModel gameModel, Level level, Sphere sphere) {
        this.cam = cam;
        this.gameModel = gameModel;
        this.level = level;
        this.sphere = sphere;
    }

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

    public void drawPlanetAndGrid(Graphics2D g2d, int width, int height, int horizonY, long time, float globalHue) {
        drawPlanet(g2d, width, horizonY, time, globalHue);
        RenderUtils.drawHorizonLine(g2d, width, horizonY);
        drawNeonGrid(g2d, width, height, horizonY, globalHue);
    }

    private void drawPlanet(Graphics2D g2d, int width, int horizonY, long time, float globalHue) {
        final int cx = width / 2;
        final int cy = horizonY - 150;
        final int r = 100;
        final float t = time / 1000.0f;
        final float pulse = (float) ((Math.sin(t * 1.5) + 1.0) / 2.0);

        final int glowR = (int) (r * (2.f + pulse * 0.15f));
        final int glowAlpha = (int) (15 + pulse * 30);
        
        final Color baseColor = getCachedColor(globalHue);
        final Color secondaryColor = getCachedColor((globalHue + 0.3f) % 1.0f);

        g2d.setPaint(new RadialGradientPaint(cx, cy, glowR, new float[]{0f, 1f},
                new Color[]{RenderCache.customColorWithAlpha(baseColor, glowAlpha), TRANSPARENT}));
        g2d.fillOval(cx - glowR, cy - glowR, glowR * 2, glowR * 2);

        final int ry = cy + (int) (Math.sin(t * 0.4) * 8);

        drawRing(g2d, cx, ry, r * 1.8f, 28, 0, RenderCache.customColorWithAlpha(secondaryColor, 60), RenderCache.STROKE_1);
        drawRing(g2d, cx, ry, r * 1.4f, 18, 0, RenderCache.customColorWithAlpha(baseColor, 40), RenderCache.STROKE_1);

        g2d.setPaint(new RadialGradientPaint(cx - r / 2.5f, cy - r / 2.5f, r * 1.5f,
                new float[]{0f, 1f}, new Color[]{PLANET_BODY_LIGHT, PLANET_BODY_DARK}));
        g2d.fillOval(cx - r, cy - r, r * 2, r * 2);

        if (!Settings.graphicsQuality.equals("LOW")) {
            g2d.setColor(RenderCache.customColorWithAlpha(baseColor, 120));
            g2d.setStroke(RenderCache.STROKE_2_5);
            g2d.drawOval(cx - r, cy - r, r * 2, r * 2);

            drawRing(g2d, cx, ry, r * 1.4f, 18, 180, RenderCache.customColorWithAlpha(baseColor, (int) (180 + 75 * pulse)), RenderCache.STROKE_2);
            drawRing(g2d, cx, ry, r * 1.8f, 28, 180, RenderCache.customColorWithAlpha(secondaryColor, (int) (140 + 60 * pulse)), RenderCache.STROKE_2_5);
            drawRing(g2d, cx, ry, r * 1.8f, 28, 180, PLANET_RING_INNER, RenderCache.STROKE_1);
        }
        g2d.setStroke(RenderCache.STROKE_1);
    }

    private void drawRing(Graphics2D g2d, int cx, int cy, float rx, int ry, int startAngle, Color color, BasicStroke stroke) {
        g2d.setColor(color);
        g2d.setStroke(stroke);
        g2d.drawArc(cx - (int) rx, cy - ry, (int) (rx * 2), ry * 2, startAngle, 180);
    }

    private void drawNeonGrid(Graphics2D g2d, int width, int height, int horizonY, float globalHue) {
        final double camZ = cam.getZ();
        final double camZmod = camZ % 150;
        drawHorizontalGrid(g2d, width, height, horizonY, globalHue, camZ, camZmod);
        drawVerticalGrid(g2d, width, horizonY, globalHue, camZ);
    }

    private void drawHorizontalGrid(Graphics2D g2d, int width, int height, int horizonY, float globalHue, double camZ, double camZmod) {
        for (int z = 0; z < 3000; z += 150) {
            final double distance = z - camZmod;
            if (distance <= 0) continue;
            if (!project(0, camZ + distance, width, horizonY)) continue;
            final int py = projScratch[1];
            if (py < 0 || py > height) continue;

            final int alpha = (int) Math.max(0, Math.min(60, 255 - (distance / 3000.0 * 255)));
            final float rowHue = (globalHue + (z / 3000f) * 0.15f) % 1.0f;
            g2d.setColor(RenderCache.customColorWithAlpha(getCachedColor(rowHue), alpha));
            g2d.drawLine(0, py, width, py);
        }
    }

    private void drawVerticalGrid(Graphics2D g2d, int width, int horizonY, float globalHue, double camZ) {
        g2d.setStroke(RenderCache.STROKE_2);
        for (int lx = -1200; lx <= 1200; lx += 120) {
            if (!project(lx, camZ + 20, width, horizonY)) continue;
            final int sx = projScratch[0], sy = projScratch[1];
            if (!project(lx, camZ + 3000, width, horizonY)) continue;
            final int ex = projScratch[0], ey = projScratch[1];

            final boolean isMainLane = Math.abs(lx) <= 300;
            final float laneHue = isMainLane ? globalHue : (globalHue + 0.5f) % 1.0f;
            final int alpha = isMainLane ? 110 : (int)(110 * 0.5);
            
            g2d.setColor(RenderCache.customColorWithAlpha(getCachedColor(laneHue), alpha));
            g2d.drawLine(sx, sy, ex, ey);
        }
        g2d.setStroke(RenderCache.STROKE_1);
    }

    public void drawGameObjects(Graphics2D g2d, WindowData windowData) {
        if (gameModel == null || gameModel.getGameState() != GameState.FINISHED) {
            final List<AbstractTile> tiles = level.tiles();
            for (int i = tiles.size() - 1; i >= 0; i--) {
                final AbstractTile tile = tiles.get(i);
                final double distance = cam.getDistanceTo(tile.getZ());
                final double tileDepth = distance + tile.getLengthInZ();
                if (tileDepth <= 0 || distance > 3000) continue;
                tile.paint3D(g2d, cam, windowData);
            }

            if (gameModel != null) {
                for (Orb orb : gameModel.getOrbs()) {
                    final double distance = cam.getDistanceTo(orb.getZ());
                    if (distance > 0 && distance < 3000) {
                        orb.paint3D(g2d, cam, windowData);
                    }
                }
            }
        }
        sphere.paint3D(g2d, cam, windowData);
    }

    private boolean project(double x, double z, int width, int horizonY) {
        final double scale = cam.getScale(z);
        if (scale <= 0) return false;
        projScratch[0] = (int) (width / 2.0 + (x - cam.getX()) * scale);
        projScratch[1] = (int) (horizonY + ((150. - cam.getY()) * scale));
        return true;
    }

    private Color getCachedColor(float hue) {
        int index = (int) ((hue % 1.0f) * 359);
        if (index < 0) index += 360;
        return HUE_CACHE[index];
    }
}
