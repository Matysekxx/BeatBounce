package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.util.UIScale;

import java.awt.*;
import java.util.Random;

/**
 * A visual popup that shows score increases (e.g., "+10").
 * It floats upwards and fades out over time with cyberpunk neon colors.
 * Uses Object Pooling to prevent GC stuttering.
 *
 * @author Matysekxx
 */
public class ScorePopup {
    /**
     * Random number generator for various visual offsets and color selection.
     */
    private static final Random RANDOM = new Random();

    /**
     * Set of neon colors used for the score popups.
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
     * The maximum size of the score popup pool.
     */
    private static final int POOL_SIZE = 100;

    /**
     * The pool of reusable ScorePopup instances.
     */
    private static final ScorePopup[] pool = new ScorePopup[POOL_SIZE];

    /**
     * The current number of available instances in the pool.
     */
    private static int poolCount = 0;

    static {
        for (int i = 0; i < POOL_SIZE; i++) {
            pool[i] = new ScorePopup();
        }
        poolCount = POOL_SIZE;
    }

    /**
     * The total duration the popup remains visible on screen.
     */
    private final double duration = 0.8;

    /**
     * The text to be displayed (e.g., "+10").
     */
    private String text;

    /**
     * The primary color of the popup text.
     */
    private Color color;

    /**
     * The speed at which the popup moves upwards.
     */
    private double speed;

    /**
     * Horizontal offset for random movement.
     */
    private double xOffset;

    /**
     * The current horizontal position of the popup.
     */
    private double x;

    /**
     * The current vertical position of the popup.
     */
    private double y;

    /**
     * The current transparency level of the popup.
     */
    private double alpha = 1.0;

    /**
     * The time elapsed since the popup was initialized.
     */
    private double elapsed = 0;

    /**
     * Private constructor for ScorePopup, instances should be obtained via {@link #createRandom(int, double, double)}.
     */
    private ScorePopup() {
    }

    /**
     * Retrieves a ScorePopup from the pool or creates a new one if empty.
     *
     * @param amount the score amount to display
     * @param startX the starting x coordinate
     * @param startY the starting y coordinate
     * @return a ScorePopup instance
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
     *
     * @param amount the score amount
     * @param startX the starting x coordinate
     * @param startY the starting y coordinate
     * @param color  the color of the popup
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

    /**
     * Updates the popup's state, moving it upwards and calculating alpha.
     *
     * @param deltaTime elapsed time since last frame in seconds
     */
    public void update(double deltaTime) {
        elapsed += deltaTime;
        y -= speed * deltaTime;
        alpha = Math.max(0, 1.0 - (elapsed / duration));
    }

    /**
     * Checks if the popup animation has finished.
     *
     * @return true if it should be returned to pool
     */
    public boolean isFinished() {
        return elapsed >= duration;
    }

    /**
     * Renders the score popup with glow and shadow effects.
     *
     * @param g2d         the graphics context
     * @param screenWidth the width of the screen (for centering if x is 0)
     */
    public void paint(Graphics2D g2d, int screenWidth) {
        if (alpha <= 0) return;

        g2d.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_26));
        final int alphaInt = (int) (alpha * 255);

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
