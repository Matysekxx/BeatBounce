package cz.matysekxx.beatbounce.gui;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.io.InputStream;

/**
 * A cache for rendering resources such as strokes, fonts, and colors with alpha.
 * It also contains predefined shapes for rendering game objects.
 */
public final class RenderCache {
    /**
     * Montserrat Black font, size 12.
     */
    public static final Font MONTSERRAT_BLACK_12 = loadFont("Montserrat-Black.ttf", 12);
    /**
     * Montserrat Black font, size 14.
     */
    public static final Font MONTSERRAT_BLACK_14 = loadFont("Montserrat-Black.ttf", 14);
    /**
     * Montserrat Black font, size 16.
     */
    public static final Font MONTSERRAT_BLACK_16 = loadFont("Montserrat-Black.ttf", 16);
    /**
     * Montserrat Black font, size 18.
     */
    public static final Font MONTSERRAT_BLACK_18 = loadFont("Montserrat-Black.ttf", 18);
    /**
     * Montserrat Black font, size 20.
     */
    public static final Font MONTSERRAT_BLACK_20 = loadFont("Montserrat-Black.ttf", 20);
    /**
     * Montserrat Black font, size 24.
     */
    public static final Font MONTSERRAT_BLACK_24 = loadFont("Montserrat-Black.ttf", 24);
    /**
     * Montserrat Black font, size 28.
     */
    public static final Font MONTSERRAT_BLACK_28 = loadFont("Montserrat-Black.ttf", 28);
    /**
     * Audiowide font, size 24.
     */
    public static final Font AUDIOWIDE_24 = loadFont("Audiowide-Regular.ttf", 24);
    /**
     * Audiowide font, size 36.
     */
    public static final Font AUDIOWIDE_36 = loadFont("Audiowide-Regular.ttf", 36);
    /**
     * Audiowide font, size 48.
     */
    public static final Font AUDIOWIDE_48 = loadFont("Audiowide-Regular.ttf", 48);
    /**
     * Audiowide font, size 64.
     */
    public static final Font AUDIOWIDE_64 = loadFont("Audiowide-Regular.ttf", 64);
    /**
     * Audiowide font, size 78.
     */
    public static final Font AUDIOWIDE_78 = loadFont("Audiowide-Regular.ttf", 78);
    /**
     * Audiowide font, size 85.
     */
    public static final Font AUDIOWIDE_85 = loadFont("Audiowide-Regular.ttf", 85);
    /**
     * Audiowide font, size 150.
     */
    public static final Font AUDIOWIDE_150 = loadFont("Audiowide-Regular.ttf", 150);

    /**
     * A stroke with width 1.0.
     */
    public static final BasicStroke STROKE_1 = new BasicStroke(1.0f);
    /**
     * A stroke with width 1.5.
     */
    public static final BasicStroke STROKE_1_5 = new BasicStroke(1.5f);
    /**
     * A stroke with width 2.0.
     */
    public static final BasicStroke STROKE_2 = new BasicStroke(2.0f);
    /**
     * A stroke with width 2.5.
     */
    public static final BasicStroke STROKE_2_5 = new BasicStroke(2.5f);
    /**
     * A stroke with width 3.0.
     */
    public static final BasicStroke STROKE_3 = new BasicStroke(3.0f);
    /**
     * A stroke with width 3.5.
     */
    public static final BasicStroke STROKE_3_5 = new BasicStroke(3.5f);
    /**
     * A stroke with width 4.0.
     */
    public static final BasicStroke STROKE_4 = new BasicStroke(4.0f);
    /**
     * A stroke with width 6.0.
     */
    public static final BasicStroke STROKE_6 = new BasicStroke(6.0f);
    /**
     * A stroke with width 8.0.
     */
    public static final BasicStroke STROKE_8 = new BasicStroke(8.0f);

