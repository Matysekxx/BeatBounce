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
    private final Runnable onClaimRequested;
    private boolean buttonHovered = false;

    public AchievementClaimPanel(Achievement achievement, Runnable onClaimRequested) {
        this.achievement = achievement;
        this.onClaimRequested = onClaimRequested;

        this.setOpaque(false);
        final int btnW = UIScale.scale(140);
        final int btnH = UIScale.scale(50);
        this.setPreferredSize(new Dimension(btnW, btnH));
        this.setMinimumSize(new Dimension(btnW, btnH));
        this.setMaximumSize(new Dimension(btnW, btnH));

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (achievement.isCompleted() && !achievement.isRewarded()) {
                    buttonHovered = true;
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                buttonHovered = false;
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (achievement.isCompleted() && !achievement.isRewarded() && onClaimRequested != null) {
                    onClaimRequested.run();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphics2D(g2);

        final int w = getWidth();
        final int h = getHeight();

        if (achievement.isCompleted()) {
            if (achievement.isRewarded()) {
                g2.setColor(new Color(0, 220, 110, 30));
                g2.fillRoundRect(0, 0, w, h, UIScale.scale(14), UIScale.scale(14));
                g2.setColor(new Color(0, 220, 110, 140));
                g2.setStroke(RenderCache.STROKE_1_5);
                g2.drawRoundRect(0, 0, w - 1, h - 1, UIScale.scale(14), UIScale.scale(14));

                g2.setColor(new Color(0, 220, 110));
                g2.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_18));
                final String claimedTxt = "CLAIMED";
                final FontMetrics fmClaimed = g2.getFontMetrics();
                g2.drawString(claimedTxt, (w - fmClaimed.stringWidth(claimedTxt)) / 2, UIScale.scale(32));
            } else {
                g2.setPaint(new LinearGradientPaint(0, 0, 0, h,
                        new float[]{0f, 1f},
                        buttonHovered ? new Color[]{new Color(255, 235, 100), new Color(255, 180, 0)}
                                : new Color[]{new Color(255, 215, 0), new Color(230, 150, 0)}));
                g2.fillRoundRect(0, 0, w, h, UIScale.scale(14), UIScale.scale(14));
                if (buttonHovered) {
                    g2.setColor(new Color(255, 255, 255, 200));
                    g2.setStroke(RenderCache.STROKE_2);
                    g2.drawRoundRect(0, 0, w - 1, h - 1, UIScale.scale(14), UIScale.scale(14));
                }

                g2.setColor(Color.BLACK);
                g2.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_20));
                final String claimTxt = "CLAIM";
                final FontMetrics fmClaim = g2.getFontMetrics();
                g2.drawString(claimTxt, (w - fmClaim.stringWidth(claimTxt)) / 2, UIScale.scale(32));
            }
        } else {
            g2.setColor(new Color(255, 255, 255, 10));
            g2.fillRoundRect(0, 0, w, h, UIScale.scale(14), UIScale.scale(14));
            g2.setColor(new Color(255, 255, 255, 20));
            g2.setStroke(RenderCache.STROKE_1);
            g2.drawRoundRect(0, 0, w - 1, h - 1, UIScale.scale(14), UIScale.scale(14));

            g2.setColor(new Color(255, 255, 255, 60));
            g2.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_18));
            final String lockedTxt = "LOCKED";
            final FontMetrics fmLocked = g2.getFontMetrics();
            g2.drawString(lockedTxt, (w - fmLocked.stringWidth(lockedTxt)) / 2, UIScale.scale(32));
        }

        g2.dispose();
    }
}
