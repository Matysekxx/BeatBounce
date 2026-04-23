package cz.matysekxx.beatbounce.gui;

import java.awt.*;
import java.util.Collection;

public final class RenderUtils {
    public final static Color cyan = new Color(0, 255, 220);
    public final static Color green = new Color(50, 255, 50);
    public final static Color blue = new Color(0, 150, 255);
    public final static Color purple = new Color(191, 0, 255);
    public final static Color yellow = new Color(255, 215, 0);

    private RenderUtils() {}

    public static void initGraphic2D(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    public static void drawBackground(Graphics2D g2d, int width, int height) {
        final RadialGradientPaint bg = new RadialGradientPaint(
                width / 2f, height / 2f, width,
                new float[]{0.0f, 1.0f},
                new Color[]{new Color(15, 0, 25), new Color(2, 0, 5)}
        );
        g2d.setPaint(bg);
        g2d.fillRect(0, 0, width, height);
    }

    public static void drawStars(Graphics2D g2d, Collection<Star> stars, int width, int horizonY) {
        g2d.translate(width / 2, horizonY - 100);
        for (Star s : stars) {
            final double fov = 300.0;
            final double projX = (s.x / s.z) * fov;
            final double projY = (s.y / s.z) * fov;
            final double size = Math.max(0.5, 4.0 - (s.z / 100.0));
            final int alpha = (int) Math.min(255, Math.max(0, 255 - (s.z * 0.5)));
            g2d.setColor(new Color(0, 255, 255, alpha));
            g2d.fillOval((int) projX, (int) projY, (int) size, (int) size);
        }
        g2d.translate(-width / 2, -(horizonY - 100));
    }

    public static void drawFloor(Graphics2D g2d, int width, int height, int horizonY) {
        g2d.setColor(new Color(3, 0, 10));
        g2d.fillRect(0, horizonY, width, height - horizonY);
    }

    public static void drawHorizonLine(Graphics2D g2d, int width, int horizonY) {
        g2d.setColor(new Color(255, 0, 255, 200));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(0, horizonY, width, horizonY);
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(Color.WHITE);
        g2d.drawLine(0, horizonY, width, horizonY);
    }

    public static void drawCRTScanlines(Graphics2D g2d, int width, int height) {
        g2d.setColor(new Color(0, 0, 0, 40));
        for (int i = 0; i < height; i += 3) {
            g2d.drawLine(0, i, width, i);
        }
    }

    public static void drawVignette(Graphics2D g2d, int width, int height) {
        final RadialGradientPaint vignette = new RadialGradientPaint(
                width / 2f, height / 2f, width * 0.8f,
                new float[]{0.4f, 1.0f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 220)}
        );
        g2d.setPaint(vignette);
        g2d.fillRect(0, 0, width, height);
    }

    public static void drawText(Graphics2D g2d, String text, int x, int y, Color c) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        final double pulse = (Math.sin(System.currentTimeMillis() / 400.0) + 1.0) / 2.0;

        for (float i = 8f; i >= 1f; i -= 1.5f) {
            float alpha = (float) (0.12 + (0.25 * pulse) / (i * 0.5));
            if (alpha > 1.0f) alpha = 1.0f;

            g2d.setColor(new Color(c.getRed()/255f, c.getGreen()/255f, c.getBlue()/255f, alpha));
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
}