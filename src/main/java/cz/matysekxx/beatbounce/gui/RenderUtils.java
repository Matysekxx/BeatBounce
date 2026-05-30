package cz.matysekxx.beatbounce.gui;

import cz.matysekxx.beatbounce.configuration.Settings;

import cz.matysekxx.beatbounce.util.UIScale;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Utility class for common rendering operations and colors.
 * It provides methods for drawing backgrounds, floors, text with bloom effects, and more.
 *
 * @author Matysekxx
 */
public final class RenderUtils {
    /**
     * The standard cyan color used in the UI.
     */
    public final static Color cyan = new Color(0, 255, 220);
    /**
     * The standard green color used in the UI.
     */
    public final static Color green = new Color(50, 255, 50);
    /**
     * The standard blue color used in the UI.
     */
    public final static Color blue = new Color(0, 150, 255);
    /**
     * The standard purple color used in the UI.
     */
    public final static Color purple = new Color(191, 0, 255);
    /**
     * The standard yellow color used in the UI.
     */
    public final static Color yellow = new Color(255, 215, 0);
    /**
     * The width of the road in the game.
     */
    public static final int ROAD_WIDTH = 300;
    /**
     * A blank cursor used to hide the mouse cursor.
     */
    public static final Cursor blankCursor;
    /**
     * A dark background color.
     */
    public static final Color BG_DARK = new Color(10, 10, 26);
    /**
     * A gray color used for text.
     */
    public static final Color TEXT_GRAY = new Color(160, 160, 170);

    /**
     * Primary background color.
     */
    private static final Color BG_COLOR = new Color(8, 8, 12);

    /**
     * Base color for the floor.
     */
    private static final Color FLOOR_COLOR = new Color(3, 0, 10);

    /**
     * Magenta component of the horizon glow.
     */
    private static final Color HORIZON_MAGENTA = new Color(255, 0, 255, 180);

    /**
     * White component of the horizon glow.
     */
    private static final Color HORIZON_WHITE = new Color(255, 255, 255, 200);