    /**
     * Monospaced Bold font, size 11.
     */
    public static final Font MONO_BOLD_11 = new Font("Monospaced", Font.BOLD, 11);
    /**
     * Monospaced Bold font, size 12.
     */
    public static final Font MONO_BOLD_12 = new Font("Monospaced", Font.BOLD, 12);
    /**
     * Monospaced Bold font, size 16.
     */
    public static final Font MONO_BOLD_16 = new Font("Monospaced", Font.BOLD, 16);
    /**
     * Monospaced Bold font, size 17.
     */
    public static final Font MONO_BOLD_17 = new Font("Monospaced", Font.BOLD, 17);
    /**
     * Monospaced Bold font, size 24.
     */
    public static final Font MONO_BOLD_24 = new Font("Monospaced", Font.BOLD, 24);
    /**
     * Monospaced Bold font, size 85.
     */
    public static final Font MONO_BOLD_85 = new Font("Monospaced", Font.BOLD, 85);
    /**
     * Monospaced Bold font, size 150.
     */
    public static final Font MONO_BOLD_150 = new Font("Monospaced", Font.BOLD, 150);

    /**
     * Monospaced Italic Bold font, size 24.
     */
    public static final Font MONO_ITALIC_BOLD_24 = new Font("Monospaced", Font.BOLD | Font.ITALIC, 24);
    /**
     * Monospaced Italic Bold font, size 48.
     */
    public static final Font MONO_ITALIC_BOLD_48 = new Font("Monospaced", Font.BOLD | Font.ITALIC, 48);
    /**
     * Monospaced Italic Bold font, size 60.
     */
    public static final Font MONO_ITALIC_BOLD_60 = new Font("Monospaced", Font.BOLD | Font.ITALIC, 60);
    /**
     * Monospaced Italic Bold font, size 65.
     */
    public static final Font MONO_ITALIC_BOLD_65 = new Font("Monospaced", Font.BOLD | Font.ITALIC, 65);
    /**
     * Monospaced Italic Bold font, size 78.
     */
    public static final Font MONO_ITALIC_BOLD_78 = new Font("Monospaced", Font.BOLD | Font.ITALIC, 78);
    /**
     * Monospaced Italic Bold font, size 150.
     */
    public static final Font MONO_ITALIC_BOLD_150 = new Font("Monospaced", Font.BOLD | Font.ITALIC, 150);

    /**
     * SansSerif Plain font, size 13.
     */
    public static final Font SANS_PLAIN_13 = new Font("SansSerif", Font.PLAIN, 13);
    /**
     * SansSerif Plain font, size 14.
     */
    public static final Font SANS_PLAIN_14 = new Font("SansSerif", Font.PLAIN, 14);
    /**
     * SansSerif Plain font, size 15.
     */
    public static final Font SANS_PLAIN_15 = new Font("SansSerif", Font.PLAIN, 15);
    /**
     * SansSerif Plain font, size 16.
     */
    public static final Font SANS_PLAIN_16 = new Font("SansSerif", Font.PLAIN, 16);
    /**
     * SansSerif Plain font, size 17.
     */
    public static final Font SANS_PLAIN_17 = new Font("SansSerif", Font.PLAIN, 17);
    /**
     * SansSerif Plain font, size 18.
     */
    public static final Font SANS_PLAIN_18 = new Font("SansSerif", Font.PLAIN, 18);
    /**
     * SansSerif Plain font, size 20.
     */
    public static final Font SANS_PLAIN_20 = new Font("SansSerif", Font.PLAIN, 20);

