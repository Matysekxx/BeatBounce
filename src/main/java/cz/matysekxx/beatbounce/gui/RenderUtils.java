package cz.matysekxx.beatbounce.gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public final class RenderUtils {
    public final static Color cyan = new Color(0, 255, 220);
    public final static Color green = new Color(50, 255, 50);
    public final static Color blue = new Color(0, 150, 255);
    public final static Color purple = new Color(191, 0, 255);
    public final static Color yellow = new Color(255, 215, 0);
    public static final int ROAD_WIDTH = 300;
    private static BufferedImage noiseTexture;
    private static int[][] starCache = null;
    public static final Cursor blankCursor;

    static {
        final var cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        blankCursor = Toolkit.getDefaultToolkit()
                .createCustomCursor(
                        cursorImg, new Point(0, 0), "blank cursor"
                );
    }


    private RenderUtils() {
    }

    public static void drawBackground(Graphics2D g2d, int w, int h) {
        g2d.setColor(new Color(8, 8, 12));
        g2d.fillRect(0, 0, w, h);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        drawStars(g2d, w, h);
        drawOrb(g2d, w, h, w * 0.35f, h * 0.4f, w * 0.6f, new Color(220, 0, 140, 35));
        drawOrb(g2d, w, h, w * 0.7f, h * 0.6f, w * 0.65f, new Color(0, 230, 255, 22));
        drawOrb(g2d, w, h, w * 0.5f, h * 0.5f, w * 0.9f, new Color(80, 0, 240, 18));
        applyNoiseOverlay(g2d, 0, 0, w, h);
    }

    private static void drawOrb(Graphics2D g2d, int w, int h, float cx, float cy, float radius, Color innerColor) {
        g2d.setPaint(new RadialGradientPaint(cx, cy, radius, new float[]{0f, 1f}, new Color[]{innerColor, new Color(0, 0, 0, 0)}));
        g2d.fillRect(0, 0, w, h);
    }

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
            g2d.setColor(new Color(255, 255, 255, star[2]));
            g2d.fillRect(star[0] % w, star[1] % h, 1, 1);
        }
    }

    public static void initGraphics2D(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    public static void drawFloor(Graphics2D g2d, int width, int height, int horizonY) {
        g2d.setColor(new Color(3, 0, 10));
        g2d.fillRect(0, horizonY, width, height - horizonY);
        g2d.setPaint(new GradientPaint(0, horizonY, new Color(8, 0, 20, 0), 0, height, new Color(15, 0, 35, 100)));
        g2d.fillRect(0, horizonY, width, height - horizonY);
    }

    public static void drawHorizonLine(Graphics2D g2d, int width, int horizonY) {
        g2d.setColor(new Color(255, 0, 255, 180));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(0, horizonY, width, horizonY);
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.drawLine(0, horizonY, width, horizonY);
    }

    public static void drawText(Graphics2D g2d, String text, int x, int y, Color c) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        final double pulse = (Math.sin(System.currentTimeMillis() / 400.0) + 1.0) / 2.0;

        for (float i = 6f; i >= 1f; i -= 1.5f) {
            final float alpha = Math.min(1.0f, (float) (0.1 + (0.2 * pulse) / (i * 0.5)));
            g2d.setColor(new Color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, alpha));
            g2d.drawString(text, x - i, y);
            g2d.drawString(text, x + i, y);
            g2d.drawString(text, x, y - i);
            g2d.drawString(text, x, y + i);
        }
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(text, x + 1, y + 1);
        g2d.setColor(new Color(255, 230, 240));
        g2d.drawString(text, x, y);
    }

    public static void applyNoiseOverlay(Graphics2D g2, int x, int y, int w, int h) {
        if (noiseTexture == null) {
            noiseTexture = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
            final Random r = new Random(12345);
            for (int ix = 0; ix < 256; ix++) {
                for (int iy = 0; iy < 256; iy++) {
                    noiseTexture.setRGB(ix, iy, new Color(255, 255, 255, r.nextInt(8)).getRGB());
                }
            }
        }
        final Composite originalComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        for (int ix = x; ix < x + w; ix += 256) {
            for (int iy = y; iy < y + h; iy += 256) {
                g2.drawImage(noiseTexture, ix, iy, null);
            }
        }
        g2.setComposite(originalComposite);
    }

    public static void drawTitle(Graphics2D g2d, int w, int h, String text) {
        g2d.setFont(new Font("Monospaced", Font.BOLD | Font.ITALIC, 130));
        RenderUtils.drawText(g2d, text, (w - g2d.getFontMetrics().stringWidth(text)) >> 1, h >> 2, RenderUtils.cyan);
    }
}