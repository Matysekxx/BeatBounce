package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;

import java.awt.*;
import java.util.Random;

/**
 * A visual popup that shows score increases (e.g., "+10").
 * It floats upwards and fades out over time with cyberpunk neon colors.
 */
public class ScorePopup {
    private static final Random RANDOM = new Random();
    private static final Color[] NEON_COLORS = {
            new Color(0, 255, 255),
            new Color(255, 0, 255),
            new Color(57, 255, 20),
            new Color(255, 110, 0),
            new Color(255, 255, 0),
            new Color(255, 0, 100)
    };

    private final String text;
    private final Color color;
    private final double duration = 0.8;
    private final double speed = 50.0;
    private final double xOffset;
    private final double x;
    private double y;
    private double alpha = 1.0;
    private double elapsed = 0;

    public ScorePopup(int amount, double startX, double startY, Color color) {
        this.text = "+" + amount;
        this.x = startX;
        this.y = startY;
        this.color = color;
        this.xOffset = (RANDOM.nextDouble() - 0.5) * 60;
    }

    /**
     * Creates a ScorePopup with a random cyberpunk neon color.
     */
    public static ScorePopup createRandom(int amount, double startX, double startY) {
        return new ScorePopup(amount, startX, startY, NEON_COLORS[RANDOM.nextInt(NEON_COLORS.length)]);
    }

    public void update(double deltaTime) {
        elapsed += deltaTime;
        y -= speed * deltaTime;
        alpha = Math.max(0, 1.0 - (elapsed / duration));
    }

    public boolean isFinished() {
        return elapsed >= duration;
    }

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