    /**
     * SansSerif Bold font, size 11.
     */
    public static final Font SANS_BOLD_11 = new Font("SansSerif", Font.BOLD, 11);
    /**
     * SansSerif Bold font, size 13.
     */
    public static final Font SANS_BOLD_13 = new Font("SansSerif", Font.BOLD, 13);
    /**
     * SansSerif Bold font, size 14.
     */
    public static final Font SANS_BOLD_14 = new Font("SansSerif", Font.BOLD, 14);
    /**
     * SansSerif Bold font, size 15.
     */
    public static final Font SANS_BOLD_15 = new Font("SansSerif", Font.BOLD, 15);
    /**
     * SansSerif Bold font, size 16.
     */
    public static final Font SANS_BOLD_16 = new Font("SansSerif", Font.BOLD, 16);
    /**
     * SansSerif Bold font, size 18.
     */
    public static final Font SANS_BOLD_18 = new Font("SansSerif", Font.BOLD, 18);
    /**
     * SansSerif Bold font, size 20.
     */
    public static final Font SANS_BOLD_20 = new Font("SansSerif", Font.BOLD, 20);
    /**
     * SansSerif Bold font, size 22.
     */
    public static final Font SANS_BOLD_22 = new Font("SansSerif", Font.BOLD, 22);
    /**
     * SansSerif Bold font, size 26.
     */
    public static final Font SANS_BOLD_26 = new Font("SansSerif", Font.BOLD, 26);
    /**
     * SansSerif Bold font, size 28.
     */
    public static final Font SANS_BOLD_28 = new Font("SansSerif", Font.BOLD, 28);
    /**
     * SansSerif Bold font, size 36.
     */
    public static final Font SANS_BOLD_36 = new Font("SansSerif", Font.BOLD, 36);
    /**
     * SansSerif Bold font, size 56.
     */
    public static final Font SANS_BOLD_56 = new Font("SansSerif", Font.BOLD, 56);

    /**
     * SansSerif Italic font, size 22.
     */
    public static final Font SANS_ITALIC_22 = new Font("SansSerif", Font.ITALIC, 22);

    /**
     * A triangle shape.
     */
    public static final Shape SHAPE_TRIANGLE;
    /**
     * A diamond shape.
     */
    public static final Shape SHAPE_DIAMOND;
    /**
     * A hexagon shape.
     */
    public static final Shape SHAPE_HEXAGON;
    /**
     * A square shape.
     */
    public static final Shape SHAPE_SQUARE;
    /**
     * A pentagon shape.
     */
    public static final Shape SHAPE_PENTAGON;
    /**
     * Monospaced Bold font, size 65.
     */
    public static final Font MONO_BOLD_65 = new Font("Monospaced", Font.BOLD, 65);
    /**
     * Cached black colors with pre-calculated alpha levels.
     */
    public static final Color[] BLACK_ALPHA = new Color[256];
    /**
     * Cached cyan colors with pre-calculated alpha levels.
     */
    private static final Color[] CYAN_ALPHA = new Color[256];
    /**
     * Cached magenta colors with pre-calculated alpha levels.
     */
    private static final Color[] MAGENTA_ALPHA = new Color[256];
    /**
     * Cached yellow colors with pre-calculated alpha levels.
     */
    private static final Color[] YELLOW_ALPHA = new Color[256];
    /**
     * Cached red colors with pre-calculated alpha levels.
     */
    private static final Color[] RED_ALPHA = new Color[256];
    /**
     * Cached white colors with pre-calculated alpha levels.
     */
    private static final Color[] WHITE_ALPHA = new Color[256];
    /**
     * A simple cache for custom colors with alpha.
     */
    private static final Color[] CUSTOM_COLOR_CACHE = new Color[1024];

