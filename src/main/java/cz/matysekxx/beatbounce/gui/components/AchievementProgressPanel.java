package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.achievement.Achievement;
import cz.matysekxx.beatbounce.util.UIScale;

import javax.swing.*;
import java.awt.*;


public class AchievementProgressPanel extends JPanel {
    private final Achievement achievement;

    public AchievementProgressPanel(Achievement achievement) {
        this.achievement = achievement;
        this.setOpaque(false);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(UIScale.scale(170), UIScale.scale(38));
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphics2D(g2);

        final int w = getWidth();

        final int barH = UIScale.scale(8);
        final int barX = 0;
        final int barY = UIScale.scale(22);

        g2.setFont(UIScale.scaleFont(RenderCache.MONO_BOLD_12));
        String progressText;
        if (achievement.isCompleted()) {
            progressText = "COMPLETED";
            g2.setColor(achievement.isRewarded() ? new Color(0, 220, 110) : new Color(255, 215, 0));
        } else {
            progressText = achievement.getCurrentProgress() + " / " + achievement.getTarget();
            g2.setColor(new Color(150, 150, 160));
        }
        final FontMetrics fmProgress = g2.getFontMetrics();
        g2.drawString(progressText, w - fmProgress.stringWidth(progressText), UIScale.scale(14));

        g2.setColor(new Color(10, 10, 20, 180));
        g2.fillRoundRect(barX, barY, w, barH, UIScale.scale(8), UIScale.scale(8));
        g2.setColor(new Color(255, 255, 255, 10));
        g2.setStroke(RenderCache.STROKE_1);
        g2.drawRoundRect(barX, barY, w - 1, barH - 1, UIScale.scale(8), UIScale.scale(8));

        final double pct = (double) achievement.getProgressPercentage() / 100.0;
        if (pct > 0) {
            final int fillW = (int) (w * pct);
            if (achievement.isCompleted()) {
                if (achievement.isRewarded()) {
                    g2.setPaint(new LinearGradientPaint(barX, barY, barX + fillW, barY,
                            new float[]{0f, 1f},
                            new Color[]{new Color(0, 180, 80), new Color(0, 255, 110)}));
                } else {
                    g2.setPaint(new LinearGradientPaint(barX, barY, barX + fillW, barY,
                            new float[]{0f, 1f},
                            new Color[]{new Color(220, 150, 0), new Color(255, 215, 0)}));
                }
            } else {
                g2.setPaint(new LinearGradientPaint(barX, barY, barX + fillW, barY,
                        new float[]{0f, 1f},
                        new Color[]{new Color(0, 100, 255), new Color(0, 255, 220)}));
            }
            g2.fillRoundRect(barX, barY, fillW, barH, UIScale.scale(8), UIScale.scale(8));
        }

        g2.dispose();
    }
}
