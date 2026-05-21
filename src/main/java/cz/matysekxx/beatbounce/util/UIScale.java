package cz.matysekxx.beatbounce.util;

import java.awt.*;

/**
 * Utility class for dynamic UI scaling.
 * It provides methods to scale values and fonts based on the current window resolution.
 */
public final class UIScale {

    /**
     * The reference height for scaling (Full HD).
     */
    private static final int REF_HEIGHT = 1080;

    /**
     * Current uniform scale factor (based on height).
     */
    private static float scale = 1.0f;

    private UIScale() {
    }

    /**
     * Updates the current window dimensions and recalculates the scale factor.
     *
     * @param width  the current window width (reserved for future use)
     * @param height the current window height
     */
    public static void update(int width, int height) {
        scale = (float) height / REF_HEIGHT;
        scale = Math.max(scale, 0.5f);
    }

    /**
     * Scales an integer value based on the current scale factor.
     *
     * @param value the base value
     * @return the scaled value
     */
    public static int scale(int value) {
        return Math.round(value * scale);
    }

    /**
     * Scales a float value based on the current scale factor.
     *
     * @param value the base value
     * @return the scaled value
     */
    public static float scale(float value) {
        return value * scale;
    }

    /**
     * Scales a font to a new size based on the current scale factor.
     *
     * @param font the base font
     * @return a new Font instance with the scaled size
     */
    public static Font scaleFont(Font font) {
        return font.deriveFont(font.getSize2D() * scale);
    }

    /**
     * Returns a scaled version of a stroke width.
     *
     * @param width the base stroke width
     * @return a new BasicStroke with the scaled width
     */
    public static BasicStroke scaleStroke(float width) {
        return new BasicStroke(width * scale);
    }

    /**
     * Gets the current scale factor.
     *
     * @return the scale factor
     */
    public static float getScale() {
        return scale;
    }
}