    /**
     * Alpha composite for applying the noise texture.
     */
    private static final AlphaComposite NOISE_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);

    /**
     * Color used for shadows under text or objects.
     */
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 100);

    /**
     * Foreground color for main titles.
     */
    private static final Color TITLE_COLOR = new Color(255, 230, 240);

    /**
     * Pre-generated noise texture for static-like overlays.
     */
    private static BufferedImage noiseTexture;

    /**
     * Cached star positions and intensities.
     */
    private static int[][] starCache = null;

    /**
     * Cached width for background resources to detect resolution changes.
     */
    private static int bgCachedW = -1;

    /**
     * Cached height for background resources to detect resolution changes.
     */
    private static int bgCachedH = -1;

    /**
     * First radial gradient for background orbs, centered on the left side.
     */
    private static RadialGradientPaint bgOrb1;

    /**
     * Second radial gradient for background orbs, centered on the right side.
     */
    private static RadialGradientPaint bgOrb2;

    /**
     * Third radial gradient for background orbs, centered in the middle.
     */
    private static RadialGradientPaint bgOrb3;

    /**
     * Gradient paint for the floor, providing a sense of depth and perspective.
     */
    private static GradientPaint floorGradient;

    static {
        final var cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        blankCursor = Toolkit.getDefaultToolkit()
                .createCustomCursor(
                        cursorImg, new Point(0, 0), "blank cursor"
                );
    }


    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private RenderUtils() {
    }

    /**
     * Draws a stylized background with stars and orbs.
     *
     * @param g2d the graphics context to draw on
     * @param w   the width of the area
     * @param h   the height of the area
     */
    public static void drawBackground(Graphics2D g2d, int w, int h) {
        g2d.setColor(BG_COLOR);
        g2d.fillRect(0, 0, w, h);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        if (Settings.particlesEnabled) drawStars(g2d, w, h);

        if (bgCachedW != w || bgCachedH != h || bgOrb1 == null) {
            bgCachedW = w;
            bgCachedH = h;
            bgOrb1 = new RadialGradientPaint(w * 0.35f, h * 0.4f, w * 0.6f, new float[]{0f, 1f}, new Color[]{new Color(220, 0, 140, 35), new Color(0, 0, 0, 0)});
            bgOrb2 = new RadialGradientPaint(w * 0.7f, h * 0.6f, w * 0.65f, new float[]{0f, 1f}, new Color[]{new Color(0, 230, 255, 22), new Color(0, 0, 0, 0)});
            bgOrb3 = new RadialGradientPaint(w * 0.5f, h * 0.5f, w * 0.9f, new float[]{0f, 1f}, new Color[]{new Color(80, 0, 240, 18), new Color(0, 0, 0, 0)});
        }

        drawOrbs(g2d, w, h, bgOrb1, bgOrb2, bgOrb3);

        applyNoiseOverlay(g2d, 0, 0, w, h);
    }

    /**
     * Draws a rounded background for menu panels with a gradient and border.
     *
     * @param g2 the graphics context
     * @param w  the width of the panel
     * @param h  the height of the panel
     */
    public static void drawMenuBackground(Graphics2D g2, int w, int h) {
        RenderUtils.initGraphics2D(g2);
        g2.setPaint(new LinearGradientPaint(0, 0, w, h,
                new float[]{0f, 1f},
                new Color[]{new Color(15, 15, 35, 180), new Color(10, 10, 25, 100)}));
        final int arc = UIScale.scale(24);
        g2.fillRoundRect(0, 0, w, h, arc, arc);
        g2.setColor(new Color(0, 255, 255, 30));
        g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
        g2.dispose();
    }

    /**
     * Draws the background orbs.
     *
     * @param g2d    the graphics context
     * @param w      the width
     * @param h      the height
     * @param bgOrb1 the first orb paint
     * @param bgOrb2 the second orb paint
     * @param bgOrb3 the third orb paint
     */
    public static void drawOrbs(Graphics2D g2d, int w, int h, RadialGradientPaint bgOrb1, RadialGradientPaint bgOrb2, RadialGradientPaint bgOrb3) {
        g2d.setPaint(bgOrb1);
        g2d.fillRect(0, 0, w, h);
        g2d.setPaint(bgOrb2);
        g2d.fillRect(0, 0, w, h);
        g2d.setPaint(bgOrb3);
        g2d.fillRect(0, 0, w, h);
    }

    /**
     * Draws a field of stars for the background.
     *
     * @param g2d the graphics context
     * @param w   the width of the area
     * @param h   the height of the area
     */
    private static void drawStars(Graphics2D g2d, int w, int h) {
        if (starCache == null) {
            final Random rng = new Random(0xABCDEF42L);
            starCache = new int[20][3];
            for (int i = 0; i < starCache.length; i++) {
                starCache[i][0] = rng.nextInt(1920);
                starCache[i][1] = rng.nextInt(1080);
                starCache[i][2] = 60 + rng.nextInt(140);
            }
        }
        for (int[] star : starCache) {
            g2d.setColor(RenderCache.whiteWithAlpha(star[2]));
            g2d.fillRect(star[0] % w, star[1] % h, 1, 1);
        }
    }

    /**
     * Initializes the Graphics2D context with standard rendering hints.
     *
     * @param g2d the graphics context to initialize
     */
    public static void initGraphics2D(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    /**
     * Draws the floor with a gradient effect.
     *
     * @param g2d      the graphics context
     * @param width    the width
     * @param height   the total height
     * @param horizonY the y-coordinate of the horizon
     */
    public static void drawFloor(Graphics2D g2d, int width, int height, int horizonY) {
        g2d.setColor(FLOOR_COLOR);
        g2d.fillRect(0, horizonY, width, height - horizonY);
        if (bgCachedW != width || bgCachedH != height || floorGradient == null) {
            floorGradient = new GradientPaint(0, horizonY, new Color(8, 0, 20, 0), 0, height, new Color(15, 0, 35, 100));
        }
        g2d.setPaint(floorGradient);
        g2d.fillRect(0, horizonY, width, height - horizonY);
    }

    /**
     * Draws a line representing the horizon.
     *
     * @param g2d      the graphics context
     * @param width    the width
     * @param horizonY the y-coordinate of the horizon
     */
    public static void drawHorizonLine(Graphics2D g2d, int width, int horizonY) {
        g2d.setColor(HORIZON_MAGENTA);
        g2d.setStroke(RenderCache.STROKE_3);
        g2d.drawLine(0, horizonY, width, horizonY);
        g2d.setStroke(RenderCache.STROKE_1);
        g2d.setColor(HORIZON_WHITE);
        g2d.drawLine(0, horizonY, width, horizonY);
    }

    /**
     * Draws text with a shadow and optional bloom effect.
     *
     * @param g2d  the graphics context
     * @param text the text to draw
     * @param x    the x-coordinate
     * @param y    the y-coordinate
     * @param c    the color of the text
     */
    public static void drawText(Graphics2D g2d, String text, int x, int y, Color c) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        final double pulse = (Math.sin(System.currentTimeMillis() / 400.0) + 1.0) / 2.0;

        drawBloom(g2d, text, x, y, pulse, c);
        g2d.setColor(SHADOW_COLOR);
        g2d.drawString(text, x + 1, y + 1);
        g2d.setColor(TITLE_COLOR);
        g2d.drawString(text, x, y);
    }

    /**
     * Draws a bloom effect behind text to simulate light glow.
     *
     * @param g2d        the graphics context
     * @param text       the text to apply bloom to
     * @param drawX      the x-coordinate
     * @param drawY      the y-coordinate
     * @param pulse      pulsation factor (0.0 to 1.0) for dynamic bloom
     * @param bloomColor the color of the glow
     */
    public static void drawBloom(Graphics2D g2d, String text, int drawX, int drawY, double pulse, Color bloomColor) {
        if (Settings.bloomEnabled) {
            for (float j = 6f; j >= 1f; j -= 2.5f) {
                final float alpha = Math.min(1.0f, (float) (0.1 + (0.2 * pulse) / (j * 0.5)));
                g2d.setColor(RenderCache.customColorWithAlpha(bloomColor, (int) (alpha * 255)));
                g2d.drawString(text, drawX - j, drawY);
                g2d.drawString(text, drawX + j, drawY);
                g2d.drawString(text, drawX, drawY - j);
                g2d.drawString(text, drawX, drawY + j);
            }
        }
    }

    /**
     * Applies a noise overlay to the specified area.
     *
     * @param g2 the graphics context
     * @param x  the x-coordinate
     * @param y  the y-coordinate
     * @param w  the width
     * @param h  the height
     */
    public static void applyNoiseOverlay(Graphics2D g2, int x, int y, int w, int h) {
        if (noiseTexture == null) {
            noiseTexture = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
            final Random r = new Random(12345);
            for (int ix = 0; ix < 256; ix++) {
                for (int iy = 0; iy < 256; iy++) {
                    noiseTexture.setRGB(ix, iy, RenderCache.whiteWithAlpha(r.nextInt(8)).getRGB());
                }
            }
        }
        final Composite originalComposite = g2.getComposite();
        g2.setComposite(NOISE_COMPOSITE);
        for (int ix = x; ix < x + w; ix += 256) {
            for (int iy = y; iy < y + h; iy += 256) {
                g2.drawImage(noiseTexture, ix, iy, null);
            }
        }
        g2.setComposite(originalComposite);
    }

}