    static {
        for (int i = 0; i < 256; i++) {
            CYAN_ALPHA[i] = new Color(0, 255, 255, i);
            MAGENTA_ALPHA[i] = new Color(255, 0, 255, i);
            YELLOW_ALPHA[i] = new Color(255, 215, 0, i);
            RED_ALPHA[i] = new Color(255, 0, 0, i);
            WHITE_ALPHA[i] = new Color(255, 255, 255, i);
            BLACK_ALPHA[i] = new Color(0, 0, 0, i);
        }

        final GeneralPath t = new GeneralPath();
        t.moveTo(0, -1);
        t.lineTo(0.866f, 0.5f);
        t.lineTo(-0.866f, 0.5f);
        t.closePath();
        SHAPE_TRIANGLE = t;

        final GeneralPath d = new GeneralPath();
        d.moveTo(0, -1);
        d.lineTo(0.6f, 0);
        d.lineTo(0, 1);
        d.lineTo(-0.6f, 0);
        d.closePath();
        SHAPE_DIAMOND = d;

        final GeneralPath h = new GeneralPath();
        for (int j = 0; j < 6; j++) {
            final float angle = (float) (Math.PI / 3) * j - (float) (Math.PI / 6);
            final float px = (float) Math.cos(angle) * 0.7f;
            final float py = (float) Math.sin(angle) * 0.7f;
            if (j == 0) h.moveTo(px, py);
            else h.lineTo(px, py);
        }
        h.closePath();
        SHAPE_HEXAGON = h;

        final GeneralPath s = new GeneralPath();
        s.moveTo(-0.7f, -0.7f);
        s.lineTo(0.7f, -0.7f);
        s.lineTo(0.7f, 0.7f);
        s.lineTo(-0.7f, 0.7f);
        s.closePath();
        SHAPE_SQUARE = s;

        final GeneralPath p = new GeneralPath();
        for (int j = 0; j < 5; j++) {
            final float angle = (float) (Math.PI * 2 / 5) * j - (float) (Math.PI / 2);
            final float px = (float) Math.cos(angle) * 0.8f;
            final float py = (float) Math.sin(angle) * 0.8f;
            if (j == 0) p.moveTo(px, py);
            else p.lineTo(px, py);
        }
        p.closePath();
        SHAPE_PENTAGON = p;
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private RenderCache() {
    }

    /**
     * Loads a custom font from the resources directory.
     *
     * @param name the name of the font file
     * @param size the size of the font
     * @return the loaded font, or a fallback font if loading fails
     */
    public static Font loadFont(String name, float size) {
        try (InputStream is = RenderCache.class.getResourceAsStream("/fonts/" + name)) {
            if (is == null) {
                return new Font("SansSerif", Font.BOLD, (int) size);
            }
            return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(size);
        } catch (Exception e) {
            return new Font("SansSerif", Font.BOLD, (int) size);
        }
    }

    /**
     * Returns a cyan color with the specified alpha value.
     *
     * @param alpha the alpha value (0-255)
     * @return the color
     */
    public static Color cyanWithAlpha(int alpha) {
        return CYAN_ALPHA[Math.clamp(alpha, 0, 255)];
    }

    /**
     * Returns a magenta color with the specified alpha value.
     *
     * @param alpha the alpha value (0-255)
     * @return the color
     */
    public static Color magentaWithAlpha(int alpha) {
        return MAGENTA_ALPHA[Math.clamp(alpha, 0, 255)];
    }

    /**
     * Returns a yellow color with the specified alpha value.
     *
     * @param alpha the alpha value (0-255)
     * @return the color
     */
    public static Color yellowWithAlpha(int alpha) {
        return YELLOW_ALPHA[Math.clamp(alpha, 0, 255)];
    }

    /**
     * Returns a red color with the specified alpha value.
     *
     * @param alpha the alpha value (0-255)
     * @return the color
     */
    public static Color redWithAlpha(int alpha) {
        return RED_ALPHA[Math.clamp(alpha, 0, 255)];
    }

    /**
     * Returns a white color with the specified alpha value.
     *
     * @param alpha the alpha value (0-255)
     * @return the color
     */
    public static Color whiteWithAlpha(int alpha) {
        return WHITE_ALPHA[Math.clamp(alpha, 0, 255)];
    }

    /**
     * Returns a black color with the specified alpha value.
     *
     * @param alpha the alpha value (0-255)
     * @return the color
     */
    public static Color blackWithAlpha(int alpha) {
        return BLACK_ALPHA[Math.clamp(alpha, 0, 255)];
    }

    /**
     * Returns a custom color with the specified alpha value.
     *
     * @param color the base color
     * @param alpha the alpha value (0-255)
     * @return the color
     */
    public static Color customColorWithAlpha(Color color, int alpha) {
        alpha = Math.clamp(alpha, 0, 255);
        final int rgb = color.getRGB() & 0x00FFFFFF;
        final int key = (rgb ^ (alpha << 16)) & 1023;
        final Color cached = CUSTOM_COLOR_CACHE[key];
        if (cached != null && cached.getRGB() == (rgb | (alpha << 24))) {
            return cached;
        }
        final Color newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        CUSTOM_COLOR_CACHE[key] = newColor;
        return newColor;
    }
}