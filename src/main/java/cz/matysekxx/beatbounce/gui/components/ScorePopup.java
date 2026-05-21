package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;

import java.awt.*;
import java.util.Random;

/**
 * A visual popup that shows score increases (e.g., "+10").
 * It floats upwards and fades out over time with cyberpunk neon colors.
 */
public class ScorePopup {
    /**
     * Random generator for offset and color selection.
     */
    private static final Random RANDOM = new Random();

    /**
     * Set of cyberpunk neon colors for the popups.
     */
    private static final Color[] NEON_COLORS = {
            new Color(0, 255, 255),
            new Color(255, 0, 255),
            new Color(57, 255, 20),
            new Color(255, 110, 0),
            new Color(255, 255, 0),
            new Color(255, 0, 100)
    };

    /**
     * The text to display (e.g., "+100").
     */
    private final String text;

    /**
     * The color of the popup's glow.
     */
    private final Color color;

    /**
     * Total lifetime of the popup in seconds.
     */
    private final double duration = 0.8;

    /**
     * Speed at which the popup floats upwards (pixels per second).
     */
    private final double speed = 50.0;

    /**
     * Random horizontal jitter applied to the popup.
     */
    private final double xOffset;

    /**
     * Initial world-space horizontal coordinate.
     */
    private final double x;

    /**
     * Current vertical coordinate.
     */
    private double y;

    /**
     * Current alpha level (0.0 to 1.0).
     */
    private double alpha = 1.0;

    /**
     * Elapsed time since creation in seconds.
     */
    private double elapsed = 0;

    /**
     * Constructs a new ScorePopup.
     *
     * @param amount the score value to display
     * @param startX the starting X-coordinate
     * @param startY the starting Y-coordinate
     * @param color  the glow color
     */
    public ScorePopup(int amount, double startX, double startY, Color color) {
        this.text = "+" + amount;
        this.x = startX;
        this.y = startY;
        this.color = color;
        this.xOffset = (RANDOM.nextDouble() - 0.5) * 60;
    }

    /**
     * Creates a ScorePopup with a random cyberpunk neon color.
     *
     * @param amount the score value to display
     * @param startX the starting X-coordinate
     * @param startY the starting Y-coordinate
     * @return a new ScorePopup instance
     */
    public static ScorePopup createRandom(int amount, double startX, double startY) {
        return new ScorePopup(amount, startX, startY, NEON_COLORS[RANDOM.nextInt(NEON_COLORS.length)]);
    }

    /**
     * Updates the popup's position and alpha based on delta time.
     *
     * @param deltaTime time since last frame in seconds
     */
    public void update(double deltaTime) {
        elapsed += deltaTime;
        y -= speed * deltaTime;
        alpha = Math.max(0, 1.0 - (elapsed / duration));
    }

    /**
     * Returns whether the popup has completed its lifetime and should be removed.
     *
     * @return true if finished, false otherwise
     */
    public boolean isFinished() {
        return elapsed >= duration;
    }

    /**
     * Renders the score popup to the screen.
     *
     * @param g2d         the graphics context
     * @param screenWidth the width of the screen for centering
     */
    public void paint(Graphics2D g2d, int screenWidth) {
        if (alpha <= 0) return;

        g2d.setFont(RenderCache.SANS_BOLD_26);
        int alphaInt = (int) (alpha * 255);

        double drawX = x;
        if (x == 0) {
            drawX = (screenWidth - g2d.getFontMetrics().stringWidth(text)) / 2.0;
        }
        drawX += xOffset;

        g2d.setColor(RenderCache.customColorWithAlpha(color, (int) (alpha * 120)));
        g2d.drawString(text, (int) drawX - 2, (int) y);
        g2d.drawString(text, (int) drawX + 2, (int) y);
        g2d.drawString(text, (int) drawX, (int) y - 2);
        g2d.drawString(text, (int) drawX, (int) y + 2);


        g2d.setColor(new Color(0, 0, 0, (int) (alpha * 150)));
        g2d.drawString(text, (int) drawX + 2, (int) y + 2);

        g2d.setColor(RenderCache.customColorWithAlpha(Color.WHITE, alphaInt));
        g2d.drawString(text, (int) drawX, (int) y);
    }
}
