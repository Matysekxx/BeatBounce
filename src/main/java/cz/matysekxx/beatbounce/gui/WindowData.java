package cz.matysekxx.beatbounce.gui;

/**
 * A record representing window dimensions.
 *
 * @param width  the width of the window
 * @param height the height of the window
 * @author Matysekxx
 */
public record WindowData(
        int width,
        int height
) {
    /**
     * Factory method to create a new {@code WindowData} instance.
     *
     * @param width  the width of the window
     * @param height the height of the window
     * @return a new {@link WindowData} instance
     */
    public static WindowData of(int width, int height) {
        return new WindowData(width, height);
    }

    /**
     * Calculates the Y-coordinate of the horizon based on the window height.
     *
     * @return the horizon Y-coordinate
     */
    public int horizonY() {
        return height / 3;
    }
}