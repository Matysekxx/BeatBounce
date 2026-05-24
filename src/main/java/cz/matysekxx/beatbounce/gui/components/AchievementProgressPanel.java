package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.achievement.Achievement;
import cz.matysekxx.beatbounce.util.UIScale;
import javax.swing.*;
import java.awt.*;

public class AchievementProgressPanel extends JPanel {

    public AchievementProgressPanel(Achievement achievement) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setAlignmentX(Component.RIGHT_ALIGNMENT);

        final JLabel progressLabel = new JLabel();
        progressLabel.setFont(UIScale.scaleFont(RenderCache.MONO_BOLD_11));
        progressLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        if (achievement.isCompleted()) {
            progressLabel.setText("COMPLETED");
            progressLabel.setForeground(RenderUtils.cyan);
        } else {
            progressLabel.setText(achievement.getCurrentProgress() + " / " + achievement.getTarget());
            progressLabel.setForeground(new Color(130, 130, 135));
        }

        final JPanel customProgressBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                RenderUtils.initGraphics2D(g2);

                final int w = getWidth();
                final int h = getHeight();
                final int arc = UIScale.scale(6);
                g2.setColor(new Color(10, 10, 20, 180));
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                g2.setColor(new Color(255, 255, 255, 10));
                g2.setStroke(RenderCache.STROKE_1);
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

                final double pct = (double) achievement.getProgressPercentage() / 100.0;
                if (pct > 0) {
                    final int fillW = (int) (w * pct);
                    g2.setPaint(new LinearGradientPaint(0, 0, fillW, 0,
                            new float[]{0f, 1f},
                            new Color[]{new Color(0, 100, 255), new Color(0, 255, 220)}));
                    g2.fillRoundRect(0, 0, fillW, h, arc, arc);
                }
                g2.dispose();
            }
        };
        customProgressBar.setOpaque(false);
        final int barW = UIScale.scale(170);
        final int barH = UIScale.scale(6);
        customProgressBar.setPreferredSize(new Dimension(barW, barH));
        customProgressBar.setMinimumSize(new Dimension(barW, barH));
        customProgressBar.setMaximumSize(new Dimension(barW, barH));
        customProgressBar.setAlignmentX(Component.RIGHT_ALIGNMENT);

        add(progressLabel);
        add(Box.createRigidArea(new Dimension(0, UIScale.scale(5))));
        add(customProgressBar);
    }
}
