package cz.matysekxx.beatbounce.gui;

public record WindowData(
        int width,
        int height
) {
    public static WindowData of(int width, int height) {
        return new WindowData(width, height);
    }
}