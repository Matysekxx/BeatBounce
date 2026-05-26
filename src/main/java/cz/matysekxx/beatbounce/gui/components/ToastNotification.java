package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.achievement.Achievement;
import cz.matysekxx.beatbounce.util.UIScale;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ToastNotification {
    private static final float DURATION = 3.5f;
    private static final float FADE_TIME = 0.5f;
    private static final Color BG_COLOR = new Color(15, 15, 25, 230);
    private static final Color BORDER_COLOR = new Color(255, 200, 0, 180);
    private static final Color TEXT_COLOR = new Color(255, 255, 255);
    private static final Color TITLE_COLOR = new Color(255, 215, 0);

    private final Achievement achievement;
    private float timer = 0f;

    public ToastNotification(Achievement achievement) {
        this.achievement = achievement;
    }

    public void update(float dt) {
        timer += dt;
    }

    public boolean isFinished() {
        return timer > (DURATION + FADE_TIME * 2);
    }

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

    private float calculateAlpha() {
        float alpha = 1f;
        if (timer < FADE_TIME) {
            alpha = timer / FADE_TIME;
        } else if (timer > DURATION + FADE_TIME) {
            alpha = 1f - ((timer - (DURATION + FADE_TIME)) / FADE_TIME);
        }
        return Math.clamp(alpha, 0f, 1f);
    }

    private int calculateX(int width, int toastW, float alpha) {
        int x = width - toastW - UIScale.scale(20);
        if (timer < FADE_TIME) {
            x += (int) (toastW * (1f - alpha));
        }
        return x;
    }

    private void drawBackground(Graphics2D g2d, int x, int y, int w, int h) {
        final RoundRectangle2D.Float rect = new RoundRectangle2D.Float(x, y, w, h, UIScale.scale(15), UIScale.scale(15));
        g2d.setColor(BG_COLOR);
        g2d.fill(rect);
        g2d.setColor(BORDER_COLOR);
        g2d.setStroke(RenderCache.STROKE_2);
        g2d.draw(rect);
    }

    private void drawIcon(Graphics2D g2d, int x, int y, int size) {
        g2d.setColor(new Color(255, 215, 0, 50));
        g2d.fillOval(x, y, size, size);
        g2d.setColor(TITLE_COLOR);
        g2d.setStroke(RenderCache.STROKE_1);
        g2d.drawOval(x, y, size, size);
        g2d.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_20));
        RenderUtils.drawText(g2d, "★", x + UIScale.scale(11), y + UIScale.scale(28), TITLE_COLOR);
    }

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