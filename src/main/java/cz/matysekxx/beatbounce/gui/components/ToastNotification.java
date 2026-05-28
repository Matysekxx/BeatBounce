package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.achievement.Achievement;
import cz.matysekxx.beatbounce.util.UIScale;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * A notification popup that appears when an achievement is unlocked.
 * It animates onto the screen from the right, stays for a duration, and then fades out.
 */
public class ToastNotification {
    /**
     * Total time the notification stays fully visible.
     */
    private static final float DURATION = 3.5f;

    /**
     * Time taken for fade-in and fade-out animations.
     */
    private static final float FADE_TIME = 0.5f;

    /**
     * Background color for the toast notification.
     */
    private static final Color BG_COLOR = new Color(15, 15, 25, 230);

    /**
     * Border color for the toast notification.
     */
    private static final Color BORDER_COLOR = new Color(255, 200, 0, 180);

    /**
     * Primary text color for the notification content.
     */
    private static final Color TEXT_COLOR = new Color(255, 255, 255);

    /**
     * Color for the notification title.
     */
    private static final Color TITLE_COLOR = new Color(255, 215, 0);

    /**
     * The achievement associated with this notification.
     */
    private final Achievement achievement;

    /**
     * Internal timer to track animation progress.
     */
    private float timer = 0f;

    /**
     * Constructs a new ToastNotification for the given achievement.
     *
     * @param achievement the unlocked achievement to display
     */
    public ToastNotification(Achievement achievement) {
        this.achievement = achievement;
    }

    /**
     * Updates the animation timer.
     *
     * @param dt elapsed time since last frame in seconds
     */
    public void update(float dt) {
        timer += dt;
    }

    /**
     * Checks if the notification animation has completed.
     *
     * @return true if the notification should be removed
     */
    public boolean isFinished() {
        return timer > (DURATION + FADE_TIME * 2);
    }

    /**
     * Renders the notification on the graphics context.
     *
     * @param g2d    the graphics context
     * @param width  the width of the screen/panel
     * @param index  the vertical index (for stacking multiple notifications)
     */
    public void draw(Graphics2D g2d, int width, int index) {
        final float alpha = calculateAlpha();
        if (alpha <= 0) return;

        final int toastW = UIScale.scale(320);
        final int toastH = UIScale.scale(80);
        final int spacing = UIScale.scale(10);

        final int toastX = calculateX(width, toastW, alpha);
        final int toastY = UIScale.scale(20) + (index * (toastH + spacing));

        final Composite oldComp = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        drawBackground(g2d, toastX, toastY, toastW, toastH);

        final int iconSize = UIScale.scale(40);
        final int iconX = toastX + UIScale.scale(20);
        final int iconY = toastY + (toastH - iconSize) / 2;

        drawIcon(g2d, iconX, iconY, iconSize);
        drawText(g2d, toastY, toastW, iconX, iconSize);

        g2d.setComposite(oldComp);
    }

    /**
     * Calculates the current alpha transparency based on the timer.
     *
     * @return the calculated alpha value between 0.0 and 1.0
     */
    private float calculateAlpha() {
        float alpha = 1f;
        if (timer < FADE_TIME) {
            alpha = timer / FADE_TIME;
        } else if (timer > DURATION + FADE_TIME) {
            alpha = 1f - ((timer - (DURATION + FADE_TIME)) / FADE_TIME);
        }
        return Math.clamp(alpha, 0f, 1f);
    }

    /**
     * Calculates the horizontal position for the sliding animation.
     *
     * @param width  the width of the container
     * @param toastW the width of the toast
     * @param alpha  the current alpha value
     * @return the calculated horizontal position
     */
    private int calculateX(int width, int toastW, float alpha) {
        int x = width - toastW - UIScale.scale(20);
        if (timer < FADE_TIME) {
            x += (int) (toastW * (1f - alpha));
        }
        return x;
    }

    /**
     * Draws the rounded background and border.
     *
     * @param g2d the Graphics2D context
     * @param x   the horizontal position
     * @param y   the vertical position
     * @param w   the width
     * @param h   the height
     */
    private void drawBackground(Graphics2D g2d, int x, int y, int w, int h) {
        final RoundRectangle2D.Float rect = new RoundRectangle2D.Float(x, y, w, h, UIScale.scale(15), UIScale.scale(15));
        g2d.setColor(BG_COLOR);
        g2d.fill(rect);
        g2d.setColor(BORDER_COLOR);
        g2d.setStroke(RenderCache.STROKE_2);
        g2d.draw(rect);
    }

    /**
     * Draws the decorative icon.
     *
     * @param g2d  the Graphics2D context
     * @param x    the horizontal position
     * @param y    the vertical position
     * @param size the size of the icon
     */
    private void drawIcon(Graphics2D g2d, int x, int y, int size) {
        g2d.setColor(new Color(255, 215, 0, 50));
        g2d.fillOval(x, y, size, size);
        g2d.setColor(TITLE_COLOR);
        g2d.setStroke(RenderCache.STROKE_1);
        g2d.drawOval(x, y, size, size);
        g2d.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_20));
        RenderUtils.drawText(g2d, "★", x + UIScale.scale(11), y + UIScale.scale(28), TITLE_COLOR);
    }

    /**
     * Draws the text content with ellipsis if necessary.
     *
     * @param g2d      the Graphics2D context
     * @param toastY   the vertical position of the toast
     * @param toastW   the width of the toast
     * @param iconX    the horizontal position of the icon
     * @param iconSize the size of the icon
     */
    private void drawText(Graphics2D g2d, int toastY, int toastW, int iconX, int iconSize) {
        final int textX = iconX + iconSize + UIScale.scale(15);
        final int textY = toastY + UIScale.scale(30);

        g2d.setFont(UIScale.scaleFont(RenderCache.MONO_BOLD_16));
        g2d.setColor(TITLE_COLOR);
        g2d.drawString("Achievement Unlocked!", textX, textY);

        g2d.setFont(UIScale.scaleFont(RenderCache.SANS_PLAIN_16));
        g2d.setColor(TEXT_COLOR);

        String title = achievement.getTitle();
        final FontMetrics fm = g2d.getFontMetrics();
        final int maxWidth = toastW - iconSize - UIScale.scale(60);

        if (fm.stringWidth(title) > maxWidth) {
            while (!title.isEmpty() && fm.stringWidth(title + "...") > maxWidth) {
                title = title.substring(0, title.length() - 1);
            }
            title += "...";
        }
        g2d.drawString(title, textX, textY + UIScale.scale(25));
    }
}
