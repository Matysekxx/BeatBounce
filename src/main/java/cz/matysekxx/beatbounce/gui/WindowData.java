package cz.matysekxx.beatbounce.gui;

/**
 * A record representing window dimensions.
 *
 * @param width  the width of the window
 * @param height the height of the window
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
}