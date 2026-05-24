package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.achievement.Achievement;
import cz.matysekxx.beatbounce.util.UIScale;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AchievementClaimPanel extends JPanel {
    private final Achievement achievement;
    private boolean buttonHovered = false;

    public AchievementClaimPanel(Achievement achievement, Runnable onClaimRequest) {
        this.achievement = achievement;
        setOpaque(false);
        final int size = UIScale.scale(26);
        setPreferredSize(new Dimension(size, size));
        setMinimumSize(new Dimension(size, size));
        setMaximumSize(new Dimension(size, size));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (achievement.isCompleted() && !achievement.isRewarded()) {
                    buttonHovered = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    repaint();
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                buttonHovered = false;
                setCursor(Cursor.getDefaultCursor());
                repaint();
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if (achievement.isCompleted() && !achievement.isRewarded() && onClaimRequest != null) {
                    onClaimRequest.run();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphics2D(g2);
        final int size = getWidth();
        final int cx = size / 2;
        final int cy = size / 2;

        if (achievement.isCompleted()) {
            if (achievement.isRewarded()) {
                drawCheckmark(g2, cx, cy);
            } else {
                drawClaimButton(g2, size, cx, cy);
            }
        } else {
            drawLock(g2, cx, cy);
        }
        g2.dispose();
    }

    private void drawCheckmark(Graphics2D g2, int cx, int cy) {
        g2.setColor(new Color(0, 220, 110));
        g2.setStroke(RenderCache.STROKE_2);
        g2.drawLine(cx - UIScale.scale(4), cy + UIScale.scale(1), cx - UIScale.scale(1), cy + UIScale.scale(4));
        g2.drawLine(cx - UIScale.scale(1), cy + UIScale.scale(4), cx + UIScale.scale(5), cy - UIScale.scale(3));
    }

    private void drawClaimButton(Graphics2D g2, int size, int cx, int cy) {
        final int br = UIScale.scale(5);
        final LinearGradientPaint btnGrad = new LinearGradientPaint(0, 0, size, size,
                new float[]{0f, 1f},
                buttonHovered ? new Color[]{new Color(255, 200, 0, 60), new Color(255, 160, 0, 40)}
                        : new Color[]{new Color(255, 190, 0, 25), new Color(255, 140, 0, 15)});
        g2.setPaint(btnGrad);
        g2.fillRoundRect(0, 0, size, size, br, br);
        g2.setColor(buttonHovered ? new Color(255, 200, 0, 180) : new Color(255, 190, 0, 110));
        g2.setStroke(RenderCache.STROKE_1);
        g2.drawRoundRect(0, 0, size - 1, size - 1, br, br);
        final int len = UIScale.scale(4);
        g2.setColor(new Color(255, 200, 0));
        g2.setStroke(RenderCache.STROKE_2);
        g2.drawLine(cx - len, cy, cx + len, cy);
        g2.drawLine(cx, cy - len, cx, cy + len);
    }

    private void drawLock(Graphics2D g2, int cx, int cy) {
        final int lw = UIScale.scale(10);
        final int lh = UIScale.scale(8);
        final int lockY = cy + UIScale.scale(1);
        g2.setColor(new Color(255, 255, 255, 80));
        g2.fillRoundRect(cx - lw / 2, lockY - lh / 2, lw, lh, UIScale.scale(1), UIScale.scale(1));
        g2.setStroke(RenderCache.STROKE_1);
        g2.drawArc(cx - lw / 2 + UIScale.scale(1), lockY - lh / 2 - UIScale.scale(4), lw - UIScale.scale(2), UIScale.scale(8), 0, 180);
    }
}
