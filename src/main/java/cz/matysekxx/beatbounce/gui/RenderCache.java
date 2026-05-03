package cz.matysekxx.beatbounce.gui;

import java.awt.*;

public final class RenderCache {
    public static final BasicStroke STROKE_1 = new BasicStroke(1.0f);
    public static final BasicStroke STROKE_1_5 = new BasicStroke(1.5f);
    public static final BasicStroke STROKE_2 = new BasicStroke(2.0f);
    public static final BasicStroke STROKE_2_5 = new BasicStroke(2.5f);
    public static final BasicStroke STROKE_3 = new BasicStroke(3.0f);
    public static final BasicStroke STROKE_3_5 = new BasicStroke(3.5f);
    public static final BasicStroke STROKE_4 = new BasicStroke(4.0f);
    public static final BasicStroke STROKE_6 = new BasicStroke(6.0f);
    public static final BasicStroke STROKE_8 = new BasicStroke(8.0f);

    public static final Font MONO_BOLD_11 = new Font("Monospaced", Font.BOLD, 11);
    public static final Font MONO_BOLD_12 = new Font("Monospaced", Font.BOLD, 12);
    public static final Font MONO_BOLD_16 = new Font("Monospaced", Font.BOLD, 16);
    public static final Font MONO_BOLD_20 = new Font("Monospaced", Font.BOLD, 20);
    public static final Font MONO_BOLD_85 = new Font("Monospaced", Font.BOLD, 85);
    public static final Font MONO_BOLD_150 = new Font("Monospaced", Font.BOLD, 150);

    public static final Font MONO_ITALIC_BOLD_48 = new Font("Monospaced", Font.BOLD | Font.ITALIC, 48);
    public static final Font MONO_ITALIC_BOLD_60 = new Font("Monospaced", Font.BOLD | Font.ITALIC, 60);
    public static final Font MONO_ITALIC_BOLD_65 = new Font("Monospaced", Font.BOLD | Font.ITALIC, 65);
    public static final Font MONO_ITALIC_BOLD_72 = new Font("Monospaced", Font.BOLD | Font.ITALIC, 72);
    public static final Font MONO_ITALIC_BOLD_78 = new Font("Monospaced", Font.BOLD | Font.ITALIC, 78);
    public static final Font MONO_ITALIC_BOLD_130 = new Font("Monospaced", Font.BOLD | Font.ITALIC, 130);

    public static final Font SANS_PLAIN_13 = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font SANS_PLAIN_14 = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font SANS_PLAIN_20 = new Font("SansSerif", Font.PLAIN, 20);

    public static final Font SANS_BOLD_13 = new Font("SansSerif", Font.BOLD, 13);
    public static final Font SANS_BOLD_14 = new Font("SansSerif", Font.BOLD, 14);
    public static final Font SANS_BOLD_15 = new Font("SansSerif", Font.BOLD, 15);
    public static final Font SANS_BOLD_16 = new Font("SansSerif", Font.BOLD, 16);
    public static final Font SANS_BOLD_28 = new Font("SansSerif", Font.BOLD, 28);

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
    }

    public static Color cyanWithAlpha(int alpha) {
        return CYAN_ALPHA[Math.max(0, Math.min(255, alpha))];
    }

    public static Color magentaWithAlpha(int alpha) {
        return MAGENTA_ALPHA[Math.max(0, Math.min(255, alpha))];
    }

    public static Color yellowWithAlpha(int alpha) {
        return YELLOW_ALPHA[Math.max(0, Math.min(255, alpha))];
    }

    public static Color redWithAlpha(int alpha) {
        return RED_ALPHA[Math.max(0, Math.min(255, alpha))];
    }

    public static Color whiteWithAlpha(int alpha) {
        return WHITE_ALPHA[Math.max(0, Math.min(255, alpha))];
    }

    public static Color blackWithAlpha(int alpha) {
        return BLACK_ALPHA[Math.max(0, Math.min(255, alpha))];
    }

    public static Color customColorWithAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alpha)));
    }

    private RenderCache() {
    }
}
