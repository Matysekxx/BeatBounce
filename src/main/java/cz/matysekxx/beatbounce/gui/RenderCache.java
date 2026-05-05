package cz.matysekxx.beatbounce.gui;

import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 * A cache for rendering resources such as strokes, fonts, and colors with alpha.
 * It also contains predefined shapes for rendering game objects.
 */
public final class RenderCache {
    /** A stroke with width 1.0. */
    public static final BasicStroke STROKE_1 = new BasicStroke(1.0f);
    /** A stroke with width 1.5. */
    public static final BasicStroke STROKE_1_5 = new BasicStroke(1.5f);
    /** A stroke with width 2.0. */
    public static final BasicStroke STROKE_2 = new BasicStroke(2.0f);
    /** A stroke with width 2.5. */
    public static final BasicStroke STROKE_2_5 = new BasicStroke(2.5f);
    /** A stroke with width 3.0. */
    public static final BasicStroke STROKE_3 = new BasicStroke(3.0f);
    /** A stroke with width 3.5. */
    public static final BasicStroke STROKE_3_5 = new BasicStroke(3.5f);
    /** A stroke with width 4.0. */
    public static final BasicStroke STROKE_4 = new BasicStroke(4.0f);
    /** A stroke with width 6.0. */
    public static final BasicStroke STROKE_6 = new BasicStroke(6.0f);
    /** A stroke with width 8.0. */
    public static final BasicStroke STROKE_8 = new BasicStroke(8.0f);

    /** A bold monospaced font of size 11. */
    public static final Font MONO_BOLD_11 = new Font("Monospaced", Font.BOLD, 11);
    /** A bold monospaced font of size 12. */
    public static final Font MONO_BOLD_12 = new Font("Monospaced", Font.BOLD, 12);
    /** A bold monospaced font of size 16. */
    public static final Font MONO_BOLD_16 = new Font("Monospaced", Font.BOLD, 16);
    /** A bold monospaced font of size 20. */
    public static final Font MONO_BOLD_20 = new Font("Monospaced", Font.BOLD, 20);
    /** A bold monospaced font of size 85. */
    public static final Font MONO_BOLD_85 = new Font("Monospaced", Font.BOLD, 85);
    /** A bold monospaced font of size 150. */
    public static final Font MONO_BOLD_150 = new Font("Monospaced", Font.BOLD, 150);

    /** A bold italic monospaced font of size 48. */
    public static final Font MONO_ITALIC_BOLD_48 = new Font("Monospaced", Font.BOLD | Font.ITALIC, 48);
    /** A bold italic monospaced font of size 60. */
    public static final Font MONO_ITALIC_BOLD_60 = new Font("Monospaced", Font.BOLD | Font.ITALIC, 60);
    /** A bold italic monospaced font of size 65. */
    public static final Font MONO_ITALIC_BOLD_65 = new Font("Monospaced", Font.BOLD | Font.ITALIC, 65);
    /** A bold italic monospaced font of size 72. */
    public static final Font MONO_ITALIC_BOLD_72 = new Font("Monospaced", Font.BOLD | Font.ITALIC, 72);
    /** A bold italic monospaced font of size 78. */
    public static final Font MONO_ITALIC_BOLD_78 = new Font("Monospaced", Font.BOLD | Font.ITALIC, 78);
    /** A bold italic monospaced font of size 130. */
    public static final Font MONO_ITALIC_BOLD_130 = new Font("Monospaced", Font.BOLD | Font.ITALIC, 130);
    /** A bold italic monospaced font of size 150. */
    public static final Font MONO_ITALIC_BOLD_150 = new Font("Monospaced", Font.BOLD | Font.ITALIC, 150);

    /** A plain sans-serif font of size 13. */
    public static final Font SANS_PLAIN_13 = new Font("SansSerif", Font.PLAIN, 13);
    /** A plain sans-serif font of size 14. */
    public static final Font SANS_PLAIN_14 = new Font("SansSerif", Font.PLAIN, 14);
    /** A plain sans-serif font of size 20. */
    public static final Font SANS_PLAIN_20 = new Font("SansSerif", Font.PLAIN, 20);

    /** A bold sans-serif font of size 13. */
    public static final Font SANS_BOLD_13 = new Font("SansSerif", Font.BOLD, 13);
    /** A bold sans-serif font of size 14. */
    public static final Font SANS_BOLD_14 = new Font("SansSerif", Font.BOLD, 14);
    /** A bold sans-serif font of size 15. */
    public static final Font SANS_BOLD_15 = new Font("SansSerif", Font.BOLD, 15);
    /** A bold sans-serif font of size 16. */
    public static final Font SANS_BOLD_16 = new Font("SansSerif", Font.BOLD, 16);
    /** A bold sans-serif font of size 28. */
    public static final Font SANS_BOLD_28 = new Font("SansSerif", Font.BOLD, 28);

    /** A triangle shape. */
    public static final Shape SHAPE_TRIANGLE;
    /** A diamond shape. */
    public static final Shape SHAPE_DIAMOND;
    /** A hexagon shape. */
    public static final Shape SHAPE_HEXAGON;

    private static final Color[] CYAN_ALPHA = new Color[256];
    private static final Color[] MAGENTA_ALPHA = new Color[256];
    private static final Color[] YELLOW_ALPHA = new Color[256];
    private static final Color[] RED_ALPHA = new Color[256];
    private static final Color[] WHITE_ALPHA = new Color[256];
    private static final Color[] BLACK_ALPHA = new Color[256];

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
    }

    private RenderCache() {
    }

    /**
     * Returns a cyan color with the specified alpha value.
     *
     * @param alpha the alpha value (0-255)
     * @return the color
     */
    public static Color cyanWithAlpha(int alpha) {
        return CYAN_ALPHA[Math.max(0, Math.min(255, alpha))];
    }

    /**
     * Returns a magenta color with the specified alpha value.
     *
     * @param alpha the alpha value (0-255)
     * @return the color
     */
    public static Color magentaWithAlpha(int alpha) {
        return MAGENTA_ALPHA[Math.max(0, Math.min(255, alpha))];
    }

    /**
     * Returns a yellow color with the specified alpha value.
     *
     * @param alpha the alpha value (0-255)
     * @return the color
     */
    public static Color yellowWithAlpha(int alpha) {
        return YELLOW_ALPHA[Math.max(0, Math.min(255, alpha))];
    }

    /**
     * Returns a red color with the specified alpha value.
     *
     * @param alpha the alpha value (0-255)
     * @return the color
     */
    public static Color redWithAlpha(int alpha) {
        return RED_ALPHA[Math.max(0, Math.min(255, alpha))];
    }

    /**
     * Returns a white color with the specified alpha value.
     *
     * @param alpha the alpha value (0-255)
     * @return the color
     */
    public static Color whiteWithAlpha(int alpha) {
        return WHITE_ALPHA[Math.max(0, Math.min(255, alpha))];
    }

    /**
     * Returns a black color with the specified alpha value.
     *
     * @param alpha the alpha value (0-255)
     * @return the color
     */
    public static Color blackWithAlpha(int alpha) {
        return BLACK_ALPHA[Math.max(0, Math.min(255, alpha))];
    }

    /**
     * Returns a custom color with the specified alpha value.
     *
     * @param color the base color
     * @param alpha the alpha value (0-255)
     * @return the color
     */
    public static Color customColorWithAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alpha)));
    }
}