package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.util.UIScale;

import java.awt.*;
import java.util.Random;

/**
 * A visual popup that shows score increases (e.g., "+10").
 * It floats upwards and fades out over time with cyberpunk neon colors.
 * Uses Object Pooling to prevent GC stuttering.
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

    private static final int POOL_SIZE = 100;
    private static final ScorePopup[] pool = new ScorePopup[POOL_SIZE];
    private static int poolCount = 0;

    static {
        for (int i = 0; i < POOL_SIZE; i++) {
            pool[i] = new ScorePopup();
        }
        poolCount = POOL_SIZE;
    }

    private final double duration = 0.8;
    private String text;
    private Color color;
    private double speed;
    private double xOffset;
    private double x;
    private double y;
    private double alpha = 1.0;
    private double elapsed = 0;

    private ScorePopup() {
    }

    /**
     * Retrieves a ScorePopup from the pool or creates a new one if empty.
     */
    public static ScorePopup createRandom(int amount, double startX, double startY) {
        final ScorePopup popup;
        if (poolCount > 0) {
            popup = pool[--poolCount];
        } else {
            popup = new ScorePopup();
        }
        popup.init(amount, startX, startY, NEON_COLORS[RANDOM.nextInt(NEON_COLORS.length)]);
        return popup;
    }

    /**
     * Initializes the popup with new parameters.
     */
    public void init(int amount, double startX, double startY, Color color) {
        this.text = "+" + amount;
        this.x = startX;
        this.y = startY;
        this.color = color;
        this.speed = UIScale.scale(50.0f);
        this.xOffset = (RANDOM.nextDouble() - 0.5) * UIScale.scale(60);
        this.alpha = 1.0;
        this.elapsed = 0;
    }

    /**
     * Returns this popup back to the pool.
     */
    public void returnToPool() {
        if (poolCount < POOL_SIZE) {
            pool[poolCount++] = this;
        }
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

        g2d.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_26));
        int alphaInt = (int) (alpha * 255);

        double drawX = x;
        if (x == 0) {
            drawX = (screenWidth - g2d.getFontMetrics().stringWidth(text)) / 2.0;
        }
        drawX += xOffset;

        g2d.setColor(RenderCache.customColorWithAlpha(color, (int) (alpha * 120)));
        g2d.drawString(text, (int) drawX - UIScale.scale(2), (int) y);
        g2d.drawString(text, (int) drawX + UIScale.scale(2), (int) y);
        g2d.drawString(text, (int) drawX, (int) y - UIScale.scale(2));
        g2d.drawString(text, (int) drawX, (int) y + UIScale.scale(2));

        g2d.setColor(new Color(0, 0, 0, (int) (alpha * 150)));
        g2d.drawString(text, (int) drawX + UIScale.scale(2), (int) y + UIScale.scale(2));

        g2d.setColor(RenderCache.customColorWithAlpha(Color.WHITE, alphaInt));
        g2d.drawString(text, (int) drawX, (int) y);
    }
}
