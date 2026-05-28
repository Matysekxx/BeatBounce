package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.achievement.Achievement;
import cz.matysekxx.beatbounce.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 * A specialized UI component for claiming achievement rewards.
 * It displays different states based on the achievement status:
 * <ul>
 *     <li><b>Locked:</b> Achievement requirements not yet met.</li>
 *     <li><b>Claim:</b> Ready to be collected (interactive).</li>
 *     <li><b>Claimed:</b> Reward already collected (disabled).</li>
 * </ul>
 */
public class AchievementClaimPanel extends JPanel {
    /**
     * The achievement being tracked.
     */
    private final Achievement achievement;

    /**
     * Action to perform when the claim button is clicked.
     */
    private final Runnable onClaimRequested;

    /**
     * Whether the mouse is currently over the claim button.
     */
    private boolean buttonHovered = false;

    /**
     * Constructs a new claim panel.
     *
     * @param achievement      the achievement to manage
     * @param onClaimRequested callback for when the user clicks 'Claim'
     */
    public AchievementClaimPanel(Achievement achievement, Runnable onClaimRequested) {
        this.achievement = achievement;
        this.onClaimRequested = onClaimRequested;

        this.setOpaque(false);

        this.addMouseListener(new MouseAdapter() {
            /**
             * Updates the button hover state and cursor when the mouse enters the panel.
             *
             * @param e the mouse event details
             */
            @Override
            public void mouseEntered(MouseEvent e) {
                if (achievement.isCompleted() && !achievement.isRewarded()) {
                    buttonHovered = true;
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                    repaint();
                }
            }

            /**
             * Resets the button hover state and cursor when the mouse leaves the panel.
             *
             * @param e the mouse event details
             */
            @Override
            public void mouseExited(MouseEvent e) {
                buttonHovered = false;
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                repaint();
            }

            /**
             * Triggers the claim action if the achievement is completed and not yet rewarded.
             *
             * @param e the mouse event details
             */
            @Override
            public void mousePressed(MouseEvent e) {
                if (achievement.isCompleted() && !achievement.isRewarded() && onClaimRequested != null) {
                    onClaimRequested.run();
                }
            }
        });
    }

    /**
     * Returns the preferred size of this panel, accounting for UI scaling.
     *
     * @return the preferred dimension
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(UIScale.scale(140), UIScale.scale(50));
    }

    /**
     * Returns the minimum size of this panel, which is same as the preferred size.
     *
     * @return the minimum dimension
     */
    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /**
     * Returns the maximum size of this panel, which is same as the preferred size.
     *
     * @return the maximum dimension
     */
    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    /**
     * Custom painting for the button state.
     * Uses gradients for active buttons and muted colors for locked/claimed states.
     *
     * @param g the Graphics context
     */
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
